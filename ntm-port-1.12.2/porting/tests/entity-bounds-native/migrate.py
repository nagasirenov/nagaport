#!/usr/bin/env python3
"""Apply only javac-attributed UTF16 token ranges; no regular-expression ownership guesses."""
import argparse,collections,hashlib,json,pathlib,subprocess
p=argparse.ArgumentParser();p.add_argument('attributed',type=pathlib.Path);a=p.parse_args();h=pathlib.Path(__file__).resolve().parent;root=h.parents[2]
sha=lambda b:hashlib.sha256(b).hexdigest()
files=collections.defaultdict(list)
for row in a.attributed.read_text().splitlines():
 path,start,end,new,receiver=row.split('\t');files[pathlib.Path(path).relative_to(root).as_posix()].append(dict(start=int(start),end=int(end),old='boundingBox',new=new,owner=receiver))
approved=json.loads((h/'source-manifest.json').read_text())
# Re-attributed edits may move after unrelated migrations, but may not change
# the finite receiver/file inventory. Reads are selected by javac, not regex.
signature=lambda es:collections.Counter((e['old'],e['new'],e['owner']) for e in es)
assert set(files)==set(approved['files']), 'approved file inventory changed'
for rel,es in files.items():assert signature(es)==signature(approved['files'][rel]['edits']), ('approved receiver inventory changed',rel)
manifest={'base':subprocess.check_output(['git','-C',str(root),'rev-parse','HEAD'],text=True).strip(),'scope':'184 native Entity boundingBox reads; no writes changed','files':{}}
for rel,edits in sorted(files.items()):
 path=root/rel;before=path.read_bytes();raw=before.decode('utf-8').encode('utf-16-le')
 for e in sorted(edits,key=lambda x:x['start'],reverse=True):
  lo,hi=e['start']*2,e['end']*2;assert raw[lo:hi].decode('utf-16-le')==e['old'];raw=raw[:lo]+e['new'].encode('utf-16-le')+raw[hi:]
 after=raw.decode('utf-16-le').encode('utf-8');path.write_bytes(after)
 delta=0
 for e in sorted(edits,key=lambda x:x['start']):
  e['after_start']=e['start']+delta;delta+=len(e['new'])-len(e['old'])
 manifest['files'][rel]={'before_sha256':sha(before),'after_sha256':sha(after),'edits':edits}
(h/'source-manifest.json').write_text(json.dumps(manifest,indent=2)+'\n')
(h/'attributed-accesses.tsv').write_text('\n'.join('\t'.join([str(pathlib.Path(row.split('\t')[0]).relative_to(root))]+row.split('\t')[1:]) for row in a.attributed.read_text().splitlines())+'\n')
print('Migrated',sum(len(x) for x in files.values()),'accesses in',len(files),'files')
