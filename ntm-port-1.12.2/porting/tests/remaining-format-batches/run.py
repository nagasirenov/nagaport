#!/usr/bin/env python3
"""Actual original/native extracted closed streams and finite driver fixture."""
import argparse,hashlib,os,pathlib,re,subprocess,tempfile,zipfile,json
import check_source as source
P=pathlib.Path
p=argparse.ArgumentParser();p.add_argument('--source-only',action='store_true');p.add_argument('--java-home',type=P);p.add_argument('--classpath');p.add_argument('--legacy-client',type=P);a=p.parse_args()
source.validate()
for rel,digest in json.loads((source.HERE/'support-hashes.json').read_text()).items():assert hashlib.sha256((source.ROOT/rel).read_bytes()).hexdigest()==digest,('support changed',rel)
if a.source_only:raise SystemExit(0)
assert a.java_home and a.classpath and a.legacy_client
expected=json.loads((source.HERE/'engine-hashes.json').read_text());actual={}
for jar in a.classpath.split(os.pathsep):
 if not zipfile.is_zipfile(jar):continue
 with zipfile.ZipFile(jar) as z:
  for name in expected:
   if name in z.namelist():assert name not in actual;actual[name]=hashlib.sha256(z.read(name)).hexdigest()
assert actual==expected,'Native engine differs from retained proof'
assert hashlib.sha256(a.legacy_client.read_bytes()).hexdigest()=='a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce'

def close_brace(text,start):
 clean=source.transform.scrub(text);i=clean.index('{',start)+1;depth=1
 while depth:depth+=(clean[i]=='{')-(clean[i]=='}');i+=1
 return i

def strip_owner(text,name):
 text=re.sub(r'(?:final )?Tessellator '+name+r' = Tessellator.(?:instance|getInstance\(\));','',text)
 text=re.sub(r'(?:final )?BufferBuilder '+name+r'Buffer = '+name+r'.getBuffer\(\);','',text)
 return text

def extract(body,stem,method,name):
 if stem=='RenderNTMSkyboxImpact' and method=='renderStars':return body[body.index('{')+1:body.rindex('}')]
 if stem=='RenderNTMSkyboxImpact' and method=='initializeDisplayLists':return body[body.index('{')+1:body.rindex('}')]
 if stem=='RenderNTMSkyboxImpact':
  start=body.index('GL11.glEnable(GL11.GL_TEXTURE_2D);',body.index('GL11.glShadeModel(GL11.GL_FLAT);'))
  end=close_brace(body,body.index('if(d0 < 0.0D)'))
  s=body[start:end]
  replacements={
   'mc.renderEngine.bindTexture(this.sunTexture);':'GL11.glBindTexture(GL11.GL_TEXTURE_2D, textures[0]);',
   'FMLClientHandler.instance().getClient().renderEngine.bindTexture(RenderNTMSkyboxImpact.moonTexture);':'GL11.glBindTexture(GL11.GL_TEXTURE_2D, textures[1]);',
   'FMLClientHandler.instance().getClient().renderEngine.bindTexture(digammaStar);':'GL11.glBindTexture(GL11.GL_TEXTURE_2D, textures[0]);',
   'FMLClientHandler.instance().getClient().renderEngine.bindTexture(bobmazonSat);':'GL11.glBindTexture(GL11.GL_TEXTURE_2D, textures[1]);',
   'world.getCelestialAngle(partialTicks)':'celestialAngle',
   'FMLClientHandler.instance().getClient().world.getMoonPhase()':'moonPhase',
   'HbmLivingProps.getDigamma(Minecraft.getMinecraft().player)':'playerDigamma',
   'mc.player.getPosition(partialTicks).y - world.getHorizon()':'playerY - horizon',
   'GL11.glCallList(this.glSkyList2);':'GL11.glCallList(boundarySkyList);',
  }
  for old,new in replacements.items():assert old in s;s=s.replace(old,new)
  assert s.count('System.currentTimeMillis()')==2
  s=s.replace('System.currentTimeMillis()','clockFirst',1).replace('System.currentTimeMillis()','clockSecond',1)
  return s
 if stem=='SkyProviderLaytheSunset':
  start=body.index('byte segments');end=body.rindex('GL11.glEnable(GL11.GL_TEXTURE_2D);')+len('GL11.glEnable(GL11.GL_TEXTURE_2D);')
  return body[start:end].replace('world.getCelestialAngleRadians(partialTicks)','celestialRadians')
 if stem=='LoadingScreenRendererNT':
  start=body.index('float f = 32.0F;');end=close_brace(body,body.index('if(p_73718_1_ >= 0)',start));return body[start:end]
 if stem=='GaugeUtil' and method=='renderGauge':
  start=body.index('int frameNum');end=body.rindex(name+'.draw();')+len(name+'.draw();')
  s=body[start:end]
  for field in ['count','height','width']:s=s.replace('gauge.'+field,'gauge'+field.title())
  return s
 if stem in ['GaugeUtil','RenderBobble']:return body[body.index('{')+1:body.rindex('}')]
 if stem=='RenderDeathBlast':
  start=body.index('GL11.glPushMatrix();');end=body.index('renderOrb(entity,');return body[start:end]
 if stem=='GUIMachineRadarNT':
  start=body.index('if(radar.showMap) {');end=body.index('if(!radar.entries.isEmpty())')
  s=body[start:end]
  for field in ['showMap','map','prevRotation','rotation']:s=s.replace('radar.'+field,field)
  return s
 raise AssertionError(stem)

