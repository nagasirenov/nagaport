#!/usr/bin/env python3
"""Complete actual furnace and inventory classes; controlled NTM surroundings."""
import ast, hashlib, pathlib, re, subprocess, tempfile, json, zipfile
R=pathlib.Path(__file__).resolve().parents[3];S=R/'src/main/java';H=pathlib.Path(__file__).resolve().parent
m=json.loads((H/'source.json').read_text());text=(R/m['path']).read_text();assert hashlib.sha256(text.encode()).hexdigest()==m['after']
before=text
for old,new in reversed(m['edits']):
 assert new in before
 before=before.replace(new,old)
assert hashlib.sha256(before.encode()).hexdigest()==m['before']
parsed=ast.parse((R/'porting/tests/probe-electric-furnace-core.py').read_text())
c=next(ast.literal_eval(n.value) for n in parsed.body if isinstance(n,ast.Assign) and any(isinstance(t,ast.Name) and t.id=='collaborators' for t in n.targets))
for k in list(c):
 if any(x in k for x in ['MachineElectricFurnace','Nodespace']):del c[k]
texts=(S/'com/hbm/tileentity/machine/TileEntityDiFurnace.java').read_text()
items=sorted(set(re.findall(r'ModItems\.(\w+)',texts)))
c['com/hbm/items/ModItems.java']='package com.hbm.items; public class ModItems {'+' '.join('public static net.minecraft.item.Item '+x+';' for x in items)+'}'
c['com/hbm/blocks/ModBlocks.java']='package com.hbm.blocks; public class ModBlocks {public static net.minecraft.block.Block block_coke=net.minecraft.init.Blocks.COAL_BLOCK,machine_difurnace_extension=net.minecraft.init.Blocks.IRON_BLOCK;}'
c['com/hbm/blocks/machine/MachineDiFurnace.java']='package com.hbm.blocks.machine; public class MachineDiFurnace { public static int calls; public static boolean active;public static void updateBlockState(boolean a,net.minecraft.world.World w,int x,int y,int z){calls++;active=a;} }'
c['com/hbm/inventory/container/ContainerDiFurnace.java']='package com.hbm.inventory.container; public class ContainerDiFurnace extends net.minecraft.inventory.Container {public ContainerDiFurnace(net.minecraft.entity.player.InventoryPlayer p,com.hbm.tileentity.machine.TileEntityDiFurnace f){} public boolean canInteractWith(net.minecraft.entity.player.EntityPlayer p){return true;}}'
c['com/hbm/inventory/gui/GUIDiFurnace.java']='package com.hbm.inventory.gui; public class GUIDiFurnace {public GUIDiFurnace(net.minecraft.entity.player.InventoryPlayer p,com.hbm.tileentity.machine.TileEntityDiFurnace f){}}'
c['com/hbm/util/CompatEnergyControl.java']='package com.hbm.util; public class CompatEnergyControl {public static final String L_ENERGY_="energy",L_CAPACITY_="capacity",I_PROGRESS="progress";}'
c['com/hbm/inventory/fluid/tank/FluidTank.java']='package com.hbm.inventory.fluid.tank; public class FluidTank {}'
c['api/hbm/fluid/IFluidStandardSender.java']='package api.hbm.fluid; public interface IFluidStandardSender {com.hbm.inventory.fluid.tank.FluidTank[] getAllTanks();com.hbm.inventory.fluid.tank.FluidTank[] getSendingTanks();}'
c['com/hbm/tileentity/TileEntityMachinePolluting.java']='''package com.hbm.tileentity; public abstract class TileEntityMachinePolluting extends TileEntityMachineBase {public int smokeCalls,polluteCalls;public float amount;public TileEntityMachinePolluting(int n,int b){super(n);} public void sendSmoke(int x,int y,int z,com.hbm.lib.ForgeDirection d){smokeCalls++;} public com.hbm.inventory.fluid.tank.FluidTank[] getSmokeTanks(){return new com.hbm.inventory.fluid.tank.FluidTank[0];} public void pollute(com.hbm.handler.pollution.PollutionHandler.PollutionType t,float n){polluteCalls++;amount+=n;} }'''
# Recipe registry/matcher boundary is controlled. Actual getOutput traversal,
# matching call order and output copy are extracted byte-exactly below.
def method(s,sign):
 b=s.index(sign);e=s.index('{',b)+1;d=1
 while d:d+=(s[e]=='{')-(s[e]=='}');e+=1
 return s[b:e]
