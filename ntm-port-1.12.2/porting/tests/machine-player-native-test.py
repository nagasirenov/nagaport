#!/usr/bin/env python3
"""Compile verbatim extracted player callbacks against real Minecraft 1.12.2 APIs.
The complete inventory base and sound registry are compiled, not complete machines.
World/player allocation and proxy core lookup are controlled fixtures, never API stubs.
Uses only an explicitly supplied existing JDK/classpath; never invokes Gradle or Git.
"""
import argparse
import importlib.util
import os
from pathlib import Path
import re
import subprocess
import tempfile

ROOT = Path(__file__).resolve().parents[2]
SRC = ROOT / 'src/main/java'


def source(owner):
    matches = list(SRC.rglob(owner + '.java'))
    assert len(matches) == 1, owner
    return matches[0].read_bytes().decode('utf-8')


def method(text, name):
    matches = list(re.finditer(r'public\s+(?:boolean|void)\s+' + name + r'\s*\(\s*EntityPlayer\s+\w+\s*\)\s*\{', text))
    assert len(matches) == 1, 'Expected exactly one native callback: ' + name
    start = matches[0].start()
    end = matches[0].end()
    depth = 1
    while depth:
        depth += (text[end] == '{') - (text[end] == '}')
        end += 1
    return text[start:end]


parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('--java-home', required=True, type=Path)
parser.add_argument('--classpath', required=True)
args = parser.parse_args()
spec = importlib.util.spec_from_file_location('player_source_check', ROOT / 'porting/tests/check-machine-player-source.py')
source_check = importlib.util.module_from_spec(spec)
spec.loader.exec_module(source_check)
source_check.main()
configs = [
    ('Machine', 'TileEntityMachineBase', ['isUsableByPlayer', 'openInventory', 'closeInventory']),
    ('ICF', 'TileEntityICF', ['isUsableByPlayer']),
    ('Fusion', 'TileEntityFusionTorus', ['isUsableByPlayer']),
    ('Launch', 'TileEntityLaunchPadRocket', ['isUsableByPlayer']),
    ('Radar', 'TileEntityMachineRadarNT', ['isUsableByPlayer']),
    ('Rocket', 'TileEntityMachineRocketAssembly', ['isUsableByPlayer']),
    ('Stardar', 'TileEntityMachineStardar', ['isUsableByPlayer']),
    ('Crate', 'TileEntityCrateBase', ['isUsableByPlayer']),
    ('Ashpit', 'TileEntityAshpit', ['openInventory', 'closeInventory']),
    ('Firebox', 'TileEntityFireboxBase', ['openInventory', 'closeInventory']),
    ('Engine', 'TileEntityMachineCombustionEngine', ['openInventory', 'closeInventory']),
    ('Turret', 'TileEntityTurretBaseNT', ['openInventory', 'closeInventory']),
    ('Combo', 'TileEntityProxyCombo', ['isUsableByPlayer', 'openInventory', 'closeInventory']),
    ('Proxy', 'TileEntityProxyInventory', ['isUsableByPlayer', 'openInventory', 'closeInventory']),
    ('PWR', 'BlockPWR', ['isUsableByPlayer', 'openInventory', 'closeInventory']),
]
fixtures = []
for short, owner, methods in configs:
    callbacks = '\n'.join('@Override\n' + method(source(owner), n) for n in methods)
    extra = 'boolean inventory; Object core; Object getCoreObject(){return core;}' if short == 'Combo' else ''
    fixtures.append('static class %s extends FixtureInventory { %s\n%s\n}' % (short, extra, callbacks))
# Extract every assessed hasPermission self-call, then exercise virtual dispatch with
# both outcomes. This catches accidental calls to the inherited legacy bridge too.
assessment = (ROOT / 'porting/machine-player-callback-assessment.md').read_text(encoding='utf-8')
section = assessment.split('### 35 descendant self-calls')[1].split('### 101 concrete container calls')[0]
paths = re.findall(r'`(src/main/java/[^`]+\.java)`', section)
assert len(paths) == 35
permission_tests = []
for index, path in enumerate(paths):
    body = method((ROOT / path).read_bytes().decode('utf-8'), 'hasPermission')
    # No override annotation: permission belongs to the real mod interface, unrelated
    # to IInventory; the body itself is preserved byte-for-byte.
    fixtures.append('static class Permission%d extends Machine { %s }' % (index, body))
    permission_tests.append('for(final boolean allowed:new boolean[]{false,true}) { Permission%d t=new Permission%d(){ public boolean isUsableByPlayer(EntityPlayer p){check(p==player,"permission player identity");return allowed;} public boolean isUseableByPlayer(EntityPlayer p){throw new AssertionError("legacy bridge used");} }; check(t.hasPermission(player)==allowed,"permission virtual dispatch %s"); }' % (index, index, path))
template = (ROOT / 'porting/tests/machine-player-native-test.java.in').read_text(encoding='utf-8')
template = template.replace('@FIXTURES@', '\n'.join(fixtures)).replace('@PERMISSION_TESTS@', '\n'.join(permission_tests))
with tempfile.TemporaryDirectory(prefix='hbm-machine-player-native-') as temp:
    test = Path(temp) / 'MachinePlayerNativeTest.java'
    test.write_bytes(template.encode('utf-8'))
    production = ['com/hbm/tileentity/TileEntityInventoryBase.java', 'com/hbm/main/ModSoundEvents.java', 'com/hbm/lib/RefStrings.java']
    exe = '.exe' if os.name == 'nt' else ''
    subprocess.run([str(args.java_home / 'bin' / ('javac' + exe)), '-source', '8', '-target', '8', '-encoding', 'UTF-8', '-cp', args.classpath, '-d', temp] + [str(SRC / p) for p in production] + [str(test)], check=True)
    subprocess.run([str(args.java_home / 'bin' / ('java' + exe)), '-cp', os.pathsep.join([temp, args.classpath]), 'MachinePlayerNativeTest'], cwd=temp, check=True)
print('Scope: verbatim extracted player callbacks and 35 permission bodies, complete production inventory base/sound registry; NOT full machines, client audio, resource reload or gameplay.')
