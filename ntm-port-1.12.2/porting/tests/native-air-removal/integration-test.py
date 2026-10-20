#!/usr/bin/env python3
import tempfile,zipfile,subprocess,sys,json
from pathlib import Path
D=Path(__file__).resolve().parent;sys.path.insert(0,str(D));import audit
with tempfile.TemporaryDirectory()as t:
 t=Path(t)
 with zipfile.ZipFile(D/'baseline.zip')as z:z.extractall(t)
 first=next(iter(json.loads((D/'manifest.json').read_text())['files']));p=t/first;p.write_bytes(b'// Independent later-tree edit.\n'+p.read_bytes())
 subprocess.run([sys.executable,str(D/'merge.py'),'--module',str(t),'--apply','--receipt',str(t/'receipt.json')],check=True)
 audit.verify_receipt(t,t/'receipt.json');assert p.read_bytes().startswith(b'// Independent later-tree edit.\n')
 p.write_bytes(p.read_bytes()+b'// unreceipted mutation\n')
 try:audit.verify_receipt(t,t/'receipt.json');raise RuntimeError('mutation survived')
 except AssertionError:print('PASS: receipt rejects unreceipted source mutation; independent pre-integration edits preserved')
