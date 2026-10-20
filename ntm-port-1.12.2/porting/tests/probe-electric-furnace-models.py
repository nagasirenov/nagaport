#!/usr/bin/env python3
"""Native electric-furnace model/registry/resource probe, without Gradle or a Git baseline."""
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
RESOURCE_ROOT = ROOT / 'src/main/resources'


def audit():
    manifest = json.loads((TESTS / 'electric-furnace-model-manifest.json').read_text(encoding='utf-8'))
    for entry in manifest['files']:
        data = (ROOT / entry['path']).read_bytes()
        assert hashlib.sha256(data).hexdigest() == entry['sha256'], 'Reviewed model source/resource changed: ' + entry['path']
    expected = {entry['path'] for entry in manifest['files'] if entry['path'].endswith('.json')}
    actual = {p.relative_to(ROOT).as_posix() for folder in ('blockstates', 'models/block', 'models/item')
              for p in (RESOURCE_ROOT / 'assets/hbm' / folder).glob('*machine_electric_furnace*.json')}
    assert actual == expected and len(expected) == 67, 'Unexpected/missing furnace model assets'
    print('PASS frozen complete model handler, 67 native JSON assets, five unchanged source PNG hashes', flush=True)
    return manifest


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--java-home', type=Path)
    parser.add_argument('--classpath')
    parser.add_argument('--source-only', action='store_true')
    parser.add_argument('--legacy-client-jar', type=Path, help='Optional official 1.7.10 client UV oracle; binary is not distributed.')
    args = parser.parse_args()
    manifest = audit()
    if args.source_only:
        return
    if not args.java_home or not args.classpath:
        parser.error('--java-home and --classpath required unless --source-only')
    suffix = '.exe' if os.name == 'nt' else ''
    javac = str(args.java_home / 'bin' / ('javac' + suffix))
    java = str(args.java_home / 'bin' / ('java' + suffix))
    with tempfile.TemporaryDirectory(prefix='electric-furnace-models-') as temporary:
        work = Path(temporary)
        jar = work / 'furnace-assets.zip'
        with zipfile.ZipFile(jar, 'w') as archive:
            for entry in manifest['files']:
                path = ROOT / entry['path']
                if entry['path'].startswith('src/main/resources/'):
                    archive.writestr(path.relative_to(RESOURCE_ROOT).as_posix(), path.read_bytes())
        sources = [ROOT / 'src/main/java/com/hbm/main/ElectricFurnaceModelHandler.java',
                   ROOT / 'src/main/java/com/hbm/lib/RefStrings.java', TESTS / 'ElectricFurnaceModelNativeTest.java']
        subprocess.run([javac, '-encoding', 'UTF-8', '-source', '8', '-target', '8', '-cp', args.classpath,
                        '-d', str(work)] + [str(path) for path in sources], check=True)
        subprocess.run([java, '-Djava.awt.headless=true', '-cp', str(work) + os.pathsep + args.classpath,
                        'ElectricFurnaceModelNativeTest', str(RESOURCE_ROOT), str(jar)], cwd=work, check=True)
        if args.legacy_client_jar:
            assert hashlib.sha1(args.legacy_client_jar.read_bytes()).hexdigest() == manifest['legacy_uv_oracle']['sha1'], 'Not the pinned official 1.7.10 client'
            subprocess.run([javac, '-encoding', 'UTF-8', '-source', '8', '-target', '8', '-cp', args.classpath,
                            '-d', str(work), str(TESTS / 'ElectricFurnaceLegacyFaceOracle.java')], check=True)
            subprocess.run([java, '-Djava.awt.headless=true', '-cp', os.pathsep.join([str(work), str(args.legacy_client_jar), args.classpath]),
                            'furnaceoracle.ElectricFurnaceLegacyFaceOracle'], cwd=work, check=True)


if __name__ == '__main__':
    main()
