#!/usr/bin/env python3
from pathlib import Path
import hashlib,json
h=Path(__file__).resolve().parent;r=h.parents[2]
def sha(b):return hashlib.sha256(b).hexdigest()
def inverse(data,entry):
 u=data.decode().encode('utf-16-le')
 # After offsets account for preceding replacement lengths.
 delta=0; positioned=[]
 for e in sorted(entry['edits'],key=lambda x:x['start']):
  start=e['start']+delta;end=start+len(e['after'].encode('utf-16-le'))//2
  positioned.append((start,end,e));delta+=len(e['after'].encode('utf-16-le'))//2-(e['end']-e['start'])
 for start,end,e in reversed(positioned):
  assert u[start*2:end*2].decode('utf-16-le')==e['after'],entry['path']
  u=u[:start*2]+e['before'].encode('utf-16-le')+u[end*2:]
 return u.decode('utf-16-le').encode()
def main():
 helper=(r/'src/main/java/com/hbm/util/NativeHeldItem.java').read_bytes();assert sha(helper)==(h/'helper.sha256').read_text().strip(),'native helper drift'
 entries=json.loads((h/'source-manifest.json').read_text());negative=0
 for e in entries:
  data=(r/e['path']).read_bytes();assert sha(data)==e['after_sha256'],e['path'];before=inverse(data,e);assert sha(before)==e['before_sha256']
  for corrupt in (data+b' ',data.replace(b'getMainHandNullable',b'getHeldItemOffhand',1),data.replace(b'getMainHandNullable',b'getHeldItemMainhand',1)):
   assert sha(corrupt)!=e['after_sha256'];negative+=1
 print('PASS',len(entries),'exact forward/inverse production files;',negative,'source mutation controls')
if __name__=='__main__':main()
