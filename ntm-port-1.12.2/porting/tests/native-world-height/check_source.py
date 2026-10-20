#!/usr/bin/env python3
import hashlib,json
from pathlib import Path
HERE=Path(__file__).resolve().parent; ROOT=HERE.parents[2]
def digest(s):return hashlib.sha256(s.encode()).hexdigest()
def check():
 m=json.loads((HERE/'manifest.json').read_text()); total=0
 assert hashlib.sha256((ROOT/m['new_helper']['path']).read_bytes()).hexdigest()==m['new_helper']['sha256']
 audit=json.loads((HERE/'client-source-audit.json').read_text())
 assert hashlib.sha256((ROOT/'src/main/java/com/hbm/items/tool/ItemSatInterface.java').read_bytes()).hexdigest()==audit['native_unchanged_gui_provider_sha256']
 for rel,r in m['files'].items():
  new=(ROOT/rel).read_bytes().decode();assert digest(new)==r['after_sha256'],rel
  spans=[];delta=0
  for e in r['edits']:
   assert e['after']=='com.hbm.world.WorldHeight.read('+e['receiver']+', '+e['arguments']+')'
   a=e['start']+delta;b=a+len(e['after']);assert new[a:b]==e['after'],rel
   spans.append((a,b,e['before']));delta+=len(e['after'])-len(e['before'])
  assert all(spans[i][1]<=spans[i+1][0] for i in range(len(spans)-1))
  old=new
  for a,b,s in reversed(spans):old=old[:a]+s+old[b:]
  assert digest(old)==r['before_sha256'],rel
  rebuilt=old
  for e in reversed(r['edits']):
   assert rebuilt[e['start']:e['end']]==e['before'];rebuilt=rebuilt[:e['start']]+e['after']+rebuilt[e['end']:]
  assert rebuilt==new;total+=len(spans)
 print('PASS:',total,'exact original receiver/argument-preserving edits;',len(m['files']),'whole-file byte-exact inverses')
if __name__=='__main__':check()
