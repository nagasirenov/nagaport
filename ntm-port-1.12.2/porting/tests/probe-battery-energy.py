#!/usr/bin/env python3
"""Native API probe. Whole IBatteryItem, EXTRACTED ItemBattery/Library methods.
No whole ItemBattery, Library, furnace, network, or gameplay compilation claim.
"""
import argparse
import os
from pathlib import Path
import re
import subprocess
import tempfile

ROOT = Path(__file__).resolve().parents[2]
REFERENCE = ROOT.parent / 'ntm-original-pristine'

def method(source, signature):
    start = source.index(signature)
    brace = source.index('{', start)
    depth = 1
    end = brace + 1
    while depth:
        depth += (source[end] == '{') - (source[end] == '}')
        end += 1
    return source[start:end]

def native_nbt(source):
    source = re.sub(r'(\w+)\.stackTagCompound = new NBTTagCompound\(\);', r'\1.setTagCompound(new NBTTagCompound());', source)
    return source.replace('.stackTagCompound', '.getTagCompound()')

def read(root, name):
    return (root / 'src/main/java' / name).read_text(encoding='utf-8')

parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('--classpath', required=True)
parser.add_argument('--java-home', required=True, type=Path)
args = parser.parse_args()
battery_path = 'com/hbm/items/machine/ItemBattery.java'
api_path = 'api/hbm/energymk2/IBatteryItem.java'
current = read(ROOT, battery_path)
reference = read(REFERENCE, battery_path)
assert read(ROOT, api_path) == native_nbt(read(REFERENCE, api_path)), 'IBatteryItem changed beyond NBT access migration'
signatures = [
    'public ItemBattery(', 'public void chargeBattery(', 'public void setCharge(',
    'public void dischargeBattery(', 'public long getCharge(', 'public long getMaxCharge(',
    'public long getChargeRate(', 'public long getDischargeRate(',
    'public static ItemStack getEmptyBattery(', 'public static ItemStack getFullBattery(',
    'public boolean showDurabilityBar(', 'public double getDurabilityForDisplay('
]
methods = []
for signature in signatures:
    actual = method(current, signature)
    assert actual == native_nbt(method(reference, signature)), signature + ' parity changed'
    methods.append(actual)
# The callback bodies preserve strings, arithmetic, rarity branches and empty/full order.
old_tooltip = method(reference, 'public void addInformation(')
assert method(current, 'public void addInformation(') == old_tooltip.replace(
    'ItemStack itemstack, EntityPlayer player, List list, boolean bool',
    'ItemStack itemstack, World world, List<String> list, ITooltipFlag flag')
# Necessary inherited callback migration: preserve the remote's distinct tooltip.
anchor_path = 'com/hbm/items/tool/ItemAnchorRemote.java'
assert method(read(ROOT, anchor_path), 'public void addInformation(') == method(read(REFERENCE, anchor_path), 'public void addInformation(').replace(
    'ItemStack itemstack, EntityPlayer player, List list, boolean bool',
    'ItemStack itemstack, World world, List<String> list, ITooltipFlag flag')
# Inventory all direct/transitive subclasses so a later inherited override is not missed.
subclasses = {'ItemBattery'}
while True:
    previous = set(subclasses)
    for path in (ROOT / 'src/main/java').rglob('*.java'):
        for child, parent in re.findall(r'\bclass\s+(\w+)\s+extends\s+([\w.]+)', path.read_text(encoding='utf-8')):
            if parent.split('.')[-1] in subclasses:
                subclasses.add(child)
    if previous == subclasses:
        break
assert subclasses == {'ItemBattery', 'ItemPotatos', 'ItemAnchorRemote'}, 'Review newly added battery subclasses and callback overrides'
for subclass_path in ['com/hbm/items/special/ItemPotatos.java', anchor_path]:
    source = read(ROOT, subclass_path)
    assert 'void getSubItems(' not in source
    assert 'super.addInformation(' not in source and 'super.getSubItems(' not in source
assert 'void addInformation(' not in read(ROOT, 'com/hbm/items/special/ItemPotatos.java')
assert method(current, 'public EnumRarity getRarity(') == method(reference, 'public EnumRarity getRarity(').replace('EnumRarity.rare', 'EnumRarity.RARE').replace('EnumRarity.uncommon', 'EnumRarity.UNCOMMON').replace('EnumRarity.common', 'EnumRarity.COMMON')
creative = method(reference, 'public void getSubItems(').replace(
    'public void getSubItems(Item item, CreativeTabs tab, List list) {',
    'public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> list) {\n\n\t\tif(!this.isInCreativeTab(tab)) return;').replace('getEmptyBattery(item)', 'getEmptyBattery(this)').replace('getFullBattery(item)', 'getFullBattery(this)')
assert method(current, 'public void getSubItems(') == creative
methods.append(creative)
transfers = []
for signature in ['public static long chargeItemsFromTE(', 'public static long chargeTEFromItems(']:
    actual = method(read(ROOT, 'com/hbm/lib/Library.java'), signature)
    assert actual == method(read(REFERENCE, 'com/hbm/lib/Library.java'), signature), 'Library transfer must remain unchanged'
    # Explicit fixture substitution: only registry identity is supplied by harness.
    transfers.append(actual.replace('ModItems.battery_creative', 'creativeIdentity'))
print('PASS: supplied-reference parity: whole IBatteryItem NBT-only; 12 ItemBattery energy methods; tooltip/rarity/creative callback contracts and ItemAnchorRemote override; complete subclass inventory; unchanged Library transfer operations', flush=True)
with tempfile.TemporaryDirectory(prefix='hbm-native-battery-') as temporary:
    out = Path(temporary)
    imports = 'import net.minecraft.item.*; import net.minecraft.nbt.*; import net.minecraft.creativetab.CreativeTabs; import net.minecraft.util.NonNullList; import api.hbm.energymk2.IBatteryItem;\n'
    (out / 'ItemBattery.java').write_text(imports + 'public class ItemBattery extends Item implements IBatteryItem {\nprotected long maxCharge, chargeRate, dischargeRate;\n' + '\n'.join(methods) + '\n}', encoding='utf-8')
    (out / 'LibraryTransferFixture.java').write_text(imports + 'public class LibraryTransferFixture {\npublic static Item creativeIdentity;\n' + '\n'.join(transfers) + '\n}', encoding='utf-8')
    sources = [ROOT / 'src/main/java' / api_path, ROOT / 'src/main/java/com/hbm/items/machine/ItemBatteryCreative.java', out / 'ItemBattery.java', out / 'LibraryTransferFixture.java', ROOT / 'porting/tests/BatteryEnergyNativeTest.java']
    subprocess.run([str(args.java_home / 'bin' / ('javac.exe' if os.name == 'nt' else 'javac')), '-source', '8', '-target', '8', '-encoding', 'UTF-8', '-cp', args.classpath, '-d', str(out)] + [str(p) for p in sources], check=True)
    subprocess.run([str(args.java_home / 'bin' / ('java.exe' if os.name == 'nt' else 'java')), '-cp', str(out) + os.pathsep + args.classpath, 'BatteryEnergyNativeTest'], check=True)
print('PASS: native battery API + extracted energy/transfer fixtures; NOT full ItemBattery/Library/furnace/network build', flush=True)
