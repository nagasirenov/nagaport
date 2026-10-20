#!/usr/bin/env python3
"""Generate CP96's frozen eight-existing/one-new source boundary from CP95 only."""
from pathlib import Path
import difflib
import hashlib
import json
import subprocess

ROOT = Path(__file__).resolve().parents[3]
MODULE = ROOT / 'ntm-port-1.12.2'
HERE = Path(__file__).resolve().parent
TESTS = MODULE / 'porting/tests'
BASE = '58f7f0c93cb4f5814d552e003ec9c7e2179a115f'
EXPECTED = {
    'src/main/java/com/hbm/blocks/ModBlocks.java',
    'src/main/java/com/hbm/blocks/generic/WasteEarth.java',
    'src/main/java/com/hbm/blocks/generic/WasteLeaves.java',
    'src/main/java/com/hbm/blocks/generic/WasteLog.java',
    'src/main/java/com/hbm/inventory/SlotNonRetarded.java',
    'src/main/java/com/hbm/inventory/container/ContainerLeadBox.java',
    'src/main/java/com/hbm/inventory/container/ContainerToolBox.java',
    'src/main/java/com/hbm/items/ItemInventoryTransfer.java',
    'src/main/java/com/hbm/tileentity/network/TileEntityCraneGrabber.java',
}
NEW_EXPECTED = {'src/main/java/com/hbm/items/ItemInventoryTransfer.java'}

def git(args):
    return subprocess.check_output(['git', '-C', str(ROOT)] + args)

def sha(data):
    return hashlib.sha256(data).hexdigest()

paths = git(['diff', '--name-only', BASE, '--', 'ntm-port-1.12.2/src/main/java']).decode().splitlines()
paths = sorted(set(paths) | set(git(['ls-files', '--others', '--exclude-standard', '--', 'ntm-port-1.12.2/src/main/java']).decode().splitlines()))
assert {str(Path(p).relative_to('ntm-port-1.12.2')) for p in paths} == EXPECTED
manifest = {'baseline': BASE, 'files': {}, 'new_files': {}, 'deleted_files': {}, 'binary_files': {}}
for path in paths:
    relative = str(Path(path).relative_to('ntm-port-1.12.2'))
    new = (ROOT / path).read_bytes().decode('utf-8')
    exists = subprocess.run(['git', '-C', str(ROOT), 'cat-file', '-e', BASE + ':' + path], stderr=subprocess.DEVNULL).returncode == 0
    if not exists:
        assert relative in NEW_EXPECTED
        manifest['new_files'][relative] = sha(new.encode('utf-8'))
        continue
    assert relative not in NEW_EXPECTED
    old = git(['show', BASE + ':' + path]).decode('utf-8')
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
assert set(manifest['new_files']) == NEW_EXPECTED and len(manifest['files']) == 8
raw = (json.dumps(manifest, indent=2, ensure_ascii=False) + '\n').encode()
(TESTS / 'native-box-waste-crane-source-manifest.json').write_bytes(raw)
original = git(['show', BASE + ':ntm-port-1.12.2/porting/tests/check-native-block-placement-source.py'])
assert sha(original) == 'f23172ecd12800f4f1590a2ff7461e4ed2bdb2100409d9ece789a4da3705753d'
(HERE / 'cp95-source-boundary.py').write_bytes(original)
original = original.decode('utf-8')
new = original.replace('native-block-placement-source-manifest.json', 'native-box-waste-crane-source-manifest.json')
new = new.replace('c6fb42093ef40396285e38ba8b1130a7a458892a7ac5d534de3ea1725bbc5fb9', sha(raw))
new = new.replace('d754432647909d6840333059865d834f87bd4a7f', BASE)
new = new.replace('len(FILES)==29 and len(NEW_FILES)==0', 'len(FILES)==8 and len(NEW_FILES)==1')
new = new.replace('Native block-placement source: 29', 'Native box/waste/crane source: 8').replace('0 new,', '1 new,')
new = new.replace('CP95 exact reversible block-placement', 'CP96 exact reversible box/waste/crane')
(TESTS / 'check-native-box-waste-crane-source.py').write_text(new)
wrapper = original.replace(
    'from pathlib import Path\n',
    'from pathlib import Path\nimport importlib.util\n'
    '_spec=importlib.util.spec_from_file_location("cp96_boundary",Path(__file__).with_name("check-native-box-waste-crane-source.py"))\n'
    '_next=importlib.util.module_from_spec(_spec);_spec.loader.exec_module(_next)\n')
wrapper = wrapper.replace('def reverse_source(relative,text):\n', 'def reverse_source(relative,text):\n    text=_next.reverse_source(relative,text)\n')
wrapper = wrapper.replace('    rejected=0\n', '    _next.verify(root,verbose)\n    rejected=0\n')
wrapper = wrapper.replace('assert forward==current', 'assert forward==_next.reverse_source(relative,current)')
(TESTS / 'check-native-block-placement-source.py').write_text(wrapper)
print('Prepared exact 8 existing and 1 new source boundaries against CP95; original CP95 checker preserved byte-for-byte.')
