#!/usr/bin/env python3
"""Historical exact CP78 application. Never rerun on an integrated or changed tree."""
import collections,hashlib,json,pathlib,sys
h=pathlib.Path(__file__).resolve().parent;root=h.parents[2];sha=lambda b:hashlib.sha256(b).hexdigest();files=collections.defaultdict(list)
for line in pathlib.Path(sys.argv[1]).read_text().splitlines():
 rel,start,end,new,receiver,field_owner,lexical_owner,access=line.split('\t')
 assert access=='READ';files[rel].append(dict(start=int(start),end=int(end),old='worldObj',new=new,receiver=receiver,field_owner=field_owner,lexical_owner=lexical_owner,access=access))
assert sum(map(len,files.values()))==31 and len(files)==9
manifest={'base':'89b6bfc71a8b532c2fe7cdbd43bb66b7991ee365','files':{}}
for rel,edits in sorted(files.items()):
 p=root/rel;before=p.read_bytes();raw=before.decode().encode('utf-16-le');delta=0
 for e in edits:
  e['after_start']=e['start']+delta;delta+=len(e['new'])-len(e['old']);e['line']=before.decode().encode('utf-16-le')[:e['start']*2].decode('utf-16-le').count('\n')+1
 for e in reversed(edits):
  lo,hi=e['start']*2,e['end']*2;assert raw[lo:hi].decode('utf-16-le')==e['old'];raw=raw[:lo]+e['new'].encode('utf-16-le')+raw[hi:]
 after=raw.decode('utf-16-le').encode();p.write_bytes(after);manifest['files'][rel]={'before_sha256':sha(before),'after_sha256':sha(after),'edits':edits}
(h/'source-manifest.json').write_text(json.dumps(manifest,indent=2)+'\n')
(h/'before.tsv').write_bytes(pathlib.Path(sys.argv[1]).read_bytes())
print('Applied 31 exact read tokens in 9 files')
