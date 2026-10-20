#!/usr/bin/env python3
"""Exact CP85 inverse and bounded/composable token inverse, never a baseline refresh."""
import collections
import hashlib
import json
import pathlib

HOME = pathlib.Path(__file__).resolve().parent
ROOT = HOME.parents[2]
DATA = json.loads((HOME / 'source-manifest.json').read_text())
sha = lambda text: hashlib.sha256(text.encode()).hexdigest()

def reverse_source(relative, text, strict=True):
    """Use strict=False only inside a separately hash-checked multi-batch inverse."""
    row = DATA['files'].get(relative)
    if row is None:
        return text
    if strict:
        assert sha(text) == row['after_sha256'], 'post-source drift: ' + relative
    replacements = collections.Counter((e['new'], e['old']) for e in row['edits'])
    for (new, old), count in replacements.items():
        assert text.count(new) == count, 'changed/missing reviewed token: ' + relative + ': ' + new
        text = text.replace(new, old)
    if strict:
        assert sha(text) == row['before_sha256'], 'CP85 inverse mismatch: ' + relative
    return text

def main():
    negatives = 0
    assert DATA['indexed_reads'] == 83 and DATA['length_reads'] == 19
    assert DATA['alias_reads'] == 8 and DATA['alias_declarations'] == 1
    assert len(DATA['excluded_writes']) == 21 and len(DATA['files']) == 31
    for relative, row in DATA['files'].items():
        text = (ROOT / relative).read_bytes().decode()
        old = reverse_source(relative, text)
        for damaged in (text + ' ', text.replace(row['edits'][0]['new'], 'changed', 1)):
            try:
                reverse_source(relative, damaged)
            except AssertionError:
                negatives += 1
            else:
                raise AssertionError('source drift accepted')
        for write in DATA['excluded_writes']:
            if write['file'] == relative:
                assert old.count(write['old']) >= 1
    helper = (ROOT / DATA['helper']['file']).read_text()
    assert sha(helper) == DATA['helper']['sha256'], 'owned helper source drift'
    assert helper.count('inventory.get(slot)') == 1
    assert 'return stack == ItemStack.EMPTY ? null : stack;' in helper
    assert '.isEmpty(' not in helper and '.copy(' not in helper
    print('PASS 31 exact CP85 source inverses; 83 indexed reads, 19 sizes, 8 Faraday alias reads; 21 writes excluded; %d drift negatives' % negatives)

if __name__ == '__main__':
    main()
