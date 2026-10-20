#!/usr/bin/env python3
import hashlib,json,pathlib
h=pathlib.Path(__file__).resolve().parent;root=h.parents[2];data=json.loads((h/'source-manifest.json').read_text())
def reverse(raw,info):
 b=raw.decode('utf-8').encode('utf-16-le')
 for e in sorted(info['edits'],key=lambda e:e['after_start'],reverse=True):
  lo=e['after_start']*2;hi=lo+len(e['new'])*2
  assert b[lo:hi].decode('utf-16-le')==e['new'],'changed native field fragment'
  b=b[:lo]+e['old'].encode('utf-16-le')+b[hi:]
 return b.decode('utf-16-le').encode('utf-8')
def check(raw,info):
 assert hashlib.sha256(raw).hexdigest()==info['after_sha256'],'unrelated bytes changed'
 assert hashlib.sha256(reverse(raw,info)).hexdigest()==info['before_sha256'],'inverse not exact'
def main():
 n=0
 for rel,info in data['files'].items():
  raw=(root/rel).read_bytes();check(raw,info)
  for bad in [raw+b' ',reverse(raw,info),raw.replace(b'getEntityBoundingBox',b'GETEntityBoundingBox',1)]:
   try:check(bad,info)
   except (AssertionError,UnicodeError):n+=1
   else:raise AssertionError('accepted mutation '+rel)
 print('PASS',len(data['files']),'exact forward/inverse files and',n,'source mutation negative controls')
if __name__=='__main__':main()
