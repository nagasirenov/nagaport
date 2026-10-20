#!/usr/bin/env python3
"""Real complete dynamic recipes, with byte-exact extracted NTM NBT accessors."""
import argparse,hashlib,re,subprocess,tempfile
from pathlib import Path
ROOT=Path(__file__).resolve().parents[2];SRC=ROOT/'src/main/java'
p=argparse.ArgumentParser();p.add_argument('--java-home',type=Path,required=True);p.add_argument('--classpath',required=True);a=p.parse_args()
subprocess.run(['python3',str(ROOT/'porting/tests/check-dynamic-crafting-source.py')],check=True)
def body(path,signature):
 s=(SRC/path).read_text();start=s.index(signature);left=s.index('{',start);depth=1;i=left+1
 while depth:
  depth+=(s[i]=='{')-(s[i]=='}');i+=1
 result=s[start:i];print('EXACT ACCESSOR',path,signature,hashlib.sha256(result.encode()).hexdigest(),flush=True);return result
rodpath='com/hbm/items/machine/ItemRBMKRod.java'
methods=['public static double '+n+'(ItemStack stack)' for n in ['getEnrichment','getPoisonLevel','getYield','getPoison','getCoreHeat','getHullHeat']]
methods+=['public static void '+n+'(ItemStack stack, double '+arg+')' for n,arg in [('setYield','yield'),('setCoreHeat','heat'),('setHullHeat','heat')]]
methods+=['public static double getDouble(ItemStack stack, String key)','public static void setDouble(ItemStack stack, String key, double yield)','private static void setNBTDefaults(ItemStack stack)']
imports='import net.minecraft.item.*; import net.minecraft.nbt.NBTTagCompound; '
rod='package com.hbm.items.machine; '+imports+'public class ItemRBMKRod extends Item { public Item pellet; public double yield; '+ '\n'.join(body(rodpath,m) for m in methods)+'}'
scrappath='com/hbm/items/machine/ItemScraps.java'
scrap='package com.hbm.items.machine; '+imports+'import com.hbm.items.ModItems; import com.hbm.inventory.material.*; import com.hbm.inventory.material.Mats.MaterialStack; public class ItemScraps extends Item {'+'\n'.join(body(scrappath,s) for s in ['public static MaterialStack getMats(ItemStack stack)','public static ItemStack create(MaterialStack stack)','public static ItemStack create(MaterialStack stack, boolean liquid)'])+'}'
shape=(SRC/'com/hbm/inventory/material/MaterialShapes.java').read_text()
assert 'new MaterialShapes(8, "nugget", "tiny")' in shape and 'new MaterialShapes(NUGGET.quantity * 9, "ingot")' in shape
q=body('com/hbm/inventory/material/MaterialShapes.java','public int q(int amount)')
fixtures={
 'com/hbm/items/machine/ItemRBMKRod.java':rod,
 'com/hbm/items/machine/ItemScraps.java':scrap,
 'ExtractedContainerPredicates.java':'import net.minecraft.item.*; public class ExtractedContainerPredicates { public static class StackSensitive extends Item {'+body('com/hbm/items/tool/ItemCraftingDegradation.java','public boolean hasContainerItem(ItemStack stack)')+'} public static class NoArg extends Item {'+body('com/hbm/items/machine/ItemFluidIDMulti.java','public boolean hasContainerItem()')+'}}',
 'com/hbm/items/ModItems.java':'package com.hbm.items; public class ModItems {public static net.minecraft.item.Item scraps,nothing,ammo_arty,ammo_shell;}',
 'com/hbm/inventory/material/NTMMaterial.java':'package com.hbm.inventory.material; public class NTMMaterial { public int id; public NTMMaterial(int id){this.id=id;} }',
 'com/hbm/inventory/material/Mats.java':'package com.hbm.inventory.material; public class Mats { public static java.util.Map<Integer,NTMMaterial> matById=new java.util.HashMap<>();'+body('com/hbm/inventory/material/Mats.java','public static class MaterialStack')+'}',
 'com/hbm/inventory/material/MaterialShapes.java':'package com.hbm.inventory.material; public class MaterialShapes {public static MaterialShapes INGOT=new MaterialShapes();public int quantity=72;'+q+'}',
}
manager=(SRC/'com/hbm/main/CraftingManager.java').read_text();statements=re.findall(r'^\s*(NtmCraftingRegistry.special\("special/(?:rbmk_fuel|scraps|cargo_shell)"[^\n]+);$',manager,re.M);assert len(statements)==3
fixtures['ExactDynamicProducers.java']='import com.hbm.crafting.nativeapi.NtmCraftingRegistry; import com.hbm.crafting.handlers.*; public class ExactDynamicProducers {public static void register(){'+';'.join(statements)+';}}'
for s in statements:print('EXACT PRODUCER',s,flush=True)
with tempfile.TemporaryDirectory(prefix='ntm-dynamic-crafting-') as tmp:
 files=[]
 for rel,text in fixtures.items():
  path=Path(tmp)/rel;path.parent.mkdir(parents=True,exist_ok=True);path.write_text(text);files.append(str(path))
 for rel in ['com/hbm/crafting/handlers/RBMKFuelCraftingHandler.java','com/hbm/crafting/handlers/ScrapsCraftingHandler.java','com/hbm/crafting/handlers/CargoShellCraftingHandler.java','com/hbm/crafting/nativeapi/NtmCraftingRegistry.java','com/hbm/crafting/nativeapi/NtmCraftingRecipe.java']:
  path=SRC/rel;files.append(str(path));print('PRODUCTION',rel,hashlib.sha256(path.read_bytes()).hexdigest(),flush=True)
 files += [str(ROOT/'porting/tests'/n) for n in ['ForgeEventNativeLauncher.java','DynamicCraftingNativeTest.java']]
 subprocess.run([str(a.java_home/'bin/javac'),'-source','8','-target','8','-cp',a.classpath,'-d',tmp]+files,check=True)
 subprocess.run([str(a.java_home/'bin/java'),'-cp',tmp+':'+a.classpath,'ForgeEventNativeLauncher','DynamicCraftingNativeTest'],cwd=tmp,check=True)
