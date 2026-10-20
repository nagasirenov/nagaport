#!/usr/bin/env python3
"""Real production helper + native Forge/Minecraft + pinned actual original face bytecode."""
import argparse, hashlib, os, subprocess, tempfile
from pathlib import Path
p=argparse.ArgumentParser();p.add_argument('--java-home',type=Path,required=True);p.add_argument('--classpath',required=True);p.add_argument('--legacy-client',type=Path,required=True);a=p.parse_args()
root=Path(__file__).resolve().parents[2]
assert hashlib.sha256(a.legacy_client.read_bytes()).hexdigest()=='a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce','Unexpected original client artifact'
source=root/'src/main/java/com/hbm/render/block/nativeblock/ConveyorCuboidGeometry.java'
print('PRODUCTION',source.relative_to(root),hashlib.sha256(source.read_bytes()).hexdigest(),flush=True)
with tempfile.TemporaryDirectory(prefix='conveyor-cuboid-native-') as tmp:
 test=Path(tmp)/'ConveyorCuboidNativeTest.java';test.write_text((root/'porting/tests/conveyor-cuboid-native-test.java.in').read_text())
 subprocess.run([str(a.java_home/'bin/javac'),'-encoding','UTF-8','-source','8','-target','8','-cp',a.classpath,'-d',tmp,str(source),str(test)],check=True)
 subprocess.run([str(a.java_home/'bin/java'),'-Djava.awt.headless=true','-Xmx512m','-cp',tmp+os.pathsep+a.classpath,'ConveyorCuboidNativeTest',str(a.legacy_client)],cwd=tmp,check=True)
