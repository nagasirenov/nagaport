#!/usr/bin/env python3
"""Native fluid model parity using pinned original selectors and an independent OBJ oracle.
Only NTM block/registry collaborators are controlled; no legacy Minecraft/Forge facades.
"""
import argparse, hashlib, os, re, subprocess, tempfile, zipfile
from pathlib import Path
p=argparse.ArgumentParser()
p.add_argument('--java-home',type=Path,required=True)
p.add_argument('--classpath',required=True)
p.add_argument('--original-root',type=Path)
p.add_argument('--legacy-client',type=Path,help='Optional already-cached original 1.7.10 client jar; never downloaded')
a=p.parse_args();root=Path(__file__).resolve().parents[2];src=root/'src/main/java';tests=root/'porting/tests'
if a.original_root is None:a.original_root=root.parent/'ntm-original-pristine'
exe='.exe' if os.name=='nt' else ''
if a.legacy_client is not None:
 got=hashlib.sha256(a.legacy_client.read_bytes()).hexdigest()
 assert got=='a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce', 'Unexpected original 1.7.10 client jar'
 print('PINNED ORIGINAL CLIENT',got,flush=True)
if not (a.java_home/'bin'/('javac'+exe)).is_file():a.java_home=a.java_home.parent
pinned={
 'src/main/java/com/hbm/render/block/RenderTestPipe.java':'a80237451058b741b2530a125429fc1511ef835d799a7823618c81d914fee44b',
 'src/main/java/com/hbm/render/util/ObjUtil.java':'570a88abd771875467643ec6d5e4f6b3f61756f80754b71cc73133b77f2b8608',
 'src/main/java/com/hbm/blocks/network/FluidDuctStandard.java':'b4a840a39fb3f0d530da6e7781790ae919dcce70b5b64c0cfb4dcaa2f10629ca',
 'src/main/java/com/hbm/inventory/fluid/Fluids.java':'11bd1035cc0fdd3cccda8609effdf143be80da6faf907106099da9998325d716',
 'src/main/resources/assets/hbm/models/blocks/pipe_neo.obj':'ef184f682148ff20754d745045f67196426e144f8970bc4017223957e93fa45d',
 'src/main/java/com/hbm/blocks/IBlockMulti.java':'9dc3339ef55b0719b0c36077dc5823789d366c52c2f980a11562d0d5e915c0ad',
 'src/main/resources/assets/hbm/textures/blocks/pipe_neo.png':'976bff02a25ee9d999418812a0fcb0895acdebc427ae4b65677f473c7c4af29f',
 'src/main/resources/assets/hbm/textures/blocks/pipe_neo_overlay.png':'0b5efb2c7e86669a98c10cee757d40ef736b0f1fa04068f93294c44db45caedf',
 'src/main/resources/assets/hbm/textures/blocks/pipe_silver.png':'2937b45ffe679ba2e07b0dcdb9370cf33c3f755dd8b89b4915889a96c592f01a',
 'src/main/resources/assets/hbm/textures/blocks/pipe_silver_overlay.png':'9fce7d76994749e59b6fae792af3b6c9c5ff99eae5bddd21cc8ac4d4367f3cf5',
 'src/main/resources/assets/hbm/textures/blocks/pipe_colored.png':'0b7d734c0f55830f6b5bc2ec81f182a4fc9c828faa4dec911dfb3eda8defb330',
 'src/main/resources/assets/hbm/textures/blocks/pipe_colored_overlay.png':'6be232f836ca0693d61efd4ee16d924085b46401406cb1d8bd481dec91e9d083',
}
for rel,want in pinned.items():
 got=hashlib.sha256((a.original_root/rel).read_bytes()).hexdigest()
 assert got==want, f'Original source changed: {rel}: {got} != {want}'
 print('PINNED ORIGINAL',rel,got,flush=True)
assets=['models/blocks/pipe_neo.obj']+['textures/blocks/'+skin+suffix+'.png' for skin in ['pipe_neo','pipe_silver','pipe_colored'] for suffix in ['', '_overlay']]
for asset in assets:
 rel='src/main/resources/assets/hbm/'+asset
 assert (a.original_root/rel).read_bytes()==(root/rel).read_bytes(), 'Native fluid assets must retain original bytes: '+rel
 print('UNCHANGED ORIGINAL ASSET',asset,hashlib.sha256((root/rel).read_bytes()).hexdigest(),flush=True)
