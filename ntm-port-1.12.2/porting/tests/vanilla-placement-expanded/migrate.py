#!/usr/bin/env python3
"""Attributed vanilla-only placement slice. Default verifies pinned CP52 edits."""
import argparse,collections,hashlib,json,re,subprocess,tempfile,zipfile
from pathlib import Path
HERE=Path(__file__).resolve().parent
BASE='e0d436953a26ba3e7cb58effa31adc48282eb501'
def h(b):return hashlib.sha256(b).hexdigest()
def replacement(r):
 a=r['args'];m=a[4]if len(a)==6 else '0';f=a[5]if len(a)==6 else '3'
 return ('{ net.minecraft.world.World nativePlacementWorld = '+r['receiver']+'; int nativePlacementX = '+a[0]+'; int nativePlacementY = '+a[1]+'; int nativePlacementZ = '+a[2]+'; net.minecraft.block.Block nativePlacementBlock = '+a[3]+'; int nativePlacementMeta = '+m+'; int nativePlacementFlags = '+f+'; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }')
def main():
 p=argparse.ArgumentParser();p.add_argument('--module',type=Path,default=HERE.parents[2]);p.add_argument('--java-home',type=Path,required=True);p.add_argument('--classpath',required=True);p.add_argument('--legacy-client',required=True);p.add_argument('--apply',action='store_true');a=p.parse_args()
 with tempfile.TemporaryDirectory(prefix='expanded-placements-')as td:
  t=Path(td);javac=str(a.java_home/'bin/javac');java=str(a.java_home/'bin/java');tools=str(a.java_home/'lib/tools.jar');old=HERE.parent/'dungeon-world-native'
  subprocess.run([javac,'-cp',tools+':'+a.classpath,'-d',td,str(HERE/'PlacementOwner.java'),str(old/'StateMappingProbe.java')],check=True)
  if a.apply:
   paths=[p for p in (a.module/'src/main/java').rglob('*.java')if '.setBlock('in p.read_text() and 'Blocks.'in p.read_text()]
   source={str(p.relative_to(a.module)):p.read_bytes() for p in paths}
   for n,b in source.items():assert b==subprocess.check_output(['git','show',BASE+':ntm-port-1.12.2/'+n],cwd=a.module),'Apply requires exact pinned baseline: '+n
  else:
   with zipfile.ZipFile(HERE/'sources.zip')as z:source={n:z.read(n)for n in z.namelist()}
  if not a.apply:assert {n:h(b)for n,b in sorted(source.items())}==json.loads((HERE/'source-hashes.json').read_text())
  paths=[]
  for n,s in source.items():p=t/n;p.parent.mkdir(parents=True,exist_ok=True);p.write_bytes(s);paths.append(str(p))
  r=subprocess.run([java,'-Xmx900m','-cp',td+':'+tools,'PlacementOwner',a.classpath]+paths,capture_output=True,text=True,check=True)
  attribution_log=r.stderr;rows=[json.loads(l)for l in r.stdout.splitlines()]
  for row in rows:row['file']=str(Path(row['file']).relative_to(t))
  rows.sort(key=lambda r:(r['file'],r['start']))
  candidates=[r for r in rows if r['world']and r['vanilla']and r['ints']and r['statement']and(len(r['args'])==4 or all(v.isdigit()for v in r['args'][4:]))]
  pairs=sorted(set((r['block'],int(r['args'][4])if len(r['args'])==6 else 0)for r in candidates));inp=t/'mapping.tsv';inp.write_text(''.join('%s\t%s\t%d\n'%(n.lower(),n,m)for n,m in pairs))
  mapping=subprocess.check_output([java,'-cp',td+':'+a.classpath,'StateMappingProbe',a.legacy_client,str(inp)],text=True)
  good={}
  for l in mapping.splitlines():
   if l.startswith('MAP\t'):
    _,oldname,n,m,oldid,newid,back,state=l.split('\t');assert oldid==newid
    if m==back:good[(n,int(m))]=int(oldid)
  selected=[];edits=collections.defaultdict(list);excluded=[]
  for r in candidates:
   pair=(r['block'],int(r['args'][4])if len(r['args'])==6 else 0)
   if pair not in good:excluded.append(dict(file=r['file'],start=r['start'],reason='invalid or non-round-tripping metadata',block=r['block'],args=r['args']));continue
   raw=source[r['file']].decode();assert 'nativePlacement'not in raw
   # javac offsets are UTF16. Convert exact positions before touching UTF8 bytes.
   def offset(n):return len(raw.encode('utf-16-le')[:n*2].decode('utf-16-le'))
   lo,hi=offset(r['start']),offset(r['end']);assert raw[hi]==';';r.update(start=lo,end=hi+1,old=raw[lo:hi+1],new=replacement(r),block_id=good[pair]);selected.append(r);edits[r['file']].append(r)
  manifest={'baseline':BASE,'placements':len(selected),'files':{},'invalid_metadata':excluded,'dungeon_deferred':{'public_virtual_random_brick':414,'public_virtual_random_shelf':34,'custom_random_concrete':2512,'other_custom_dynamic':4324}}
  for n,es in edits.items():
   raw=source[n].decode();out=raw
   for e in sorted(es,key=lambda e:e['start'],reverse=True):out=out[:e['start']]+e['new']+out[e['end']:]
   manifest['files'][n]={'before':h(source[n]),'after':h(out.encode()),'placements':len(es)}
   if a.apply:(a.module/n).write_bytes(out.encode())
   else:assert (a.module/n).read_bytes()==out.encode(),'Production drift: '+n
  if a.apply:
   with zipfile.ZipFile(HERE/'sources.zip','w',zipfile.ZIP_DEFLATED)as z:
    for n in sorted(source):z.writestr(n,source[n])
   (HERE/'selection.json').write_text(json.dumps(selected,indent=2)+'\n');(HERE/'manifest.json').write_text(json.dumps(manifest,indent=2)+'\n');(HERE/'mapping.tsv').write_text('\n'.join(l for l in mapping.splitlines()if l.startswith(('MAP\t','REJECT\t')))+'\n');(HERE/'attribution.log').write_text(attribution_log);(HERE/'source-hashes.json').write_text(json.dumps({n:h(b)for n,b in sorted(source.items())},indent=2)+'\n')
  else:assert manifest==json.loads((HERE/'manifest.json').read_text());assert selected==json.loads((HERE/'selection.json').read_text())
  print('PASS',len(selected),'attributed native placements across',len(edits),'files; exact bounded source edits, native registry/meta proof. Unresolved surrounding classes are NOT compiled.')
if __name__=='__main__':main()
