#!/usr/bin/env python3
"""Reproduce all-source before/after attribution without copying unchanged sources/assets."""
import json,os,pathlib,subprocess,tempfile
import source
H=source.HERE;R=source.ROOT
J=pathlib.Path(os.environ.get('JAVA8_HOME','/workspace/shared/nagaport-toolchain/java8'))
CP=pathlib.Path(os.environ.get('NATIVE_CP_FILE','/tmp/nagaport-ae2-native-classpath.txt'))
subprocess.run(['python3',str(H/'source.py'),'--strict-all'],check=True)
m=json.loads((H/'manifest.json').read_text());paths=sorted((R/'src/main/java').rglob('*.java'))
with tempfile.TemporaryDirectory(prefix='bounds-attribution-') as tmp:
 t=pathlib.Path(tmp);classes=t/'classes';classes.mkdir()
 subprocess.run([str(J/'bin/javac'),'-cp',str(J/'lib/tools.jar'),'-d',str(classes),str(H/'BoundScanner.java')],check=True)
 original=[]
 for p in paths:
  rel=str(p.relative_to(R));rec=m['files'].get(rel)
  if rec:
   f=t/'before'/rel;f.parent.mkdir(parents=True,exist_ok=True);f.write_bytes(source.verify(p.read_bytes(),rec).encode());original.append(f)
  else:original.append(p)
 for name,ps in [('before',original),('after',paths)]:
  inputs=t/'inputs';inputs.write_text('\n'.join(map(str,ps)))
  cmd=[str(J/'bin/java'),'-Xmx1800m','-cp',str(classes)+':'+str(J/'lib/tools.jar'),'BoundScanner',str(inputs),str(CP)]+(['after'] if name=='after' else [])
  with (H/(name+'.jsonl')).open('w') as out,(H/(name+'.log')).open('w') as err:subprocess.run(cmd,stdout=out,stderr=err,check=True)
  print((H/(name+'.log')).read_text().strip())
subprocess.run(['python3',str(H/'verify_attribution.py')],check=True)
