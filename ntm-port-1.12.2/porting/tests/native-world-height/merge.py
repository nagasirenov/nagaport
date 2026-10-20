#!/usr/bin/env python3
"""Preflight every exact immutable invocation before writing any integrated source."""
import argparse,json,re
from pathlib import Path
p=argparse.ArgumentParser();p.add_argument('--target',type=Path,required=True);p.add_argument('--apply',action='store_true');a=p.parse_args()
m=json.loads(Path(__file__).with_name('manifest.json').read_text());pending=[];n=0
for rel,r in m['files'].items():
 path=a.target/rel;s=path.read_bytes().decode();edits=[]
 for e in r['edits']:
  anchors=list(re.finditer(re.escape(e['left_context']+e['before']),s))
  if len(anchors)==1:pos=anchors[0].start()+len(e['left_context'])
  else:
   hits=list(re.finditer(re.escape(e['before']),s));assert len(hits)==e['invocation_total'],('Invocation drift',rel,e['before']);pos=hits[e['invocation_ordinal']].start()
  assert s[pos:pos+len(e['before'])]==e['before'];edits.append((pos,pos+len(e['before']),e['after']));n+=1
 edits.sort();assert all(edits[i][1]<=edits[i+1][0] for i in range(len(edits)-1)),rel
 for start,end,value in reversed(edits):s=s[:start]+value+s[end:]
 pending.append((path,s.encode()))
if a.apply:
 for path,data in pending:path.write_bytes(data)
print('APPLIED' if a.apply else 'PREFLIGHT PASS',n,'exact invocations in',len(pending),'files')
