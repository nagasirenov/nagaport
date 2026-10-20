#!/usr/bin/env python3
import collections, csv, hashlib, json
from pathlib import Path
HERE = Path(__file__).resolve().parent
rows = json.loads((HERE/'sites.json').read_text())
before = list(csv.DictReader((HERE/'before.tsv').open(), delimiter='\t'))
after = list(csv.DictReader((HERE/'after.tsv').open(), delimiter='\t'))
assert len(before) == len(after) == 71
for row in rows:
    old = [r for r in before if int(r['line']) == row['line']]
    new = [r for r in after if int(r['line']) == row['line']]
    b = next(r for r in old if r['method'] == 'setStepSound')
    a = next(r for r in new if r['method'] == 'withSoundType')
    qualified = 'com.hbm.blocks.generic.' + row['owner']
    assert b['receiver_source'] == row['receiver'] and b['receiver_kind'] == 'DECLARED' and b['receiver_type'] == 'net.minecraft.block.Block'
    assert a['receiver_source'] == '((' + row['owner'] + ') ' + row['receiver'] + ')'
    assert a['receiver_kind'] == 'DECLARED' and a['method_element_kind'] == 'METHOD'
    assert a['receiver_type'] == a['method_owner'] == a['return_type'] == qualified
    assert a['method_signature'] == 'withSoundType(net.minecraft.block.SoundType)'
    assert a['arguments_source'] == b['arguments_source'] == row['sound']
    assert a['arguments_type'] == b['arguments_type'] == ('net.minecraft.block.SoundType' if row['sound'] == 'SoundType.WOOD' else 'com.hbm.blocks.ModSoundType')
    assert a['arguments_kind'] == b['arguments_kind'] == 'DECLARED'
    for method in ['<init>', 'setTranslationKey']:
        x = next(r for r in old if r['method'] == method)
        y = next(r for r in new if r['method'] == method)
        assert all(x[k] == y[k] for k in x if k not in ['start', 'end'])
        if method == '<init>':
            assert x['receiver_source'] == 'NAMED' and x['method_element_kind'] == 'CONSTRUCTOR'
            assert x['method_owner'] == x['receiver_type'] == qualified
        else:
            assert x['method_element_kind'] == 'METHOD'
            assert x['method_owner'] == x['return_type'] == 'net.minecraft.block.Block'
    for method in ['setHardness', 'setResistance', 'setCreativeTab']:
        x = next(r for r in old if r['method'] == method)
        y = next(r for r in new if r['method'] == method)
        assert y['method_element_kind'] == 'METHOD' and y['receiver_kind'] == 'DECLARED'
        assert y['method_owner'] == y['return_type'] == 'net.minecraft.block.Block'
        assert x['arguments_source'] == y['arguments_source'] and x['arguments_type'] == y['arguments_type']
    if row['owner'] == 'BlockCrate':
        x = next(r for r in old if r['method'] == 'setBlockTextureName')
        y = next(r for r in new if r['method'] == 'setBlockTextureName')
        assert x['receiver_kind'] == 'ERROR'
        assert y['receiver_kind'] == 'DECLARED' and y['receiver_type'] == 'net.minecraft.block.Block'
        assert y['method_element_kind'] != 'METHOD', 'Texture migration is explicitly not claimed'

def errors(name):
    return collections.Counter('\t'.join(x.split('\t')[:1] + x.split('\t')[2:]) for x in (HERE/(name+'-errors.tsv')).read_text().splitlines())
b, a = errors('before'), errors('after')
removed, added = b-a, a-b
assert sum(b.values()) == 11787 and sum(a.values()) == 11781
assert sum(removed.values()) == 11 and sum(added.values()) == 5
assert all(x.startswith('src/main/java/com/hbm/blocks/ModBlocks.java\t') and 'setStepSound(' in x for x in removed)
assert all(x.startswith('src/main/java/com/hbm/blocks/ModBlocks.java\t') and 'setBlockTextureName(java.lang.String)' in x for x in added)
for stage in ['before', 'after']:
    log = (HERE/(stage+'.log')).read_text()
    assert 'Parsed 4066 units; parse errors=0' in log and 'Selected invocation count=71' in log
print('PASS complete all-source parse/attribution, 4066 units each, zero syntax errors; 11 exact owner casts and native method bindings; eleven sound failures removed, five pre-existing texture failures exposed; 11787 -> 11781 diagnostics, no other message-multiset changes. No class generation/full build.')
