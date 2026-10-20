#!/usr/bin/env python3
"""Compile the complete production inventory base against real Minecraft/Forge."""
import argparse
import os
from pathlib import Path
import subprocess
import tempfile
p = argparse.ArgumentParser()
p.add_argument('--java-home', type=Path, required=True)
p.add_argument('--classpath', required=True)
a = p.parse_args()
root = Path(__file__).resolve().parents[2]
exe = '.exe' if os.name == 'nt' else ''
subprocess.run([__import__('sys').executable, str(root / 'porting/tests/check-inventory-boundary.py')], check=True)
with tempfile.TemporaryDirectory(prefix='hbm-inventory-boundary-') as out:
    subprocess.run([str(a.java_home / 'bin' / ('javac' + exe)), '-encoding', 'UTF-8', '-cp', a.classpath, '-d', out,
                    str(root / 'src/main/java/com/hbm/tileentity/TileEntityInventoryBase.java'),
                    str(root / 'porting/tests/InventoryBoundaryMinecraftTest.java')], check=True)
    subprocess.run([str(a.java_home / 'bin' / ('java' + exe)), '-cp', os.pathsep.join([out, a.classpath]),
                    'InventoryBoundaryMinecraftTest'], check=True, cwd=out)
