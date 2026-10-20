#!/usr/bin/env python3
"""Complete native owner/model/registry closure; explicit reactor tile and player-property boundaries."""
import argparse,pathlib,subprocess,tempfile,os,importlib.util,runpy,zipfile,hashlib
p=argparse.ArgumentParser();p.add_argument('--classpath',required=True);p.add_argument('--java-home',type=pathlib.Path,required=True);p.add_argument('--legacy-client',type=pathlib.Path);a=p.parse_args()
r=pathlib.Path(__file__).resolve().parents[2];s=r/'src/main/java'
if a.legacy_client: assert hashlib.sha256(a.legacy_client.read_bytes()).hexdigest()=='a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce','Unexpected original client artifact'
production=['blocks/BlockBase','blocks/BlockMulti','blocks/BlockEnumMulti','blocks/IBlockMulti','util/EnumUtil','lib/RefStrings','render/icon/IIcon','render/icon/IIconRegister','blocks/generic/UniversalBlockState','blocks/generic/BlockSandbags','blocks/generic/BlockBarrier','blocks/generic/BlockWoodStructure','blocks/machine/rbmk/RBMKMiniPanelBase','blocks/machine/rbmk/RBMKDisplay','blocks/machine/rbmk/RBMKGauge','blocks/machine/rbmk/RBMKKeyPad','render/block/ISBRHUniversal','render/block/RenderISBRHUniversal','render/block/nativeblock/UniversalCuboidGeometry','render/block/nativeblock/UniversalModelFactory','render/block/nativeblock/ThermalModelTransforms','main/UniversalBlockModelHandler']
collaborators={
'com/hbm/main/MainRegistry.java':'package com.hbm.main;public class MainRegistry {public static Object instance=new Object();}',
'com/hbm/extprop/HbmPlayerProps.java':'package com.hbm.extprop;public class HbmPlayerProps { public boolean isOnLadder;public static HbmPlayerProps instance=new HbmPlayerProps();public static HbmPlayerProps getData(net.minecraft.entity.player.EntityPlayer p){return instance;} }',
'api/hbm/block/IToolable.java':'package api.hbm.block;public interface IToolable {enum ToolType {SCREWDRIVER,WRENCH} boolean onScrew(net.minecraft.world.World w,net.minecraft.entity.player.EntityPlayer p,int x,int y,int z,int side,float a,float b,float c,ToolType t);}',
'com/hbm/tileentity/machine/rbmk/TileEntityRBMKDisplay.java':'package com.hbm.tileentity.machine.rbmk;public class TileEntityRBMKDisplay extends net.minecraft.tileentity.TileEntity {public int rotations;public void rotate(){rotations++;}}',
'com/hbm/tileentity/machine/rbmk/TileEntityRBMKGauge.java':'package com.hbm.tileentity.machine.rbmk;public class TileEntityRBMKGauge extends net.minecraft.tileentity.TileEntity {}',
'com/hbm/tileentity/machine/rbmk/TileEntityRBMKKeyPad.java':'package com.hbm.tileentity.machine.rbmk;public class TileEntityRBMKKeyPad extends net.minecraft.tileentity.TileEntity {public Key[] keys={new Key(),new Key(),new Key(),new Key()};public static class Key {public boolean active=true;public int clicks;public void click(){clicks++;}}}'
}
# Execute the exact new production retention hook, not an assumed fixture policy.
for name in ['Display','Gauge','KeyPad']:
 source=(s/('com/hbm/tileentity/machine/rbmk/TileEntityRBMK'+name+'.java')).read_text()
 start=source.index('    @Override\n    public boolean shouldRefresh(');end=source.index('\n    }',start)+6
 path='com/hbm/tileentity/machine/rbmk/TileEntityRBMK'+name+'.java'
 collaborators[path]=collaborators[path][:-1]+source[start:end]+'}'
rebar=runpy.run_path(str(r/'porting/tests/native-rebar-fixtures.py'))
combined=dict(rebar['collaborators']);combined.update(collaborators)
combined['com/hbm/main/MainRegistry.java']='package com.hbm.main;public class MainRegistry {public static Object instance=new Object();public static final ServerProxy proxy=new ServerProxy();}'
collaborators=combined
extra=[str(s/path) for path in rebar['paths'] if str(s/path) not in [str(s/('com/hbm/'+x+'.java')) for x in production]]
with tempfile.TemporaryDirectory(prefix='native-universal-') as d:
 files=[str(s/('com/hbm/'+x+'.java')) for x in production]+extra+[str(r/'porting/tests/NativeUniversalBlocksTest.java')]
 spec=importlib.util.spec_from_file_location('oracle',r/'porting/tests/universal-legacy-geometry-oracle.py');module=importlib.util.module_from_spec(spec);spec.loader.exec_module(module)
 oracle=pathlib.Path(d)/'LegacyUniversalGeometryOracle.java';oracle.write_text(module.generate(r));files.append(str(oracle))
 face=pathlib.Path(d)/'LegacyUniversalFaceOracle.java';face.write_text(module.face_oracle(r));files.append(str(face))
 for path,body in collaborators.items():
  target=pathlib.Path(d)/path;target.parent.mkdir(parents=True,exist_ok=True);target.write_text(body);files.append(str(target))
 inputs=[pathlib.Path(f) for f in files if str(f).startswith(str(r/'src/main/java'))]+[s/('com/hbm/tileentity/machine/rbmk/TileEntityRBMK'+name+'.java') for name in ['Display','Gauge','KeyPad']]
 hashes={p:hashlib.sha256(p.read_bytes()).hexdigest() for p in inputs}
 subprocess.run([str(a.java_home/'bin/javac'),'-source','8','-target','8','-encoding','UTF-8','-sourcepath',d,'-cp',a.classpath,'-d',d]+files,check=True)
 print('PASS '+str(len(production)+len(extra))+' complete production classes against native Forge/Minecraft; '+str(len(collaborators))+' explicit NTM external collaborator types, no platform substitutes',flush=True)
 for jar in a.classpath.split(os.pathsep):
  if 'lwjgl-platform' in jar and jar.endswith('natives-linux.jar'):
   with zipfile.ZipFile(jar) as z:
    for name in z.namelist():
     if name.endswith('.so') and '/' not in name:(pathlib.Path(d)/name).write_bytes(z.read(name))
 subprocess.run([str(a.java_home/'bin/java'),'-Djava.awt.headless=true','-Djava.library.path='+d,'-cp',d+os.pathsep+a.classpath,'NativeUniversalBlocksTest',str(r)]+([str(a.legacy_client)] if a.legacy_client else []),cwd=d,check=True)

 for path,expected in hashes.items():
  assert hashlib.sha256(path.read_bytes()).hexdigest()==expected,'input changed during test: '+str(path)
  print(expected,path.relative_to(r))
