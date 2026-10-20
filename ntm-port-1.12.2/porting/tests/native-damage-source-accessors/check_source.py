#!/usr/bin/env python3
import argparse,collections,hashlib,json,re,subprocess
from pathlib import Path
from migrate import read_rows,NAMES

def sha(data):return hashlib.sha256(data).hexdigest()
def check(root,manifest,original=None,attribution=None):
 expected=collections.Counter();tokens=0
 for f in manifest['files']:
  raw=(root/f['path']).read_bytes();assert sha(raw)==f['after_sha256'],('changed output',f['path'])
  units=raw.decode('utf-8').encode('utf-16-le')
  for e in reversed(f['edits']):
   assert units[e['after_start']*2:e['after_end']*2].decode('utf-16-le')==e['new'],e
   units=units[:e['after_start']*2]+e['old'].encode('utf-16-le')+units[e['after_end']*2:];tokens+=1
   context=e['context'].replace('#'+e['old']+'()','#'+e['new']+'()')
   expected[(f['path'],e['kind'],e['after_start'],e['new'],e['receiver'],context)]+=1
  assert sha(units.decode('utf-16-le').encode('utf-8'))==f['before_sha256'],('inverse changed unrelated bytes',f['path'])
  if original:
   text=(original/f['path']).read_text()
   for old in NAMES:assert len(re.findall(r'\b'+old+r'\s*\(',text))==sum(e['old']==old for e in f['edits']),('original legacy inventory',f['path'],old)
 if original:
  rel='src/main/java/com/hbm/items/weapon/sedna/DamageSourceSednaWithAttacker.java';old=(original/rel).read_text();now=(root/rel).read_text()
  for source,target in NAMES.items():
   a=re.search(r'@Override public Entity '+source+r'\(\) \{[^}]*\}',old).group();b=re.search(r'@Override public Entity '+target+r'\(\) \{[^}]*\}',now).group();assert a.replace(source,target)==b
 if attribution:
  actual=collections.Counter();rows=read_rows(attribution,root)
  for row in rows:
   assert row['old'] not in NAMES,('legacy call/override remains',row)
   if any(row['path']==u['path'] and row['context']==u['context'] for u in manifest['untouched']):continue
   assert row['owner'] in {'net.minecraft.util.DamageSource','net.minecraft.util.EntityDamageSource','net.minecraft.util.EntityDamageSourceIndirect','com.hbm.items.weapon.sedna.DamageSourceSednaWithAttacker'},row
   actual[(row['path'],row['kind'],row['start'],row['old'],row['receiver'],row['context'])]+=1
  assert actual==expected,(actual-expected,expected-actual)
 print('PASS',tokens,'exact source-token inverses across',len(manifest['files']),'files; original inventory and Sedna override bodies retained' if original else 'files')
 if attribution:print('PASS every changed token resolves to actual native/custom declaration, all exact positions, original receiver type and enclosing method; 1 pre-existing native site untouched')

if __name__=='__main__':
 p=argparse.ArgumentParser();p.add_argument('--module',type=Path,required=True);p.add_argument('--manifest',type=Path,default=Path(__file__).with_name('source-manifest.json'));p.add_argument('--original',type=Path);p.add_argument('--attribution',type=Path);a=p.parse_args();check(a.module.resolve(),json.loads(a.manifest.read_text()),a.original,a.attribution)
