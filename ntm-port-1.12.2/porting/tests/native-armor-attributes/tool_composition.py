#!/usr/bin/env python3
"""Strictly invert only the frozen CP92 tool family before the CP91 API census."""
from pathlib import Path
import hashlib,importlib.util,json
BASE='f5ce86cd607379cc9810896cf91d1fee86ce6331'
MIGRATE_SHA='8aae00963332b6301b3e3da02dff4aaac140cd0126cc009914fb92178aaec225'
MANIFEST_SHA='e3926fb4fede22ba02c5b0b713c5cb2255b5b54d197ed1c21f2932629df0c422'
def sha(b):return hashlib.sha256(b).hexdigest()
def load(root):
 h=Path(root)/'porting/tests/native-tool-attributes';script=(h/'migrate.py').read_bytes();manifest=(h/'manifest.json').read_bytes()
 assert sha(script)==MIGRATE_SHA,'Unreviewed tool inverse script'
 assert sha(manifest)==MANIFEST_SHA,'Unreviewed tool inverse manifest'
 spec=importlib.util.spec_from_file_location('armor_tool_inverse',h/'migrate.py');m=importlib.util.module_from_spec(spec);spec.loader.exec_module(m);d=json.loads(manifest)
 assert m.BASE==d['baseline']==BASE and len(d['files'])==9
 return m,d

def restore(rel,current,m,d):
 row=d['files'][rel]
 if sha(current)!=row['before_sha256']:
  h=Path(m.__file__).parent.parent/'native-world-air-query/tool_composition.py'
  spec=importlib.util.spec_from_file_location('armor_world_air_inverse',h);w=importlib.util.module_from_spec(spec);spec.loader.exec_module(w)
  current=w.normalize(Path(m.__file__).resolve().parents[3],rel,current)
 if sha(current)==row['before_sha256']:return current
 assert sha(current)==row['after_sha256'],'Unreviewed tool source '+rel
 original=m.transform(rel,current.decode(),True,row['rules']).encode()
 assert sha(original)==row['before_sha256'],'Tool inverse mismatch '+rel
 return original

def restore_census(files,root):
 m,d=load(root)
 for rel in d['files']:files[rel]=restore(rel,files[rel],m,d)
 return files

def controls(root):
 m,d=load(root);checks=0;receipts=[]
 for rel,row in d['files'].items():
  current=(Path(root)/rel).read_bytes();before=restore(rel,current,m,d)
  after=m.transform(rel,before.decode(),False,row['rules']).encode();assert restore(rel,after,m,d)==before;checks+=1
  assert restore(rel,before,m,d)==before;checks+=1
  for data in (after+b'\n// drift\n',after[:-1],after.replace(row['rules'][0][1].encode(),row['rules'][0][1].encode()+b'/* drift */',1)):
   try:restore(rel,data,m,d)
   except AssertionError:checks+=1
   else:raise AssertionError('accepted tool drift '+rel)
  receipts.append({'file':rel,'original_sha256':sha(before),'approved_composed_sha256':sha(after)})
 return {'checks':checks,'files':receipts,'census_replaced':False,'protected_pins_replaced':False}
if __name__=='__main__':
 import argparse
 here=Path(__file__).resolve().parent;p=argparse.ArgumentParser();p.add_argument('--source-root',type=Path,default=here.parents[2]);a=p.parse_args();r=controls(a.source_root);(here/'tool-composition-receipt.json').write_text(json.dumps(r,indent=2)+'\n');print('PASS strict tool composition:',r['checks'],'original/composed/drift controls; no census or protected pins replaced')
