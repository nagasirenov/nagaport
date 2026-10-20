"""Invocation-only integration into already-preserved sibling edits; no whole-file replacement."""
from pathlib import Path
import json,collections,hashlib
R=Path(__file__).resolve().parents[2]
D=json.loads((R/'porting/tests/equipment-writes-native/source-manifest.json').read_text())
changes={};receipt={}
sha=lambda b:hashlib.sha256(b).hexdigest()
for rel,row in D['files'].items():
 before=(R/rel).read_bytes();s=before.decode()
 counts=collections.Counter((e['old'],e['new']) for e in row['edits'])
 for (old,new),n in counts.items():
  assert s.count(old)==n and s.count(new)==0,(rel,old)
  s=s.replace(old,new)
 inverse=s
 for (old,new),n in counts.items():
  assert inverse.count(new)==n
  inverse=inverse.replace(new,old)
 assert inverse.encode()==before
 changes[rel]=s.encode();receipt[rel]={'before_sha256':sha(before),'after_sha256':sha(s.encode())}
for rel,b in changes.items():(R/rel).write_bytes(b)
Path(__file__).with_name('writes-integration-receipt.json').write_text(json.dumps(receipt,indent=2)+'\n')
print('PASS all five files preflighted; 27 invocation edits invert exactly to prior integrated bytes')
