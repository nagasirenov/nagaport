#!/usr/bin/env python3
"""Only replace manifest-approved selectors. Optional --apply; default verifies migrated scope."""
import pathlib,json,sys,hashlib,collections
root=pathlib.Path(__file__).resolve().parents[3];rows=json.loads(pathlib.Path(__file__).with_name('manifest.json').read_text());groups=collections.defaultdict(list)
probe=pathlib.Path(__file__).with_name('BoxFactoryProbe.java').read_text()
for i,r in enumerate(rows):
 args=r['before'].split('getBoundingBox',1)[1]
 assert f'candidate(azt.a{args},new AxisAlignedBB{args},{i});' in probe, r
 groups[r['path']].append(r)
for name,rs in groups.items():
 p=root/name; raw=p.read_bytes();s=raw.decode()
 if '--apply' in sys.argv:
  assert hashlib.sha256(raw).hexdigest()==rs[0]['sha256'],name
  for r in sorted(rs,key=lambda r:r['start'],reverse=True):
   assert s[r['start']:r['end']]==r['before'],r
   s=s[:r['start']]+r['after']+s[r['end']:]
  p.write_bytes(s.encode())
 else:
  delta=0; edits=[]
  for r in sorted(rs,key=lambda r:r['start']):
   start=r['start']+delta;end=start+len(r['after']);assert s[start:end]==r['after'],r;edits.append((start,end,r));delta+=len(r['after'])-len(r['before'])
  for start,end,r in reversed(edits):s=s[:start]+r['before']+s[end:]
  assert hashlib.sha256(s.encode()).hexdigest()==rs[0]['sha256'],name
print('PASS exact reversible literal-only scope:',len(rows),'calls;',len(groups),'files')
