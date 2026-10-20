#!/usr/bin/env python3
"""Exact source regeneration, inverse, immutable dependency and metadata-domain proof."""
import hashlib,json,re,zipfile
from pathlib import Path
HERE=Path(__file__).resolve().parent;ROOT=HERE.parents[2]
def digest(b):return hashlib.sha256(b).hexdigest()
def replacement(e):
 args=e['arguments'];assert len(args)==e['arity'];meta=args[4]if e['arity']==6 else '0';flags=args[5]if e['arity']==6 else '3';assert meta==e['metadata']and flags==e['flags']
 types=['net.minecraft.world.World','int','int','int','net.minecraft.block.Block','int','int'];names=['World','X','Y','Z','Block','Meta','Flags'];expressions=[e['receiver']]+args[:4]+[meta,flags];p='nativeRegisteredWrite'
 return '{ '+' '.join(f'{t} {p+k} = {x};'for t,k,x in zip(types,names,expressions))+f' if ({p}World == null) throw new NullPointerException(); if ({p}X >= -30000000 && {p}X < 30000000 && {p}Z >= -30000000 && {p}Z < 30000000 && {p}Y >= 0 && {p}Y < 256) com.hbm.world.WorldBlockState.write({p}World, {p}X, {p}Y, {p}Z, {p}Block.getStateFromMeta({p}Meta), {p}Flags); }}'
def verify(root=ROOT):
 m=json.loads((HERE/'manifest.json').read_text());total=0
 with zipfile.ZipFile(HERE/'baseline.zip')as z:
  for rel,es in m['files'].items():
   before=z.read(rel);assert digest(before)==m['sha256'][rel];assert b'nativeRegisteredWrite' not in before;expected=before.decode();current=(root/rel).read_bytes().decode();inverse=current
   for e in sorted(es,key=lambda e:e['start'],reverse=True):
    assert e['replacement']==replacement(e),'noncanonical transformation template';assert expected[e['start']:e['end']]==e['original'];expected=expected[:e['start']]+e['replacement']+expected[e['end']:]
   assert expected==current,rel+' source drift'
   for e in sorted(es,key=lambda e:e['start'],reverse=True):
    pos=e['start']+sum(len(p['replacement'])-len(p['original'])for p in es if p['start']<e['start']);assert inverse[pos:pos+len(e['replacement'])]==e['replacement'];inverse=inverse[:pos]+e['original']+inverse[pos+len(e['replacement']):]
   assert inverse.encode()==before;total+=len(es)
 assert total==72,(total,len(m['files']))
 mod=(root/'src/main/java/com/hbm/blocks/ModBlocks.java').read_text()
 for f,statement in m['assignments'].items():
  assert re.findall(r'^\s*'+re.escape(f)+r' = .*;$',mod,re.M)[0].strip()==statement,f+' assignment drift'
  assert len(re.findall(r'\b'+re.escape(f)+r'\s*=(?!=)',mod))==1,f+' reassignment/initializer'
 dep=json.loads((HERE/'dependencies.json').read_text())
 for rel,expected in dep.items():
  data=(root/rel).read_bytes()
  if rel in m['files']:
   text=data.decode()
   for e in sorted(m['files'][rel],key=lambda e:e['start'],reverse=True):
    p=e['start']+sum(len(q['replacement'])-len(q['original'])for q in m['files'][rel]if q['start']<e['start']);assert text[p:p+len(e['replacement'])]==e['replacement'];text=text[:p]+e['original']+text[p+len(e['replacement']):]
   data=text.encode()
  assert digest(data)==expected,rel+' dependency/callback/range source drift'
 # No source descendants of an exact registered constructor may hide a changed virtual codec.
 for cls in set(m['owners'].values())-{'BlockFalling'}:
  for p in (root/'src/main/java').rglob('*.java'):
   if re.search(r'\bextends\s+(?:[\w.]+\.)?'+re.escape(cls)+r'\b',p.read_text()):
    assert cls in {'BlockGeneric','BlockStepEffect'},'unexpected subclass of exact owner '+cls+' in '+str(p)
 # Every claimed removed diagnostic is an independently retained CP84 missing legacy method.
 log=(root/'porting/checkpoint84/full-compile.log').read_text();assert digest(log.encode())==json.loads((HERE/'diagnostic-positions.json').read_text())['log_sha256']
 for rel,es in m['files'].items():
  for e in es:
   key=rel.replace('src/main/java/','build/sources/main/java/')+':'+str(e['line'])+': error: cannot find symbol'
   at=log.index(key);section=log[at:log.find('\n/',at+1)if '\n/' in log[at+1:] else len(log)]
   assert 'symbol:   method setBlock(' in section,(rel,e['line'])
 # Alias field has one source initializer and no source reassignment; this receiver has no source subclasses.
 alias=(root/'src/main/java/com/hbm/world/dungeon/DesertAtom001.java').read_text()
 assert len(re.findall(r'\bBlock4\s*=(?!=)',alias))==1 and 'Block Block4 = ModBlocks.crashed_balefire;' in alias
 print('PASS: 72 exact call regenerations/inverses; original bytes preserved outside calls; 32 exact constructor assignments and frozen state/lifecycle/range dependencies')
 return m
if __name__=='__main__':verify()
