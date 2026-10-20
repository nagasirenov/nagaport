#!/usr/bin/env python3
"""Exact 37-call boundary; inverse is reusable by checkpoint integration."""
from pathlib import Path
import hashlib,json,subprocess
HERE=Path(__file__).resolve().parent
ROOT=HERE.parents[3]
MANIFEST=json.loads((HERE/'manifest.json').read_text())
FILES={f['path']:f for f in MANIFEST['files']}
def sha(text):return hashlib.sha256(text.encode()).hexdigest()
def forward_source(relative,text):
 f=FILES[relative]
 assert sha(text)==f['before_sha256'],relative+' baseline source differs'
 for c in reversed(f['calls']):
  assert text[c['start']:c['end']]==c['before']
  text=text[:c['start']]+c['after']+text[c['end']:]
 assert sha(text)==f['after_sha256']
 return text

def reverse_source(relative,text):
 if relative not in FILES:return text
 f=FILES[relative]
 assert sha(text)==f['after_sha256'],relative+' patched source differs'
 offset=0;positions=[]
 for c in f['calls']:
  a=c['start']+offset;positions.append((a,a+len(c['after']),c))
  offset+=len(c['after'])-len(c['before'])
 for a,b,c in reversed(positions):
  assert text[a:b]==c['after']
  text=text[:a]+c['before']+text[b:]
 assert sha(text)==f['before_sha256']
 return text

def verify():
 rejected=0
 for relative,f in FILES.items():
  text=(ROOT/relative).read_bytes().decode();original=reverse_source(relative,text)
  sealed=subprocess.check_output(['git','-C',str(ROOT),'show',MANIFEST['baseline']+':'+relative]).decode()
  assert original==sealed and forward_source(relative,sealed)==text
  offset=0
  for c in f['calls']:
   start=c['start']+offset;end=start+len(c['after']);assert text[start:end]==c['after']
   offset+=len(c['after'])-len(c['before'])
   # Wrong target, modified delay and block/coordinate identity must all fail.
   for bad in [c['after'].replace('.scheduleUpdate(','.scheduleBlockUpdate(',1),c['after'][:-1]+' + 1)',c['after'].replace('BlockPos(','BlockPos(1 + ',1)]:
    try:reverse_source(relative,text[:start]+bad+text[end:])
    except AssertionError:rejected+=1
    else:raise AssertionError('accepted mutation '+relative)
  try:reverse_source(relative,text+'\n')
  except AssertionError:rejected+=1
  else:raise AssertionError('accepted unrelated edit')
 assert rejected==132
 print('PASS exact forward/reversal: 37 calls in 21 files; 132 wrong-target/delay/coordinate/unrelated mutations rejected; surrounding bytes preserved.')
if __name__=='__main__':verify()
