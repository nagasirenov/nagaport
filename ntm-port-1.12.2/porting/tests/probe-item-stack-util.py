#!/usr/bin/env python3
"""Compile/test the entire production ItemStackUtil against native Minecraft/Forge; no Gradle or Git required."""
import argparse
import hashlib
import json
import os
from pathlib import Path
import subprocess
import tempfile
ROOT = Path(__file__).resolve().parents[2]
p = argparse.ArgumentParser(description=__doc__)
p.add_argument('--java-home', type=Path)
p.add_argument('--classpath')
p.add_argument('--source-only', action='store_true')
a = p.parse_args()
manifest = json.loads(Path(__file__).with_name('item-stack-util-manifest.json').read_text(encoding='utf-8'))
source = ROOT / manifest['source']
digest = hashlib.sha256(source.read_text(encoding='utf-8').encode('utf-8')).hexdigest()
assert digest == manifest['reviewed_native_sha256'], 'Production utility differs from reviewed migration; re-review and refresh manifest.'
print('PASS reviewed complete production ItemStackUtil source digest (normalized newlines; no Git dependency)', flush=True)
if a.source_only:
    raise SystemExit(0)
if not a.java_home or not a.classpath:
    p.error('--java-home and --classpath required unless --source-only')
exe = '.exe' if os.name == 'nt' else ''
with tempfile.TemporaryDirectory(prefix='hbm-item-stack-util-') as tmp:
    subprocess.run([str(a.java_home / 'bin' / ('javac' + exe)), '-encoding', 'UTF-8', '-source', '8', '-target', '8',
                    '-cp', a.classpath, '-d', tmp, str(source), str(ROOT / 'porting/tests/ItemStackUtilMinecraftTest.java')], check=True)
    subprocess.run([str(a.java_home / 'bin' / ('java' + exe)), '-cp', os.pathsep.join([tmp, a.classpath]),
                    'ItemStackUtilMinecraftTest'], check=True, cwd=tmp)
