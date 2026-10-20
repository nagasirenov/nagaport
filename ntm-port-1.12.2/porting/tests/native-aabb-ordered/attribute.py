#!/usr/bin/env python3
"""Read-only javac attribution for all sources, original inverse and current view; no class emission."""
import gzip,json,os,pathlib,subprocess,tempfile
import source
H=source.HERE;R=source.ROOT
J=pathlib.Path(os.environ.get('JAVA8_HOME','/workspace/shared/nagaport-toolchain/java8'));CP=pathlib.Path(os.environ.get('NATIVE_CP_FILE','/tmp/nagaport-ae2-native-classpath.txt'))
subprocess.run(['python3',str(H/'source.py'),'--strict-all'],check=True)
m=json.loads((H/'manifest.json').read_text());paths=sorted((R/'src/main/java').rglob('*.java'))
with tempfile.TemporaryDirectory(prefix='ordered-aabb-attribution-') as tmp:
 t=pathlib.Path(tmp);classes=t/'classes';classes.mkdir()
 subprocess.run([str(J/'bin/javac'),'-cp',str(J/'lib/tools.jar'),'-d',str(classes),str(H/'OrderedAabbScanner.java')],check=True)
 original=[]
 for p in paths:
  rel=str(p.relative_to(R));rec=m['files'].get(rel)
  if rec:
   f=t/'before'/rel;f.parent.mkdir(parents=True,exist_ok=True);f.write_bytes(source.verify(p.read_bytes(),rec).encode());original.append(f)
  else:original.append(p)
 for name,ps in [('before',original),('after',paths)]:
  inputs=t/'inputs';inputs.write_text('\n'.join(map(str,ps)));diags=t/'diagnostics.jsonl'
  cmd=[str(J/'bin/java'),'-Xmx2400m','-cp',str(classes)+':'+str(J/'lib/tools.jar'),'OrderedAabbScanner',str(inputs),str(CP),name,str(diags)]
  with (H/('proof/'+name+'.jsonl')).open('w') as out,(H/('proof/'+name+'.log')).open('w') as err:subprocess.run(cmd,stdout=out,stderr=err,check=True)
  with (H/('proof/'+name+'-diagnostics.jsonl.gz')).open('wb') as f:
   with gzip.GzipFile(filename='',mode='wb',fileobj=f,mtime=0) as z:z.write(diags.read_bytes())
  print((H/('proof/'+name+'.log')).read_text().strip())
subprocess.run(['python3',str(H/'verify_attribution.py')],check=True)
