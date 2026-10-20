#!/usr/bin/env python3
"""Compile full tape/pole owners and model consumer closure against native Forge 1.12.2."""
import argparse, pathlib, subprocess, tempfile, os, zipfile
p=argparse.ArgumentParser();p.add_argument('--classpath',required=True);p.add_argument('--java-home',type=pathlib.Path,required=True);a=p.parse_args()
r=pathlib.Path(__file__).resolve().parents[2];s=r/'src/main/java'
production=['blocks/generic/DecoTapeRecorder', 'blocks/generic/DecoSteelPoles', 'world/gen/nbt/INBTBlockTransformable', 'lib/RefStrings', 'render/block/RenderTapeBlock', 'render/block/nativeblock/AtlasObjGeometry', 'render/block/nativeblock/BlockPositionLightQuad', 'render/block/nativeblock/AtlasObjBakedModel', 'render/block/nativeblock/ThermalModelTransforms', 'render/mesh/ObjMesh', 'render/mesh/ObjMeshParser', 'main/TapeBlockModelHandler']
collaborators={}
with tempfile.TemporaryDirectory(prefix='native-tape-') as d:
 files=[str(s/('com/hbm/'+x+'.java')) for x in production]+[str(r/'porting/tests/NativeTapeTest.java')]
 for path,body in collaborators.items():
  target=pathlib.Path(d)/path;target.parent.mkdir(parents=True,exist_ok=True);target.write_text(body);files.append(str(target))
 subprocess.run([str(a.java_home/'bin/javac'),'-source','8','-target','8','-encoding','UTF-8','-sourcepath',d,'-cp',a.classpath,'-d',d]+files,check=True)
 print('PASS 12 complete production classes; no Minecraft/Forge or NTM source substitutes',flush=True)
 for archive in a.classpath.split(os.pathsep):
  if 'natives-linux' in archive and pathlib.Path(archive).is_file():
   with zipfile.ZipFile(archive) as z:
    for name in z.namelist():
     if name.endswith('.so'): (pathlib.Path(d)/pathlib.Path(name).name).write_bytes(z.read(name))
 resource_zip=pathlib.Path(d)/'resources.zip'
 with zipfile.ZipFile(resource_zip,'w') as z:
  for name in ['models/blocks/taperecorder.obj','models/blocks/pole.obj','textures/blocks/deco_tape_recorder.png','textures/blocks/steel_beam.png','blockstates/tile.tape_recorder.json','blockstates/tile.steel_poles.json']:
   f=r/'src/main/resources/assets/hbm'/name;z.write(f,'assets/hbm/'+name)
 subprocess.run([str(a.java_home/'bin/java'),'-Djava.library.path='+d,'-Djava.awt.headless=true','-cp',d+os.pathsep+a.classpath,'NativeTapeTest',str(r),str(resource_zip)],check=True)
 subprocess.run([str(a.java_home/'bin/java'),'-Djava.library.path='+d,'-Djava.awt.headless=true','-cp',d+os.pathsep+a.classpath,'NativeTapeTest',str(r),'replacements'],check=True)
