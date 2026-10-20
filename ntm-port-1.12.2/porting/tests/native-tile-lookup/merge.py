#!/usr/bin/env python3
"""Apply only exact approved fragments to another project, preserving other workers' edits.
Preflight all files before writing. Default is dry-run; pass --apply to commit writes.
"""
import argparse,importlib.util
from pathlib import Path
HERE=Path(__file__).resolve().parent
spec=importlib.util.spec_from_file_location('tile_audit',HERE/'audit.py');audit=importlib.util.module_from_spec(spec);spec.loader.exec_module(audit)
p=argparse.ArgumentParser();p.add_argument('--target',required=True,type=Path);p.add_argument('--apply',action='store_true');a=p.parse_args();pending=[];already=0
for rel,record in audit.manifest()['files'].items():
 path=a.target/rel;text=path.read_bytes().decode()
 try:new=audit.transform(text,record)
 except AssertionError:
  audit.transform(text,record,True);already+=1;continue
 pending.append((path,new.encode()))
for source in HERE.iterdir():
 if not source.is_file():continue
 dest=a.target/'porting/tests/native-tile-lookup'/source.name;data=source.read_bytes()
 if dest.exists():assert dest.read_bytes()==data,('Existing evidence file differs',dest)
 else:pending.append((dest,data))
print('Preflight PASS: %d writes, %d source files already migrated'%(len(pending),already))
if a.apply:
 for path,data in pending:path.parent.mkdir(parents=True,exist_ok=True);path.write_bytes(data)
 print('Applied exact fragments and new evidence; no whole production file replacement from worker snapshot')
