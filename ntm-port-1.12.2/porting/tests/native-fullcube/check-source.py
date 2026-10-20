#!/usr/bin/env python3
import collections,csv,hashlib,json,re,subprocess
from pathlib import Path
HERE=Path(__file__).resolve().parent;ROOT=HERE.parents[2];REPO=ROOT.parent;BASE='91da806c9ad17b279ef5142d83d4fe60365d563f'
def base(path):return subprocess.check_output(['git','-C',str(REPO),'show',BASE+':ntm-port-1.12.2/'+path])
def table(name,kind):return list(csv.DictReader((HERE/name/(kind+'.tsv')).open(),delimiter='\t'))
rows=json.loads((HERE/'sites.json').read_text());assert len(rows)==85 and len({r['path'] for r in rows})==85
paths={r['path'] for r in rows}
for r in rows:
 old=base(r['path']);new=(ROOT/r['path']).read_bytes();before=r['before'].encode();after=r['after'].encode()
 assert old.count(before)==new.count(after)==1,r['path']
 assert new.replace(after,before)==old,r['path']
 assert after==before.replace(b'renderAsNormalBlock()',b'isFullCube(net.minecraft.block.state.IBlockState state)')
 assert re.fullmatch(rb'public boolean renderAsNormalBlock\(\)\s*\{\s*return false;\s*}',before)
for r in json.loads((HERE/'excluded.json').read_text()):assert (ROOT/r['path']).read_bytes()==base(r['path'])
b=table('before','methods');a=table('after','methods');assert len(b)==len(a)==150
selected={}
for old,new in zip(b,a):
 if old['path'] in paths and old['parameters']=='':
  assert old['signature']=='renderAsNormalBlock()' and old['native_override']=='false'
  assert new['signature']=='isFullCube(net.minecraft.block.state.IBlockState)' and new['native_override']=='true'
  assert new['parameters']=='net.minecraft.block.state.IBlockState' and new['parameter_kinds']=='DECLARED'
  for k in ['path','line','owner','body']:assert old[k]==new[k]
  selected[old['owner']]=old
 else:assert old==new,'Unselected existing full-cube method changed'
assert len(selected)==85
bc=table('before','classes');ac=table('after','classes');assert bc==ac,'All source hierarchy records unchanged'
classes={r['owner']:r for r in bc if r['owner']};closure={}
for name in classes:
 c=name;seen=[]
 while c in classes and c not in seen:
  seen.append(c)
  if c in selected:closure[name]={'selected_ancestor':c,'chain':seen};break
  c=classes[c]['superclass']
assert closure==json.loads((HERE/'closure.json').read_text()) and len(closure)==104
for owner,record in closure.items():
 c=owner;seen=set()
 while c in classes and c not in seen:
  seen.add(c);r=classes[c];assert r['superkind']=='DECLARED' and r['block_subtype']=='true';c=r['superclass']
 assert c in ['net.minecraft.block.Block','net.minecraft.block.BlockContainer']
for row in rows:
 for ancestor in row['chain'][1:]:
  assert not any(m['owner']==ancestor and m['signature'].startswith('isFullCube(') for m in b),'Existing custom native ancestor override would need separate proof'
ancestor_paths=set()
for owner in closure:
 c=owner
 while c in classes:
  ancestor_paths.add(classes[c]['path']);c=classes[c]['superclass']
assert len(ancestor_paths)==111
assert {p for p in ancestor_paths if 'createBlockState(' in (ROOT/p).read_text()}=={
 'src/main/java/com/hbm/blocks/bomb/BlockChargeBase.java',
 'src/main/java/com/hbm/blocks/generic/BlockNTMFlower.java'}
assert not any(not c['owner'] and c['superclass'] in closure for c in bc),'No anonymous descendant omitted'
assert not any(m['owner'] in closure and m['owner'] not in selected for m in b),'No child native/legacy callback override overlooked'
assert (HERE/'before/calls.tsv').read_bytes()==(HERE/'after/calls.tsv').read_bytes()
for call in table('before','calls'):
 if call['method_kind']=='METHOD':assert call['method_owner'] not in closure,'A direct legacy call must be independently migrated first'
 else:assert call['method_kind']=='CLASS','Unresolved unrelated calls retained honestly'
def errs(name):return collections.Counter('\t'.join(x.split('\t')[:1]+x.split('\t')[2:]) for x in (HERE/name/'errors.tsv').read_text().splitlines())
before=errs('before');after=errs('after');removed=before-after
assert sum(before.values())==14742 and sum(after.values())==14662 and not after-before
assert sum(removed.values())==80
assert all(s.split('\t')[0] in paths and 'compiler.err.method.does.not.override.superclass' in s for s in removed)
for proof in json.loads((HERE/'original-provenance.json').read_text()):
 raw=subprocess.check_output(['git','-C',str(REPO),'show',BASE+':'+proof['path']]);assert hashlib.sha256(raw).hexdigest()==proof['sha256'];assert proof['method'] in raw.decode()
manifest=json.loads((HERE/'all-source-hashes.json').read_text());current={str(p.relative_to(ROOT)):hashlib.sha256(p.read_bytes()).hexdigest() for p in (ROOT/'src/main/java').rglob('*.java')};assert current==manifest
print('PASS 85 signature-only exact inverses/original false bodies, 104-owner fully resolved descendant closure, 552 anonymous-class exclusions, all full-cube declarations/calls preserved except intended native overrides, unchanged full source hashes and exactly 80 override diagnostics removed (14742 -> 14662).')
