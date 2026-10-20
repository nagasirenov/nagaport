#!/usr/bin/env python3
import tempfile,zipfile,subprocess,json,hashlib,sys
from pathlib import Path
import audit
m=audit.verify()
with tempfile.TemporaryDirectory(prefix='destroy-merge-')as d:
 root=Path(d)/'module';root.mkdir();before={}
 with zipfile.ZipFile(audit.HERE/'baseline.zip')as z:
  for rel in m['files']:
   p=root/rel;p.parent.mkdir(parents=True,exist_ok=True);data=z.read(rel)+b'\n// Independent later-tree edit retained.\n';p.write_bytes(data);before[rel]=data
 receipt=Path(d)/'receipt.json';subprocess.run([sys.executable,str(audit.HERE/'merge.py'),'--module',str(root),'--apply','--receipt',str(receipt)],check=True);audit.verify_receipt(root,receipt)
 for rel in m['files']:assert (root/rel).read_bytes().endswith(b'// Independent later-tree edit retained.\n')
 print('PASS: all 60 invocation-only edits preserve unrelated later-tree content and exact inverse hashes')
 for rel,data in before.items():(root/rel).write_bytes(data)
 rel=next(iter(m['files']));p=root/rel;old=m['files'][rel][0]['old'].encode();p.write_bytes(p.read_bytes().replace(old,old.replace(b'func_147480_a',b'changedCall'),1));snap={f:f.read_bytes()for f in root.rglob('*.java')}
 r=subprocess.run([sys.executable,str(audit.HERE/'merge.py'),'--module',str(root),'--apply','--receipt',str(receipt)],stdout=subprocess.PIPE,stderr=subprocess.STDOUT);assert r.returncode!=0
 assert all(f.read_bytes()==v for f,v in snap.items());print('PASS: changed invocation rejected before any write')
