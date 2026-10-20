#!/usr/bin/env python3
"""Fail-closed audit-only reversal of the bounded native rocket metadata migration.
No production rewrites, API shims, git dependency, or historical audit hash updates.
"""
import hashlib
import json
from pathlib import Path
ROOT = Path(__file__).resolve().parents[2]
MANIFEST = json.loads((ROOT / 'porting/tests/rocket-metadata-source-manifest.json').read_text(encoding='utf-8'))
FILES = {entry['path']: entry for entry in MANIFEST['files']}

def reverse_source(rel, text):
    entry = FILES.get(Path(rel).as_posix())
    if entry is None:
        return text
    # read_text normalizes Windows CRLF, as do the baseline snapshots for this audit.
    text = text.replace('\r\n', '\n')
    for edit in reversed(entry['edits']):
        offset = edit['offset']
        assert text[offset:offset + len(edit['new'])] == edit['new'], (rel, 'rocket metadata edit drift', offset)
        text = text[:offset] + edit['old'] + text[offset + len(edit['new']):]
    assert hashlib.sha256(text.encode('utf-8')).hexdigest() == entry['baseline_sha256'], (rel, 'rocket metadata whole-source baseline drift')
    return text

def main():
    for rel in FILES:
        reverse_source(rel, (ROOT / rel).read_text(encoding='utf-8'))
    print('PASS: exact complete RocketStruct and EntityRideableRocket/dummy reversal to checkpoint10/11; source-only, not whole entity compilation.')

if __name__ == '__main__':
    main()
