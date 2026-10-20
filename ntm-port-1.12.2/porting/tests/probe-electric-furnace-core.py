#!/usr/bin/env python3
"""Complete electric-furnace production classes with real native APIs and explicit NTM collaborators.
No production source is rewritten/excluded. Only Library charging and BufPacket transport body are extracted.
This is headless behavioral evidence, NOT GUI/achievements/network topology or live gameplay acceptance.
"""
import argparse, hashlib, os, subprocess, tempfile
from pathlib import Path
p=argparse.ArgumentParser(description=__doc__);p.add_argument('--java-home',type=Path,required=True);p.add_argument('--classpath',required=True);p.add_argument('--combined',action='store_true',help='Compile and run the actual tile and block together on native World/Chunk');a=p.parse_args()
ROOT=Path(__file__).resolve().parents[2];src=ROOT/'src/main/java';exe='.exe' if os.name=='nt' else ''
if not (a.java_home/'bin'/('javac'+exe)).is_file() and (a.java_home.parent/'bin'/('javac'+exe)).is_file():a.java_home=a.java_home.parent
collaborators = {
    'com/hbm/dim/orbit/WorldProviderOrbit.java': """
package com.hbm.dim.orbit; public class WorldProviderOrbit extends net.minecraft.world.WorldProviderSurface {}
""",
    'com/hbm/inventory/fluid/Fluids.java': """
package com.hbm.inventory.fluid; public class Fluids { public static final Object EARTHAIR=new Object(), OXYGEN=new Object(); }
""",
    'com/hbm/dim/trait/CBT_Atmosphere.java': """
package com.hbm.dim.trait; public class CBT_Atmosphere { public boolean breathable; public boolean hasFluid(Object type,double fraction) { return breathable; } }
""",
    'com/hbm/dim/CelestialBody.java': """
package com.hbm.dim; public class CelestialBody { public static com.hbm.dim.trait.CBT_Atmosphere atmosphere; public static <T> T getTrait(net.minecraft.world.World w,Class<T> c){return c.cast(atmosphere);} }
""",
    'com/hbm/handler/atmosphere/AtmosphereBlob.java': """
package com.hbm.handler.atmosphere; public class AtmosphereBlob { public boolean breathable; public int consumed; public boolean hasFluid(Object type,double fraction){return breathable;} public void consume(int n){consumed+=n;} }
""",
    'com/hbm/handler/atmosphere/ChunkAtmosphereManager.java': """
package com.hbm.handler.atmosphere; public class ChunkAtmosphereManager { public static final ChunkAtmosphereManager proxy=new ChunkAtmosphereManager(); public java.util.List<AtmosphereBlob> blobs=new java.util.ArrayList<>(); public java.util.List<AtmosphereBlob> getBlobs(net.minecraft.world.World w,int x,int y,int z){return blobs;} }
""",
    'com/hbm/util/fauxpointtwelve/DirPos.java': """
package com.hbm.util.fauxpointtwelve; public class DirPos extends BlockPos { private final com.hbm.lib.ForgeDirection dir; public DirPos(int x,int y,int z,com.hbm.lib.ForgeDirection d){super(x,y,z);dir=d;} public com.hbm.lib.ForgeDirection getDir(){return dir;} }
""",
    'com/hbm/handler/threading/PacketThreading.java': """
package com.hbm.handler.threading; public class PacketThreading {
public static int calls; public static com.hbm.packet.toclient.BufPacket packet;
public static net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint target;
public static void createAllAroundThreadedPacket(com.hbm.packet.toclient.BufPacket p, net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint t){calls++;packet=p;target=t; com.hbm.furnaceprobe.Observations.events.add("packet");}
public static void createAllAroundThreadedPacket(com.hbm.packet.toclient.AuxParticlePacketNT p, net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint t){throw new AssertionError("debug branch");}}
""",
    'com/hbm/furnaceprobe/Observations.java': """
package com.hbm.furnaceprobe; public class Observations {public static java.util.List<String> events=new java.util.ArrayList<>();}
""",
    'com/hbm/blocks/ModBlocks.java': """
package com.hbm.blocks; public class ModBlocks { public static net.minecraft.block.Block machine_electric_furnace_off=net.minecraft.init.Blocks.FURNACE; }
""",
    'com/hbm/blocks/machine/MachineElectricFurnace.java': """
package com.hbm.blocks.machine; public class MachineElectricFurnace {public static int calls; public static boolean active; public static net.minecraft.world.World world; public static int x,y,z; public static void updateBlockState(boolean a,net.minecraft.world.World w,int i,int j,int k){calls++;active=a;world=w;x=i;y=j;z=k;com.hbm.furnaceprobe.Observations.events.add("block:"+a);}}
""",
    'com/hbm/handler/pollution/PollutionHandler.java': """
package com.hbm.handler.pollution; public class PollutionHandler {public enum PollutionType {SOOT} public static final float SOOT_PER_SECOND=1F / 25F; public static int calls,x,y,z; public static float amount; public static PollutionType type; public static net.minecraft.world.World world; public static void incrementPollution(net.minecraft.world.World w,int i,int j,int k,PollutionType t,float n){calls++;world=w;x=i;y=j;z=k;type=t;amount=n;com.hbm.furnaceprobe.Observations.events.add("pollution");}}
""",
    'com/hbm/inventory/gui/GUIMachineElectricFurnace.java': """
package com.hbm.inventory.gui; public class GUIMachineElectricFurnace {public GUIMachineElectricFurnace(net.minecraft.entity.player.InventoryPlayer i,com.hbm.tileentity.machine.TileEntityMachineElectricFurnace t){}}
""",
    'com/hbm/inventory/SlotSmelting.java': """
package com.hbm.inventory; public class SlotSmelting extends net.minecraft.inventory.Slot {public static int takes,changes; public SlotSmelting(net.minecraft.entity.player.EntityPlayer p,net.minecraft.inventory.IInventory i,int s,int x,int y){super(i,s,x,y);} @Override public boolean isItemValid(net.minecraft.item.ItemStack s){return false;} @Override public void onSlotChange(net.minecraft.item.ItemStack a,net.minecraft.item.ItemStack b){changes++;super.onSlotChange(a,b);} @Override public net.minecraft.item.ItemStack onTake(net.minecraft.entity.player.EntityPlayer p,net.minecraft.item.ItemStack s){takes++;return super.onTake(p,s);}}
""",
    'com/hbm/util/i18n/I18nUtil.java': """
package com.hbm.util.i18n; public class I18nUtil {public static String resolveKey(String k,Object... args){return k+java.util.Arrays.toString(args);}}
""",
    'com/hbm/util/BobMathUtil.java': """
package com.hbm.util; public class BobMathUtil {public static String getShortNumber(long n){return Long.toString(n);}}
""",
    'com/hbm/items/ModItems.java': """
package com.hbm.items; public class ModItems {public static net.minecraft.item.Item fusion_core,energy_core,battery_schrabidium,upgrade_radius,upgrade_health,upgrade_smelter,upgrade_shredder,upgrade_centrifuge,upgrade_crystallizer,upgrade_screm,upgrade_nullifier,upgrade_gc_speed; public static net.minecraft.item.Item battery_creative = new com.hbm.items.machine.ItemBatteryCreative();}
""",
    'com/hbm/util/CompatEnergyControl.java': """
package com.hbm.util; public class CompatEnergyControl {public static final String L_ENERGY_HE="energy",L_CAPACITY_HE="capacity";}
""",
    'com/hbm/util/Compat.java': """
package com.hbm.util; public class Compat {public static int calls; public static net.minecraft.tileentity.TileEntity getTileStandard(net.minecraft.world.World w,int x,int y,int z){calls++;com.hbm.furnaceprobe.Observations.events.add("subscribe:"+x+","+y+","+z);return w.getTileEntity(new net.minecraft.util.math.BlockPos(x,y,z));}}
""",
    'api/hbm/energymk2/Nodespace.java': """
package api.hbm.energymk2; public class Nodespace { public static PowerNode selected; public static PowerNode getNode(net.minecraft.world.World w,int x,int y,int z){return selected;} public static class Net {public int received,removed; public IEnergyReceiverMK2 receiver; public void addReceiver(IEnergyReceiverMK2 r){received++;receiver=r;} public void removeReceiver(IEnergyReceiverMK2 r){removed++;receiver=r;}} public static class PowerNode {public Net net; public PowerNode(com.hbm.util.fauxpointtwelve.BlockPos... p){} public PowerNode setConnections(com.hbm.util.fauxpointtwelve.DirPos... p){return this;}}}
""",
    'com/hbm/packet/toclient/AuxParticlePacketNT.java': """
package com.hbm.packet.toclient; public class AuxParticlePacketNT {public AuxParticlePacketNT(net.minecraft.nbt.NBTTagCompound n,double x,double y,double z){throw new AssertionError("debug branch");}}
""",
    'com/hbm/util/fauxpointtwelve/BlockPos.java': """
package com.hbm.util.fauxpointtwelve; public class BlockPos extends net.minecraft.util.math.BlockPos { public BlockPos(int x,int y,int z){super(x,y,z);} }
""",
}

