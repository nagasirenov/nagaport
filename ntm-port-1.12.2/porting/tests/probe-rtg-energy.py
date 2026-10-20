#!/usr/bin/env python3
"""Compile complete RTG tiles, pellet, utility, provider/receiver APIs against real Forge. Registry/config/GUI/topology/thread dispatch are explicit collaborators; BufPacket serializer body retained, transport handler excluded. No full-network or live-game claim."""
import argparse, hashlib, os, subprocess, tempfile
from pathlib import Path
p=argparse.ArgumentParser(description=__doc__);p.add_argument('--java-home',type=Path,required=True);p.add_argument('--classpath',required=True);a=p.parse_args()
ROOT=Path(__file__).resolve().parents[2];src=ROOT/'src/main/java';exe='.exe' if os.name=='nt' else ''
collaborators = {'com/hbm/handler/threading/PacketThreading.java': '\n'
                                                   'package com.hbm.handler.threading; public class '
                                                   'PacketThreading {\n'
                                                   'public static int calls; public static '
                                                   'com.hbm.packet.toclient.BufPacket packet;\n'
                                                   'public static '
                                                   'net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint '
                                                   'target;\n'
                                                   'public static void '
                                                   'createAllAroundThreadedPacket(com.hbm.packet.toclient.BufPacket '
                                                   'p, '
                                                   'net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint '
                                                   't){calls++;packet=p;target=t; '
                                                   'com.hbm.furnaceprobe.Observations.events.add("packet");}\n'
                                                   'public static void '
                                                   'createAllAroundThreadedPacket(com.hbm.packet.toclient.AuxParticlePacketNT '
                                                   'p, '
                                                   'net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint '
                                                   't){throw new AssertionError("debug branch");}}\n',
 'com/hbm/furnaceprobe/Observations.java': '\n'
                                           'package com.hbm.furnaceprobe; public class Observations {public '
                                           'static java.util.List<String> events=new '
                                           'java.util.ArrayList<>();}\n',
 'com/hbm/util/i18n/I18nUtil.java': '\n'
                                    'package com.hbm.util.i18n; public class I18nUtil {public static String '
                                    'resolveKey(String k,Object... args){return '
                                    'k+java.util.Arrays.toString(args);}}\n',
 'com/hbm/util/Compat.java': '\n'
                             'package com.hbm.util; public class Compat {public static int calls; public '
                             'static net.minecraft.tileentity.TileEntity '
                             'getTileStandard(net.minecraft.world.World w,int x,int y,int '
                             'z){calls++;com.hbm.furnaceprobe.Observations.events.add("subscribe:"+x+","+y+","+z);return '
                             'w.getTileEntity(new net.minecraft.util.math.BlockPos(x,y,z));}}\n',
 'api/hbm/energymk2/Nodespace.java': '\n'
                                     'package api.hbm.energymk2; public class Nodespace { public static '
                                     'PowerNode selected; public static PowerNode '
                                     'getNode(net.minecraft.world.World w,int x,int y,int z){return '
                                     'selected;} public static class Net {public int '
                                     'received,removed,provided; public void addProvider(IEnergyProviderMK2 '
                                     'p){provided++;} public IEnergyReceiverMK2 receiver; public void '
                                     'addReceiver(IEnergyReceiverMK2 r){received++;receiver=r;} public void '
                                     'removeReceiver(IEnergyReceiverMK2 r){removed++;receiver=r;}} public '
                                     'static class PowerNode {public Net net; public '
                                     'PowerNode(com.hbm.util.fauxpointtwelve.BlockPos... p){} public '
                                     'PowerNode setConnections(com.hbm.util.fauxpointtwelve.DirPos... '
                                     'p){return this;}}}\n',
 'com/hbm/packet/toclient/AuxParticlePacketNT.java': '\n'
                                                     'package com.hbm.packet.toclient; public class '
                                                     'AuxParticlePacketNT {public '
                                                     'AuxParticlePacketNT(net.minecraft.nbt.NBTTagCompound '
                                                     'n,double x,double y,double z){throw new '
                                                     'AssertionError("debug branch");}}\n',
 'com/hbm/util/fauxpointtwelve/BlockPos.java': '\n'
                                               'package com.hbm.util.fauxpointtwelve; public class BlockPos '
                                               'extends net.minecraft.util.math.BlockPos { public '
                                               'BlockPos(int x,int y,int z){super(x,y,z);} }\n',
 'com/hbm/util/fauxpointtwelve/DirPos.java': '\n'
                                             'package com.hbm.util.fauxpointtwelve; public class DirPos '
                                             'extends BlockPos { private final com.hbm.lib.ForgeDirection '
                                             'dir; public DirPos(int x,int y,int '
                                             'z,com.hbm.lib.ForgeDirection d){super(x,y,z);dir=d;} public '
                                             'com.hbm.lib.ForgeDirection getDir(){return dir;} }\n',
 'com/hbm/config/VersatileConfig.java': 'package com.hbm.config; public class VersatileConfig { public '
                                        'static boolean decay,scale; public static boolean rtgDecay(){return '
                                        'decay;} public static boolean scaleRTGPower(){return scale;} }',
 'com/hbm/items/ModItems.java': 'package com.hbm.items; public class ModItems { public static '
                                'net.minecraft.item.Item pellet_rtg,pellet_rtg_depleted; }',
 'com/hbm/blocks/ModBlocks.java': 'package com.hbm.blocks; public class ModBlocks { public static '
                                  'net.minecraft.block.Block machine_powerrtg; }',
 'com/hbm/util/CompatEnergyControl.java': 'package com.hbm.util; public class CompatEnergyControl {public '
                                          'static final String '
                                          'L_ENERGY_HE="energy",L_CAPACITY_HE="capacity",B_ACTIVE="active",D_OUTPUT_HE="output";}',
 'com/hbm/util/BobMathUtil.java': 'package com.hbm.util; public class BobMathUtil {public static String '
                                  'toPercentage(long a,long b){return "";} public static String[] '
                                  'ticksToDate(long t){return new String[]{"0","0","0"};}}',
 'com/hbm/items/machine/ItemRTGPelletDepleted.java': 'package com.hbm.items.machine; public class '
                                                     'ItemRTGPelletDepleted {public enum DepletedRTGMaterial '
                                                     '{BISMUTH,MERCURY,NEPTUNIUM,LEAD,ZIRCONIUM,AMERICIUM,NICKEL;}}',
 'com/hbm/inventory/container/ContainerMachineRTG.java': 'package com.hbm.inventory.container; public class '
                                                         'ContainerMachineRTG extends '
                                                         'net.minecraft.inventory.Container {public '
                                                         'ContainerMachineRTG(net.minecraft.entity.player.InventoryPlayer '
                                                         'p,com.hbm.tileentity.machine.TileEntityMachineRTG '
                                                         't){} public boolean '
                                                         'canInteractWith(net.minecraft.entity.player.EntityPlayer '
                                                         'p){return true;}}',
 'com/hbm/inventory/gui/GUIMachineRTG.java': 'package com.hbm.inventory.gui; public class GUIMachineRTG '
                                             '{public '
                                             'GUIMachineRTG(net.minecraft.entity.player.InventoryPlayer '
                                             'p,com.hbm.tileentity.machine.TileEntityMachineRTG t){}}'}
