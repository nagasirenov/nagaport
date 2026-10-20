#!/usr/bin/env python3
"""Portable SolarMirror native closure proof against an executed pristine-renderer oracle."""
import argparse,hashlib,json,os,pathlib,re,subprocess,tempfile,zipfile
p=argparse.ArgumentParser(description=__doc__)
p.add_argument('--module',type=pathlib.Path,required=True)
p.add_argument('--java-home',type=pathlib.Path,required=True)
p.add_argument('--classpath',required=True)
p.add_argument('--original',type=pathlib.Path,required=True)
a=p.parse_args();bundle=pathlib.Path(__file__).resolve().parent;module=a.module.resolve();original=a.original.resolve();java=a.java_home.resolve()/'bin';cp=a.classpath
for line in (bundle/'manifest.sha256').read_text().splitlines():
 want,rel=line.split('  ',1)
 assert hashlib.sha256((bundle/rel).read_bytes()).hexdigest()==want,'Evidence bundle mismatch: '+rel
for rel,want in json.loads((bundle/'original-sha256.json').read_text()).items():
 assert hashlib.sha256((original/rel).read_bytes()).hexdigest()==want,'Pristine source mismatch: '+rel
 if rel.startswith('src/main/resources/'):
  assert hashlib.sha256((module/rel).read_bytes()).hexdigest()==want,'Port resource mismatch: '+rel

def method(source,start):
 begin=source.index(start);brace=source.index('{',begin);depth=1;end=brace+1
 while depth:
  if source[end]=='{':depth+=1
  elif source[end]=='}':depth-=1
  end+=1
 return source[begin:end]

owned=['blocks/machine/SolarMirror','blocks/machine/SolarMirrorModelState','tileentity/machine/TileEntitySolarMirror','tileentity/machine/SolarMirrorPosition','render/block/RenderMirror','render/block/nativeblock/SolarMirrorGeometry','render/block/nativeblock/SolarMirrorBakedModel','main/SolarMirrorModelHandler']
shared=['lib/RefStrings','render/mesh/ObjMesh','render/mesh/ObjMeshParser','render/block/nativeblock/AtlasObjGeometry','render/block/nativeblock/BlockPositionLightQuad']
fixtures=sorted((bundle/'fixtures').rglob('*.java'));assert len(fixtures)==2
assert [str(f.relative_to(bundle/'fixtures')) for f in fixtures]==['com/hbm/tileentity/TileEntityTickingBase.java','com/hbm/tileentity/machine/TileEntitySolarBoiler.java']
files=[module/'src/main/java/com/hbm'/(x+'.java') for x in owned+shared]+fixtures+sorted(bundle.glob('*.java'))
with tempfile.TemporaryDirectory(prefix='solar-mirror-native-') as tmp:
 tmp=pathlib.Path(tmp);classes=tmp/'classes';classes.mkdir();oracle=tmp/'oracle';oracle.mkdir()
 source=(original/'src/main/java/com/hbm/render/block/RenderMirror.java').read_text()
 adapted=re.sub(r'^package [^;]+;','package solaroracle;',source,flags=re.M)
 adapted=re.sub(r'^import [^;]+;\s*','',adapted,flags=re.M)
 (oracle/'RenderMirror.java').write_text(adapted)
 source=(original/'src/main/java/com/hbm/render/util/ObjUtil.java').read_text()
 signatures=[
 'public static void renderPartWithIcon(HFRWavefrontObject model, String name, IIcon icon, Tessellator tes, float rot, boolean shadow)',
 'public static void renderPartWithIcon(HFRWavefrontObject model, String name, IIcon icon, Tessellator tes, float rot, float pitch, boolean shadow)',
 'public static void renderPartWithIcon(HFRWavefrontObject model, String name, IIcon icon, Tessellator tes, float rot, float pitch, float roll, boolean shadow)']
 (oracle/'ObjUtil.java').write_text('package solaroracle;\nfinal class ObjUtil {\nprivate static boolean hasColor=false; private static int red,green,blue;\n'+'\n'.join(method(source,s) for s in signatures)+'\n}\n')
 files+=sorted(oracle.glob('*.java'))
 subprocess.run([str(java/'javac'),'-source','8','-target','8','-encoding','UTF-8','-sourcepath',str(bundle/'fixtures'),'-cp',cp,'-d',str(classes)]+list(map(str,files)),check=True,cwd=tmp)
 bytecode=subprocess.run([str(java/'javap'),'-c','-p','-classpath',str(classes)]+['com.hbm.'+x.replace('/','.') for x in owned+shared],check=True,text=True,capture_output=True).stdout
 for forbidden in ['com/hbm/compat','net/minecraft/util/Vec3;', 'cpw/mods','ISimpleBlockRenderingHandler','RenderBlocks','net/minecraft/util/IIcon','worldObj','xCoord','yCoord','zCoord']:
  assert forbidden not in bytecode,'Legacy production bytecode reference: '+forbidden
 print('PASS 13 complete actual production classes; exactly two explicit outside-owner NTM fixtures; unchanged pristine renderer method bodies and named-part helper compiled; legacy owner bytecode references absent',flush=True)
 for jar in cp.split(os.pathsep):
  if 'lwjgl-platform' in jar and jar.endswith('natives-linux.jar'):
   with zipfile.ZipFile(jar) as z:z.extractall(classes)
 subprocess.run([str(java/'java'),'-Djava.library.path='+str(classes),'-Djava.awt.headless=true','-cp',str(classes)+os.pathsep+cp,'NativeSolarMirrorTest',str(module/'src/main/resources')],check=True,cwd=tmp)
 for mode in ['subclass','unrelated']:
  subprocess.run([str(java/'java'),'-Djava.library.path='+str(classes),'-Djava.awt.headless=true','-cp',str(classes)+os.pathsep+cp,'NativeSolarMirrorTest',str(module/'src/main/resources'),mode],check=True,cwd=tmp)
