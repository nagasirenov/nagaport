#!/usr/bin/env python3
"""Exact finite native source mapping guard, not behavior proof."""
import argparse,hashlib,json,pathlib
p=argparse.ArgumentParser();p.add_argument('--source-root',type=pathlib.Path);a=p.parse_args()
here=pathlib.Path(__file__).resolve().parent;root=a.source_root or here.parents[2]
scope=json.loads((here/'mapping.json').read_text())
def digest(s):return hashlib.sha256(s.encode()).hexdigest()
def undo(text,entry):
 assert digest(text)==entry['native_sha256'],'changed native source'
 for part in reversed(entry['edits']):
  assert text[part['native_start']:part['native_end']]==part['after'],'changed exact replacement'
  text=text[:part['native_start']]+part['before']+text[part['native_end']:]
 assert digest(text)==entry['baseline_sha256'],'inverse differs from pinned complete source'
 return text
mutations=0
for path,entry in scope['complete_sources'].items():
 s=(root/path).read_text();undo(s,entry)
 for bad in [s+'\n',s.replace('package com.hbm','package com.hbn',1),s[:len(s)//2]+' '+s[len(s)//2:]]:
  try:undo(bad,entry)
  except AssertionError:mutations+=1
  else:raise AssertionError('guard accepted deliberate source drift')
for path,entry in scope['shared_fragments'].items():
 s=(root/path).read_text();assert s.count(entry['after'])==1 and entry['before'] not in s,('shared registration fragment',path)
print('PASS exact inverse mapping for '+str(len(scope['complete_sources']))+' complete sources, '+str(len(scope['shared_fragments']))+' exact shared registration fragment, '+str(mutations)+' rejected drift mutations; source proof only.')
