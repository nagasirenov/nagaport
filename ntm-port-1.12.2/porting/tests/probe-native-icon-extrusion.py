#!/usr/bin/env python3
"""Compare native buffer output with pinned original method, redirecting only its output sink."""
import argparse,hashlib,pathlib,subprocess,tempfile
p=argparse.ArgumentParser();p.add_argument('--classpath',required=True);p.add_argument('--java-home',type=pathlib.Path,required=True);p.add_argument('--legacy-client',type=pathlib.Path,required=True);a=p.parse_args()
if hashlib.sha256(a.legacy_client.read_bytes()).hexdigest()!='a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce':raise SystemExit('Refusing unexpected legacy client; require pinned official Minecraft 1.7.10 client.')
root=pathlib.Path(__file__).resolve().parents[2]
with tempfile.TemporaryDirectory(prefix='native-icon-extrusion-') as tmp:
 subprocess.run([str(a.java_home/'bin/javac'),'-encoding','UTF-8','-source','8','-target','8','-cp',a.classpath,'-d',tmp,str(root/'src/main/java/com/hbm/render/util/NativeIconExtrusion.java'),str(root/'porting/tests/NativeIconExtrusionTest.java')],check=True)
 import os
 subprocess.run([str(a.java_home/'bin/java'),'-cp',tmp+os.pathsep+a.classpath,'NativeIconExtrusionTest',str(a.legacy_client)],check=True)
