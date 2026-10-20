#!/usr/bin/env python3
import argparse,collections,hashlib,json,pathlib,re,subprocess
H=pathlib.Path(__file__).resolve().parent;R=H.parents[2]
BASE='27dceb0ef9e5815de3dc5f0494ca43aef9997370';LAYER='23630f8019bbf0ae17d92b1a430e97c7afd3f243'
def sha(b):return hashlib.sha256(b).hexdigest()
def read(p):return [json.loads(l) for l in p.read_text().splitlines()]
def rel(p):return 'src/main/java/'+p.split('/src/main/java/',1)[1]
def pos(s,n):return len(s.encode('utf-16-le')[:n*2].decode('utf-16-le'))
def norm(s):
 s=re.sub(r'\s+','',s).replace('.getEntityBoundingBox()','.boundingBox').replace('newAxisAlignedBB(','AxisAlignedBB.getBoundingBox(').replace('event.getEntity()','event.entity')
 return 'boundingBox' if s=='this.boundingBox' else s

def inverse(b,r):
 assert sha(b)==r['after_sha256'],'changed source'
 s=b.decode()
 for e in reversed(r['edits']):
  a=e['after_start'];assert s[a:a+4]=='grow';s=s[:a]+'expand'+s[a+4:]
 assert sha(s.encode())==r['before_sha256'],'whole-file inverse'
 return s.encode()
def main():
 p=argparse.ArgumentParser();p.add_argument('--apply',action='store_true');p.add_argument('--strict-all',action='store_true');a=p.parse_args()
 if a.apply:
  assert subprocess.check_output(['git','rev-parse','HEAD'],cwd=R,text=True).strip()==BASE
  assert subprocess.check_output(['git','write-tree'],cwd=R,text=True).strip()==LAYER
  subprocess.run(['git','diff','--exit-code','--','src/main/java'],cwd=R,check=True,stdout=subprocess.DEVNULL)
  before=read(H/'before.jsonl');original=read(H/'original.jsonl');selected=[];by=collections.defaultdict(list)
  for x in before:
   if not(x['name']=='expand' and x['candidate']):continue
   rr=rel(x['path']);matches=[y for y in original if rel(y['path'])==rr and y['name']=='expand' and y['method']==x['method'] and y['classes']==x['classes'] and norm(y['receiver'])==norm(x['receiver']) and list(map(norm,y['arguments']))==list(map(norm,x['arguments']))]
   assert len(matches)==1,(rr,x,len(matches));x=dict(x);x['original']=matches[0];by[rr].append(x);selected.append(x)
  assert len(selected)==32 and len(by)==26
  allhashes={str(p.relative_to(R)):sha(p.read_bytes()) for p in sorted((R/'src/main/java').rglob('*.java'))};files={}
  for rr,rs in by.items():
   p=R/rr;b=p.read_bytes();s=b.decode();delta=0;ed=[]
   original_path='ntm-original-pristine/'+rr;ob=subprocess.check_output(['git','show',BASE+':'+original_path],cwd=R)
   for x in sorted(rs,key=lambda x:x['nameStart']):
    aa,bb=pos(s,x['nameStart']),pos(s,x['nameEnd']);assert s[aa:bb]=='expand';x.update(start=aa,end=bb,after_start=aa+delta);ed.append(x);delta-=2
    os=pathlib.Path(x['original']['path']).read_bytes();assert os==ob,'pristine original bytes';x['original']['source_sha256']=sha(ob)
   for x in reversed(ed):s=s[:x['start']]+'grow'+s[x['end']:]
   p.write_bytes(s.encode());files[rr]={'before_sha256':sha(b),'after_sha256':sha(s.encode()),'edits':ed}
  m={'baseline':BASE,'prerequisite_layer_tree':LAYER,'prerequisite_patch_sha256':'cb9ea2fa3984e38839ab73c04780cd86128e64ab03a0a9e5cd69fd17110af7f3','selected':32,'files':files,'before_all_java_sha256':allhashes};(H/'manifest.json').write_text(json.dumps(m,indent=2)+'\n')
 m=json.loads((H/'manifest.json').read_text());mutants=0
 for rr,r in m['files'].items():
  b=(R/rr).read_bytes();inverse(b,r)
  for bad in [b+b'\n',b.replace(b'.grow(',b'.expand(',1)]:
   try:inverse(bad,r)
   except AssertionError:mutants+=1
   else:raise AssertionError('source mutant escaped')
 if a.strict_all:
  current={str(p.relative_to(R)):sha(p.read_bytes()) for p in (R/'src/main/java').rglob('*.java')};assert set(current)==set(m['before_all_java_sha256'])
  for rr,h in m['before_all_java_sha256'].items():
   if rr not in m['files']:assert current[rr]==h,rr
 print('PASS 32 original-provenance expand-to-grow tokens / 26 exact source inverses / '+str(mutants)+' source mutants'+(' / all 4062 production inputs retained' if a.strict_all else ''))
if __name__=='__main__':main()
