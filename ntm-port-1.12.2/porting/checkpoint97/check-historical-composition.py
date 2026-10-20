#!/usr/bin/env python3
"""Restore two exact CP97 sources and omit its one hash-verified new model helper.

Run the immutable CP96 historical stack and CP96 waste-sound guard against the
finite CP96 source view. No production files are written. Static source evidence
only; this does not establish runtime, rendering, build or gameplay acceptance.
"""
from pathlib import Path
import hashlib
import importlib.util
import json
import os
import shutil
import subprocess
import sys
import tempfile

sys.dont_write_bytecode = True
HERE = Path(__file__).resolve().parent
MODULE = HERE.parents[1]
REPO = MODULE.parent
TESTS = MODULE / 'porting/tests'
DATA = json.loads((HERE / 'historical-composition.json').read_text())
assert not sys.flags.optimize
assert DATA['baseline'] == 'f44d3ffc5ad3bfa0afcfd93d035e51bdecde2de4'

def sha(raw):
    return hashlib.sha256(raw).hexdigest()

for relative, expected in DATA['pins'].items():
    assert sha((MODULE / relative).read_bytes()) == expected, ('evidence drift', relative)
assert sha((HERE / 'cp96-source-boundary.py').read_bytes()) == 'a5787a452c4fdab48e1ad4d589d009a31c0330cd563ac7b146df43a9134b6696'
spec = importlib.util.spec_from_file_location('cp97_exact_inverse', TESTS / 'check-native-pillar-model-source.py')
boundary = importlib.util.module_from_spec(spec)
spec.loader.exec_module(boundary)
assert sorted(boundary.FILES) == DATA['files'] and len(boundary.FILES) == 2
assert sorted(boundary.NEW_FILES) == DATA['new_files'] == ['src/main/java/com/hbm/main/PillarModelHandler.java']
boundary.verify(MODULE)
for family, relative, key in (
    ('inventory', 'native-item-inventory/manifest.json', 'files'),
    ('normal_cube', 'native-normal-cube-queries/manifest.json', 'files'),
    ('air', 'native-block-air-captures/manifest.json', 'files'),
    ('press', 'legacy-press-promotion/cache-audit/manifest.json', 'source_sha256'),
    ('placement', 'native-block-placement/manifest.json', 'files'),
):
    historical = json.loads((TESTS / relative).read_text())
    assert sorted(set(boundary.FILES) & set(historical[key])) == DATA['intersections'][family]
for name, family in (('sound_cp94', 'log-pillar-sound-builders-cp94'),
                     ('sound_cp96', 'waste-terrain-sound-builders-cp96')):
    files = {row['path'] for row in json.loads((TESTS / family / 'source-hashes.json').read_text())}
    assert sorted(set(boundary.FILES) & files) == DATA['intersections'][name]
assert DATA['intersections'] == {
    'inventory': [], 'normal_cube': [], 'air': [], 'press': [], 'placement': [],
    'sound_cp94': ['src/main/java/com/hbm/blocks/ModBlocks.java'],
    'sound_cp96': ['src/main/java/com/hbm/blocks/ModBlocks.java'],
}
before = {str(p.relative_to(MODULE)): sha(p.read_bytes()) for p in (MODULE / 'src').rglob('*.java')}
with tempfile.TemporaryDirectory(prefix='cp97-historical-view-') as temporary:
    worktree = Path(temporary)
    (worktree / '.git').symlink_to(REPO / '.git')
    view = worktree / MODULE.name
    for relative in before:
        source, target = MODULE / relative, view / relative
        if relative in boundary.NEW_FILES:
            assert boundary.reverse_source(relative, source.read_bytes().decode('utf-8')) == ''
            continue
        target.parent.mkdir(parents=True, exist_ok=True)
        if relative in boundary.FILES:
            original = boundary.reverse_source(relative, source.read_bytes().decode('utf-8')).encode('utf-8')
            assert sha(original) == boundary.FILES[relative]['before_sha256']
            target.write_bytes(original)
        else:
            target.symlink_to(source)
    historical_paths = {str(p.relative_to(view)) for p in (view / 'src').rglob('*.java')}
    assert historical_paths == set(before) - set(boundary.NEW_FILES)
    assert len(historical_paths) == len(before) - 1
    assert not (view / DATA['new_files'][0]).exists()
    # The previous checkpoint's helper remains present until that older layer
    # verifies and removes it within its own exact historical reconstruction.
    assert (view / 'src/main/java/com/hbm/items/ItemInventoryTransfer.java').exists()
    view_tests = view / 'porting/tests'
    view_tests.mkdir(parents=True)
    for source in TESTS.iterdir():
        target = view_tests / source.name
        if source.name == '__pycache__':
            continue
        if source.name == 'check-native-box-waste-crane-source.py':
            shutil.copyfile(HERE / 'cp96-source-boundary.py', target)
        elif source.name == 'waste-terrain-sound-builders-cp96':
            target.mkdir()
            for item in source.iterdir():
                if item.name == '__pycache__':
                    continue
                if item.name == 'check-source.py':
                    shutil.copyfile(item, target / item.name)
                else:
                    (target / item.name).symlink_to(item)
        else:
            target.symlink_to(source)
    for checkpoint in ('checkpoint94', 'checkpoint95'):
        (view / 'porting' / checkpoint).symlink_to(MODULE / 'porting' / checkpoint)
    prior = view / 'porting/checkpoint96'
    prior.mkdir()
    for source in (MODULE / 'porting/checkpoint96').iterdir():
        if source.name == '__pycache__':
            continue
        if source.name == 'check-historical-composition.py':
            shutil.copyfile(source, prior / source.name)
        else:
            (prior / source.name).symlink_to(source)
    environment = dict(os.environ, PYTHONDONTWRITEBYTECODE='1')
    subprocess.run([sys.executable, '-u', str(prior / 'check-historical-composition.py')],
                   cwd=view, env=environment, check=True)
    subprocess.run([sys.executable, '-u', str(view_tests / 'waste-terrain-sound-builders-cp96/check-source.py'), '--family-only'],
                   cwd=view, env=environment, check=True)
after = {str(p.relative_to(MODULE)): sha(p.read_bytes()) for p in (MODULE / 'src').rglob('*.java')}
assert after == before, 'source changed while checking historical composition'
print('PASS CP97 exact 2-file inverse plus 1 verified model-helper omission before unchanged CP96 historical stack; '
      'ModBlocks is the sole intersection with each CP94/CP96 sound family; '
      + str(len(before)) + ' current source Java files unchanged, '
      + str(len(historical_paths)) + ' historical source files. Not runtime/GL/build proof.', flush=True)
