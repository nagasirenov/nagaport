#!/usr/bin/env python3
"""Exact NTM constructor/builder slices; every Minecraft/Forge dependency is native."""
import argparse, hashlib, json, re, subprocess, sys, tempfile
from pathlib import Path
HERE = Path(__file__).resolve().parent
ROOT = HERE.parents[2]
p = argparse.ArgumentParser()
p.add_argument('--composed-source', action='store_true', help='Require the pinned CP88 aggregate and exact actual-current three-file crate reconstruction')
p.add_argument('--java-home', type=Path, required=True)
p.add_argument('--classpath-file', type=Path, required=True)
p.add_argument('--legacy-jar', type=Path, required=True)
p.add_argument('--mutant', choices=['drop-assignment', 'wrong-argument', 'null-return'])
p.add_argument('--mutant-owner', choices=['BlockCrate', 'BlockStorageCrate'])
a = p.parse_args()
assert not sys.flags.optimize
assert bool(a.mutant) == bool(a.mutant_owner)
subprocess.run([sys.executable, str(HERE/'check-source.py')] + (['--composed-source'] if a.composed_source else []), check=True)
cp = a.classpath_file.read_text().strip()
assert hashlib.sha256(a.legacy_jar.read_bytes()).hexdigest() == 'a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce'

def method(source, pattern):
    match = re.search(pattern, source)
    assert match, pattern
    end, depth = match.end(), 1
    while depth:
        if source[end] == '{': depth += 1
        if source[end] == '}': depth -= 1
        end += 1
    return source[match.start():end]

rows = json.loads((HERE / 'sites.json').read_text())
with tempfile.TemporaryDirectory(prefix='crate-sound-native-') as d:
    t, files = Path(d), []
    def write(name, source):
        path = t / name
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(source)
        files.append(path)
    for name, parent in [('BlockCrate', 'BlockFalling'), ('BlockStorageCrate', 'BlockContainer')]:
        source = (ROOT / ('src/main/java/com/hbm/blocks/generic/' + name + '.java')).read_text()
        constructor = method(source, r'public ' + name + r'\(Material p_i45394_1_\)\s*\{')
        builder = method(source, r'public ' + name + r' withSoundType\([^)]*\)\s*\{')
        if a.mutant_owner == name:
            if a.mutant == 'drop-assignment': builder = builder.replace('super.setSoundType(sound);', ';')
            if a.mutant == 'wrong-argument': builder = builder.replace('super.setSoundType(sound);', 'super.setSoundType(net.minecraft.block.SoundType.STONE);')
            if a.mutant == 'null-return': builder = builder.replace('return this;', 'return null;')
        # Explicitly out-of-scope NTM callback, fail if any test crosses this boundary.
        callback = '' if name == 'BlockCrate' else 'public net.minecraft.tileentity.TileEntity createNewTileEntity(net.minecraft.world.World w,int m){throw new AssertionError("Out-of-scope crate tile callback");}'
        write('com/hbm/blocks/generic/' + name + '.java', 'package com.hbm.blocks.generic; import net.minecraft.block.*; import net.minecraft.block.material.Material; public class ' + name + ' extends ' + parent + ' {' + constructor + builder + callback + '}')
    write('com/hbm/main/MainRegistry.java', 'package com.hbm.main; public final class MainRegistry { public static final MainRegistry instance=new MainRegistry(); public final java.util.Random rand=new java.util.Random(1); public static final net.minecraft.creativetab.CreativeTabs machineTab=net.minecraft.creativetab.CreativeTabs.REDSTONE, consumableTab=net.minecraft.creativetab.CreativeTabs.MISC; }')
    production = (ROOT / 'src/main/java/com/hbm/blocks/ModBlocks.java').read_text()
    slices = []
    for row in rows:
        assert production.count(row['after']) == 1
        text = next(line for line in production.splitlines() if line == row['after']).strip()
        if '.setBlockTextureName(' in text:
            text = text[:text.index('.setBlockTextureName(')] + ';'
        slices.append(text)
    names = [r['name'] for r in rows]
    def values(expression): return ','.join(expression(r) for r in rows)
    generated = 'import net.minecraft.block.*; import net.minecraft.block.material.Material; import net.minecraft.creativetab.CreativeTabs; import com.hbm.blocks.*; import com.hbm.blocks.generic.*; import com.hbm.main.MainRegistry; public class CrateConstruction { static Block ' + ','.join(names) + '; static Block[] create(){' + '\n'.join(slices) + 'return new Block[]{' + ','.join(names) + '};}'
    generated += 'static SoundType[] expected={' + values(lambda r:r['sound']) + '};'
    generated += 'static String[] names={' + values(lambda r:'"' + r['name'] + '"') + '};'
    generated += 'static Material[] materials={' + values(lambda r:'Material.' + r['material']) + '};'
    generated += 'static float[] hardness={' + values(lambda r:re.search(r'\.setHardness\(([^)]+)\)', r['before']).group(1)) + '};'
    generated += 'static float[] resistance={' + values(lambda r:re.search(r'\.setResistance\(([^)]+)\)', r['before']).group(1)) + '};'
    generated += 'static CreativeTabs[] tabs={' + values(lambda r:re.search(r'\.setCreativeTab\(([^)]+)\)', r['before']).group(1)) + '};}'
    write('CrateConstruction.java', generated)
    files += [ROOT / 'src/main/java' / f for f in ['com/hbm/blocks/ModSoundTypes.java', 'com/hbm/blocks/ModSoundType.java', 'com/hbm/main/ModSoundEvents.java', 'com/hbm/lib/RefStrings.java']]
    files += [HERE / 'CrateSoundNativeTest.java', HERE.parent / 'ForgeEventNativeLauncher.java', HERE.parent / 'LegacyBlockSoundOracle.java']
    subprocess.run([str(a.java_home / 'bin/javac'), '-encoding', 'UTF-8', '-cp', cp, '-sourcepath', str(t/'empty'), '-d', d] + list(map(str, files)), check=True)
    print('COMPILE_PASS: exact native constructor/builder slice fixtures', flush=True)
    subprocess.run([str(a.java_home / 'bin/java'), '-cp', d + ':' + cp, 'LegacyBlockSoundOracle', str(a.legacy_jar)], cwd=d, check=True)
    subprocess.run([str(a.java_home / 'bin/java'), '-Djava.awt.headless=true', '-cp', d + ':' + cp, 'ForgeEventNativeLauncher', 'CrateSoundNativeTest'], cwd=d, check=True)
