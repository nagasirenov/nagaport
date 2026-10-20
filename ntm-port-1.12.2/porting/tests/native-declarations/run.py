from pathlib import Path
import argparse,json,hashlib,subprocess,tempfile,os
p=argparse.ArgumentParser();p.add_argument('--source-root',type=Path,required=True);p.add_argument('--java-home',type=Path,required=True);p.add_argument('--classpath',required=True);p.add_argument('--legacy-client',type=Path,required=True);a=p.parse_args();t=Path(__file__).resolve().parent
assert hashlib.sha256(a.legacy_client.read_bytes()).hexdigest()=='a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce'
for f,e in json.loads((t/'mapping.json').read_text()).items():
 current=(a.source_root/f).read_bytes();assert hashlib.sha256(current).hexdigest()==e['after_sha256'];before=current
 for v in reversed(e['edits']):
  x=v['new'].encode();y=v['old'].encode();assert before.count(x)==v['count'],(f,v);before=before.replace(x,y)
 assert hashlib.sha256(before).hexdigest()==e['before_sha256'];forward=before
 for v in e['edits']:forward=forward.replace(v['old'].encode(),v['new'].encode())
 assert forward==current
print('PASS nine exact whole-source forward/reverse API mappings; not whole-owner compilation',flush=True)
with tempfile.TemporaryDirectory(prefix='native-declarations-') as d:
 subprocess.run([str(a.java_home/'bin/javac'),'-source','8','-target','8','-cp',a.classpath,'-d',d,str(t/'NativeDeclarationsTest.java')],check=True)
 subprocess.run([str(a.java_home/'bin/java'),'-cp',d+os.pathsep+a.classpath,'NativeDeclarationsTest',str(a.legacy_client)],cwd=d,check=True)
