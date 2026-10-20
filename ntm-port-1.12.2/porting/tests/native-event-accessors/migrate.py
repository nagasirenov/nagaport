#!/usr/bin/env python3
"""Apply only freshly compiler-attributed field token reads. No textual discovery."""
import sys,json,hashlib,collections
from pathlib import Path
root=Path(__file__).resolve().parents[3]
out=Path(__file__).resolve().parent
rows=[]
for arg in sys.argv[1:]:
 for line in Path(arg).read_text().splitlines():
  path,start,end,new,receiver=line.split('\t');rows.append((Path(path),int(start),int(end),new,receiver))
assert len(rows)==268, len(rows)
if (out/'source-manifest.json').exists():
 expected=json.loads((out/'source-manifest.json').read_text())
 inventory=collections.Counter((f['path'],e['new'],e['receiver']) for f in expected['files'] for e in f['edits'])
 actual=collections.Counter((str(path.relative_to(root)),new,receiver) for path,start,end,new,receiver in rows)
 assert inventory==actual, 'Receiver/accessor/path inventory changed; review required'
manifest=[]
for path in sorted(set(r[0] for r in rows)):
 raw=path.read_bytes(); src=raw.decode(); data=src.encode('utf-16-le'); edits=[]
 for _,start,end,new,receiver in sorted((r for r in rows if r[0]==path),key=lambda r:r[1],reverse=True):
  old=data[start*2:end*2].decode('utf-16-le'); assert {'entityLiving':'getEntityLiving()','entity':'getEntity()','world':'getWorld()'}[old]==new
  data=data[:start*2]+new.encode('utf-16-le')+data[end*2:]
  edits.append(dict(start=start,end=end,old=old,new=new,receiver=receiver))
 after=data.decode('utf-16-le').encode();path.write_bytes(after)
 manifest.append(dict(path=str(path.relative_to(root)),before=hashlib.sha256(raw).hexdigest(),after=hashlib.sha256(after).hexdigest(),edits=sorted(edits,key=lambda r:r['start'])))
(out/'source-manifest.json').write_text(json.dumps({'baseline':'0732d318302d807ce8579878a0cdde6b18bbba86','files':manifest},indent=2)+'\n')
print('Applied',len(rows),'attributed reads in',len(manifest),'files')
