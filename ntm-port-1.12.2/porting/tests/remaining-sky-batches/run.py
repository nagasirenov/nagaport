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

def inner(body):return body[body.index('{')+1:body.rindex('}')]
def extract(body,method):
 if method=='render':
  start=body.rfind('GL11.glColor4f(',0,body.index('Vec3d pos ='))
  s=body[start:body.rindex('}')]
  for old,new in {'mc.player.getPosition(partialTicks)':'position','world.getHorizon()':'horizon','world.provider.isSkyColored()':'skyColored','world.getSunBrightness(partialTicks)':'sunBrightnessInput','mc.renderEngine.bindTexture(body.texture);':'GL11.glBindTexture(GL11.GL_TEXTURE_2D,textures[0]);'}.items():s=s.replace(old,new)
  return s
 if method=='renderSunset':
  s=body[body.index('if(sunsetColor != null)'):body.rindex('}')]
  return s.replace('mc.gameSettings.anaglyph','anaglyph').replace('world.getCelestialAngleRadians(partialTicks)','celestialRadians').replace('world.provider.getWorldTime()','worldTime').replace('mc.renderEngine.bindTexture(surfaceTexture);','GL11.glBindTexture(GL11.GL_TEXTURE_2D,textures[1]);')
 if method=='renderStars':
  return inner(body).replace('mc.renderEngine.bindTexture(nightTexture);','GL11.glBindTexture(GL11.GL_TEXTURE_2D,textures[1]);')
 if method=='renderSun':
  s=body[body.index('if(sun.shader != null'):body.rindex('}')]
  for old,new in {'sun.shader != null && sun.hasTrait(CBT_Destroyed.class)':'destroyed','Shader shader = sun.shader;':'','sun.shaderScale':'shaderScale','world.getWorldTime()':'worldTime','mc.renderEngine.bindTexture(noise);':'GL11.glBindTexture(GL11.GL_TEXTURE_2D,textures[0]);','mc.renderEngine.bindTexture(SolarSystem.kerbol.texture);':'GL11.glBindTexture(GL11.GL_TEXTURE_2D,textures[0]);','mc.renderEngine.bindTexture(flareTexture);':'GL11.glBindTexture(GL11.GL_TEXTURE_2D,textures[1]);'}.items():s=s.replace(old,new)
  start=s.index('float[] sunColor =');end=s.index(';',start)+1;s=s[:start]+'float[] sunColor = inputSunColor;'+s[end:]
  return s
 if method=='renderSwarm':return inner(body).replace('mc.displayHeight','displayHeight').replace('mc.gameSettings.fovSetting','fovSetting').replace('world.getWorldTime()','worldTime')
 if method=='renderSkyboxSide':return inner(body)
 raise AssertionError(method)

