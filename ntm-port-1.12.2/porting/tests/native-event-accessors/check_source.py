#!/usr/bin/env python3
import json,hashlib
from pathlib import Path
folder=Path(__file__).resolve().parent;root=folder.parents[2]
m=json.loads((folder/'source-manifest.json').read_text());n=0
for file in m['files']:
 raw=(root/file['path']).read_bytes(); assert hashlib.sha256(raw).hexdigest()==file['after'],file['path']
 data=raw.decode().encode('utf-16-le'); shift=0;edits=[]
 for e in file['edits']:
  start=e['start']+shift;end=start+len(e['new']);assert data[start*2:end*2].decode('utf-16-le')==e['new'];edits.append((start,end,e['old']));shift+=len(e['new'])-(e['end']-e['start'])
 for start,end,old in reversed(edits):data=data[:start*2]+old.encode('utf-16-le')+data[end*2:]
 assert hashlib.sha256(data.decode('utf-16-le').encode()).hexdigest()==file['before'];n+=len(edits)
print('PASS exact source inverses:',n,'reads;',len(m['files']),'files; all other bytes retained')
