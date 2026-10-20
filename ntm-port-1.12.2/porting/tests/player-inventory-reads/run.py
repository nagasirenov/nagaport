#!/usr/bin/env python3
"""Bounded native helper + all 110 migrated read-expression probes, not a mod build."""
import argparse
import hashlib
import importlib.util
import json
import os
import collections
from pathlib import Path
import subprocess
import tempfile

HOME = Path(__file__).resolve().parent
ROOT = HOME.parents[2]
parser = argparse.ArgumentParser()
parser.add_argument('--composed-source',action='store_true',help='Require pinned CP86 combined whole-source guard before native probes')
parser.add_argument('--java-home', type=Path, required=True)
parser.add_argument('--classpath-file', type=Path, required=True)
a = parser.parse_args()
cp = a.classpath_file.read_text().strip()
exe = '.exe' if os.name == 'nt' else ''
guard = HOME.parent / 'check-native-player-inventory-range-source.py' if a.composed_source else HOME / 'check-source.py'
subprocess.run([__import__('sys').executable, str(guard)], check=True)
data = json.loads((HOME / 'source-manifest.json').read_text())
rows = [e for row in data['files'].values() for e in row['edits'] if e['kind'] != 'alias_declaration']
assert len(rows) == 110
native_sig = 'EntityPlayer player, EntityPlayer p, EntityPlayer pl, EntityPlayer e, InventoryPlayer inventory, NonNullList<ItemStack> armor, int i, int j, int armorSlot'
old_sig = 'LegacyPlayer player, LegacyPlayer p, LegacyPlayer pl, LegacyPlayer e, LegacyInventory inventory, ItemStack[] armor, int i, int j, int armorSlot'
code = ['import net.minecraft.entity.player.*;', 'import net.minecraft.item.ItemStack;', 'import net.minecraft.util.NonNullList;', 'final class ProductionExpressions {',
        'static final int COUNT = %d;' % len(rows),
        'static final class LegacyInventory { ItemStack[] mainInventory = new ItemStack[36]; ItemStack[] armorInventory = new ItemStack[4]; int currentItem; }',
        'static final class LegacyPlayer { LegacyInventory inventory = new LegacyInventory(); }']
for native, signature, field in [(True, native_sig, 'new'), (False, old_sig, 'old')]:
    code.append('static Object %s(int which, %s) { switch (which) {' % ('current' if native else 'original', signature))
    for index, row in enumerate(rows):
        expression = row[field] if native else row[field].replace('(EntityPlayer)', '(LegacyPlayer)')
        code.append('case %d: return %s;' % (index, expression))
    code.extend(['default: throw new AssertionError(which);', '}}'])
# Complete production Faraday body, with only the callee predicate instrumented.
src = (ROOT / 'src/main/java/com/hbm/util/ArmorUtil.java').read_text()
start = src.index('\tpublic static boolean checkForFaraday(')
end = src.index('\n\tpublic static final String[] metals', start)
code.append(src[start:end])
code.extend(['static int predicateCalls, rejectAt = -1;', 'static final java.util.List<ItemStack> seen = new java.util.ArrayList<ItemStack>();',
             'static EntityPlayer replaceOwner; static InventoryPlayer replacement;',
             'static boolean isFaradayArmor(ItemStack s) { seen.add(s); int at = predicateCalls++; if (at == 0 && replacement != null) replaceOwner.inventory = replacement; return at != rejectAt; }', '}'])
# Ensure every extracted after-token occurs in its production file and preserve full
# per-file source identity through check-source.py before generation.
with tempfile.TemporaryDirectory(prefix='hbm-player-inventory-reads-') as temp:
    out = Path(temp)
    (out / 'ProductionExpressions.java').write_text('\n'.join(code) + '\n')
    baseline = ['import net.minecraft.entity.player.*;', 'import net.minecraft.item.ItemStack;', 'final class BaselineExpressions {',
                'static Object read(int which, ' + native_sig.replace('NonNullList<ItemStack> armor', 'ItemStack[] armor') + ') { switch (which) {']
    selected = [row for row in rows if row['kind'] in ('indexed', 'length')]
    baseline.extend('case %d: return %s;' % (i, row['old']) for i, row in enumerate(selected))
    baseline.extend(['default: throw new AssertionError(which);', '}}', '}'])
    (out / 'BaselineExpressions.java').write_text('\n'.join(baseline) + '\n')
    before = subprocess.run([str(a.java_home / 'bin' / ('javac' + exe)), '-encoding', 'UTF-8', '-Xmaxerrs', '1000', '-cp', cp, '-d', str(out), str(out / 'BaselineExpressions.java')], capture_output=True, text=True)
    assert before.returncode != 0, 'baseline expressions unexpectedly compiled'
    failures = collections.Counter(line.split('error: ', 1)[1] for line in before.stderr.splitlines() if 'error: ' in line)
    assert failures == {'array required, but NonNullList<ItemStack> found': 83, 'cannot find symbol': 19}, failures
    (HOME / 'baseline-expression-diagnostics.log').write_text(before.stderr.replace(str(out), '<probe>'))
    print('PASS bounded baseline diagnostic reproduction: 83 array reads + 19 length reads fail against real native types')
    command = [str(a.java_home / 'bin' / ('javac' + exe)), '-encoding', 'UTF-8', '-source', '8', '-target', '8', '-cp', cp, '-d', str(out),
               str(ROOT / 'src/main/java/com/hbm/util/PlayerInventoryRead.java'), str(out / 'ProductionExpressions.java'), str(HOME / 'NativePlayerInventoryReadTest.java')]
    subprocess.run(command, check=True)
    subprocess.run([str(a.java_home / 'bin' / ('java' + exe)), '-cp', os.pathsep.join([str(out), cp]), 'NativePlayerInventoryReadTest'], cwd=out, check=True)
    javap = subprocess.run([str(a.java_home / 'bin' / ('javap' + exe)), '-classpath', cp, '-c', 'net.minecraft.entity.player.InventoryPlayer', 'net.minecraft.util.NonNullList'], check=True, capture_output=True, text=True)
    (HOME / 'native-inventory-contract.javap.txt').write_text(javap.stdout)
print('PASS native helper compiled with exact 110 migrated expressions and complete Faraday body. No full production-class/client/server claim.')