def method(text,signature):
 start=text.index(signature);i=text.index('{',start)+1;depth=1
 while depth:depth+=(text[i]=='{')-(text[i]=='}');i+=1
 return text[start:i]
lib=(src/'com/hbm/lib/Library.java').read_text(encoding='utf-8')
import re
constants='\n'.join(re.findall(r'public static final ForgeDirection (?:POS|NEG)_[XYZ] = ForgeDirection.\w+;',lib))
collaborators['com/hbm/lib/Library.java']='package com.hbm.lib; import com.hbm.items.ModItems; import api.hbm.energymk2.IBatteryItem; import net.minecraft.item.ItemStack; public class Library {'+constants+method(lib,'public static long chargeTEFromItems(')+'}'
packet=(src/'com/hbm/packet/toclient/BufPacket.java').read_text(encoding='utf-8')
body=packet.split('public class BufPacket extends PrecompiledPacket {',1)[1].split('\n\tpublic static class Handler',1)[0]
collaborators['com/hbm/packet/toclient/BufPacket.java']='package com.hbm.packet.toclient; import com.hbm.packet.threading.PrecompiledPacket; import com.hbm.tileentity.IBufPacketReceiver; import io.netty.buffer.ByteBuf; public class BufPacket extends PrecompiledPacket {'+body+'\n}'
production=[
'com/hbm/tileentity/machine/TileEntityMachineElectricFurnace.java',
'com/hbm/inventory/container/ContainerElectricFurnace.java','com/hbm/inventory/SlotUpgrade.java',
'com/hbm/items/machine/ItemMachineUpgrade.java','com/hbm/tileentity/IUpgradeInfoProvider.java',
'com/hbm/inventory/UpgradeManagerNT.java','com/hbm/items/machine/ItemBattery.java','com/hbm/items/machine/ItemBatteryCreative.java',
'api/hbm/energymk2/IBatteryItem.java','api/hbm/energymk2/IEnergyReceiverMK2.java',
'api/hbm/energymk2/IEnergyHandlerMK2.java','api/hbm/energymk2/IEnergyConnectorMK2.java',
'api/hbm/energymk2/IEnergyConductorMK2.java','api/hbm/tile/ILoadedTile.java',
'com/hbm/util/Tuple.java','com/hbm/interfaces/NotableComments.java','com/hbm/interfaces/Spaghetti.java',
'com/hbm/tileentity/TileEntityMachineBase.java','com/hbm/tileentity/TileEntityLoadedBase.java',
'com/hbm/tileentity/IGUIProvider.java','com/hbm/tileentity/IBufPacketReceiver.java',
'com/hbm/lib/ForgeDirection.java','com/hbm/sound/AudioWrapper.java',
'com/hbm/packet/threading/ThreadedPacket.java','com/hbm/packet/threading/PrecompiledPacket.java']
test_name='ElectricFurnaceNativeTest'
if a.combined:
 collaborators.pop('com/hbm/blocks/machine/MachineElectricFurnace.java')
 collaborators['com/hbm/blocks/ModBlocks.java']='package com.hbm.blocks; public class ModBlocks {public static net.minecraft.block.Block machine_electric_furnace_off,machine_electric_furnace_on;}'
 collaborators['com/hbm/main/MainRegistry.java']='package com.hbm.main; public class MainRegistry {public static final Object instance=new Object();}'
 production += ['com/hbm/blocks/machine/MachineElectricFurnace.java','com/hbm/lib/RefStrings.java']
 test_name='ElectricFurnaceCombinedNativeTest'
with tempfile.TemporaryDirectory(prefix='hbm-electric-furnace-native-') as d:
 out=Path(d);files=[]
 for path,text in collaborators.items():
  target=out/path;target.parent.mkdir(parents=True,exist_ok=True);target.write_text(text,encoding='utf-8');files.append(str(target))
 for relative in production:
  path=src/relative;print('PRODUCTION',relative,hashlib.sha256(path.read_bytes()).hexdigest(),flush=True)
 files += [str(src/f) for f in production]+[str(ROOT/('porting/tests/'+test_name+'.java'))]
 subprocess.run([str(a.java_home/'bin'/('javac'+exe)),'-encoding','UTF-8','-source','8','-target','8','-cp',a.classpath,'-d',d]+files,check=True)
 subprocess.run([str(a.java_home/'bin'/('java'+exe)),'-cp',os.pathsep.join([d,a.classpath]),test_name],cwd=d,check=True)
print('PASS '+('combined actual block/tile/World/Chunk smoke; ' if a.combined else '')+'complete furnace/container/upgrade/battery/native inheritance compile; controlled NTM collaborators, no live gameplay claim.',flush=True)
