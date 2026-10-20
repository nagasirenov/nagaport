#!/usr/bin/env python3
"""Full-source javac attribution against real native classpath; not a build-success claim."""
import argparse,json,subprocess,tempfile
from pathlib import Path
import check_source
H=check_source.HERE;R=check_source.ROOT
p=argparse.ArgumentParser();p.add_argument('--java-home',type=Path,required=True);p.add_argument('--classpath',required=True);a=p.parse_args()
m=json.loads((H/'manifest.json').read_text());check_source.check()
def run(cmd,**kw):return subprocess.run(list(map(str,cmd)),check=True,**kw)
with tempfile.TemporaryDirectory(prefix='ntm-world-increment-') as t:
 t=Path(t);classes=t/'classes';classes.mkdir();cpf=t/'cp';cpf.write_text(a.classpath);inputs=t/'inputs'
 oldscanner=H/'HeightScanner.java'
 run([a.java_home/'bin/javac','-cp',a.java_home/'lib/tools.jar','-d',classes,oldscanner,H/'DestinationScanner.java'])
 def scan(name,paths):
  inputs.write_text('\n'.join(map(str,paths)));r=run([a.java_home/'bin/java','-Xmx1800m','-cp',str(classes)+':'+str(a.java_home/'lib/tools.jar'),name,inputs,cpf],stdout=subprocess.PIPE,stderr=subprocess.PIPE,universal_newlines=True)
  print(r.stderr.strip());return [l.split('\t') for l in r.stdout.splitlines()]
 paths=sorted((R/'src/main/java').rglob('*.java'));dest=scan('DestinationScanner',paths)
 by={(str(Path(row[0]).relative_to(R)),int(row[1])):row for row in dest};n=0
 for rel,rec in m['files'].items():
  s=(R/rel).read_bytes().decode();raw=s.encode('utf-16-le');delta=0
  for e in rec['edits']:
   pos=e['start']+delta;utf=len(s[:pos].encode('utf-16-le'))//2
   row=by[(rel,utf)];rs,re,aa,bb=map(int,row[2:6]);assert raw[2*rs:2*re].decode('utf-16-le')==e['receiver'];assert raw[2*aa:2*bb].decode('utf-16-le')==e['arguments'];n+=1;assert (row[6]=='onUpdate' and row[7]=='true') if rel.endswith('EntityNukeTorex.java') else row[6]=='drawMap';delta+=len(e['after'])-len(e['before'])
 baseline=t/'baseline';expected=set()
 for path in paths:
  rel=str(path.relative_to(R));s=path.read_bytes().decode();r=m['files'].get(rel)
  if r:
   spans=[];delta=0
   for e in r['edits']:
    pos=e['start']+delta;spans.append((pos,pos+len(e['after']),e['before']));delta+=len(e['after'])-len(e['before'])
   for aa,bb,v in reversed(spans):s=s[:aa]+v+s[bb:]
   for e in r['edits']:expected.add((rel,len(s[:e['start']].encode('utf-16-le'))//2))
  d=baseline/rel;d.parent.mkdir(parents=True,exist_ok=True);d.write_bytes(s.encode())
 rows=scan('HeightScanner',sorted(baseline.rglob('*.java')))
 actual={(str(Path(row[0]).relative_to(baseline)),int(row[1])) for row in rows if str(Path(row[0]).relative_to(baseline)) in m['files']}
 assert actual==expected,(actual^expected)
 print('PASS:',n,'original exact-World/int attributed height sites and native WorldHeight.read destinations and client lifecycle scopes; exact receiver and coordinate AST spans match')
