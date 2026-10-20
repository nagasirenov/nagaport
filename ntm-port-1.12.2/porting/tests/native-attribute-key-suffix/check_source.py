#!/usr/bin/env python3
import hashlib,json,pathlib
h=pathlib.Path(__file__).resolve().parent;root=h.parents[2];data=json.loads((h/'source-manifest.json').read_text());sha=lambda b:hashlib.sha256(b).hexdigest()
def reverse(raw,row):
 assert sha(raw)==row['after_sha256'],'post-source drift'
 u=raw.decode().encode('utf-16-le')
 for e in sorted(row['edits'],key=lambda e:e['after_start'],reverse=True):
  lo=e['after_start']*2;hi=lo+len(e['new'].encode('utf-16-le'));assert u[lo:hi].decode('utf-16-le')==e['new'];u=u[:lo]+e['old'].encode('utf-16-le')+u[hi:]
 old=u.decode('utf-16-le').encode();assert sha(old)==row['before_sha256'],'inverse mismatch';return old
def reverse_source(relative,text):
 if relative not in data['files']:return text
 return reverse(text.encode(),data['files'][relative]).decode()
def main():
 checks=0
 for rel,row in data['files'].items():
  raw=(root/rel).read_bytes();reverse(raw,row)
  try:reverse(raw+b' ',row)
  except AssertionError:checks+=1
  else:raise AssertionError('whole-file drift accepted')
  for e in row['edits']:
   u=raw.decode().encode('utf-16-le');at=e['after_start']*2;mutated=(u[:at]+b'X\0'+u[at+2:]).decode('utf-16-le').encode()
   try:reverse(mutated,row)
   except AssertionError:checks+=1
   else:raise AssertionError('token drift accepted')
 print('PASS',len(data['files']),'exact byte inverses and',checks,'whole-file/token drift negatives; no historical baseline hashes refreshed')
if __name__=='__main__':main()
