#!/usr/bin/env python3
"""Verify bounded native resource-name normalization without changing asset bytes."""
import hashlib
import json
from pathlib import Path

root = Path(__file__).resolve().parents[2]
manifest = json.loads((root / 'porting/tests/resource-case-migration.json').read_text())
stored_paths = {p.relative_to(root).as_posix() for p in (root / "src/main/resources/assets/hbm").rglob("*") if p.is_file()}
seen = set()
for entry in manifest['renames']:
    before, after = root / entry['before'], root / entry['after']
    assert entry['after'] == entry['before'].lower(), entry
    assert entry['after'] not in seen, entry
    seen.add(entry['after'])
    assert entry['before'] not in stored_paths, 'Old exact-case resource entry remains: ' + str(before)
    assert entry['after'] in stored_paths, 'Missing exact canonical resource entry: ' + str(after)
    assert hashlib.sha256(after.read_bytes()).hexdigest() == entry['sha256'], str(after)
print('PASS: %d canonical resource renames, original bytes preserved; broader resource inventory is not certified.' % len(seen))
