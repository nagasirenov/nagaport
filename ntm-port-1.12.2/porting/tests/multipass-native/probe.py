#!/usr/bin/env python3
"""Native multipass model/registry and exact owner-render-hook proof; complete Fissure owner.
Other NTM mechanics collaborators are explicit fixtures. No Minecraft/Forge substitutes.
"""
import argparse,hashlib,json,os,re,subprocess,tempfile
from pathlib import Path
H=Path(__file__).resolve().parent;R=H.parents[2];S=R/'src/main/java'
p=argparse.ArgumentParser();p.add_argument('--java-home',required=True,type=Path);p.add_argument('--classpath',required=True);p.add_argument('--legacy-client',required=True);p.add_argument('--material-source',type=Path,default=S/'com/hbm/render/util/NativeBlockMaterial.java');a=p.parse_args()
def method(s,sig):
 st=s.index(sig);i=s.index('{',st)+1;depth=1
 while depth:depth+=(s[i]=='{')-(s[i]=='}');i+=1
 return s[st:i]
registrations=json.loads((H/'registrations.json').read_text())
production=['com/hbm/blocks/IBlockMultiPass.java','com/hbm/blocks/MultipassContext.java','com/hbm/blocks/generic/BlockFissure.java','com/hbm/blocks/BlockBase.java','com/hbm/render/icon/IIcon.java','com/hbm/render/icon/IIconRegister.java','com/hbm/lib/RefStrings.java','com/hbm/render/block/RenderBlockMultipass.java','com/hbm/render/block/multipass/MultipassMaterials.java','com/hbm/main/MultipassModelHandler.java','com/hbm/render/block/nativeblock/ConveyorCuboidGeometry.java','com/hbm/render/block/ct/RenderBlocksCT.java','com/hbm/render/block/ct/CTContext.java','com/hbm/render/block/ct/IBlockCT.java','com/hbm/render/block/ct/CT.java','com/hbm/lib/ForgeDirection.java']
with tempfile.TemporaryDirectory(prefix='multipass-native-') as temp:
 d=Path(temp);files=[]
 def fixture(rel,text):
  f=d/rel;f.parent.mkdir(parents=True,exist_ok=True);f.write_text(text);files.append(str(f))
 fixtures={
 'com/hbm/dim/SolarSystem.java':'''package com.hbm.dim; public class SolarSystem { public enum Body { ORBIT("minecraft:stone"),KERBIN("minecraft:stone"),MUN("hbm:moon_rock"),MINMUS("hbm:minmus_stone"),DUNA("hbm:duna_rock"),MOHO("hbm:moho_stone"),DRES("hbm:dresbase"),EVE("hbm:eve_stone_2"),IKE("hbm:ike_stone"),LAYTHE("minecraft:stone"),TEKTO("hbm:basalt"); final String texture; Body(String t){texture=t;} public net.minecraft.util.ResourceLocation getStoneTexture(){String[] p=texture.split(":");return new net.minecraft.util.ResourceLocation(p[0],"textures/blocks/"+p[1]+".png");} } }''',
 'com/hbm/tileentity/TileEntityLoadedBase.java':'''package com.hbm.tileentity; public class TileEntityLoadedBase extends net.minecraft.tileentity.TileEntity {}''',
 'com/hbm/inventory/fluid/FluidType.java':'''package com.hbm.inventory.fluid; public class FluidType { public int color=0x123456;public int getColor(){return color;} }''',
 'com/hbm/inventory/fluid/Fluids.java':'''package com.hbm.inventory.fluid; public class Fluids {public static final FluidType LAVA=new FluidType();}''',
 'com/hbm/inventory/fluid/tank/FluidTank.java':'''package com.hbm.inventory.fluid.tank; import com.hbm.inventory.fluid.FluidType; public class FluidTank {public final FluidType type;public final int capacity;public int fill;public FluidTank(FluidType t,int c){type=t;capacity=c;}public void setFill(int n){fill=n;} }''',
 'api/hbm/fluid/IFluidStandardSender.java':'''package api.hbm.fluid; import com.hbm.inventory.fluid.*;import com.hbm.inventory.fluid.tank.*;import com.hbm.lib.ForgeDirection;import net.minecraft.world.World;public interface IFluidStandardSender {boolean canConnect(FluidType type,ForgeDirection dir);FluidTank[] getAllTanks();FluidTank[] getSendingTanks();class Trace {public static int calls,x,y,z;public static FluidTank tank;public static World world;public static ForgeDirection dir;} default void sendFluid(FluidTank tank,World world,int x,int y,int z,ForgeDirection dir){Trace.calls++;Trace.tank=tank;Trace.world=world;Trace.x=x;Trace.y=y;Trace.z=z;Trace.dir=dir;} }''',
 'com/hbm/blocks/ModBlocks.java':'package com.hbm.blocks; public class ModBlocks { public static net.minecraft.block.Block rad_lava_block,volcanic_lava_block; }'
 }
 for rel,text in fixtures.items():fixture(rel,text)
 baseclasses={r['owner']:r['kind'] for r in registrations};names=[]
 for full,kind in baseclasses.items():
  cls=full.split('.')[-1];pkg=full.rsplit('.',1)[0]
  if cls=='BlockFissure':continue
  if cls in ('BlockOreOutgas','BlockOreFluid','BlockCluster'):
   fixture(full.replace('.','/')+'.java','package '+pkg+';public class '+cls+' extends BlockOre {public '+cls+'(){super();}}');continue
  source=(S/(full.replace('.','/')+'.java')).read_text()
  signatures=['protected net.minecraft.block.state.BlockStateContainer createBlockState','public net.minecraft.block.state.IBlockState getStateFromMeta','public int getMetaFromState','public net.minecraft.util.EnumBlockRenderType getRenderType','public net.minecraft.util.BlockRenderLayer getRenderLayer','public net.minecraft.block.state.IBlockState getExtendedState','public int getPasses']
  for optional in ['public '+cls+' withSoundType','public MultipassContext getMultipassContext','public boolean shouldRenderItemMulti','public boolean shouldSideBeRendered(net.minecraft.block.state.IBlockState']:
   if optional in source:signatures.append(optional)
  methods='\n'.join(method(source,s) for s in signatures)
  nested=''
  match=re.search(r'if\(tile instanceof (\w+)\)',methods)
  if match:
   nested='''public static class '''+match[1]+''' extends TileEntity {public net.minecraft.block.Block block;public int meta,color=0x13579b,shape=3;public com.hbm.lib.ForgeDirection ejectionDir=com.hbm.lib.ForgeDirection.NORTH,insertionDir=com.hbm.lib.ForgeDirection.SOUTH;public int getColor(){return color;}public com.hbm.inventory.fluid.FluidType getType(){return new com.hbm.inventory.fluid.FluidType();}}'''
  fixture(full.replace('.','/')+'.java','package '+pkg+';import com.hbm.blocks.*;import net.minecraft.tileentity.TileEntity; public class '+cls+' extends net.minecraft.block.Block implements IBlockMultiPass { public '+cls+'(){super(net.minecraft.block.material.Material.ROCK);} '+methods+nested+'}')
  print('EXTRACTED OWNER RENDER HOOKS',full,','.join(signatures),flush=True)
 initializers='\n'.join('owners.put("'+r['name']+'",new '+r['owner']+'().setRegistryName("hbm","tile.'+r['name']+'"));' for r in registrations)
 test=(H/'MultipassNativeTest.java.in').read_text().replace('INITIALIZERS',initializers)
 fixture('MultipassNativeTest.java',test)
 for rel in production:
  f=S/rel;files.append(str(f));print('PRODUCTION',rel,hashlib.sha256(f.read_bytes()).hexdigest(),flush=True)
 assert a.material_source.exists(),'Shared NativeBlockMaterial source required (from native block-entity checkpoint)'
 files.append(str(a.material_source));print('SHARED PRODUCTION DEPENDENCY',a.material_source.name,hashlib.sha256(a.material_source.read_bytes()).hexdigest(),flush=True)
 subprocess.run([str(a.java_home/'bin/javac'),'-encoding','UTF-8','-source','8','-target','8','-sourcepath',temp,'-cp',a.classpath,'-d',temp]+files,check=True)
 subprocess.run([str(a.java_home/'bin/java'),'-Djava.awt.headless=true','-Xmx1g','-cp',temp+os.pathsep+a.classpath,'MultipassNativeTest',a.legacy_client,str(R/'src/main/resources')],cwd=temp,check=True)
