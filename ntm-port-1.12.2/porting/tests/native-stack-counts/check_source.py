#!/usr/bin/env python3
import argparse,hashlib,json,pathlib
h=pathlib.Path(__file__).resolve().parent;root=h.parents[2];data=json.loads((h/'source-manifest.json').read_text())
def sha(raw):return hashlib.sha256(raw).hexdigest()
def forward(raw, info):
 assert sha(raw)==info['before_sha256'],'Baseline bytes changed'
 for e in sorted(info['edits'],key=lambda x:x['start'],reverse=True):
  a,b=e['start'],e['end'];assert raw[a:b]==e['before'].encode();raw=raw[:a]+e['after'].encode()+raw[b:]
 assert sha(raw)==info['after_sha256'];return raw
def reverse(raw,info):
 assert sha(raw)==info['after_sha256'],'Migrated bytes changed'
 delta=0;edits=[]
 for e in sorted(info['edits'],key=lambda x:x['start']):
  a=e['start']+delta;b=a+len(e['after'].encode());edits.append((a,b,e));delta+=len(e['after'].encode())-len(e['before'].encode())
 for a,b,e in reversed(edits):assert raw[a:b]==e['after'].encode();raw=raw[:a]+e['before'].encode()+raw[b:]
 assert sha(raw)==info['before_sha256'];return raw
if __name__=='__main__':
 count=0
 for rel,info in data['files'].items():
  raw=(root/rel).read_bytes();old=reverse(raw,info);assert forward(old,info)==raw
  for e in info['edits']:
   mutant=raw.replace(e['after'].encode(),e['before'].encode(),1)
   try:reverse(mutant,info)
   except AssertionError:count+=1
   else:raise AssertionError('Mutation not rejected')
  try:reverse(raw+b' ',info)
  except AssertionError:count+=1
  else:raise AssertionError('Unrelated byte mutation accepted')
 print('PASS exact forward/inverse UTF8 bytes for',len(data['files']),'files;',count,'mutation controls')