def method(text,name):
 m=re.search(r'(?:public|private) (?:static )?(?:void|boolean) '+name+r'\([^)]*\)\s*\{',text);assert m,name
 end=m.end();depth=1
 while depth:depth+=(text[end]=='{')-(text[end]=='}');end+=1
 return text[m.end():end-1]
original=(a.original_root/'src/main/java/com/hbm/render/block/RenderTestPipe.java').read_text()
selector=method(original,'renderWorldBlock');selector=selector[selector.index('if(mask == 0)'):selector.index('tessellator.addTranslation(-x')]
selector=re.sub(r'renderDuct\(iicon, overlay, color, tessellator, "([^"]+)"\);',r'result.add("\1");',selector)
item=method(original,'renderInventoryBlock')
assert 'GL11.glRotated(180, 0, 1, 0);' in item and 'GL11.glScaled(1.25D, 1.25D, 1.25D);' in item
itemparts=re.findall(r'renderPartWithIcon\(\(HFRWavefrontObject\) ResourceManager.pipe_neo, "([^"]+)", (iicon|overlay)',item)
assert len(itemparts)==8
worldlayers=re.findall(r'ResourceManager.pipe_neo, part, (iicon|overlay)',method(original,'renderDuct'))
assert worldlayers==['iicon','overlay']
shader=(a.original_root/'src/main/java/com/hbm/render/util/ObjUtil.java').read_text()
shader=shader[shader.index('if(shadow || hasColor)'):]
shader=shader[:shader.index('for(int i = 0; i < f.vertices.length; i++)')]
flags='boolean pX=(mask&32)!=0,nX=(mask&16)!=0,pY=(mask&8)!=0,nY=(mask&4)!=0,pZ=(mask&2)!=0,nZ=(mask&1)!=0;'
fluid_source=(a.original_root/'src/main/java/com/hbm/inventory/fluid/Fluids.java').read_text()
none=re.search(r'NONE\s*=\s*new FluidType\("NONE",\s*(0x[0-9a-fA-F]+)',fluid_source).group(1)
oracle="""import java.util.*;
final class OriginalFluidOracle {
 static final int NONE_COLOR="""+none+""";
 static final class Layer {final String group; final boolean overlay;Layer(String group,boolean overlay){this.group=group;this.overlay=overlay;}}
 static List<Layer> layers(int mask,boolean item){List<Layer> out=new ArrayList<>();if(item){
"""+''.join('out.add(new Layer("'+name+'",'+str(layer=='overlay').lower()+'));' for name,layer in itemparts)+"""
 }else for(String group:groups(mask)){"""+''.join('out.add(new Layer(group,'+str(layer=='overlay').lower()+'));' for layer in worldlayers)+"""}return out;}
 static List<String> groups(int mask){List<String> result=new ArrayList<>();"""+flags+selector+"""return result;}
 static final class Normal {final double xCoord,yCoord,zCoord;Normal(float x,float y,float z){xCoord=x;yCoord=y;zCoord=z;}}
 // Legacy Tessellator.setColorRGBA clamps channels after float/int conversion.
 static final class Color {int value=0xffffffff;void setColorOpaque(int r,int g,int b){r=Math.max(0,Math.min(255,r));g=Math.max(0,Math.min(255,g));b=Math.max(0,Math.min(255,b));value=0xff000000|(b<<16)|(g<<8)|r;}void setColorOpaque_F(float r,float g,float b){setColorOpaque((int)(r*255F),(int)(g*255F),(int)(b*255F));}}
 static int originalColor(float nx,float ny,float nz,boolean shadow,boolean hasColor,int rgb){
 int red=(rgb&0xff0000)>>16,green=(rgb&0x00ff00)>>8,blue=rgb&0xff;Normal normal=new Normal(nx,ny,nz);Color tes=new Color();
"""+shader+"""return tes.value;}
}
"""
lib=(src/'com/hbm/lib/Library.java').read_text();connect=method(lib,'canConnect')
collaborators={
 'com/hbm/blocks/network/FluidDuctStandard.java':"""package com.hbm.blocks.network;
 import net.minecraft.block.Block;import net.minecraft.block.material.Material;import net.minecraft.block.properties.*;import net.minecraft.block.state.*;import net.minecraftforge.common.property.*;
 public class FluidDuctStandard extends Block {
 public static final PropertyInteger METADATA=PropertyInteger.create("metadata",0,15);
 public FluidDuctStandard(Material material){super(material);setDefaultState(blockState.getBaseState().withProperty(METADATA,0));}
 protected BlockStateContainer createBlockState(){return new ExtendedBlockState(this,new IProperty<?>[]{METADATA},new IUnlistedProperty<?>[]{ConnectionState.CONNECTIONS,FluidConnectionState.FLUID_COLOR});}
 public IBlockState getStateFromMeta(int meta){return getDefaultState().withProperty(METADATA,meta&15);}public int getMetaFromState(IBlockState state){return state.getValue(METADATA);}
 public int rectify(int meta){return Math.abs(meta%3);}
 public boolean isOpaqueCube(IBlockState state){return false;}public boolean isFullCube(IBlockState state){return false;}
 }
 """,
 'com/hbm/inventory/fluid/FluidType.java':'package com.hbm.inventory.fluid;public final class FluidType {public int getColor(){return '+none+';}}',
 'com/hbm/inventory/fluid/Fluids.java':'package com.hbm.inventory.fluid;public final class Fluids {public static final FluidType NONE=new FluidType();}',
 'com/hbm/lib/Library.java':"""package com.hbm.lib;import net.minecraft.world.IBlockAccess;import net.minecraft.util.math.BlockPos;import net.minecraft.block.Block;import net.minecraft.tileentity.TileEntity;import api.hbm.energymk2.*;
 public final class Library {public static final ForgeDirection POS_X=ForgeDirection.EAST,NEG_X=ForgeDirection.WEST,POS_Y=ForgeDirection.UP,NEG_Y=ForgeDirection.DOWN,POS_Z=ForgeDirection.SOUTH,NEG_Z=ForgeDirection.NORTH;
 public static boolean canConnect(IBlockAccess world,int x,int y,int z,ForgeDirection dir){"""+connect+'}}',
}
production=[src/rel for rel in ['com/hbm/render/block/RenderTestPipe.java','com/hbm/main/FluidDuctModelHandler.java','com/hbm/lib/RefStrings.java','com/hbm/lib/ForgeDirection.java','api/hbm/energymk2/IEnergyConnectorBlock.java','api/hbm/energymk2/IEnergyConnectorMK2.java','com/hbm/render/mesh/ObjMesh.java','com/hbm/render/mesh/ObjMeshParser.java','com/hbm/blocks/network/ConnectionState.java','com/hbm/blocks/network/FluidConnectionState.java']]
production += [src/('com/hbm/render/block/nativeblock/'+name+'.java') for name in ['FluidDuctBakedModel','ConnectionQuadFactory','BlockPositionLightQuad','ThermalModelTransforms']]
with tempfile.TemporaryDirectory(prefix='fluid-model-native-') as tmp:
 out=Path(tmp);files=[]
 for rel,content in collaborators.items():
  assert rel.startswith('com/hbm/'),'Only NTM collaborators may be substituted'
  target=out/rel;target.parent.mkdir(parents=True,exist_ok=True);target.write_text(content);files.append(str(target))
 for source in production:
  print('PRODUCTION',source.relative_to(src),hashlib.sha256(source.read_bytes()).hexdigest(),flush=True);files.append(str(source))
 for name,content in [('OriginalFluidOracle',oracle),('FluidDuctModelNativeTest',(tests/'fluid-duct-model-native-test.java.in').read_text()),('LegacyFluidColorOracleTest',(tests/'fluid-duct-model-native-legacy-color-test.java.in').read_text())]:
  f=out/(name+'.java');f.write_text(content);files.append(str(f))
 subprocess.run([str(a.java_home/'bin'/('javac'+exe)),'-encoding','UTF-8','-source','8','-target','8','-cp',a.classpath,'-d',tmp]+files,check=True)
 if a.legacy_client is not None:
  subprocess.run([str(a.java_home/'bin'/('java'+exe)),'-Djava.awt.headless=true','-cp',os.pathsep.join([tmp,a.classpath]),'LegacyFluidColorOracleTest',str(a.legacy_client)],cwd=tmp,check=True)
 resources=root/'src/main/resources';jar=out/'fluid-model-resources.jar'
 with zipfile.ZipFile(jar,'w') as z:
  for asset in assets+['blockstates/tile.fluid_duct_neo.json']:
   path=resources/'assets/hbm'/asset;z.write(path,path.relative_to(resources).as_posix())
 subprocess.run([str(a.java_home/'bin'/('java'+exe)),'-Djava.awt.headless=true','-Xmx768m','-cp',os.pathsep.join([tmp,a.classpath]),'FluidDuctModelNativeTest',str(resources),str(jar),str(a.original_root/'src/main/resources/assets/hbm/models/blocks/pipe_neo.obj')],cwd=tmp,check=True)
