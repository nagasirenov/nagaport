#!/usr/bin/env python3
"""Restore only six CP93 intersections, then run the unmodified CP92 air guard."""
from pathlib import Path
import argparse,hashlib,importlib.util,json,tempfile
H=Path(__file__).resolve().parent
p=argparse.ArgumentParser();p.add_argument('--module',type=Path,default=H.parents[2]);a=p.parse_args();R=a.module
D=json.loads((H/'composition-cp93.json').read_text())
for path,want in D['pins'].items():assert hashlib.sha256((R/path).read_bytes()).hexdigest()==want,path
def load(name,path):
 spec=importlib.util.spec_from_file_location(name,path);m=importlib.util.module_from_spec(spec);spec.loader.exec_module(m);return m
current=load('air_cp93_current',R/'porting/tests/check-native-normal-world-air-source.py')
historical=load('air_cp92_historical',R/'porting/tests/native-block-air-captures/check_source.py')
intersect=sorted(set(current.FILES)&set(historical.data['files']));assert intersect==D['files'] and len(intersect)==6
with tempfile.TemporaryDirectory(prefix='air-cp93-composition-') as temporary:
 root=Path(temporary);rejected=0
 for rel in historical.data['files']:
  raw=(R/rel).read_bytes()
  if rel in intersect:
   text=raw.decode();raw=current.reverse_source(rel,text).encode()
   for mutant in (text+'\n// drift\n',text[:-1],text.replace('\r\n','\n') if '\r\n' in text else text.replace('\n','\r\n')):
    assert mutant!=text
    try:current.reverse_source(rel,mutant)
    except AssertionError:rejected+=1
    else:raise AssertionError('accepted CP93 source drift '+rel)
  target=root/rel;target.parent.mkdir(parents=True,exist_ok=True);target.write_bytes(raw)
 historical.verify(root)
 assert rejected==18
print('PASS exact six-file CP93 inverse composition over unchanged CP92 air manifest/checker; 18 rejected CP93 mutations plus 42 historical controls')
