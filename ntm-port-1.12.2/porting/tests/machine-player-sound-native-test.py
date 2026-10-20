#!/usr/bin/env python3
"""Bounded real-native sound registry/resource checks; no Gradle or Git baseline needed."""
import argparse
import hashlib
import json
import os
from pathlib import Path
import subprocess
import tempfile
import zipfile

ROOT = Path(__file__).resolve().parents[2]
TESTS = ROOT / 'porting/tests'

def audit():
    manifest = json.loads((TESTS / 'machine-player-sound-manifest.json').read_text(encoding='utf-8'))
    sha = lambda b: hashlib.sha256(b).hexdigest()
    registry = manifest['registry']
    data = (ROOT / registry['path']).read_bytes()
    assert sha(data) == registry['migrated_sha256'], 'Unexpected registry edits'
    text = data.decode('utf-8')
    for replacement in registry['reverse_replacements']:
        assert text.count(replacement['after']) == 1
        text = text.replace(replacement['after'], replacement['before'])
    assert sha(text.encode('utf-8')) == registry['original_sha256'], 'Registry reversal is not original full file'
    sounds = manifest['sounds_json']
    assert sha((ROOT / sounds['path']).read_bytes()) == sounds['sha256'], 'sounds.json changed'
    # Stored names, not exists(), detect old spellings on case-insensitive hosts.
    stored = {p.relative_to(ROOT).as_posix(): p for p in (ROOT / 'src/main/resources/assets/hbm/sounds').rglob('*') if p.is_file()}
    assert len(manifest['renames']) == 6
    for item in manifest['renames']:
        assert item['after'] == item['before'].lower()
        assert item['before'] not in stored, 'Old stored name remains: ' + item['before']
        assert item['after'] in stored, 'Canonical stored name missing: ' + item['after']
        assert sha(stored[item['after']].read_bytes()) == item['sha256'], 'OGG bytes changed'
    print('PASS full-file registry reversal, unchanged sounds.json, six exact stored lowercase names and original hashes')
    return manifest, stored

def main():
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument('--classpath')
    p.add_argument('--java-home', type=Path)
    p.add_argument('--source-only', action='store_true')
    a = p.parse_args()
    manifest, stored = audit()
    if a.source_only:
        return
    if not a.classpath or not a.java_home:
        p.error('--classpath and --java-home required unless --source-only')
    suffix = '.exe' if os.name == 'nt' else ''
    with tempfile.TemporaryDirectory(prefix='machine-player-sound-') as name:
        tmp = Path(name)
        source = tmp / 'MachinePlayerSoundNativeTest.java'
        source.write_text((TESTS / 'machine-player-sound-native-test.java.in').read_text(encoding='utf-8'), encoding='utf-8')
        tsv = tmp / 'resources.tsv'
        prefix = 'src/main/resources/assets/hbm/'
        tsv.write_text(''.join(e['before'][len(prefix):] + '\t' + e['after'][len(prefix):] + '\t' + e['sha256'] + '\n' for e in manifest['renames']), encoding='utf-8')
        old_zip, new_zip = tmp / 'historical.zip', tmp / 'canonical.zip'
        with zipfile.ZipFile(old_zip, 'w') as old, zipfile.ZipFile(new_zip, 'w') as new:
            for e in manifest['renames']:
                data = stored[e['after']].read_bytes()
                old.writestr(e['before'][len('src/main/resources/'):], data)
                new.writestr(stored[e['after']].relative_to(ROOT / 'src/main/resources').as_posix(), data)
        subprocess.run([str(a.java_home / 'bin' / ('javac' + suffix)), '-encoding', 'UTF-8', '-source', '8', '-target', '8', '-cp', a.classpath, '-d', str(tmp), str(source), str(ROOT / manifest['registry']['path']), str(ROOT / 'src/main/java/com/hbm/lib/RefStrings.java')], check=True)
        subprocess.run([str(a.java_home / 'bin' / ('java' + suffix)), '-Djava.awt.headless=true', '-cp', str(tmp) + os.pathsep + a.classpath, 'MachinePlayerSoundNativeTest', str(ROOT / 'src/main/resources'), str(tsv), str(old_zip), str(new_zip)], cwd=tmp, check=True)
    print('PASS real-native sound registry and resource-pack lookup; not audio playback/reload, full-mod or gameplay acceptance')

if __name__ == '__main__':
    main()
