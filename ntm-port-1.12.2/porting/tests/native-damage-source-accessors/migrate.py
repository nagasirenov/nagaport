#!/usr/bin/env python3
"""Token-only migration; a fresh javac inventory permits unrelated overlapping edits."""
import argparse,collections,hashlib,json
from pathlib import Path
NAMES={'getEntity':'getTrueSource','getSourceOfDamage':'getImmediateSource'}
def sha(data):return hashlib.sha256(data).hexdigest()
def read_rows(path,root):
 rows=[]
 for line in Path(path).read_text().splitlines():
  filename,kind,start,end,name,receiver,owner,context=line.split('\t')
  rows.append(dict(path=str(Path(filename).relative_to(root)),kind=kind,start=int(start),end=int(end),old=name,new=NAMES.get(name,name),receiver=receiver,owner=owner,context=context))
 return rows

def inventory(rows):return collections.Counter((r['path'],r['kind'],r['old'],r['receiver'],r['context']) for r in rows if r['old'] in NAMES)
def apply(root,rows,reference=None):
 selected=[r for r in rows if r['old'] in NAMES]
 if reference:
  wanted=inventory([dict(e,path=f['path']) for f in reference['files'] for e in f['edits']])
  assert inventory(selected)==wanted,(inventory(selected)-wanted,wanted-inventory(selected))
 assert collections.Counter(r['old'] for r in selected)=={'getEntity':31,'getSourceOfDamage':6}
 assert collections.Counter(r['kind'] for r in selected)=={'CALL':35,'DECL':2}
 files=[]
 for rel in sorted({r['path'] for r in selected}):
  path=root/rel;before=path.read_bytes();text=before.decode('utf-8');units=text.encode('utf-16-le');edits=sorted([dict(r) for r in selected if r['path']==rel],key=lambda r:r['start'])
  for e in reversed(edits):
   assert units[e['start']*2:e['end']*2].decode('utf-16-le')==e['old'],e
   units=units[:e['start']*2]+e['new'].encode('utf-16-le')+units[e['end']*2:]
  after=units.decode('utf-16-le').encode('utf-8');shift=0
  for e in edits:
   e['after_start']=e['start']+shift;e['after_end']=e['after_start']+len(e['new']);shift+=len(e['new'])-(e['end']-e['start']);e.pop('path')
  files.append(dict(path=rel,before_sha256=sha(before),after_sha256=sha(after),edits=edits));path.write_bytes(after)
 return dict(baseline='d5ef20a8f1cb65fbd06faa86a4901870c8bab3f2',scope='35 source-entity calls plus both Sedna virtual overrides; no other production changes',files=files,untouched=[r for r in rows if r['old'] not in NAMES])

if __name__=='__main__':
 p=argparse.ArgumentParser();p.add_argument('--module',type=Path,required=True);p.add_argument('--attribution',type=Path,required=True);p.add_argument('--reference',type=Path);p.add_argument('--output',type=Path,required=True);a=p.parse_args()
 result=apply(a.module.resolve(),read_rows(a.attribution,a.module.resolve()),json.loads(a.reference.read_text()) if a.reference else None)
 a.output.write_text(json.dumps(result,indent=2)+'\n');print('PASS token-only migration: 37 edits in',len(result['files']),'files; independent existing edits preserved')
