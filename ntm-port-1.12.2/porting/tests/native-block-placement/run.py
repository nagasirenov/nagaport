#!/usr/bin/env python3
"""Bounded offline placement proof. No Gradle/project build and no owner stubs in production."""
import argparse,base64,collections,hashlib,importlib.util,io,json,pathlib,subprocess,tarfile,tempfile
P=argparse.ArgumentParser();P.add_argument('--verify-attribution',action='store_true');a=P.parse_args()
H=pathlib.Path(__file__).resolve().parent;ROOT=H.parents[2];REPO=ROOT.parent
JAVA=pathlib.Path('/workspace/shared/nagaport-toolchain/java8');CPF=pathlib.Path('/workspace/shared/nagaport-toolchain/native-classpath.txt');cp=CPF.read_text().strip();tools=JAVA/'lib/tools.jar'
OLD=pathlib.Path('/tmp/nagaport-legacy-client.jar')
spec=importlib.util.spec_from_file_location('placement_guard',H/'check_source.py');g=importlib.util.module_from_spec(spec);spec.loader.exec_module(g);g.verify()
def digest(raw):return hashlib.sha256(raw).hexdigest()
def call(args,**kw):
 r=subprocess.run(list(map(str,args)),stdout=subprocess.PIPE,stderr=subprocess.PIPE,text=True,**kw)
 if r.returncode:raise RuntimeError('Command failed: '+' '.join(map(str,args))+'\n'+r.stdout+r.stderr)
 return r
