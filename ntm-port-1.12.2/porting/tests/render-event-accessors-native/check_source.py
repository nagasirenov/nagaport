#!/usr/bin/env python3
"""Current source-only recovery guard. This does not execute native dependencies."""
import json,hashlib
from pathlib import Path
HERE=Path(__file__).resolve().parent; ROOT=HERE.parents[2]
def sha(b): return hashlib.sha256(b).hexdigest()
def reverse(raw,file):
 assert sha(raw)==file['after'], file['path']+' changed source hash'
 data=raw.decode('utf-8').encode('utf-16-le'); shifted=[];delta=0
 for e in file['edits']:
  start=e['start']+delta; end=start+len(e['new']);shifted.append((start,end,e));delta+=len(e['new'])-len(e['old'])
 for start,end,e in reversed(shifted):
  assert data[start*2:end*2].decode('utf-16-le')==e['new'], 'selected token differs'
  data=data[:start*2]+e['old'].encode('utf-16-le')+data[end*2:]
 before=data.decode('utf-16-le').encode('utf-8');assert sha(before)==file['before'],file['path']+' outside selected tokens changed';return before
if __name__=='__main__':
 manifest=json.loads((HERE/'source-manifest.json').read_text());n=mutations=0
 for file in manifest['files']:
  raw=(ROOT/file['path']).read_bytes();reverse(raw,file);delta=0
  for e in file['edits']:
   n+=1;start=e['start']+delta;end=start+len(e['new']);data=raw.decode().encode('utf-16-le');mutant=(data[:start*2]+e['old'].encode('utf-16-le')+data[end*2:]).decode('utf-16-le').encode()
   try:reverse(mutant,file)
   except AssertionError:mutations+=1
   else:raise AssertionError('unrejected reverted getter')
   delta+=len(e['new'])-len(e['old'])
 assert n==65 and mutations==65 and len(manifest['files'])==10
 print('PASS source-only recovery: 65 exact token edits, 10 complete reversals, 65 rejected single-token regressions; not native tests')
