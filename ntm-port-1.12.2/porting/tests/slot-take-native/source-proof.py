#!/usr/bin/env python3
"""Exact, fail-closed CP81 callback-only forward/inverse verifier. No quantity/storage normalization."""
from pathlib import Path
import json,hashlib,zipfile,subprocess
HERE=Path(__file__).resolve().parent;ROOT=HERE.parents[3]
M=json.loads((HERE/'manifest.json').read_text());FILES={f['path']:f for f in M['files']}
def sha(s):return hashlib.sha256(s.encode()).hexdigest()
def norm(p):return p if p.startswith('ntm-port-1.12.2/') else 'ntm-port-1.12.2/'+p

def reverse_source(relative,text):
 f=FILES[norm(relative)];assert sha(text)==f['after_sha256'],relative+' current-source drift'
 delta=0;edits=[]
 for e in f['edits']:
  start=e['start']+delta;edits.append((start,start+len(e['new']),e));delta+=len(e['new'])-len(e['old'])
 for start,end,e in reversed(edits):
  assert text[start:end]==e['new'];text=text[:start]+e['old']+text[end:]
 assert sha(text)==f['before_sha256'];return text

def forward_source(relative,text):
 f=FILES[norm(relative)];assert sha(text)==f['before_sha256']
 for e in reversed(f['edits']):
  assert text[e['start']:e['end']]==e['old'];text=text[:e['start']]+e['new']+text[e['end']:]
 assert sha(text)==f['after_sha256'];return text

def verify():
 mutations=0;callbacks=0;calls=0
 with zipfile.ZipFile(HERE/'baseline.zip') as z:
  for p,f in FILES.items():
   current=(ROOT/p).read_bytes().decode();before=z.read(p).decode()
   assert forward_source(p,before)==current;assert reverse_source(p,current)==before
   for e in f['edits']:
    if e['kind']=='callback':
     callbacks+=1;old=e['old'];new=e['new'];back=new.replace('public ItemStack onTake(', 'public void onPickupFromSlot(').replace('super.onTake(', 'super.onPickupFromSlot(')
     if p.endswith('/SlotCraftingOutput.java'):back=back.replace('return super.onPickupFromSlot(', 'super.onPickupFromSlot(')
     else:
      # Exactly one new terminal return, and only original bare returns gain the same stack.
      ix=back.rfind('\n');prev=back.rfind('\n',0,ix);assert back[prev:ix].strip()=='return stack;';back=back[:prev]+back[ix:]
      back=back.replace('return stack;', 'return;')
     assert back==old,p+' callback body not invariant under native signature/return mapping'
    else:assert e['old']=='onPickupFromSlot' and e['new']=='onTake';calls+=1
   # Callback name loss, arbitrary payload change, and unrelated trailing-source drift all rejected.
   for altered in [current.replace('onTake','onFake',1),current.replace('ItemStack','ItemStuck',1),current+'\n// drift\n']:
    assert altered!=current
    try:reverse_source(p,altered)
    except AssertionError:mutations+=1
    else:raise AssertionError('accepted drift '+p)
  for ex in M['exclusions']:assert (ROOT/ex['path']).read_bytes()==z.read(ex['path'])
 assert callbacks==7 and calls==32 and len(FILES)==32
 print('PASS 32 exact file forward/inverses; 7 complete callback body inverses; 32 invocation-token-only edits; 96 drift rejections; historical Javadoc exclusion unchanged')
 return dict(files=32,callbacks=callbacks,ordinary_calls=calls,super_calls=7,drift_rejections=mutations)
if __name__=='__main__':verify()
