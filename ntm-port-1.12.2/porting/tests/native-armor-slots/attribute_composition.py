#!/usr/bin/env python3
"""Accept only the exact CP91 attribute delta before the original CP90 slot inverse."""
from pathlib import Path
import hashlib,importlib.util,json
BASE='0be133c9fb9f07053e26f081371487e641c95214'
MIGRATE_SHA='26e00d622b61ba7fb44c8475265535289584f27c1421852a12842cd34eef45b4'
MANIFEST_SHA='7f772ea087992489b11c18b4cc32659b00cdf01cb7157dbcb8af1bb4073c0a89'
def sha(data):return hashlib.sha256(data).hexdigest()
def load(root):
 h=Path(root)/'porting/tests/native-armor-attributes';p=h/'migrate.py'
 assert sha(p.read_bytes())==MIGRATE_SHA,'Unreviewed attribute inverse script'
 data=(h/'manifest.json').read_bytes();assert sha(data)==MANIFEST_SHA,'Unreviewed attribute inverse manifest'
 d=json.loads(data);assert d['baseline']==BASE
 spec=importlib.util.spec_from_file_location('slot_attribute_inverse',p);m=importlib.util.module_from_spec(spec);spec.loader.exec_module(m)
 assert m.BASE==BASE and len(d['files'])==12
 return m,d

def restore(relative,current,expected_hash,root):
 if sha(current)==expected_hash:return current
 m,d=load(root);assert relative in d['files'],'Non-attribute owner drift: '+relative
 row=d['files'][relative];assert sha(current)==row['after_sha256'],'Attribute bytes drift: '+relative
 original=m.transform(current.decode(),Path(relative).stem,inverse=True).encode()
 assert sha(original)==row['before_sha256']==expected_hash,'Attribute inverse does not restore exact CP90 pin: '+relative
 return original

def restore_owned(relative,current,expected_hash,root):
 m,d=load(root)
 return restore(relative,current,expected_hash,root) if relative in d['files'] else current

def controls(root,slots):
 m,d=load(root);checks=0;receipts=[]
 for relative,row in d['files'].items():
  if relative not in slots:continue
  current=(Path(root)/relative).read_bytes();before=m.transform(current.decode(),Path(relative).stem,True).encode();expected=slots[relative]['after_sha256']
  assert restore(relative,current,expected,root)==before;checks+=1
  assert restore(relative,before,expected,root)==before;checks+=1
  for changed in (current+b'\n// drift\n',current[:-1],current.replace(row['rules'][0][1].encode(),row['rules'][0][1].encode()+b'/* drift */',1)):
   try:restore(relative,changed,expected,root)
   except AssertionError:checks+=1
   else:raise AssertionError('Accepted attribute drift '+relative)
  receipts.append({'file':relative,'original_slot_pin':expected,'composed_sha256':sha(current),'restored_sha256':sha(before)})
 return {'checks':checks,'pins_replaced':False,'attribute_baseline':BASE,'files':receipts}
if __name__=='__main__':
 import argparse
 here=Path(__file__).resolve().parent;p=argparse.ArgumentParser();p.add_argument('--source-root',type=Path,default=here.parents[2]);a=p.parse_args()
 d=json.loads((here/'source-manifest.json').read_text());receipt=controls(a.source_root,dict({f:{'after_sha256':v} for f,v in d['protected'].items()},**d['files']));(here/'attribute-composition-receipt.json').write_text(json.dumps(receipt,indent=2)+'\n');print('PASS strict attribute composition:',receipt['checks'],'original/composed/drift controls, zero repins')
