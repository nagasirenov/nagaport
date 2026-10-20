#!/usr/bin/env python3
"""Strict complete-file CP91 inverse and independent native typed inventory closure."""
from pathlib import Path
import hashlib,json,re,base64,subprocess
H=Path(__file__).resolve().parent;R=H.parents[2];D=json.loads((H/'manifest.json').read_text())
OWNERS={'com.hbm.items.ItemInventory','com.hbm.items.tool.ItemLeadBox.InventoryLeadBox','com.hbm.items.tool.ItemToolBox.InventoryToolBox','com.hbm.items.tool.ItemRebarPlacer.InventoryRebar','com.hbm.items.block.ItemBlockStorageCrate.InventoryCrate'}
def sha(data):return hashlib.sha256(data).hexdigest()
def restore_base(rel,data):
 row=D['files'][rel];assert sha(data)==row['after_sha256'],('unreviewed source bytes',rel)
 text=data.decode()
 for old,new,n in reversed(row['rules']):
  assert text.count(new)==n,('inverse multiplicity',rel,new,n,text.count(new));text=text.replace(new,old)
 before=text.encode();assert sha(before)==row['before_sha256'],('complete-file inverse mismatch',rel);return before

def lineage(files):
 return sorted([rel,m.group(0)] for rel,b in files.items() for m in re.finditer(r'class\s+\w+\s+extends\s+(?:[\w$]+\.)*(?:ItemInventory|InventoryLeadBox|InventoryToolBox|InventoryRebar|InventoryCrate)\b',b.decode()))
def main():
 controls=0
 for rel,row in D['files'].items():
  data=(R/rel).read_bytes();restore_base(rel,data)
  for changed in [data+b'\n// drift\n',data[:-1],data.replace(row['rules'][0][1].encode(),row['rules'][0][0].encode(),1)]:
   try:restore_base(rel,changed)
   except AssertionError:controls+=1
   else:raise AssertionError(('accepted source drift',rel))
 for rel,digest in D['protected'].items():assert sha((R/rel).read_bytes())==digest,('protected source drift',rel)
 files={str(p.relative_to(R)):p.read_bytes() for p in (R/'src/main/java').rglob('*.java')}
 assert lineage(files)==json.loads((H/'baseline-lineage.json').read_text()),'ItemInventory descendant closure drift'
 rows=[x.split('\t') for x in (H/'after.tsv').read_text().splitlines()]
 declarations=[r for r in rows if r[0]=='declaration' and r[6] in OWNERS]
 helpers={'checkNBT','getStackInSlotNullable','quantity','writeStacksToNBT','readStacksFromNBT'}
 hooks=[r for r in declarations if r[7].split('(')[0] not in helpers]
 assert len(hooks)==37,len(hooks)
 assert all(r[7]==r[8] for r in hooks),'not native IInventory/WorldNameable override'
 classes=[r for r in rows if r[0]=='inventory-class' and r[7] in OWNERS];assert len(classes)==5,len(classes)
 for r in classes:
  source=(R/r[1]).read_bytes().decode();assert source[int(r[3]):int(r[4])]==base64.b64decode(r[9]).decode(),('stale typed attribution bytes',r[7])
 calls=[r for r in rows if r[0]=='call' and r[6] in OWNERS]
 current=[[r[0],r[1],*r[5:9],base64.b64decode(r[9]).decode()] for r in calls]
 assert current==json.loads((H/'owned-call-closure.json').read_text()),'typed call/virtual-owner closure drift'
 for r in calls:assert r[7].split('(')[0] not in {'getInventoryName','hasCustomInventoryName','isUseableByPlayer'},r
 delta=json.loads((H/'attribution-delta.json').read_text());assert len(delta['removed'])==21 and not delta['added']
 print('PASS 9 complete-file CP91 inverses;',controls,'drift controls;',len(D['protected']),'unchanged dependency/sibling pins; 4 exact descendants, 37 native hooks,',len(calls),'typed owned calls; 21 diagnostics removed, none added')
if __name__=='__main__':main()
