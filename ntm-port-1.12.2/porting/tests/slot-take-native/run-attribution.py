#!/usr/bin/env python3
"""Reproduce exact sealed CP81 source closure without Gradle or production class generation.
Usage: run-attribution.py JAVA_HOME CLASSPATH_FILE OUTPUT_DIR
"""
from pathlib import Path
import sys,subprocess,hashlib,zipfile,json,tempfile
HERE=Path(__file__).resolve().parent;ROOT=HERE.parents[3];MODULE=ROOT/'ntm-port-1.12.2'
JAVA,CPF,OUT=map(lambda s:Path(s).resolve(),sys.argv[1:]);OUT.mkdir(parents=True,exist_ok=True)
M=json.loads((HERE/'manifest.json').read_text());selected={f['path'] for f in M['files']}
def run(args):
 p=subprocess.run(list(map(str,args)),stdout=subprocess.PIPE,stderr=subprocess.STDOUT,text=True)
 print(p.stdout,end='');assert p.returncode==0,p.stdout;return p.stdout
def blob(data):return hashlib.sha1(b'blob '+str(len(data)).encode()+b'\0'+data).hexdigest()
run([sys.executable,HERE/'source-proof.py'])
# Verify every Java source against the actual sealed Git tree, not a mutable external directory.
rows=subprocess.check_output(['git','-C',str(ROOT),'ls-tree','-r',M['baseline'],'--','ntm-port-1.12.2/src/main/java']).decode().splitlines()
inputs={row.split('\t',1)[1]:row.split()[2] for row in rows if row.endswith('.java')};assert len(inputs)==4065
classes=OUT/'compiler';classes.mkdir(exist_ok=True)
run([JAVA/'bin/javac','-cp',JAVA/'lib/tools.jar','-d',classes,HERE/'SlotAttribution.java'])
with tempfile.TemporaryDirectory(prefix='slot-attribution-baseline-') as tmp,zipfile.ZipFile(HERE/'baseline.zip') as z:
 baseline=Path(tmp)
 for path,expected in inputs.items():
  actual=(ROOT/path).read_bytes();original=z.read(path) if path in selected else actual
  assert blob(original)==expected,path+' is not exact CP81 baseline'
  target=baseline/Path(path).relative_to('ntm-port-1.12.2');target.parent.mkdir(parents=True,exist_ok=True)
  if path in selected:target.write_bytes(original)
  else:target.symlink_to(ROOT/path)
 for name,tree in [('before',baseline),('after',MODULE)]:
  log=run([JAVA/'bin/java','-Xmx5g','-cp',str(classes)+':'+str(JAVA/'lib/tools.jar'),'SlotAttribution',tree,CPF,OUT/name]);(OUT/(name+'.log')).write_text(log)
run([sys.executable,HERE/'check-attribution.py',OUT/'before',OUT/'after',OUT/'attribution.json'])
print('PASS exact 4065-source baseline/after attribution; no class emission, source exclusions or full Gradle build')
