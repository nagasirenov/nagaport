#!/usr/bin/env python3
"""Exact reversible owned source boundary; shared fragments tolerate unrelated siblings."""
from pathlib import Path
import hashlib,json
HERE=Path(__file__).resolve().parent
ROOT=HERE.parents[2]
RAW=(HERE/'source-manifest.json').read_bytes()
assert hashlib.sha256(RAW).hexdigest()=='bf1f9325dc8e998c8b9bffc076b6c82bb9481a57d326be49464ceb9ffb545d74','Rail scope manifest drift'
MANIFEST=json.loads(RAW)
assert MANIFEST['baseline']=='caf22cf027f33f9d0d0be366454c2f54fdba7915'
def sha(text):return hashlib.sha256(text.encode()).hexdigest()
def reverse_source(relative,text):
 if relative in MANIFEST['owned']:
  e=MANIFEST['owned'][relative];assert sha(text)==e['after_sha256'],(relative,'owned source drift');return e['before'] if e['before'] is not None else ''
 if relative in MANIFEST['shared']:
  for f in MANIFEST['shared'][relative]['fragments']:
   assert text.count(f['new'])==1,(relative,'missing/duplicate native fragment')
   assert f['old'] not in text,(relative,'legacy fragment reintroduced')
   text=text.replace(f['new'],f['old'])
 return text
def verify(root=ROOT):
 rejected=0
 for p,e in MANIFEST['owned'].items():
  text=(Path(root)/p).read_bytes().decode();before=reverse_source(p,text)
  assert (sha(before)==e['before_sha256']) if e['before'] is not None else before==''
  for mutation in [text+'\n',text[:-1]]:
   try:reverse_source(p,mutation)
   except AssertionError:rejected+=1
   else:raise AssertionError((p,'owned mutation accepted'))
 for p,e in MANIFEST['shared'].items():
  text=(Path(root)/p).read_bytes().decode();old=reverse_source(p,text)
  forward=old
  for f in e['fragments']:assert forward.count(f['old'])==1;forward=forward.replace(f['old'],f['new'])
  assert forward==text
  assert reverse_source(p,text+'// unrelated sibling edit\r\n')==old+'// unrelated sibling edit\r\n'
  for f in e['fragments']:
   for mutation in [text.replace(f['new'],''),text+f['new'],text+f['old']]:
    try:reverse_source(p,mutation)
    except AssertionError:rejected+=1
    else:raise AssertionError((p,'shared mutation accepted'))
 assert reverse_source('unrelated.java','unchanged\r\n')=='unchanged\r\n'
 print('PASS rail source scope:',len(MANIFEST['owned']),'owned files;',sum(len(v['fragments']) for v in MANIFEST['shared'].values()),'unique shared fragments;',rejected,'drift/duplication/reintroduction mutations rejected. Not runtime proof.')
if __name__=='__main__':
 import argparse
 parser=argparse.ArgumentParser();parser.add_argument('--root',type=Path,default=ROOT);a=parser.parse_args();verify(a.root)
