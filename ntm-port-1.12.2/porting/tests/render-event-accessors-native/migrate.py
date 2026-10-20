#!/usr/bin/env python3
"""Integrate only a fresh attributed inventory equal to the recovered pinned scope."""
import sys,json,hashlib,collections
from pathlib import Path
HERE=Path(__file__).resolve().parent;ROOT=HERE.parents[2]
previous=json.loads((HERE/'source-manifest.json').read_text());rows=[]
for line in Path(sys.argv[1]).read_text().splitlines():
 path,start,end,old,new,receiver,owner=line.split('\t')
 rows.append(dict(path=str(Path(path).relative_to(ROOT)),start=int(start),end=int(end),old=old,new=new,receiver=receiver,owner=owner))
def inventory(es):return collections.Counter((e['path'],e['old'],e['new'],e['receiver'],e['owner']) for e in es)
assert len(rows)==65 and inventory(rows)==inventory(dict(e,path=f['path']) for f in previous['files'] for e in f['edits']), 'Inventory changed; review required'
files=[]
for rel in sorted({r['path'] for r in rows}):
 path=ROOT/rel;before=path.read_bytes();data=before.decode().encode('utf-16-le');edits=sorted((r for r in rows if r['path']==rel),key=lambda r:r['start'])
 for e in reversed(edits):
  start,end=e['start']*2,e['end']*2;assert data[start:end].decode('utf-16-le')==e['old'];data=data[:start]+e['new'].encode('utf-16-le')+data[end:]
 after=data.decode('utf-16-le').encode();path.write_bytes(after)
 files.append(dict(path=rel,before=hashlib.sha256(before).hexdigest(),after=hashlib.sha256(after).hexdigest(),edits=[{k:v for k,v in e.items() if k!='path'} for e in edits]))
previous.update(files=files,attribution='Fresh compiler-selected integration; native proof must be rerun',composition='Hashes describe this integration input/output; original CP76 is historical provenance')
(HERE/'source-manifest.json').write_text(json.dumps(previous,indent=2)+'\n');print('Applied 65 freshly attributed tokens; native proof not implied')
