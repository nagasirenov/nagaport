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
 if stem=='RenderExcavator':
  start=body.index('bindTexture(cobble);');end=body.rindex(name+'.draw();')+len(name+'.draw();')
  s=body[start:end].replace('drill.enableCrusher','enableCrusher').replace('System.currentTimeMillis()','clockTime').replace('bindTexture(cobble);','GL11.glBindTexture(GL11.GL_TEXTURE_2D,textures[0]);').replace('bindTexture(smoosh ? gravel : cobble);','GL11.glBindTexture(GL11.GL_TEXTURE_2D,textures[smoosh?1:0]);')
  return s
 if stem=='RenderChemical':
  s=body[body.index('{')+1:body.rindex('}')]
  replacements={'chem.getMaxAge()':'maxAge','chem.getEntityId()':'entityId','chem.getType().getColor()':'cloudColor','this.renderManager.playerViewY':'playerViewY','this.renderManager.playerViewX':'playerViewX'}
  for field in ['ticksExisted','prevRotationYaw','rotationYaw','prevRotationPitch','rotationPitch','motionX','motionY','motionZ']:replacements['chem.'+field]=field
  for old,new in replacements.items():s=s.replace(old,new)
  return s
 if stem=='RenderRadarScreen':
  start=body.index('if(screen.linked)');end=close_brace(body,body.index('} else {',start)+2)
  s=body[start:end]
  for old,new in {'screen.linked':'linked','screen.entries':'entries','screen.refX':'refX','screen.refZ':'refZ','screen.range':'range','tileEntity.getWorld().getTotalWorldTime()':'worldTime','tileEntity.getWorld().rand':'rand'}.items():s=s.replace(old,new)
  return s.replace('RadarEntry entry','int[] entry').replace('entry.posX','entry[0]').replace('entry.posZ','entry[1]').replace('entry.blipLevel','entry[2]')
 if stem.startswith('SkyProvider'):
  s=body[body.index('{')+1:body.rindex('}')]
  return s.replace('RenderManager.instance.playerViewY','playerViewY').replace('RenderManager.instance.playerViewX','playerViewX').replace('TextureManager tex = Minecraft.getMinecraft().getTextureManager();','').replace('Minecraft.getMinecraft().player.world.getRainStrength(partialTicks)','rainStrength').replace('tex.bindTexture(loc1);','GL11.glBindTexture(GL11.GL_TEXTURE_2D,textures[caseIndex%2]);')
 if stem=='ItemRenderAberrator':
  return body[body.index('GL11.glPushMatrix();',body.index('Tessellator tess =')):body.rindex('}')]
 raise AssertionError(stem)

