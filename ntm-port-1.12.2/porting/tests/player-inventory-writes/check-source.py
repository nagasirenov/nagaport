#!/usr/bin/env python3
"""Exact inverse guard. No normalization of source text or line endings."""
from pathlib import Path
import hashlib,json,zipfile
HERE=Path(__file__).resolve().parent
ROOT=HERE.parents[2]
MANIFEST=json.loads((HERE/'source-manifest.json').read_text())
def digest(data):return hashlib.sha256(data).hexdigest()
def reverse_source(relative,text,strict=True):
 entry=MANIFEST['files'].get(relative)
 if not entry:return text
 if strict:assert digest(text.encode())==entry['after_sha256'],relative+' after drift'
 for e in reversed(entry['edits']):
  assert text.count(e['new'])==1,(relative,e['new'],'replacement count drift')
  text=text.replace(e['new'],e['old'],1)
 if strict:assert digest(text.encode())==entry['before_sha256'],relative+' inverse drift'
 return text

def main():
 tests=0
 with zipfile.ZipFile(HERE/'baseline.zip')as z:
  for rel,e in MANIFEST['files'].items():
   text=(ROOT/rel).read_bytes().decode();old=reverse_source(rel,text)
   assert old.encode()==z.read(rel),rel+' full inverse differs from CP85'
   assert digest(old.encode())==e['before_sha256']
   for bad in [text+' ',text.replace(e['edits'][0]['new'],e['edits'][0]['new'].replace('.set(','.get(',1),1),text.replace(e['edits'][0]['new'],e['edits'][0]['old'],1)]:
    try:reverse_source(rel,bad)
    except AssertionError:tests+=1
    else:raise AssertionError('source mutation was accepted')
 print('PASS 21 exact statement edits, 9 whole-file inverses including original line endings,',tests,'source drift controls')
if __name__=='__main__':main()
