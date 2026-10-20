#!/usr/bin/env python3
"""Parse (not analyze/build) the complete CP85 production tree and verify finite dispatch closure."""
from pathlib import Path
import json,os,subprocess,tarfile,tempfile
H=Path(__file__).resolve().parent;REPO=H.parents[3];M=json.loads((H/'manifest.json').read_text());C=json.loads((H/'dispatch-closure.json').read_text())
J=Path(os.environ.get('JAVA8_HOME','/workspace/shared/nagaport-toolchain/java8'))
def run(args):return subprocess.check_output(list(map(str,args)),stderr=subprocess.STDOUT,text=True)
with tempfile.TemporaryDirectory(prefix='range-source-closure-') as td:
 d=Path(td);archive=d/'source.tar'
 with archive.open('wb') as out:subprocess.run(['git','-C',str(REPO),'archive',M['base_commit'],'ntm-port-1.12.2/src/main/java'],stdout=out,check=True)
 with tarfile.open(archive) as t:t.extractall(d/'tree',filter='data')
 root=d/'tree';paths=sorted((root/'ntm-port-1.12.2/src/main/java').rglob('*.java'));assert len(paths)==C['source_units']
 inputs=d/'inputs';inputs.write_text('\n'.join(map(str,paths))+'\n')
 run([J/'bin/javac','-cp',J/'lib/tools.jar','-d',d,H/'SourceClosureScan.java'])
 output=run([J/'bin/java','-Xmx1g','-cp',str(d)+':'+str(J/'lib/tools.jar'),'SourceClosureScan',inputs]);rows=[s.split('\t') for s in output.splitlines()]
 # Qualified declarations in this finite class closure use plain named superclasses.
 parents={r[1]:r[2] for r in rows if r[0]=='CLASS'}
 selected=set(C['selected_owners']);descendants=selected.copy()
 while True:
  n=descendants|{c for c,p in parents.items() if p in descendants}
  if n==descendants:break
  descendants=n
 assert sorted(descendants-selected)==C['local_descendants']
 ancestors=descendants.copy()
 for c in descendants:
  while c in parents:ancestors.add(parents[c]);c=parents[c]
 assert {c:p for c,p in parents.items() if c in ancestors}==C['parents']
 for c in ancestors:
  if c in parents:assert sum(r[0]=='CLASS' and r[1]==c for r in rows)==1,'ambiguous simple owner '+c
 ranges={r[1]:str(Path(r[2]).relative_to(root)) for r in rows if r[0]=='RANGE' and r[1] in ancestors}
 assert ranges==C['baseline_range_overrides']
 accesses=[r for r in rows if r[0]=='ACCESS'];assert len(accesses)==30
 assert sum(r[3]=='ASSIGNMENT' for r in accesses)==29
 assert [(r[1],r[3]) for r in accesses if r[3]!='ASSIGNMENT']==[('EntityGhost','MULTIPLY_ASSIGNMENT')]
 assert not any(r[0]=='DECL' for r in rows),'local field shadow would invalidate inherited-owner audit'
 actual=set()
 for r in accesses:
  assert r[1] in selected and r[2]=='<init>' and r[3] in ('ASSIGNMENT','MULTIPLY_ASSIGNMENT'),r
  actual.add((str(Path(r[4]).relative_to(root)),int(r[5])))
 assert actual=={(p,line) for p,line,_ in C['all_field_occurrences']}
 # Record native full-compile ownership attribution for exactly those finite positions.
 diagnostic=json.loads((H/'compiler-attribution.json').read_text())
 assert {(r['path'],r['line']) for r in diagnostic['diagnostics']}==actual
 assert all(r['message']=='renderDistanceWeight has private access in Entity' for r in diagnostic['diagnostics'])
 print('PASS parse-only 4,065 actual production units: 29 constructor writes + one Ghost read-modify-write, zero local field declarations, 4 local descendants, one pre-existing effective range override. Exact file/line pairing with 30 authoritative native compiler Entity-private-field diagnostics.')
