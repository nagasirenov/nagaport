#!/usr/bin/env python3
"""Finite complete diode/partitioner owner+model compile, real MC/Forge APIs.
The explicitly enumerated NTM-domain collaborators are test doubles, not transport acceptance.
"""
import argparse,hashlib,json,os,re,subprocess,tempfile,runpy
from pathlib import Path
p=argparse.ArgumentParser();p.add_argument('--module',type=Path,help='Production/test module, defaults to this script parent module');p.add_argument('--java-home',type=Path,required=True);p.add_argument('--classpath',required=True);p.add_argument('--base',type=Path,required=True);p.add_argument('--pristine',type=Path,required=True);p.add_argument('--legacy-client',type=Path,required=True);p.add_argument('--legacy-forge-userdev',type=Path,required=True);a=p.parse_args()
r=a.module or Path(__file__).resolve().parents[2];s=r/'src/main/java';t=r/'porting/tests'
collaborators={
'com/hbm/blocks/ILookOverlay.java':'''package com.hbm.blocks; public interface ILookOverlay {void printHook(net.minecraftforge.client.event.RenderGameOverlayEvent.Pre e,net.minecraft.world.World w,int x,int y,int z);static void printGeneric(net.minecraftforge.client.event.RenderGameOverlayEvent.Pre e,String n,int a,int b,java.util.List<String> text){}}''',
'com/hbm/util/i18n/I18nUtil.java':'package com.hbm.util.i18n; public class I18nUtil {public static String[] resolveKeyArray(String s){return new String[]{s};}public static String resolveKey(String s){return s;}}',
'com/hbm/inventory/RecipesCommon.java':'package com.hbm.inventory; public class RecipesCommon {public static class ComparableStack {public ComparableStack(net.minecraft.item.ItemStack s){}}}',
'com/hbm/util/BobMathUtil.java':'package com.hbm.util; public class BobMathUtil {public static String getShortNumber(long n){return Long.toString(n);}}',
'com/hbm/util/Compat.java':'''package com.hbm.util; public class Compat {public static net.minecraft.tileentity.TileEntity getTileStandard(net.minecraft.world.World w,int x,int y,int z){return w.getTileEntity(new net.minecraft.util.math.BlockPos(x,y,z));}}''',
'com/hbm/util/CompatEnergyControl.java':'package com.hbm.util; public class CompatEnergyControl {public static final String L_ENERGY_HE="energy",L_CAPACITY_HE="capacity";}',
'com/hbm/lib/Library.java':'''package com.hbm.lib; public class Library { public static final ForgeDirection POS_X=ForgeDirection.EAST,NEG_X=ForgeDirection.WEST,POS_Y=ForgeDirection.UP,NEG_Y=ForgeDirection.DOWN,POS_Z=ForgeDirection.SOUTH,NEG_Z=ForgeDirection.NORTH;
public static boolean canConnect(net.minecraft.world.IBlockAccess w,int x,int y,int z,ForgeDirection d){if(y<0||y>255)return false;net.minecraft.util.math.BlockPos p=new net.minecraft.util.math.BlockPos(x,y,z);net.minecraft.block.Block b=w.getBlockState(p).getBlock();if(b instanceof api.hbm.energymk2.IEnergyConnectorBlock&&((api.hbm.energymk2.IEnergyConnectorBlock)b).canConnect(w,x,y,z,d.getOpposite()))return true;net.minecraft.tileentity.TileEntity t=w.getTileEntity(p);return t instanceof api.hbm.energymk2.IEnergyConnectorMK2&&((api.hbm.energymk2.IEnergyConnectorMK2)t).canConnect(d.getOpposite());}}''',
'com/hbm/sound/AudioWrapper.java':'package com.hbm.sound; public class AudioWrapper {public void stopSound(){}public void startSound(){}}',
'com/hbm/handler/threading/PacketThreading.java':'package com.hbm.handler.threading; public class PacketThreading {public static void createAllAroundThreadedPacket(Object p,net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint t){}}',
'com/hbm/packet/toclient/BufPacket.java':'package com.hbm.packet.toclient; public class BufPacket {public BufPacket(int x,int y,int z,com.hbm.tileentity.IBufPacketReceiver r){}public io.netty.buffer.ByteBuf getCompiledBuffer(){return io.netty.buffer.Unpooled.buffer();}}',
'com/hbm/packet/toclient/AuxParticlePacketNT.java':'package com.hbm.packet.toclient; public class AuxParticlePacketNT {public AuxParticlePacketNT(net.minecraft.nbt.NBTTagCompound n,double x,double y,double z){}}',
'api/hbm/energymk2/IEnergyConductorMK2.java':'package api.hbm.energymk2; public interface IEnergyConductorMK2 extends IEnergyConnectorMK2 {Nodespace.PowerNode createNode();}',
'api/hbm/energymk2/Nodespace.java':'''package api.hbm.energymk2; public class Nodespace {public static final java.util.Map<net.minecraft.util.math.BlockPos,PowerNode> nodes=new java.util.HashMap<>();public static PowerNode getNode(net.minecraft.world.World w,int x,int y,int z){return nodes.get(new net.minecraft.util.math.BlockPos(x,y,z));}public static class PowerNode {public boolean expired;public Network net;public boolean hasValidNet(){return net!=null;}}public static class Network {public long accepted;public int subscriptions;public long sendPowerDiode(long p){accepted+=p;return 0;}public void addReceiver(IEnergyReceiverMK2 r){subscriptions++;}public void removeReceiver(IEnergyReceiverMK2 r){subscriptions--;}}}''',
'com/hbm/tileentity/TileEntityMachineBase.java':'''package com.hbm.tileentity;
public abstract class TileEntityMachineBase extends TileEntityLoadedBase implements net.minecraft.inventory.ISidedInventory,net.minecraft.util.ITickable {
public net.minecraft.item.ItemStack[] slots;public TileEntityMachineBase(int n){slots=new net.minecraft.item.ItemStack[n];}public abstract String getDefaultName();
public String getName(){return getDefaultName();}public boolean hasCustomName(){return false;}public net.minecraft.util.text.ITextComponent getDisplayName(){return new net.minecraft.util.text.TextComponentString(getName());}
public int getSizeInventory(){return slots.length;}public boolean isEmpty(){for(net.minecraft.item.ItemStack s:slots)if(s!=null&&!s.isEmpty())return false;return true;}
public net.minecraft.item.ItemStack getStackInSlot(int i){return slots[i]==null?net.minecraft.item.ItemStack.EMPTY:slots[i];}public net.minecraft.item.ItemStack decrStackSize(int i,int n){return slots[i].splitStack(n);}public net.minecraft.item.ItemStack removeStackFromSlot(int i){net.minecraft.item.ItemStack s=getStackInSlot(i);slots[i]=null;return s;}public void setInventorySlotContents(int i,net.minecraft.item.ItemStack s){slots[i]=s.isEmpty()?null:s;}public int getInventoryStackLimit(){return 64;}public boolean isUsableByPlayer(net.minecraft.entity.player.EntityPlayer p){return true;}public void openInventory(net.minecraft.entity.player.EntityPlayer p){}public void closeInventory(net.minecraft.entity.player.EntityPlayer p){}public int getField(int i){return 0;}public void setField(int i,int v){}public int getFieldCount(){return 0;}public void clear(){java.util.Arrays.fill(slots,null);}public boolean canInsertItem(int i,net.minecraft.item.ItemStack s,net.minecraft.util.EnumFacing f){return isItemValidForSlot(i,s);}
}''',
'com/hbm/entity/item/EntityMovingItem.java':'''package com.hbm.entity.item; public class EntityMovingItem extends net.minecraft.entity.Entity {public net.minecraft.item.ItemStack stack;public EntityMovingItem(net.minecraft.world.World w){super(w);}public void setItemStack(net.minecraft.item.ItemStack s){stack=s;}protected void entityInit(){}protected void readEntityFromNBT(net.minecraft.nbt.NBTTagCompound n){}protected void writeEntityToNBT(net.minecraft.nbt.NBTTagCompound n){}}''',
'com/hbm/inventory/recipes/CrystallizerRecipes.java':'''package com.hbm.inventory.recipes; public class CrystallizerRecipes {public static int getAmount(net.minecraft.item.ItemStack s){return s!=null&&!s.isEmpty()&&s.getItem()==net.minecraft.init.Items.IRON_INGOT?3:0;}}''',
'com/hbm/util/InventoryUtil.java':'''package com.hbm.util; public class InventoryUtil {public static int start,end;public static net.minecraft.item.ItemStack tryAddItemToInventory(net.minecraft.inventory.IInventory i,int s,int e,net.minecraft.item.ItemStack stack){start=s;end=e;i.setInventorySlotContents(s,stack.copy());return null;}}'''
}
basefiles=['com/hbm/util/Tuple.java','com/hbm/interfaces/Spaghetti.java','com/hbm/util/fauxpointtwelve/BlockPos.java','com/hbm/util/fauxpointtwelve/Rotation.java','api/hbm/block/IToolable.java','api/hbm/energymk2/IEnergyConnectorBlock.java','api/hbm/energymk2/IEnergyConnectorMK2.java','api/hbm/energymk2/IEnergyHandlerMK2.java','api/hbm/energymk2/IEnergyReceiverMK2.java','api/hbm/tile/ILoadedTile.java','com/hbm/interfaces/NotableComments.java','com/hbm/util/fauxpointtwelve/DirPos.java','com/hbm/tileentity/TileEntityLoadedBase.java','com/hbm/tileentity/IBufPacketReceiver.java','com/hbm/blocks/ITooltipProvider.java','com/hbm/lib/ForgeDirection.java','com/hbm/lib/RefStrings.java','com/hbm/blocks/network/ConnectionState.java','api/hbm/conveyor/IConveyorBelt.java','api/hbm/conveyor/IConveyorItem.java','api/hbm/conveyor/IConveyorPackage.java','api/hbm/conveyor/IEnterableBlock.java','com/hbm/render/block/nativeblock/AtlasObjGeometry.java','com/hbm/render/block/nativeblock/AtlasObjBakedModel.java','com/hbm/render/block/nativeblock/BlockPositionLightQuad.java','com/hbm/render/block/nativeblock/ConveyorCuboidGeometry.java','com/hbm/render/block/nativeblock/ThermalModelTransforms.java','com/hbm/render/mesh/ObjMesh.java','com/hbm/render/mesh/ObjMeshParser.java']
production=['com/hbm/render/tileentity/NetworkBreakingRenderer.java','com/hbm/blocks/network/CableDiode.java','com/hbm/blocks/network/CranePartitioner.java','com/hbm/render/block/RenderDiode.java','com/hbm/render/block/RenderPartitioner.java','com/hbm/render/block/nativeblock/DiodeBakedModel.java','com/hbm/main/DiodeNetworkModelHandler.java']
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
manifest={'scope':'Complete diode and partitioner owners plus model paths, explicit NTM-domain collaborators; not whole-mod or functional transport acceptance; exact-owner custom native damage route verified through actual dispatcher and colorless buffers; GL upload/state restoration is bytecode-audited, not live-tested.','production':{},'pristine':{},'assets':{},'collaborators':sorted(collaborators)}
for rel in ['com/hbm/blocks/BlockDummyable.java','com/hbm/blocks/network/CraneSplitter.java','com/hbm/tileentity/network/TileEntityCraneSplitter.java','api/hbm/energymk2/Nodespace.java','api/hbm/energymk2/PowerNetMK2.java','com/hbm/blocks/network/CableDiode.java','com/hbm/blocks/network/CranePartitioner.java','com/hbm/render/block/RenderDiode.java','com/hbm/render/block/RenderPartitioner.java','com/hbm/render/util/ObjUtil.java']:
 manifest['pristine']['src/main/java/'+rel]=sha(a.pristine/'src/main/java'/rel)
