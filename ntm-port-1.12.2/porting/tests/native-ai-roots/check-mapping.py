#!/usr/bin/env python3
"""Exact finite owner substitutions; no obsolete source is compiled."""
from pathlib import Path
import argparse, hashlib, json
HERE = Path(__file__).resolve().parent
MANIFEST = json.loads((HERE / 'source-manifest.json').read_text(encoding='utf-8'))
def digest(data): return hashlib.sha256(data).hexdigest()
def reverse_source(rel, text):
    entry = MANIFEST['existing'].get(Path(rel).as_posix())
    if entry is None: return text
    assert digest(text.encode('utf-8')) == entry['after_sha256'], 'Changed frozen AI owner: ' + rel
    old = text
    for before, after in reversed(entry['replacements']):
        old = old.replace(after, before)
    assert digest(old.encode('utf-8')) == entry['before_sha256'], 'Unexpected AI owner delta: ' + rel
    forward = old
    for before, after in entry['replacements']: forward = forward.replace(before, after)
    assert forward == text, 'Incomplete AI owner mapping: ' + rel
    return old

def check(module):
    rejected = 0
    for rel, entry in MANIFEST['existing'].items():
        text=(module / rel).read_bytes().decode('utf-8')
        reverse_source(rel, text)
        for mutation in [text+'\n', text.replace('package ', 'package /* drift */ ',1), text.replace('0.5D','0.6D',1) if '0.5D' in text else text.replace('1D','2D',1)]:
            assert mutation != text
            try: reverse_source(rel, mutation)
            except AssertionError: rejected += 1
            else: raise AssertionError('AI drift was accepted: '+rel)
    for rel, expected in MANIFEST['new'].items():
        assert digest((module / rel).read_bytes()) == expected, 'Changed frozen native AI implementation: '+rel
    print('PASS: three exact original-owner mappings, two complete native implementation hashes; %d drift mutations rejected.'%rejected,flush=True)
if __name__=='__main__':
    p=argparse.ArgumentParser();p.add_argument('--module',type=Path,default=HERE.parents[2]);a=p.parse_args();check(a.module)
