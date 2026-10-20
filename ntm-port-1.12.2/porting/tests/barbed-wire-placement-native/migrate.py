#!/usr/bin/env python3
"""Bounded mutable concrete-field placement migration; no generic metadata assumptions."""
import argparse, collections, hashlib, importlib.util, json, subprocess, tempfile, zipfile
from pathlib import Path
HERE=Path(__file__).resolve().parent
BASE='621a2656fe88635d67ea54e5836ec35aeaf57d16'
spec=importlib.util.spec_from_file_location('prior',HERE.parent/'vanilla-placement-expanded/migrate.py');prior=importlib.util.module_from_spec(spec);spec.loader.exec_module(prior)
def replacement(r): return 'com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock('+r['receiver']+', '+', '.join(r['args'][:4])+');'
def h(b): return hashlib.sha256(b).hexdigest()
def main():
 p=argparse.ArgumentParser();p.add_argument('--module',type=Path,default=HERE.parents[2]);p.add_argument('--java-home',type=Path,required=True);p.add_argument('--classpath-file',type=Path,required=True);p.add_argument('--apply',action='store_true');a=p.parse_args();cp=a.classpath_file.read_text().strip()
 if a.apply:
  paths=[a.module/'src/main/java/com/hbm/world/dungeon'/n for n in ['DesertAtom002.java', 'DesertAtom003.java']]
  source={str(p.relative_to(a.module)):p.read_bytes() for p in paths}
  for n,b in source.items(): assert b==subprocess.check_output(['git','show',BASE+':ntm-port-1.12.2/'+n],cwd=a.module),'Pinned CP56 input required '+n
 else:
  with zipfile.ZipFile(HERE/'sources.zip') as z: source={n:z.read(n) for n in z.namelist()}
  assert {n:h(b) for n,b in sorted(source.items())}==json.loads((HERE/'source-hashes.json').read_text())
 with tempfile.TemporaryDirectory(prefix='concrete-attribution-') as td:
  t=Path(td);paths=[]
  for n,b in source.items():
   f=t/n;f.parent.mkdir(parents=True,exist_ok=True);f.write_bytes(b);paths.append(str(f))
  fixture=t/'com/hbm/blocks/ModBlocks.java';fixture.parent.mkdir(parents=True,exist_ok=True);fixture.write_text('package com.hbm.blocks;public class ModBlocks {public static net.minecraft.block.Block barbed_wire;}');paths.append(str(fixture))
  tools=str(a.java_home/'lib/tools.jar')
  subprocess.run([str(a.java_home/'bin/javac'),'-cp',tools,'-d',td,str(HERE/'PaletteOwner.java')],check=True)
  result=subprocess.run([str(a.java_home/'bin/java'),'-Xmx1000m','-cp',td+':'+tools,'PaletteOwner',cp]+paths,capture_output=True,text=True,check=True)
  rows=[json.loads(l) for l in result.stdout.splitlines()];selected=[];edits=collections.defaultdict(list)
  for r in rows:
   r['file']=str(Path(r['file']).relative_to(t))
   if not (r['world'] and r['vanilla'] and r['ints'] and r['statement']):continue
   if len(r['args'])!=6 or r['args'][4]!='0' or r['args'][5]!='3':continue
   raw=source[r['file']].decode()
   def off(n): return len(raw.encode('utf-16-le')[:n*2].decode('utf-16-le'))
   lo,hi=off(r['start']),off(r['end']);assert raw[hi]==';'
   r.update(start=lo,end=hi+1,old=raw[lo:hi+1],new=replacement(r));selected.append(r);edits[r['file']].append(r)
  selected.sort(key=lambda r:(r['file'],r['start']))
  manifest={'baseline':BASE,'placements':len(selected),'files':{}}
  for n,es in sorted(edits.items()):
   out=source[n].decode()
   for e in sorted(es,key=lambda e:e['start'],reverse=True):out=out[:e['start']]+e['new']+out[e['end']:]
   manifest['files'][n]={'before':h(source[n]),'after':h(out.encode()),'placements':len(es)}
   if a.apply:(a.module/n).write_bytes(out.encode())
   else: assert (a.module/n).read_bytes()==out.encode(),'Production drift '+n
  if a.apply:
   with zipfile.ZipFile(HERE/'sources.zip','w',zipfile.ZIP_DEFLATED) as z:
    for n in sorted(source):z.writestr(n,source[n])
   (HERE/'selection.json').write_text(json.dumps(selected,indent=2)+'\n');(HERE/'manifest.json').write_text(json.dumps(manifest,indent=2)+'\n');(HERE/'source-hashes.json').write_text(json.dumps({n:h(b) for n,b in sorted(source.items())},indent=2)+'\n');(HERE/'attribution.log').write_text(result.stderr)
  else:assert selected==json.loads((HERE/'selection.json').read_text());assert manifest==json.loads((HERE/'manifest.json').read_text())
  print('PASS',len(selected),'audited structure-palette native placements across',len(edits),'files; metadata0 flags3 only.')
if __name__=='__main__':main()
