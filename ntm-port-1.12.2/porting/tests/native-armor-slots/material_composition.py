#!/usr/bin/env python3
"""Only the frozen CP90 material family's exact inverse may restore protected registry bytes."""
from pathlib import Path
import hashlib,importlib.util,json
HERE=Path(__file__).resolve().parent
FILES=('src/main/java/com/hbm/main/MainRegistry.java','src/main/java/com/hbm/items/ModItemsArmor.java','src/main/java/com/hbm/items/ModItems.java')
BASE='42dea8e6e3241d0c4ce4ba9c5459fab2bdfdc99b'
SCRIPT_SHA256='15274a584ed63348b57c98a7e8e34611829f9d0a43621432c00937437b5b9575'
def sha(data):return hashlib.sha256(data).hexdigest()
def verify_script(data):assert sha(data)==SCRIPT_SHA256,'Unreviewed material inverse implementation'
def migration(root):
 path=Path(root)/'porting/tests/armor-material-registration/migrate.py';verify_script(path.read_bytes())
 spec=importlib.util.spec_from_file_location('armor_slot_material_inverse',path);module=importlib.util.module_from_spec(spec);spec.loader.exec_module(module)
 assert module.BASE==BASE and tuple(module.FILES)==FILES,'Unexpected material inverse scope'
 return module

def restore(relative,current,expected_hash,root):
 if sha(current)==expected_hash:return current
 assert relative in FILES,'Protected non-material source changed: '+relative
 material=migration(root)
 # restore_base independently regenerates the exact approved after file from immutable CP89,
 # checks complete byte identity, and only then returns that immutable original.
 original=material.restore_base(relative,current)
 assert sha(original)==expected_hash,'Material inverse did not restore the original slot-family pin: '+relative
 return original

def controls(root,protected):
 material=migration(root);checks=0;receipts=[]
 for relative in FILES:
  current=(Path(root)/relative).read_bytes();original=material.original(relative);after,row=material.transform(relative)
  assert sha(original)==protected[relative]
  assert restore(relative,original,protected[relative],root)==original;checks+=1
  assert restore(relative,after,protected[relative],root)==original;checks+=1
  assert current in (original,after),'Current material file is neither isolated nor exactly composed'
  for mutated in (after+b'\n// unrelated drift\n',after[:-1],after.replace(b'new int[] { ',b'new int[] { 99, ',1),after.replace(b'OreDictionary.WILDCARD_VALUE',b'0',1)):
   try:restore(relative,mutated,protected[relative],root)
   except AssertionError:checks+=1
   else:raise AssertionError('Accepted material drift: '+relative)
  receipts.append({'file':relative,'original_sha256':sha(original),'approved_after_sha256':sha(after),'current_sha256':sha(current),'restored_sha256':sha(restore(relative,current,protected[relative],root))})
 script=(Path(root)/'porting/tests/armor-material-registration/migrate.py').read_bytes()
 for changed in (script+b'\n# drift\n',script[:-1]):
  try:verify_script(changed)
  except AssertionError:checks+=1
  else:raise AssertionError('Accepted inverse implementation drift')
 other='src/main/java/com/hbm/render/model/ModelArmorBase.java'
 try:restore(other,(Path(root)/other).read_bytes()+b'\n',protected[other],root)
 except AssertionError:checks+=1
 else:raise AssertionError('Material composition accepted unrelated model drift')
 return {'baseline':BASE,'material_inverse_sha256':SCRIPT_SHA256,'checks':checks,'files':receipts,'protected_hashes_replaced':False}
if __name__=='__main__':
 import argparse
 p=argparse.ArgumentParser();p.add_argument('--source-root',type=Path,default=HERE.parents[2]);a=p.parse_args()
 protected=json.loads((HERE/'source-manifest.json').read_text())['protected']
 receipt=controls(a.source_root,protected);(HERE/'material-composition-receipt.json').write_text(json.dumps(receipt,indent=2)+'\n');print('PASS strict material-family composition:',receipt['checks'],'baseline/combined/drift controls; no protected pins changed')
