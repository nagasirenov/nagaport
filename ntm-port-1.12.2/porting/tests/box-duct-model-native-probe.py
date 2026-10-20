#!/usr/bin/env python3
"""Whole native box-duct models/owners, real Forge registry/dispatcher; extracted shared tile hooks."""
import argparse,hashlib,json,os,re,subprocess,tempfile,zipfile
from pathlib import Path
p=argparse.ArgumentParser();p.add_argument('--module',type=Path,required=True);p.add_argument('--java-home',type=Path,required=True);p.add_argument('--classpath',required=True);a=p.parse_args();a.module=a.module.resolve();src=a.module/'src/main/java';here=Path(__file__).resolve().parent
exe='.exe' if os.name=='nt' else ''
def method(text,name):
 m=re.search(r'public (?:static )?[^\n]+\b'+name+r'\([^)]*\)\s*\{',text);assert m,name;e=m.end();d=1
 while d:d+=(text[e]=='{')-(text[e]=='}');e+=1
 return text[m.end():e-1]
# Reuse exactly the separately documented owner-fixture boundaries, never platform facades.
owner=(here/'box-duct-owner-native-probe.py').read_text();scope={'src':src,'method':method}
exec(owner[owner.index("lib=(src/"):owner.index('with tempfile.TemporaryDirectory')],scope)
fixtures=scope['collaborators'];production=scope['production']
for name in ['TileEntityPipeBaseNT','TileEntityPipeExhaust','TileEntityCableBaseNT']:
 rel='com/hbm/tileentity/network/'+name+'.java';body=method((src/rel).read_text(),'canRenderBreaking');base=fixtures[rel];fixtures[rel]=base[:-1]+' public boolean canRenderBreaking(){'+body+'}}'
fixtures['com/hbm/blocks/network/CableDiode.java']='package com.hbm.blocks.network;public class CableDiode {public static class TileEntityDiode extends net.minecraft.tileentity.TileEntity {}public static boolean isNativeModelOwner(net.minecraft.block.Block b){return false;}}'
fixtures['com/hbm/blocks/network/CranePartitioner.java']='package com.hbm.blocks.network;public class CranePartitioner {public static class TileEntityCranePartitioner extends net.minecraft.tileentity.TileEntity {}public static boolean isNativeModelOwner(net.minecraft.block.Block b){return false;}}'
fixtures['com/hbm/render/block/nativeblock/DiodeBakedModel.java']='package com.hbm.render.block.nativeblock;public abstract class DiodeBakedModel implements net.minecraft.client.renderer.block.model.IBakedModel {public java.util.List<net.minecraft.client.renderer.block.model.BakedQuad> getAllQuads(net.minecraft.block.state.IBlockState s){return java.util.Collections.emptyList();}}'
production += [src/('com/hbm/'+rel+'.java') for rel in ['blocks/network/BoxDuctRenderState','main/BoxDuctModelHandler','render/block/RenderBoxDuct','render/block/nativeblock/BoxDuctMaterials','render/block/nativeblock/BoxDuctBakedModel','render/block/nativeblock/PneumaticCuboidGeometry','render/block/nativeblock/ThermalModelTransforms','render/block/nativeblock/AtlasObjBakedModel','render/item/nativeitem/ItemTransformMatrix','render/item/nativeitem/AngelCoreFrameCorrection','render/tileentity/NetworkBreakingRenderer','render/tileentity/BoxDuctBreakingRenderer']]
production=list(dict.fromkeys(production))
with tempfile.TemporaryDirectory(prefix='box-duct-model-native-') as d:
 out=Path(d);files=[]
 for rel,body in fixtures.items():
  assert rel.startswith(('com/hbm/','api/hbm/'));f=out/rel;f.parent.mkdir(parents=True,exist_ok=True);f.write_text(body);files.append(f)
 for f in production:print('COMPLETE PRODUCTION',f.relative_to(a.module),hashlib.sha256(f.read_bytes()).hexdigest(),flush=True);files.append(f)
 files += [here/'BoxDuctModelNativeTest.java',here/'ForgeEventNativeLauncher.java']
 subprocess.run([str(a.java_home/'bin'/('javac'+exe)),'-encoding','UTF-8','-source','8','-target','8','-sourcepath',d,'-cp',a.classpath,'-d',d]+list(map(str,files)),check=True)
 classes=[str(f.relative_to(src))[:-5].replace(os.sep,'.') for f in production]
 bytecode=subprocess.run([str(a.java_home/'bin'/('javap'+exe)),'-c','-p','-classpath',d]+classes,text=True,capture_output=True,check=True).stdout
 for forbidden in ['ISimpleBlockRenderingHandler','net/minecraft/client/renderer/RenderBlocks','getNextAvailableRenderId','getBlockMetadata:']:assert forbidden not in bytecode,forbidden
 print('PASS complete native production bytecode has no removed block rendering APIs',flush=True)
 jar=out/'box-duct-resources.jar';res=a.module/'src/main/resources'
 with zipfile.ZipFile(jar,'w') as z:
  for f in (res/'assets/hbm').rglob('*'):
   if f.is_file() and ('boxduct_' in f.name or f.name=='fluid_duct_box.png' or f.name in ['tile.fluid_duct_box.json','tile.fluid_duct_exhaust.json','tile.red_cable_box.json']):z.write(f,f.relative_to(res))
 subprocess.run([str(a.java_home/'bin'/('java'+exe)),'-Xmx1G','-Djava.awt.headless=true','-cp',os.pathsep.join([d,a.classpath]),'ForgeEventNativeLauncher','BoxDuctModelNativeTest',str(res),str(jar)],cwd=d,check=True)
