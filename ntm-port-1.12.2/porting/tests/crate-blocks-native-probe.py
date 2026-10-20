#!/usr/bin/env python3
"""Compile both complete production crate owners and the complete nested supply tile
against real Forge/Minecraft 1.12.2 dependencies. Only NTM item registration and
food effects and tool-recipe matching are controlled; the original food-variant enum is extracted unchanged.
No native facade classes, owner extraction, alternate APIs, or production rewrites.
"""
import argparse
import hashlib
import os
from pathlib import Path
import subprocess
import sys
import tempfile

p = argparse.ArgumentParser()
p.add_argument('--java-home', type=Path, required=True)
p.add_argument('--classpath', required=True)
a = p.parse_args()
root = Path(__file__).resolve().parents[2]
src = root / 'src/main/java'
old = root.parent / 'ntm-original-pristine/src/main/java'
tests = root / 'porting/tests'
for name, digest in {
    'BlockCanCrate': '5e919ea587ca86ecb5b034947e7e0110101651b5cfd4df66007904dd69d31911',
    'BlockSupplyCrate': '03b9d45afa713e49b69a2c1b788ff7a81a22e065a00840aa0374d1d40a356e2c',
}.items():
    original = old / ('com/hbm/blocks/generic/' + name + '.java')
    assert hashlib.sha256(original.read_bytes()).hexdigest() == digest
original = (old / 'com/hbm/blocks/generic/BlockCanCrate.java').read_text()
current = (src / 'com/hbm/blocks/generic/BlockCanCrate.java').read_text()
start = '\tpublic ArrayList<ItemStack> getContents'
assert original[original.index(start):] == current[current.index(start):], 'Can choice order/count/random consumption must remain byte-exact.'
print('PASS exact pristine can contents, random choice order, and count bodies', flush=True)
food = (old / 'com/hbm/items/food/ItemConserve.java').read_text()
food_enum = food[food.index('\tpublic static enum EnumFoodType'):food.rindex('\n}')]
current_food = (src / 'com/hbm/items/food/ItemConserve.java').read_text()
assert food_enum == current_food[current_food.index('\tpublic static enum EnumFoodType'):current_food.rindex('\n}')]
print('PASS exact pristine food variant enum retained in controlled food-effect collaborator', flush=True)
items = 'crowbar canned_conserve can_smart can_creature can_redbomb can_mrsugar can_overcharge can_luna can_breen can_bepis pudding'.split()
collaborators = {
    'com/hbm/inventory/RecipesCommon.java': 'package com.hbm.inventory; public class RecipesCommon { public static class ComparableStack { public ComparableStack(net.minecraft.item.ItemStack stack) {} } }',
    'com/hbm/items/ModItems.java': 'package com.hbm.items; public class ModItems { public static net.minecraft.item.Item ' + ','.join(items) + '; }',
    'com/hbm/items/food/ItemConserve.java': 'package com.hbm.items.food; public class ItemConserve {\n' + food_enum + '\n}\n',
}
production = ['com/hbm/blocks/generic/BlockCanCrate.java', 'com/hbm/blocks/generic/BlockSupplyCrate.java', 'com/hbm/main/ModSoundEvents.java', 'com/hbm/lib/RefStrings.java', 'com/hbm/blocks/generic/BlockSteelBeam.java', 'api/hbm/block/IToolable.java', 'com/hbm/world/gen/nbt/INBTBlockTransformable.java']
exe = '.exe' if os.name == 'nt' else ''
with tempfile.TemporaryDirectory(prefix='crate-blocks-native-') as tmp:
    out = Path(tmp)
    files = []
    for rel, content in collaborators.items():
        assert rel.startswith('com/hbm/'), 'No native facades permitted'
        f = out / rel
        f.parent.mkdir(parents=True, exist_ok=True)
        f.write_text(content)
        files.append(str(f))
    for rel in production:
        f = src / rel
        print('PRODUCTION', rel, hashlib.sha256(f.read_bytes()).hexdigest(), flush=True)
        files.append(str(f))
    f = out / 'CrateBlocksNativeTest.java'
    f.write_text((tests / 'crate-blocks-native-test.java.in').read_text())
    files.append(str(f))
    compiled = subprocess.run([str(a.java_home / 'bin' / ('javac' + exe)), '-encoding', 'UTF-8', '-source', '8', '-target', '8', '-cp', a.classpath, '-d', tmp] + files)
    if compiled.returncode:
        sys.exit(compiled.returncode)
    executed = subprocess.run([str(a.java_home / 'bin' / ('java' + exe)), '-Djava.awt.headless=true', '-cp', os.pathsep.join([tmp, a.classpath]), 'CrateBlocksNativeTest'], cwd=tmp)
    sys.exit(executed.returncode)