for asset in ['models/blocks/cable_neo.obj','models/blocks/crane_buffer.obj','textures/blocks/crane_splitter_belt.png.mcmeta']+['textures/blocks/'+n+'.png' for n in ['cable_diode','hadron_coil_alloy','cable_neo','crane_partitioner_side','crane_partitioner_back','crane_top','crane_splitter_inner','crane_splitter_inner_side','crane_splitter_belt']]:
 rel='src/main/resources/assets/hbm/'+asset;assert sha(a.base/rel)==sha(a.pristine/rel);manifest['assets'][rel]=sha(a.pristine/rel)
def method(text,name):
 m=re.search(r'(?:public|private) (?:static )?(?:void|boolean) '+name+r'\([^)]*\)\s*\{',text);assert m,name
 i=m.end();depth=1
 while depth:depth+=(text[i]=='{')-(text[i]=='}');i+=1
 return text[m.start():i]
oracle_methods=[]
for owner,prefix in [('Diode','diode'),('Partitioner','partitioner')]:
 source=(a.pristine/('src/main/java/com/hbm/render/block/Render'+owner+'.java')).read_text()
 for name in ['renderWorldBlock','renderInventoryBlock']+(['drawPartitioner'] if owner=='Partitioner' else []):
  body=method(source,name)
  for old,new in {'Block':'OracleBlock','CranePartitioner':'OracleBlock','IBlockAccess':'OracleWorld','IIcon':'int'}.items():body=re.sub(r'\b'+old+r'\b',new,body)
  body=body.replace('renderWorldBlock',prefix+'World').replace('renderInventoryBlock',prefix+'Item')
  oracle_methods.append(body)
