#!/usr/bin/env python3
import hashlib,json,re
from pathlib import Path
H=Path(__file__).resolve().parent;R=H.parents[2]
def verify():
 m=json.loads((H/'manifest.json').read_text());count=0
 worlds=[p for p in (R/'src/main/java').rglob('*.java') if re.search(r'extends\s+(?:net\.minecraft\.world\.)?(?:World|WorldServer|WorldClient)\b',p.read_text())]
 assert [str(p.relative_to(R)) for p in worlds]==['src/main/java/com/hbm/wiaj/FauxWorld.java']
 assert not re.search(r'\b(?:spawnEntityInWorld|spawnEntity)\s*\(',worlds[0].read_text())
 print('PASS owned source World subclass audit: only FauxWorld, no spawn override')
 for rel,info in m['files'].items():
  raw=(R/rel).read_bytes();assert hashlib.sha256(raw).hexdigest()==info['after'],rel
  s=raw.decode();delta=0;inverse=[]
  for e in sorted(info['edits'],key=lambda e:e['start']):
   a=e['start']+delta;b=a+len(e['new']);assert s[a:b]==e['new'];inverse.append((a,b,e['old']));delta+=len(e['new'])-len(e['old']);count+=1
  for a,b,old in reversed(inverse):s=s[:a]+old+s[b:]
  assert hashlib.sha256(s.encode()).hexdigest()==info['before'],rel
 assert count==43 and len(m['files'])==17
 print('PASS exact inverse: only 43 method-name tokens in 17 files; receivers, argument expressions, ordering, surrounding control flow and ignored returns unchanged')
 return m
if __name__=='__main__':verify()
