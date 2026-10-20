#!/usr/bin/env python3
"""Whole native TESRs and source-preserving original rendering oracles on genuine GL."""
import argparse,hashlib,json,os,pathlib,re,subprocess,tempfile,zipfile
P=pathlib.Path
p=argparse.ArgumentParser();p.add_argument('--classpath',required=True);p.add_argument('--java-home',type=P,required=True);p.add_argument('--legacy-client',type=P,required=True);p.add_argument('--original-root',type=P);p.add_argument('--write-scope',action='store_true');p.add_argument('--negative-control',choices=['lava-uv','preview-geometry','animation-direction']);a=p.parse_args()
here=P(__file__).resolve().parent;root=here.parents[2];exe='.exe' if os.name=='nt' else ''
assert hashlib.sha256(a.legacy_client.read_bytes()).hexdigest()=='a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce'
names=['RenderMagma','RenderMultiblock','RenderSoyuzMultiblock']
original_names=['src/main/java/com/hbm/render/tileentity/'+n+'.java' for n in names]
original_names+=['src/main/java/com/hbm/blocks/ModBlocks.java','src/main/java/com/hbm/blocks/machine/MachineMagma.java','src/main/java/com/hbm/main/ResourceManager.java','src/main/java/com/hbm/blocks/BlockBase.java','src/main/java/com/hbm/blocks/generic/BlockGeneric.java','src/main/java/com/hbm/blocks/generic/BlockRadResistant.java']
assets=['models/machines/magma_drill.obj','textures/models/machines/magma_drill.png','textures/blocks/struct_launcher.png','textures/blocks/struct_scaffold.png','textures/blocks/concrete.png']
if a.original_root:
 original={n:(a.original_root/n).read_bytes() for n in original_names}
else:
 with zipfile.ZipFile(here/'original-sources.zip') as z:original={n:z.read(n) for n in original_names}
for n in assets:
 if a.original_root:assert (a.original_root/'src/main/resources/assets/hbm'/n).read_bytes()==(root/'src/main/resources/assets/hbm'/n).read_bytes()
# These were the entire removed RenderBlocks material lookups. Check actual original
# registry declarations, not an inferred native particle sprite or a guessed path.
materials={}
old_blocks=original['src/main/java/com/hbm/blocks/ModBlocks.java'].decode()
for n in ['struct_launcher','struct_scaffold','concrete_smooth']:
 declaration=re.search(r'\b'+n+r' = [^\n]+',old_blocks).group(0)
 tex=re.search(r'setBlockTextureName\(RefStrings.MODID \+ ":([^"]+)"\)',declaration).group(1)
 materials[n]=tex
for n in ['BlockBase','generic/BlockGeneric','generic/BlockRadResistant']:
 assert 'getIcon(' not in original['src/main/java/com/hbm/blocks/'+n+'.java'].decode(),'Original fixed preview material owner now overrides getIcon'
assert materials=={'struct_launcher':'struct_launcher','struct_scaffold':'struct_scaffold','concrete_smooth':'concrete'}
assert 'return 3;' in original['src/main/java/com/hbm/blocks/machine/MachineMagma.java'].decode().split('public int getHeightOffset()')[1].split('}')[0]
client=(root/'src/main/java/com/hbm/main/ClientProxy.java').read_text();bindings=[]
for tile,render in [('TileEntityMachineMagma','RenderMagma'),('TileEntityMultiblock','RenderMultiblock'),('TileEntitySoyuzStruct','RenderSoyuzMultiblock')]:
 line='ClientRegistry.bindTileEntitySpecialRenderer('+tile+'.class, new '+render+'());';assert client.count(line)==1;bindings.append(line)
