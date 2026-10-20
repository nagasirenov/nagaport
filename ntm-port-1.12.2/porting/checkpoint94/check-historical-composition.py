#!/usr/bin/env python3
"""Run three immutable historical guards through CP94's finite exact source view.

Only the thirteen pinned CP94 files are restored, and only the exact CP93
aggregate-checker snapshot replaces its new wrapper. Production is read-only.
This is source evidence, not runtime/rendering/build acceptance.
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
assert DATA['baseline'] == 'd2f171750be2f0d156a019f25fa781db39db308a'
assert not sys.flags.optimize

def sha(raw):
    return hashlib.sha256(raw).hexdigest()

for relative, expected in DATA['pins'].items():
    assert sha((MODULE / relative).read_bytes()) == expected, ('historical evidence drift', relative)
assert sha((HERE / 'cp93-source-boundary.py').read_bytes()) == '63a7aea8157ea60c2e25974717e094e37648f293361e7040fe2f3d14d9cc332c'
spec = importlib.util.spec_from_file_location('cp94_historical_view', TESTS / 'check-native-inventory-sound-sky-source.py')
boundary = importlib.util.module_from_spec(spec)
spec.loader.exec_module(boundary)
assert sorted(boundary.FILES) == DATA['files'] and len(boundary.FILES) == 13
boundary.verify(MODULE)
for family, relative in (
    ('normal', 'remaining-normal-batches/manifest.json'),
    ('air', 'native-block-air-captures/manifest.json'),
    ('press', 'legacy-press-promotion/cache-audit/manifest.json'),
):
    historical = json.loads((TESTS / relative).read_text())
    files = historical['source_sha256'] if family == 'press' else historical['files']
    assert sorted(set(boundary.FILES) & set(files)) == DATA['intersections'][family]
assert DATA['intersections'] == {
    'normal': ['src/main/java/com/hbm/dim/SkyProviderCelestial.java'], 'air': [], 'press': []}

before = {str(p.relative_to(MODULE)): sha(p.read_bytes()) for p in (MODULE / 'src').rglob('*.java')}
with tempfile.TemporaryDirectory(prefix='cp94-historical-view-') as temporary:
    worktree = Path(temporary)
    view = worktree / MODULE.name
    for relative in before:
        source, target = MODULE / relative, view / relative
        target.parent.mkdir(parents=True, exist_ok=True)
        if relative in boundary.FILES:
            original = boundary.reverse_source(relative, source.read_bytes().decode('utf-8')).encode('utf-8')
            assert sha(original) == boundary.FILES[relative]['before_sha256']
            target.write_bytes(original)
        else:
            target.symlink_to(source)
    view_tests = view / 'porting/tests'
    view_tests.mkdir(parents=True)
    for source in TESTS.iterdir():
        target = view_tests / source.name
        if source.name == '__pycache__':
            continue
        if source.name == 'check-native-normal-world-air-source.py':
            shutil.copyfile(HERE / 'cp93-source-boundary.py', target)
        elif source.name == 'remaining-normal-batches':
            target.mkdir()
            for item in source.iterdir():
                if item.name == '__pycache__':
                    continue
                if item.name == 'check_source.py':
                    shutil.copyfile(item, target / item.name)
                else:
                    (target / item.name).symlink_to(item)
        else:
            target.symlink_to(source)
    environment = dict(os.environ, PYTHONDONTWRITEBYTECODE='1',
                       GIT_DIR=str(REPO / '.git'), GIT_WORK_TREE=str(worktree))
    # The unchanged normal guard retains its exact Git CP92 reconstruction check.
    # Its script is copied, not rewritten; its ROOT therefore selects this view.
    subprocess.run([sys.executable, str(view_tests / 'remaining-normal-batches/check_source.py')],
                   cwd=view, env=environment, check=True)
    subprocess.run([sys.executable, str(TESTS / 'native-block-air-captures/check_cp93_composition.py'),
                    '--module', str(view)], cwd=MODULE, env=environment, check=True)
    subprocess.run([sys.executable, str(TESTS / 'legacy-press-promotion/cache-audit/scan.py'),
                    '--module', str(view), '--compose-colored', '--compose-replaceability',
                    '--compose-cp92', '--compose-cp93'], cwd=MODULE, env=environment, check=True)

after = {str(p.relative_to(MODULE)): sha(p.read_bytes()) for p in (MODULE / 'src').rglob('*.java')}
assert after == before, 'production source changed while checking historical composition'
print('PASS CP94 finite thirteen-file exact historical view; unchanged CP93 normal, air and press-cache guards/pins; '
      + str(len(before)) + ' production Java files unchanged. Not runtime/GL/build proof.', flush=True)
