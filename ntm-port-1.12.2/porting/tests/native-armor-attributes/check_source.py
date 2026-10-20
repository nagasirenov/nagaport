#!/usr/bin/env python3
"""Strict complete-file inverse, immutable dependency pins and global API census."""
from pathlib import Path
import hashlib,json,re,subprocess,collections
import migrate
H=Path(__file__).resolve().parent;R=H.parents[2];D=json.loads((H/'manifest.json').read_text())
def sha(b):return hashlib.sha256(b).hexdigest()
def restore_base(rel,data):
 row=D['files'][rel];assert sha(data)==row['after_sha256'],('changed owned file',rel)
 result=migrate.transform(data.decode(),Path(rel).stem,True).encode();assert sha(result)==row['before_sha256'],('full inverse',rel);return result
def census(files):
 rows=[]
 for rel,b in sorted(files.items()):
  s=b.decode();
  for m in re.finditer(r'\b(getItemAttributeModifiers|getAttributeModifiers|getModifiers)\s*\([^;{}]*\)',s):rows.append([rel,' '.join(m.group().split())])
 return rows
def main():
 controls=0
 for rel,row in D['files'].items():
  current=(R/rel).read_bytes();restore_base(rel,current)
  for mutant in (current+b'\n// unrelated drift\n',current[:-1],current.replace(row['rules'][0][1].encode(),row['rules'][0][0].encode(),1)):
   try:restore_base(rel,mutant)
   except AssertionError:controls+=1
   else:raise AssertionError('accepted source drift '+rel)
 for rel,digest in D['protected'].items():assert sha((R/rel).read_bytes())==digest,('dependency drift',rel)
 files={str(p.relative_to(R)):p.read_bytes() for p in (R/'src/main/java').rglob('*.java')}
 for rel in D['files']:files[rel]=restore_base(rel,files[rel])
 import tool_composition
 files=tool_composition.restore_census(files,R)
 assert census(files)==json.loads((H/'baseline-census.json').read_text()),'global attribute API census drift'
 before=[x.split('\t') for x in (H/'before.tsv').read_text().splitlines()];after=[x.split('\t') for x in (H/'after.tsv').read_text().splitlines()]
 for name in migrate.OVERRIDES:
  rel=migrate.PREFIX+name+'.java';rows=[r for r in after if r[0]=='declaration' and r[1]==rel and r[7].startswith('getItemAttributeModifiers(')]
  assert len(rows)==1 and rows[0][8]=='getItemAttributeModifiers(net.minecraft.inventory.EntityEquipmentSlot)',name
 for name in migrate.MANUAL:
  rows=[r for r in after if r[0]=='call' and r[1]==migrate.PREFIX+name+'.java' and r[7].startswith('getAttributeModifiers(')]
  assert len(rows)==1 and rows[0][6]=='net.minecraft.item.Item' and rows[0][7]=='getAttributeModifiers(net.minecraft.inventory.EntityEquipmentSlot,net.minecraft.item.ItemStack)',name
 # All source subclasses of owned families are listed from real compiler symbols.
 classes={r[7]:r[8] for r in after if r[0]=='item-class'}
 for name in migrate.MANUAL:
  owner='com.hbm.items.armor.'+name
  assert not any(parent==owner for parent in classes.values()),('new concrete subclass requires dispatch audit',owner)
 print('PASS',controls,'source drift controls')
 print('PASS strict source guard: 12 complete-file inverses, 7 dependency pins, global API census, 3 native overrides and 9 native bridges')
if __name__=='__main__':main()
