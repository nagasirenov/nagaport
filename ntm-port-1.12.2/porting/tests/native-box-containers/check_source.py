#!/usr/bin/env python3
"""Strict bounded complete-file inverse, protected sources, candidate non-activation."""
from pathlib import Path
import hashlib,json,subprocess
H=Path(__file__).resolve().parent;R=H.parents[2];D=json.loads((H/'manifest.json').read_text())
def sha(b):return hashlib.sha256(b).hexdigest()
def read(rel):
 p=R/rel
 return p.read_bytes() if p.exists() else subprocess.check_output(['git','-C',str(R.parent),'show','HEAD:ntm-port-1.12.2/'+rel])
def restore_base(rel,data):
 row=D['files'][rel];assert sha(data)==row['after_sha256'],('source drift',rel)
 text=data.decode()
 for old,new in reversed(row['rules']):assert text.count(new)==1,('inverse multiplicity',rel);text=text.replace(new,old)
 out=text.encode();assert sha(out)==row['before_sha256'],('inverse mismatch',rel);return out
controls=0
for rel,row in D['files'].items():
 data=read(rel);old=restore_base(rel,data);assert old==subprocess.check_output(['git','-C',str(R.parent),'show',D['baseline']+':ntm-port-1.12.2/'+rel])
 for mutant in [data+b'\n// unreviewed\n',data[:-1],data.replace(row['rules'][0][1].encode(),row['rules'][0][0].encode(),1)]:
  try:restore_base(rel,mutant)
  except AssertionError:controls+=1
  else:raise AssertionError(('accepted mutation',rel))
for rel,digest in D['new'].items():assert sha(read(rel))==digest,('new helper drift',rel)
for rel,digest in D['protected'].items():assert sha(read(rel))==digest,('protected drift',rel)
receipt=json.loads((H/'receipt.json').read_text())
for rel,digest in receipt['complete'].items():assert sha(read('src/main/java/'+rel))==digest,('runtime source stale',rel)
for rel,row in receipt['projections'].items():assert sha(read('src/main/java/'+rel))==row['sha256'],('runtime projection stale',rel)
for name,key in [('NativeBoxContainersTest.java','test_sha256'),('run.py','generator_sha256'),('AdaptOriginalMerge.java','adapter_sha256')]:assert sha((H/name).read_bytes())==receipt[key],('test receipt stale',name)
assert len(receipt['rejected_mutants'])==4 and receipt['assertions']==11370
print('PASS 3 exact complete-file CP94 inverses;',controls,'drift controls; 1 complete helper;',len(D['protected']),'protected pins; native dispatch remains unchanged/unactivated; runtime receipt current')
