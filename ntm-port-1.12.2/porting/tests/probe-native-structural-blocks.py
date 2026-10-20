#!/usr/bin/env python3
"""Compile the complete metal-fence/chain owners and models against actual Forge 1.12.2."""
import argparse,pathlib,subprocess,tempfile,os,importlib.util,zipfile
p=argparse.ArgumentParser();p.add_argument('--classpath',required=True);p.add_argument('--java-home',type=pathlib.Path,required=True);a=p.parse_args()
r=pathlib.Path(__file__).resolve().parents[2];s=r/'src/main/java'
production=['blocks/generic/BlockMetalFence','blocks/generic/BlockChain','blocks/IBlockMulti','world/gen/nbt/INBTBlockTransformable','lib/RefStrings','render/block/RenderFence','render/block/RenderChain','render/block/nativeblock/ConveyorCuboidGeometry','render/block/nativeblock/BlockPositionLightQuad','render/block/nativeblock/StructuralGeometry','render/block/nativeblock/StructuralBakedModel','main/StructuralBlockModelHandler']
spec=importlib.util.spec_from_file_location('oracle',r/'porting/tests/structural-original-oracle.py');oracle=importlib.util.module_from_spec(spec);spec.loader.exec_module(oracle)
with tempfile.TemporaryDirectory(prefix='native-structural-') as d:
 generated=pathlib.Path(d)/'StructuralOriginalOracle.java';generated.write_text(oracle.generate(r))
 files=[str(generated)]+[str(s/('com/hbm/'+x+'.java')) for x in production]+[str(r/'porting/tests/NativeStructuralTest.java')]
 target=pathlib.Path(d)/'com/hbm/blocks/ModBlocks.java';target.parent.mkdir(parents=True,exist_ok=True);target.write_text('package com.hbm.blocks; public final class ModBlocks {public static net.minecraft.block.Block fence_gate=net.minecraft.init.Blocks.IRON_DOOR;}');files.append(str(target))
 subprocess.run([str(a.java_home/'bin/javac'),'-source','8','-target','8','-encoding','UTF-8','-sourcepath',d,'-cp',a.classpath,'-d',d]+files,check=True)
 print('PASS 12 complete production classes; one NTM registry identity collaborator, no Minecraft/Forge substitutes',flush=True)
 for archive in a.classpath.split(os.pathsep):
  if 'lwjgl-platform' in archive and archive.endswith('natives-linux.jar'):
   with zipfile.ZipFile(archive) as z:
    for name in z.namelist():
     if name.endswith('.so'): (pathlib.Path(d)/pathlib.Path(name).name).write_bytes(z.read(name))
 subprocess.run([str(a.java_home/'bin/java'),'-Djava.library.path='+d,'-Djava.awt.headless=true','-cp',d+os.pathsep+a.classpath,'NativeStructuralTest',str(r)],check=True)
 subprocess.run([str(a.java_home/'bin/java'),'-Djava.library.path='+d,'-Djava.awt.headless=true','-cp',d+os.pathsep+a.classpath,'NativeStructuralTest',str(r),'replacements'],check=True)
