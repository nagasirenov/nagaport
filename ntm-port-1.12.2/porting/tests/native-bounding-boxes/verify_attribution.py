#!/usr/bin/env python3
"""Verify selected whole-source attributed spans and all destination constructor signatures."""
import json,pathlib
import source
H=source.HERE;R=source.ROOT;m=json.loads((H/'manifest.json').read_text())
def read(p):return [json.loads(s) for s in p.read_text().splitlines()]
def rel(p):return 'src/main/java/'+p.split('/src/main/java/',1)[1]
before=read(H/'before.jsonl');after=read(H/'after.jsonl')
selected={(rel(x['path']),x['start'],x['end']) for x in before if x['selected']}
actual={(rel(x['path']),x['start'],x['end'],x['descriptor']) for x in after};expected=set();checked=0
for p,r in m['files'].items():
 s=(R/p).read_bytes().decode();old=source.verify(s.encode(),r)
 for e in r['edits']:
  start=len(old[:e['start']].encode('utf-16-le'))//2;end=len(old[:e['end']].encode('utf-16-le'))//2;expected.add((p,start,end))
  assert e['owner']=='CLASS:net.minecraft.util.math.AxisAlignedBB'
  a=e['after_start'];b=a+len(e['after']);na=len(s[:a].encode('utf-16-le'))//2;nb=len(s[:b].encode('utf-16-le'))//2
  assert (p,na,nb,'(DDDDDD)V') in actual,(p,na,nb);checked+=1
assert expected==selected
assert len(before)==372 and checked==20
print('PASS all 20 selected exact-native receivers and actual native (DDDDDD)V constructor destinations; argument bytes unchanged')
