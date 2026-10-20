#!/usr/bin/env python3
"""Apply exact approved fragments to a separately changed source tree, preserving all other bytes."""
import argparse,hashlib,json
from pathlib import Path
H=Path(__file__).resolve().parent
p=argparse.ArgumentParser();p.add_argument('--module',type=Path,required=True);p.add_argument('--write',action='store_true');p.add_argument('--receipt',type=Path);a=p.parse_args();d=json.loads((H/'selection.json').read_text());writes=[];receipt=[]
for e in d['operations']:
 path=a.module/'src/main/java'/e['path'];before=path.read_bytes();text=before.decode()
 # Reverse ordered replacement operations to detect an already composed file.
 inverse=text;already=True
 for old,new in reversed(e['replacements']):
  if inverse.count(new)!=1:already=False;break
  inverse=inverse.replace(new,old)
 if already:receipt.append({'path':e['path'],'status':'already applied','sha256':hashlib.sha256(before).hexdigest()});continue
 after=text
 for old,new in e['replacements']:
  if after.count(old)!=1:raise SystemExit('Ambiguous or absent authorized fragment: '+e['path']+' / '+old)
  after=after.replace(old,new)
 reverse=after
 for old,new in reversed(e['replacements']):
  assert reverse.count(new)==1,(e['path'],new);reverse=reverse.replace(new,old)
 assert reverse==text,'unrelated byte change'
 writes.append((path,after.encode()));receipt.append({'path':e['path'],'status':'will apply' if not a.write else 'applied','before_sha256':hashlib.sha256(before).hexdigest(),'after_sha256':hashlib.sha256(after.encode()).hexdigest(),'inverse_preserves_all_other_bytes':True})
if a.write:
 for path,data in writes:path.write_bytes(data)
if a.receipt:a.receipt.write_text(json.dumps({'baseline':d['baseline'],'write':a.write,'files':receipt},indent=2)+'\n')
print(('Applied' if a.write else 'Validated')+' '+str(len(writes))+' exact source fragments; '+str(len(receipt)-len(writes))+' paths already applied.')
