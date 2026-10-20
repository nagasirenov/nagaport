#!/usr/bin/env python3
"""Compile complete wall/corner owners and model route against native 1.12.2 dependencies."""
import argparse, hashlib, json, os, subprocess, tempfile, zipfile
import build_oracle
from pathlib import Path
p=argparse.ArgumentParser()
p.add_argument('--module',type=Path,required=True)
p.add_argument('--java-home',type=Path,required=True)
p.add_argument('--classpath',required=True)
p.add_argument('--original',type=Path)
p.add_argument('--legacy-client',type=Path,required=True)
a=p.parse_args(); a.module=a.module.resolve(); a.java_home=a.java_home.resolve(); a.legacy_client=a.legacy_client.resolve(); here=Path(__file__).resolve().parent; src=a.module/'src/main/java';res=a.module/'src/main/resources'
production=['api/hbm/block/IToolable.java','com/hbm/world/gen/nbt/INBTBlockTransformable.java','com/hbm/lib/RefStrings.java','com/hbm/blocks/generic/BlockSteelDecoration.java','com/hbm/render/block/RenderSteelWall.java','com/hbm/render/block/RenderSteelCorner.java','com/hbm/main/SteelDecorationModelHandler.java']+['com/hbm/render/block/nativeblock/'+n+'.java' for n in ['ConveyorCuboidGeometry','PneumaticCuboidGeometry','ThermalModelTransforms','SteelDecorationBakedModel']]
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
# Only the external tool-recipe key dependency is controlled; no block/model/native API facade.
collaborators={'com/hbm/inventory/RecipesCommon.java':'package com.hbm.inventory; public class RecipesCommon { public static class ComparableStack { public ComparableStack(net.minecraft.item.ItemStack stack) {} } }'}
oracle=build_oracle.generate(a.original,a.legacy_client)
manifest=json.loads((here/'source-manifest.json').read_text())
asset='assets/hbm/textures/blocks/steel_wall.png'
assert manifest['assets']['src/main/resources/'+asset]==sha(res/asset),'Original steel material changed'
exe='.exe' if os.name=='nt' else ''
with tempfile.TemporaryDirectory(prefix='steel-native-') as d:
 out=Path(d);files=[]
 for rel,body in collaborators.items():
  f=out/rel;f.parent.mkdir(parents=True,exist_ok=True);f.write_text(body);files.append(str(f))
 for rel in production:
  f=src/rel;print('PRODUCTION',rel,sha(f),flush=True);files.append(str(f))
 f=out/'OriginalSteelOracle.java';f.write_text(oracle);files.append(str(f))
 for name in ['SteelNativeTest']:
  f=out/(name+'.java');f.write_text((here/(name+'.java')).read_text());files.append(str(f))
 subprocess.run([str(a.java_home/'bin'/('javac'+exe)),'-encoding','UTF-8','-source','8','-target','8','-sourcepath',d,'-cp',a.classpath,'-d',d]+files,check=True)
 jar=out/'steel-resources.jar'
 with zipfile.ZipFile(jar,'w') as z:
  for rel in [asset,'assets/hbm/blockstates/tile.steel_wall.json','assets/hbm/blockstates/tile.steel_corner.json']:z.write(res/rel,rel)
 bytecode=subprocess.run([str(a.java_home/'bin'/('javap'+exe)),'-c','-p','-classpath',d,'com.hbm.blocks.generic.BlockSteelDecoration','com.hbm.render.block.RenderSteelWall','com.hbm.render.block.RenderSteelCorner','com.hbm.main.SteelDecorationModelHandler','com.hbm.render.block.nativeblock.SteelDecorationBakedModel'],check=True,text=True,capture_output=True).stdout
 for forbidden in ['ISimpleBlockRenderingHandler','net/minecraft/client/renderer/RenderBlocks','cpw/mods','worldObj','getBlockMetadata','setBlockMetadataWithNotify']:
  assert forbidden not in bytecode,'Legacy family bytecode reference: '+forbidden
 print('PASS complete native wall/corner production bytecode contains no legacy rendering/world API references',flush=True)
 subprocess.run([str(a.java_home/'bin'/('java'+exe)),'-Xmx1400m','-Djava.awt.headless=true','-cp',os.pathsep.join([d,a.classpath]),'SteelNativeTest',str(res),str(jar),str(a.legacy_client)],cwd=d,check=True)

 for mode in ['wrong-kind','replacement']:
  subprocess.run([str(a.java_home/'bin'/('java'+exe)),'-Djava.awt.headless=true','-cp',os.pathsep.join([d,a.classpath]),'SteelNativeTest',str(res),str(jar),str(a.legacy_client),mode],cwd=d,check=True)
