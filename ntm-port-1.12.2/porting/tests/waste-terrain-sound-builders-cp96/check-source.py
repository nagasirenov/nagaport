#!/usr/bin/env python3
"""Exact four-file inverse; never claims acceptance of unrelated composed files."""
import argparse, hashlib, json, re, subprocess
from pathlib import Path
HERE=Path(__file__).resolve().parent; ROOT=HERE.parents[2]; REPO=ROOT.parent
BASE='58f7f0c93cb4f5814d552e003ec9c7e2179a115f'
MOD='src/main/java/com/hbm/blocks/ModBlocks.java'
OWNERS=['WasteEarth','WasteLog','WasteLeaves']
FAMILY=[MOD]+['src/main/java/com/hbm/blocks/generic/'+x+'.java' for x in OWNERS]
EXPECTED=[('waste_earth','WasteEarth','PLANT'),('waste_mycelium','WasteEarth','PLANT'),('waste_log','WasteLog','WOOD'),('waste_leaves','WasteLeaves','PLANT'),('frozen_grass','WasteEarth','GLASS'),('frozen_log','WasteLog','GLASS'),('burning_earth','WasteEarth','PLANT')]
def inputs():
 old={f:subprocess.check_output(['git','-C',str(REPO),'show',BASE+':ntm-port-1.12.2/'+f]) for f in FAMILY}
 return old,{f:(ROOT/f).read_bytes() for f in FAMILY}
def validate(old,new):
 assert set(old)==set(new)==set(FAMILY), 'Exact four existing owners/registry files only'
 rows=json.loads((HERE/'sites.json').read_text());adds=json.loads((HERE/'additions.json').read_text())
 assert [(r['name'],r['owner'],r['sound'].removeprefix('SoundType.')) for r in rows]==EXPECTED
 assert set(adds)==set(FAMILY[1:]);inverse=dict(new)
 for r in rows:
  owner=r['owner'];sound=r['sound'];receiver=r['receiver']
  assert re.fullmatch('new '+owner+r'\(Material\.\w+(?:, (?:true|false))?\)\.setTranslationKey\("'+r['name']+r'"\)',receiver), 'Cast is only over this exact named fresh receiver'
  assert old[MOD].decode().splitlines()[r['line']-1]==r['before']
  assert r['after']==r['before'].replace(receiver+'.setStepSound('+sound+')','(('+owner+') '+receiver+').withSoundType('+sound+')')
  before,after=r['before'].encode(),r['after'].encode();assert old[MOD].count(before)==new[MOD].count(after)==1
  inverse[MOD]=inverse[MOD].replace(after,before)
 for path,add in adds.items():
  owner=Path(path).stem
  exact='\n\t/** NTM construction hook for the native protected sound setter. */\n\tpublic '+owner+' withSoundType(net.minecraft.block.SoundType sound) {\n\t\tsuper.setSoundType(sound);\n\t\treturn this;\n\t}\n'
  assert add==exact and new[path].count(add.encode())==1
  inverse[path]=new[path].replace(add.encode(),b'')
  assert ('class '+owner+' extends Block {').encode() in old[path]
  for method in ['setSoundType','setTranslationKey','setHardness','setResistance','setCreativeTab','setLightLevel']:
   assert not re.search(rb'\b(?:public|protected|private)\s+\w+\s+'+method.encode()+rb'\s*\(',new[path]), 'Cannot cast across an owner override: '+method
 for path in FAMILY:assert inverse[path]==old[path], 'Whole-file inverse differs: '+path
 assert new[MOD].count(b'.setStepSound(')==old[MOD].count(b'.setStepSound(')-7
 assert new[MOD].count(b'.setBlockTextureName(')==old[MOD].count(b'.setBlockTextureName(')
 assert new[MOD].count(b'\r\n')==old[MOD].count(b'\r\n') and new[MOD].count(b'\n')==old[MOD].count(b'\n')
 for row in json.loads((HERE/'source-hashes.json').read_text()):
  path=row['path'];assert hashlib.sha256(old[path]).hexdigest()==row['before_sha256'] and hashlib.sha256(new[path]).hexdigest()==row['after_sha256'], 'Exact tested source hash mismatch: '+path
 return len(old)
if __name__=='__main__':
 assert __debug__;p=argparse.ArgumentParser();p.add_argument('--family-only',action='store_true',help='Four complete files only; this guard never claims composed unrelated source acceptance');p.parse_args()
 for path,digest in json.loads((HERE/'prerequisite-hashes.json').read_text()).items():assert hashlib.sha256((ROOT/path).read_bytes()).hexdigest()==digest,'Changed probe dependency: '+path
 assert validate(*inputs())==4
 print('PASS exact CP95 inverse over all four complete family files; seven fresh-owner casts; exact constructor arguments/sound objects/chain positions; all texture and gameplay bytes unchanged. FOUR-FILE FAMILY ONLY; no claim about unrelated composed sources.')
