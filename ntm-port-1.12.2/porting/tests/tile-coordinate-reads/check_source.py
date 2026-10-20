#!/usr/bin/env python3
import hashlib,json,pathlib
H=pathlib.Path(__file__).resolve().parent;ROOT=H.parents[2]
def manifest():return json.loads((H/'source-manifest.json').read_text())
def reverse_source(rel,text,spec=None):
 f=(spec or manifest())['files'].get(rel)
 if f is None:return text
 assert hashlib.sha256(text.encode()).hexdigest()==f['after_sha256'],(rel,'after drift')
 raw=text.encode('utf-16-le')
 for e in sorted(f['edits'],key=lambda e:e['after_start'],reverse=True):
  lo=e['after_start']*2;hi=lo+len(e['new'].encode('utf-16-le'));assert raw[lo:hi].decode('utf-16-le')==e['new'];raw=raw[:lo]+e['old'].encode('utf-16-le')+raw[hi:]
 result=raw.decode('utf-16-le');assert hashlib.sha256(result.encode()).hexdigest()==f['before_sha256'],(rel,'inverse drift');return result
def main():
 m=manifest();n=0
 for rel,f in m['files'].items():
  text=(ROOT/rel).read_bytes().decode();before=reverse_source(rel,text,m)
  for e in f['edits']:
   n+=1;at=e['after_start']*2;raw=text.encode('utf-16-le');bad=(raw[:at]+'X'.encode('utf-16-le')+raw[at+2:]).decode('utf-16-le')
   try:reverse_source(rel,bad,m)
   except AssertionError:pass
   else:raise AssertionError('mutation accepted')
  try:reverse_source(rel,text+' ',m)
  except AssertionError:pass
  else:raise AssertionError('unrelated drift accepted')
 print('PASS',n,'coordinate token mutation negatives,',len(m['files']),'exact inverses and whole-file drift negatives')
if __name__=='__main__':main()
