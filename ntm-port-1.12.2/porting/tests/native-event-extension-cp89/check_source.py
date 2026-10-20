#!/usr/bin/env python3
"""Exact CP88-to-CP89 two-phase native accessor boundary; preserves every unrelated byte."""
from pathlib import Path
import argparse,hashlib,json,subprocess,sys
H=Path(__file__).resolve().parent
RAW=(H/'source-manifest.json').read_bytes()
PIN='f8ad5e58aa4474b008ea74bc3b2dbd5c78e9c8b56df8455cf1d6d2eb110c742e'
assert not sys.flags.optimize
assert hashlib.sha256(RAW).hexdigest()==PIN,'source manifest changed'
M=json.loads(RAW)
assert M['base']=='a65d3acbf05634c3cedbce5eff43ad0bd15cefdb'
def sha(data):return hashlib.sha256(data).hexdigest()
def inverse(data,f):
 assert sha(data)==f['after'],(f['path'],'post source drift')
 raw=data.decode().encode('utf-16-le');shift=0;positions=[]
 for e in f['edits']:
  start=e['start']+shift;positions.append((start,e));shift+=len(e['new'].encode('utf-16-le'))//2-(e['end']-e['start'])
 for start,e in reversed(positions):
  a=start*2;b=a+len(e['new'].encode('utf-16-le'));assert raw[a:b].decode('utf-16-le')==e['new'];raw=raw[:a]+e['old'].encode('utf-16-le')+raw[b:]
 out=raw.decode('utf-16-le').encode();assert sha(out)==f['before'],(f['path'],'baseline drift');return out

def verify(root,verbose=True):
 paths={f['path'] for phase in M['phases'] for f in phase['files']};state={p:(root/p).read_bytes() for p in paths};mutations=0
 for phase in reversed(M['phases']):
  for f in phase['files']:
   original=state[f['path']];before=inverse(original,f);shift=0;data=original.decode().encode('utf-16-le')
   for e in f['edits']:
    a=(e['start']+shift)*2;b=a+len(e['new'].encode('utf-16-le'));bad=(data[:a]+e['old'].encode('utf-16-le')+data[b:]).decode('utf-16-le').encode();assert bad!=original
    try:inverse(bad,f)
    except AssertionError:mutations+=1
    else:raise AssertionError('selected edit reversal accepted')
    shift+=len(e['new'].encode('utf-16-le'))//2-(e['end']-e['start'])
   state[f['path']]=before
 for p,data in state.items():
  git=subprocess.check_output(['git','-C',str(root),'show',M['base']+':ntm-port-1.12.2/'+p]);assert data==git,(p,'does not reconstruct actual CP88 blob')
 assert len(paths)==21 and mutations==175
 if verbose:print('PASS 21 byte-exact CP88 source inverses; 165 read-token and 10 setter edits; 175 per-edit mutations rejected; every unrelated byte preserved.')
 return state
if __name__=='__main__':
 p=argparse.ArgumentParser();p.add_argument('--module',type=Path,default=H.parents[2]);a=p.parse_args();verify(a.module)
