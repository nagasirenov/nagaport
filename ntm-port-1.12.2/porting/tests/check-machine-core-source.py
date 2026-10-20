#!/usr/bin/env python3
"""Exact frozen five-file core/proxy source reversal, no Git dependency."""
from pathlib import Path
import hashlib,json
ROOT=Path(__file__).resolve().parents[2]
FILES={e['path']:e for e in json.loads((ROOT/'porting/tests/machine-core-source-manifest.json').read_text(encoding='utf-8'))['files']}
def sha(text):return hashlib.sha256(text.encode('utf-8')).hexdigest()
def reverse_source(relative,text):
 e=FILES.get(relative)
 if e is None:return text
 assert sha(text)==e['after_sha256'],(relative,'core native source drift')
 assert sha(e['before_source'])==e['before_sha256'],(relative,'core frozen baseline corrupt')
 return e['before_source']
def main():
 for rel in FILES:
  text=(ROOT/rel).read_bytes().decode('utf-8');reverse_source(rel,text)
  try:reverse_source(rel,text+'\n')
  except AssertionError:pass
  else:raise AssertionError((rel,'drift accepted'))
 print('PASS five exact core/proxy whole-file reversals and drift rejections')
if __name__=='__main__':main()
