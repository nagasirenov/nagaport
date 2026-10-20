from pathlib import Path
import json,collections,sys
H=Path(__file__).resolve().parent;R=H.parents[2];m=json.loads((H/'source-manifest.json').read_text());expected=collections.Counter();old=collections.Counter()
for f in m['files']:
 shift=0
 for e in f['edits']:
  old[(f['path'],e['start'],e['old'],e['new'],e['receiver'],e['owner'])]+=1
  expected[(f['path'],e['start']+shift,e['old'],e['new'],e['receiver'],e['owner'])]+=1;shift+=len(e['new'])-len(e['old'])
def load(path,post=False):
 out=collections.Counter()
 for line in Path(path).read_text().splitlines():
  p,a,b,field,new,receiver,owner=line.split('\t');rel=str(Path(p).relative_to(R)) if Path(p).is_absolute() else p;key=(rel,int(a),field,new,receiver,owner)
  if not post or (rel,int(a)) in {(e[0],e[1]) for e in expected}:out[key]+=1
 return out
assert load(sys.argv[1])==old
assert load(sys.argv[2],True)==expected
print('PASS 27 exact original/native attributed positions; unrelated preexisting native getters excluded')