oracle=(t/'diode-network-oracle.java.in').read_text().replace('/* METHODS */','\n'.join(oracle_methods))
with tempfile.TemporaryDirectory(prefix='native-diode-network-') as d:
 out=Path(d);files=[]
 for rel,body in collaborators.items():
  f=out/rel;f.parent.mkdir(parents=True,exist_ok=True);f.write_text(body);files.append(str(f))
 for rel in production+basefiles:
  f=s/rel if rel in production else a.base/'src/main/java'/rel
  files.append(str(f));manifest['production'][('' if rel in production else 'base/')+'src/main/java/'+rel]=sha(f)
 f=out/'LegacyNetworkOracle.java';f.write_text(oracle);files.append(str(f))
 files.append(str(t/'DiodeNetworkNativeTest.java'))
 subprocess.run([str(a.java_home/'bin/javac'),'-encoding','UTF-8','-source','8','-target','8','-sourcepath',d,'-cp',a.classpath,'-d',d]+files,check=True)
 runpy.run_path(str(t/'audit-native-diode-damage.py'))['audit'](a.java_home,d+os.pathsep+a.classpath,a.legacy_client,a.legacy_forge_userdev,t/'diode-damage-bytecode-evidence.txt')
 print('PASS complete native CableDiode, CranePartitioner, embedded tiles and selected model sources compile.',flush=True)
 subprocess.run([str(a.java_home/'bin/java'),'-Xmx1200m','-Djava.awt.headless=true','-cp',d+os.pathsep+a.classpath,'DiodeNetworkNativeTest',str(a.base),str(a.pristine)],cwd=d,check=True)
manifest_path=t/'diode-network-source-manifest.json'
if manifest_path.exists():
 previous=json.loads(manifest_path.read_text());assert previous['pristine']==manifest['pristine'],'Pinned pristine sources changed';assert previous['assets']==manifest['assets'],'Pinned assets changed'
manifest_path.write_text(json.dumps(manifest,indent=2,sort_keys=True)+'\n')
