#!/usr/bin/env python3
import argparse,hashlib,os,pathlib,re,subprocess,tempfile,zipfile,json
import check_source as source
from transform import forward_source
P=pathlib.Path
p=argparse.ArgumentParser();p.add_argument('--source-only',action='store_true');p.add_argument('--java-home',type=P);p.add_argument('--classpath');p.add_argument('--legacy-client',type=P);a=p.parse_args()
source.validate()
for relative,digest in json.loads((source.HERE/"support-hashes.json").read_text()).items():
 assert hashlib.sha256((source.ROOT/relative).read_bytes()).hexdigest()==digest,("changed supporting source",relative)
if a.source_only:raise SystemExit(0)
assert a.java_home and a.classpath and a.legacy_client
assert hashlib.sha256(a.legacy_client.read_bytes()).hexdigest()=='a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce'
fixtures={
 'GUIFEL':'int color=RGB[seed],guiLeft=17+seed,guiTop=-7;',
 'GUISILEX':'int color=RGB[seed],guiLeft=17+seed,guiTop=-7,x1=-3,x2=61,y1=13,y2=9;',
 'NumberDisplay':'int color=RGB[seed],renX=17+seed,renY=-7,width=23,height=5;float z=-90.125F;',
 'RenderBAT9000':'double off=2.2,height=seed*.5;Type type=new Type(seed);',
 'RenderRBMKFuelChannel':'double offset=seed*.5;',
 'RenderInfoSystem':'int pX=17+seed,pZ=-7,height=23,side=55,z=0;',
 'RenderBeam5':'float color=new float[]{-.1F,.125F,.5F,1.1F}[seed],o=seed*.175F/8;boolean red=false,green=false,blue=true;int distance=2;'}
for name in ['GUIScreenBobble','GUIScreenHolotape','GUIScreenSnowglobe']:fixtures[name]='int left=13+seed,top=-5,sizeX=171,sizeY=92;'
methods=[];calls=[]
def method(name,env,legacy,native,var,signature):
 legacy=legacy.replace(var+'.draw();','').replace(var+'.','old.')
 native=native.replace(var+'.draw();','').replace(var+'Buffer.','buffer.')
 return 'void '+signature+' throws Exception {\n'+env+'\nTessellator owner=new Tessellator(8192);BufferBuilder buffer=owner.getBuffer();buffer.setTranslation(translation,-translation,translation*.25);Legacy old=new Legacy(translation);\n{\n'+native+'\n}\n{\n'+legacy+'\n}\ncompare(owner,old,"'+name+'");\n}'
for relative,record in source.FILES.items():
 current=(source.ROOT/relative).read_text();old=source.reverse_source(relative,current);var=record['name']
 before=re.findall(var+r'\.startDrawing(?:Quads)?\([^;]*;.*?'+var+r'\.draw\(\);',old,re.S)
 after=re.findall(var+r'Buffer.begin\([^;]*;.*?'+var+r'\.draw\(\);',current,re.S)
 for index,(legacy,native) in enumerate(zip(before,after)):
  number=len(methods);env=fixtures[P(relative).stem]
  methods.append(method(relative+':'+str(index),env,legacy,native,var,'batch%d(int seed,double translation)'%number));calls.append('test.batch%d(seed,translation);'%number)
assert len(methods)==14
# Exercise the exact production float conversion template, independently against
# the original implementation over values unreachable in most literal sites.
legacy='tess.startDrawingQuads();\ntess.setColorRGBA_F(r, g, b, a);\n'+''.join('tess.addVertex(%d, 0, 0);\n'%i for i in range(4))+'tess.draw();'
synthetic='Tessellator tess = Tessellator.instance;\n'+legacy
native=forward_source(synthetic)[0];native=native[native.index('tessBuffer.begin'):]
methods.append(method('float conversion boundaries','double translation=0;float r=.25F,g=.5F,b=.75F,a=1F;if(channel==0)r=value;if(channel==1)g=value;if(channel==2)b=value;if(channel==3)a=value;',legacy,native,'tess','boundary(float value,int channel)'))
java=(source.HERE/'NativeColorTest.java.in').read_text().replace('/*GENERATED_METHODS*/','\n'.join(methods)).replace('/*GENERATED_CALLS*/','\n'.join(calls))
with tempfile.TemporaryDirectory(prefix='simple-colored-native-') as td:
 tmp=P(td);file=tmp/'NativeColorTest.java';file.write_text(java)
 for jar in a.classpath.split(os.pathsep):
  if 'lwjgl-platform' in jar and jar.endswith('natives-linux.jar'):
   with zipfile.ZipFile(jar) as z:
    for n in z.namelist():
     if n.endswith('.so') and '/' not in n:(tmp/n).write_bytes(z.read(n))
 support=[source.ROOT/'porting/tests/MaterialTextureAtlasSpriteNativeTest.java',source.ROOT/'src/main/java/com/hbm/render/item/nativeitem/MaterialTextureAtlasSprite.java']
 exe='.exe' if os.name=='nt' else ''
 subprocess.run([str(a.java_home/'bin'/('javac'+exe)),'-encoding','UTF-8','-source','8','-target','8','-cp',a.classpath,'-d',str(tmp),str(file)]+list(map(str,support)),check=True)
 subprocess.run([str(a.java_home/'bin'/('java'+exe)),'-Djava.awt.headless=true','-Djava.library.path='+str(tmp),'-cp',str(tmp)+os.pathsep+a.classpath,'NativeColorTest',str(a.legacy_client)],cwd=tmp,env=dict(os.environ,MESA_SHADER_CACHE_DISABLE='true'),check=True)
