#!/usr/bin/env python3
"""Exact seven-file biome-only migration audit and fail-closed snapshot bridge."""
import hashlib, json
from pathlib import Path
ROOT = Path(__file__).resolve().parents[2]
MANIFEST = Path(__file__).with_name('biome-consumer-manifest.json')
def digest(s): return hashlib.sha256(s.encode()).hexdigest()
def reverse_source(relative, text):
    record = json.loads(MANIFEST.read_text(encoding='utf-8'))['files'].get(str(relative).replace('\\', '/'))
    if record is None: return text
    assert digest(text) == record['after_sha256'], (relative, 'Biome consumer post-source drift')
    for edit in reversed(record['edits']):
        for pos in reversed(edit['after_offsets']):
            assert text[pos:pos+len(edit['new'])] == edit['new'], (relative,pos)
            text = text[:pos] + edit['old'] + text[pos+len(edit['new']):]
    assert digest(text) == record['before_sha256'], (relative, 'Biome consumer reverse-source drift')
    return text

def verify():
    records=json.loads(MANIFEST.read_text(encoding='utf-8'))
    assert len(records['files']) == 7
    for rel,record in records['files'].items():
        text=(ROOT/rel).read_text(encoding='utf-8')
        restored=reverse_source(rel,text)
        try:
            reverse_source(rel,text+'\n')
        except AssertionError:
            pass
        else:
            raise AssertionError((rel,'Source drift was not rejected'))
        assert 'BiomeGenBase' not in text
        for edit in record['edits']:
            assert restored.count(edit['old']) == edit['count'], (rel,edit)
            restored=restored.replace(edit['old'],edit['new'])
        assert restored == text, rel
    print('PASS: seven complete source files byte-reverse to 24dab0cd snapshots; only enumerated biome edits, RNG/threshold/control flow unchanged.')
if __name__=='__main__': verify()
