#!/usr/bin/env python3
"""Exact byte-preserving finite migration, including the pre-existing CRLF bytes."""
import hashlib
import json
import zipfile
from pathlib import Path

HERE = Path(__file__).resolve().parent
ROOT = HERE.parents[2]


def digest(data):
    return hashlib.sha256(data).hexdigest()


def original_bytes(path, actual):
    """Permit the earlier proof to invert only this independently pinned migration."""
    manifest = json.loads((HERE / 'manifest.json').read_text())
    record = manifest['files'][path]
    assert digest(actual) == record['after_sha256'], path
    with zipfile.ZipFile(HERE / 'baseline.zip') as archive:
        original = archive.read(path)
    assert digest(original) == record['before_sha256'], path
    generated = original.decode()
    for edit in reversed(record['edits']):
        assert generated[edit['start']:edit['end']] == edit['old'], path
        generated = generated[:edit['start']] + edit['new'] + generated[edit['end']:]
    assert generated.encode() == actual, path
    # Inverse in forward edit order restores each next original character offset.
    for edit in record['edits']:
        assert generated[edit['start']:edit['start'] + len(edit['new'])] == edit['new'], path
        generated = generated[:edit['start']] + edit['old'] + generated[edit['start'] + len(edit['new']):]
    assert generated.encode() == original, path
    return original


def verify(root=ROOT):
    manifest = json.loads((HERE / 'manifest.json').read_text())
    assert len(manifest['files']) == 4
    assert sum(len(r['edits']) for r in manifest['files'].values()) == 7
    for path in manifest['files']:
        original_bytes(path, (root / path).read_bytes())
    texts = '\n'.join(p.read_text() for p in (root / 'src/main/java').rglob('*.java'))
    assert '.getPlayerManager().markBlockForUpdate(' not in texts, 'unmigrated direct server update'
    print('PASS: seven edits, four complete-file regeneration/inverse/hash checks; no old direct server-update chain remains')
    return manifest


if __name__ == '__main__':
    verify()
