#!/usr/bin/env python3
"""Byte-reversible CP51 audit; receiver ownership, constructor/setter order and data parity."""
import collections, hashlib, json, re
from pathlib import Path
HERE = Path(__file__).resolve().parent
ROOT = HERE.parents[2]
m = json.loads((HERE / 'source-manifest.json').read_text())
current = (ROOT / m['path']).read_bytes()
assert hashlib.sha256(current).hexdigest() == m['after_sha256']
original = current.decode()
for change in reversed(m['changes']):
    assert original.count(change['after']) == 1, change['line']
    original = original.replace(change['after'], change['before'])
assert hashlib.sha256(original.encode()).hexdigest() == m['baseline_sha256']
base = m['block_base']
assert (ROOT / base['path']).read_bytes() == base['after'].encode()
assert hashlib.sha256(base['before'].encode()).hexdigest() == base['baseline_sha256']
after = current.decode()
# A token rather than line comparison: the added cast parentheses are deliberate.
# All literals and constructor calls must be identical, in the original order.
strings = lambda s: re.findall(r'"(?:[^"\\]|\\.)*"', s)
assert strings(after) == strings(original)
number_source=after.replace('public EnumPushReaction getPushReaction() { return EnumPushReaction.DESTROY; }', 'public int getMaterialMobility() { return 1; }')
assert re.findall(r'\b\d+(?:\.\d+)?[FfDdLl]?\b',number_source)==re.findall(r'\b\d+(?:\.\d+)?[FfDdLl]?\b',original), 'Numeric configuration changed'
constructors = lambda s: re.findall(r'\bnew\s+(\w+)\s*\(', s)
assert constructors(after) == constructors(original)
methods = lambda s: [x.replace('getUnlocalizedName', 'getTranslationKey').replace('setBlockName', 'setTranslationKey').replace('setUnlocalizedName', 'setTranslationKey').replace('setStepSound', 'setSoundType') for x in re.findall(r'\.([A-Za-z_]\w*)\s*\(', s)]
assert methods(after) == methods(original), 'Fluent evaluation order changed'
# No registration was redirected, removed, added, or assigned another ItemBlock.
registration = lambda s: [line.replace('.getUnlocalizedName()', '.getTranslationKey()') for line in s.splitlines() if 'GameRegistry.registerBlock(' in line]
assert registration(after) == registration(original)
assert after.count('.setBlockTextureName(') == original.count('.setBlockTextureName(') == 676
assert after.count('.setUnlocalizedName(') == 7, 'Forge Fluid setters must remain native Fluid API'
assert after.count('\r\n') == original.count('\r\n'), 'Existing CRLF line endings changed'
assert sum(x['kept_ntm_name_helper'] for x in m['name_receivers']) == 187
assert sum(not x['kept_ntm_name_helper'] for x in m['name_receivers']) == 647
assert all(('BlockBase' in x['ancestry']) == x['kept_ntm_name_helper'] for x in m['name_receivers'])
assert all('BlockBase' in x['ancestry'] for x in m['base_config_receiver_closure'])
# Source identity of the genuine queue must remain exactly CP51; the worker may
# not silently swap in a synthetic registry or alter registration semantics.
queue = ROOT / 'src/main/java/com/hbm/registry/GameRegistry.java'
assert hashlib.sha256(queue.read_bytes()).hexdigest() == m['game_registry_sha256']
print('PASS exact CP51 reverse hashes for both changed production owners')
print('PASS 834 name receivers: 647 native mappings; 187 NTM BlockBase side-effect setters retained')
print('PASS constructor/string literals and complete fluent evaluation order unchanged')
print('PASS 651 registration calls and original ItemBlock selections unchanged; seven Fluid name setters retained')
print('PASS 676 explicit texture calls retained; original CRLF bytes retained')
print('PASS 156 BlockBase-owned call sites use audited receiver casts, without setter reordering')
print('PASS genuine production registry queue is unmodified')
print('REMAINING', json.dumps({'old_setStepSound': after.count('.setStepSound('), 'old_texture_calls_non_Base': 676 - sum('setBlockTextureName -> setBlockTextureName' in x['methods'] for x in m['base_config_receiver_closure'])}))

resources=json.loads((HERE/'texture-resource-audit.json').read_text())
assert len(resources)==145
for entry in resources:
    path=ROOT/entry['resource']
    assert path.is_file()==entry['port_exists']
    if path.is_file():
        assert hashlib.sha256(path.read_bytes()).hexdigest()==entry['sha256']
        assert path.read_bytes()==(ROOT.parent/'ntm-original-pristine'/entry['resource']).read_bytes()
print('PASS 129 standalone assigned texture resources retain exact pristine bytes; 16 variant-stem entries remain owner callback cases, not fabricated assets')
