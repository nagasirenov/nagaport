#!/usr/bin/env python3
"""Run full production registration queue + deferred model listener against genuine Forge.
No replacement platform registry, manually injected block-item map, Git, network, or downloads.
Provider item/model registration are explicitly controlled boundaries in the Java fixture.
"""
import argparse
import hashlib
import os
from pathlib import Path
import subprocess
import tempfile

ROOT = Path(__file__).resolve().parents[2]
p = argparse.ArgumentParser(description=__doc__)
p.add_argument('--java-home', required=True, type=Path)
p.add_argument('--classpath', required=True)
a = p.parse_args()
exe = '.exe' if os.name == 'nt' else ''
if not (a.java_home / 'bin' / ('javac' + exe)).is_file() and (a.java_home.parent / 'bin' / ('javac' + exe)).is_file():
    a.java_home = a.java_home.parent
production = [
    'src/main/java/com/hbm/registry/GameRegistry.java',
    'src/main/java/com/hbm/lib/RefStrings.java',
    'src/main/java/com/hbm/render/item/ItemRenderBase.java',
    'src/main/java/com/hbm/render/tileentity/IItemRendererProvider.java',
    'src/main/java/com/hbm/render/item/nativeitem/callback/DeferredItemModelRegistration.java',
]
with tempfile.TemporaryDirectory(prefix='hbm-deferred-item-registry-') as directory:
    files = []
    for relative in production:
        path = ROOT / relative
        print('FULL PRODUCTION', relative, hashlib.sha256(path.read_bytes()).hexdigest(), flush=True)
        files.append(str(path))
    files.extend(str(ROOT / 'porting/tests' / name) for name in [
        'ForgeEventNativeLauncher.java', 'DeferredItemRegistryNativeTest.java'])
    subprocess.run([str(a.java_home / 'bin' / ('javac' + exe)), '-source', '8', '-target', '8',
                    '-encoding', 'UTF-8', '-cp', a.classpath, '-d', directory] + files, check=True)
    subprocess.run([str(a.java_home / 'bin' / ('java' + exe)), '-cp', directory + os.pathsep + a.classpath,
                    'ForgeEventNativeLauncher', 'DeferredItemRegistryNativeTest'], check=True, cwd=directory)
