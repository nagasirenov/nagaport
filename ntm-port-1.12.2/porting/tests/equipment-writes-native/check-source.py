#!/usr/bin/env python3
"""Frozen CP86 inverse for the finite concrete-mob write family."""
from pathlib import Path
import collections
import hashlib
import json
HOME = Path(__file__).resolve().parent
ROOT = HOME.parents[2]
DATA = json.loads((HOME / 'source-manifest.json').read_text())
sha = lambda text: hashlib.sha256(text.encode()).hexdigest()
def reverse_source(relative, text, strict=True):
    row = DATA['files'].get(relative)
    if row is None:
        return text
    if strict:
        assert sha(text) == row['after_sha256'], 'post-source drift: ' + relative
    counts = collections.Counter((e['new'], e['old']) for e in row['edits'])
    for (new, old), count in counts.items():
        assert text.count(new) == count, 'changed or missing selected call: ' + relative
        text = text.replace(new, old)
    if strict:
        assert sha(text) == row['before_sha256'], 'CP86 inverse mismatch: ' + relative
    return text

def main():
    assert DATA['setter_count'] == 24 and DATA['drop_count'] == 3
    assert len(DATA['files']) == 5 and len(DATA['excluded']) == 7
    negatives = 0
    for relative, row in DATA['files'].items():
        text = (ROOT / relative).read_bytes().decode()
        reverse_source(relative, text)
        for edit in row['edits']:
            assert edit['rhs'] in edit['old'] and edit['rhs'] in edit['new']
            broken = text.replace(edit['new'], edit['new'].replace('EntityEquipmentSlot.', 'EntityEquipmentSlot.OFFHAND /* drift */ '), 1)
            try:
                reverse_source(relative, broken)
            except AssertionError:
                negatives += 1
            else:
                raise AssertionError('selected slot drift accepted')
        for broken in (text + ' ', text.replace(row['edits'][0]['new'], 'changed', 1)):
            try:
                reverse_source(relative, broken)
            except AssertionError:
                negatives += 1
            else:
                raise AssertionError('whole source drift accepted')
    for edit in DATA['excluded']:
        assert edit['old'] in (ROOT / edit['path']).read_bytes().decode(), 'deferred expression changed'
    print('PASS 5 exact CP86 source inverses; 24 concrete mob setters + 3 MAINHAND drops; all RHS unchanged; 7 generic/dynamic setters retained; %d drift negatives' % negatives)
if __name__ == '__main__':
    main()