fixture={
 ('RenderNTMSkyboxImpact','render'):('float f7,f8,f9,f10;float rain=new float[]{0F,.1F,.5F,1F,-.2F,2F}[caseIndex],dust=1-rain,celestialAngle=caseIndex*.19F,moonPhase=caseIndex+2,playerDigamma=caseIndex*3.5F;double playerY=new double[]{0,1,-10,128,1.25,-30}[caseIndex],horizon=1;long clockFirst=new long[]{0,1,999,360001,12345678,-7}[caseIndex],clockSecond=clockFirst+17;int boundarySkyList=GLAllocation.generateDisplayLists(1);GL11.glNewList(boundarySkyList,GL11.GL_COMPILE);GL11.glEndList();','5+(playerY-horizon<0?1:0)','0','GL11.glDeleteLists(boundarySkyList,1);'),
 ('SkyProviderLaytheSunset','renderSunset'):('float[] sunsetColor=new float[]{.2F+caseIndex*.2F,.6F,.9F,new float[]{0F,.25F,1F,2F,-1F,Float.NaN}[caseIndex]},anaglyphColor=new float[]{.9F,.2F+caseIndex*.1F,.5F,1F};float celestialRadians=caseIndex*1.5F;','3','0',''),
 ('LoadingScreenRendererNT','setLoadingProgress'):('int width=new int[]{1,320,640,1920,3840,-2}[caseIndex],height=new int[]{1,200,360,1080,2160,-3}[caseIndex],p_73718_1_=new int[]{-1,0,1,50,100,175}[caseIndex];','1+(p_73718_1_>=0?1:0)','0',''),
 ('GaugeUtil','renderGauge'):('double x=caseIndex*.5,y=-caseIndex*1.25,z=-90.125,progress=new double[]{-1,0,.5,1,2,Double.NaN}[caseIndex];int gaugeCount=new int[]{1,2,3,16,32,0}[caseIndex],gaugeWidth=16+caseIndex,gaugeHeight=32+caseIndex;','1','0',''),
 ('GaugeUtil','drawSmoothGauge:int colorOuter'):('int x=caseIndex*13,y=-caseIndex*5,color=new int[]{0,0x123456,0xFF804020,-1,Integer.MIN_VALUE,Integer.MAX_VALUE}[caseIndex],colorOuter=~color;double z=-90.125,progress=new double[]{-1,0,.5,1,2,Double.NaN}[caseIndex],tipLength=caseIndex*3,backLength=caseIndex*.5,backSide=caseIndex*.25;','1','0',''),
 ('RenderBobble','renderOrigin'):('','1','0',''),
 ('RenderDeathBlast','doRender'):('double x=caseIndex*.5,y=-caseIndex*1.25,z=caseIndex*17.25;','16','0',''),
 ('GUIMachineRadarNT','drawGuiContainerBackgroundLayer'):('boolean showMap=caseIndex!=0;byte[] map=new byte[40000];for(int k=0;k<map.length;k++){if(caseIndex==2)map[k]=(byte)-1;else if(caseIndex==3&&k%997==0)map[k]=(byte)(k%127+1);else if(caseIndex==4)map[k]=(byte)(k%127+1);else if(caseIndex==5)map[k]=(byte)(k%256-128);}int guiLeft=caseIndex*7,guiTop=-caseIndex*3;float prevRotation=caseIndex*91.25F,rotation=prevRotation+360.25F,f=new float[]{0,.125F,.5F,1,2,-1}[caseIndex];','1+(showMap?1:0)','0',''),
}

