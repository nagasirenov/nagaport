#!/usr/bin/env python3
"""Prevalidate and replay only exact opacity methods; preserve independent callback edits."""
import argparse,hashlib,json
from pathlib import Path
HERE=Path(__file__).resolve().parent
p=argparse.ArgumentParser();p.add_argument('--root',type=Path,required=True);p.add_argument('--apply',action='store_true');p.add_argument('--reverse',action='store_true');p.add_argument('--receipt',type=Path);a=p.parse_args();changes={};receipt=[]
for row in json.loads((HERE/'sites.json').read_text()):
 path=row['path'];original=(a.root/path).read_bytes();old=row['before'].encode();new=row['after'].encode()
 if a.reverse:old,new=new,old
 if original.count(new)==1 and original.count(old)==0:value=original;inverse=original;changed=False
 else:
  assert original.count(old)==1 and original.count(new)==0,('Missing/ambiguous exact method',path)
  value=original.replace(old,new);inverse=value.replace(new,old);changed=True
 assert inverse==original
 changes[path]=value;sha=lambda x:hashlib.sha256(x).hexdigest();receipt.append(dict(path=path,before=sha(original),after=sha(value),inverse=sha(inverse),changed=changed))
if a.apply:
 for path,value in changes.items():(a.root/path).write_bytes(value)
result=json.dumps(dict(direction='reverse' if a.reverse else 'forward',written=a.apply,files=receipt),indent=2)+'\n'
if a.receipt:a.receipt.write_text(result)
print(result,end='')
