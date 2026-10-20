#!/usr/bin/env python3
"""Compile full production sound files against cached native APIs. No Minecraft/Forge stubs."""
import argparse, hashlib, json, os, subprocess, tempfile, zipfile
from pathlib import Path
p = argparse.ArgumentParser()
p.add_argument('--java-home', type=Path, required=True)
p.add_argument('--classpath', required=True)
p.add_argument('--legacy-jar', type=Path, help='Optional cached original 1.7.10 client jar for independent runtime oracle')
a = p.parse_args()
root = Path(__file__).resolve().parents[2]
tests = root / 'porting/tests'
src = root / 'src/main/java'
resources = root / 'src/main/resources'
manifest = json.loads((tests / 'native-block-sound-resource-renames.json').read_text())['renames']
manifest += json.loads((tests / 'decoration-crate-sound-renames.json').read_text())['renames']
manifest += json.loads((tests / 'foundry-sound-renames.json').read_text())['renames']
particle_resources = json.loads((tests / 'particle-sound-resources.json').read_text())
small_resources = json.loads((tests / 'small-explosion-sound-resources.json').read_text())
particle_resources['keys'].update(small_resources['keys'])
particle_resources['renames'] += small_resources['renames']
particle_resources['samples'] += small_resources['samples']
manifest += particle_resources['renames']
assert len(manifest) == 21
pristine = root.parent / 'ntm-original-pristine'
# Compare stored directory-entry spelling, not path lookup: on Windows the old
# case resolves to the renamed file even though no duplicate entry exists.
stored_paths = {path.relative_to(root).as_posix() for path in (resources / 'assets/hbm/sounds').rglob('*') if path.is_file()}
for entry in manifest:
    actual = root / entry['after']
    original = pristine / entry['before']
    assert hashlib.sha256(actual.read_bytes()).hexdigest() == entry['sha256'], actual
    assert actual.read_bytes() == original.read_bytes(), 'Audio differs from pristine: ' + str(actual)
    assert entry['after'] in stored_paths, 'Expected exact lowercase audio spelling: ' + entry['after']
    assert entry['before'] not in stored_paths, 'Case-only duplicates would break Windows: ' + entry['before']
print('PASS twenty-one portable lowercase audio renames retain exact pristine bytes', flush=True)
original_json = json.loads((pristine / 'src/main/resources/assets/hbm/sounds.json').read_text())
current_json = json.loads((resources / 'assets/hbm/sounds.json').read_text())
for key in ('block.pipePlaced', 'step.metalBlock', 'step.morkite', 'item.battery', 'block.crateBreak', 'item.upgradePlug', 'weapon.explosionLargeNear', 'weapon.explosionLargeFar', 'weapon.casing.small', 'weapon.casing.medium', 'weapon.casing.large', 'weapon.casing.shell', 'weapon.explosionSmallNear', 'weapon.explosionSmallFar'):
    expected = json.loads(json.dumps(original_json[key]))
    expected['sounds'] = ['hbm:' + sound if isinstance(sound, str) else dict(sound, name='hbm:' + sound['name']) for sound in expected['sounds']]
    assert current_json[key] == expected, 'Only namespace prefixes may differ; original sample order and metadata must be preserved: ' + key
battery = 'src/main/resources/assets/hbm/sounds/tool/battery.ogg'
assert (root / battery).read_bytes() == (pristine / battery).read_bytes(), 'Battery audio must retain pristine bytes'
for sample in particle_resources['samples']:
    actual=root/sample['path']
    original_name=next((x['before'] for x in particle_resources['renames'] if x['after']==sample['path']),sample['path'])
    assert hashlib.sha256(actual.read_bytes()).hexdigest()==sample['sha256']
    assert actual.read_bytes()==(pristine/original_name).read_bytes()
print('PASS unchanged original battery audio', hashlib.sha256((root / battery).read_bytes()).hexdigest(), flush=True)
print('PASS fourteen sound entries preserve original sample order and metadata, changing only required hbm: prefixes', flush=True)
if a.legacy_jar:
    assert hashlib.sha256(a.legacy_jar.read_bytes()).hexdigest() == 'a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce', 'Unexpected legacy oracle jar'
with tempfile.TemporaryDirectory(prefix='block-sound-native-') as tmp:
    out = Path(tmp)
    fixture = out / 'com/hbm/main/MainRegistry.java'
    fixture.parent.mkdir(parents=True)
    fixture.write_text('package com.hbm.main; public final class MainRegistry { public static final MainRegistry instance = new MainRegistry(); public java.util.Random rand = new java.util.Random(); }\n')
    production = [src / rel for rel in ('com/hbm/blocks/ModSoundType.java', 'com/hbm/blocks/ModSoundTypes.java', 'com/hbm/main/ModSoundEvents.java', 'com/hbm/lib/RefStrings.java')]
    for f in production: print('PRODUCTION', f.relative_to(src), hashlib.sha256(f.read_bytes()).hexdigest(), flush=True)
    subprocess.run([str(a.java_home / 'bin/javac'), '-encoding', 'UTF-8', '-source', '8', '-target', '8', '-cp', a.classpath, '-d', tmp] + [str(f) for f in production] + [str(fixture), str(tests / 'BlockSoundNativeTest.java'), str(tests / 'ForgeEventNativeLauncher.java'), str(tests / 'LegacyBlockSoundOracle.java')], check=True)
    if a.legacy_jar:
        subprocess.run([str(a.java_home / 'bin/java'), '-cp', os.pathsep.join([tmp, a.classpath]), 'LegacyBlockSoundOracle', str(a.legacy_jar.resolve())], cwd=tmp, check=True)
    jar = out / 'block-sound-resources.jar'
    with zipfile.ZipFile(jar, 'w') as z:
        f = resources / 'assets/hbm/sounds.json'
        z.write(f, f.relative_to(resources).as_posix())
        f = root / battery
        z.write(f, f.relative_to(resources).as_posix())
        for entry in manifest:
            f = root / entry['after']
            z.write(f, f.relative_to(resources).as_posix())
        for sample in particle_resources['samples']:
            f=root/sample['path']; name=f.relative_to(resources).as_posix()
            if name not in z.namelist():z.write(f,name)
    subprocess.run([str(a.java_home / 'bin/java'), '-Djava.awt.headless=true', '-cp', os.pathsep.join([tmp, a.classpath]), 'ForgeEventNativeLauncher', 'BlockSoundNativeTest', str(resources), str(jar)], cwd=tmp, check=True)
