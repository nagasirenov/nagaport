#!/usr/bin/env python3
"""Newest complete furnace block layer, reversed BEFORE checkpoint17 drop/EMPTY audits.
Do not regenerate historic hashes. ModBlocks change is exactly four names and CRLF-safe.
"""
import hashlib,json
from pathlib import Path
ROOT=Path(__file__).resolve().parents[2]
MANIFEST=json.loads((ROOT/'porting/tests/furnace-block-source-manifest.json').read_text(encoding='utf-8'))
FILES={entry['path']:entry for entry in MANIFEST['files']}
def sha(text):return hashlib.sha256(text.encode('utf-8')).hexdigest()
def reverse_source(relative,text):
 entry=FILES.get(relative)
 if entry is None:return text
 assert sha(text)==entry['after_sha256'],(relative,'native furnace block whole-file drift')
 lines=text.splitlines(keepends=True)
 for edit in reversed(entry['edits']):
  start,end=edit['after_start'],edit['after_end']
  assert ''.join(lines[start:end])==edit['new'],(relative,'native furnace block slice drift')
  lines[start:end]=edit['old'].splitlines(keepends=True)
 before=''.join(lines)
 assert sha(before)==entry['before_sha256'],(relative,'checkpoint17 native furnace block baseline drift')
 return before
def main():
 assert len(FILES)==2
 rejected=0
 for relative in FILES:
  text=(ROOT/relative).read_bytes().decode('utf-8');before=reverse_source(relative,text)
  for drift in [text+'\n',before,text.replace('machine_electric_furnace_off','machine_electric_furnace_other',1)]:
   try:reverse_source(relative,drift)
   except AssertionError:rejected+=1
   else:raise AssertionError((relative,'drift not rejected'))
 print('PASS: two native furnace block whole-file reversals and %d drift rejections; exact checkpoint17 bytes preserved.'%rejected)
if __name__=='__main__':main()
