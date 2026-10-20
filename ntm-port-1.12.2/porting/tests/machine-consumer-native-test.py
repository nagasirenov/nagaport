#!/usr/bin/env python3
"""Compile/run verbatim extracted transfer methods against real Minecraft/Forge.
Not full machine compilation or gameplay. Tube filters/registry and receiver hard-cap
class are controlled domain fixtures; Minecraft classes are never stubbed.
"""
import argparse, os, re, subprocess, tempfile
from pathlib import Path
ROOT=Path(__file__).resolve().parents[2]
SRC=ROOT/'src/main/java'
def extract(path, marker):
 s=(SRC/path).read_text(encoding='utf-8');a=s.index(marker);b=s.index('{',a);d=1;e=b+1
 while d:
  d+=(s[e]=='{')-(s[e]=='}');e+=1
 return s[a:e]
def methods(path, markers):return '\n'.join(extract(path,m) for m in markers)
p=argparse.ArgumentParser(description=__doc__);p.add_argument('--java-home',type=Path,required=True);p.add_argument('--classpath',required=True);a=p.parse_args()
exe='.exe' if os.name=='nt' else ''
if not (a.java_home/'bin'/('javac'+exe)).is_file() and (a.java_home.parent/'bin'/('javac'+exe)).is_file(): a.java_home=a.java_home.parent
inv=methods('com/hbm/util/InventoryUtil.java',['public static ItemStack tryAddItemToExistingStack(IInventory','public static boolean tryAddItemToNewSlot(IInventory','public static boolean mergeItemStack(','public static boolean doesStackDataMatch('])
item=methods('com/hbm/util/ItemStackUtil.java',['public static ItemStack carefulCopyWithSize(','public static boolean areStacksCompatible('])
math=methods('com/hbm/util/BobMathUtil.java',['public static int min(','public static void shuffleIntArray(','public static void reverseIntArray('])
network='com/hbm/uninos/networkproviders/PneumaticNetwork.java'
s=(SRC/network).read_text(encoding='utf-8');fields=s[s.index('public static final byte SEND_FIRST'):s.index('public void addReceiver')]
net=fields+methods(network,['public void addReceiver(','public boolean send(','public static int[] getSlotAccess(','public static class ReceiverComparator'])
imports='''import java.util.*;
import java.util.Map.Entry;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.init.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.MathHelper;
import com.hbm.lib.ForgeDirection;
import com.hbm.tileentity.TileEntityInventoryBase;
import com.hbm.util.Tuple.Triplet;
'''
classes='static class InventoryUtil {'+inv+'}\nstatic class ItemStackUtil {'+item+'}\nstatic class BobMathUtil {'+math+'}\nstatic class Network {'+net+'}\n'
classes+='static class NodeNet {'+extract('com/hbm/uninos/NodeNet.java','public static boolean isBadLink(')+'}\n'
classes+='static class TileEntityPneumoTube extends TileEntity { boolean whitelist=true; boolean accepts=true; boolean matchesFilter(ItemStack s){return accepts;}'+extract('com/hbm/tileentity/network/TileEntityPneumoTube.java','public static int getIdentifier(')+'}\n'
classes+='static class TileEntityMachineAutocrafter extends Inv { TileEntityMachineAutocrafter(){super(2);} }\n'
classes+='interface ILoadedTile {'+re.search(r'public boolean isLoaded\(\);',(SRC/'api/hbm/tile/ILoadedTile.java').read_text(encoding='utf-8')).group()+'}\n'
classes+='static class SatDock extends Inv {SatDock(){super(16);}'+methods('com/hbm/tileentity/machine/TileEntityMachineSatDock.java',['public ItemStack removeStackFromSlot(', 'public void setInventorySlotContents('])+'}\n'
code=imports+'public class MachineConsumerNativeTest {\n'+classes+(ROOT/'porting/tests/machine-consumer-native-test.java.in').read_text(encoding='utf-8')+'\n}'
with tempfile.TemporaryDirectory(prefix='hbm-machine-consumer-') as t:
 f=Path(t)/'MachineConsumerNativeTest.java';f.write_text(code, encoding='utf-8')
 prod=['com/hbm/inventory/SlotPattern.java','com/hbm/tileentity/TileEntityInventoryBase.java','com/hbm/lib/ForgeDirection.java','com/hbm/util/Tuple.java','com/hbm/interfaces/Spaghetti.java']
 exe='.exe' if os.name=='nt' else ''
 subprocess.run([str(a.java_home/'bin'/('javac'+exe)),'-source','8','-target','8','-encoding','UTF-8','-cp',a.classpath,'-d',t]+[str(SRC/x) for x in prod]+[str(f)],check=True)
 subprocess.run([str(a.java_home/'bin'/('java'+exe)),'-cp',os.pathsep.join([t,a.classpath]),'MachineConsumerNativeTest'],cwd=t,check=True)
print('Scope: actual extracted InventoryUtil insertion/Slot merge and PneumaticNetwork transfer, plus SatDock setter/removal and full SlotPattern; not full machines or gameplay.')
