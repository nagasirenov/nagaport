import pathlib,json,collections
D=pathlib.Path(__file__).resolve().parent;root=D.parents[2];rows=json.loads((D/'manifest.json').read_text());native=set()
for l in (D/'attribution-native.tsv').read_text().splitlines():
 p,s,e,desc=l.split('\t');native.add(('src/main/java/'+p.split('/src/main/java/',1)[1],int(s),int(e),desc))
g=collections.defaultdict(list)
for r in rows:g[r['path']].append(r)
for p,rs in g.items():
 delta=0
 for r in sorted(rs,key=lambda r:r['start']):
  s=r['start']+delta;e=s+len(r['after']);assert (p,s,e,'(DDDDDD)V') in native,(p,s,e);delta+=len(r['after'])-len(r['before'])
print('PASS all 74 migrated production expressions attributed to actual native AxisAlignedBB constructor (DDDDDD)V')
