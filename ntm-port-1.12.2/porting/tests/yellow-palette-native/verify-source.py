#!/usr/bin/env python3
"""Dependency-free, byte-exact reconstruction check; not a Java/native runtime test."""
from pathlib import Path
import hashlib,json,zipfile
H=Path(__file__).resolve().parent;M=H.parents[2]
def sha(b):return hashlib.sha256(b).hexdigest()
manifest=json.loads((H/'manifest.json').read_text());rows=json.loads((H/'selection.json').read_text())
assert len(rows)==manifest['placements']==105
with zipfile.ZipFile(H/'baseline.zip') as z:
 for path,hashes in manifest['files'].items():
  before=z.read(path);assert sha(before)==hashes['before'];out=before.decode()
  selected=[r for r in rows if r['file']==path];assert len(selected)==hashes['placements']
  for r in reversed(selected):
   assert out[r['start']:r['end']]==r['old']
   out=out[:r['start']]+r['new']+out[r['end']:]
  assert sha(out.encode())==hashes['after']
  assert (M/path).read_bytes()==out.encode()
for path,digest in json.loads((H/'dependency-hashes.json').read_text()).items():assert sha((M/path).read_bytes())==digest,path
print('PASS reconstructed source check: 105 exact replacements and all preserved dependency hashes. No native execution claimed.')
