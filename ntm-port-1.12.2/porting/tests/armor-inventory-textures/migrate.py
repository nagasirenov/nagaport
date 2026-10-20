#!/usr/bin/env python3
"""Compiler-attributed, receiver-exact, reversible armor item texture declarations only."""
import argparse,csv,hashlib,json,pathlib,re,subprocess
HERE=pathlib.Path(__file__).resolve().parent;ROOT=HERE.parents[2];REPO=ROOT.parent
BASE='df18fc077648dd18e6d0c1055d82c11dc2ebdb78'
FILES=['src/main/java/com/hbm/items/ModItemsArmor.java','src/main/java/com/hbm/items/armor/ArmorFSB.java']
IMPORT='import com.hbm.render.item.NativeItemTextureRegistry;\n'
def digest(value):return hashlib.sha256(value.encode() if isinstance(value,str) else value).hexdigest()
def raw(s):return s.replace('\\n','\n').replace('\\r','\r').replace('\\t','\t').replace('\\\\','\\')
def baseline(path):return subprocess.check_output(['git','show',BASE+':ntm-port-1.12.2/'+path],cwd=REPO).decode()
def transform():
 rows=list(csv.DictReader((HERE/'before.tsv').open(),delimiter='\t'));assert len(rows)==129
 result={};manifest={'baseline':BASE,'files':{},'selected':[],'excluded':[]}
 for path in FILES:
  old=baseline(path);edits=[]
  for row in rows:
   if row['path']!=path:continue
   start,end=int(row['start']),int(row['end']);call=old[start:end];assert call==raw(row['full_invocation'])
   receiver=raw(row['receiver_source']);arg=raw(row['arguments_source'])
   assert row['argument_count']=='1' and row['arguments_kind']=='DECLARED' and row['arguments_type']=='java.lang.String'
   assert re.fullmatch(r'RefStrings.MODID \+ ":[a-z0-9_]+"',arg)
   assert call==receiver+'.setTextureName('+arg+')'
   row={**row,'texture':'hbm'+re.fullmatch(r'RefStrings.MODID \+ "([^"]+)"',arg)[1]}
   if row['receiver_kind']!='DECLARED':
    assert row['receiver_kind']=='ERROR' and '.setUnlocalizedName(' in receiver
    manifest['excluded'].append(row);continue
   assert row['receiver_type'] in ('net.minecraft.item.Item','com.hbm.items.armor.ArmorFSB')
   if path==FILES[0]:
    before=old[:start];field=re.search(r'(\w+)\s*=\s*$',before)[1];cls=re.match(r'new (\w+)\(',receiver)[1]
    assert old[end]==';' and '.setTranslationKey(' in receiver
    row.update(field=field,owner=cls)
    if cls in ('ItemDrop','ArmorNo9'):
     assert (cls,field) in (('ItemDrop','beta'),('ArmorNo9','no9'));row['reason']='Non-damageable default-route owner: existing native provider fails original nonzero-metadata icon parity; separate provider correction required.';manifest['excluded'].append(row);continue
   else:
    assert receiver=='this' and arg=='RefStrings.MODID + ":armor"';row.update(field=None,owner='ArmorFSB')
   new='NativeItemTextureRegistry.texture('+receiver+', '+arg+')'
   edits.append({'start':start,'end':end,'before':call,'after':new});manifest['selected'].append(row)
  at=old.index('\n',old.index('package '))+1;edits.append({'start':at,'end':at,'before':'','after':'\n'+IMPORT})
  edits.sort(key=lambda e:e['start']);assert all(x['end']<=y['start'] for x,y in zip(edits,edits[1:]))
  new=old
  for edit in reversed(edits):new=new[:edit['start']]+edit['after']+new[edit['end']:]
  delta=0
  for edit in edits:edit['after_start']=edit['start']+delta;delta+=len(edit['after'])-len(edit['before'])
  restored=new
  for edit in reversed(edits):
   pos=edit['after_start'];assert restored[pos:pos+len(edit['after'])]==edit['after'];restored=restored[:pos]+edit['before']+restored[pos+len(edit['after']):]
  assert restored==old
  result[path]=new;manifest['files'][path]={'before_sha256':digest(old),'after_sha256':digest(new),'edits':edits}
 assert len(manifest['selected'])==106 and len(manifest['excluded'])==23
 return result,manifest

def verify():
 result,manifest=transform()
 assert json.loads((HERE/'source-manifest.json').read_text())==manifest
 for path,text in result.items():assert (ROOT/path).read_bytes()==text.encode(),('Production drift',path)
 return manifest
if __name__=='__main__':
 p=argparse.ArgumentParser();p.add_argument('--apply',action='store_true');a=p.parse_args();result,manifest=transform()
 if a.apply:
  for path,text in result.items():(ROOT/path).write_bytes(text.encode())
  (HERE/'source-manifest.json').write_text(json.dumps(manifest,indent=2)+'\n')
 verify();print('PASS 106 compiler-attributed full receivers, exact original texture expressions, two byte-exact source inverses; 21 poisoned naming chains and non-damageable beta/no9 unchanged.')
