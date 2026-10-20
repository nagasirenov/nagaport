#!/usr/bin/env python3
import json,zipfile,hashlib,re
from pathlib import Path
HERE=Path(__file__).resolve().parent;ROOT=HERE.parents[2]
def verify(root=ROOT):
 m=json.loads((HERE/'manifest.json').read_text());h=lambda b:hashlib.sha256(b).hexdigest()
 assert h((root/m['helper']).read_bytes())==m['helper_sha256']
 with zipfile.ZipFile(HERE/'baseline.zip')as z:
  for f,edits in m['files'].items():
   old=z.read(f);new=old.decode();assert h(old)==edits[0]['before_sha256']
   for e in reversed(edits):assert new[e['start']:e['end']]==e['old'];new=new[:e['start']]+e['new']+new[e['end']:]
   actual=(root/f).read_bytes()
   if f=='src/main/java/com/hbm/blocks/network/PneumoTubePaintableBlock.java' and h(actual)!=edits[0]['after_sha256']:
    # CP89 changes a different call in Pneumo. Invert only its independently pinned complete-file migration.
    import importlib.util
    spec=importlib.util.spec_from_file_location('player_chunk_update_audit',HERE.parent/'player-chunk-update-native'/'audit.py')
    later=importlib.util.module_from_spec(spec);spec.loader.exec_module(later)
    actual=later.original_bytes(f,actual)
   assert actual==new.encode(),f;assert h(actual)==edits[0]['after_sha256']
   for e in edits:assert new[e['start']:e['start']+len(e['new'])]==e['new'];new=new[:e['start']]+e['old']+new[e['start']+len(e['new']):]
   assert new.encode()==old,f
   assert not re.search(r'\b(?:getCollisionBoundingBox|getBoundingBox)\s*\(',actual.decode()),f
 for f,d in m['closure_sha256'].items():assert h((root/f).read_bytes())==d,f
 paths=list((root/'src/main/java').rglob('*.java'));alltext='\n'.join(p.read_text()for p in paths)
 owners=['BlockCablePaintable','BlockOpenComputersCablePaintable','PneumoTubePaintableBlock','FluidDuctPaintable','FluidDuctPaintableBlockExhaust']
 for owner in owners:
  assert len(re.findall(r'new\s+'+owner+r'\s*\(',alltext))==1,owner
  assert not re.search(r'extends\s+'+owner+r'\b',alltext),owner
 assert len(re.findall(r'\.onScrew\(',alltext))==3
 assert not re.search(r'\b(?:getCollisionBoundingBox|getBoundingBox)\s*\(', (root/'src/main/java/com/hbm/blocks/network/FluidDuctBase.java').read_text())
 print('PASS: five complete-file exact inverses and one helper hash; typed-tool closure files, exact owner construction counts, no owner subclasses or collision overrides')
 return m
if __name__=='__main__':verify()
