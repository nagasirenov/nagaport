#!/usr/bin/env python3
"""Verify the explicit scheduler composition and reject bounded wrong-delta mutations."""
from pathlib import Path
import argparse,importlib.util,json,zipfile
HERE=Path(__file__).resolve().parent
p=argparse.ArgumentParser(description=__doc__);p.add_argument('--source-root',type=Path,required=True);a=p.parse_args()
spec=importlib.util.spec_from_file_location('multi_state_probe',HERE/'run-native.py');m=importlib.util.module_from_spec(spec);spec.loader.exec_module(m)
receipt=json.loads((HERE/'composition-scheduled-updates.json').read_text());rejected=0
with zipfile.ZipFile(HERE/'source-baseline.zip') as archive:
 for relative,f in receipt['files'].items():
  current=(a.source_root/'src/main/java'/relative).read_bytes()
  baseline=archive.read('cp83/'+relative)
  assert m.composed_source(relative,current,True)==baseline
  text=current.decode(); offset=0
  mutations=[current+b'\n',baseline]
  for c in f['calls']:
   start=c['start']+offset;end=start+len(c['after']);offset+=len(c['after'])-len(c['before'])
   assert text[start:end]==c['after']
   for changed in [c['after'].replace('.scheduleUpdate(','.scheduleBlockUpdate(',1),c['after'][:-1]+' + 1)',c['after'].replace('BlockPos(','BlockPos(1 + ',1)]:
    mutations.append((text[:start]+changed+text[end:]).encode())
  for changed in mutations:
   try:m.composed_source(relative,changed,True)
   except AssertionError:rejected+=1
   else:raise AssertionError('accepted a wrong target/delay/coordinate, unrelated drift, or absent composition: '+relative)
assert rejected==16
print('PASS exact scheduler composition of four calls in two dependency files back to pinned CP83; 16 wrong-delta/absent/unrelated-source mutations rejected.')
