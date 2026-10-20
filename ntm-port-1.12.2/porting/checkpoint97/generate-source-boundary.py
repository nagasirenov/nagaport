#!/usr/bin/env python3
"""Generate CP97's frozen two-existing/one-new pillar source boundary from CP96 only."""
from pathlib import Path
import difflib
import hashlib
import json
import subprocess

ROOT = Path(__file__).resolve().parents[3]
MODULE = ROOT / 'ntm-port-1.12.2'
HERE = Path(__file__).resolve().parent
TESTS = MODULE / 'porting/tests'
BASE = 'f44d3ffc5ad3bfa0afcfd93d035e51bdecde2de4'
EXPECTED = {
    'src/main/java/com/hbm/blocks/ModBlocks.java',
    'src/main/java/com/hbm/blocks/machine/BlockPillar.java',
    'src/main/java/com/hbm/main/PillarModelHandler.java',
}
NEW_EXPECTED = {'src/main/java/com/hbm/main/PillarModelHandler.java'}

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
assert set(manifest['new_files']) == NEW_EXPECTED and len(manifest['files']) == 2
raw = (json.dumps(manifest, indent=2, ensure_ascii=False) + '\n').encode()
(TESTS / 'native-pillar-model-source-manifest.json').write_bytes(raw)
original = git(['show', BASE + ':ntm-port-1.12.2/porting/tests/check-native-box-waste-crane-source.py'])
assert sha(original) == 'a5787a452c4fdab48e1ad4d589d009a31c0330cd563ac7b146df43a9134b6696'
(HERE / 'cp96-source-boundary.py').write_bytes(original)
original = original.decode('utf-8')
new = original.replace('native-box-waste-crane-source-manifest.json', 'native-pillar-model-source-manifest.json')
new = new.replace('39db10c2faa6bbfe2633f47d266d97b83c43a32386372932fc11960c129e239b', sha(raw))
new = new.replace('58f7f0c93cb4f5814d552e003ec9c7e2179a115f', BASE)
new = new.replace('len(FILES)==8 and len(NEW_FILES)==1', 'len(FILES)==2 and len(NEW_FILES)==1')
new = new.replace('Native box/waste/crane source: 8', 'Native pillar/model source: 2')
new = new.replace('CP96 exact reversible box/waste/crane', 'CP97 exact reversible pillar/model')
(TESTS / 'check-native-pillar-model-source.py').write_text(new)
wrapper = original.replace(
    'from pathlib import Path\n',
    'from pathlib import Path\nimport importlib.util\n'
    '_spec=importlib.util.spec_from_file_location("cp97_boundary",Path(__file__).with_name("check-native-pillar-model-source.py"))\n'
    '_next=importlib.util.module_from_spec(_spec);_spec.loader.exec_module(_next)\n')
wrapper = wrapper.replace('def reverse_source(relative,text):\n', 'def reverse_source(relative,text):\n    text=_next.reverse_source(relative,text)\n')
wrapper = wrapper.replace('    rejected=0\n', '    _next.verify(root,verbose)\n    rejected=0\n')
wrapper = wrapper.replace('assert forward==current', 'assert forward==_next.reverse_source(relative,current)')
(TESTS / 'check-native-box-waste-crane-source.py').write_text(wrapper)
print('Prepared exact 2 existing and 1 new pillar source boundaries against CP96; original CP96 checker preserved byte-for-byte.')
