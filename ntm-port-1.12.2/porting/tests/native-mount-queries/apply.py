#!/usr/bin/env python3
"""Replay only the exact pinned expression edits; reject all source drift first."""
import pathlib,json,hashlib,argparse
p=argparse.ArgumentParser();p.add_argument('repository',type=pathlib.Path);p.add_argument('--reverse',action='store_true');a=p.parse_args();m=json.loads(pathlib.Path(__file__).with_name('manifest.json').read_text());writes={}
for rel,meta in m['files'].items():
 path=a.repository/rel;raw=path.read_bytes();assert hashlib.sha256(raw).hexdigest()==meta['after_sha256' if a.reverse else 'before_sha256'],rel
 s=raw.decode();edits=[e for e in m['edits'] if e['path']==rel];changes=[];shift=0
 for e in edits:
  if a.reverse:
   start=e['start']+shift;end=start+len(e['after']);old=e['after'];new=e['before'];shift+=len(e['after'])-len(e['before'])
  else:start=e['start'];end=e['end'];old=e['before'];new=e['after']
  assert s[start:end]==old;changes.append((start,end,new))
 for start,end,new in reversed(changes):s=s[:start]+new+s[end:]
 data=s.encode();assert hashlib.sha256(data).hexdigest()==meta['before_sha256' if a.reverse else 'after_sha256'];writes[path]=data
for path,data in writes.items():path.write_bytes(data)
print('PASS',len(writes),'exact pinned file transformations')
