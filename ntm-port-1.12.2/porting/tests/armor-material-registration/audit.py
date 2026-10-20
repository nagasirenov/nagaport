#!/usr/bin/env python3
"""Audit original material consumers and native adaptation ownership, without whole-mod compilation."""
import hashlib,json,re,subprocess,zipfile
from pathlib import Path
import migrate
HERE=Path(__file__).resolve().parent;ROOT=migrate.ROOT
m=migrate.verify();materials={r['variable'] for f in m['files'] for r in f['creations']}
def git(path):return subprocess.check_output(['git','-C',str(ROOT),'show',migrate.BASE+':'+path])
def pristine(path):return git('ntm-original-pristine/'+path)
def body(text,start):
 a=text.index('{',start);depth=1;i=a+1
 while depth:
  if text[i]=='{':depth+=1
  if text[i]=='}':depth-=1
  i+=1
 return text[a:i]
classes={};consumers=[]
def owner(name):
 if name in classes:return classes[name]
 rel='src/main/java/com/hbm/items/armor/'+name+'.java';data=migrate.original(rel);old=pristine(rel);text=data.decode();orig=old.decode()
 parent=re.search(r'public class '+name+r' extends (\w+)',text)[1]
 original_parent=re.search(r'public class '+name+r' extends (\w+)',orig)[1];assert parent==original_parent
 signature=re.search(r'public '+name+r'\(ArmorMaterial [^)]*\)',text);assert signature,name
 constructor=body(text,signature.start());orig_sig=re.search(r'public '+name+r'\(ArmorMaterial [^)]*\)',orig);assert orig_sig
 old_ctor=body(orig,orig_sig.start());super_call=re.search(r'\bsuper\([^;]+;',constructor).group();assert super_call in old_ctor,(name,super_call)
 texture=re.search(r'public String getArmorTexture\([^)]*\)',text)
 texture_body=body(text,texture.start()) if texture else None
 old_texture=re.search(r'public String getArmorTexture\([^)]*\)',orig)
 if texture:assert old_texture and texture_body==body(orig,old_texture.start()),name
 row=dict(parent=parent,source=rel,baseline_sha256=migrate.sha(data),pristine_sha256=migrate.sha(old),constructor_signature=signature.group(),super_call=super_call,texture_body=texture_body,texture_owner=name if texture else None)
 classes[name]=row
 if parent=='ItemArmor':assert re.fullmatch(r'super\(\w+, 0, \w+\);',super_call),name
 else:
  p=owner(parent)
  if not row['texture_owner']:row['texture_owner']=p['texture_owner']
 assert row['texture_owner'],name
 return row
for f in m['files']:
 old=pristine(f['path'])
 for row in f['creations']+f['repairs']:assert old.count(row['before'].encode())==1,('Original declaration/repair mismatch',row)
 text=migrate.original(f['path']).decode()
 for match in re.finditer(r'new (\w+)\(((?:MainRegistry\.)?aMat\w+), ([0-3])([,)])',text):
  name,mat,slot=match[1],match[2],match[3];assert mat.split('.')[-1] in materials
  row=owner(name);line=text[:match.start()].count('\n')+1
  consumers.append(dict(source=f['path'],line=line,owner=name,material=mat,legacy_slot=int(slot),constructor_prefix=match.group(),texture_owner=row['texture_owner']))
# Verify no custom-material construction has escaped the three-owner consumer inventory.
all_sources=subprocess.check_output(['git','-C',str(ROOT.parent),'grep','-l','-E','aMat[A-Z]',migrate.BASE,'--','ntm-port-1.12.2/src/main/java']).decode().splitlines()
assert sorted(s.split(':',1)[1].removeprefix('ntm-port-1.12.2/') for s in all_sources)==sorted(migrate.FILES),all_sources
main=migrate.original(migrate.FILES[0]);items=migrate.original(migrate.FILES[2]);assert main.index(b'ModItems.mainRegistry();')<main.index(b'aMatSchrab.customCraftingMaterial');assert items.index(b'ModItemsArmor.init();')<items.index(b'ArmorMaterial aMatLiquidator')
used={x['material'].split('.')[-1] for x in consumers};unused=sorted(materials-used);assert unused==['aMatAus3'],unused
# Forge1614's actual supplied source patch establishes the original custom Item reference rule.
forge=Path('/tmp/nagaport-forge1614-userdev.jar')
with zipfile.ZipFile(forge) as z:
 import io
 with zipfile.ZipFile(io.BytesIO(z.read('forgepatches.zip'))) as patches:
  patch=patches.read('net/minecraft/item/ItemArmor.java.patch')
 assert b'public Item customCraftingMaterial = null;' in patch and b'customCraftingMaterial))));' in patch
 (HERE/'original-forge-itemarmor.patch').write_bytes(patch)
 mapping=z.read('conf/packaged.srg').decode();selected=[s for s in mapping.splitlines() if (s.startswith('CL: ') and s.endswith(('net/minecraft/item/ItemArmor','net/minecraft/item/ItemArmor$ArmorMaterial','net/minecraft/client/renderer/entity/RenderBiped','net/minecraft/init/Bootstrap'))) or (s.startswith(('MD: abd/','MD: abb/','MD: bnw/','MD: adb/o ')))]
 (HERE/'original-engine-mapping.txt').write_text('\n'.join(selected)+'\n')
report=dict(baseline=migrate.BASE,consumer_count=len(consumers),consumers=consumers,classes=classes,unused_materials=unused,forge1614_userdev_sha256=migrate.sha(forge.read_bytes()),forge1614_patch_sha256=migrate.sha(patch),engine_parameters={'texture_name':'leather: exact old renderIndex 0 fallback, all original custom texture callbacks remain unchanged','sound':'ITEM_ARMOR_EQUIP_GENERIC: ordinary new native equip feedback, not old step/jump/fall fields','toughness':'0.0F: no added toughness mechanic; no old equivalent'},boundaries=['Native armor/combat formula differs from 1.7.10; this migration does not recreate old global combat mechanics.','Native positive ItemStack metadata above 65535 is empty; negative constructor metadata is clamped to zero. Zero/negative counts and cached-empty states differ from the original item-only predicate. See repair-engine-boundaries.tsv; no broad parity or acceptance is claimed.'])
(HERE/'consumer-audit.json').write_text(json.dumps(report,indent=2)+'\n')
# Strong drift rejection follows from comparing each complete file to independently generated exact bytes.
for f in m['files']:
 data=(ROOT/f['path']).read_bytes();expected=migrate.transform(f['path'])[0]
 for changed in (data+b' ',data[:-1],data.replace(b'"leather"',b'"iron"',1),data.replace(b'OreDictionary.WILDCARD_VALUE',b'0',1)):
  try:migrate.restore_base(f['path'],changed)
  except AssertionError:pass
  else:raise AssertionError('Mutation was not rejected')
print('PASS original/pristine closure: %d custom-material constructor consumers, %d concrete/ancestor owners, original texture callback bodies and renderIndex 0 preserved; Aus3 remains intentionally unused; exact creation/repair source and 12 mutation controls.'%(len(consumers),len(classes)))
