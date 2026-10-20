#!/usr/bin/env python3
"""Generate CP95's frozen twenty-nine-file reversible boundary from CP94 only."""
from pathlib import Path
import difflib
import hashlib
import json
import subprocess

ROOT = Path(__file__).resolve().parents[3]
MODULE = ROOT / 'ntm-port-1.12.2'
HERE = Path(__file__).resolve().parent
TESTS = MODULE / 'porting/tests'
BASE = 'd754432647909d6840333059865d834f87bd4a7f'
EXPECTED = {
    'src/main/java/com/hbm/blocks/BlockKelp.java',
    'src/main/java/com/hbm/blocks/BlockVinylVine.java',
    'src/main/java/com/hbm/blocks/BlockWaterPlant.java',
    'src/main/java/com/hbm/blocks/bomb/Balefire.java',
    'src/main/java/com/hbm/blocks/bomb/DigammaFlame.java',
    'src/main/java/com/hbm/blocks/bomb/Landmine.java',
    'src/main/java/com/hbm/blocks/fluid/MeltedFlesh.java',
    'src/main/java/com/hbm/blocks/gas/BlockGasRadonDense.java',
    'src/main/java/com/hbm/blocks/generic/BlockDeadPlant.java',
    'src/main/java/com/hbm/blocks/generic/BlockFallout.java',
    'src/main/java/com/hbm/blocks/generic/BlockModDoor.java',
    'src/main/java/com/hbm/blocks/generic/BlockMush.java',
    'src/main/java/com/hbm/blocks/generic/BlockRubberPlant.java',
    'src/main/java/com/hbm/blocks/generic/BlockStalagmite.java',
    'src/main/java/com/hbm/blocks/generic/BlockTallPlant.java',
    'src/main/java/com/hbm/blocks/generic/BlockTallPlantWater.java',
    'src/main/java/com/hbm/blocks/machine/BlockOrbitalStation.java',
    'src/main/java/com/hbm/blocks/machine/MachineChungus.java',
    'src/main/java/com/hbm/blocks/machine/MachineITER.java',
    'src/main/java/com/hbm/blocks/machine/fusion/MachineFusionTorus.java',
    'src/main/java/com/hbm/entity/effect/EntityFalloutRain.java',
    'src/main/java/com/hbm/entity/grenade/EntityWastePearl.java',
    'src/main/java/com/hbm/explosion/ExplosionThermo.java',
    'src/main/java/com/hbm/items/tool/ItemModDoor.java',
    'src/main/java/com/hbm/items/weapon/sedna/factory/XFactoryTool.java',
    'src/main/java/com/hbm/main/ModEventHandler.java',
    'src/main/java/com/hbm/tileentity/machine/TileEntityMachineDrain.java',
    'src/main/java/com/hbm/world/feature/OilSpot.java',
    'src/main/java/com/hbm/world/feature/OreCave.java',
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
(TESTS / 'native-block-placement-source-manifest.json').write_bytes(raw)
original = git(['show', BASE + ':ntm-port-1.12.2/porting/tests/check-native-inventory-sound-sky-source.py'])
assert sha(original) == '4daa4fceee66e7fa3c1bc42f84b5db9013c94ccf2d7a19cbe8e7eb2e45bc3aa3'
(HERE / 'cp94-source-boundary.py').write_bytes(original)
original = original.decode('utf-8')
new = original.replace('native-inventory-sound-sky-source-manifest.json', 'native-block-placement-source-manifest.json')
new = new.replace('1c6767e7c5efcd3a46ee7ad24962d440b6a54a450b59f541c57efb216587645f', sha(raw))
new = new.replace('d2f171750be2f0d156a019f25fa781db39db308a', BASE)
new = new.replace('len(FILES)==13 and len(NEW_FILES)==0', 'len(FILES)==29 and len(NEW_FILES)==0')
new = new.replace('Native inventory/sound/sky source: 13', 'Native block-placement source: 29')
new = new.replace('CP94 exact reversible inventory/sound/sky', 'CP95 exact reversible block-placement')
(TESTS / 'check-native-block-placement-source.py').write_text(new)
wrapper = original.replace(
    'from pathlib import Path\n',
    'from pathlib import Path\nimport importlib.util\n'
    '_spec=importlib.util.spec_from_file_location("cp95_boundary",Path(__file__).with_name("check-native-block-placement-source.py"))\n'
    '_next=importlib.util.module_from_spec(_spec);_spec.loader.exec_module(_next)\n')
wrapper = wrapper.replace('def reverse_source(relative,text):\n', 'def reverse_source(relative,text):\n    text=_next.reverse_source(relative,text)\n')
wrapper = wrapper.replace('    rejected=0\n', '    _next.verify(root,verbose)\n    rejected=0\n')
wrapper = wrapper.replace('assert forward==current', 'assert forward==_next.reverse_source(relative,current)')
(TESTS / 'check-native-inventory-sound-sky-source.py').write_text(wrapper)
print('Prepared exact 29 existing and 0 new source boundaries against CP94; original CP94 checker preserved byte-for-byte.')
