#!/usr/bin/env python3
"""Exact CP83 source delta, with inverse verification and UTF-16 javac span conversion."""
import argparse,collections,hashlib,json,pathlib,subprocess
HERE=pathlib.Path(__file__).resolve().parent
ROOT=HERE.parents[2]
BASE='bdb4c897d5f554458d23c6efab036b056ee20de0'
def sha(b):return hashlib.sha256(b).hexdigest()
def text(b):return b.decode('utf-8')
def offset(s,n):return len(s.encode('utf-16-le')[:n*2].decode('utf-16-le'))
def verify(data,rec):
 assert sha(data)==rec['after_sha256'],'changed-source hash'
 s=text(data)
 for e in reversed(rec['edits']):
  p=e['after_start'];assert s[p:p+len(e['after'])]==e['after'],'exact native expression'
  s=s[:p]+e['before']+s[p+len(e['after']):]
 assert sha(s.encode())==rec['before_sha256'],'whole-file inverse'
 forward=s
 for e in reversed(rec['edits']):
  assert forward[e['start']:e['end']]==e['before']
  forward=forward[:e['start']]+e['after']+forward[e['end']:]
 assert forward.encode()==data,'byte-exact forward round trip'
 return s

def main():
 p=argparse.ArgumentParser();p.add_argument('--apply',action='store_true');p.add_argument('--strict-all',action='store_true');a=p.parse_args()
 if a.apply:
  assert subprocess.check_output(['git','rev-parse','HEAD'],cwd=ROOT,text=True).strip()==BASE
  subprocess.run(['git','diff','--exit-code',BASE,'--','src/main/java'],cwd=ROOT,check=True,stdout=subprocess.DEVNULL)
  allpaths=sorted((ROOT/'src/main/java').rglob('*.java'))
  before={str(x.relative_to(ROOT)):sha(x.read_bytes()) for x in allpaths}
  records=[json.loads(l) for l in (HERE/'proof/before.jsonl').read_text().splitlines()]
  selected=[r for r in records if r['selected']];assert len(selected)==24
  by=collections.defaultdict(list)
  for r in selected:by['src/main/java/'+r['path'].split('/src/main/java/',1)[1]].append(r)
  files={}
  for rel,rs in by.items():
   f=ROOT/rel;b=f.read_bytes();s=text(b);ed=[];delta=0
   for r in sorted(rs,key=lambda z:z['start']):
    p,q,z=(offset(s,r[k]) for k in ['start','selectorEnd','end'])
    original=s[p:z];selector=s[p:q];assert selector in ('AxisAlignedBB.getBoundingBox','net.minecraft.util.math.AxisAlignedBB.getBoundingBox')
    replacement='new '+selector[:-len('.getBoundingBox')]+s[q:z]
    assert original[original.index('('):]==replacement[replacement.index('('):]
    e=dict(r);e.pop('path');e.update(start=p,end=z,after_start=p+delta,before=original,after=replacement)
    ed.append(e);delta+=len(replacement)-len(original)
   for e in reversed(ed):s=s[:e['start']]+e['after']+s[e['end']:]
   f.write_bytes(s.encode());files[rel]={'before_sha256':sha(b),'after_sha256':sha(f.read_bytes()),'edits':ed}
  m={'baseline':BASE,'selected':24,'candidate_count':len(records),'production_input_count':len(allpaths),'files':files,'before_all_java_sha256':before}
  (HERE/'manifest.json').write_text(json.dumps(m,indent=2)+'\n')
 m=json.loads((HERE/'manifest.json').read_text());assert m['baseline']==BASE
 assert m['selected']==24 and len(m['files'])==15
 for rel,r in m['files'].items():verify((ROOT/rel).read_bytes(),r)
 if a.strict_all:
  current={str(x.relative_to(ROOT)) for x in (ROOT/'src/main/java').rglob('*.java')};assert current==set(m['before_all_java_sha256'])
  for rel,h in m['before_all_java_sha256'].items():
   if rel not in m['files']:assert sha((ROOT/rel).read_bytes())==h,rel
 rejected=0
 for rel,r in m['files'].items():
  b=(ROOT/rel).read_bytes()
  for bad in [b+b'\n',b.replace(b'new AxisAlignedBB(',b'new AxisAlignedBB(0 + ',1)]:
   try:verify(bad,r)
   except AssertionError:rejected+=1
   else:raise AssertionError('source mutation escaped')
 print('PASS 24 exact replacements / 15 whole-file inverses; 30 source mutations rejected'+('; all 4065 production inputs preserved' if a.strict_all else ''))
if __name__=='__main__':main()
