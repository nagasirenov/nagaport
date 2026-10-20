#!/usr/bin/env python3
"""Complete plant owners/models; actual Forge classes, explicit config/item registry fixtures."""
import argparse,pathlib,tempfile,subprocess,os,zipfile,importlib.util,hashlib,json
p=argparse.ArgumentParser();p.add_argument('--classpath',required=True);p.add_argument('--legacy-client',type=pathlib.Path,default=pathlib.Path('/tmp/nagaport-legacy-client.jar'));p.add_argument('--java-home',type=pathlib.Path,required=True);a=p.parse_args()
r=pathlib.Path(__file__).resolve().parents[2];s=r/'src/main/java'
manifest=json.loads((r/'porting/tests/plants-original-manifest.json').read_text())
for rel,digest in manifest['original_tree'].items():
 assert hashlib.sha256((r.parent/'ntm-original-pristine'/rel).read_bytes()).hexdigest()==digest,rel
 if '/resources/' in rel:assert (r/rel).read_bytes()==(r.parent/'ntm-original-pristine'/rel).read_bytes(),rel
assert hashlib.sha256(a.legacy_client.read_bytes()).hexdigest()==manifest['legacy_client_sha256'],'Original 1.7 client bytecode oracle changed'
print('PASS pinned pristine renderers/owners, byte-identical original textures and pinned original 1.7 client oracle',flush=True)
production=['blocks/BlockCoral','blocks/IBlockMulti','blocks/generic/BlockReeds','blocks/generic/BlockHangingVine','blocks/generic/PlantBlockState','render/block/RenderCoral','render/block/RenderReeds','render/block/RenderHangingVine','render/block/nativeblock/PlantGeometry','render/block/nativeblock/PlantBakedModel','render/block/nativeblock/BlockPositionLightQuad','main/PlantBlockModelHandler','lib/RefStrings']
with tempfile.TemporaryDirectory(prefix='native-plants-') as d:
 files=[str(s/('com/hbm/'+x+'.java')) for x in production]
 spec=importlib.util.spec_from_file_location('oracle',r/'porting/tests/plant-original-oracle.py');oracle=importlib.util.module_from_spec(spec);spec.loader.exec_module(oracle)
 generated=pathlib.Path(d)/'PlantOriginalOracle.java';generated.write_text(oracle.generate(r));files += [str(generated),str(r/'porting/tests/NativePlantLegacyOracle.java')]
 fixtures={'items/ModItems':'public class ModItems { public static net.minecraft.item.Item powder_calcium=net.minecraft.init.Items.DYE; }','config/ClientConfig':'public class ClientConfig {public static final Flag RENDER_REEDS=new Flag();public static class Flag {public boolean enabled=true;public Boolean get(){return enabled;}}}'}
 for name,body in fixtures.items():
  f=pathlib.Path(d)/('com/hbm/'+name+'.java');f.parent.mkdir(parents=True,exist_ok=True);f.write_text('package com.hbm.'+name.split('/')[0]+';'+body);files.append(str(f))
 test=r/'porting/tests/NativePlantsTest.java'
 if test.exists():files.append(str(test))
 subprocess.run([str(a.java_home/'bin/javac'),'-source','8','-target','8','-encoding','UTF-8','-sourcepath',d,'-cp',a.classpath,'-d',d]+files,check=True)
 print('PASS 13 complete production classes against native Forge 1.12.2; config boolean and calcium item identity external fixtures',flush=True)
 if test.exists():
  for archive in a.classpath.split(os.pathsep):
   if 'lwjgl-platform' in archive and archive.endswith('natives-linux.jar'):
    with zipfile.ZipFile(archive) as z:
     for name in z.namelist():
      if name.endswith('.so'):(pathlib.Path(d)/pathlib.Path(name).name).write_bytes(z.read(name))
  subprocess.run([str(a.java_home/'bin/java'),'-Djava.library.path='+d,'-Djava.awt.headless=true','-cp',d+os.pathsep+a.classpath,'NativePlantsTest',str(r),str(a.legacy_client)],cwd=d,check=True)

  subprocess.run([str(a.java_home/'bin/java'),'-Djava.library.path='+d,'-Djava.awt.headless=true','-cp',d+os.pathsep+a.classpath,'NativePlantsTest',str(r),str(a.legacy_client),'replacements'],cwd=d,check=True)
