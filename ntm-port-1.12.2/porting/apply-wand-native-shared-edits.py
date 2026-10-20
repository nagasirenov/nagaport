#!/usr/bin/env python3
"""Coordinator-only explicit byte edits; leaves all unrelated bytes unchanged."""
import json,sys
from pathlib import Path
module=Path(sys.argv[1]).resolve()
edits=json.loads((Path(__file__).parent/'wand-native-shared-edits.json').read_text())
planned=[]
for e in edits:
 p=module/e['path'];old=p.read_bytes();before=e['before'].encode();after=e['after'].encode()
 if old.count(before)!=1:raise SystemExit('Expected exactly one unchanged shared fragment: '+str(p))
 planned.append((p,old.replace(before,after,1)))
for p,data in planned:p.write_bytes(data);print('Applied bounded byte edit:',p)