fixture={
 'render':('Vec3d position=new Vec3d(new double[]{0,1,-1025,10000,Double.NaN,4096}[caseIndex],new double[]{0,16,-1,1000,2000,500}[caseIndex],0);double horizon=1;boolean skyColored=caseIndex%2==0;float skyR=caseIndex*.1F,skyG=.5F,skyB=.7F,sunBrightnessInput=caseIndex*.2F;int glSkyList2=GLAllocation.generateDisplayLists(1);GL11.glNewList(glSkyList2,GL11.GL_COMPILE);GL11.glEndList();','1+(position.y-horizon<0?1:0)','0','0','GL11.glDeleteLists(glSkyList2,1);'),
 'renderSunset':('float[] sunsetColor=caseIndex==0?null:new float[]{.9F,.2F+caseIndex*.1F,.5F,caseIndex*.2F};boolean anaglyph=caseIndex%2==0;float celestialRadians=caseIndex*1.5F,pressure=new float[]{-.1F,0F,.049F,.05F,1F,-1F}[caseIndex];','sunsetColor==null?0:1+(pressure<.05F?1:0)','sunsetColor!=null&&pressure<.05F?3076:0','0',''),
 'renderStars':('float starBrightness=new float[]{-.5F,0F,.25F,.5F,1F,2F}[caseIndex],siderealAngle=caseIndex*.21F,axialTilt=caseIndex*17.5F;','starBrightness>0?6:0','0','0',''),
 'renderSun':('boolean destroyed=caseIndex%3==0;double sunSize=new double[]{0,1,10,30,100,-5}[caseIndex],coronaSize=sunSize*2,shaderScale=3;int swarmCount=new int[]{0,1,2,3,8,-1}[caseIndex];float visibility=caseIndex*.2F,pressure=new float[]{-.1F,0F,.049F,.05F,1F,-1F}[caseIndex];float[] inputSunColor=new float[]{.2F+caseIndex*.1F,.75F,.9F};','destroyed?1:7','0','destroyed?2:1+Math.max(swarmCount,0)',''),
 'renderSwarm':('double swarmRadius=new double[]{0,1,10,30,100,-5}[caseIndex];int swarmCount=new int[]{0,1,2,3,8,-1}[caseIndex];','3','0','1+Math.max(swarmCount,0)',''),
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

def shaders(body):
 for receiver,program in [('swarmShader','swarmProgram'),('shader','blackProgram')]:
  body=body.replace(receiver+'.use();','int previousProgram=use('+program+');').replace(receiver+'.stop();','stop(previousProgram);')
  body=body.replace(receiver+'.getUniformLocation("iOffset")','GL20.glGetUniformLocation('+program+',"iOffset")')
  body=body.replace(receiver+'.setUniform1f("iTime", time);','uniform1f('+program+',GL20.glGetUniformLocation('+program+',"iTime"),time);')
  body=body.replace(receiver+'.setUniform1f(offsetLocation, i);','uniform1f('+program+',offsetLocation,i);')
  body=body.replace(receiver+'.setUniform1i("iChannel1", 0);','uniform1i('+program+',GL20.glGetUniformLocation('+program+',"iChannel1"),0);')
 return body

def endpoints(body,native):
 body=endpoint_replace(body,'tessellator',native)
 if native:
  body=body.replace('renderSkyboxSide(tessellator,','nativeSkyboxSide(owner,nativeRecords,')
  body=body.replace('renderSwarm(partialTicks, world, mc, sunSize * 0.5, swarmCount);','nativeSwarm(owner,nativeRecords,sunSize * 0.5,swarmCount);')
 else:
  body=body.replace('renderSkyboxSide(tessellator,','oldSkyboxSide(old,oldRecords,')
  body=body.replace('renderSwarm(partialTicks, world, mc, sunSize * 0.5, swarmCount);','oldSwarm(old,oldRecords,sunSize * 0.5,swarmCount);')
 return shaders(body).replace('Random rand = new Random(','CountRandom rand = new CountRandom(')

rel=next(iter(source.FILES));current=(source.ROOT/rel).read_text();old=source.reverse_source(rel,current)
def region(text,method):a,b=source.transform.method_span(text,method);return extract(text[a:b],method)
methods=[];calls=[]
for helper in ['renderSkyboxSide','renderSwarm']:
 for native,text in [(True,current),(False,old)]:
  body=endpoints(region(text,helper),native);kind='native' if native else 'old';owner='Tessellator owner,List<Record> nativeRecords' if native else 'Legacy old,List<Record> oldRecords';params='int side' if helper=='renderSkyboxSide' else 'double swarmRadius,int swarmCount'
  method=kind+('SkyboxSide' if helper=='renderSkyboxSide' else 'Swarm')
  methods.append('void '+method+'('+owner+','+params+')throws Exception{'+('BufferBuilder buffer=owner.getBuffer();' if native else '')+body+'}')
aa,bb=source.transform.method_span(current,'applyAnaglyph');methods.append(current[aa:bb])
for method in ['render','renderSunset','renderStars','renderSun','renderSwarm']:
 ob=endpoints(region(old,method),False);nb=endpoints(region(current,method),True);env,expected_batches,expected_rng,expected_shader,cleanup=fixture[method];number=len(calls)
 methods.append("""void batch%d(int caseIndex,double translation)throws Exception{
 worldTime=new long[]{0,1,100,800000,16777217,-1}[caseIndex];partialTicks=caseIndex*.125F;displayHeight=new int[]{0,200,1080,2160,-1,720}[caseIndex];fovSetting=new float[]{70,0,45,90,-10,120}[caseIndex];
 List<Record> nativeRecords=new ArrayList<>(),oldRecords=new ArrayList<>();int nc,oc,ns,os;int[] nativeFinal,oldFinal;List<Integer> nt,ot;%s
 reset(caseIndex);randomCalls=0;{Tessellator owner=Tessellator.getInstance();BufferBuilder buffer=owner.getBuffer();buffer.setTranslation(translation,-translation,translation*.25);
 %s
 nc=randomCalls;ns=shaderCalls;nt=new ArrayList<>(shaderTrace);nativeFinal=state();}
 reset(caseIndex);randomCalls=0;{Legacy old=new Legacy(translation);
 %s
 oc=randomCalls;os=shaderCalls;ot=new ArrayList<>(shaderTrace);oldFinal=state();}
 if(ns!=os||ns!=(%s)||!nt.equals(ot))throw new AssertionError("shader calls %s "+ns+"/"+os);
 compare(nativeRecords,oldRecords,nc,oc,%s,%s,nativeFinal,oldFinal,"%s");%s
 }"""%(number,env,nb,ob,expected_shader,method,expected_rng,expected_batches,method,cleanup));calls.append('test.batch%d(seed,translation);'%number)
java=(source.HERE/'NativeSkyTest.java.in').read_text().replace('/*METHODS*/','\n'.join(methods)).replace('/*CALLS*/','\n'.join(calls)).replace('/*FORMAT_COUNTS*/','111,132,21,0')
for k,v in [('EXECUTIONS',90),('BATCHES',264),('VERTICES',10257),('DRAWS',438),('EMPTY',90)]:java=java.replace('/*EXPECT_'+k+'*/',str(v))
with tempfile.TemporaryDirectory(prefix='remaining-sky-native-') as td:
 tmp=P(td);file=tmp/'NativeSkyTest.java';file.write_text(java)
 for jar in a.classpath.split(os.pathsep):
  if 'lwjgl-platform' in jar and jar.endswith('natives-linux.jar'):
   with zipfile.ZipFile(jar) as z:
    for name in z.namelist():
     if name.endswith('.so') and '/' not in name:(tmp/name).write_bytes(z.read(name))
 support=[source.ROOT/'porting/tests/MaterialTextureAtlasSpriteNativeTest.java',source.ROOT/'src/main/java/com/hbm/render/item/nativeitem/MaterialTextureAtlasSprite.java']
 subprocess.run([str(a.java_home/'bin/javac'),'-encoding','UTF-8','-source','8','-target','8','-cp',a.classpath,'-d',str(tmp),str(file)]+list(map(str,support)),check=True)
 subprocess.run([str(a.java_home/'bin/java'),'-Djava.awt.headless=true','-Djava.library.path='+str(tmp),'-cp',str(tmp)+os.pathsep+a.classpath,'NativeSkyTest',str(a.legacy_client),str(source.ROOT/'src/main/resources/assets/hbm/shaders')],cwd=tmp,env=dict(os.environ,MESA_SHADER_CACHE_DISABLE='true'),check=True)