fixture={
 'RenderExcavator':('boolean enableCrusher=caseIndex%2==0;long clockTime=new long[]{0,1,249,250,123456789,-1000}[caseIndex];','2','0'),
 'RenderChemical':('int ticksExisted=new int[]{0,1,50,100,250,-1}[caseIndex],maxAge=new int[]{100,100,100,100,250,0}[caseIndex],entityId=caseIndex*7919,cloudColor=new int[]{0,0x123456,0xFF804020,-1,Integer.MIN_VALUE,Integer.MAX_VALUE}[caseIndex];float interp=new float[]{0F,.125F,.5F,1F,-.25F,2F}[caseIndex],playerViewX=caseIndex*17.5F,playerViewY=caseIndex*90.25F,prevRotationYaw=caseIndex*10.5F,rotationYaw=prevRotationYaw+67F,prevRotationPitch=-caseIndex*15F,rotationPitch=prevRotationPitch+15.25F;double motionX=caseIndex*.5,motionY=-caseIndex*1.25,motionZ=caseIndex*7.5;','1','0'),
 'RenderRadarScreen':('boolean linked=caseIndex%3!=0;List<int[]> entries=new ArrayList<>();int amount=new int[]{0,0,1,0,3,10}[caseIndex];for(int k=0;k<amount;k++){entries.add(new int[]{k*17-caseIndex,-k*11+caseIndex,k%8});}int refX=caseIndex*13,refZ=-caseIndex*7,range=new int[]{1,0,100,200,-1,16}[caseIndex];long worldTime=caseIndex*53;float f=new float[]{0F,.125F,.5F,1F,-.25F,2F}[caseIndex];CountRandom rand=new CountRandom(91L+caseIndex);','linked?(entries.isEmpty()?1:2):1','linked?0:1'),
 'SkyProviderThatmo':('long age=new long[]{0,1,20,35,100,-1}[caseIndex];double x=new double[]{0,2017,2018,2117,2217,50000}[caseIndex],y=new double[]{0,35000,35001,55000,75000,100000}[caseIndex],z=0;float partialTicks=caseIndex*.125F,playerViewX=caseIndex*17.5F,playerViewY=caseIndex*90.25F,rainStrength=new float[]{0F,.25F,.5F,1F,2F,-1F}[caseIndex];','1','0'),
 'SkyProviderCelestial':('long age=new long[]{0,1,20,35,100,-1}[caseIndex];double x=new double[]{0,2017,2018,2117,2217,50000}[caseIndex],y=new double[]{0,35000,35001,55000,75000,100000}[caseIndex],z=0;float partialTicks=caseIndex*.125F,playerViewX=caseIndex*17.5F,playerViewY=caseIndex*90.25F,rainStrength=new float[]{0F,.25F,.5F,1F,2F,-1F}[caseIndex];','1','0'),
 'ItemRenderAberrator':('float aimingProgress=new float[]{0F,.25F,.5F,1F,2F,-1F}[caseIndex],minU=caseIndex*.125F,maxU=minU+.0625F,minV=caseIndex*.0625F,maxV=minV+.125F;','16','0'),
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
   attributes=(int('.tex(' in batch) if native else int('.addVertexWithUV(' in batch))+(2*int('.color(' in batch) if native else 2*int('.setColor' in batch))+(4*int('.normal(' in batch) if native else 4*int('.setNormal(' in batch))
   if native:assert ('DefaultVertexFormats.POSITION'+('_TEX' if attributes&1 else '')+('_COLOR' if attributes&2 else '')+('_NORMAL' if attributes&4 else '')) in t[0]
 assert attributes is None
 for start,end,s in reversed(edits):body=body[:start]+s+body[end:]
 return strip_owner(body,name).replace(name+'Buffer.','buffer.') if native else strip_owner(body,name).replace(name+'.','old.')

def make_method(number,label,env,old,native,name,expected_draws,expected_rng):
 native=endpoint_replace(native,name,True);old=endpoint_replace(old,name,False)
 for oldRandom,newRandom in [('Random rand = new Random(','CountRandom rand = new CountRandom(')]:native=native.replace(oldRandom,newRandom);old=old.replace(oldRandom,newRandom)
 return """void batch%d(int caseIndex,double translation)throws Exception{
 List<Record> nativeRecords=new ArrayList<>(),oldRecords=new ArrayList<>();int nc,oc;int[] nativeFinal,oldFinal;%s
 reset(caseIndex);randomCalls=0;{Tessellator owner=Tessellator.getInstance();BufferBuilder buffer=owner.getBuffer();buffer.setTranslation(translation,-translation,translation*.25);
 %s
 nc=randomCalls;nativeFinal=state();}
 reset(caseIndex);randomCalls=0;{Legacy old=new Legacy(translation);
 %s
 oc=randomCalls;oldFinal=state();}
 compare(nativeRecords,oldRecords,nc,oc,%s,%s,nativeFinal,oldFinal,"%s");
 }"""%(number,env,native,old,expected_rng,expected_draws,label)
methods=[];calls=[]
for rel,r in source.FILES.items():
 current=(source.ROOT/rel).read_text();old=source.reverse_source(rel,current);stem=P(rel).stem
 for method,record in r['scopes'].items():
  name=record['name'];aa,bb=source.transform.method_span(old,method);ob=extract(old[aa:bb],stem,method,name);aa,bb=source.transform.method_span(current,method);nb=extract(current[aa:bb],stem,method,name)
  env,expected_draws,expected_rng=fixture[stem]
  if method=='renderGasCloud':expected_rng='2'
  # Radar world RNG state is an explicit input and must be reset, not reused
  # after the native side has consumed the unlinked-noise offset.
  if stem=='RenderRadarScreen':nb='rand.setSeed(91L+caseIndex);'+nb;ob='rand.setSeed(91L+caseIndex);'+ob
  number=len(methods);methods.append(make_method(number,stem+'.'+method,env,ob,nb,name,expected_draws,expected_rng));calls.append('test.batch%d(seed,translation);'%number)
assert len(methods)==10
java=(source.HERE/'NativeNormalTest.java.in').read_text().replace('/*METHODS*/','\n'.join(methods)).replace('/*CALLS*/','\n'.join(calls))
for k,v in [('EXECUTIONS',180),('BATCHES',477),('VERTICES',2688),('DRAWS',954),('EMPTY',0)]:java=java.replace('/*EXPECT_'+k+'*/',str(v))
with tempfile.TemporaryDirectory(prefix='remaining-normal-native-') as td:
 tmp=P(td);file=tmp/'NativeNormalTest.java';file.write_text(java)
 for jar in a.classpath.split(os.pathsep):
  if 'lwjgl-platform' in jar and jar.endswith('natives-linux.jar'):
   with zipfile.ZipFile(jar) as z:
    for name in z.namelist():
     if name.endswith('.so') and '/' not in name:(tmp/name).write_bytes(z.read(name))
 support=[source.ROOT/'porting/tests/MaterialTextureAtlasSpriteNativeTest.java',source.ROOT/'src/main/java/com/hbm/render/item/nativeitem/MaterialTextureAtlasSprite.java']
 subprocess.run([str(a.java_home/'bin/javac'),'-encoding','UTF-8','-source','8','-target','8','-cp',a.classpath,'-d',str(tmp),str(file)]+list(map(str,support)),check=True)
 subprocess.run([str(a.java_home/'bin/java'),'-Djava.awt.headless=true','-Djava.library.path='+str(tmp),'-cp',str(tmp)+os.pathsep+a.classpath,'NativeNormalTest',str(a.legacy_client)],cwd=tmp,env=dict(os.environ,MESA_SHADER_CACHE_DISABLE='true'),check=True)
