"""Line-insensitive diagnostic comparison; not a gameplay regression oracle."""
import re,collections,json
from pathlib import Path
H=Path(__file__).resolve().parent

def read(p):
 s=p.read_text();heads=list(re.finditer(r'^([^\n]+?\.java):\d+: error: ([^\n]*)$',s,re.M));c=collections.Counter()
 for i,h in enumerate(heads):
  body=s[h.end():heads[i+1].start() if i+1<len(heads) else len(s)]
  rel=h[1].split('/build/sources/main/java/')[-1]
  sym=re.search(r'^\s*symbol:\s+([^\n]*)',body,re.M);loc=re.search(r'^\s*location:\s+([^\n]*)',body,re.M)
  c[(rel,h[2],sym[1] if sym else '',loc[1] if loc else '')]+=1
 return c

a=read(H.parent/'checkpoint90/full-compile.log');b=read(H/'full-compile.log')
assert sum(a.values())==10831 and sum(b.values())==10583
added=b-a;removed=a-b
rows=lambda c:[dict(file=k[0],message=k[1],symbol=k[2],location=k[3],count=v)for k,v in c.items()]
out={'before':sum(a.values()),'after':sum(b.values()),'new_diagnostics_count':sum(added.values()),'removed_diagnostics_count':sum(removed.values()),'new':rows(added),'removed':rows(removed)}
(H/'diagnostic-delta.json').write_text(json.dumps(out,indent=2)+'\n')
print('PASS 253 removed / five newly exposed unresolved GUI hit-test calls; net reduction248')
assert sum(removed.values())==253 and sum(added.values())==5
assert all(k[1:]==('cannot find symbol','method func_146978_c(int,int,int,int,int,int)','') for k in added)
