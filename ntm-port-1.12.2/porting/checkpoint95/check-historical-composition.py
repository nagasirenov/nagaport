#!/usr/bin/env python3
"""Compose the exact placement inverse BEFORE immutable CP94/CP93/CP92 guards.

Restore only 29 frozen placement files and the original CP94 boundary checker in
a temporary view. Reuse CP94's unchanged finite historical runner, then check
normal-cube source evidence against that placement-reversed view. No runtime,
rendering, build or gameplay acceptance is claimed; production is read-only.
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
assert DATA['baseline'] == 'd754432647909d6840333059865d834f87bd4a7f'

def sha(raw):
    return hashlib.sha256(raw).hexdigest()

def load(name, path):
    spec = importlib.util.spec_from_file_location(name, path)
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module

for relative, expected in DATA['pins'].items():
    assert sha((MODULE / relative).read_bytes()) == expected, ('evidence drift', relative)
assert DATA['pins']['porting/tests/native-block-placement/check_source.py'] == 'd8f97063b6eefdbb20ae5979fed9e09442ed81e15118ef6200aa02c92e8b8849'
assert DATA['pins']['porting/tests/native-block-placement/manifest.json'] == '554c8892672d0b3d2d14f2a6a5de7b05a259cc7a2b009a3bfec35ce198d9fd78'
assert sha((HERE / 'cp94-source-boundary.py').read_bytes()) == '4daa4fceee66e7fa3c1bc42f84b5db9013c94ccf2d7a19cbe8e7eb2e45bc3aa3'
placement = load('cp95_placement_inverse', TESTS / 'native-block-placement/check_source.py')
boundary = load('cp95_aggregate_inverse', TESTS / 'check-native-block-placement-source.py')
assert sorted(placement.data['files']) == sorted(boundary.FILES) == DATA['files']
assert len(DATA['files']) == 29
boundary.verify(MODULE)
placement.verify(MODULE)
for family, relative in (
    ('air', 'native-block-air-captures/manifest.json'),
    ('press', 'legacy-press-promotion/cache-audit/manifest.json'),
    ('normal_cube', 'native-normal-cube-queries/manifest.json'),
):
    historical = json.loads((TESTS / relative).read_text())
    files = historical['source_sha256'] if family == 'press' else historical['files']
    assert sorted(set(boundary.FILES) & set(files)) == DATA['intersections'][family]
assert DATA['intersections'] == {
    'air': ['src/main/java/com/hbm/blocks/generic/BlockFallout.java'],
    'press': ['src/main/java/com/hbm/blocks/machine/MachineChungus.java',
              'src/main/java/com/hbm/tileentity/machine/TileEntityMachineDrain.java'],
    'normal_cube': ['src/main/java/com/hbm/entity/effect/EntityFalloutRain.java',
                    'src/main/java/com/hbm/items/tool/ItemModDoor.java',
                    'src/main/java/com/hbm/world/feature/OilSpot.java',
                    'src/main/java/com/hbm/world/feature/OreCave.java'],
}
before = {str(p.relative_to(MODULE)): sha(p.read_bytes()) for p in (MODULE / 'src').rglob('*.java')}
with tempfile.TemporaryDirectory(prefix='cp95-historical-view-') as temporary:
    worktree = Path(temporary)
    # CP94's unchanged normal-stream guard retains its Git reconstruction check.
    # The symlink only exposes the existing read-only Git object source to it.
    (worktree / '.git').symlink_to(REPO / '.git')
    view = worktree / MODULE.name
    for relative in before:
        source, target = MODULE / relative, view / relative
        target.parent.mkdir(parents=True, exist_ok=True)
        if relative in boundary.FILES:
            raw = source.read_bytes()
            original = placement.reverse(raw, placement.data['files'][relative])
            assert original == boundary.reverse_source(relative, raw.decode('utf-8')).encode('utf-8')
            target.write_bytes(original)
        else:
            target.symlink_to(source)
    view_tests = view / 'porting/tests'
    view_tests.mkdir(parents=True)
    for source in TESTS.iterdir():
        target = view_tests / source.name
        if source.name == '__pycache__':
            continue
        if source.name == 'check-native-inventory-sound-sky-source.py':
            shutil.copyfile(HERE / 'cp94-source-boundary.py', target)
        else:
            target.symlink_to(source)
    prior = view / 'porting/checkpoint94'
    prior.mkdir()
    for source in (MODULE / 'porting/checkpoint94').iterdir():
        if source.name == '__pycache__':
            continue
        if source.name == 'check-historical-composition.py':
            shutil.copyfile(source, prior / source.name)
        else:
            (prior / source.name).symlink_to(source)
    environment = dict(os.environ, PYTHONDONTWRITEBYTECODE='1')
    subprocess.run([sys.executable, '-u', str(prior / 'check-historical-composition.py')],
                   cwd=view, env=environment, check=True)
    normal_cube = load('cp95_historical_normal_cube', TESTS / 'native-normal-cube-queries/check_source.py')
    normal_cube.verify(view)
after = {str(p.relative_to(MODULE)): sha(p.read_bytes()) for p in (MODULE / 'src').rglob('*.java')}
assert after == before, 'production source changed while checking historical composition'
print('PASS CP95 finite 29-file placement inverse before unchanged CP94/CP93/CP92 historical guards; '
      'air 1, press-cache 2 and normal-cube 4 exact intersections; '
      + str(len(before)) + ' source Java files unchanged. Not runtime/GL/build proof.', flush=True)
