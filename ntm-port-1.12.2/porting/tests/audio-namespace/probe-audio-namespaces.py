#!/usr/bin/env python3
"""Verify migrated production resources and run native folder/JAR sound resolution.
No fixed checkout paths, downloads, production mutations, or audio hardware required.
"""
import argparse, hashlib, json, os, shutil, subprocess, tempfile, zipfile
from pathlib import Path
p=argparse.ArgumentParser(description=__doc__)
p.add_argument('--module',required=True,type=Path,help='Migrated ntm-port-1.12.2 module')
p.add_argument('--java-home',required=True,type=Path,help='JDK 8 home')
p.add_argument('--classpath',required=True,help='Native 1.12.2 dependency classpath')
p.add_argument('--original',type=Path,help='Pristine original module; defaults to module sibling ntm-original-pristine')
a=p.parse_args();module=a.module.resolve();original=(a.original or module.parent/'ntm-original-pristine').resolve();here=Path(__file__).resolve().parent
m=json.loads((here/'manifest.json').read_text(encoding='utf-8'));base=module/'src/main/resources';j=base/'assets/hbm/sounds.json';sha=lambda b:hashlib.sha256(b).hexdigest()
raw=j.read_bytes();assert sha(raw)==m['result_json_sha256'],'migrated production JSON differs from manifest'
text=raw.decode('utf-8');restored=text;shift=0;spans=[]
for e in sorted(m['changes'],key=lambda e:e['start']):
 s=e['start']+shift;end=s+len(e['new_token']);assert text[s:end]==e['new_token'],'namespace token mismatch'
 spans.append((s,end,e['old_token']));shift+=len(e['new_token'])-len(e['old_token'])
for start,end,old in reversed(spans):restored=restored[:start]+old+restored[end:]
assert sha(restored.encode('utf-8'))==m['baseline_json_sha256'],'JSON inversion not byte-exact CP37'
before=json.loads(restored);after=json.loads(text)
assert len(m['changes'])==456 and len(m['renames'])==225 and len(m['assets'])==593
assert len({e['event'] for e in m['changes']})==318
for e in m['changes']:
 sample=after[e['event']]['sounds'][e['index']]
 if isinstance(sample,dict):
  assert sample.get('type','file')=='file' and sample['name']==e['new'];sample['name']=e['old']
 else:assert sample==e['new'];after[e['event']]['sounds'][e['index']]=e['old']
assert after==before,'event names, categories, flags or other JSON metadata changed'
assert m['event_references_unchanged']==[]
for event,index,name in m['unresolved']:
 sample=json.loads(text)[event]['sounds'][index];assert (sample['name'] if isinstance(sample,dict) else sample)==name
assert m['unresolved']==[['fm.clap',0,'clap'],['fm.mug',0,'mug'],['fm.sample',0,'sample']]
# Walk actual names to distinguish case-only names even on Windows filesystems.
actual={f.relative_to(base).as_posix() for f in (base/'assets/hbm/sounds').rglob('*.ogg')}
expected={e['target'] for e in m['assets']}
assert actual==expected,'actual OGG names/additions/removals differ from manifest'
assert len({p.lower() for p in actual})==len(actual),'case-folded OGG collision'
for e in m['assets']:
 current=base/e['target'];pristine=original/e['original']
 assert sha(current.read_bytes())==e['sha256'] and current.stat().st_size==e['size'],'migrated asset hash mismatch: '+e['target']
 assert sha(pristine.read_bytes())==e['sha256'],'pristine asset hash mismatch: '+e['original']
print('PASS 456 token edits / 318 events, byte-exact JSON inversion, unchanged metadata, 225 names, all 593 pristine/current OGG hashes, zero collisions',flush=True)
# Resolve classpath entries before changing working directory; safe on Windows too.
cp=os.pathsep.join(str(Path(x).resolve()) for x in a.classpath.split(os.pathsep) if x)
java=a.java_home.resolve()/'bin'/('java.exe' if os.name=='nt' else 'java');javac=a.java_home.resolve()/'bin'/('javac.exe' if os.name=='nt' else 'javac')
with tempfile.TemporaryDirectory(prefix='audio-namespace-native-') as tmp:
 out=Path(tmp);resources=out/'resources';classes=out/'classes';classes.mkdir();(resources/'assets/hbm').mkdir(parents=True)
 shutil.copy2(j,resources/'assets/hbm/sounds.json');shutil.copytree(base/'assets/hbm/sounds',resources/'assets/hbm/sounds')
 jar=out/'resources.jar'
 with zipfile.ZipFile(jar,'w',compression=zipfile.ZIP_STORED) as z:
  for f in resources.rglob('*'):
   if f.is_file():z.write(f,f.relative_to(resources).as_posix())
 sources=[here/'AudioNamespaceNativeTest.java',module/'porting/tests/ForgeEventNativeLauncher.java',module/'src/main/java/com/hbm/sound/SoundCategoryResolver.java']
 subprocess.run([str(javac),'-encoding','UTF-8','-source','8','-target','8','-cp',cp,'-d',str(classes)]+list(map(str,sources)),check=True)
 subprocess.run([str(java),'-Djava.awt.headless=true','-cp',str(classes)+os.pathsep+cp,'ForgeEventNativeLauncher','AudioNamespaceNativeTest',str(resources),str(jar)],cwd=out,check=True)
print('PASS portable migrated-module probe; temporary artifacts removed, no audio hardware or full-client claim',flush=True)
