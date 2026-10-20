#!/usr/bin/env python3
"""Bounded AST-owned native migration. --apply writes only the documented dungeon scope."""
import argparse,collections,gzip,hashlib,json,re,subprocess,tempfile,zipfile
from pathlib import Path
HERE=Path(__file__).resolve().parent
SCOPE='src/main/java/com/hbm/world/dungeon/'
def sha(s):return hashlib.sha256(s.encode()).hexdigest()
def coord(s,axis):return bool(re.fullmatch(axis+r'(?:\s*[+-]\s*-?\d+)?',s))
def field(name):return {'fence':'OAK_FENCE','wooden_door':'OAK_DOOR'}.get(name,name.upper())
def main():
 p=argparse.ArgumentParser();p.add_argument('--module',type=Path,required=True);p.add_argument('--java-home',type=Path,required=True);p.add_argument('--classpath',required=True);p.add_argument('--legacy-client',type=Path,required=True);p.add_argument('--apply',action='store_true');a=p.parse_args()
 with zipfile.ZipFile(HERE/'sources.zip')as z:
  hashes=json.loads((HERE/'source-hashes.json').read_text());assert set(z.namelist())==set(hashes)
  for n,h in hashes.items():assert hashlib.sha256(z.read(n)).hexdigest()==h,'Pinned source mismatch: '+n
  base={n:z.read(n).decode() for n in z.namelist() if n.startswith('baseline/')}
 with tempfile.TemporaryDirectory(prefix='dungeon-native-')as d:
  work=Path(d);files=[]
  for name,s in base.items():
   q=work/name;q.parent.mkdir(parents=True,exist_ok=True);q.write_text(s);files.append(str(q))
  java=str(a.java_home/'bin/java');javac=str(a.java_home/'bin/javac');tools=str(a.java_home/'lib/tools.jar')
  subprocess.run([javac,'-cp',tools+':'+a.classpath,'-sourcepath',d,'-d',d,str(HERE/'PlacementAst.java'),str(HERE/'StateMappingProbe.java')],check=True)
  rows=[json.loads(s) for s in subprocess.check_output([java,'-cp',d+':'+tools,'PlacementAst']+sorted(files),text=True).splitlines()]
  for r in rows:r['file']=Path(r['file']).relative_to(work/'baseline').as_posix()
  constants=[r for r in rows if r['kind']=='constant'];calls=[r for r in rows if r['kind']=='call'];pairs={(r['name'],0)for r in constants}
  for r in calls:
   v=[v['text']for v in r['args']]
   if r['name']=='setBlock' and len(v) in (4,6) and v[3].startswith('Blocks.') and (len(v)==4 or v[4].isdigit()):pairs.add((v[3][7:],int(v[4])if len(v)==6 else 0))
  inp=work/'map.tsv';inp.write_text(''.join('%s\t%s\t%d\n'%(name,field(name),meta)for name,meta in sorted(pairs)))
  mapping=subprocess.check_output([java,'-cp',d+':'+a.classpath,'StateMappingProbe',str(a.legacy_client),str(inp)],text=True)
  good={};ids={}
  for line in mapping.splitlines():
   if line.startswith('MAP\t'):
    _,name,native,meta,oldid,newid,back,state=line.split('\t');assert oldid==newid;ids[name]=int(oldid)
    if meta==back:good[(name,int(meta))]=state
  assert set(r['name']for r in constants)<=set(ids),'Unproven constant identity'
  edits=collections.defaultdict(list);excluded=[];selected=[];coordinate=[];covered=collections.defaultdict(list)
  for r in calls:
   v=[v['text']for v in r['args']];s=base['baseline/'+r['file']];reason=None
   if not r['owned'] or r['receiver']!='world':reason='unproven World parameter ownership'
   elif not all(t['pure']for t in r['args']):reason='effectful or unproven argument'
   elif r['name']=='setBlock':
    if len(v)not in(4,6):reason='unproven overload'
    elif not r['statement']:reason='return value used'
    elif not r['xyzParams']or not all(coord(v[i],'xyz'[i])for i in range(3)):reason='non-parameter coordinate expression'
    elif not v[3].startswith('Blocks.'):reason='custom or dynamic block state owner'
    elif len(v)==6 and (not v[4].isdigit()or not v[5].isdigit()):reason='nonliteral metadata or flags'
    elif (v[3][7:],int(v[4])if len(v)==6 else 0)not in good:reason='metadata invalid or non-round-tripping'
    else:
     assert s[r['end']]==';';assert not re.search(r'\bdungeonPos\b',s)
     name=v[3][7:];meta=v[4]if len(v)==6 else '0';flags=v[5]if len(v)==6 else '3';pos=', '.join(v[:3]);new='{ BlockPos dungeonPos = new BlockPos('+pos+'); if (world.isValid(dungeonPos)) world.setBlockState(dungeonPos, Blocks.'+field(name)+'.getStateFromMeta('+meta+'), '+flags+'); }'
     e={'start':r['start'],'end':r['end']+1,'new':new,'kind':'placement'};edits[r['file']].append(e);covered[r['file']].append((e['start'],e['end']));selected.append(dict(r,native=new,block_id=ids[name],meta=int(meta),flags=int(flags)));continue
   elif r['name'] in ('getBlock','getTileEntity','setBlockToAir') and len(v)==3:
    # Bounded coordinate-only native methods. No metadata mutation or owner conversion.
    if not r['xyzParams']or not all(coord(v[i],'xyz'[i])for i in range(3)):reason='non-parameter coordinate expression'
    else:
     method={'getBlock':'getBlockState','getTileEntity':'getTileEntity','setBlockToAir':'setBlockToAir'}[r['name']];new=r['receiver']+'.'+method+'(new BlockPos('+', '.join(v)+'))'+('.getBlock()'if r['name']=='getBlock'else '')
     if r['name']=='getBlock':
      if r['statement']:
       assert s[r['end']]==';' and not re.search(r'\bdungeonPos\b',s)
       new='{ BlockPos dungeonPos = new BlockPos('+', '.join(v)+'); if (world.isValid(dungeonPos)) world.getBlockState(dungeonPos).getBlock(); }'
      else:new='(world.isValid(new BlockPos('+', '.join(v)+')) ? '+new+' : Blocks.AIR)'
     # setBlockToAir has the same horizontal-boundary difference and is not a placement-state case.
     if r['name']=='setBlockToAir':reason='air-removal boundary requires separate audit'
     else:edits[r['file']].append({'start':r['start'],'end':r['end']+(1 if r['name']=='getBlock'and r['statement']else 0),'new':new,'kind':'coordinate'});coordinate.append(r);continue
   else:reason='method outside bounded rule'
   excluded.append({'file':r['file'],'line':r['line'],'name':r['name'],'reason':reason})
  for r in constants:
   if any(lo<=r['start']<hi for lo,hi in covered[r['file']]):continue
   edits[r['file']].append({'start':r['start'],'end':r['end'],'new':'Blocks.'+field(r['name']),'kind':'constant'})
  manifest={'baseline_commit':'077492a6c31c910f34697783f1aa0cfabd8ff88e','scope':SCOPE,'legacy_client_sha256':hashlib.sha256(a.legacy_client.read_bytes()).hexdigest(),'active_setBlock_calls':sum(r['name']=='setBlock'for r in calls),'placements':len(selected),'coordinate_calls':len(coordinate),'constant_references':len(constants),'exclusion_reasons':dict(collections.Counter(r['reason']for r in excluded)),'files':{}}
  parsed_outputs=[]
  for name,entries in sorted(edits.items()):
   s=base['baseline/'+name];haspos=any(e['kind']in('placement','coordinate')for e in entries)
   if haspos and 'import net.minecraft.util.math.BlockPos;'not in s:
    i=s.index('import net.minecraft.world.World;');entries.append({'start':i,'end':i,'new':'import net.minecraft.util.math.BlockPos;\n','kind':'import'})
   entries.sort(key=lambda e:e['start']);end=0;out='';inverse=[]
   for e in entries:
    assert e['start']>=end,(name,e);out+=s[end:e['start']];lo=len(out);out+=e['new'];inverse.append((lo,len(out),s[e['start']:e['end']]));end=e['end']
   out+=s[end:];rev=out
   for lo,hi,old in reversed(inverse):rev=rev[:lo]+old+rev[hi:]
   assert rev==s,'Exact inverse failure'
   parsed=work/'result'/name;parsed.parent.mkdir(parents=True,exist_ok=True);parsed.write_text(out);parsed_outputs.append(str(parsed))
   target=a.module/name
   if a.apply:target.write_text(out)
   else:assert target.read_text()==out,'Production drift or incorrect transformation: '+name
   manifest['files'][name]={'before_sha256':sha(s),'after_sha256':sha(out),'edits':dict(collections.Counter(e['kind']for e in entries))}
  subprocess.run([java,'-cp',d+':'+tools,'PlacementAst']+parsed_outputs,stdout=subprocess.DEVNULL,check=True)
  if a.apply:
   (HERE/'manifest.json').write_text(json.dumps(manifest,indent=2)+'\n');(HERE/'excluded.json').write_text(json.dumps(excluded,separators=(',',':'))+'\n');(HERE/'native-mapping.tsv').write_text('\n'.join(l for l in mapping.splitlines()if l.startswith(('MAP\t','REJECT\t')))+'\n')
  else:assert manifest==json.loads((HERE/'manifest.json').read_text()),'Manifest drift'
  # Persist ephemeral, source-bound stream inputs only for the independent runner.
  (HERE/'stream-selection.json.gz').write_bytes(gzip.compress((json.dumps(selected,separators=(',',':'))+'\n').encode(),mtime=0)) if a.apply else None
  print(json.dumps({k:v for k,v in manifest.items()if k!='files'},indent=2));print('PASS javac AST ownership, native registry ID/state round-trip proof, disjoint replacements, exact inverse, full production output equality')
if __name__=='__main__':main()
