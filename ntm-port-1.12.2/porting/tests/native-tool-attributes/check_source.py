#!/usr/bin/env python3
"""Strict complete CP91 inverse and typed native attribute owner/dispatch closure."""
from pathlib import Path
import hashlib,json,re,base64
import migrate
import importlib.util
_spec=importlib.util.spec_from_file_location("world_air_tool_composition",Path(__file__).parent.parent/"native-world-air-query/tool_composition.py")
_world_air=importlib.util.module_from_spec(_spec);_spec.loader.exec_module(_world_air)
H=Path(__file__).resolve().parent;R=H.parents[2];D=json.loads((H/'manifest.json').read_text())
def sha(data):return hashlib.sha256(data).hexdigest()
def restore_base(rel,data):
 data=_world_air.normalize(R,rel,data)
 row=D['files'][rel];assert sha(data)==row['after_sha256'],('unreviewed source bytes',rel)
 old=migrate.transform(rel,data.decode(),True,row['rules']).encode();assert sha(old)==row['before_sha256'],('full inverse mismatch',rel);return old
def census(files):
 rows=[]
 for rel,b in sorted(files.items()):
  for m in re.finditer(r'\b(getItemAttributeModifiers|getAttributeModifiers|getModifiers)\s*\([^;{}]*\)',b.decode()):rows.append([rel,' '.join(m.group().split())])
 return rows
def main():
 controls=0
 for rel,row in D['files'].items():
  data=(R/rel).read_bytes();restore_base(rel,data)
  for changed in (data+b'\n// drift\n',data[:-1],data.replace(row['rules'][0][1].encode(),row['rules'][0][0].encode(),1)):
   try:restore_base(rel,changed)
   except AssertionError:controls+=1
   else:raise AssertionError('accepted source drift '+rel)
 for rel,digest in D['protected'].items():assert sha((R/rel).read_bytes())==digest,('ancestor/descendant source drift',rel)
 files={str(p.relative_to(R)):p.read_bytes() for p in (R/'src/main/java').rglob('*.java')}
 assert not any(b'field_111210_e' in b for b in files.values()),'legacy damage UUID remains'
 for rel in D['files']:files[rel]=restore_base(rel,files[rel])
 assert census(files)==json.loads((H/'baseline-census.json').read_text()),'global attribute declaration/caller drift'
 rows=[x.split('\t') for x in (H/'after.tsv').read_text().splitlines()]
 for name in migrate.OWNERS:
  found=[r for r in rows if r[0]=='declaration' and r[1]==migrate.path(name) and r[7].startswith(('getItemAttributeModifiers(','getAttributeModifiers('))];assert len(found)==1,name
  assert found[0][8]==found[0][7] and 'EntityEquipmentSlot' in found[0][7],('not native override',name)
 fields=[r for r in rows if r[0]=='damage-uuid'];assert len(fields)==15 and all(r[5:8]==['FIELD','net.minecraft.item.Item','ATTACK_DAMAGE_MODIFIER'] for r in fields)
 ctors=[r for r in rows if r[0]=='call' and r[5]=='CONSTRUCTOR'];assert len(ctors)==1 and ctors[0][6]=='net.minecraft.item.ItemTool' and ctors[0][7]=='ItemTool(net.minecraft.item.Item.ToolMaterial,java.util.Set<net.minecraft.block.Block>)'
 raw=[r for r in rows if r[0]=='call' and r[1]==migrate.path('WeaponSpecial') and r[7]=='getAttackDamage()'];assert len(raw)==1 and raw[0][6]=='net.minecraft.item.ItemSword'
 held=[r for r in rows if r[0]=='call' and r[1]==migrate.EVENT and r[7].startswith('getAttributeModifiers(')];assert len(held)==1 and held[0][6]=='net.minecraft.item.ItemStack' and held[0][7]=='getAttributeModifiers(net.minecraft.inventory.EntityEquipmentSlot)'
 print('PASS strict tool source guard: 9 complete-file CP91 inverses;',controls,'drift controls; 7 ancestry pins; 8 native overrides, 15 native UUID references, native ItemTool constructor, Sword raw-material getter and ItemStack held caller')
if __name__=='__main__':main()