production=['render/tileentity/'+n for n in names]+['lib/RefStrings','render/item/nativeitem/MaterialTextureAtlasSprite','render/item/nativeitem/context/NativeProjectionScope','render/item/nativeitem/context/NativeItemFailureScope','render/item/nativeitem/context/NativeItemFields']+['render/mesh/'+n for n in ['MeshModel','ObjMesh','ObjMeshParser','ObjMeshRenderer','ObjMeshVboPayload','ReloadableObjMesh','ObjMeshModel']]+['render/loader/HFRWavefrontObject','render/loader/HFRWavefrontObjectVBO']
with tempfile.TemporaryDirectory(prefix='tile-visuals-native-') as td:
 tmp=P(td);files=[root/('src/main/java/com/hbm/'+n+'.java') for n in production]
 files.append(root/'porting/tests/MaterialTextureAtlasSpriteNativeTest.java')
 def fixture(name,s):
  f=tmp/'fixtures'/(name+'.java');f.parent.mkdir(parents=True,exist_ok=True);f.write_text(s);files.append(f)
 fixture('com/hbm/blocks/BlockDummyable','''package com.hbm.blocks; public class BlockDummyable extends net.minecraft.block.Block { public static final int offset=10; public int height=3;public BlockDummyable(){super(net.minecraft.block.material.Material.IRON);}public int getHeightOffset(){return height;} }''')
 fixture('com/hbm/blocks/ModBlocks','''package com.hbm.blocks; public class ModBlocks { public static net.minecraft.block.Block machine_magma=new BlockDummyable(),struct_launcher=net.minecraft.init.Blocks.STONE,struct_scaffold=net.minecraft.init.Blocks.GLASS,concrete_smooth=net.minecraft.init.Blocks.COBBLESTONE,struct_launcher_core=net.minecraft.init.Blocks.GOLD_BLOCK,struct_launcher_core_large=net.minecraft.init.Blocks.IRON_BLOCK; }''')
 fixture('com/hbm/tileentity/machine/TileEntityMachineMagma','''package com.hbm.tileentity.machine;public class TileEntityMachineMagma extends net.minecraft.tileentity.TileEntity {public int metadata;public float prevDrillRotation,drillRotation,prevLavaHeight,lavaHeight;@Override public int getBlockMetadata(){return metadata;} }''')
 for n in ['TileEntityMultiblock','TileEntitySoyuzStruct']:
  fixture('com/hbm/tileentity/machine/'+n,'package com.hbm.tileentity.machine;public class '+n+' extends net.minecraft.tileentity.TileEntity {public net.minecraft.block.Block block;@Override public net.minecraft.block.Block getBlockType(){return block;}}')
 fixture('com/hbm/main/ResourceManager',(here/'ResourceManager.java.in').read_text())
 fixture('tileoracle/Bindings','package tileoracle;import com.hbm.render.tileentity.*;import com.hbm.tileentity.machine.*;import net.minecraftforge.fml.client.registry.ClientRegistry;public class Bindings {public static void bind(){'+''.join(bindings)+'}}')
 for name in names:
  s=original['src/main/java/com/hbm/render/tileentity/'+name+'.java'].decode()
  if name=='RenderMagma':
   # CP49's item-provider path was already independently ported. This oracle tests
   # only the untouched original world callback, not the removed legacy item API.
   start=s.index('public IItemRenderer getRenderer()');brace=s.index('{',start);depth=1;i=brace+1
   while depth:
    if s[i]=='{':depth+=1
    if s[i]=='}':depth-=1
    i+=1
   s=s[:start]+s[i:]
   s=s.replace(' implements IItemRendererProvider','').replace('import com.hbm.render.item.ItemRenderBase;\n','').replace('import net.minecraftforge.client.IItemRenderer;\n','')
  s=s.replace('package com.hbm.render.tileentity;','package tileoracle;')
  s=s.replace(name,'Original'+name).replace('extends TileEntitySpecialRenderer {','extends TileEntitySpecialRenderer<TileEntity> {').replace('@Override\n','')
  s=s.replace('import net.minecraft.client.renderer.RenderBlocks;','import tileoracle.LegacyBlocksRenderer;').replace('RenderBlocks','LegacyBlocksRenderer')
  s=s.replace('import net.minecraft.client.renderer.Tessellator;','import tileoracle.LegacyTess;').replace('Tessellator','LegacyTess')
  s=s.replace('import net.minecraft.util.IIcon;','import net.minecraft.client.renderer.texture.TextureAtlasSprite;').replace('IIcon','TextureAtlasSprite')
  s=s.replace('TextureMap.locationBlocksTexture','TextureMap.LOCATION_BLOCKS_TEXTURE')
  s=s.replace('import net.minecraft.init.Blocks;','import tileoracle.LegacyBlocks;').replace('Blocks.lava','LegacyBlocks.lava')
  fixture('tileoracle/Original'+name,s)
 for name in ['LegacyTess','LegacyBlocksRenderer','LegacyBlocks']:fixture('tileoracle/'+name,(here/(name+'.java.in')).read_text())
 files.append(here/'TileVisualsNativeTest.java')
 for jar in a.classpath.split(os.pathsep):
  if 'lwjgl-platform' in jar and jar.endswith('natives-linux.jar'):
   with zipfile.ZipFile(jar) as z:
    for n in z.namelist():
     if n.endswith('.so') and '/' not in n:(tmp/n).write_bytes(z.read(n))
 assert (tmp/'liblwjgl64.so').exists(),'Existing Linux LWJGL libraries required; no downloads'
 scope={'base':'caf22cf027f33f9d0d0be366454c2f54fdba7915','complete_compiled_sources':{str(f.relative_to(root)):hashlib.sha256(f.read_bytes()).hexdigest() for f in files if root in f.parents and '/src/' in str(f)},'original_sources':{n:hashlib.sha256(s).hexdigest() for n,s in original.items()},'assets':{n:hashlib.sha256((root/'src/main/resources/assets/hbm'/n).read_bytes()).hexdigest() for n in assets},'binding_fragments':bindings,'original_materials':materials,'fixtures':[str(f.relative_to(tmp)) for f in files if tmp in f.parents],'test_inputs':{str(f.relative_to(root)):hashlib.sha256(f.read_bytes()).hexdigest() for f in files if root in f.parents and '/porting/' in str(f)},'test_templates':{f.name:hashlib.sha256(f.read_bytes()).hexdigest() for f in [here/'run.py',*sorted(here.glob('*.java.in'))]}}
 if a.write_scope:
  (here/'scope.json').write_text(json.dumps(scope,indent=2)+'\n')
  with zipfile.ZipFile(here/'original-sources.zip','w',zipfile.ZIP_DEFLATED) as z:
   for n,s in original.items():z.writestr(n,s)
 else:assert json.loads((here/'scope.json').read_text())==scope,'Frozen production/original/fixture/material scope changed'
 if a.negative_control:
  mutations={
   'lava-uv':('RenderMagma','tex(lava.getMaxU(), lava.getMaxV())','tex(lava.getMinU(), lava.getMaxV())','magma 0'),
   'preview-geometry':('RenderMultiblock','float pixel = 1F/16F;','float pixel = 1F/8F;','compact/unowned vertex'),
   'animation-direction':('RenderMagma','GL11.glRotatef(-drillRotation, 0, 1, 0);','GL11.glRotatef(drillRotation, 0, 1, 0);','original interpolated opposed drill rotation')}
  name,before,after,wanted=mutations[a.negative_control];source=root/('src/main/java/com/hbm/render/tileentity/'+name+'.java');text=source.read_text();assert text.count(before)==1
  mutated=tmp/'mutant'/(name+'.java');mutated.parent.mkdir();mutated.write_text(text.replace(before,after));files[files.index(source)]=mutated
 subprocess.run([str(a.java_home/'bin'/('javac'+exe)),'-encoding','UTF-8','-source','8','-target','8','-sourcepath',str(tmp),'-cp',a.classpath,'-d',str(tmp)]+list(map(str,files)),check=True)
 print('PASS all three complete native TESRs, existing native mesh/state helpers and exact native registration statements compile.',flush=True)
 command=[str(a.java_home/'bin'/('java'+exe)),'-Xmx1300m','-Djava.awt.headless=true','-Djava.library.path='+str(tmp),'-cp',str(tmp)+os.pathsep+a.classpath,'TileVisualsNativeTest',str(a.legacy_client.resolve()),str(root/'src/main/resources')]
 if a.negative_control:
  result=subprocess.run(command,cwd=tmp,env=dict(os.environ,MESA_SHADER_CACHE_DISABLE='true'),text=True,stdout=subprocess.PIPE,stderr=subprocess.STDOUT)
  assert result.returncode!=0 and 'AssertionError' in result.stdout and wanted in result.stdout,result.stdout
  print('PASS behavioral negative control '+a.negative_control+' rejected by actual native/original assertion: '+next(line for line in result.stdout.splitlines() if 'AssertionError' in line),flush=True)
 else:subprocess.run(command,cwd=tmp,env=dict(os.environ,MESA_SHADER_CACHE_DISABLE='true'),check=True)
