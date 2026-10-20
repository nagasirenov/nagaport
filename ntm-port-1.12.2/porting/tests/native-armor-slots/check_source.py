#!/usr/bin/env python3
"""Exact full-file inverse and complete native slot ownership/override closure."""
from pathlib import Path
import base64,hashlib,json,subprocess,collections,re
H=Path(__file__).resolve().parent;R=H.parents[2];D=json.loads((H/'source-manifest.json').read_text())
def sha(x):return hashlib.sha256(x).hexdigest()
def inverse(f,text,strict=True):
 row=D['files'][f]
 if strict:assert sha(text.encode())==row['after_sha256'],f+' changed beyond reviewed slots'
 # Offsets are exact baseline compiler offsets; derive post-edit positions, never global replace.
 delta=0; edits=[]
 for e in row['edits']:
  at=e['start']+delta; assert text[at:at+len(e['new'])]==e['new'],(f,at);edits.append((at,e));delta+=len(e['new'])-len(e['old'])
 for at,e in reversed(edits):text=text[:at]+e['old']+text[at+len(e['new']):]
 assert sha(text.encode())==row['before_sha256'],f+' inverse mismatch'
 return text

def rows(name):
 out=[]
 for line in (H/(name+'.tsv')).read_text().splitlines():
  a=line.split('\t');out.append(dict(kind=a[0],file=a[1],line=int(a[2]),start=int(a[3]),end=int(a[4]),ek=a[5],owner=a[6],element=a[7],type=a[8],override=a[9],text=base64.b64decode(a[10]).decode()))
 return out

def typed():
 before=rows('before');after=rows('after');oldhooks=[r for r in before if r['kind']=='hook-declaration'];newhooks=[r for r in after if r['kind']=='hook-declaration']
 assert len(oldhooks)==len(newhooks)==39
 assert all(not r['override'] for r in oldhooks)
 assert all(r['override'] and 'EntityEquipmentSlot' in r['override'] for r in newhooks)
 assert collections.Counter(r['element'].split('(')[0] for r in newhooks)=={'getArmorModel':24,'getArmorTexture':13,'isValidArmor':2}
 for f,d in D['files'].items():
  delta=0
  for e in d['edits']:
   start=e['start']+delta;end=start+len(e['new']);delta+=len(e['new'])-len(e['old'])
   if e.get('kind')=='hook-declaration':
    assert len([r for r in newhooks if (r['file'],r['start'],r['end'],r['text'])==(f,start,end,e['new'])])==1
   if e.get('kind')=='call' and e['old'].startswith('super('):
    found=[r for r in after if (r['kind'],r['file'],r['start'],r['end'],r['text'])==('call',f,start,end,e['new'])];assert len(found)==1
    assert found[0]['ek']=='CONSTRUCTOR' and found[0]['owner']=='net.minecraft.item.ItemArmor'
    assert found[0]['element']=='ItemArmor(net.minecraft.item.ItemArmor.ArmorMaterial,int,net.minecraft.inventory.EntityEquipmentSlot)'
 fields=[r for r in after if r['kind']=='armorType' and r['ek']=='FIELD'];assert len(fields)==52
 assert all(r['owner']=='net.minecraft.item.ItemArmor' and r['type']=='net.minecraft.inventory.EntityEquipmentSlot' for r in fields)
 mapping=[r for r in after if r['kind']=='call' and r['owner']=='com.hbm.util.ArmorSlotOrder'];assert len(mapping)==75
 assert all(r['ek']=='METHOD' and r['element'] in ('fromIndex(int)','index(net.minecraft.inventory.EntityEquipmentSlot)') for r in mapping)
 constructions=[r for r in before if r['kind']=='construction'];native_constructions=[r for r in after if r['kind']=='construction']
 assert len(constructions)==len(native_constructions)==152
 key=lambda r:(r['file'],r['owner'],r['element'],r['text'])
 assert collections.Counter(map(key,constructions))==collections.Counter(map(key,native_constructions))
 assert all(r['ek']=='CONSTRUCTOR' for r in constructions+native_constructions)
 domain=[]
 for r in constructions:
  if r['owner']=='com.hbm.items.armor.ArmorGasMask':assert r['text']=='new ArmorGasMask()';slot=0
  else:
   m=re.match(r'new \w+\([^,]+, ([0-3])(?:,|\))',r['text']);assert m,r;slot=int(m[1])
  domain.append({'file':r['file'],'line':r['line'],'owner':r['owner'],'legacy_slot':slot,'expression':r['text']})
 oldchains=[r for r in before if r['kind']=='constructor-chain'];newchains=[r for r in after if r['kind']=='constructor-chain'];assert len(oldchains)==len(newchains)==24
 assert collections.Counter(map(key,oldchains))==collections.Counter(map(key,newchains))
 assert all(re.match(r'super\([^,]+, (slot|armorType)(,|\))',r['text']) for r in oldchains)
 assert len([r for r in before if r['kind']=='constructor-declaration'])==32
 (H/'constructor-domain.json').write_text(json.dumps({'construction_count':152,'forwarding_count':24,'root_count':8,'owned_constructor_count':32,'all_arguments_unchanged':True,'constructions':domain,'forwarding':oldchains},indent=2)+'\n')
 print('PASS constructor domain: all 152 creations use 0..3 (four fixed-head gas masks), 24 unchanged forwarding constructors, 8 native roots')
 print('PASS typed closure: 8 native ItemArmor constructors, 39 native overrides (24 model/13 texture/2 valid), 52 native fields, 75 mapped index calls')

def main(source_root=R):
 import material_composition,attribute_composition
 source_root=Path(source_root)
 controls=0
 for f,row in D['files'].items():
  current=(source_root/f).read_bytes();s=attribute_composition.restore(f,current,row['after_sha256'],source_root).decode();old=inverse(f,s)
  assert old.encode()==subprocess.check_output(['git','-C',str(source_root.parent),'show',D['baseline']+':ntm-port-1.12.2/'+f])
  for mutant in (s+'\n// unrelated\n',s[:-1],s.replace(row['edits'][0]['new'],row['edits'][0]['old'],1)):
   try:inverse(f,mutant)
   except AssertionError:controls+=1
   else:raise AssertionError('Accepted unrelated source mutation '+f)
 helper=D['helper'];assert sha((source_root/helper['file']).read_bytes())==helper['sha256']
 for f,h in D['protected'].items():
  current=attribute_composition.restore_owned(f,(source_root/f).read_bytes(),h,source_root)
  checked=material_composition.restore(f,current,h,source_root)
  assert sha(checked)==h,'Protected source changed '+f
 typed()
 print('PASS',len(D['files']),'complete-file CP89 inverses;',controls,'drift controls;',len(D['protected']),'protected constructor registrations/model/domain files')
if __name__=='__main__':
 import argparse
 p=argparse.ArgumentParser();p.add_argument('--source-root',type=Path,default=R);a=p.parse_args();main(a.source_root)
