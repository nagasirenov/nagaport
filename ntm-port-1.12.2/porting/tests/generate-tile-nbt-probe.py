#!/usr/bin/env python3
"""Extract exact production writer methods into explicitly limited native fixtures.
These are not replacement production classes and are never in production sources.
"""
import importlib.util
import sys
from pathlib import Path
spec = importlib.util.spec_from_file_location('parity', Path(__file__).with_name('check-tile-nbt-migration.py'))
check = importlib.util.module_from_spec(spec); spec.loader.exec_module(check)
check.main()
texts, methods, _ = check.scan()

def method(owner):
    matches = [m for m in methods if m['owner']['name'] == owner and m['tile']]
    assert len(matches) == 1
    m = matches[0]
    return texts[m['path']][m['start']:m['body_end']+1]

parts = ['''import net.minecraft.tileentity.TileEntity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.item.ItemStack;
// GENERATED, method-only native fixtures. Not actual production tile classes.
public final class ExtractedTileWriters {
public static class Loaded extends TileEntity {
 public boolean muffled;
''', method('TileEntityLoadedBase'), '''
}
public static class Inventory extends Loaded {
 public ItemStack[] slots = new ItemStack[4];
 public String customName;
''', method('TileEntityMachineBase'), '''
}
public static class Furnace extends Inventory {
 public long power;
 public int progress;
''', method('TileEntityMachineElectricFurnace'), '''
}
public static class Hatch extends TileEntity {
 public int x, y, z;
''', method('TileEntityHatch'), '\n}\n']
custom = method('TileEntityCustomMachine')
start = custom.index('if(machineType == null || this.config == null)')
opening = custom.index('{', start)
closing = custom.index('}', opening)
fragment = custom[start:closing+1]
# Only the complete early-return branch is extracted; real CustomMachine parent,
# tanks/recipe/matcher and normal branch are expressly NOT represented.
parts += ['''public static class EarlyBranch extends Inventory {
 public String machineType;
 public Object config;
 public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
''', fragment, '''
 throw new AssertionError("Normal CustomMachine path is outside this fixture");
 }
}
}
''']
Path(sys.argv[1]).write_text('\n'.join(parts))
print('Generated exact LoadedBase/MachineBase/ElectricFurnace/Hatch writers and CustomMachine early branch only')
