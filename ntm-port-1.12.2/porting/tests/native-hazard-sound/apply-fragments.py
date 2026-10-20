#!/usr/bin/env python3
"""Exact, atomic prevalidated fragment replay preserving unrelated current edits."""
import argparse,hashlib,json
from pathlib import Path
HERE=Path(__file__).resolve().parent
p=argparse.ArgumentParser();p.add_argument('--root',type=Path,required=True,help='Target ntm-port-1.12.2 module');p.add_argument('--apply',action='store_true');p.add_argument('--reverse',action='store_true');p.add_argument('--receipt',type=Path);a=p.parse_args()
rows=json.loads((HERE/'sites.json').read_text());additions=json.loads((HERE/'additions.json').read_text())
changes={};receipt=[]
def sha(x):return hashlib.sha256(x).hexdigest()
path='src/main/java/com/hbm/blocks/ModBlocks.java';original=(a.root/path).read_bytes();value=original;done=0
for row in rows:
 old=row['before'].encode();new=row['after'].encode()
 if a.reverse:old,new=new,old
 if value.count(new)==1 and value.count(old)==0:continue
 assert value.count(old)==1 and value.count(new)==0,('ambiguous/missing exact statement',row['name'])
 value=value.replace(old,new);done+=1
# Verify no byte outside our exact changed fragments was touched.
restored=value
for row in rows:
 old=row['before'].encode();new=row['after'].encode()
 if a.reverse:old,new=new,old
 if original.count(old)==1:restored=restored.replace(new,old)
assert restored==original
changes[path]=value;receipt.append(dict(path=path,before=sha(original),after=sha(value),inverse=sha(restored),fragments=done))
for path,addition in additions.items():
 original=(a.root/path).read_bytes();extra=addition.encode();name=Path(path).stem;anchor=('\n\tpublic '+name+' makeBeaconable()').encode();value=original
 assert original.count(extra)<=1 and original.count(anchor)==1,(path,'ambiguous owner builder anchor')
 if a.reverse:
  if extra in value:value=value.replace(extra,b'')
 else:
  if extra not in value:value=value.replace(anchor,extra+anchor)
 if value!=original:
  restored=value.replace(anchor,extra+anchor) if a.reverse else value.replace(extra,b'')
 else:restored=value
 assert restored==original
 changes[path]=value;receipt.append(dict(path=path,before=sha(original),after=sha(value),inverse=sha(restored),fragments=int(value!=original)))
if a.apply:
 for path,value in changes.items():(a.root/path).write_bytes(value)
result=dict(mode='reverse' if a.reverse else 'forward',written=a.apply,files=receipt)
text=json.dumps(result,indent=2)+'\n'
if a.receipt:a.receipt.write_text(text)
print(text,end='')
