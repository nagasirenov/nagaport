#!/usr/bin/env python3
"""Frozen callback-only inverse and complete attributed inheritance/caller closure."""
from pathlib import Path
import collections,hashlib,json,re,subprocess,tempfile
HOME=Path(__file__).resolve().parent;ROOT=HOME.parents[3];DATA=json.loads((HOME/'source-manifest.json').read_text())
def sha(b):return hashlib.sha256(b).hexdigest()
def baseline(p):return subprocess.check_output(['git','-C',str(ROOT),'show',DATA['base']+':'+p])
def method(source,name):
 m=re.search(r'(?:public|protected)\s+(?:void|double|float)\s+'+re.escape(name)+r'\s*\(',source);assert m,name
 start=m.start();opening=source.index('{',m.end());end=opening+1;depth=1
 while depth:depth+=(source[end]=='{')-(source[end]=='}');end+=1
 return source[start:end]
def verify_file(p,current):
 f=DATA['files'][p];before=baseline(p);assert sha(before)==f['before_sha256'];assert sha(current)==f['after_sha256'],p
 restored=current.decode().encode('utf-16-le');delta=0
 for e in sorted(f['edits'],key=lambda e:e['ns']):
  start=e['ns']+delta;assert restored[start*2:(start+5)*2].decode('utf-16-le')=='shoot'
  restored=restored[:2*start]+'setThrowableHeading'.encode('utf-16-le')+restored[(start+5)*2:]
  # Each reversal restores original indexing for all later identifiers.
 assert restored.decode('utf-16-le').encode()==before,p
 return before

