#!/usr/bin/env python3
"""Exact whitelist/count-guarded six-file thermal transformation and reverse audit.
Default verifies current files and rejects drift. --apply-from-baseline applies only
when ALL six exact baseline hashes and every old edit/count match, before any write.
Unknown paths pass through for composed historical source guards; no global null rule.
"""
import argparse,hashlib,json
from pathlib import Path
ROOT=Path(__file__).resolve().parents[2]
MANIFEST=json.loads(Path(__file__).with_name('thermal-furnace-family-source-manifest.json').read_text())
FILES={e['path']:e for e in MANIFEST['files']}
WHITELIST=tuple(FILES)
EXPECTED=tuple(f'src/main/java/com/hbm/{kind}Furnace{name}.java' for name in ('Brick','Iron','Steel') for kind in ('tileentity/machine/TileEntity','inventory/container/Container'))
assert WHITELIST==EXPECTED,'thermal file whitelist changed'
def sha(t):return hashlib.sha256(t.encode('utf-8')).hexdigest()
def transform(relative,text,reverse=False):
 e=FILES.get(str(relative).replace('\\','/'))
 if e is None:return text
 source='after' if reverse else 'before';target='before' if reverse else 'after'
 assert sha(text)==e[source+'_sha256'],(relative,source,'whole-file drift')
 assert len(e['edits'])==e['edit_count'],(relative,'edit count')
 # Descending source offsets make every edit independently count/position guarded.
 for edit in reversed(e['edits']):
  old=edit['new' if reverse else 'old'];new=edit['old' if reverse else 'new'];at=edit[source+'_offset']
  assert text[at:at+len(old)]==old,(relative,'exact edit source/count mismatch')
  text=text[:at]+new+text[at+len(old):]
 assert sha(text)==e[target+'_sha256'],(relative,target,'whole-file mismatch')
 return text
def reverse_source(relative,text):return transform(relative,text,True)
def main(source_transform=None):
 p=argparse.ArgumentParser(description=__doc__);p.add_argument('--apply-from-baseline',action='store_true');a=p.parse_args([] if source_transform is not None else None)
 if a.apply_from_baseline:
  results={r:transform(r,(ROOT/r).read_text()) for r in WHITELIST}
  for r,t in results.items():(ROOT/r).write_text(t)
  print('PASS six-file guarded family apply; all baselines validated before writes.');return
 for r in WHITELIST:
  text=(ROOT/r).read_bytes().decode('utf-8')
  if source_transform is not None:text=source_transform(r,text)
  before=reverse_source(r,text);assert transform(r,before)==text
  for mutant in (text+'\n',text[:-1],text.replace('public ','private ',1)):
   try:reverse_source(r,mutant)
   except AssertionError:pass
   else:raise AssertionError((r,'mutant accepted'))
  if '/container/' in r:
   assert '.onTake(' not in text
   assert text.count('.onSlotChange(')==(0 if 'Brick' in r else 1)
 for r,h in MANIFEST['unchanged_support'].items():
  text=(ROOT/r).read_bytes().decode('utf-8')
  if source_transform is not None:text=source_transform(r,text)
  assert sha(text)==h,(r,'unchanged source support drift')
 assert reverse_source('unrelated.java','anything')=='anything'
 print('PASS six exact thermal whole-file forward/reverse reconstructions; 18 drift mutations rejected; original callback counts and unchanged support verified.')
if __name__=='__main__':main()
