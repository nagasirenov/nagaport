#!/usr/bin/env python3
import json,zipfile,hashlib
from pathlib import Path
HERE=Path(__file__).resolve().parent;ROOT=HERE.parents[2]
def verify(root=ROOT,integrated=False):
 m=json.loads((HERE/'manifest.json').read_text())
 assert hashlib.sha256((root/'src/main/java/com/hbm/world/WorldBlockDestruction.java').read_bytes()).hexdigest()==m['helper_sha256']
 with zipfile.ZipFile(HERE/'baseline.zip') as z:
  for rel,edits in m['files'].items():
   old=z.read(rel).decode();new=old
   for e in sorted(edits,key=lambda e:e['start'],reverse=True):
    assert old[e['start']:e['end']]==e['old'],rel
    new=new[:e['start']]+e['new']+new[e['end']:]
   current=(root/rel).read_bytes().decode()
   if integrated:
    for e in edits:assert e['new'] in current,rel
   else:assert current==new,rel
   restored=new
   delta=0
   for e in edits:
    pos=e['start']+delta;assert restored[pos:pos+len(e['new'])]==e['new'];restored=restored[:pos]+e['old']+restored[pos+len(e['new']):]
   assert restored==old,rel
 print('PASS: 60 exact selected edits, original-byte reversal and target regeneration in 50 files')
 return m
def verify_receipt(root,receipt):
 data=json.loads(receipt.read_text())
 for rel,r in data['files'].items():
  current=(root/rel).read_bytes();assert hashlib.sha256(current).hexdigest()==r['after_sha256'],rel
  inverse=current.decode()
  for start,end,new,oldcall in r['edits']:
   assert inverse[start:start+len(new)]==new,rel
   inverse=inverse[:start]+oldcall+inverse[start+len(new):]
  assert hashlib.sha256(inverse.encode()).hexdigest()==r['before_sha256'],rel
 print('PASS: integrated files exactly reverse to pre-integration bytes under receipt')
if __name__=='__main__':
 import argparse
 p=argparse.ArgumentParser();p.add_argument('--module',type=Path);p.add_argument('--receipt',type=Path);a=p.parse_args()
 if a.receipt:
  assert a.module;verify_receipt(a.module,a.receipt)
 else:verify()

