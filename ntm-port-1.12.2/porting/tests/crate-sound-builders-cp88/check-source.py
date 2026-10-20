#!/usr/bin/env python3
"""Exact CP86 inverse; no unrelated production bytes, including crate mechanics, may change."""
import argparse, hashlib, importlib.util, io, json, re, subprocess, sys, tarfile
sys.dont_write_bytecode = True
from pathlib import Path
HERE = Path(__file__).resolve().parent
ROOT = HERE.parents[2]
REPO = ROOT.parent
BASE = 'fc2cd2dba34caba0dba74ed288f95107ab4dc979'
MOD = 'src/main/java/com/hbm/blocks/ModBlocks.java'
EXPECTED = ['crate', 'crate_weapon', 'crate_lead', 'crate_metal', 'crate_red', 'crate_iron', 'crate_steel', 'crate_desh', 'crate_tungsten', 'crate_template', 'safe']

def inputs():
    archive = subprocess.check_output(['git', '-C', str(REPO), 'archive', BASE, 'ntm-port-1.12.2/src/main/java'])
    with tarfile.open(fileobj=io.BytesIO(archive)) as tf:
        old = {m.name.split('/', 1)[1]: tf.extractfile(m).read() for m in tf.getmembers() if m.isfile() and m.name.endswith('.java')}
    new = {p.relative_to(ROOT).as_posix(): p.read_bytes() for p in (ROOT/'src/main/java').rglob('*.java')}
    return old, new

def validate(old, new):
    rows = json.loads((HERE/'sites.json').read_text())
    additions = json.loads((HERE/'additions.json').read_text())
    assert [r['name'] for r in rows] == EXPECTED
    assert len(additions) == 2 and len(rows) == 11
    assert set(old) == set(new), 'No added/removed production Java owners'
    inverse = dict(new)
    for row in rows:
        b, a = row['before'].encode(), row['after'].encode()
        assert old[MOD].count(b) == new[MOD].count(a) == 1, row['name']
        receiver = 'new ' + row['owner'] + '(Material.' + row['material'] + ').setTranslationKey("' + row['name'] + '")'
        assert row['receiver'] == receiver
        assert row['owner'] == ('BlockCrate' if row['name'] in EXPECTED[:5] else 'BlockStorageCrate')
        assert row['sound'] == ('SoundType.WOOD' if row['name'] in EXPECTED[:2] else 'ModSoundTypes.legacyMetal')
        assert row['after'] == row['before'].replace(receiver + '.setStepSound(' + row['sound'] + ')', '((' + row['owner'] + ') ' + receiver + ').withSoundType(' + row['sound'] + ')')
        assert old[MOD].decode().splitlines()[row['line'] - 1] == row['before']
        inverse[MOD] = inverse[MOD].replace(a, b)
    for path, addition in additions.items():
        owner = Path(path).stem
        expected = '\n\t/** NTM construction hook for the native protected sound setter. */\n\tpublic ' + owner + ' withSoundType(net.minecraft.block.SoundType sound) {\n\t\tsuper.setSoundType(sound);\n\t\treturn this;\n\t}\n'
        assert addition == expected and new[path].count(addition.encode()) == 1
        inverse[path] = new[path].replace(addition.encode(), b'')
        parent = 'BlockFalling' if owner == 'BlockCrate' else 'BlockContainer'
        assert ('public class ' + owner + ' extends ' + parent).encode() in old[path]
        for method in ['setSoundType', 'setTranslationKey', 'setHardness', 'setResistance', 'setCreativeTab']:
            assert not re.search(rb'\b(?:public|protected|private)\s+\w+\s+' + method.encode() + rb'\s*\(', new[path]), 'No shadow/native setter override'
        ctor = ('public ' + owner + '(Material p_i45394_1_) {\n\t\tsuper(p_i45394_1_);\n\t}').encode()
        assert old[path].count(ctor) == new[path].count(ctor) == 1
    for path in old:
        assert inverse[path] == old[path], 'Whole-file inverse/unselected source differs: ' + path
    assert old[MOD].count(b'.setStepSound(') == 113
    assert new[MOD].count(b'.setStepSound(') == 102
    return len(old)

COMPOSED_BASE = 'c2f60149e9a9fb836bade505fe566d40a54ef3cc'
COMPOSED_GUARD = 'check-native-shoot-sync-machine-source.py'
COMPOSED_MANIFEST = 'native-shoot-sync-machine-source-manifest.json'
FAMILY = (MOD, 'src/main/java/com/hbm/blocks/generic/BlockCrate.java', 'src/main/java/com/hbm/blocks/generic/BlockStorageCrate.java')

