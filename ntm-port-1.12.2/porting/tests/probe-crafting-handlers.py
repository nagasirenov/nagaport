#!/usr/bin/env python3
"""Bounded complete-owner tests for MKU/storage crafting, with explicit NTM identity fixtures."""
import argparse, hashlib, json, re, subprocess, tempfile
from pathlib import Path
ROOT = Path(__file__).resolve().parents[2]
SRC = ROOT / 'src/main/java'
p = argparse.ArgumentParser()
p.add_argument('--java-home', type=Path, required=True)
p.add_argument('--classpath', required=True)
a = p.parse_args()
subprocess.run(['python3', str(ROOT / 'porting/tests/check-crafting-handlers-source.py')], check=True)
manager = (SRC / 'com/hbm/main/CraftingManager.java').read_text()
rod = (SRC / 'com/hbm/crafting/RodRecipes.java').read_text()
statements = re.findall(r'^\s*(NtmCraftingRegistry\.special\("(?:special/mku|container_upgrade/[^\"]+)"[^\n]+);$', manager, re.M)
statements += re.findall(r'^\s*(NtmCraftingRegistry\.shaped\("rod/zirnox_empty"[^\n]+);$', rod, re.M)
assert len(statements) == 7
for statement in statements: print('EXACT PRODUCTION STATEMENT', statement, flush=True)
items = 'plate_desh circuit rod_zirnox_empty powder_iodine powder_fire dust ingot_mercury morning_glory syringe_metal_empty syringe_mkunicorn flame_pony'.split()
enum_source = (SRC / 'com/hbm/items/machine/ItemCircuit.java').read_text()
enum = re.search(r'public static enum EnumCircuitType \{[^}]+\}', enum_source).group(0)
collaborators = {
 'com/hbm/blocks/generic/BlockStorageCrate.java': 'package com.hbm.blocks.generic; public class BlockStorageCrate extends net.minecraft.block.Block { public BlockStorageCrate(){super(net.minecraft.block.material.Material.IRON);} }',
 'com/hbm/blocks/machine/BlockMassStorage.java': 'package com.hbm.blocks.machine; public class BlockMassStorage extends net.minecraft.block.Block { public BlockMassStorage(){super(net.minecraft.block.material.Material.IRON);} }',
 'com/hbm/blocks/ModBlocks.java': '''package com.hbm.blocks; public class ModBlocks {
 public static final com.hbm.blocks.generic.BlockStorageCrate crate_desh=new com.hbm.blocks.generic.BlockStorageCrate(), crate_tungsten=new com.hbm.blocks.generic.BlockStorageCrate(), crate_steel=new com.hbm.blocks.generic.BlockStorageCrate(), safe=new com.hbm.blocks.generic.BlockStorageCrate();
 public static final com.hbm.blocks.machine.BlockMassStorage mass_storage=new com.hbm.blocks.machine.BlockMassStorage(); }''',
 'com/hbm/items/ModItems.java': 'package com.hbm.items; public class ModItems { public static net.minecraft.item.Item ' + ', '.join(items) + '; }',
 'com/hbm/items/machine/ItemCircuit.java': 'package com.hbm.items.machine; public class ItemCircuit {' + enum + '}',
 'com/hbm/items/special/ItemBookLore.java': '''package com.hbm.items.special; public class ItemBookLore {
 public static net.minecraft.item.ItemStack createBook(String key,int pages,int a,int b){ throw new AssertionError("Book rendering outside crafting probe"); }
 public static void addArgs(net.minecraft.item.ItemStack stack,int page,String arg){throw new AssertionError("Book rendering outside crafting probe");} }''',
 'com/hbm/inventory/OreDictManager.java': '''package com.hbm.inventory;
 public class OreDictManager {
 public static final DictFrame W=new DictFrame("Tungsten"),CU=new DictFrame("Copper"),PB=new DictFrame("Lead"),ALLOY=new DictFrame("AdvancedAlloy"),DESH=new DictFrame("Desh"),ANY_RESISTANTALLOY=new DictFrame("AnyResistantAlloy"),ZR=new DictFrame("Zirconium"),BE=new DictFrame("Beryllium");
 public static class DictFrame { final String name; public DictFrame(String name){this.name=name;}
 public String block(){return "block"+name;} public String plateCast(){return "plateTriple"+name;} public String plate(){return "plate"+name;} public String ingot(){return "ingot"+name;} public String nugget(){return "nugget"+name;}
 public static net.minecraft.item.ItemStack fromOne(net.minecraft.item.Item item,Enum<?> variant){return new net.minecraft.item.ItemStack(item,1,variant.ordinal());} } }''',
}
# Freeze actual ore-name values used by these expressions, rather than guessing fixture aliases.
ore_source = (SRC / 'com/hbm/inventory/OreDictManager.java').read_text()
shape_source = (SRC / 'com/hbm/inventory/material/MaterialShapes.java').read_text()
for name in ['Tungsten','Copper','Lead','AdvancedAlloy','Desh','AnyResistantAlloy','Zirconium','Beryllium']:
 assert re.search(r'new Dict(?:Frame|Group)\("'+name+'"', ore_source)
