#!/usr/bin/env python3
"""Exact three-file inverse, CP92 baseline; optional explicit family-only integration scope."""
import argparse, hashlib, io, json, re, subprocess, tarfile
from pathlib import Path
HERE=Path(__file__).resolve().parent; ROOT=HERE.parents[2]; REPO=ROOT.parent
BASE='0675e599e5ee31a60776b560af792372df191790'
MOD='src/main/java/com/hbm/blocks/ModBlocks.java'
EXPECTED=['block_insulator','block_fiberglass','block_tritium','pink_log','vinyl_log','pvc_log','lattice_log','bf_log','primed_log','eu_log']
FAMILY=[MOD]+['src/main/java/com/hbm/blocks/generic/'+x+'.java' for x in ['BlockLogNT','BlockRotatablePillar']]
def inputs(family=False):
 if family:
  old={f:subprocess.check_output(['git','-C',str(REPO),'show',BASE+':ntm-port-1.12.2/'+f]) for f in FAMILY}
  return old,{f:(ROOT/f).read_bytes() for f in FAMILY}
 archive=subprocess.check_output(['git','-C',str(REPO),'archive',BASE,'ntm-port-1.12.2/src/main/java'])
 with tarfile.open(fileobj=io.BytesIO(archive)) as z: old={m.name.split('/',1)[1]:z.extractfile(m).read() for m in z.getmembers() if m.isfile() and m.name.endswith('.java')}
 new={p.relative_to(ROOT).as_posix():p.read_bytes() for p in (ROOT/'src/main/java').rglob('*.java')}
 return old,new
def validate(old,new):
 rows=json.loads((HERE/'sites.json').read_text()); adds=json.loads((HERE/'additions.json').read_text())
 assert set(old)==set(new), 'Added/removed production owner'
 assert [r['name'] for r in rows]==EXPECTED and set(adds)==set(FAMILY[1:])
 inverse=dict(new)
 for i,r in enumerate(rows):
  owner='BlockRotatablePillar' if i<3 else 'BlockLogNT';sound='SoundType.CLOTH' if i<2 else 'SoundType.GLASS' if i==2 else 'SoundType.WOOD'
  assert r['owner']==owner and r['sound']==sound
  assert r['receiver'].startswith('new '+owner+'(')
  assert old[MOD].decode().splitlines()[r['line']-1]==r['before']
  assert r['after']==r['before'].replace(r['receiver']+'.setStepSound('+sound+')','(('+owner+') '+r['receiver']+').withSoundType('+sound+')')
  b,a=r['before'].encode(),r['after'].encode(); assert old[MOD].count(b)==new[MOD].count(a)==1
  inverse[MOD]=inverse[MOD].replace(a,b)
 for path,add in adds.items():
  owner=Path(path).stem
  exact='\n\t/** NTM construction hook for the native protected sound setter. */\n\tpublic '+owner+' withSoundType(net.minecraft.block.SoundType sound) {\n\t\tsuper.setSoundType(sound);\n\t\treturn this;\n\t}\n'
  assert add==exact and new[path].count(add.encode())==1
  inverse[path]=new[path].replace(add.encode(),b'')
  parent='BlockLog' if owner=='BlockLogNT' else 'BlockRotatedPillar'
  assert ('class '+owner+' extends '+parent+' ').encode() in old[path]
  for method in ['setSoundType','setTranslationKey','setHardness','setResistance','setCreativeTab']:
   assert not re.search(rb'\b(?:public|protected|private)\s+\w+\s+'+method.encode()+rb'\s*\(',new[path])
 for path in old: assert inverse[path]==old[path], 'Whole-file inverse differs: '+path
 assert old[MOD].count(b'.setStepSound(')==102 and new[MOD].count(b'.setStepSound(')==92
 for row in json.loads((HERE/'source-hashes.json').read_text()):
  path=row['path'];assert hashlib.sha256(old[path]).hexdigest()==row['before_sha256'] and hashlib.sha256(new[path]).hexdigest()==row['after_sha256'], 'Exact tested source hash mismatch: '+path
 return len(old)
if __name__=='__main__':
 assert __debug__
 p=argparse.ArgumentParser();p.add_argument('--family-only',action='store_true',help='Explicitly verify ONLY the three complete family files against CP92; no all-production-source claim');a=p.parse_args()
 for path,digest in json.loads((HERE/'prerequisite-hashes.json').read_text()).items():
  assert hashlib.sha256((ROOT/path).read_bytes()).hexdigest()==digest, 'Changed native probe dependency: '+path
 count=validate(*inputs(a.family_only))
 print('PASS exact CP92 whole-file inverse across %d production files; ten closed fresh-owner casts; exact original sound arguments and full chain order; unchanged constructor, texture and gameplay bodies. Scope: %s.'%(count,'THREE-FILE FAMILY ONLY' if a.family_only else 'ALL PRODUCTION JAVA'))
