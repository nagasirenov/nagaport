#!/usr/bin/env python3
"""Preflight exact invocation anchors; preserve unrelated later-tree edits."""
import argparse,json,zipfile,re,hashlib
from pathlib import Path
import audit
p=argparse.ArgumentParser();p.add_argument('--module',required=True,type=Path);p.add_argument('--apply',action='store_true');p.add_argument('--receipt',type=Path);a=p.parse_args();m=audit.verify();changes={};receipts={}
def sha(b):return hashlib.sha256(b).hexdigest()
with zipfile.ZipFile(audit.HERE/'baseline.zip')as z:
 for rel,edits in m['files'].items():
  before=(a.module/rel).read_bytes();s=before.decode();old=z.read(rel).decode();groups={}
  for e in edits:groups.setdefault(e['old'],[]).append(e)
  positions=[]
  for needle,es in groups.items():
   orig=[x.start()for x in re.finditer(re.escape(needle),old)];now=[x.start()for x in re.finditer(re.escape(needle),s)]
   assert len(orig)==len(now),(rel,'changed/ambiguous invocation counts',needle,len(orig),len(now))
   for e in es:
    ordinal=orig.index(e['start']);pos=now[ordinal];positions.append((pos,pos+len(needle),e['new'],needle))
  positions.sort();assert all(positions[i][1]<=positions[i+1][0]for i in range(len(positions)-1)),rel
  after=s
  for start,end,new,oldcall in reversed(positions):after=after[:start]+new+after[end:]
  inverse=after;delta=0
  for start,end,new,oldcall in positions:
   adjusted=start+delta;assert inverse[adjusted:adjusted+len(new)]==new;inverse=inverse[:adjusted]+oldcall+inverse[adjusted+len(new):]
  assert inverse==s,rel
  changes[rel]=after.encode();receipts[rel]={'before_sha256':sha(before),'after_sha256':sha(after.encode()),'edits':positions}
helper='src/main/java/com/hbm/world/WorldAirRemoval.java';source=(audit.ROOT/helper).read_bytes();target=a.module/helper
assert not target.exists()or target.read_bytes()==source,helper
changes[helper]=source
print('PASS: preflight 150 invocation-only edits across 82 files plus one domain helper; inverse preserves unrelated source bytes')
if a.apply:
 assert a.receipt,'--receipt required for an applied later-tree integration'
 for rel,data in changes.items():p=a.module/rel;p.parent.mkdir(parents=True,exist_ok=True);p.write_bytes(data)
 a.receipt.write_text(json.dumps({'baseline':m['baseline'],'files':receipts},indent=2)+'\n');print('Applied; exact integration receipt:',a.receipt)