print(call(['python3',H/'engine/scan.py']).stdout.strip(),flush=True)
for rel,sha in json.loads((H/'support-source-hashes.json').read_text()).items():assert digest((ROOT/rel).read_bytes())==sha,rel
closure=json.loads((H/'current-callback-closure.json').read_text())
with tempfile.TemporaryDirectory(prefix='ntm-placement-') as td:
 d=pathlib.Path(td);classes=d/'classes';classes.mkdir();empty=d/'empty';empty.mkdir();api=d/'api';api.mkdir()
 api_sources=json.loads((H/'api-source-hashes.json').read_text())
 for rel,sha in api_sources.items():assert digest((ROOT/rel).read_bytes())==sha,rel
 call([JAVA/'bin/javac','-source','8','-target','8','-proc:none','-implicit:none','-sourcepath',empty,'-cp',cp,'-d',api]+[ROOT/rel for rel in api_sources])
 assert {str(p.relative_to(api)):digest(p.read_bytes()) for p in api.rglob('*.class')}==json.loads((H/'compiled-api-hashes.json').read_text()),'compiled genuine API drift'
 # Exact current owner methods; constructors/identity registry/stay collaborators are named fixtures.
 chunks=['import net.minecraft.block.*;import net.minecraft.block.material.Material;import net.minecraft.world.*;import net.minecraft.init.Blocks;import com.hbm.lib.ForgeDirection; public final class PlacementOwners { public static final class ModBlocks { public static Block laythe_silt,orbital_station,stalagmite,stalactite; }']
 for owner in g.data['owners']:
  raw=(ROOT/owner['file']).read_text();method=owner['after_method'];assert raw.count(method)==1
  name=owner['owner'].split('.')[-1];parent='BlockVine' if name=='BlockVinylVine' else 'Block';ctor='super();' if parent=='BlockVine' else 'super(Material.ROCK);'
  extra=''
  if 'this.canBlockStay(' in method:extra='public boolean canBlockStay(World w,int x,int y,int z){return PlacementNativeTest.stay(this,w,x,y,z);}'
  if name in ('BlockFallout','MeltedFlesh'):
   hooks=[r for r in closure['hooks'] if r[1]==owner['owner'] and r[2]=='isOpaqueCube'];assert len(hooks)==1
   opaque=base64.b64decode(hooks[0][-1]).decode();assert raw.count(opaque)==1;extra+=opaque
  chunks.append('public static class '+name+' extends '+parent+' { public '+name+'(){'+ctor+'} '+extra+'\n'+method+'\n}')
 chunks+=['public static class BlockFenceGate extends BlockModDoor {}','public static class BlockOrbitalStationLauncher extends BlockOrbitalStation {}','}']
 bank=d/'PlacementOwners.java';bank.write_text('\n'.join(chunks))
 production=[ROOT/p for p in json.loads((H/'support-source-hashes.json').read_text())]
 call([JAVA/'bin/javac','-source','8','-target','8','-proc:none','-implicit:none','-sourcepath',empty,'-cp',cp+':'+str(tools),'-d',classes]+list(H.glob('*.java'))+production+[bank])
 print('PASS scoped native javac: full WorldBlockState, ForgeDirection, FacingCompat, BlockBeamBase, BlockHangingVine, PlantBlockState; exact 15 placement method projections and two inherited descendants, named fixture collaborators',flush=True)
 print(call([JAVA/'bin/java','-cp',str(classes)+':'+cp,'PlacementNativeTest',OLD],cwd=d).stdout.strip(),flush=True)
 scan=call([JAVA/'bin/java','-cp',str(classes)+':'+str(tools),'PlacementSourceScan',ROOT/'src/main/java']).stdout
 rows=[r.split('\t') for r in scan.splitlines()]
 current={'hooks':[r[1:] for r in rows if r[0]=='HOOK'],'block_parents':[r[1:] for r in rows if r[0]=='CLASS' and r[2].startswith('com.hbm.blocks.')],'placement':[r[1:4]+[r[6]] for r in rows if r[0]=='METHOD']}
 assert current==closure,'current callback or owner closure drift';assert not any(r[0]=='CALL' for r in rows),'legacy placement call'
 assert len(current['placement'])==22 and all('int,int,int' not in r[2] for r in current['placement'])
 originals=json.loads((H/'engine/owners.json').read_text())
 for key,name,arity in [('original_side_solid_owners','isSideSolid',4),('original_leaves_owners','isLeaves',3)]:
  expected={o['owner'].replace('/','.') for o in originals[key] if o['owner'].startswith('com/hbm/blocks/')}
  actual={r[1] for r in current['hooks'] if r[2]==name and len(r[3].split(','))==arity}
  assert expected<=actual,(name,expected-actual)
 print('PASS exact 22 native placement owners; zero legacy four-argument calls; current block inheritance and all leaves/opacity/replaceability/side/stay callback bodies pinned; all original NTM support/leaves hooks already native',flush=True)
 if a.verify_attribution:
  # Rebuild the exact frozen CP93 snapshot, never silently replace historical rows with later sources.
  packed=subprocess.check_output(['git','-C',str(REPO),'archive',g.data['baseline'],'ntm-port-1.12.2/src/main/java'])
  before=d/'before';after=d/'after';before.mkdir();after.mkdir()
  with tarfile.open(fileobj=io.BytesIO(packed)) as archive:
   for m in archive.getmembers():
    if not m.isfile():continue
    rel=pathlib.PurePosixPath(m.name).relative_to('ntm-port-1.12.2');assert '..' not in rel.parts
    raw=archive.extractfile(m).read()
    for root in [before,after]:target=root/rel;target.parent.mkdir(parents=True,exist_ok=True);target.write_bytes(raw)
  for rel,entry in g.data['files'].items():
   raw=(ROOT/rel).read_bytes();assert g.reverse(raw,entry)==(before/rel).read_bytes();(after/rel).write_bytes(raw)
  pin=json.loads((H/'attribution-source-hashes.json').read_text())
  for mode,root in [('before',before),('after',after)]:
   actual={str(p.relative_to(root)):digest(p.read_bytes()) for p in sorted((root/'src/main/java').rglob('*.java'))};assert len(actual)==4071 and actual==pin[mode],mode
   result=call([JAVA/'bin/java','-Xmx3g','-cp',str(classes)+':'+str(tools),'PlacementAttribution',root,cp+':'+str(api),empty])
   assert result.stdout==(H/('attributed-'+mode+'.tsv')).read_text(),mode+' attribution drift'
   assert result.stderr==(H/('attributed-'+mode+'.log')).read_text(),mode+' diagnostics drift: '+result.stderr
   records=[r.split('\t') for r in result.stdout.splitlines()];methods=[r for r in records if r[0]=='METHOD'];calls=[r for r in records if r[0]=='CALL'];assert len(methods)==22 and len(calls)==41
   assert sum(r[-1]=='true' for r in methods)==(7 if mode=='before' else 22)
   assert sum(r[3]=='4' for r in calls)==(31 if mode=='before' else 0)
   if mode=='after':assert all(r[3]=='2' and r[5]=='net.minecraft.world.World,net.minecraft.util.math.BlockPos' and r[-1]=='canPlaceBlockAt(net.minecraft.world.World,net.minecraft.util.math.BlockPos)' for r in calls)
   else:
    assert all(r[5]=='net.minecraft.world.World,int,int,int' for r in calls if r[3]=='4')
    assert collections.Counter((r[1],int(r[2])) for r in calls if r[3]=='4')==collections.Counter((r['file'],r['before_line']) for r in g.data['callers'])
   print(result.stderr.strip(),flush=True)
  print('PASS complete frozen CP93 before/after attribution: all 4071 input hashes, 22 overrides, 41 calls; 10164→10117 diagnostics. Analyze-only, not a successful whole-project build.',flush=True)
print('PASS bounded placement proof; inherited vanilla/Forge behavior is native platform behavior, not universal old/new world-read equivalence.',flush=True)
