#!/usr/bin/env python3
"""Portable standalone actual-key/native target probe; optional official legacy oracle."""
import argparse
import os
from pathlib import Path
import subprocess
import tempfile
p = argparse.ArgumentParser()
p.add_argument('--java-home', type=Path, required=True)
p.add_argument('--classpath', required=True)
p.add_argument('--legacy-client', type=Path)
a = p.parse_args()
root = Path(__file__).resolve().parents[2]
exe = '.exe' if os.name == 'nt' else ''
with tempfile.TemporaryDirectory(prefix='hbm-native-coordinates-') as out:
    subprocess.run([str(a.java_home / 'bin' / ('javac' + exe)), '-encoding', 'UTF-8', '-cp', a.classpath, '-d', out,
                    str(root / 'src/main/java/com/hbm/tileentity/machine/SolarMirrorPosition.java'),
                    str(root / 'porting/tests/NativeCoordinateMigrationTest.java')], check=True)
    command = [str(a.java_home / 'bin' / ('java' + exe)), '-cp', os.pathsep.join([out, a.classpath]), 'NativeCoordinateMigrationTest']
    if a.legacy_client: command.append(str(a.legacy_client.resolve()))
    subprocess.run(command, check=True, cwd=out)
