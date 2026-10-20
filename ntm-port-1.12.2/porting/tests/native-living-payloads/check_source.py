#!/usr/bin/env python3
import hashlib, json
from pathlib import Path

HERE = Path(__file__).resolve().parent
ROOT = HERE.parents[2]
manifest = json.loads((HERE / 'source-manifest.json').read_text())
accesses = 0
for file in manifest['files']:
    raw = (ROOT / file['path']).read_bytes()
    assert hashlib.sha256(raw).hexdigest() == file['after'], file['path']
    data = raw.decode().encode('utf-16-le')
    for e in file['edits']:
        assert data[e['after_start']*2:e['after_end']*2].decode('utf-16-le') == e['new']
        if e['kind'] != 'CLOSE': accesses += 1
    for e in reversed(file['edits']):
        data = data[:e['after_start']*2] + e['old'].encode('utf-16-le') + data[e['after_end']*2:]
    assert hashlib.sha256(data.decode('utf-16-le').encode()).hexdigest() == file['before'], file['path']
assert accesses == manifest['accesses'] == 52
print('PASS 52 exact source inverses across 14 files; all other bytes, branches, formulas and cancellation calls retained')
