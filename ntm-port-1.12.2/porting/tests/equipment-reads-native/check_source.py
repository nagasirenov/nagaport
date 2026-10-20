#!/usr/bin/env python3
"""Pinned CP86 source inverse, ownership evidence and drift controls."""
from pathlib import Path
import collections, hashlib, json
HOME=Path(__file__).resolve().parent
ROOT=HOME.parents[2]
DATA=json.loads((HOME/'source-manifest.json').read_text())
def sha(s): return hashlib.sha256(s if isinstance(s,bytes) else s.encode()).hexdigest()
def inverse(relative,text,strict=True):
 row=DATA['files'][relative]
 if strict: assert sha(text)==row['after_sha256'],'after-source drift: '+relative
 for (new,old),count in collections.Counter((e['new'],e['old']) for e in row['edits']).items():
  assert text.count(new)==count,'selected token drift: '+relative+': '+new
  text=text.replace(new,old)
 if strict: assert sha(text)==row['before_sha256'],'CP86 inverse mismatch: '+relative
 return text

def main():
 assert DATA['baseline_commit']=='fc2cd2dba34caba0dba74ed288f95107ab4dc979'
 assert DATA['all_attributed_calls']==44 and DATA['selected_calls']==44 and len(DATA['excluded'])==0
 negatives=0
 for relative,row in DATA['files'].items():
  text=(ROOT/relative).read_bytes().decode();inverse(relative,text)
  for damaged in [text+' ',text.replace(row['edits'][0]['new'],'changed',1)]:
   try:inverse(relative,damaged)
   except AssertionError:negatives+=1
   else:raise AssertionError('drift accepted')
 helper=(ROOT/DATA['helper']['file']).read_text()
 assert sha(helper)==DATA['helper']['sha256'],'helper drift'
 assert helper.count('return stack == ItemStack.EMPTY ? null : stack;')==2
 assert '.isEmpty(' not in helper and '.copy(' not in helper and 'OFFHAND' not in helper
 for row in DATA['excluded']:assert row['old'] in (ROOT/row['file']).read_text()
 print('PASS %d CP86 whole-file source inverses; 44 exact selected calls; machine-energy surroundings byte-preserved; %d drift negatives'%(len(DATA['files']),negatives))
if __name__=='__main__':main()
