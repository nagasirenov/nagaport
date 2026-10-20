#!/usr/bin/env python3
"""Finite CP90 armor attribute transition; strict counts and complete-file inverse."""
from pathlib import Path
import hashlib,json,subprocess
H=Path(__file__).resolve().parent; R=H.parents[2]
BASE='0be133c9fb9f07053e26f081371487e641c95214'
PREFIX='src/main/java/com/hbm/items/armor/'
OVERRIDES=['ArmorDiesel','ArmorDesh','ArmorLiquidator']
MANUAL=['ArmorNCRPA','ArmorEnvsuit','ArmorDNT','ItemModWD40','ItemModIron','ItemModServos','ItemModInsert','ItemModV1','ItemModHealth']
def rules(name):
 if name in OVERRIDES:
  return [('public Multimap getItemAttributeModifiers()', 'public Multimap getItemAttributeModifiers(net.minecraft.inventory.EntityEquipmentSlot slot)'), ('Multimap multimap = HashMultimap.create();', 'Multimap multimap = super.getItemAttributeModifiers(slot);\n\t\tif(slot == net.minecraft.inventory.EntityEquipmentSlot.OFFHAND) return multimap;')]
 arg='stack' if name.startswith('Armor') else 'armor'
 return [(f'super.getAttributeModifiers({arg})',f'super.getAttributeModifiers(net.minecraft.inventory.EntityEquipmentSlot.MAINHAND, {arg})')]
def transform(text,name,inverse=False):
 for a,b in rules(name):
  if inverse:a,b=b,a
  assert text.count(a)==1,(name,a,text.count(a))
  text=text.replace(a,b)
 return text
if __name__=='__main__':
 import argparse
 p=argparse.ArgumentParser();p.add_argument('--apply',action='store_true');args=p.parse_args()
 manifest={'baseline':BASE,'files':{},'protected':{}}
 for name in OVERRIDES+MANUAL:
  rel=PREFIX+name+'.java'; old=subprocess.check_output(['git','-C',str(R.parent),'show',BASE+':ntm-port-1.12.2/'+rel]);new=transform(old.decode(),name).encode()
  manifest['files'][rel]={'before_sha256':hashlib.sha256(old).hexdigest(),'after_sha256':hashlib.sha256(new).hexdigest(),'rules':rules(name)}
  if args.apply:
   assert (R/rel).read_bytes()==old,rel
   (R/rel).write_bytes(new)
 for name in ['ArmorFSB','ArmorFSBPowered','ArmorFSBFueled','ItemArmorMod','ArmorLiquidatorMask']:
  rel=PREFIX+name+'.java';manifest['protected'][rel]=hashlib.sha256((R/rel).read_bytes()).hexdigest()
 for rel in ['src/main/java/com/hbm/handler/ArmorModHandler.java','src/main/java/com/hbm/util/ArmorSlotOrder.java']:
  manifest['protected'][rel]=hashlib.sha256((R/rel).read_bytes()).hexdigest()
 (H/'manifest.json').write_text(json.dumps(manifest,indent=2)+'\n')
