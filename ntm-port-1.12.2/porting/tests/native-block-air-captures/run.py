#!/usr/bin/env python3
"""Offline bounded air-capture proof. No source changes or whole-project compilation."""
import argparse,hashlib,importlib.util,io,json,pathlib,subprocess,tempfile,zipfile
p=argparse.ArgumentParser();p.add_argument('--java-home',type=pathlib.Path,required=True);p.add_argument('--classpath-file',type=pathlib.Path,required=True);p.add_argument('--original-jar',type=pathlib.Path,required=True);p.add_argument('--legacy-client',type=pathlib.Path,required=True);p.add_argument('--forge1614',type=pathlib.Path,required=True);p.add_argument('--verify-attribution',action='store_true');a=p.parse_args()
H=pathlib.Path(__file__).resolve().parent;ROOT=H.parents[2]
spec=importlib.util.spec_from_file_location('source_guard',H/'check_source.py');g=importlib.util.module_from_spec(spec);spec.loader.exec_module(g);g.verify()
cp=a.classpath_file.read_text().strip();java=a.java_home/'bin/java';javac=a.java_home/'bin/javac';tools=a.java_home/'lib/tools.jar'
def call(command,**kw):
 r=subprocess.run(list(map(str,command)),text=True,stdout=subprocess.PIPE,stderr=subprocess.PIPE,**kw)
 if r.returncode:raise RuntimeError('Command failed: '+' '.join(map(str,command))+'\n'+r.stdout+r.stderr)
 return r
hash=lambda p:hashlib.sha256(pathlib.Path(p).read_bytes()).hexdigest()
expected=json.loads((H/'artifact-hashes.json').read_text());actual={str(p):hash(p) for p in [pathlib.Path(x) for x in cp.split(':') if pathlib.Path(x).is_file()]+[a.original_jar,a.legacy_client,a.forge1614]};assert actual==expected,'artifact drift'
for rel,digest in json.loads((H/'support-source-hashes.json').read_text()).items():assert hash(ROOT/rel)==digest,rel
with zipfile.ZipFile(a.forge1614) as userdev:
 with zipfile.ZipFile(io.BytesIO(userdev.read('forgepatches.zip'))) as patches:
  original_patch=patches.read('net/minecraft/block/Block.java.patch').decode();excerpt=(H/'legacy-base-patch.txt').read_text().split('\n',1)[1].rstrip('\n');assert excerpt in original_patch
  assert 'return func_149688_o() == Material.field_151579_a;' in excerpt
  assert 'return o() == awt.a;' in (H/'OriginalForgeAir.java').read_text()
