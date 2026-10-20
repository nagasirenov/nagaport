#!/usr/bin/env python3
"""Exact regenerate/inverse guard for the bounded CP81 write-call transform."""
import hashlib,json,zipfile
from pathlib import Path
HERE=Path(__file__).resolve().parent
ROOT=HERE.parents[2]
def verify(root=ROOT):
 m=json.loads((HERE/'manifest.json').read_text())
 with zipfile.ZipFile(HERE/'baseline.zip')as z:
  count=0
  for rel,edits in m['files'].items():
   before=z.read(rel);assert hashlib.sha256(before).hexdigest()==m['sha256'][rel],rel
   expected=before.decode();inverse=(root/rel).read_bytes().decode();shift=0
   for e in sorted(edits,key=lambda e:e['start'],reverse=True):
    assert expected[e['start']:e['end']]==e['original'],rel
    expected=expected[:e['start']]+e['replacement']+expected[e['end']:]
   assert (root/rel).read_bytes()==expected.encode(),rel+' differs beyond the exact selected calls'
   for e in sorted(edits,key=lambda e:e['start'],reverse=True):
    # Compute relocated offset from all preceding replacements, preserving repeated calls.
    start=e['start']+sum(len(p['replacement'])-len(p['original'])for p in edits if p['start']<e['start'])
    assert inverse[start:start+len(e['replacement'])]==e['replacement'],rel
    inverse=inverse[:start]+e['original']+inverse[start+len(e['replacement']):]
   assert inverse.encode()==before,rel+' failed exact inverse';count+=len(edits)
  helper='src/main/java/com/hbm/world/WorldBlockState.java'
  assert hashlib.sha256((root/helper).read_bytes()).hexdigest()==m['helper_sha256'],'domain helper changed'
  original=z.read(helper).decode();target=(root/helper).read_bytes().decode()
  method=target[target.index('    /**\n     * Write an already'):target.index('    /** Metadata belongs')]
  assert target.replace(method,'').replace('/** Native states within','/** Native state reads within')==original,'existing read/metadata behavior changed'
 assert count==36 and len(m['files'])==17 and len(m['excluded'])==15
 for e in m['excluded']:assert e['original'] in (root/e['file']).read_bytes().decode(),'gated metadata call changed: '+e['file']
 print('PASS: 36 exact selected call regenerations and inverses across 17 files; helper additions only; 15 metadata gates unchanged')
 return m
if __name__=='__main__':verify()
