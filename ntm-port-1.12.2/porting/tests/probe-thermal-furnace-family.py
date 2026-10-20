#!/usr/bin/env python3
"""Compile six complete thermal furnace classes and complete native dependencies.
Only explicitly named NTM collaborators are controlled; never fake Minecraft/Forge.
Ash classifier, DictFrame constructor, InventoryUtil.mergeItemStack, and BufPacket body
are verbatim extractions. Blocks, pollution, registries, output achievements and GUI
are controlled boundaries, not full gameplay acceptance.
"""
import argparse,ast,hashlib,os,re,subprocess,tempfile
from pathlib import Path
R=Path(__file__).resolve().parents[2];S=R/'src/main/java';exe='.exe' if os.name=='nt' else ''
p=argparse.ArgumentParser();p.add_argument('--java-home',type=Path,required=True);p.add_argument('--classpath',required=True);a=p.parse_args()
def method(t,s):
 b=t.index(s);e=t.index('{',b)+1;d=1
 while d:d+=(t[e]=='{')-(t[e]=='}');e+=1
 return t[b:e]
def body(path,s):return method((S/path).read_text(encoding='utf-8'),s)
# Reuse explicit test collaborator definitions, not production substitutions.
parsed=ast.parse((R/'porting/tests/probe-electric-furnace-core.py').read_text(encoding='utf-8'))
collaborators=next(ast.literal_eval(n.value) for n in parsed.body if isinstance(n,ast.Assign) and any(isinstance(t,ast.Name) and t.id=='collaborators' for t in n.targets))
for name in ['com/hbm/blocks/machine/MachineElectricFurnace.java','com/hbm/inventory/gui/GUIMachineElectricFurnace.java','api/hbm/energymk2/Nodespace.java']:collaborators.pop(name)
for n in ['Brick','Iron','Steel']:
 collaborators[f'com/hbm/inventory/gui/GUIFurnace{n}.java']=f'package com.hbm.inventory.gui; public class GUIFurnace{n} {{public GUIFurnace{n}(net.minecraft.entity.player.InventoryPlayer p,com.hbm.tileentity.machine.TileEntityFurnace{n} t){{}}}}'
