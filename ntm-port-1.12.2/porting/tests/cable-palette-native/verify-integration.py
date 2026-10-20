#!/usr/bin/env python3
import argparse,hashlib,json
from pathlib import Path
h=Path(__file__).resolve().parent
p=argparse.ArgumentParser();p.add_argument('--module',type=Path,required=True);a=p.parse_args()
manifest=json.loads((h/'manifest.json').read_text())
for name,record in manifest['files'].items():assert hashlib.sha256((a.module/name).read_bytes()).hexdigest()==record['after'],name
print('CURRENT SOURCE INTEGRATION PASS: all four target files exactly match the retained frozen44 result SHA256 values. Read-only verification; no native execution.')
