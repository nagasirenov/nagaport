#!/usr/bin/env python3
"""One grouped full-production BufferUtil native probe. Java 8, no fake platform or Git required."""
import argparse
import importlib.util
import os
from pathlib import Path
import subprocess
import tempfile

ROOT = Path(__file__).resolve().parents[2]

def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--java-home', type=Path)
    parser.add_argument('--classpath', help='Actual native Minecraft/Forge/Netty classpath using this OS separator')
    parser.add_argument('--source-only', action='store_true')
    args = parser.parse_args()
    spec = importlib.util.spec_from_file_location('buffer_util_native_source', Path(__file__).with_name('check-buffer-util-native-source.py'))
    audit = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(audit)
    audit.main()
    if args.source_only:
        return
    if not args.java_home or not args.classpath:
        parser.error('--java-home and --classpath required unless --source-only')
    exe = '.exe' if os.name == 'nt' else ''
    java_home = args.java_home
    if not (java_home / 'bin' / ('javac' + exe)).is_file() and (java_home.parent / 'bin' / ('javac' + exe)).is_file():
        java_home = java_home.parent
    for relative in audit.FILES:
        print('PRODUCTION', relative, audit.sha((ROOT / relative).read_bytes().decode('utf-8')), flush=True)
    with tempfile.TemporaryDirectory(prefix='hbm-buffer-util-native-') as temporary:
        subprocess.run([str(java_home / 'bin' / ('javac' + exe)), '-encoding', 'UTF-8', '-source', '8', '-target', '8',
                        '-cp', args.classpath, '-d', temporary,
                        str(ROOT / 'src/main/java/com/hbm/util/BufferUtil.java'),
                        str(ROOT / 'porting/tests/BufferUtilNativeCodecTest.java')], check=True)
        subprocess.run([str(java_home / 'bin' / ('java' + exe)), '-cp', os.pathsep.join([temporary, args.classpath]),
                        'BufferUtilNativeCodecTest'], check=True, cwd=temporary)

if __name__ == '__main__':
    main()
