#!/usr/bin/env python3
import argparse,collections,csv,gzip,hashlib,json,re,zipfile
from pathlib import Path
p=argparse.ArgumentParser();p.add_argument('--project',type=Path,default=Path(__file__).resolve().parents[3]);p.add_argument('--attribution',type=Path);a=p.parse_args();e=Path(__file__).resolve().parent;m=json.loads((e/'manifest.json').read_text());sha=lambda x:hashlib.sha256(x).hexdigest()
with zipfile.ZipFile(e/'sources.zip') as z:
 for path,data in m['files'].items():
  before=z.read(path);after=(a.project/'src/main/java'/path).read_bytes();assert sha(before)==data['before'];assert sha(after)==data['after'],path
  assert after.count(m['new_selector'].encode())==data['calls'];assert after.replace(m['new_selector'].encode(),m['old_selector'].encode())==before,path
for path,h in m['fixed_sources'].items():assert sha((a.project/path).read_bytes())==h,path
print('PASS 157 exact selectors across 83 whole files; full inverse, untouched comment, fixed native helper/resolver sources.')
if a.attribution:
 def rows(n):
  path=a.attribution/n
  if path.exists():return list(csv.DictReader(path.open(),delimiter='\t'))
  return list(csv.DictReader(gzip.open(str(path)+'.gz','rt'),delimiter='\t'))
 b=rows('before.tsv');c=rows('after.tsv');assert len(b)==len(c)==314
 for old,new in zip(b,c):
  assert old['path']==new['path'] and old['line']==new['line']
  assert new['method_element_kind']=='METHOD'
  if old['method']=='func_147674_a':
   assert old['receiver_type']=='net.minecraft.client.audio.PositionedSoundRecord' and old['receiver_element_kind']=='CLASS'
   assert new['method_owner']=='com.hbm.sound.GuiSound' and new['method_signature']=='create(net.minecraft.util.ResourceLocation,float)'
   for f in ['arguments_source','arguments_tree_kind','arguments_kind','arguments_type']:assert old[f]==new[f],(old['path'],f)
  else:
   assert old['method']==new['method']=='playSound'
   for f in ['receiver_source','receiver_tree_kind','receiver_kind','receiver_type','method_owner','method_signature']:assert old[f]==new[f],(old['path'],f)
   assert new['method_owner']=='net.minecraft.client.audio.SoundHandler'
  assert new['full_invocation'].replace(m['new_selector'],m['old_selector'])==old['full_invocation']
  assert new['enclosing_method_source'].replace(m['new_selector'],m['old_selector'])==old['enclosing_method_source']
 before=collections.Counter((x['path'],x['line'],x['code'],x['message']) for x in rows('before-diagnostics.tsv'));after=collections.Counter((x['path'],x['line'],x['code'],x['message']) for x in rows('after-diagnostics.tsv'));assert not after-before;removed=before-after;assert sum(removed.values())==157;assert all('func_147674_a' in x[3] for x in removed)
 print('PASS full genuine-source binding: 157 unchanged complete argument pairs/receiver/method bodies; 157 genuine native helper and handler destinations. 15952 -> 15795 diagnostics, no other diagnostic changes. Not a successful whole-mod build.')
