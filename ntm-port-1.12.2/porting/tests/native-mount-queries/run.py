#!/usr/bin/env python3
import argparse,pathlib,json,hashlib,subprocess,tempfile,os
h=pathlib.Path(__file__).resolve().parent; root=h.parents[3];m=json.loads((h/'manifest.json').read_text())
p=argparse.ArgumentParser();p.add_argument('--java-home',type=pathlib.Path,required=True);p.add_argument('--classpath-file',type=pathlib.Path,required=True);p.add_argument('--source-only',action='store_true');a=p.parse_args()
def sha(b):return hashlib.sha256(b).hexdigest()
rows=[x.split('\t') for x in (h/'attribution.tsv').read_text().splitlines()]
count=0
for rel,meta in m['files'].items():
 raw=(root/rel).read_bytes();assert sha(raw)==meta['after_sha256'],rel
 s=raw.decode(); es=[e for e in m['edits'] if e['path']==rel];shift=0; located=[]
 for e in es:
  start=e['start']+shift;end=start+len(e['after']);assert s[start:end]==e['after'];located.append((start,end,e['before']));shift+=len(e['after'])-(e['end']-e['start'])
 for start,end,before in reversed(located):s=s[:start]+before+s[end:]
 assert sha(s.encode())==meta['before_sha256'],rel
 for e in es:
  # Scanner offsets use UTF-16 code units, Python manifest offsets Unicode characters.
  start=len(s[:e['start']].encode('utf-16-le'))//2;end=len(s[:e['end']].encode('utf-16-le'))//2
  matches=[r for r in rows if r[0].endswith('/'+rel) and ((e['kind']=='riding-field-read' and int(r[2])==end and r[3]=='ridingEntity') or (e['kind']=='passenger-presence' and int(r[1])==start and r[3]=='riddenByEntity'))]
  assert len(matches)==1,(rel,e,matches);count+=1
  # Every selected changed token detects stale field or an unrelated replacement.
  for bad in (e['before'],e['after']+'/* drift */'):
   assert bad!=e['after']
   pos=e['start']+sum(len(x['after'])-(x['end']-x['start']) for x in es if x['start']<e['start'])
   changed=raw.decode();mutant=(changed[:pos]+bad+changed[pos+len(e['after']):]).encode();assert sha(mutant)!=meta['after_sha256']
print('PASS',len(m['files']),'whole-file exact inverses;',count,'receiver-attributed selections;',count*2,'changed-token mutation controls')
if a.source_only:raise SystemExit
cp=a.classpath_file.read_text().strip()
with tempfile.TemporaryDirectory(prefix='native-mount-query-') as tmp:
 d=pathlib.Path(tmp)
 # Extract actual complete caller boolean expressions, not restated equivalents.
 station=(root/'ntm-port-1.12.2/src/main/java/com/hbm/tileentity/machine/TileEntityOrbitalStation.java').read_text()
 import re
 has=re.search(r'hasRider = (hasDocked && docked\.isBeingRidden\(\));',station).group(1)
 reject=re.search(r'if\((docked == null \|\| docked\.isBeingRidden\(\))\) return;',station).group(1)
 orbital=(root/'ntm-port-1.12.2/src/main/java/com/hbm/dim/orbit/OrbitalStation.java').read_text();skip=re.search(r'if\((rocket\.isBeingRidden\(\))\) continue;',orbital).group(1)
 (d/'Queries.java').write_text('import net.minecraft.entity.Entity; class Queries {static boolean has(boolean hasDocked,Entity docked){return '+has+';}static boolean reject(Entity docked){return '+reject+';}static boolean skip(Entity rocket){return '+skip+';}}')
 subprocess.run([str(a.java_home/'bin/javac'),'-cp',cp,'-d',str(d),str(h/'Probe.java'),str(d/'Queries.java')],check=True)
 subprocess.run([str(a.java_home/'bin/java'),'-cp',str(d)+os.pathsep+cp,'Probe',str(a.classpath_file)],cwd=d,check=True)

 # A compiled but wrong native predicate must fail the same production-expression tests.
 original=(d/'Queries.java').read_text();(d/'Queries.java').write_text(original.replace('isBeingRidden()', 'isRiding()'))
 subprocess.run([str(a.java_home/'bin/javac'),'-cp',cp,'-d',str(d),str(d/'Queries.java')],check=True)
 mutant=subprocess.run([str(a.java_home/'bin/java'),'-cp',str(d)+os.pathsep+cp,'Probe',str(a.classpath_file)],cwd=d,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,text=True)
 assert mutant.returncode!=0 and 'complete production multipassenger queries' in mutant.stdout,mutant.stdout
 print('PASS runtime predicate mutant: being a passenger does not mean having passengers')