def endpoint_replace(body,name,native):
 # The same static begin/draw grammar derives one exact attribute mask per site.
 clean=source.transform.scrub(body);tokens=list(re.finditer(r'\b'+name+('Buffer' if native else '')+r'\.(begin|startDrawingQuads|startDrawing)\([^;]*\);|\b'+name+r'\.draw\(\);',clean));edits=[];attributes=None
 for i,t in enumerate(tokens):
  if t[0].endswith('.draw();'):
   assert attributes is not None
   edits.append((t.start(),t.end(),('captureNative(owner,nativeRecords,' if native else 'captureOld(old,oldRecords,')+str(attributes)+');'));attributes=None
  else:
   assert attributes is None
   end=tokens[i+1].start();assert tokens[i+1][0].endswith('.draw();');batch=clean[t.end():end]
   attributes=(int('.tex(' in batch) if native else int('.addVertexWithUV(' in batch))+(2*int('.color(' in batch) if native else 2*int('.setColor' in batch))
   if native:assert ('DefaultVertexFormats.POSITION'+('_TEX' if attributes&1 else '')+('_COLOR' if attributes&2 else '')) in t[0]
 assert attributes is None
 for start,end,s in reversed(edits):body=body[:start]+s+body[end:]
 return strip_owner(body,name).replace(name+'Buffer.','buffer.') if native else strip_owner(body,name).replace(name+'.','old.')

def make_method(number,label,env,old,native,name,expected_draws,expected_rng,cleanup,display=False):
 native=endpoint_replace(native,name,True);old=endpoint_replace(old,name,False)
 native=native.replace('Random random = new Random(','CountRandom random = new CountRandom(');old=old.replace('Random random = new Random(','CountRandom random = new CountRandom(')
 if display:
  native=native.replace('this.','');old=old.replace('this.','')
  env='int starGLCallList=0,glSkyList=0,glSkyList2=0;boolean displayListsInitialized=false;int[][] nativeReplay,oldReplay;'
  afterNative='if(!displayListsInitialized)throw new AssertionError("init flag");nativeReplay=replayLists(starGLCallList,caseIndex);GL11.glDeleteLists(starGLCallList,3);displayListsInitialized=false;'
  afterOld='if(!displayListsInitialized)throw new AssertionError("init flag");oldReplay=replayLists(starGLCallList,caseIndex);GL11.glDeleteLists(starGLCallList,3);if(!Arrays.deepEquals(nativeReplay,oldReplay))throw new AssertionError("list replay state");'
 else:afterNative=afterOld=''
 return '''void batch%d(int caseIndex,double translation)throws Exception{
 List<Record> nativeRecords=new ArrayList<>(),oldRecords=new ArrayList<>();int nc,oc;int[] nativeFinal,oldFinal;%s
 reset(caseIndex);randomCalls=0;{Tessellator owner=Tessellator.getInstance();BufferBuilder buffer=owner.getBuffer();buffer.setTranslation(translation,-translation,translation*.25);
 %s
 nc=randomCalls;nativeFinal=state();%s}
 reset(caseIndex);randomCalls=0;{Legacy old=new Legacy(translation);
 %s
 oc=randomCalls;oldFinal=state();%s}
 compare(nativeRecords,oldRecords,nc,oc,%s,%s,nativeFinal,oldFinal,"%s");%s
 }'''%(number,env,native,afterNative,old,afterOld,expected_rng,expected_draws,label,cleanup)
