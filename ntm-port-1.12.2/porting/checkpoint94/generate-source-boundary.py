#!/usr/bin/env python3
"""Generate CP94's frozen thirteen-file reversible boundary from CP93 only."""
from pathlib import Path
import difflib
import hashlib
import json
import subprocess

ROOT = Path(__file__).resolve().parents[3]
MODULE = ROOT / 'ntm-port-1.12.2'
HERE = Path(__file__).resolve().parent
TESTS = MODULE / 'porting/tests'
BASE = 'd2f171750be2f0d156a019f25fa781db39db308a'
EXPECTED = {
    'src/main/java/com/hbm/blocks/ModBlocks.java',
    'src/main/java/com/hbm/blocks/generic/BlockLogNT.java',
    'src/main/java/com/hbm/blocks/generic/BlockRotatablePillar.java',
    'src/main/java/com/hbm/dim/SkyProviderCelestial.java',
    'src/main/java/com/hbm/inventory/container/ContainerLeadBox.java',
    'src/main/java/com/hbm/inventory/container/ContainerToolBox.java',
    'src/main/java/com/hbm/inventory/gui/GUILeadBox.java',
    'src/main/java/com/hbm/inventory/gui/GUIToolBox.java',
    'src/main/java/com/hbm/items/ItemInventory.java',
    'src/main/java/com/hbm/items/block/ItemBlockStorageCrate.java',
    'src/main/java/com/hbm/items/tool/ItemLeadBox.java',
    'src/main/java/com/hbm/items/tool/ItemRebarPlacer.java',
    'src/main/java/com/hbm/items/tool/ItemToolBox.java',
}

def git(args):
    return subprocess.check_output(['git', '-C', str(ROOT)] + args)

def sha(data):
    return hashlib.sha256(data).hexdigest()

paths = git(['diff', '--name-only', BASE, '--', 'ntm-port-1.12.2/src/main/java']).decode().splitlines()
assert {str(Path(p).relative_to('ntm-port-1.12.2')) for p in paths} == EXPECTED
assert not git(['ls-files', '--others', '--exclude-standard', '--', 'ntm-port-1.12.2/src/main/java'])
manifest = {'baseline': BASE, 'files': {}, 'new_files': {}, 'deleted_files': {}, 'binary_files': {}}
for path in paths:
    old = git(['show', BASE + ':' + path]).decode('utf-8')
    new = (ROOT / path).read_bytes().decode('utf-8')
    old_lines, new_lines = old.splitlines(keepends=True), new.splitlines(keepends=True)
    old_offsets, new_offsets = [0], [0]
    for line in old_lines:
        old_offsets.append(old_offsets[-1] + len(line))
    for line in new_lines:
        new_offsets.append(new_offsets[-1] + len(line))
    edits = []
    for tag, i, j, a, b in difflib.SequenceMatcher(None, old_lines, new_lines).get_opcodes():
        if tag != 'equal':
            edits.append({'before_offset': old_offsets[i], 'after_offset': new_offsets[a],
                          'old': old[old_offsets[i]:old_offsets[j]],
                          'new': new[new_offsets[a]:new_offsets[b]]})
    manifest['files'][str(Path(path).relative_to('ntm-port-1.12.2'))] = {
        'before_sha256': sha(old.encode()), 'after_sha256': sha(new.encode()), 'edits': edits}
raw = (json.dumps(manifest, indent=2, ensure_ascii=False) + '\n').encode()
(TESTS / 'native-inventory-sound-sky-source-manifest.json').write_bytes(raw)
original = git(['show', BASE + ':ntm-port-1.12.2/porting/tests/check-native-normal-world-air-source.py'])
assert sha(original) == '63a7aea8157ea60c2e25974717e094e37648f293361e7040fe2f3d14d9cc332c'
(HERE / 'cp93-source-boundary.py').write_bytes(original)
original = original.decode('utf-8')
new = original.replace('native-normal-world-air-source-manifest.json', 'native-inventory-sound-sky-source-manifest.json')
new = new.replace('516ce9e9579be5477075bfef64416bc798cb250136d56c44c33d4d27f456cd8a', sha(raw))
new = new.replace('0675e599e5ee31a60776b560af792372df191790', BASE)
new = new.replace('len(FILES)==49 and len(NEW_FILES)==0', 'len(FILES)==13 and len(NEW_FILES)==0')
new = new.replace('Native normal/world-air source: 49', 'Native inventory/sound/sky source: 13')
new = new.replace('CP93 exact reversible normal/world-air', 'CP94 exact reversible inventory/sound/sky')
(TESTS / 'check-native-inventory-sound-sky-source.py').write_text(new)
wrapper = original.replace(
    'from pathlib import Path\n',
    'from pathlib import Path\nimport importlib.util\n'
    '_spec=importlib.util.spec_from_file_location("cp94_boundary",Path(__file__).with_name("check-native-inventory-sound-sky-source.py"))\n'
    '_next=importlib.util.module_from_spec(_spec);_spec.loader.exec_module(_next)\n')
wrapper = wrapper.replace('def reverse_source(relative,text):\n', 'def reverse_source(relative,text):\n    text=_next.reverse_source(relative,text)\n')
wrapper = wrapper.replace('    rejected=0\n', '    _next.verify(root,verbose)\n    rejected=0\n')
wrapper = wrapper.replace('assert forward==current', 'assert forward==_next.reverse_source(relative,current)')
(TESTS / 'check-native-normal-world-air-source.py').write_text(wrapper)
print('Prepared exact 13 existing and 0 new source boundaries against CP93; original CP93 checker preserved byte-for-byte.')
