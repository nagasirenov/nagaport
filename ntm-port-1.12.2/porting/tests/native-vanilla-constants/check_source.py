#!/usr/bin/env python3
"""Exact source inverse plus mutation rejection; not a native behavior test."""
from pathlib import Path
import json,hashlib
h=Path(__file__).resolve().parent;m=h.parents[2];data=json.loads((h/'source-manifest.json').read_text());assert data['baseline']=='077492a6c31c910f34697783f1aa0cfabd8ff88e'
sha=lambda b:hashlib.sha256(b).hexdigest()
def reverse(raw,e):
 assert sha(raw)==e['after_sha256'];units=raw.decode().encode('utf-16-le')
 for x in sorted(e['edits'],key=lambda x:x['after_utf16'],reverse=True):
  p=x['after_utf16']*2;n=x['new'].encode('utf-16-le');assert units[p:p+len(n)]==n;units=units[:p]+x['old'].encode('utf-16-le')+units[p+len(n):]
 result=units.decode('utf-16-le').encode();assert sha(result)==e['before_sha256'];return result
count=0;rejected=0
for f,e in data['files'].items():
 raw=(m/f).read_bytes();old=reverse(raw,e);units=old.decode().encode('utf-16-le')
 for x in sorted(e['edits'],key=lambda x:x['start_utf16'],reverse=True):
  a,b=x['start_utf16']*2,x['end_utf16']*2;assert units[a:b].decode('utf-16-le')==x['old'];units=units[:a]+x['new'].encode('utf-16-le')+units[b:];count+=1
 assert units.decode('utf-16-le').encode()==raw
 for changed in [raw+b'\n',raw[:-1],old]:
  try:reverse(changed,e)
  except AssertionError:rejected+=1
  else:raise AssertionError('mutation accepted: '+f)
print('PASS',count,'exact native constant replacements across',len(data['files']),'files;',rejected,'source drift/reintroduction mutations rejected. Original newlines/comments/unrelated bytes recover exactly; not full-class compile/gameplay proof.')