methods=[];calls=[]
for rel,r in source.FILES.items():
 current=(source.ROOT/rel).read_text();old=source.reverse_source(rel,current);stem=P(rel).stem
 for method,record in r['scopes'].items():
  if method=='renderStars':continue # Included, in order, inside actual display-list compilation.
  name=record['name'];aa,bb=source.transform.method_span(old,method);ob=extract(old[aa:bb],stem,method,name);aa,bb=source.transform.method_span(current,method);nb=extract(current[aa:bb],stem,method,name)
  display=method=='initializeDisplayLists'
  if display:
   for isNative,text in [(False,old),(True,current)]:
    aa,bb=source.transform.method_span(text,'renderStars');stars=strip_owner(extract(text[aa:bb],stem,'renderStars',name),name)
    if isNative:nb=nb.replace('this.renderStars();','{'+stars+'}')
    else:ob=ob.replace('this.renderStars();','{'+stars+'}')
   env,expected_draws,expected_rng,cleanup='','171','6780',''
  else:env,expected_draws,expected_rng,cleanup=fixture[(stem,method)]
  number=len(methods);methods.append(make_method(number,stem+'.'+method,env,ob,nb,name,expected_draws,expected_rng,cleanup,display));calls.append('test.batch%d(seed,translation);'%number)
assert len(methods)==9
java=(source.HERE/'NativeFormatTest.java.in').read_text().replace('/*METHODS*/','\n'.join(methods)).replace('/*CALLS*/','\n'.join(calls))
for k,v in [('EXECUTIONS',162),('BATCHES',3639),('VERTICES',802272),('DRAWS',7266),('EMPTY',12)]:java=java.replace('/*EXPECT_'+k+'*/',str(v))
with tempfile.TemporaryDirectory(prefix='remaining-format-native-') as td:
 tmp=P(td);file=tmp/'NativeFormatTest.java';file.write_text(java)
 for jar in a.classpath.split(os.pathsep):
  if 'lwjgl-platform' in jar and jar.endswith('natives-linux.jar'):
   with zipfile.ZipFile(jar) as z:
    for name in z.namelist():
     if name.endswith('.so') and '/' not in name:(tmp/name).write_bytes(z.read(name))
 support=[source.ROOT/'porting/tests/MaterialTextureAtlasSpriteNativeTest.java',source.ROOT/'src/main/java/com/hbm/render/item/nativeitem/MaterialTextureAtlasSprite.java',source.ROOT/'src/main/java/com/hbm/util/Vec3NT.java']
 subprocess.run([str(a.java_home/'bin/javac'),'-encoding','UTF-8','-source','8','-target','8','-cp',a.classpath,'-d',str(tmp),str(file)]+list(map(str,support)),check=True)
 subprocess.run([str(a.java_home/'bin/java'),'-Djava.awt.headless=true','-Djava.library.path='+str(tmp),'-cp',str(tmp)+os.pathsep+a.classpath,'NativeFormatTest',str(a.legacy_client)],cwd=tmp,env=dict(os.environ,MESA_SHADER_CACHE_DISABLE='true'),check=True)
