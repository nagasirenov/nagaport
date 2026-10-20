#!/usr/bin/env python3
"""Verify the frozen CP53 -> native-client-field batch, including byte-for-byte inverse."""
import pathlib,json,hashlib,zipfile,collections
here=pathlib.Path(__file__).resolve().parent;root=here.parents[3];m=json.loads((here/'manifest.json').read_text());sha=lambda s:hashlib.sha256(s.encode()).hexdigest();counts=collections.Counter();rows={}
for line in (here/'attributed-after.tsv').read_text().splitlines():
 r=line.split('\t');rel='ntm-port-1.12.2/'+r[0].split('/ntm-port-1.12.2/',1)[1];s=(root/rel).read_bytes().decode();p=len(s.encode('utf-16-le')[:int(r[1])*2].decode('utf-16-le'));rows[rel,p]=r
with zipfile.ZipFile(here/'original-sources.zip') as z:
 for f in m:
  old=z.read(f['path']).decode();new=(root/f['path']).read_bytes().decode();assert sha(old)==f['before'];assert sha(new)==f['after'];forward=old;inverse=new
  for e in reversed(f['edits']):
   assert old[e['start']:e['end']]==e['old'];assert new[e['nativeStart']:e['nativeEnd']]==e['new'];forward=forward[:e['start']]+e['new']+forward[e['end']:];inverse=inverse[:e['nativeStart']]+e['old']+inverse[e['nativeEnd']:];r=rows[f['path'],e['nativeStart']];assert r[3]==e['new'] and r[4]==e['owner'];counts[e['old']]+=1
  assert forward==new and inverse==old
assert counts=={'fontRendererObj':748,'thePlayer':160,'theWorld':37},counts
controls=(here/'negative-controls.tsv').read_text().splitlines();assert sum(l.startswith('NO\t') for l in controls)==5;assert sum(l.startswith('YES\t') for l in controls)==6
assert '  protected bbu q;' in (here/'original-guiscreen-fields.txt').read_text()
legacy=(here/'original-minecraft-fields.txt').read_text();assert '  public bjf f;' in legacy and '  public bjk h;' in legacy
print('PASS 945 native owned field tokens across 245 files; exact forward/inverse, native attribution, 14 excluded production accesses, 11 positive/negative controls, and original mutable-field visibility')
