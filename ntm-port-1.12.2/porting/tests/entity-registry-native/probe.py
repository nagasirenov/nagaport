#!/usr/bin/env python3
"""Portable native proof of complete production registration and falling owners. No fixtures replace production owners."""
from pathlib import Path
import os, subprocess, re, json, hashlib, shutil
import argparse, tempfile
parser=argparse.ArgumentParser(description=__doc__)
parser.add_argument('--module',type=Path,required=True)
parser.add_argument('--original',type=Path,required=True)
parser.add_argument('--java-home',type=Path,required=True)
parser.add_argument('--classpath',required=True)
parser.add_argument('--output',type=Path,help='Proof output directory; defaults to a new temporary directory')
args=parser.parse_args()
root=Path(__file__).resolve().parent
main=args.module.resolve();original=args.original.resolve();java=args.java_home.resolve()/'bin';cp=args.classpath
output=args.output.resolve() if args.output else Path(tempfile.mkdtemp(prefix='entity-registry-native-'))
output.mkdir(parents=True,exist_ok=True)
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
for rel,want in json.loads((root/'original-sha256.json').read_text()).items():assert sha(original/rel)==want,rel
pattern=r'\b(addEntity|addMob)\((\w+)\.class,\s*"([^"]+)",\s*([^;]+)\);'
def mappings(p):
 a=re.findall(pattern,p.read_text());return [v for v in a if v[0]=='addEntity']+[v for v in a if v[0]=='addMob']
rel='src/main/java/com/hbm/entity/EntityMappings.java'
a=mappings(main/rel);assert a==mappings(original/rel),'Mapping order/options changed'
entries=[]
for i,(kind,cls,name,opts) in enumerate(a):
 files=list((main/'src/main/java/com/hbm/entity').rglob(cls+'.java'))
 if cls=='EntityMinecartDestroyer':
  assert len(files)==1 and 'package com.hbm.entity.cart;' in files[0].read_text()
 if cls=='EntityCloudSolinium':
  assert len(files)==1 and 'package com.hbm.entity.effect;' in files[0].read_text()
 key='hbm:'+('entity_ntm_cart_destroyer' if cls=='EntityMinecartDestroyer' else 'entity_cloud_solinium' if cls=='EntityCloudSolinium' else name)
 entries.append(dict(local_id=i,entity_class=cls,legacy_name=name,native_key=key,translation_name='hbm.'+name,kind=kind,options=opts))
assert len(entries)==212 and len(set(e['native_key'] for e in entries))==212
assert len(set(e['entity_class'] for e in entries))==212
assert entries[158]['entity_class']=='EntityFallingBlockNT'
assert entries[142]['entity_class']=='EntityMinecartCrate' and entries[143]['entity_class']=='EntityMinecartDestroyer'
assert entries==json.loads((root/'mapping-manifest.json').read_text()),'Pinned mapping manifest changed'
owner=(main/'src/main/java/com/hbm/entity/ModEntityList.java').read_text()
assert 'com.hbm.entity.cart.EntityMinecartDestroyer' in owner
assert 'com.hbm.entity.effect.EntityCloudSolinium' in owner
assert 'RefStrings.MODID + "." + entityName' in owner
symbols=['eggIdMap','eggIdList','getUniqueEntityEggId','eggIDCounter']
for p in (main/'src').rglob('*.java'):
 if p.name=='ModEntityList.java':continue
 assert not any(re.search(r'\b'+s+r'\b',p.read_text()) for s in symbols),str(p)
print('PASS full 212-entry original/native mapping manifest: local IDs/order/options preserved, unique classes/keys, legacy translation namespace; removed integer egg APIs have zero external callers',flush=True)
classes=output/'classes'
if classes.exists():shutil.rmtree(classes)
classes.mkdir()
production=[main/'src/main/java/com/hbm/entity/ModEntityList.java',main/'src/main/java/com/hbm/lib/RefStrings.java']
# Exact native falling compile closure, all complete production sources.
production += [main/'src/main/java/com/hbm/blocks/BlockFallingNT.java',
               main/'src/main/java/com/hbm/entity/item/EntityFallingBlockNT.java',
               main/'src/main/java/com/hbm/blocks/ISpotlight.java']
classpath=cp
sources=production+[root/'NativeEntityRegistryTest.java',main/'porting/tests/ForgeEventNativeLauncher.java']
subprocess.run([str(java/'javac'),'-source','8','-target','8','-encoding','UTF-8','-sourcepath',str(output/'empty-sourcepath'),'-cp',classpath,'-d',str(classes)]+list(map(str,sources)),check=True)
manifest={str(p):sha(p) for p in sources}
(output/'evidence-input-sha256.json').write_text(json.dumps(manifest,indent=2)+'\n')
subprocess.run([str(java/'java'),'-cp',str(classes)+os.pathsep+classpath,'ForgeEventNativeLauncher','NativeEntityRegistryTest'],check=True,cwd=output)

print("Proof output: "+str(output),flush=True)
