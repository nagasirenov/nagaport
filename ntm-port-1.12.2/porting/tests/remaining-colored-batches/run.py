#!/usr/bin/env python3
"""Actual old/native full-loop streaming and Mesa driver parity, finite source scopes."""
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

def extract(body,stem,name):
 if stem in ['RenderForceField','RenderMachineForceField']:
  start=body.index('float sRot');end=body.index('GL11.glEnable(GL11.GL_LIGHTING);',start)
 elif stem=='RenderPumpjack':
  start=body.index('com.hbm.util.Vec3NT backPos');end=body.index(name+'.draw();',start)+len(name+'.draw();')
 elif stem in ['RenderSpear','RenderBFA','RenderFlare']:
  start=body.index('float f1 =' if stem=='RenderFlare' else 'Random random =')
  end=close_brace(body,body.index('for(int i = 0; i <',start));end=body.index('GL11.glPopMatrix();',end)+len('GL11.glPopMatrix();')
 elif stem=='RenderBlackHole':
  start=body.index('for(int j = -1;');end=close_brace(body,start)
 elif stem=='RenderSiegeLaser':
  start=body.index('// front');end=body.rindex(name+'.draw();')+len(name+'.draw();')
 elif stem=='RenderCloudTom':
  start=body.index('double scale =');end=body.index(name+'.draw();',start)+len(name+'.draw();')
 else:raise AssertionError(stem)
 result=body[start:end]
 # Exact boundary reads only. These are not mock entity/world implementations.
 result=result.replace('p_76986_1_.ticksExisted','ticksExisted').replace('blast.age','age').replace('Minecraft.getMinecraft().player.ticksExisted','playerTicks')
 # Real renderer texture lookup is outside the stream/driver fixture boundary.
 result=result.replace('bindTexture(this.getEntityTexture(blast));','')
 result=result.replace('Random random = new Random(','CountRandom random = new CountRandom(')
 result=re.sub(r'(?m)^\s*Tessellator '+name+r' = Tessellator.(?:instance|getInstance\(\));\n','',result)
 result=re.sub(r'(?m)^\s*BufferBuilder '+name+r'Buffer = '+name+r'.getBuffer\(\);\n','',result)
 return result

fixture={
 'RenderForceField':('int l=new int[]{0,1,2,3,4,8}[caseIndex],s=new int[]{0,2,3,6,8,16}[caseIndex];float rad=new float[]{0F,.25F,1F,8F,64F,-2F}[caseIndex];int hex=new int[]{0,0x123456,0xFF804020,-1,Integer.MIN_VALUE,Integer.MAX_VALUE}[caseIndex];','l*s','0'),
 'RenderMachineForceField':('int l=new int[]{0,1,2,3,4,8}[caseIndex],s=new int[]{0,2,3,6,8,16}[caseIndex];float rad=new float[]{0F,.25F,1F,8F,64F,-2F}[caseIndex];int hex=new int[]{0,0x123456,0xFF804020,-1,Integer.MIN_VALUE,Integer.MAX_VALUE}[caseIndex];','l*s','0'),
 'RenderPumpjack':('float rotation=new float[]{0F,30F,90F,180F,359F,-45F}[caseIndex];','1','0'),
 'RenderSpear':('double intensity=new double[]{0,.1,.5,1,2,-1}[caseIndex];','64','448'),
 'RenderBFA':('double intensity=new double[]{0,.1,.5,1,2,-1}[caseIndex];float seed=caseIndex*37.5F;','64','448'),
 'RenderFlare':('int ticksExisted=new int[]{0,1,50,249,250,1000}[caseIndex];double p_76986_2_=caseIndex*.125,p_76986_4_=-caseIndex*.25,p_76986_6_=caseIndex*.5;','(ticksExisted<250?ticksExisted*3:250)','(ticksExisted<250?ticksExisted*3:250)*8'),
 'RenderBlackHole':('','2','0'),
 'RenderSiegeLaser':('int color=new int[]{0,0x123456,0xFF804020,-1,Integer.MIN_VALUE,Integer.MAX_VALUE}[caseIndex];','2','0'),
 'RenderCloudTom':('int age=new int[]{0,1,20,50,100,-1}[caseIndex],playerTicks=caseIndex*101;float f0=new float[]{0F,.125F,.5F,.75F,1F,-.25F}[caseIndex];','1','0'),
}
methods=[];calls=[]
def make_method(signature,label,env,old,native,name,textured,expected_draws,expected_rng):
 native=native.replace(name+'.draw();','captureNative(owner,nativeRecords,'+str(textured).lower()+');').replace(name+'Buffer.','buffer.')
 old=old.replace(name+'.draw();','captureOld(old,oldRecords,'+str(textured).lower()+');').replace(name+'.','old.')
 nc='random.calls' if 'CountRandom random' in native else '0';oc='random.calls' if 'CountRandom random' in old else '0'
 return '''void %s throws Exception {
 List<Record> nativeRecords=new ArrayList<>(),oldRecords=new ArrayList<>();int nc,oc;int[] nativeFinal,oldFinal;%s
 reset(caseIndex);{Tessellator owner=Tessellator.getInstance();BufferBuilder buffer=owner.getBuffer();buffer.setTranslation(translation,-translation,translation*.25);
 %s
 nc=%s;nativeFinal=state();}
 reset(caseIndex);{Legacy old=new Legacy(translation);
 %s
 oc=%s;oldFinal=state();}
 compare(nativeRecords,oldRecords,nc,oc,%s,%s,nativeFinal,oldFinal,"%s");
}'''%(signature,env,native,nc,old,oc,expected_rng,expected_draws,label)
for rel,r in source.FILES.items():
 current=(source.ROOT/rel).read_text();old=source.reverse_source(rel,current);stem=P(rel).stem
 for method,record in r['scopes'].items():
  name=record['name'];aa,bb=source.transform.method_span(old,method);ob=extract(old[aa:bb],stem,name);aa,bb=source.transform.method_span(current,method);nb=extract(current[aa:bb],stem,name)
  env,expected_draws,expected_rng=fixture[stem];number=len(methods)
  methods.append(make_method('batch%d(int caseIndex,double translation)'%number,stem+'.'+method,env,ob,nb,name,record['format']=='POSITION_TEX_COLOR',expected_draws,expected_rng));calls.append('test.batch%d(seed,translation);'%number)
