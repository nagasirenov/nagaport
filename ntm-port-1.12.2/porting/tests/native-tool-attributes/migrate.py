#!/usr/bin/env python3
"""Finite native tool/weapon hooks; strict exact full-file inverse, no stat compensation."""
from pathlib import Path
import hashlib,json,subprocess
H=Path(__file__).resolve().parent;R=H.parents[2]
BASE='f5ce86cd607379cc9810896cf91d1fee86ce6331'
OWNERS={'ItemMirrorTool':'tool','ItemRBMKTool':'tool','ItemToolingWeapon':'tool','ItemSwordAbility':'tool','ItemToolAbility':'tool','GunB92':'weapon','ItemCrucible':'weapon','WeaponSpecial':'tool'}
def path(name):return 'src/main/java/com/hbm/items/'+OWNERS[name]+'/'+name+'.java'
EVENT='src/main/java/com/hbm/main/ModEventHandler.java'
def rules(rel,text):
 name=Path(rel).stem;nl='\r\n' if '\r\n' in text else '\n'
 if rel==EVENT:return [('com.hbm.util.NativeHeldItem.getMainHandNullable(player).getAttributeModifiers()', 'com.hbm.util.NativeHeldItem.getMainHandNullable(player).getAttributeModifiers(net.minecraft.inventory.EntityEquipmentSlot.MAINHAND)',1)]
 stack=name in ('ItemMirrorTool','ItemRBMKTool','ItemToolingWeapon','ItemCrucible')
 result=[('public Multimap getAttributeModifiers(ItemStack stack)' if stack else 'public Multimap getItemAttributeModifiers()', 'public Multimap getAttributeModifiers(net.minecraft.inventory.EntityEquipmentSlot slot, ItemStack stack)' if stack else 'public Multimap getItemAttributeModifiers(net.minecraft.inventory.EntityEquipmentSlot slot)',1)]
 if name in ('ItemMirrorTool','ItemRBMKTool','ItemToolingWeapon'):
  old='Multimap multimap = super.getAttributeModifiers(stack);';new='Multimap multimap = super.getAttributeModifiers(slot, stack);'
 elif name in ('GunB92','WeaponSpecial'):old='Multimap multimap = super.getItemAttributeModifiers();';new='Multimap multimap = super.getItemAttributeModifiers(slot);'
 else:old=new='Multimap multimap = HashMultimap.create();'
 if name=='WeaponSpecial':
  new+=nl+'\t\tif(slot == net.minecraft.inventory.EntityEquipmentSlot.MAINHAND) {'+nl+'\t\t\tAttributeModifier damage = new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", (double) (4.0F + super.getAttackDamage()), 0);'+nl+'\t\t\tmultimap.remove(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), damage);'+nl+'\t\t\tmultimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), damage);'+nl+'\t\t}'
 result.append((old,new+nl+'\t\tif(slot == net.minecraft.inventory.EntityEquipmentSlot.OFFHAND) return multimap;',1))
 count=text.count('field_111210_e');assert count in (1,2,3)
 result.append(('field_111210_e','ATTACK_DAMAGE_MODIFIER',count))
 if name=='ItemCrucible':result.append(('\tpublic Multimap getAttributeModifiers(', '\t@Override'+nl+'\tpublic Multimap getAttributeModifiers(',1))
 if name=='ItemToolAbility':result.append(('super(0, material, type.blocks);','super(material, type.blocks);',1))
 return result
def transform(rel,text,inverse=False,edits=None):
 edits=rules(rel,text) if edits is None else edits
 for a,b,n in edits:
  if inverse:a,b=b,a
  assert text.count(a)==n,(rel,a,text.count(a),n)
  text=text.replace(a,b)
 return text
if __name__=='__main__':
 import argparse
 p=argparse.ArgumentParser();p.add_argument('--apply',action='store_true');a=p.parse_args()
 d={'baseline':BASE,'files':{},'protected':{}}
 for rel in [path(n) for n in OWNERS]+[EVENT]:
  old=subprocess.check_output(['git','-C',str(R.parent),'show',BASE+':ntm-port-1.12.2/'+rel]);edits=rules(rel,old.decode());new=transform(rel,old.decode(),edits=edits).encode();d['files'][rel]={'before_sha256':hashlib.sha256(old).hexdigest(),'after_sha256':hashlib.sha256(new).hexdigest(),'rules':edits}
  if a.apply:
   assert (R/rel).read_bytes()==old,rel;(R/rel).write_bytes(new)
 for name in ['ItemTooling','ItemCraftingDegradation','ItemSwordAbilityPower','ItemSwordMeteorite','ItemToolAbilityFueled','ItemToolAbilityPower','ItemChainsaw']:
  rel='src/main/java/com/hbm/items/tool/'+name+'.java';d['protected'][rel]=hashlib.sha256((R/rel).read_bytes()).hexdigest()
 (H/'manifest.json').write_text(json.dumps(d,indent=2)+'\n')
