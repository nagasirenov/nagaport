#!/usr/bin/env python3
"""Run bounded tool/weapon attribute engine evidence; no build/download/cache mutation."""
from pathlib import Path
import argparse,hashlib,json,os,subprocess,tempfile,zipfile
HERE=Path(__file__).resolve().parent
p=argparse.ArgumentParser()
p.add_argument('--java-home',type=Path,default=Path('/workspace/shared/nagaport-toolchain/java8'))
p.add_argument('--classpath-file',type=Path,default=Path('/workspace/shared/nagaport-toolchain/native-classpath.txt'))
p.add_argument('--legacy-client',type=Path,default=Path('/tmp/nagaport-legacy-client.jar'))
p.add_argument('--original-mod',type=Path,default=Path('/workspace/shared/nagaport/mods1710/HBM-NTM-.1.0.27_X5645_H261.jar'))
p.add_argument('--forge-userdev',type=Path,default=Path('/tmp/nagaport-forge1614-userdev.jar'))
a=p.parse_args(); cp=a.classpath_file.read_text().strip(); logs=[]
for path,expected in [(a.legacy_client,'a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce'),(a.original_mod,'3f5629077628b5956ff7ff4e254c25ef0e1bc51f5b6668fde77badd5f784f67b'),(a.forge_userdev,'6ed364c0defdb481a41ef2d99c02d382f16fc42a6ea0ea1c2b849848d8466e91')]:
 assert hashlib.sha256(path.read_bytes()).hexdigest()==expected,str(path)
def run(cmd,cwd):
 r=subprocess.run(list(map(str,cmd)),cwd=cwd,capture_output=True,text=True);logs.append(r.stdout+r.stderr)
 (HERE/'runtime-oracle.log').write_text(''.join(logs))
 if r.returncode:raise RuntimeError(r.stdout+r.stderr)
 print(r.stdout,end='');return r.stdout
with tempfile.TemporaryDirectory(prefix='native-tool-engine-') as tmp:
 d=Path(tmp);unsigned=d/'legacy-unsigned.jar'
 with zipfile.ZipFile(a.legacy_client) as src,zipfile.ZipFile(unsigned,'w') as out:
  for e in src.infolist():
   if e.filename.upper().startswith('META-INF/') and e.filename.upper().endswith(('.SF','.RSA','.DSA')):continue
   out.writestr(e,src.read(e.filename))
 run([a.java_home/'bin/javac','-source','8','-target','8','-cp',cp,'-d',d,HERE/'NativeToolAttributeEngineTest.java',HERE/'OriginalToolPayloadOracle.java'],d)
 run([a.java_home/'bin/java','-cp',str(d)+os.pathsep+cp,'NativeToolAttributeEngineTest',unsigned],d)
 run([a.java_home/'bin/java','-cp',str(d)+os.pathsep+cp,'OriginalToolPayloadOracle',unsigned,a.original_mod,a.forge_userdev,HERE/'original-payloads.json'],d)
(HERE/'runtime-inputs.json').write_text(json.dumps({str(x):hashlib.sha256(x.read_bytes()).hexdigest() for x in [a.legacy_client,a.original_mod,a.forge_userdev,HERE/'NativeToolAttributeEngineTest.java',HERE/'OriginalToolPayloadOracle.java',Path(next(x for x in cp.split(':') if '/forgeBin-' in x))]},indent=2)+'\n')
