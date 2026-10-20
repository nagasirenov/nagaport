#!/usr/bin/env python3
"""Compile eight whole atlas-family block classes, three bases and nested capacitor
against real Minecraft/Forge/OC 1.12.2 dependencies, without native facades.
Controlled NTM collaborators isolate registries and external gameplay engines.
The full nested capacitor is pinned against pristine, allowing only native API
substitutions and the approved lifecycle override. Only the barrel tile lifecycle
override is extracted into a controlled collaborator; its full tile is not compiled.
"""
import argparse, hashlib, os, re, subprocess, sys, tempfile
from pathlib import Path
p=argparse.ArgumentParser();p.add_argument('--java-home',type=Path,required=True);p.add_argument('--classpath',required=True)
a=p.parse_args();root=Path(__file__).resolve().parents[2];src=root/'src/main/java';tests=root/'porting/tests';old=root.parent/'ntm-original-pristine/src/main/java'
for name,digest in {'MachineCapacitor':'116cb7f065429618dfe713973de000b519cea7198c064525fd49f044dffbd2c4','BlockFluidBarrel':'bdcd41828bdbb421aefa45ae2ca34c2b61a56ffeb466de29e3ca689207ae1dee'}.items():
 f=old/('com/hbm/blocks/machine/'+name+'.java');assert hashlib.sha256(f.read_bytes()).hexdigest()==digest
