#!/usr/bin/env python3
"""Compose six exact callback changes while retaining unrelated owner's bytes."""
import argparse,hashlib,json
from pathlib import Path
H=Path(__file__).resolve().parent
p=argparse.ArgumentParser();p.add_argument('--module',type=Path,required=True);p.add_argument('--write',action='store_true');p.add_argument('--receipt',type=Path);a=p.parse_args();d=json.loads((H/'selection.json').read_text());writes=[];receipts=[]
for e in d['entries']:
 path=a.module/'src/main/java'/e['path'];before=path.read_text()
 if before.count(e['new'])==1 and e['old'] not in before:receipts.append({'path':e['path'],'status':'already applied'});continue
 assert before.count(e['old'])==1 and e['new'] not in before,('absent/ambiguous authorized callback',e['path'])
 after=before.replace(e['old'],e['new']);assert after.count(e['new'])==1 and after.replace(e['new'],e['old'])==before
 writes.append((path,after));receipts.append({'path':e['path'],'status':'applied' if a.write else 'ready','before_sha256':hashlib.sha256(before.encode()).hexdigest(),'after_sha256':hashlib.sha256(after.encode()).hexdigest(),'all_unrelated_bytes_preserved':True})
if a.write:
 for path,s in writes:path.write_text(s)
if a.receipt:a.receipt.write_text(json.dumps({'base':d['base'],'write':a.write,'files':receipts},indent=2)+'\n')
print(('Applied' if a.write else 'Validated')+' '+str(len(writes))+' exact callback fragments; '+str(len(receipts)-len(writes))+' already applied')
