#!/usr/bin/env python3
"""Exact RecipesCommon whole-source reversal; no local Git/original dependency.
Known paths fail closed on source drift. Unknown paths pass through unchanged.
"""
import hashlib
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
MANIFEST = json.loads(Path(__file__).with_name('recipe-key-source-manifest.json').read_text(encoding='utf-8'))
FILES = {entry['path']: entry for entry in MANIFEST['files']}

def sha(text):
    return hashlib.sha256(text.encode('utf-8')).hexdigest()

def reverse_source(relative, text):
    entry = FILES.get(str(relative).replace('\\', '/'))
    if entry is None:
        return text
    assert sha(text) == entry['after_sha256'], (relative, 'recipe-key native whole-file drift')
    for edit in reversed(entry['edits']):
        pos = edit['after_offset']
        assert text[pos:pos+len(edit['new'])] == edit['new'], (relative, 'recipe-key reverse edit mismatch')
        text = text[:pos] + edit['old'] + text[pos+len(edit['new']):]
    assert sha(text) == entry['before_sha256'], (relative, 'recipe-key baseline whole-file mismatch')
    return text

def main():
    rejected = 0
    for relative, entry in FILES.items():
        current = (ROOT / relative).read_bytes().decode('utf-8')
        baseline = reverse_source(relative, current)
        rebuilt = baseline
        for edit in reversed(entry['edits']):
            pos = edit['before_offset']
            assert rebuilt[pos:pos+len(edit['old'])] == edit['old'], (relative, 'recipe-key forward mismatch')
            rebuilt = rebuilt[:pos] + edit['new'] + rebuilt[pos+len(edit['old']):]
        assert rebuilt == current, (relative, 'recipe-key exact forward roundtrip')
        mutations = [current+'\n', current[:-1], current.replace('this.stacksize = 1;', 'this.stacksize = 2;', 1),
                     current.replace('.toString().hashCode()', '.hashCode()', 1),
                     current.replace('if(getClass() != obj.getClass()) return false;', '', 1),
                     current.replace('this.nbt = nbt;', 'this.nbt = nbt.copy();', 1)]
        for mutation in mutations:
            assert mutation != current, 'ineffective mutation'
            try:
                reverse_source(relative, mutation)
            except AssertionError:
                rejected += 1
            else:
                raise AssertionError((relative, 'recipe-key drift accepted'))
    assert reverse_source('unrelated.java', 'arbitrary') == 'arbitrary'
    print('PASS exact RecipesCommon reverse/forward source reconstruction; %d drift mutations rejected; no Git dependency.' % rejected, flush=True)

if __name__ == '__main__':
    main()