original=(old/'com/hbm/blocks/machine/MachineCapacitor.java').read_text();current=(src/'com/hbm/blocks/machine/MachineCapacitor.java').read_text()
start='public static class TileEntityCapacitor'
expected=original[original.index(start):].replace('CompatHandler.OCComponent {','CompatHandler.OCComponent, net.minecraft.util.ITickable {').replace('updateEntity()', 'update()').replace('worldObj','this.world')
for coord,getter in [('xCoord','this.pos.getX()'),('yCoord','this.pos.getY()'),('zCoord','this.pos.getZ()')]:expected=expected.replace(coord,getter)
expected=expected.replace('this.world.getBlock(pos.getX(), pos.getY(), pos.getZ())','this.world.getBlockState(pos).getBlock()').replace('this.world.getBlockMetadata(pos.getX(), pos.getY(), pos.getZ())','this.world.getBlockState(pos).getBlock().getMetaFromState(this.world.getBlockState(pos))').replace('pos.offset(current)','pos.add(current.offsetX, current.offsetY, current.offsetZ)')
expected=expected.replace('public void writeToNBT(NBTTagCompound nbt)', 'public NBTTagCompound writeToNBT(NBTTagCompound nbt)').replace('nbt.setLong("maxPower", maxPower);\n\t\t}', 'nbt.setLong("maxPower", maxPower);\n\t\t\treturn nbt;\n\t\t}')
lifecycle="\n\t\t/** Placement metadata does not change the stored energy tile's identity. */\n\t\t@Override\n\t\tpublic boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {\n\t\t\treturn oldState.getBlock() != newState.getBlock();\n\t\t}\n"
assert current.count(lifecycle)==1, 'Exact capacitor lifecycle override changed'
assert current[current.index(start):].replace(lifecycle,'')==expected, 'Nested capacitor changed beyond native APIs: energy, bus, packets, NBT, OC, ROR must remain exact.'
print('PASS exact pristine full nested capacitor control flow and payload preservation',flush=True)
collaborators={
'com/hbm/blocks/ModBlocks.java':'package com.hbm.blocks; public class ModBlocks {public static net.minecraft.block.Block capacitor_bus,capacitor_complex,barrel_plastic,barrel_corroded,barrel_iron,barrel_steel,barrel_tcalloy,barrel_antimatter,red_barrel,pink_barrel,yellow_barrel,oil_barrel,vitrified_barrel,lox_barrel,taint_barrel,toxic_block,gas_radon_dense,taint;}',
'com/hbm/main/MainRegistry.java':'package com.hbm.main; public class MainRegistry {public static final MainRegistry instance=new MainRegistry();public final java.util.Random rand=new java.util.Random(17);}',
'com/hbm/util/CompatExternal.java':'package com.hbm.util; public class CompatExternal {public static net.minecraft.tileentity.TileEntity getCoreFromPos(net.minecraft.world.World w,int x,int y,int z){return w.getTileEntity(new net.minecraft.util.math.BlockPos(x,y,z));}}',
'com/hbm/util/i18n/I18nUtil.java':'package com.hbm.util.i18n; public class I18nUtil {public static String resolveKey(String k){return k;}public static String[] resolveKeyArray(String k){return new String[]{k};}}',
'com/hbm/util/BobMathUtil.java':'package com.hbm.util; public class BobMathUtil {public static String getShortNumber(long n){return ""+n;}}',
'com/hbm/blocks/ILookOverlay.java':'package com.hbm.blocks; public interface ILookOverlay {void printHook(net.minecraftforge.client.event.RenderGameOverlayEvent.Pre p,net.minecraft.world.World w,int x,int y,int z);static void printGeneric(net.minecraftforge.client.event.RenderGameOverlayEvent.Pre p,String s,int a,int b,java.util.List<String> l){}}',
'com/hbm/handler/CompatHandler.java':'package com.hbm.handler; public class CompatHandler {public interface OCComponent {String[] methods();Object[] invoke(String n,li.cil.oc.api.machine.Context c,li.cil.oc.api.machine.Arguments a)throws Exception;}}',
'api/hbm/energymk2/IEnergyHandlerMK2.java':'package api.hbm.energymk2; public interface IEnergyHandlerMK2 {enum ConnectionPriority {LOW}long getPower();long getMaxPower();void setPower(long n);boolean canConnect(com.hbm.lib.ForgeDirection d);ConnectionPriority getPriority();}',
'api/hbm/energymk2/IEnergyProviderMK2.java':'package api.hbm.energymk2;public interface IEnergyProviderMK2 extends IEnergyHandlerMK2 {void usePower(long n);long getProviderSpeed();default void tryProvide(net.minecraft.world.World w,int x,int y,int z,com.hbm.lib.ForgeDirection d){}}',
'api/hbm/energymk2/IEnergyReceiverMK2.java':'package api.hbm.energymk2;public interface IEnergyReceiverMK2 extends IEnergyHandlerMK2 {long transferPower(long n);long getReceiverSpeed();default void trySubscribe(net.minecraft.world.World w,int x,int y,int z,com.hbm.lib.ForgeDirection d){}default void tryUnsubscribe(net.minecraft.world.World w,int x,int y,int z){}}',
'api/hbm/tile/ILoadedTile.java':'package api.hbm.tile;public interface ILoadedTile {boolean isLoaded();}',
'com/hbm/sound/AudioWrapper.java':'package com.hbm.sound; public class AudioWrapper {public void stopSound(){}public void startSound(){}}',
'com/hbm/handler/threading/PacketThreading.java':'package com.hbm.handler.threading;public class PacketThreading {public static void createAllAroundThreadedPacket(Object packet,net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint p){}}',
'com/hbm/packet/toclient/BufPacket.java':'package com.hbm.packet.toclient;public class BufPacket {final com.hbm.tileentity.IBufPacketReceiver source;public BufPacket(int x,int y,int z,com.hbm.tileentity.IBufPacketReceiver s){source=s;}public io.netty.buffer.ByteBuf getCompiledBuffer(){io.netty.buffer.ByteBuf b=io.netty.buffer.Unpooled.buffer();source.serialize(b);return b;}}',
'com/hbm/inventory/fluid/FluidType.java':'package com.hbm.inventory.fluid;public class FluidType {public String getConditionalName(){return "fluid.fixture";}public String getLocalizedName(){return "Fixture";}}',
'com/hbm/inventory/fluid/Fluids.java':'package com.hbm.inventory.fluid;public class Fluids {public static final FluidType NONE=new FluidType();}',
'com/hbm/inventory/fluid/tank/FluidTank.java':'''package com.hbm.inventory.fluid.tank;import com.hbm.inventory.fluid.*;import net.minecraft.nbt.NBTTagCompound;public class FluidTank {private FluidType type;private int fill,max;public FluidTank(FluidType t,int m){type=t;max=m;}public void setTankType(FluidType t){type=t;}public FluidType getTankType(){return type;}public int getFill(){return fill;}public int getMaxFill(){return max;}public void readFromNBT(NBTTagCompound n,String key){fill=n.getCompoundTag(key).getInteger("fill");max=n.getCompoundTag(key).getInteger("max");}}''',
'com/hbm/tileentity/machine/storage/TileEntityBarrel.java':'''package com.hbm.tileentity.machine.storage;
import net.minecraft.item.ItemStack;import net.minecraft.entity.player.EntityPlayer;import net.minecraft.util.EnumFacing;import net.minecraft.nbt.NBTTagCompound;
public class TileEntityBarrel extends net.minecraft.tileentity.TileEntity implements net.minecraft.inventory.ISidedInventory,com.hbm.tileentity.IPersistentNBT {
 public final ItemStack[] slots=new ItemStack[4];public final com.hbm.inventory.fluid.tank.FluidTank tank;public final int capacity;public int comparator,dirty,persistent;
 public TileEntityBarrel(){this(0);}public TileEntityBarrel(int n){capacity=n;tank=new com.hbm.inventory.fluid.tank.FluidTank(com.hbm.inventory.fluid.Fluids.NONE,n);}
 public int getComparatorPower(){return comparator;}public void markDirty(){dirty++;}
 public void writeNBT(NBTTagCompound n){n.setInteger("fixture",persistent);}public void readNBT(NBTTagCompound n){persistent=n.getInteger("fixture");}
 public String getName(){return "fixture";}public boolean hasCustomName(){return false;}public net.minecraft.util.text.ITextComponent getDisplayName(){return new net.minecraft.util.text.TextComponentString(getName());}
 public int getSizeInventory(){return slots.length;}public boolean isEmpty(){for(ItemStack s:slots)if(s!=null&&!s.isEmpty())return false;return true;}
 public ItemStack getStackInSlot(int n){return slots[n]==null?ItemStack.EMPTY:slots[n];}public ItemStack decrStackSize(int n,int c){return getStackInSlot(n).splitStack(c);}public ItemStack removeStackFromSlot(int n){ItemStack s=getStackInSlot(n);slots[n]=ItemStack.EMPTY;return s;}
 public void setInventorySlotContents(int n,ItemStack s){slots[n]=s;}public int getInventoryStackLimit(){return 64;}public boolean isUsableByPlayer(EntityPlayer p){return true;}public void openInventory(EntityPlayer p){}public void closeInventory(EntityPlayer p){}public boolean isItemValidForSlot(int n,ItemStack s){return true;}
 public int getField(int n){return 0;}public void setField(int n,int v){}public int getFieldCount(){return 0;}public void clear(){java.util.Arrays.fill(slots,ItemStack.EMPTY);}public int[] getSlotsForFace(EnumFacing f){return new int[]{0,1,2,3};}public boolean canInsertItem(int n,ItemStack s,EnumFacing f){return true;}public boolean canExtractItem(int n,ItemStack s,EnumFacing f){return true;}
}'''
}
# The actual barrel tile is not fully compiled: transplant only this exact approved
# lifecycle override into the controlled storage collaborator, leaving its production
# inventory/fluid/NBT/wire bodies outside this bounded proof.
barrel_source=(src/'com/hbm/tileentity/machine/storage/TileEntityBarrel.java').read_text()
barrel_lifecycle='\n\n\t/** Placement metadata does not replace a barrel; corrosion block changes do. */\n\t@Override\n\tpublic boolean shouldRefresh(net.minecraft.world.World world, net.minecraft.util.math.BlockPos pos,\n\t\t\tnet.minecraft.block.state.IBlockState oldState, net.minecraft.block.state.IBlockState newState) {\n\t\treturn oldState.getBlock() != newState.getBlock();\n\t}\n'
assert barrel_source.count(barrel_lifecycle)==1
assert hashlib.sha256(barrel_source.replace(barrel_lifecycle,'').encode()).hexdigest()=='2ff5e4b96b4955909b7007a68a21030942d7be33e754df07d73287204e246be2'
collaborators['com/hbm/tileentity/machine/storage/TileEntityBarrel.java']=collaborators['com/hbm/tileentity/machine/storage/TileEntityBarrel.java'].replace(' public final ItemStack[] slots=',barrel_lifecycle+' public final ItemStack[] slots=')
print('PASS actual barrel lifecycle override; all pre-existing tile source bytes unchanged',flush=True)
collaborators['com/hbm/items/armor/ArmorFSB.java']='package com.hbm.items.armor;public class ArmorFSB {public static boolean eligible=true;public static boolean hasFSBArmorIgnoreCharge(net.minecraft.entity.player.EntityPlayer p){return eligible;}}'
collaborators['com/hbm/items/armor/ArmorFSBPowered.java']='''package com.hbm.items.armor;import net.minecraft.item.*;import net.minecraft.nbt.NBTTagCompound;public class ArmorFSBPowered extends Item implements api.hbm.energymk2.IBatteryItem {public void setCharge(ItemStack s,long n){if(!s.hasTagCompound())s.setTagCompound(new NBTTagCompound());s.getTagCompound().setLong("charge",n);}public long getCharge(ItemStack s){return s.hasTagCompound()?s.getTagCompound().getLong("charge"):0;}public long getMaxCharge(ItemStack s){return 200000;}public void chargeBattery(ItemStack s,long n){setCharge(s,getCharge(s)+n);}public void dischargeBattery(ItemStack s,long n){setCharge(s,getCharge(s)-n);}public long getChargeRate(ItemStack s){return 200000;}public long getDischargeRate(ItemStack s){return 200000;}}'''
collaborators['com/hbm/entity/item/EntityTNTPrimedBase.java']='''package com.hbm.entity.item;public class EntityTNTPrimedBase extends net.minecraft.entity.Entity {public int fuse;public boolean detonateOnCollision;public final api.hbm.block.IFuckingExplode owner;public final net.minecraft.entity.EntityLivingBase igniter;public EntityTNTPrimedBase(net.minecraft.world.World w,double x,double y,double z,net.minecraft.entity.EntityLivingBase e,api.hbm.block.IFuckingExplode b){super(w);setPosition(x,y,z);igniter=e;owner=b;}protected void entityInit(){}protected void readEntityFromNBT(net.minecraft.nbt.NBTTagCompound n){}protected void writeEntityToNBT(net.minecraft.nbt.NBTTagCompound n){}}'''
collaborators['com/hbm/explosion/ExplosionNukeGeneric.java']='''package com.hbm.explosion;public class ExplosionNukeGeneric {public static final java.util.List<String> calls=new java.util.ArrayList<>();public static void waste(net.minecraft.world.World w,int x,int y,int z,int radius){calls.add(x+","+y+","+z+":"+radius);}}'''
collaborators['com/hbm/explosion/ExplosionThermo.java']='''package com.hbm.explosion;public class ExplosionThermo {public static final java.util.List<String> calls=new java.util.ArrayList<>();public static void freeze(net.minecraft.world.World w,int x,int y,int z,int radius){calls.add(x+","+y+","+z+":"+radius);}}'''
collaborators['com/hbm/handler/radiation/ChunkRadiationManager.java']='''package com.hbm.handler.radiation;public class ChunkRadiationManager {public static final ChunkRadiationManager proxy=new ChunkRadiationManager();public final java.util.List<String> calls=new java.util.ArrayList<>();public void incrementRad(net.minecraft.world.World w,int x,int y,int z,float amount){calls.add(x+","+y+","+z+":"+amount);}}'''
collaborators['com/hbm/world/feature/OilSpot.java']='''package com.hbm.world.feature;public class OilSpot {public static final java.util.List<String> calls=new java.util.ArrayList<>();public static void generateOilSpot(net.minecraft.world.World w,int x,int z,int a,int b,boolean flag){calls.add(x+","+z+":"+a+":"+b+":"+flag);}}'''
production=['com/hbm/blocks/generic/BrownBarrel.java','com/hbm/blocks/generic/YellowBarrel.java','com/hbm/blocks/generic/RedBarrel.java','com/hbm/blocks/generic/BlockFlammable.java','com/hbm/blocks/bomb/BlockDetonatable.java','com/hbm/blocks/BlockBase.java','com/hbm/render/icon/IIcon.java','com/hbm/render/icon/IIconRegister.java','api/hbm/block/IFuckingExplode.java','com/hbm/blocks/generic/BlockPipe.java','com/hbm/blocks/generic/HEVBattery.java','com/hbm/blocks/generic/DecoPoleTop.java','com/hbm/blocks/ModSoundType.java','com/hbm/blocks/ModSoundTypes.java','com/hbm/main/ModSoundEvents.java','com/hbm/lib/RefStrings.java','com/hbm/world/gen/nbt/INBTBlockTransformable.java','api/hbm/energymk2/IBatteryItem.java','com/hbm/blocks/machine/MachineCapacitor.java','com/hbm/blocks/machine/BlockFluidBarrel.java','com/hbm/blocks/IPersistentInfoProvider.java','com/hbm/blocks/ITooltipProvider.java','com/hbm/tileentity/IPersistentNBT.java','com/hbm/tileentity/TileEntityLoadedBase.java','com/hbm/tileentity/IBufPacketReceiver.java','com/hbm/lib/ForgeDirection.java','com/hbm/items/machine/IItemFluidIdentifier.java','api/hbm/redstoneoverradio/IRORInfo.java','api/hbm/redstoneoverradio/IRORValueProvider.java']
exe='.exe' if os.name=='nt' else ''
with tempfile.TemporaryDirectory(prefix='atlas-block-machines-') as tmp:
 out=Path(tmp);files=[]
 for rel,text in collaborators.items():
  assert rel.startswith(('com/hbm/','api/hbm/')), 'No native facades permitted'
  f=out/rel;f.parent.mkdir(parents=True,exist_ok=True);f.write_text(text);files.append(str(f))
 for rel in production:
  f=src/rel;print('PRODUCTION',rel,hashlib.sha256(f.read_bytes()).hexdigest(),flush=True);files.append(str(f))
 f=out/'AtlasBlockMachinesNativeTest.java';f.write_text((tests/'atlas-block-machines-native-test.java.in').read_text());files.append(str(f))
 compiled=subprocess.run([str(a.java_home/'bin'/('javac'+exe)),'-encoding','UTF-8','-source','8','-target','8','-cp',a.classpath,'-d',tmp]+files)
 if compiled.returncode:sys.exit(compiled.returncode)
 executed=subprocess.run([str(a.java_home/'bin'/('java'+exe)),'-Djava.awt.headless=true','-cp',os.pathsep.join([tmp,a.classpath]),'AtlasBlockMachinesNativeTest'],cwd=tmp)
 if executed.returncode:sys.exit(executed.returncode)
