#!/usr/bin/env python3
"""Merge approved statement fragments after unrelated worker edits. Fails on ambiguity."""
import pathlib,json,sys
h=pathlib.Path(__file__).resolve().parent;data=json.loads((h/'source-manifest.json').read_text());root=pathlib.Path(sys.argv[1]);pending=[]
for rel,info in data['files'].items():
 raw=(root/rel).read_bytes();edits=info['edits'];counts={}
 for e in edits:counts[e['before']]=counts.get(e['before'],0)+1
 for before,n in counts.items():
  after=next(e['after'] for e in edits if e['before']==before)
  if raw.count(before.encode())!=n:raise SystemExit('Ambiguous or absent fragment: '+rel+' '+before)
  raw=raw.replace(before.encode(),after.encode())
 pending.append((root/rel,raw))
for p,raw in pending:p.write_bytes(raw)
print('Applied',sum(len(i['edits']) for i in data['files'].values()),'exact statement fragments; unrelated bytes retained.')