with tempfile.TemporaryDirectory(prefix='native-air-captures-') as temporary:
 d=pathlib.Path(temporary);classes=d/'classes';classes.mkdir();old=d/'old';old.mkdir();empty=d/'empty';empty.mkdir()
 production=[ROOT/'src/main/java'/s for s in ['com/hbm/blocks/machine/BlockBeamBase.java','com/hbm/world/WorldBlockState.java','com/hbm/lib/ForgeDirection.java']]
 sources=[f for f in H.glob('*.java') if f.name!='OriginalForgeAir.java']
 call([javac,'-source','8','-target','8','-proc:none','-implicit:none','-sourcepath',empty,'-cp',cp+':'+str(tools),'-d',classes]+sources+production)
 call([javac,'-source','8','-target','8','-proc:none','-sourcepath',empty,'-cp',str(a.legacy_client)+':'+cp,'-d',old,H/'OriginalForgeAir.java'])
 print('PASS scoped javac: full genuine BlockBeamBase and two production helpers; original Forge body projection on actual old Block/Material; no NTM dependency stubs')
 print(call([java,'-cp',str(classes)+':'+cp,'AirCaptureNativeTest',a.original_jar,a.legacy_client,old,H/'material-mapping.tsv'],cwd=d).stdout.strip())
 scan=call([java,'-cp',str(classes)+':'+str(tools),'AirSourceScan',ROOT/'src/main/java']).stdout
 rows=[r.split('\t') for r in scan.splitlines()];methods=[r for r in rows if r[0]=='METHOD'];calls=[r for r in rows if r[0]=='CALL'];assert len(methods)==1 and methods[0][2]=='com.hbm.blocks.machine.BlockBeamBase' and methods[0][3]=='IBlockState,IBlockAccess,BlockPos';assert not any(r[0]=='MATERIAL' for r in rows)
 expected_calls=[]
 for c in g.data['excluded']:
  line=c['line'];entry=g.data['files'].get(c['file'])
  if entry:
   before=g.reverse((ROOT/c['file']).read_bytes(),entry)
   line+=sum(e['new'].count('\n')-e['old'].count('\n') for e in entry['edits'] if before[:e['before_offset']].count(b'\n')+1<c['line'])
  expected_calls.append((c['file'].replace('src/main/java/',''),line))
 assert sorted((r[1],int(r[2])) for r in calls)==sorted(expected_calls)
 classes_source={r[2]:r[3] for r in rows if r[0]=='CLASS'}
 (d/'original-cp.txt').write_text(str(a.original_jar)+'\n')
 original=call([java,'-cp',str(classes)+':'+cp,'AirBytecodeScan',d/'original-cp.txt']).stdout;oldrows=[r.split('\t') for r in original.splitlines()]
 assert '\n'.join('\t'.join(r) for r in oldrows if r[0]!='CLASS')+'\n'==(H/'original-methods.tsv').read_text()
 oldclasses={r[1].replace('/','.'):r[2].replace('/','.') for r in oldrows if r[0]=='CLASS'}
 def closure(mapping):
  result=[]
  for name in sorted(mapping):
   n=name;seen=set()
   while n and n not in seen:
    seen.add(n)
    if n=='com.hbm.blocks.machine.BlockBeamBase':result.append(name);break
    n=mapping.get(n,'')
  return result
 assert closure(classes_source)==closure(oldclasses)==json.loads((H/'owner-descendants.json').read_text())
 assert call([java,'-cp',str(classes)+':'+cp,'AirBytecodeScan',a.classpath_file]).stdout==(H/'native-methods.tsv').read_text()
 print('PASS full-source/native-dependency ownership inventory: one NTM air override, three genuine owner/descendant types, no source material getter overrides, exact native air/material getter inventory; 24 exclusions retained')
 if a.verify_attribution:
  originals=d/'originals';paths=list(g.data['files'])
  for rel in paths:
   raw=g.reverse((ROOT/rel).read_bytes(),g.data['files'][rel]);path=originals/rel;path.parent.mkdir(parents=True,exist_ok=True);path.write_bytes(raw)
  (d/'files.txt').write_text('\n'.join(paths)+'\n')
  for mode,root in [('before',originals),('after',ROOT)]:
   result=call([java,'-cp',str(classes)+':'+cp+':'+str(tools),'AirAttribution',root,d/'files.txt',str(classes)+':'+cp+':'+str(a.original_jar),empty])
   assert result.stdout==(H/('attributed-'+mode+'.tsv')).read_text(),mode
   indexed={(r[0],int(r[1])):r for r in (line.split('\t') for line in result.stdout.splitlines())}
   for c in g.data['callers']:
    row=indexed[(c['file'],c[mode+'_line'])];assert row[2]==('4' if mode=='before' else '3') and row[3]=='net.minecraft.block.Block',row
    if mode=='after':assert row[4]=='net.minecraft.block.state.IBlockState' and row[5]=='isAir(net.minecraft.block.state.IBlockState,net.minecraft.world.IBlockAccess,net.minecraft.util.math.BlockPos)',row
   print(result.stderr.strip())
  print('PASS all 17 genuine Block receiver attributions before/after and exact native state/method attribution after; partial source errors are not a successful project build')