assert len(methods)==11
# Additional exceptional input tests use the same finite production transform.
for kind,args,signature,env in [('setColorRGBA_F','r, g, b, alpha','floatBoundary(float value,int channel)','int caseIndex=0;double translation=0;float r=.25F,g=.5F,b=.75F,alpha=1F;if(channel==0)r=value;if(channel==1)g=value;if(channel==2)b=value;if(channel==3)alpha=value;'),('setColorRGBA_I','rgb, alpha','packedBoundary(int rgb,int alpha)','int caseIndex=0;double translation=0;')]:
 old='Tessellator tess = Tessellator.instance;\ntess.startDrawingQuads();\ntess.'+kind+'('+args+');\n'+''.join('tess.addVertex(%d, 0, 0);\n'%i for i in range(4))+'tess.draw();\n';native,meta=source.transform.forward_scope(old)
 old=old[old.index('tess.startDrawingQuads();'):];native=native[native.index('tessBuffer.begin('):]
 methods.append(make_method(signature,kind+' channel boundary',env,old,native,'tess',False,'1','0'))
boundaries='for(float value:new float[]{-Float.POSITIVE_INFINITY,-100F,-1F,-.001F,-0F,0F,1F/255F,.1F,.25F,.5F,.999F,1F,1.001F,2F,Float.POSITIVE_INFINITY,Float.NaN})for(int channel=0;channel<4;channel++)test.floatBoundary(value,channel);\nfor(int rgb:new int[]{0,0x123456,0xFF804020,-1,Integer.MIN_VALUE,Integer.MAX_VALUE})for(int alpha:new int[]{Integer.MIN_VALUE,-1,0,1,127,254,255,256,Integer.MAX_VALUE})test.packedBoundary(rgb,alpha);'
java=(source.HERE/'NativeColoredTest.java.in').read_text().replace('/*METHODS*/','\n'.join(methods)).replace('/*CALLS*/','\n'.join(calls)).replace('/*BOUNDARIES*/',boundaries)
for k,v in [('EXECUTIONS',198),('BATCHES',8844),('VERTICES',44976),('DRAWS',17688)]:java=java.replace('/*EXPECT_'+k+'*/',str(v))
with tempfile.TemporaryDirectory(prefix='remaining-colored-native-') as td:
 tmp=P(td);file=tmp/'NativeColoredTest.java';file.write_text(java)
 for jar in a.classpath.split(os.pathsep):
  if 'lwjgl-platform' in jar and jar.endswith('natives-linux.jar'):
   with zipfile.ZipFile(jar) as z:
    for name in z.namelist():
     if name.endswith('.so') and '/' not in name:(tmp/name).write_bytes(z.read(name))
 support=[source.ROOT/'porting/tests/MaterialTextureAtlasSpriteNativeTest.java',source.ROOT/'src/main/java/com/hbm/render/item/nativeitem/MaterialTextureAtlasSprite.java',source.ROOT/'src/main/java/com/hbm/util/Vec3NT.java']
 subprocess.run([str(a.java_home/'bin/javac'),'-encoding','UTF-8','-source','8','-target','8','-cp',a.classpath,'-d',str(tmp),str(file)]+list(map(str,support)),check=True)
 subprocess.run([str(a.java_home/'bin/java'),'-Djava.awt.headless=true','-Djava.library.path='+str(tmp),'-cp',str(tmp)+os.pathsep+a.classpath,'NativeColoredTest',str(a.legacy_client)],cwd=tmp,env=dict(os.environ,MESA_SHADER_CACHE_DISABLE='true'),check=True)
