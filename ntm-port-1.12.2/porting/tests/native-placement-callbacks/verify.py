#!/usr/bin/env python3
"""Exact callback-only reversible boundary. No compile/game completeness assertion."""
from pathlib import Path
import hashlib,json
HERE=Path(__file__).resolve().parent;ROOT=HERE.parents[3]
MANIFEST=json.loads((HERE/'manifest.json').read_text())
FILES={f['path']:f for f in MANIFEST['files']}
def sha(t):return hashlib.sha256(t.encode()).hexdigest()
def reverse_source(relative,text):
 key=relative if relative.startswith('ntm-port-1.12.2/') else 'ntm-port-1.12.2/'+relative
 f=FILES.get(key)
 if not f:return text
 assert sha(text)==f['after_sha256'],(relative,'native drift')
 for e in reversed(f['edits']):
  assert text[e['start']:e['start']+len(e['new'])]==e['new']
  text=text[:e['start']]+e['old']+text[e['start']+len(e['new']):]
 assert sha(text)==f['before_sha256'],(relative,'baseline drift')
 return text

def verify(root=ROOT):
 rejected=0
 for f in FILES.values():
  text=(Path(root)/f['path']).read_bytes().decode();old=reverse_source(f['path'],text);new=old
  for e in reversed(f['edits']):
   assert new[e['start']:e['end']]==e['old'];new=new[:e['start']]+e['new']+new[e['end']:]
  assert text==new
  for mutation in (text+'\n',text[:-1],text.replace('nativePlacementPos.getY()','nativePlacementPos.getZ()',1)):
   assert mutation!=text
   try:reverse_source(f['path'],mutation)
   except AssertionError:rejected+=1
   else:raise AssertionError('mutation accepted')
 assert reverse_source('not-selected.java','untouched\r\n')=='untouched\r\n'
 print('PASS',len(FILES),'exact source inverses/forward replays;',rejected,'mutation rejections')
if __name__=='__main__':verify()
