#!/usr/bin/env python3
import argparse,pathlib,tempfile,subprocess,os,hashlib
p=argparse.ArgumentParser();p.add_argument('--java-home',type=pathlib.Path,required=True);p.add_argument('--classpath',required=True);p.add_argument('--legacy-client',type=pathlib.Path,required=True);a=p.parse_args();h=pathlib.Path(__file__).resolve().parent;m=h.parents[2]
assert hashlib.sha256(a.legacy_client.read_bytes()).hexdigest()=='a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce'
with tempfile.TemporaryDirectory(prefix='boss-marker-native-') as d:
 subprocess.run([str(a.java_home/'bin/javac'),'-cp',a.classpath,'-d',d,str(m/'src/main/java/com/hbm/entity/boss/INtmBossDisplay.java'),str(m/'src/main/java/com/hbm/render/util/ClientBossDisplay.java'),str(h/'BossMarkerNativeTest.java'),str(h/'BossDisplayNativeTest.java')],check=True)
 subprocess.run([str(a.java_home/'bin/java'),'-cp',d+os.pathsep+a.classpath,'BossMarkerNativeTest'],check=True,cwd=d)
 subprocess.run([str(a.java_home/'bin/java'),'-cp',d+os.pathsep+a.classpath,'BossDisplayNativeTest',str(a.legacy_client)],check=True,cwd=d)
