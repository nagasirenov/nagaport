#!/usr/bin/env python3
import argparse,hashlib,os,pathlib,re,subprocess,tempfile,zipfile,json
import check_source as source
from transform import forward_source
P=pathlib.Path
p=argparse.ArgumentParser();p.add_argument('--source-only',action='store_true');p.add_argument('--java-home',type=P);p.add_argument('--classpath');p.add_argument('--legacy-client',type=P);a=p.parse_args()
source.validate()
for relative,digest in json.loads((source.HERE/'support-hashes.json').read_text()).items():
 assert hashlib.sha256((source.ROOT/relative).read_bytes()).hexdigest()==digest,('changed support',relative)
if a.source_only:raise SystemExit(0)
assert a.java_home and a.classpath and a.legacy_client
expected=json.loads((source.HERE/'engine-hashes.json').read_text());actual={}
for jar in a.classpath.split(os.pathsep):
 if not zipfile.is_zipfile(jar):continue
 with zipfile.ZipFile(jar) as z:
  for name,digest in expected.items():
   if name in z.namelist():
    assert name not in actual,('duplicate native engine definition',name)
    actual[name]=hashlib.sha256(z.read(name)).hexdigest()
assert actual==expected,'Native engine bytecode differs from the retained proof'
assert hashlib.sha256(a.legacy_client.read_bytes()).hexdigest()=='a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce'
fixtures={
 'RenderSmallReactor':'int flux=new int[]{11,50,1000,7000,10000,Integer.MAX_VALUE}[seed];',
 'RenderSolarBoiler':'double dist=new double[]{.25,1,3.75,40,256,10000}[seed];float min=.005F,max=.01F;',
 'RenderDemonLamp':'',
 'RenderBatteryREDD':'float speed=new float[]{-1F,0F,.0001F,1F,15F,30F}[seed];Vec3NT vec=new Vec3NT(0,0,4);'}

def extract(text,name,native):
 start=text.index('tessBuffer.begin(' if native else 'tess.startDrawingQuads();')
 end=text.index('tess.draw();',start)+len('tess.draw();')
 if name=='RenderSmallReactor':
  start=text.index('for(double d = 0.285;');end=text.index('\n\t\t\t}',end)+len('\n\t\t\t}')
 return text[start:end]

def method(label,signature,env,native,old,mixed):
 # Source extraction substitutions are test endpoints and typed batch-boundary values.
 # Same counted RNG provides deterministic old/native Math.random input streams.
 common=lambda s:s.replace('Math.random()','rng.nextDouble()').replace('reactor.totalFlux','flux')
 native=common(native).replace('tessBuffer.','buffer.').replace('tess.draw();','captureNative(owner, nativeRecords);')
 old=common(old).replace('tess.draw();','captureOld(old, oldRecords);').replace('tess.','old.')
 return '''void %s throws Exception {
 List<Record> nativeRecords=new ArrayList<>(),oldRecords=new ArrayList<>();int nr,orr;
 reset();{CountRandom rng=new CountRandom(1000+seed);%s
 Tessellator owner=new Tessellator(8192);BufferBuilder buffer=owner.getBuffer();buffer.setTranslation(translation,-translation,translation*.25);
 %s
 nr=rng.calls;}
 reset();{CountRandom rng=new CountRandom(1000+seed);%s
 Legacy old=new Legacy(translation);
 %s
 orr=rng.calls;}
 compare(nativeRecords,oldRecords,nr,orr,"%s");
}'''%(signature,env,native,env,old,label)
methods=[];calls=[]
for rel,r in source.FILES.items():
 name=P(rel).stem;s=(source.ROOT/rel).read_text();old=source.reverse_source(rel,s)
 n=len(methods);methods.append(method(name,'batch%d(int seed,double translation)'%n,fixtures[name],extract(s,name,True),extract(old,name,False),r['mixed']));calls.append('test.batch%d(seed,translation);'%n)
legacy='tess.startDrawingQuads();\ntess.setColorRGBA_F(r, g, b, alpha);\n'+''.join('tess.addVertex(%d, 0, 0);\n'%i for i in range(4))+'tess.draw();\n'
synthetic='Tessellator tess = Tessellator.instance;\n'+legacy
native=forward_source(synthetic)[0];native=native[native.index('tessBuffer.begin'):]
methods.append(method('float channel boundaries','colorBoundary(float value,int channel)','int seed=0;double translation=0;float r=.25F,g=.5F,b=.75F,alpha=1F;if(channel==0)r=value;if(channel==1)g=value;if(channel==2)b=value;if(channel==3)alpha=value;',native,legacy,False).replace('int nr,orr;','int nr,orr; int seed=0;double translation=0;').replace('int seed=0;double translation=0;float','float'))
java=(source.HERE/'NativeGlowTest.java.in').read_text().replace('/*METHODS*/','\n'.join(methods)).replace('/*CALLS*/','\n'.join(calls))
with tempfile.TemporaryDirectory(prefix='machine-glow-native-') as td:
 tmp=P(td);file=tmp/'NativeGlowTest.java';file.write_text(java)
 for jar in a.classpath.split(os.pathsep):
  if 'lwjgl-platform' in jar and jar.endswith('natives-linux.jar'):
   with zipfile.ZipFile(jar) as z:
    for n in z.namelist():
     if n.endswith('.so') and '/' not in n:(tmp/n).write_bytes(z.read(n))
 support=[source.ROOT/'porting/tests/MaterialTextureAtlasSpriteNativeTest.java',source.ROOT/'src/main/java/com/hbm/render/item/nativeitem/MaterialTextureAtlasSprite.java',source.ROOT/'src/main/java/com/hbm/util/Vec3NT.java']
 subprocess.run([str(a.java_home/'bin/javac'),'-encoding','UTF-8','-source','8','-target','8','-cp',a.classpath,'-d',str(tmp),str(file)]+list(map(str,support)),check=True)
 subprocess.run([str(a.java_home/'bin/java'),'-Djava.awt.headless=true','-Djava.library.path='+str(tmp),'-cp',str(tmp)+os.pathsep+a.classpath,'NativeGlowTest',str(a.legacy_client)],cwd=tmp,env=dict(os.environ,MESA_SHADER_CACHE_DISABLE='true'),check=True)
