"""Reproduce exact original parity and full-tree named/anonymous descendant inventory."""
import argparse,json,re,hashlib
from pathlib import Path
H=Path(__file__).resolve().parent;D=json.loads((H/'selection.json').read_text())
p=argparse.ArgumentParser();p.add_argument('--original-source',type=Path,required=True);p.add_argument('--full-source',type=Path,required=True);a=p.parse_args()
def method(s):
 start=s.index('public void breakBlock(');i=s.index('{',start)+1;n=1
 while n:n+=(s[i]=='{')-(s[i]=='}');i+=1
 return s[start:i]
for e in D['entries']:
 assert method((a.original_source/e['path']).read_text())==e['original_method']
 for f in a.full_source.rglob('*.java'):
  s=f.read_text();assert not re.search(r'\bextends\s+'+e['name']+r'\b',s),(f,e['name'])
  assert not re.search(r'new\s+'+e['name']+r'\s*\([^;]*?\)\s*\{',s),(f,e['name'])
print('PASS twelve exact original callback sources; no named/anonymous descendants in supplied full source tree')
for f in a.full_source.rglob('*.java'):
 for n,line in enumerate(f.read_text().splitlines(),1):
  if '.breakBlock(' in line and 'super.breakBlock(' not in line:print(str(f.relative_to(a.full_source))+':'+str(n)+':'+line.strip())
