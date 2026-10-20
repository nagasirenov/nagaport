#!/usr/bin/env python3
"""Focused real-native owner/model/registration proof, not a full mod compile."""
import argparse, hashlib, json, os, subprocess, tempfile, zipfile
from pathlib import Path
p=argparse.ArgumentParser()
p.add_argument('--module',type=Path,required=True)
p.add_argument('--base-module',type=Path)
p.add_argument('--java-home',type=Path,required=True)
p.add_argument('--classpath',required=True)
p.add_argument('--original',type=Path)
p.add_argument('--legacy-client',type=Path,required=True)
p.add_argument('--draw-order',action='store_true',help='Run actual offscreen Mesa OpenGL framebuffer comparison (EGL required)')
a=p.parse_args();a.module=a.module.resolve();a.java_home=a.java_home.resolve();a.legacy_client=a.legacy_client.resolve();base=(a.base_module or a.module).resolve();here=Path(__file__).resolve().parent
manifest=json.loads((here/'source-manifest.json').read_text())
def sha(data):return hashlib.sha256(data).hexdigest()
with zipfile.ZipFile(here/'pristine-wand-sources.zip') as z:
 original={name:(a.original/name).read_bytes() if a.original else z.read(name) for name in manifest['sources']}
for name,expected in manifest['sources'].items():assert sha(original[name])==expected,'Pinned pristine source differs: '+name
assert sha(a.legacy_client.read_bytes())==manifest['legacy_client_sha256'],'Unexpected original client bytecode'
owner=original['src/main/java/com/hbm/blocks/generic/BlockWand.java'].decode()
render=original['src/main/java/com/hbm/render/block/RenderBlockWand.java'].decode()
registry=original['src/main/java/com/hbm/blocks/ModBlocks.java'].decode()
assert 'setBlockBounds(1F/16F, 1F/16F, 1F/16F, 15F/16F, 15F/16F, 15F/16F);' in owner
assert 'return block != this;' in owner and 'return null;' in owner and 'super(Material.glass)' in owner
assert render.index('renderer.renderFromInside = true;') < render.index('renderer.renderStandardBlock(block, x, y, z);') < render.index('renderer.renderFromInside = false;')
assert render.count('renderer.renderStandardBlock(block, x, y, z);')==2
assert 'wand_air = new BlockWand(Blocks.air).setBlockName("wand_air").setBlockTextureName(RefStrings.MODID + ":wand_air");' in registry
assert registry.count('new BlockWand(')==1 and 'register(wand_air);' in registry
production=['com/hbm/blocks/generic/BlockWand.java','com/hbm/render/block/RenderBlockWand.java','com/hbm/render/block/nativeblock/WandBakedModel.java','com/hbm/main/WandModelHandler.java','com/hbm/render/block/nativeblock/ConveyorCuboidGeometry.java','com/hbm/lib/RefStrings.java']
def resolve(relative):
 f=a.module/relative
 return f if f.exists() else base/relative
res=resolve('src/main/resources');asset='assets/hbm/textures/blocks/wand_air.png'
assert sha((res/asset).read_bytes())==manifest['sources']['src/main/resources/'+asset],'Original wand PNG changed'
with tempfile.TemporaryDirectory(prefix='wand-native-') as d:
 out=Path(d); files=[]
 for rel in production:
  f=resolve('src/main/java/'+rel);print('PRODUCTION',rel,sha(f.read_bytes()),flush=True);files.append(str(f))
 files.extend(str(here/n) for n in ['OriginalWandOracle.java','WandNativeTest.java'])
 subprocess.run([str(a.java_home/'bin/javac'),'-encoding','UTF-8','-source','8','-target','8','-sourcepath',d,'-cp',a.classpath,'-d',d]+files,check=True)
 jar=out/'wand-resources.jar'
 with zipfile.ZipFile(jar,'w') as z:
  for rel in [asset,'assets/hbm/blockstates/tile.wand_air.json']:z.write(res/rel,rel)
 bytecode=subprocess.run([str(a.java_home/'bin/javap'),'-c','-p','-classpath',d]+[s[:-5].replace('/','.') for s in production],check=True,text=True,capture_output=True).stdout
 for forbidden in ['ISimpleBlockRenderingHandler','net/minecraft/client/renderer/RenderBlocks','cpw/mods','worldObj','getBlockMetadata','setBlockMetadataWithNotify']:
  assert forbidden not in bytecode,'Legacy native production reference: '+forbidden
 print('PASS complete native owner, rendering, model handler and helper contain no legacy API references; zero NTM collaborator stubs',flush=True)
 for mode in ['normal','wrong-export','replacement','subclass']:
  subprocess.run([str(a.java_home/'bin/java'),'-Xmx1000m','-Djava.awt.headless=true','-cp',os.pathsep.join([d,a.classpath]),'WandNativeTest',str(res),str(jar),str(a.legacy_client),mode]+([str(out/'draw.json')] if a.draw_order and mode=='normal' else []),cwd=d,check=True)
 if a.draw_order:subprocess.run(['python3',str(here/'draw_order.py'),str(out/'draw.json')],check=True)
