#!/usr/bin/env python3
"""Exact finite infrastructure guard. This does not approve entity-position migration."""
import argparse, hashlib, json, pathlib, sys
HERE = pathlib.Path(__file__).resolve().parent
p = argparse.ArgumentParser()
p.add_argument('--root', type=pathlib.Path, default=HERE.parents[3])
a = p.parse_args()
manifest = json.loads((HERE / 'source-manifest.json').read_text())
sha = lambda b: hashlib.sha256(b).hexdigest()
for section in ('production', 'frozen_sound_sources'):
    for path, expected in manifest[section].items():
        file = a.root / path
        if not file.exists() and section == 'frozen_sound_sources':
            # Sparse delivery validation checks these through the exact baseline tree.
            import subprocess
            actual = subprocess.check_output(['git', '-C', str(a.root), 'show', 'HEAD:' + path])
        else:
            actual = file.read_bytes()
        assert sha(actual) == expected, (section, path)
for path, rule in manifest['inverse'].items():
    text = (a.root / path).read_text()
    for old, new in rule['replacements']:
        assert text.count(old) == 1, ('inverse multiplicity', path, old)
        text = text.replace(old, new)
    assert sha(text.encode()) == rule['baseline_sha256'], ('inverse baseline', path)
for path, expected in manifest['evidence_pins'].items():
    assert sha((HERE / path).read_bytes()) == expected, ('evidence pin', path)
source = a.root / 'ntm-port-1.12.2/src/main/java'
for file in source.rglob('*.java'):
    if file.name != 'EntitySoundTransport.java':
        assert 'EntitySoundTransport' not in file.read_text(), ('unreviewed activation', file)
print('PASS exact transport sources, two baseline inverses, frozen coordinate/category/registry sources, evidence pins and no current call activation.')
