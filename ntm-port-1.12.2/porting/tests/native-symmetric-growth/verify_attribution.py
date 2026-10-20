#!/usr/bin/env python3
import json
import source
H=source.H;R=source.R;m=json.loads((H/'manifest.json').read_text())
before=source.read(H/'before.jsonl');after=source.read(H/'after.jsonl')
before_by={(source.rel(x['path']),x['nameStart']):x for x in before};after_by={(source.rel(x['path']),x['nameStart']):x for x in after};checked=0
for rel,rec in m['files'].items():
 s=(R/rel).read_bytes().decode();old=source.inverse(s.encode(),rec).decode()
 for e in rec['edits']:
  aa=len(old[:e['start']].encode('utf-16-le'))//2;bb=len(s[:e['after_start']].encode('utf-16-le'))//2
  a=before_by[(rel,aa)];b=after_by[(rel,bb)]
  assert a['native'] and b['native'] and a['name']=='expand' and b['name']=='grow'
  assert a['owner']==b['owner']=='net.minecraft.util.math.AxisAlignedBB'
  assert a['signature']==b['signature']=='(double,double,double)net.minecraft.util.math.AxisAlignedBB'
  assert a['receiver']==b['receiver']==e['receiver'] and a['arguments']==b['arguments']==e['arguments'];checked+=1
assert checked==32
# Existing native grow methods, domain methods, dynamic/negative or unresolved calls are not selected.
expected=32;assert sum(x['name']=='expand' and x['candidate'] for x in before)==expected
print('PASS 32 genuine native (DDD) expand/grow bindings; each receiver and all argument AST bytes identical; source AABB override exclusion active')