def verify_hashes():
    for row in json.loads((HERE/'source-hashes.json').read_text()):
        original = subprocess.check_output(['git', '-C', str(REPO), 'show', BASE+':ntm-port-1.12.2/'+row['path']])
        assert hashlib.sha256(original).hexdigest() == row['before_sha256']
        assert hashlib.sha256((ROOT/row['path']).read_bytes()).hexdigest() == row['after_sha256']
        assert row['changed'] == (row['before_sha256'] != row['after_sha256'])

def reconstruct_family(old):
    """Forward-replay this family only; never use these bytes as native test inputs."""
    assert set(old) == set(FAMILY)
    reconstructed = dict(old)
    for row in json.loads((HERE/'sites.json').read_text()):
        before, after = row['before'].encode(), row['after'].encode()
        assert reconstructed[MOD].count(before) == 1
        reconstructed[MOD] = reconstructed[MOD].replace(before, after)
    for path, addition in json.loads((HERE/'additions.json').read_text()).items():
        owner = Path(path).stem
        constructor = ('\tpublic ' + owner + '(Material p_i45394_1_) {\n\t\tsuper(p_i45394_1_);\n\t}\n').encode()
        assert reconstructed[path].count(constructor) == 1
        reconstructed[path] = reconstructed[path].replace(constructor, constructor + addition.encode())
    # Reuse the complete isolated-family formula, owner-domain and whole-file inverse checks.
    assert validate(old, reconstructed) == 3
    return reconstructed

def verify_composed():
    guard_path = HERE.parent/COMPOSED_GUARD
    manifest_path = HERE.parent/COMPOSED_MANIFEST
    assert guard_path.is_file() and manifest_path.is_file(), 'Required pinned CP88 aggregate guard/manifest is absent; composed proof is unavailable'
    script = guard_path.read_text()
    pins = re.findall(r"assert hashlib\.sha256\(RAW\)\.hexdigest\(\) == '([0-9a-f]{64})'", script)
    assert len(pins) == 1, 'CP88 aggregate must contain its explicit SHA-256 manifest pin'
    raw = manifest_path.read_bytes()
    assert hashlib.sha256(raw).hexdigest() == pins[0], 'CP88 aggregate manifest pin mismatch'
    manifest = json.loads(raw)
    assert manifest['baseline'] == COMPOSED_BASE, 'Expected CP88 aggregate based on sealed CP87'
    assert set(FAMILY).issubset(manifest['files']), 'CP88 aggregate must account for all three crate files'
    assert not set(FAMILY).intersection(set(manifest['new_files']) | set(manifest['deleted_files']) | set(manifest['binary_files']))
    spec = importlib.util.spec_from_file_location('crate_cp88_aggregate', guard_path)
    aggregate = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(aggregate)
    assert aggregate.RAW == raw and aggregate.MANIFEST == manifest
    assert aggregate.verify(ROOT, verbose=False) == manifest, 'CP88 aggregate verification failed'
    old = {path: subprocess.check_output(['git', '-C', str(REPO), 'show', BASE+':ntm-port-1.12.2/'+path]) for path in FAMILY}
    current = {path: (ROOT/path).read_bytes() for path in FAMILY}
    for path in FAMILY:
        cp87 = subprocess.check_output(['git', '-C', str(REPO), 'show', COMPOSED_BASE+':ntm-port-1.12.2/'+path])
        assert cp87 == old[path], (path, 'CP87 must not have changed this isolated crate family')
        assert aggregate.reverse_source(path, current[path].decode('utf-8')).encode('utf-8') == cp87
    reconstructed = reconstruct_family(old)
    assert reconstructed == current, 'Actual tested files must equal the exact isolated three-file family reconstruction'
    assert validate(old, current) == 3
    verify_hashes()
    print('PASS pinned CP88 aggregate plus exact three-file crate reconstruction and actual-current source hashes; native probes use current files, not reconstructed bytes.')

if __name__ == '__main__':
    assert not sys.flags.optimize
    parser = argparse.ArgumentParser()
    parser.add_argument('--composed-source', action='store_true', help='Require the pinned CP88 aggregate and exact isolated three-file reconstruction; never waive standalone checks implicitly')
    args = parser.parse_args()
    if args.composed_source:
        verify_composed()
    else:
        old, new = inputs()
        total = validate(old, new)
        verify_hashes()
        print('PASS exact three-file CP86 inversion across all %d production Java units; eleven closed named-constructor casts; untouched crate loot/storage, sound definitions, native visibility, and all 102 remaining registry sites.' % total)
