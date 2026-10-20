#!/usr/bin/env python3
"""Preflight exact call-boundary edits; never replace whole existing production files."""
import argparse
from pathlib import Path
import audit
p=argparse.ArgumentParser();p.add_argument('--target',type=Path,required=True);p.add_argument('--apply',action='store_true');a=p.parse_args();pending=[]
for rel,r in audit.manifest()['files'].items():
 path=a.target/rel;old=path.read_bytes().decode();pending.append((path,audit.transform(old,r).encode()))
for source,dest in [(audit.ROOT/'src/main/java/com/hbm/world/WorldBlockState.java',a.target/'src/main/java/com/hbm/world/WorldBlockState.java')]+[(s,a.target/'porting/tests/native-world-reads'/s.name) for s in audit.HERE.iterdir() if s.is_file()]:
 data=source.read_bytes()
 if dest.exists():assert dest.read_bytes()==data,('New file collision',dest)
 else:pending.append((dest,data))
print('PASS: %d writes preflighted; argument interiors retain concurrent changes'%len(pending))
if a.apply:
 for p,data in pending:p.parent.mkdir(parents=True,exist_ok=True);p.write_bytes(data)
 print('Applied exact receiver and closing boundaries only; no existing whole-file replacement')
