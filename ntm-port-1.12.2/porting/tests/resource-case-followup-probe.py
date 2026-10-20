#!/usr/bin/env python3
"""Bounded real native lookup probe. Compiles only a test fixture, not project sources."""
import argparse
import collections
import hashlib
import json
import os
import zipfile
from pathlib import Path
import subprocess
import tempfile
p = argparse.ArgumentParser()
p.add_argument('--classpath', required=True)
p.add_argument('--java-home', type=Path, required=True)
a = p.parse_args()
root = Path(__file__).resolve().parents[2]
tests = root / 'porting/tests'
entries = json.loads((tests / 'resource-case-followup-manifest.json').read_text())['renames']
assert len(entries) == 32
previous = json.loads((tests / "resource-case-migration.json").read_text())["renames"]
all_entries = previous + entries
assert len(all_entries) == 43
assets = root / 'src/main/resources/assets/hbm'
folded = collections.defaultdict(list)
for path in assets.rglob('*'):
    if path.is_file():
        folded[path.relative_to(assets).as_posix().lower()].append(path)
assert not any(len(v) > 1 for v in folded.values()), 'Case collisions'
assert sum(map(len, folded.values())) == 8645, 'Unexpected asset count'
stored_paths = {p.relative_to(root).as_posix(): p for paths in folded.values() for p in paths}
seen = set()
for e in all_entries:
    assert e['after'] == e['before'].lower()
    assert e['after'] not in seen
    seen.add(e['after'])
    assert e['before'] not in stored_paths, 'Old exact-case directory entry remains: ' + e['before']
    assert e['after'] in stored_paths, 'Missing exact canonical directory entry: ' + e['after']
    assert hashlib.sha256((root / e['after']).read_bytes()).hexdigest() == e['sha256']
with tempfile.TemporaryDirectory(prefix='resource-case-followup-') as name:
    tmp = Path(name)
    source = tmp / 'ResourceCaseFollowupMinecraftTest.java'
    source.write_text((tests / 'resource-case-followup-test.java.in').read_text())
    manifest = tmp / 'manifest.tsv'
    manifest.write_text(''.join(e['literal'] + '\t' + e['literal'].lower() + '\t' + e['sha256'] + '\n' for e in entries))
    zip_manifest = tmp / 'zip-manifest.tsv'
    prefix = 'src/main/resources/assets/hbm/'
    zip_manifest.write_text(''.join(e['before'][len(prefix):] + '\t' + e['after'][len(prefix):] + '\t' + e['sha256'] + '\n' for e in all_entries))
    old_zip, current_zip = tmp / 'old-case.jar', tmp / 'canonical.jar'
    with zipfile.ZipFile(old_zip, 'w') as old, zipfile.ZipFile(current_zip, 'w') as current:
        for e in all_entries:
            # Canonical archive names come from actual stored directory entries.
            path = stored_paths[e['after']]
            data = path.read_bytes()
            current.writestr(path.relative_to(root / 'src/main/resources').as_posix(), data)
            # Reconstruct only historical entry names, retaining frozen original bytes.
            old.writestr(e['before'][len('src/main/resources/'):], data)
    subprocess.run([str(a.java_home / 'bin/javac'), '-source', '8', '-target', '8', '-cp', a.classpath, '-d', str(tmp), str(source)], check=True)
    subprocess.run([str(a.java_home / 'bin/java'), '-Djava.awt.headless=true', '-cp', str(tmp) + os.pathsep + a.classpath, 'ResourceCaseFollowupMinecraftTest', str(root / 'src/main/resources'), str(manifest), str(zip_manifest), str(old_zip), str(current_zip)], cwd=tmp, check=True)
print('PASS: exactly 32 follow-up renames and 43 combined native ZIP lookups, zero collisions; no broader resource acceptance.')
