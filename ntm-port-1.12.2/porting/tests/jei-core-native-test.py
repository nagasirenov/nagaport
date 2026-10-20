#!/usr/bin/env python3
"""Compile the complete three-file JEI production core and exercise real target APIs.

Minecraft item/NBT classes and JEI ingredient/helper implementations are the actual
pinned binaries. API proxies observe GUI calls without starting OpenGL or a game.
No source slicing, replacement Minecraft classes or production stubs are used.
This is a scoped core test, not evidence that the whole mod compiles or runs.
"""
import argparse
import os
from pathlib import Path
import subprocess
import tempfile

parser = argparse.ArgumentParser()
parser.add_argument('--java-home', type=Path, required=True)
parser.add_argument('--classpath', required=True)
args = parser.parse_args()
def executable(name):
    for suffix in ('.exe', '') if os.name == 'nt' else ('', '.exe'):
        path = args.java_home / 'bin' / (name + suffix)
        if path.is_file():
            return str(path)
    raise FileNotFoundError('Missing Java executable: ' + name + ' under ' + str(args.java_home))
root = Path(__file__).resolve().parents[2]
core = root / 'src/main/java/com/hbm/handler/jei'
files = [core / name for name in (
    'MachineRecipeCategory.java', 'MachineRecipeView.java', 'NativeRecipeLookup.java')]
files.append(root / 'porting/tests/JeiCoreNativeTest.java')
with tempfile.TemporaryDirectory(prefix='jei-core-native-') as temporary:
    output = Path(temporary) / 'classes'
    output.mkdir()
    subprocess.run([
        executable('javac'), '-source', '8', '-target', '8',
        '-proc:none', '-implicit:none', '-encoding', 'UTF-8',
        '-cp', args.classpath, '-d', str(output)
    ] + [str(file) for file in files], check=True)
    subprocess.run([
        executable('java'), '-cp', str(output) + os.pathsep + args.classpath,
        'JeiCoreNativeTest'
    ], cwd=temporary, check=True)