import re
constants='\n'.join(re.findall(r'public static final ForgeDirection (?:POS|NEG)_[XYZ] = ForgeDirection.\w+;', (src/'com/hbm/lib/Library.java').read_text()))
collaborators['com/hbm/lib/Library.java']='package com.hbm.lib; public class Library {'+constants+'}'
collaborators['com/hbm/blocks/ModBlocks.java']='package com.hbm.blocks; public class ModBlocks {public static net.minecraft.block.Block machine_rtg_grey,machine_minirtg,machine_powerrtg;}'
collaborators['com/hbm/main/MainRegistry.java']='package com.hbm.main; public class MainRegistry {public static final Object instance="hbm";}'
packet=(src/'com/hbm/packet/toclient/BufPacket.java').read_text(encoding='utf-8')
body=packet.split('public class BufPacket extends PrecompiledPacket {',1)[1].split('\n\tpublic static class Handler',1)[0]
collaborators['com/hbm/packet/toclient/BufPacket.java']='package com.hbm.packet.toclient; import com.hbm.packet.threading.PrecompiledPacket; import com.hbm.tileentity.IBufPacketReceiver; import io.netty.buffer.ByteBuf; public class BufPacket extends PrecompiledPacket {'+body+'\n}'
production=['com/hbm/tileentity/machine/TileEntityMachineRTG.java', 'com/hbm/tileentity/machine/TileEntityMachineMiniRTG.java', 'com/hbm/items/machine/ItemRTGPellet.java', 'com/hbm/util/RTGUtil.java', 'com/hbm/interfaces/HalfLifeType.java', 'api/hbm/tile/IInfoProviderEC.java', 'api/hbm/energymk2/IEnergyProviderMK2.java', 'api/hbm/energymk2/IEnergyReceiverMK2.java', 'api/hbm/energymk2/IEnergyHandlerMK2.java', 'api/hbm/energymk2/IEnergyConnectorMK2.java', 'api/hbm/energymk2/IEnergyConductorMK2.java', 'api/hbm/tile/ILoadedTile.java', 'com/hbm/util/Tuple.java', 'com/hbm/interfaces/NotableComments.java', 'com/hbm/interfaces/Spaghetti.java', 'com/hbm/tileentity/TileEntityLoadedBase.java', 'com/hbm/tileentity/IGUIProvider.java', 'com/hbm/tileentity/IBufPacketReceiver.java', 'com/hbm/lib/ForgeDirection.java', 'com/hbm/sound/AudioWrapper.java', 'com/hbm/packet/threading/ThreadedPacket.java', 'com/hbm/packet/threading/PrecompiledPacket.java']
production += ["com/hbm/blocks/machine/MachineRTG.java","com/hbm/blocks/machine/MachineMiniRTG.java"]
test_name="RTGEnergyNativeTest"
with tempfile.TemporaryDirectory(prefix='hbm-rtg-energy-native-') as d:
 out=Path(d);files=[]
 for path,text in collaborators.items():
  target=out/path;target.parent.mkdir(parents=True,exist_ok=True);target.write_text(text,encoding='utf-8');files.append(str(target))
 for relative in production:
  path=src/relative;print('PRODUCTION',relative,hashlib.sha256(path.read_bytes()).hexdigest(),flush=True)
 files += [str(src/f) for f in production]+[str(ROOT/('porting/tests/'+test_name+'.java'))]
 subprocess.run([str(a.java_home/'bin'/('javac'+exe)),'-encoding','UTF-8','-source','8','-target','8','-cp',a.classpath,'-d',d]+files,check=True)
 subprocess.run([str(a.java_home/'bin'/('java'+exe)),'-cp',os.pathsep.join([d,a.classpath]),test_name],cwd=d,check=True)
print('PASS complete native RTG owner probe, explicit external collaborators; not full-game acceptance.')