def main(composed=False):
 aggregate=None
 if composed:
  import importlib.util
  spec=importlib.util.spec_from_file_location('cp88_aggregate',HOME.parent/'check-native-shoot-sync-machine-source.py')
  aggregate=importlib.util.module_from_spec(spec);spec.loader.exec_module(aggregate);aggregate.verify()
  assert aggregate.MANIFEST['baseline']==DATA['base']
 count=0
 for p,f in DATA['files'].items():
  current=(ROOT/p).read_bytes();before=verify_file(p,current);original=(ROOT/p.replace('ntm-port-1.12.2/','ntm-original-pristine/')).read_bytes();assert sha(original)==f['pristine_sha256']
  count+=len(f['edits'])
  for e in f['edits']:
   assert e['name']=='setThrowableHeading' and e['accept']
   old=before.decode().encode('utf-16-le')[2*e['ns']:2*e['ne']].decode('utf-16-le');assert old=='setThrowableHeading'
   if e['kind']=='declaration':assert method(original.decode(),'setThrowableHeading').replace('MathHelper.sqrt_double','MathHelper.sqrt')==method(before.decode(),'setThrowableHeading')
 assert count==33 and len(DATA['files'])==16
 # Typed full source census has no hidden/nested callback override; preserves all 90 classes.
 before=[json.loads(x) for x in (HOME/'before-attribution.jsonl').read_text().splitlines()];after=[json.loads(x) for x in (HOME/'after-attribution.jsonl').read_text().splitlines()]
 def owned(r):return r.get('accept') and r.get('type')!='com.hbm.entity.projectile.EntitySiegeLaser'
 expected={(p,e['kind'],e['ns']) for p,f in DATA['files'].items() for e in f['edits']}
 assert {(r['path'],r['kind'],r['ns']) for r in before if owned(r)}==expected
 actual=set()
 for p,f in DATA['files'].items():
  delta=0
  for e in sorted(f['edits'],key=lambda e:e['ns']):actual.add((p,e['kind'],e['ns']+delta));delta+=len('shoot')-len('setThrowableHeading')
 assert {(r['path'],r['kind'],r['ns']) for r in after if owned(r)}==actual
 after_by={(r['path'],r['kind'],r['ns']):r for r in after if owned(r)}
 for p,f in DATA['files'].items():
  delta=0
  for e in sorted(f['edits'],key=lambda e:e['ns']):
   r=after_by[p,e['kind'],e['ns']+delta];assert r['owner']==e['owner'] and r['type']==e['type'],(p,e,r)
   delta+=len('shoot')-len('setThrowableHeading')
 assert not any(r.get('kind')=='declaration' and r.get('name')=='shoot' for r in before),'Pre-existing shoot overload/override collision'
 assert {r['owner'] for r in before if r.get('kind')=='declaration' and r.get('name')=='setThrowableHeading'}=={r['owner'] for r in before if owned(r) and r['kind']=='declaration'}|{'com.hbm.entity.mob.glyphid.EntityGlyphidBrawler'}
 roots={r['name'] for r in before if r['kind']=='class' and 'net.minecraft.entity.IProjectile' in r['interfaces']};assert all(r['parent']=='net.minecraft.entity.Entity' for r in before if r['kind']=='class' and r['name'] in roots)
 # Complete current-tree lexical census guards additions outside the frozen family too.
 calls=[]
 for p in (ROOT/'ntm-port-1.12.2/src/main/java').rglob('*.java'):
  calls.extend((str(p.relative_to(ROOT)),m.group()) for m in re.finditer(r'\bsetThrowableHeading\s*\(',p.read_text()))
 assert len(calls)==3 and collections.Counter(p for p,_ in calls)=={'ntm-port-1.12.2/src/main/java/com/hbm/entity/mob/glyphid/EntityGlyphidBrawler.java':2,'ntm-port-1.12.2/src/main/java/com/hbm/entity/mob/siege/EntitySiegeCraft.java':1}

 assert collections.Counter((r['kind'],r['name']) for r in after if owned(r))=={('declaration','shoot'):7,('call','shoot'):26}
 classes={r['name']:r for r in before if r['kind']=='class'};family={n for n,e in classes.items() if 'net.minecraft.entity.IProjectile' in e['interfaces']};assert len(family)==7
 while True:
  newer=family|{n for n,e in classes.items() if e['parent'] in family}
  if newer==family:break
  family=newer
 assert family=={r['name'] for r in DATA['hierarchy']} and len(family)==90
 assert [{k:v for k,v in r.items() if k!='sha256'} for r in DATA['hierarchy']]==[{k:v for k,v in classes[n].items() if k!='kind'} for n in sorted(family)]
 for c in DATA['hierarchy']:assert sha((ROOT/c['path']).read_bytes())==c['sha256']
 for name in ('setThrowableHeading','shoot'):
  declarations=[r for r in before if r.get('kind')=='declaration' and r.get('name')==name and r.get('owner') in family]
  assert len(declarations)==(7 if name=='setThrowableHeading' else 0)
 # Local different-domain method, legacy alternate bullet method and the native-engine delta remain untouched.
 for p in ['ntm-port-1.12.2/src/main/java/com/hbm/entity/mob/glyphid/EntityGlyphidBrawler.java','ntm-port-1.12.2/src/main/java/com/hbm/entity/mob/siege/EntitySiegeCraft.java','ntm-port-1.12.2/src/main/java/com/hbm/entity/projectile/EntitySiegeLaser.java']:
  current=(ROOT/p).read_bytes()
  if composed:
   current=aggregate.reverse_source(p[len('ntm-port-1.12.2/') :],current.decode()).encode()
  assert current==baseline(p),p
 bullet='ntm-port-1.12.2/src/main/java/com/hbm/entity/projectile/EntityBullet.java';assert method((ROOT/bullet).read_text(),'setThrowableHeading2')==method(baseline(bullet).decode(),'setThrowableHeading2')
 # A changed formula, forgotten call, added bridge, unreviewed subclass override or unrelated byte fails the frozen proof.
 p=next(p for p in DATA['files'] if p.endswith('/EntityThrowableNT.java'));s=(ROOT/p).read_bytes()
 for mutation in [s.replace(b'0.0075D',b'0.0074D',1),s.replace(b'this.shoot(',b'this.setThrowableHeading(',1),s.replace(b'public void shoot(',b'public void setThrowableHeading(',1),s+b'\n// unrelated change\n']:
  try:verify_file(p,mutation)
  except AssertionError:pass
  else:raise AssertionError('negative source control escaped')
 with tempfile.TemporaryDirectory(prefix='projectile-source-inverse-') as d:
  for p in DATA['files']:
   dest=Path(d)/p;dest.parent.mkdir(parents=True,exist_ok=True);dest.write_bytes((ROOT/p).read_bytes())
  subprocess.run(['git','apply','--check',str(HOME/'source-inverse.patch')],cwd=d,check=True);subprocess.run(['git','apply',str(HOME/'source-inverse.patch')],cwd=d,check=True)
  for p in DATA['files']:assert (Path(d)/p).read_bytes()==baseline(p)
 print('PASS 33 identifier-only edits / 16 files; full-file inverse; 7 pristine callback bodies; typed 90-class closure; 26 resolved callers; 4 source negative controls; SiegeLaser/Brawler/heading2 exclusions unchanged')
if __name__=='__main__':main()
