#!/usr/bin/env python3
"""Compile actual AE2 integration against the real supplied/remapped AE2 and Forge.

Only unported NTM surroundings are fixtures. Exact production storage helper
method bodies are extracted, not reimplemented. No AE2/Minecraft/Forge types
are supplied by this test. Runtime bootstrap uses actual AE2 classes.
"""
import argparse
import os
from pathlib import Path
import subprocess
import tempfile

parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('--java-home', type=Path, required=True)
classpath_arg = parser.add_mutually_exclusive_group(required=True)
classpath_arg.add_argument('--classpath')
classpath_arg.add_argument('--classpath-file', type=Path)
args = parser.parse_args()
if args.classpath_file:
    args.classpath = args.classpath_file.read_text(encoding='utf-8').strip()
root = Path(__file__).resolve().parents[2]
src = root / 'src/main/java/com/hbm'

def exe(name):
    for suffix in ('.exe', '') if os.name == 'nt' else ('', '.exe'):
        p = args.java_home / 'bin' / (name + suffix)
        if p.is_file(): return str(p)
    raise FileNotFoundError(name)

def extract(path, declaration):
    s = (src / path).read_text(encoding='utf-8')
    start = s.index(declaration)
    i = s.index('{', start) + 1
    depth = 1
    while depth:
        depth += (s[i] == '{') - (s[i] == '}')
        i += 1
    return s[start:i]

msu = '\n'.join(extract('tileentity/machine/storage/TileEntityMassStorage.java', d) for d in (
    'public int getTotalStockpile()', 'public int increaseTotalStockpile(',
    'public int decreaseTotalStockpile(', 'private int changeTotalStockpile(', 'public ItemStack getType()'))
afl = '\n'.join(extract('tileentity/machine/TileEntityMachineArcFurnaceLarge.java', d) for d in (
    'public ItemStack distributeInput(', 'public ItemStack collectRequested(', 'public ItemStack getAvailableItemFromSlot('))
fixtures = {
    'tileentity/machine/storage/TileEntityMassStorage.java': '''package com.hbm.tileentity.machine.storage;
import net.minecraft.item.ItemStack; import net.minecraft.tileentity.TileEntity;
import com.hbm.util.ItemStackUtil;
/** NTM-only surrounding state. Operations below are exact extracted production methods. */
public class TileEntityMassStorage extends TileEntity {
 public ItemStack[] slots = new ItemStack[3]; public int stack; public int capacity = 100;
 public boolean output; public boolean locked; public int dirties;
 public int getCapacity(){return capacity;} public int getStockpile(){return stack;}
 @Override public void markDirty(){dirties++;}
''' + msu + '\n}',
    'tileentity/machine/TileEntityMachineArcFurnaceLarge.java': '''package com.hbm.tileentity.machine;
import net.minecraft.item.ItemStack; import net.minecraft.tileentity.TileEntity;
import com.hbm.util.ItemStackUtil; import com.hbm.items.ModItems;
import com.hbm.inventory.recipes.ArcFurnaceRecipes;
import com.hbm.inventory.recipes.ArcFurnaceRecipes.ArcFurnaceRecipe;
/** NTM-only surroundings. Operations below are exact extracted production methods. */
public class TileEntityMachineArcFurnaceLarge extends TileEntity {
 public ItemStack[] slots = new ItemStack[30]; public boolean liquidMode; public float lid;
 public int maxInput=64; public int dirties;
 public int getMaxInputSize(){return maxInput;}
 public void setInventorySlotContents(int i,ItemStack s){slots[i]=s;markDirty();}
 @Override public void markDirty(){dirties++;}
''' + afl + '\n}',
    'tileentity/TileEntityProxyCombo.java': '''package com.hbm.tileentity;
import net.minecraft.tileentity.TileEntity;
/** NTM-only proxy resolution control; real TileEntity lifecycle remains native. */
public class TileEntityProxyCombo extends TileEntity {
 public TileEntity core; public boolean inventory;
 public TileEntity getTile(){return core;}
}''',
    'util/ItemStackUtil.java': '''package com.hbm.util;
import net.minecraft.item.ItemStack;
public class ItemStackUtil {''' + extract('util/ItemStackUtil.java', 'public static boolean areStacksCompatible(') + '\n}',
    'items/ModItems.java': '''package com.hbm.items;
import net.minecraft.item.Item; import net.minecraft.init.Items;
public class ModItems {public static final Item arc_electrode=Items.BLAZE_ROD;}
''',
    'inventory/recipes/ArcFurnaceRecipes.java': '''package com.hbm.inventory.recipes;
import net.minecraft.item.ItemStack; import net.minecraft.init.Items;
/** NTM recipe lookup fixture; two controlled native-item recipes, not a production recipe database. */
public class ArcFurnaceRecipes {
 public static class ArcFurnaceRecipe {public ItemStack solidOutput; public Object fluidOutput;}
 public static ArcFurnaceRecipe getOutput(ItemStack s,boolean liquid) {
  if(s.isEmpty() || s.getItem()!=Items.IRON_ORE) return null;
  ArcFurnaceRecipe r=new ArcFurnaceRecipe();r.solidOutput=new ItemStack(Items.IRON_INGOT,2);r.fluidOutput=new Object();return r;
 }
}'''.replace('Items.IRON_ORE', 'net.minecraft.item.Item.getItemFromBlock(net.minecraft.init.Blocks.IRON_ORE)'),
}
classpath_entries = args.classpath.split(os.pathsep)
ae2_entries = [p for p in classpath_entries if 'appliedenergistics2' in Path(p).name]
assert len(ae2_entries) == 1 and 'deobf' in ae2_entries[0], 'Use exactly one genuine Gradle-remapped AE2 JAR, not the original SRG JAR.'
files = list((src / 'handler/ae2').glob('*.java'))
files += [root / 'porting/tests/AE2NativeLauncher.java', root / 'porting/tests/AE2StorageNativeTest.java', root / 'porting/tests/AE2WithoutModClassLoaderTest.java']
with tempfile.TemporaryDirectory(prefix='ae2-storage-native-') as temp:
    temp = Path(temp)
    output = temp / 'classes'; output.mkdir()
    for name, text in fixtures.items():
        p = temp / 'fixtures/com/hbm' / name
        p.parent.mkdir(parents=True, exist_ok=True); p.write_text(text, encoding='utf-8'); files.append(p)
    result = subprocess.run([exe('javac'), '-source','8','-target','8','-proc:none','-implicit:none','-encoding','UTF-8',
                    '-cp',args.classpath,'-d',str(output)] + [str(f) for f in files])
    if result.returncode: raise SystemExit('Native AE2 javac failed; see diagnostics above.')
    cp = str(output) + os.pathsep + args.classpath
    result = subprocess.run([exe('java'), '-cp',cp,'AE2NativeLauncher'], cwd=temp)
    if result.returncode: raise SystemExit('Native AE2 execution failed; see diagnostics above.')
    no_ae2 = str(output) + os.pathsep + os.pathsep.join(p for p in classpath_entries if p not in ae2_entries)
    result = subprocess.run([exe('java'), '-cp',no_ae2,'AE2WithoutModClassLoaderTest'], cwd=temp)
    if result.returncode: raise SystemExit('No-AE2 execution failed; see diagnostics above.')
print('Whole AE2 integration/provider compiled; exact 8 production tile helper bodies tested with native MC/Forge/AE2. Not a full-machine/gameplay test.')
