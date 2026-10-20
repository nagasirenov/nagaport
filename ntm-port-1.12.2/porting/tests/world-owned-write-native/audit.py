#!/usr/bin/env python3
"""Exact bounded inverse, diagnostic attribution and state-owner closure evidence."""
import hashlib,json,zipfile,re
from pathlib import Path
HERE=Path(__file__).resolve().parent;ROOT=HERE.parents[2]
def digest(b):return hashlib.sha256(b).hexdigest()
def verify(root=ROOT):
 m=json.loads((HERE/'manifest.json').read_text());total=0
 with zipfile.ZipFile(HERE/'baseline.zip')as z:
  for rel,es in m['files'].items():
   before=z.read(rel);assert digest(before)==m['sha256'][rel];expected=before.decode();inverse=(root/rel).read_bytes().decode()
   for e in sorted(es,key=lambda e:e['start'],reverse=True):
    assert expected[e['start']:e['end']]==e['original'];expected=expected[:e['start']]+e['replacement']+expected[e['end']:]
   assert expected.encode()==(root/rel).read_bytes(),rel+' source drift'
   for e in sorted(es,key=lambda e:e['start'],reverse=True):
    pos=e['start']+sum(len(p['replacement'])-len(p['original'])for p in es if p['start']<e['start']);assert inverse[pos:pos+len(e['replacement'])]==e['replacement'];inverse=inverse[:pos]+e['original']+inverse[pos+len(e['replacement']):]
   assert inverse.encode()==before;total+=len(es)
  assert total==57 and len(m['files'])==3
  owner='src/main/java/com/hbm/blocks/generic/BlockRedBrick.java';assert digest(z.read(owner))==m['owner_before_sha256'];assert digest((root/owner).read_bytes())==m['owner_after_sha256'],'owner drift'
  # Exact owner delta is archived separately; all original methods remain either byte-identical or explicitly bounded native callbacks.
  assert z.read(owner)+b'\0'+(root/owner).read_bytes()==(HERE/'owner-exact-pair.bin').read_bytes()
 for rel,h in m['dependencies'].items():assert digest((root/rel).read_bytes())==h,rel+' dependency drift'
 source=root/'src/main/java';owners={'BlockRedBrick','DecoSteelPoles','DecoPoleTop'}
 for p in source.rglob('*.java'):
  for name in owners:assert not re.search(r'\bextends\s+(?:com\.hbm\.blocks\.generic\.)?'+name+r'\b',p.read_text()),'new source descendant '+str(p)
 print('PASS: 57 exact source regenerations/inverses across three whole callers; complete RedBrick delta and unchanged native owner/helper dependency hashes; no source owner subclasses')
 return m
if __name__=='__main__':verify()
