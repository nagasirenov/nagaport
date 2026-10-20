#!/usr/bin/env python3
"""Restore CP96's exact eight-file inverse and omit its one verified new helper.

Run the immutable CP95 historical stack, plus the CP94 inventory and three-file
sound guards, against this finite CP95 source view. Every current source byte,
including the new helper, is verified first and remains untouched. Static source
composition only, never runtime, GL, build or gameplay acceptance.
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
assert DATA['baseline'] == '58f7f0c93cb4f5814d552e003ec9c7e2179a115f'

def sha(raw):
    return hashlib.sha256(raw).hexdigest()

for relative, expected in DATA['pins'].items():
    assert sha((MODULE / relative).read_bytes()) == expected, ('evidence drift', relative)
assert sha((HERE / 'cp95-source-boundary.py').read_bytes()) == 'f23172ecd12800f4f1590a2ff7461e4ed2bdb2100409d9ece789a4da3705753d'
spec = importlib.util.spec_from_file_location('cp96_exact_inverse', TESTS / 'check-native-box-waste-crane-source.py')
boundary = importlib.util.module_from_spec(spec)
spec.loader.exec_module(boundary)
assert sorted(boundary.FILES) == DATA['files'] and len(boundary.FILES) == 8
assert sorted(boundary.NEW_FILES) == DATA['new_files'] == ['src/main/java/com/hbm/items/ItemInventoryTransfer.java']
boundary.verify(MODULE)
for family, relative, key in (
    ('inventory', 'native-item-inventory/manifest.json', 'files'),
    ('inventory_protected', 'native-item-inventory/manifest.json', 'protected'),
    ('normal_cube', 'native-normal-cube-queries/manifest.json', 'files'),
    ('air', 'native-block-air-captures/manifest.json', 'files'),
    ('press', 'legacy-press-promotion/cache-audit/manifest.json', 'source_sha256'),
    ('placement', 'native-block-placement/manifest.json', 'files'),
):
    historical = json.loads((TESTS / relative).read_text())
    assert sorted(set(boundary.FILES) & set(historical[key])) == DATA['intersections'][family]
sound_files = {row['path'] for row in json.loads((TESTS / 'log-pillar-sound-builders-cp94/source-hashes.json').read_text())}
assert sorted(set(boundary.FILES) & sound_files) == DATA['intersections']['sound']
assert DATA['intersections'] == {
    'inventory': ['src/main/java/com/hbm/inventory/container/ContainerLeadBox.java',
                  'src/main/java/com/hbm/inventory/container/ContainerToolBox.java'],
    'inventory_protected': [],
    'normal_cube': ['src/main/java/com/hbm/blocks/generic/WasteEarth.java'],
    'air': [],
    'press': ['src/main/java/com/hbm/tileentity/network/TileEntityCraneGrabber.java'],
    'placement': [],
    'sound': ['src/main/java/com/hbm/blocks/ModBlocks.java'],
}
before = {str(p.relative_to(MODULE)): sha(p.read_bytes()) for p in (MODULE / 'src').rglob('*.java')}
with tempfile.TemporaryDirectory(prefix='cp96-historical-view-') as temporary:
    worktree = Path(temporary)
    (worktree / '.git').symlink_to(REPO / '.git')
    view = worktree / MODULE.name
    for relative in before:
        source, target = MODULE / relative, view / relative
        if relative in boundary.NEW_FILES:
            # Verify exact current bytes and its absent historical representation.
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
    view_tests = view / 'porting/tests'
    view_tests.mkdir(parents=True)
    copied_families = {
        'native-item-inventory': 'check_source.py',
        'log-pillar-sound-builders-cp94': 'check-source.py',
    }
    for source in TESTS.iterdir():
        target = view_tests / source.name
        if source.name == '__pycache__':
            continue
        if source.name == 'check-native-block-placement-source.py':
            shutil.copyfile(HERE / 'cp95-source-boundary.py', target)
        elif source.name in copied_families:
            target.mkdir()
            for item in source.iterdir():
                if item.name == '__pycache__':
                    continue
                if item.name == copied_families[source.name]:
                    shutil.copyfile(item, target / item.name)
                else:
                    (target / item.name).symlink_to(item)
        else:
            target.symlink_to(source)
    (view / 'porting/checkpoint94').symlink_to(MODULE / 'porting/checkpoint94')
    prior = view / 'porting/checkpoint95'
    prior.mkdir()
    for source in (MODULE / 'porting/checkpoint95').iterdir():
        if source.name == '__pycache__':
            continue
        if source.name == 'check-historical-composition.py':
            shutil.copyfile(source, prior / source.name)
        else:
            (prior / source.name).symlink_to(source)
    environment = dict(os.environ, PYTHONDONTWRITEBYTECODE='1')
    subprocess.run([sys.executable, '-u', str(prior / 'check-historical-composition.py')],
                   cwd=view, env=environment, check=True)
    subprocess.run([sys.executable, '-u', str(view_tests / 'native-item-inventory/check_source.py')],
                   cwd=view, env=environment, check=True)
    subprocess.run([sys.executable, '-u', str(view_tests / 'log-pillar-sound-builders-cp94/check-source.py'), '--family-only'],
                   cwd=view, env=environment, check=True)
after = {str(p.relative_to(MODULE)): sha(p.read_bytes()) for p in (MODULE / 'src').rglob('*.java')}
assert after == before, 'source changed while checking historical composition'
print('PASS CP96 exact 8-file inverse plus 1 verified new-helper omission before unchanged CP95 historical stack; '
      'inventory 2, sound 1, press-cache 1 and normal-cube 1 exact intersections; '
      + str(len(before)) + ' current source Java files unchanged, '
      + str(len(historical_paths)) + ' historical source files. Not runtime/GL/build proof.', flush=True)