assert '"plateTriple"' in shape_source
assert re.search(r'String ingot\(\) \{\s*return INGOT\.name\(\)\s*\+ groupName;',ore_source)
assert re.search(r'ItemStack fromOne\(Item item, Enum en\) \{\s*return new ItemStack\(item, 1, en\.ordinal\(\)\);',ore_source)
for prefix, method in [('BLOCK','block'),('CASTPLATE','plateCast'),('PLATE','plate'),('INGOT','ingot'),('NUGGET','nugget')]:
 assert re.search(r'String '+method+r'\(\) \{\s*return '+prefix+r'\.name\(\)\s*\+ mats\[0\];',ore_source)
production = ['com/hbm/crafting/handlers/'+n+'.java' for n in ['ContainerUpgradeCraftingHandler','MKUCraftingHandler','ShapedOreRecipeExt']]
production += ['com/hbm/crafting/nativeapi/NtmCraftingRecipe.java','com/hbm/crafting/nativeapi/NtmCraftingRegistry.java','com/hbm/registry/GameRegistry.java','com/hbm/lib/RefStrings.java']
with tempfile.TemporaryDirectory(prefix='ntm-handlers-native-') as temporary:
 out = Path(temporary); files=[]
 for rel, text in collaborators.items():
  f=out/rel;f.parent.mkdir(parents=True,exist_ok=True);f.write_text(text);files.append(str(f))
 fixture = out/'ExactHandlerProducers.java'
 fixture.write_text('import com.hbm.crafting.nativeapi.NtmCraftingRegistry; import com.hbm.crafting.handlers.*; import com.hbm.blocks.ModBlocks; import com.hbm.items.ModItems; import net.minecraft.item.ItemStack; import static com.hbm.inventory.OreDictManager.*; import com.hbm.inventory.OreDictManager.DictFrame; import com.hbm.items.machine.ItemCircuit.EnumCircuitType; public class ExactHandlerProducers { public static void register(){\n' + ';\n'.join(statements) + ';\n} }')
 files.append(str(fixture))
 for rel in production:
  f=SRC/rel;files.append(str(f));print('PRODUCTION',rel,hashlib.sha256(f.read_bytes()).hexdigest(),flush=True)
 files += [str(ROOT/'porting/tests/ForgeEventNativeLauncher.java'),str(ROOT/'porting/tests/CraftingHandlersNativeTest.java')]
 subprocess.run([str(a.java_home/'bin/javac'),'-encoding','UTF-8','-source','8','-target','8','-cp',a.classpath,'-d',temporary]+files,check=True)
 subprocess.run([str(a.java_home/'bin/java'),'-cp',temporary+':'+a.classpath,'ForgeEventNativeLauncher','CraftingHandlersNativeTest'],cwd=temporary,check=True)
