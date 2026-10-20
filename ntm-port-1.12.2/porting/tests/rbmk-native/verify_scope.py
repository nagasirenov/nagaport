#!/usr/bin/env python3
"""Exact frozen owned sources and reverse proof; concurrent shared owners use narrow fragments."""
import argparse,hashlib,json
from pathlib import Path
p=argparse.ArgumentParser();p.add_argument('--module',type=Path,required=True);a=p.parse_args();here=Path(__file__).resolve().parent;scope=json.loads((here/'scope.json').read_text());frozen=0;reversals=0;shared=0
for record in scope['production']:
 raw=(a.module/record['path']).read_bytes()
 if record.get('shared'):
  for edit in json.loads((here/'shared-edits.json').read_text()):
   if edit['path']!=record['path']:continue
   assert raw.count(edit['new'].encode())==1,(record['path'],'missing/ambiguous new shared fragment')
   assert edit['old'].encode() not in raw,(record['path'],'old shared fragment remains')
   shared+=1
  continue
 assert hashlib.sha256(raw).hexdigest()==record['sha256'],record['path']+' drift'
 if 'edits' in record:
  text=raw.decode()
  for edit in reversed(record['edits']):
   assert text[edit['start']:edit['end']]==edit['new'],(record['path'],'inverse fragment mismatch')
   text=text[:edit['start']]+edit['old']+text[edit['end']:]
  assert hashlib.sha256(text.encode()).hexdigest()==record['baseline_sha256'],record['path']+' changed outside declared edits'
  reversals+=1
 frozen+=1
print('PASS frozen RBMK scope:',frozen,'owned paths;',reversals,'exact owner reversals;',shared,'concurrent-safe shared fragments. Source guard is not behavior proof.')