collaborators['com/hbm/blocks/machine/MachineBrickFurnace.java']='package com.hbm.blocks.machine; public class MachineBrickFurnace {public static int calls;public static boolean active;public static void updateBlockState(boolean a,net.minecraft.world.World w,int x,int y,int z){calls++;active=a;com.hbm.furnaceprobe.Observations.events.add("block:"+a);}}'
# Fields only: actual registries are not silently simulated.
texts='\n'.join((S/f).read_text(encoding='utf-8') for f in ['com/hbm/tileentity/machine/TileEntityFurnaceBrick.java','com/hbm/module/ModuleBurnTime.java','com/hbm/handler/FuelHandler.java','com/hbm/items/machine/ItemMachineUpgrade.java','com/hbm/inventory/RecipesCommon.java'])
items=sorted(set(re.findall(r'ModItems\.(\w+)',texts)))
collaborators['com/hbm/items/ModItems.java']='package com.hbm.items; public class ModItems {'+' '.join('public static net.minecraft.item.Item '+n+';' for n in items)+'}'
blocks=sorted(set(re.findall(r'ModBlocks\.(\w+)',texts)))+['furnace_iron']
collaborators['com/hbm/blocks/ModBlocks.java']='package com.hbm.blocks; public class ModBlocks {'+' '.join('public static net.minecraft.block.Block '+n+'=net.minecraft.init.Blocks.STONE;' for n in blocks)+'}'
collaborators['com/hbm/config/GeneralConfig.java']='package com.hbm.config; public class GeneralConfig {public static boolean enableSilentCompStackErrors;}'
collaborators['com/hbm/main/MainRegistry.java']='package com.hbm.main; public class MainRegistry {public static org.apache.logging.log4j.Logger logger=org.apache.logging.log4j.LogManager.getLogger();public static Proxy proxy=new Proxy();public static class Proxy{public java.util.List<net.minecraft.item.ItemStack> getSubItems(net.minecraft.item.ItemStack s){return java.util.Collections.singletonList(s);}}}'
collaborators['com/hbm/inventory/SlotCraftingOutput.java']='package com.hbm.inventory; public class SlotCraftingOutput extends net.minecraft.inventory.Slot {public SlotCraftingOutput(net.minecraft.entity.player.EntityPlayer p,net.minecraft.inventory.IInventory i,int s,int x,int y){super(i,s,x,y);} public boolean isItemValid(net.minecraft.item.ItemStack s){return false;}}'
collaborators['com/hbm/tileentity/machine/TileEntityFireboxBase.java']='package com.hbm.tileentity.machine; import java.util.List; import com.hbm.util.ItemStackUtil; import net.minecraft.item.ItemStack; import com.hbm.items.ItemEnums.EnumAshType; public class TileEntityFireboxBase {'+body('com/hbm/tileentity/machine/TileEntityFireboxBase.java','public static EnumAshType getAshFromFuel(')+'}'
collaborators['com/hbm/inventory/OreDictManager.java']='package com.hbm.inventory; import net.minecraft.item.Item; import net.minecraft.item.ItemStack; public class OreDictManager {public static class DictFrame {'+body('com/hbm/inventory/OreDictManager.java','public static ItemStack fromOne(Item item, Enum en)')+'}}'
collaborators['com/hbm/util/InventoryUtil.java']='package com.hbm.util; import java.util.List;import net.minecraft.inventory.Slot;import net.minecraft.item.ItemStack;public class InventoryUtil {'+body('com/hbm/util/InventoryUtil.java','public static boolean mergeItemStack(')+'}'
packet=(S/'com/hbm/packet/toclient/BufPacket.java').read_text(encoding='utf-8').split('public class BufPacket extends PrecompiledPacket {',1)[1].split('\n\tpublic static class Handler',1)[0]
collaborators['com/hbm/packet/toclient/BufPacket.java']='package com.hbm.packet.toclient; import com.hbm.packet.threading.PrecompiledPacket; import com.hbm.tileentity.IBufPacketReceiver; import io.netty.buffer.ByteBuf; public class BufPacket extends PrecompiledPacket {'+packet+'\n}'
production=[f'com/hbm/{kind}Furnace{n}.java' for n in ['Brick','Iron','Steel'] for kind in ['tileentity/machine/TileEntity','inventory/container/Container']]+[
'com/hbm/module/ModuleBurnTime.java','com/hbm/handler/FuelHandler.java','com/hbm/tileentity/IConfigurableMachine.java','api/hbm/tile/IHeatSource.java','com/hbm/util/ItemStackUtil.java','com/hbm/inventory/RecipesCommon.java','com/hbm/util/BufferUtil.java','com/hbm/items/ItemEnums.java',
'com/hbm/inventory/SlotUpgrade.java','com/hbm/items/machine/ItemMachineUpgrade.java','com/hbm/tileentity/IUpgradeInfoProvider.java','com/hbm/inventory/UpgradeManagerNT.java',
'api/hbm/tile/ILoadedTile.java','com/hbm/util/Tuple.java','com/hbm/interfaces/NotableComments.java','com/hbm/interfaces/Spaghetti.java','com/hbm/tileentity/TileEntityMachineBase.java','com/hbm/tileentity/TileEntityLoadedBase.java','com/hbm/tileentity/IGUIProvider.java','com/hbm/tileentity/IBufPacketReceiver.java','com/hbm/lib/ForgeDirection.java','com/hbm/sound/AudioWrapper.java','com/hbm/packet/threading/ThreadedPacket.java','com/hbm/packet/threading/PrecompiledPacket.java']
with tempfile.TemporaryDirectory(prefix='hbm-thermal-family-') as d:
 files=[]
 for path,text in collaborators.items():
  target=Path(d)/path;target.parent.mkdir(parents=True,exist_ok=True);target.write_bytes(text.encode('utf-8'));files.append(str(target))
 for path in production:print('PRODUCTION',path,hashlib.sha256((S/path).read_bytes()).hexdigest(),flush=True)
 files += [str(S/path) for path in production]+[str(R/'porting/tests/ThermalFurnaceFamilyNativeTest.java')]
 subprocess.run([str(a.java_home/'bin'/('javac'+exe)),'-encoding','UTF-8','-source','8','-target','8','-cp',a.classpath,'-d',d]+files,check=True)
 subprocess.run([str(a.java_home/'bin'/('java'+exe)),'-cp',os.pathsep.join([d,a.classpath]),'ThermalFurnaceFamilyNativeTest'],cwd=d,check=True)
print('PASS complete thermal family/dependency native compilation and behavior; controlled NTM boundaries, no gameplay claim.',flush=True)
