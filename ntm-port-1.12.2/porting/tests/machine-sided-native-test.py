#!/usr/bin/env python3
"""Native dispatch probe for EXTRACTED machine callbacks, not full machine compilation.
Compiles the complete production inventory base, ForgeDirection, IBatteryItem
and IConditionalInvAccess.
No Minecraft API stubs. Registry identities, ItemStamp, proxy target resolution/coordinate fields are controlled
fixture dependencies; unrelated machine lifecycle/recipes are not compiled.
"""
import argparse
import os
from pathlib import Path
import re
import subprocess
import tempfile

ROOT = Path(__file__).resolve().parents[2]
SRC = ROOT / 'src/main/java'

def method(source, name):
    match = re.search(r'public\s+(?:int\[\]|boolean)\s+' + name + r'\s*\(', source)
    if not match:
        raise AssertionError('Missing native callback: ' + name)
    start = match.start()
    brace = source.index('{', start)
    depth, end = 1, brace + 1
    while depth:
        depth += (source[end] == '{') - (source[end] == '}')
        end += 1
    result = source[start:end]
    if name != 'isItemValidForSlot':
        assert 'EnumFacing ' in result[:result.index('{')], 'Not a native signature: ' + result
    return '@Override\n' + result

parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('--java-home', type=Path, required=True)
parser.add_argument('--classpath', required=True)
args = parser.parse_args()
configs = [
    ('BatteryMachine', 'TileEntityMachineBattery', ['slots_top', 'slots_bottom', 'slots_side'], ['getSlotsForFace', 'canInsertItem', 'canExtractItem', 'isItemValidForSlot']),
    ('Combo', 'TileEntityProxyCombo', [], ['getSlotsForFace', 'canInsertItem', 'canExtractItem']),
    ('Proxy', 'TileEntityProxyInventory', [], ['getSlotsForFace', 'canInsertItem', 'canExtractItem']),
    ('Electric', 'TileEntityMachineElectricFurnace', ['slots_io'], ['getSlotsForFace', 'canExtractItem']),
    ('Press', 'TileEntityMachinePress', [], ['getSlotsForFace', 'canInsertItem', 'canExtractItem', 'isItemValidForSlot']),
    ('Drum', 'TileEntityStorageDrum', ['slots_arr'], ['getSlotsForFace', 'canInsertItem', 'canExtractItem', 'isItemValidForSlot']),
    ('ICFPress', 'TileEntityICFPress', ['topBottom', 'sides'], ['getSlotsForFace', 'canExtractItem']),
    ('Funnel', 'TileEntityMachineFunnel', ['topAccess', 'bottomAccess'], ['getSlotsForFace', 'canExtractItem']),
]
fixtures, registry = [], set()
for short, owner, fields, methods in configs:
    paths = list(SRC.rglob(owner + '.java'))
    assert len(paths) == 1, owner
    source = paths[0].read_text(encoding='utf-8')
    parts = [method(source, name) for name in methods]
    if short == 'Proxy':
        # Only target resolution is controlled; all three forwarding callbacks are production.
        parts.append('ISidedInventory target; private ISidedInventory getBase(){return target;}')
    for field in fields:
        found = re.search(r'(?:public|private|protected)\s+(?:(?:static|final)\s+)*int\[\]\s+' + field + r'\s*=\s*new int\[\]\s*\{[^}]*\}\s*;', source)
        assert found, 'Missing production array ' + owner + '.' + field
        parts.append(found.group())
    if short == 'Combo':
        # Lifecycle/coordinate storage are fixture context; production forwarding stays verbatim.
        parts.append('boolean inventory; int xCoord=17, yCoord=-3, zCoord=209; Object core; Object getCoreObject(){return core;}')
    registry.update(re.findall(r'ModItems\.(\w+)', '\n'.join(parts)))
    fixtures.append('static class ' + short + ' extends TileEntityInventoryBase {\n' + short + '(){super(24);}\n@Override protected String getDefaultName(){return "probe";}\n' + '\n'.join(parts) + '\n}')
imports = '''import com.hbm.tileentity.TileEntityInventoryBase;
import com.hbm.lib.ForgeDirection;
import com.hbm.tileentity.IConditionalInvAccess;
import api.hbm.energymk2.IBatteryItem;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.util.EnumFacing;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;
import java.util.Arrays;
'''
registry_class = 'static class ModItems {\n' + '\n'.join('static final Item ' + n + ' = new Item();' for n in sorted(registry)) + '\n}\nstatic class ItemStamp extends Item {}\n'
template = (ROOT / 'porting/tests/machine-sided-native-test.java.in').read_text(encoding='utf-8')
with tempfile.TemporaryDirectory(prefix='hbm-machine-sided-native-') as temp:
    out = Path(temp)
    test = out / 'MachineSidedNativeTest.java'
    test.write_text(imports + 'public class MachineSidedNativeTest {\n' + registry_class + '\n'.join(fixtures) + template + '\n}', encoding='utf-8')
    production = ['com/hbm/tileentity/IConditionalInvAccess.java', 'com/hbm/tileentity/TileEntityInventoryBase.java', 'com/hbm/lib/ForgeDirection.java', 'api/hbm/energymk2/IBatteryItem.java']
    exe = '.exe' if os.name == 'nt' else ''
    subprocess.run([str(args.java_home / 'bin' / ('javac' + exe)), '-source', '8', '-target', '8', '-encoding', 'UTF-8', '-cp', args.classpath, '-d', temp] + [str(SRC / p) for p in production] + [str(test)], check=True)
    subprocess.run([str(args.java_home / 'bin' / ('java' + exe)), '-cp', os.pathsep.join([temp, args.classpath]), 'MachineSidedNativeTest'], cwd=temp, check=True)
print('Scope: extracted current production callbacks + complete inventory base/ForgeDirection/IBatteryItem/IConditionalInvAccess; NOT full machines or gameplay.')
