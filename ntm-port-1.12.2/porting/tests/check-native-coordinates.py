#!/usr/bin/env python3
"""Exact bounded coordinate reversal. No git/pristine checkout required."""
import hashlib
import json
from pathlib import Path
ROOT = Path(__file__).resolve().parents[2]
MANIFEST = json.loads((ROOT / 'porting/tests/native-coordinate-manifest.json').read_text())
FILES = {entry['path']: entry for entry in MANIFEST['files']}

def reverse_source(rel, text):
    entry = FILES.get(rel)
    if entry is None:
        return text
    for edit in reversed(entry['edits']):
        assert len(edit['positions']) == edit['count'], (rel, 'coordinate replacement count')
        for offset in reversed(edit['positions']):
            assert text[offset:offset + len(edit['new'])] == edit['new'], (rel, 'coordinate replacement mismatch', offset)
            text = text[:offset] + edit['old'] + text[offset + len(edit['new']):]
    assert hashlib.sha256(text.encode('utf-8')).hexdigest() == entry['baseline_sha256'], (rel, 'coordinate baseline drift')
    return text

def main(transform=None):
    for rel in FILES:
        text = (ROOT / rel).read_bytes().decode('utf-8')
        if transform is not None: text = transform(rel, text)
        reverse_source(rel, text)
    remaining = [str(p.relative_to(ROOT)) for p in (ROOT / 'src/main/java').rglob('*.java')
                 if 'import net.minecraft.util.ChunkCoordinates;' in p.read_text()]
    assert not remaining, remaining
    print('PASS: eight exact coordinate consumer reversals to checkpoint9; no remaining ChunkCoordinates imports. Not full-class compilation/gameplay.')
if __name__ == '__main__':
    main()