s=(S/'com/hbm/inventory/recipes/BlastFurnaceRecipes.java').read_text()
match=method((S/'com/hbm/inventory/RecipesCommon.java').read_text(),'public boolean matchesRecipe(ItemStack stack, boolean ignoreSize)')
c['com/hbm/inventory/recipes/BlastFurnaceRecipes.java']='''package com.hbm.inventory.recipes; import net.minecraft.item.ItemStack;import java.util.*;import net.minecraftforge.oredict.OreDictionary;import com.hbm.util.Tuple.Triplet;public class BlastFurnaceRecipes {public static final ArrayList<Triplet<Object,Object,ItemStack>> blastFurnaceRecipes=new ArrayList<>();public static int calls; public static class AStack {public net.minecraft.item.Item item; public AStack(net.minecraft.item.Item i){item=i;} public int meta=0,stacksize=1;'''+match+''' } private static AStack[] getRecipeStacks(Object x){return new AStack[]{(AStack)x};}'''+method(s,'public static ItemStack getOutput(')+method(s,'private static boolean doStacksMatch(')+'}'
packet=(S/'com/hbm/packet/toclient/BufPacket.java').read_text().split('public class BufPacket extends PrecompiledPacket {',1)[1].split('\n\tpublic static class Handler',1)[0]
c['com/hbm/packet/toclient/BufPacket.java']='package com.hbm.packet.toclient; import com.hbm.packet.threading.PrecompiledPacket; import com.hbm.tileentity.IBufPacketReceiver; import io.netty.buffer.ByteBuf; public class BufPacket extends PrecompiledPacket {'+packet+'\n}'
production=['com/hbm/tileentity/machine/TileEntityDiFurnace.java','com/hbm/tileentity/TileEntityMachineBase.java','com/hbm/tileentity/TileEntityLoadedBase.java','com/hbm/tileentity/IGUIProvider.java','com/hbm/tileentity/IBufPacketReceiver.java','com/hbm/lib/ForgeDirection.java','com/hbm/sound/AudioWrapper.java','com/hbm/packet/threading/ThreadedPacket.java','com/hbm/packet/threading/PrecompiledPacket.java','api/hbm/tile/ILoadedTile.java','api/hbm/tile/IInfoProviderEC.java','com/hbm/world/WorldBlockState.java','com/hbm/util/Tuple.java','com/hbm/interfaces/Spaghetti.java']
j='/workspace/shared/nagaport-toolchain/java8/bin/';cp=pathlib.Path('/tmp/nagaport-ae2-native-classpath.txt').read_text().strip()
with tempfile.TemporaryDirectory(prefix='difurnace-native-') as d:
 files=[]
 for name,source in c.items():
  print('CONTROLLED_NTM',name,hashlib.sha256(source.encode()).hexdigest(),flush=True)
  p=pathlib.Path(d)/name;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(source);files.append(str(p))
 for name in production:print('PRODUCTION',name,hashlib.sha256((S/name).read_bytes()).hexdigest(),flush=True)
 files += [str(S/name) for name in production]+[str(H/'DiFurnaceTest.java')]
 subprocess.run([j+'javac','-cp',cp,'-d',d]+files,check=True)
 subprocess.run([j+'java','-cp',d+':'+cp,'DiFurnaceTest'],cwd=d,check=True)
 # Exercise the fallback on the GENUINE native class with only its private
 # quantity field and all references renamed by the official MCP->SRG mapping.
 mapper=R/'porting/tests/difurnace-raw-reader/RemapQuantityField.java'
 subprocess.run([j+'javac','-cp',cp,'-d',d,str(mapper)],check=True)
 original=None
 for entry in cp.split(':'):
  if pathlib.Path(entry).is_file() and zipfile.is_zipfile(entry):
   with zipfile.ZipFile(entry) as z:
    try:original=z.read('net/minecraft/item/ItemStack.class');break
    except KeyError:pass
 assert original is not None
 originalFile=pathlib.Path(d)/'original-ItemStack.class';originalFile.write_bytes(original)
 renamed=pathlib.Path(d)/'srg/net/minecraft/item/ItemStack.class'
 subprocess.run([j+'java','-cp',d+':'+cp,'RemapQuantityField',str(originalFile),str(renamed)],check=True)
 print('GENUINE_NATIVE_FIELD_REMAP',hashlib.sha256(original).hexdigest(),hashlib.sha256(renamed.read_bytes()).hexdigest(),flush=True)
 subprocess.run([j+'java','-DexpectedQuantityField=field_77994_a','-cp',str(pathlib.Path(d)/'srg')+':'+d+':'+cp,'DiFurnaceTest'],cwd=d,check=True)
 print('PASS genuine native ItemStack SRG-field fallback; field-only runtime layout test, not full obfuscated game launch',flush=True)
 # Compile deliberately incorrect copies in the temporary directory only.
 mutant=pathlib.Path(d)/'com/hbm/tileentity/machine/TileEntityDiFurnace.java';mutant.parent.mkdir(parents=True,exist_ok=True)
 mutations=[('energy','fuel -= 1;','fuel -= 2;'),('merge','slots[3].grow(quantity(itemStack));','slots[3].setCount(quantity(itemStack));'),('last fuel receiver','fuelItem.getContainerItem(this.slots[2])','this.slots[2].getItem().getContainerItem(this.slots[2])'),('exact capacity','<= slots[3].getMaxStackSize()','< slots[3].getMaxStackSize()'),('input cost','this.decrStackSize(i, 1);','this.decrStackSize(i, 2);'),('stale capacity','quantity(slots[3]) + quantity(output)','slots[3].getCount() + output.getCount()'),('second result','if(itemStack == null || itemStack.isEmpty()) return;','if(itemStack == null) return;')]
 for label,old,new in mutations:
  assert text.count(old)==1
  mutant.write_text(text.replace(old,new))
  subprocess.run([j+'javac','-cp',d+':'+cp,'-d',d,str(mutant)],check=True)
  result=subprocess.run([j+'java','-cp',d+':'+cp,'DiFurnaceTest'],cwd=d,stdout=subprocess.PIPE,stderr=subprocess.PIPE)
  assert result.returncode!=0, label+' mutation survived'
  print('PASS rejected mutation',label,flush=True)
print('PASS complete production DiFurnace and inventory bases; controlled NTM recipe registration, pollution, GUI and networking collaborators; no gameplay claim')
