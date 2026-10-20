#!/usr/bin/env python3
"""Portable native falling/anvil evidence runner. Requires only supplied module, native classpath and JDK8."""
import argparse,hashlib,json,os,pathlib,subprocess,tempfile,zipfile
p=argparse.ArgumentParser(description=__doc__)
p.add_argument('--module',type=pathlib.Path,required=True)
p.add_argument('--java-home',type=pathlib.Path,required=True)
p.add_argument('--classpath',required=True)
p.add_argument('--original',type=pathlib.Path,help='Optional pristine source tree, verified against pinned source and asset hashes.')
p.add_argument('--smoke',action='store_true',help='Compile every selected production/test source and run physics plus native registration; skip equivalent exhaustive anvil geometry run.')
a=p.parse_args();bundle=pathlib.Path(__file__).resolve().parent;module=a.module.resolve();java=a.java_home.resolve()/'bin';cp=a.classpath
for line in (bundle/'manifest.sha256').read_text().splitlines():
 want,rel=line.split('  ',1);assert hashlib.sha256((bundle/rel).read_bytes()).hexdigest()==want,'Bundle modified: '+rel
oracle=json.loads((bundle/'original-sha256.json').read_text())
for rel,want in oracle.items():
 if rel.startswith('src/main/resources/'):
  assert hashlib.sha256((module/rel).read_bytes()).hexdigest()==want,'Original resource mismatch: '+rel
 if a.original:
  assert hashlib.sha256((a.original/rel).read_bytes()).hexdigest()==want,'Pristine oracle mismatch: '+rel
source=(module/'src/main/java/com/hbm/entity/item/EntityFallingBlockNT.java').read_text()
for exact in ['this.motionY -= 0.04D;', 'this.motionX *= 0.98D;', 'this.motionY *= 0.98D;', 'this.motionZ *= 0.98D;', 'this.motionX *= 0.7D;', 'this.motionZ *= 0.7D;', 'this.motionY *= -0.5D;', 'this.damageCap = 40;', 'this.damageAmount = 2.0F;', 'nbt.setByte("Time", (byte) this.fallingTicks);', 'this.fallingTicks = nbt.getByte("Time") & 255;', '0.05D + (double) fall * 0.05D']:
 assert exact in source,exact
start=source.index('public EntityFallingBlockNT(World world, double x, double y, double z, Block block, int meta)');end=source.index('@Override protected void entityInit()',start)
assert 'super(world);' in source[start:end] and 'setSize(' not in source[start:end]
assert 'this.setSize(0.98F, 0.98F);' in source[:start]
print('PASS immutable extracted source oracle, pinned module OBJ/textures, constructor asymmetry and byte Time'+('; pristine source tree verified' if a.original else ''),flush=True)
owned=['blocks/BlockFallingNT','blocks/generic/BlockFallingTint','blocks/machine/NTMAnvil','entity/item/EntityFallingBlockNT','render/entity/item/RenderFallingBlockNT','render/block/RenderAnvil','main/FallingTintModelHandler','main/AnvilModelHandler','render/block/nativeblock/FallingTintBakedModel']
shared=['blocks/ISpotlight','tileentity/IGUIProvider','lib/RefStrings','render/mesh/ObjMesh','render/mesh/ObjMeshParser','render/block/nativeblock/AtlasObjGeometry','render/block/nativeblock/AtlasObjBakedModel','render/block/nativeblock/BlockPositionLightQuad','render/block/nativeblock/ThermalModelTransforms']
fixtures=list((bundle/'fixtures').rglob('*.java'));assert len(fixtures)==4
files=[module/'src/main/java/com/hbm'/(x+'.java') for x in owned+shared]+fixtures+list(bundle.glob('*.java'))
with tempfile.TemporaryDirectory(prefix='native-falling-proof-') as tmp:
 classes=pathlib.Path(tmp)/'classes';classes.mkdir()
 subprocess.run([str(java/'javac'),'-source','8','-target','8','-encoding','UTF-8','-sourcepath',str(bundle/'fixtures'),'-cp',cp,'-d',str(classes)]+list(map(str,files)),check=True,cwd=tmp)
 print('PASS 18 complete real production classes plus all tests compiled; exactly four explicit NTM collaborator fixtures',flush=True)
 for jar in cp.split(os.pathsep):
  if 'lwjgl-platform' in jar and jar.endswith('natives-linux.jar'):
   with zipfile.ZipFile(jar) as z:z.extractall(classes)
 def run(test,*args):
  subprocess.run([str(java/'java'),'-Djava.library.path='+str(classes),'-Djava.awt.headless=true','-cp',str(classes)+os.pathsep+cp,test]+list(args),check=True,cwd=tmp)
 run('NativeFallingTest');run('NativeFallingModelTest')
 if not a.smoke:run('NativeAnvilTest',str(module/'src/main/resources'),str(bundle/'skins.txt'))
 else:print('SMOKE: exhaustive anvil geometry intentionally not rerun; full equivalent prior evidence is bundled.',flush=True)
