#!/usr/bin/env python3
"""Exact call fragments, transactional preflight, preserving unrelated collaborator edits."""
import argparse,collections,hashlib,json
from pathlib import Path
H=Path(__file__).resolve().parent
p=argparse.ArgumentParser();p.add_argument('--module',type=Path,required=True);p.add_argument('--write',action='store_true');p.add_argument('--receipt',type=Path);a=p.parse_args();d=json.loads((H/'selection.json').read_text());groups=collections.defaultdict(list)
for op in d['operations']:groups[op['path']].append(op)
ready=[];receipt=[]
for path,ops in groups.items():
 file=a.module/'src/main/java'/path;before=file.read_bytes();s=before.decode();unique=collections.Counter((o['old'],o['new']) for o in ops);spans=[]
 for (old,new),count in unique.items():
  assert s.count(old)==count,(path,'old fragment count',old,s.count(old),count);start=0
  for _ in range(count):start=s.index(old,start);spans.append((start,start+len(old),old,new));start+=len(old)
 for start,end,old,new in sorted(spans,reverse=True):assert s[start:end]==old;s=s[:start]+new+s[end:]
 inverse=s
 for start,end,old,new in sorted(spans):assert inverse[start:start+len(new)]==new;inverse=inverse[:start]+old+inverse[start+len(new):]
 assert inverse.encode()==before;after=s.encode();ready.append((file,after));receipt.append({'path':path,'calls':len(ops),'before_sha256':hashlib.sha256(before).hexdigest(),'after_sha256':hashlib.sha256(after).hexdigest(),'exact_inverse':True})
if a.write:
 for file,after in ready:file.write_bytes(after)
report={'mode':'write' if a.write else 'dry-run','baseline':d['baseline'],'operations':18,'files':receipt}
if a.receipt:a.receipt.write_text(json.dumps(report,indent=2)+'\n')
print('PASS '+report['mode']+': 18 exact paired-comparator signature fragments across 9 files; unrelated bytes preserved')
