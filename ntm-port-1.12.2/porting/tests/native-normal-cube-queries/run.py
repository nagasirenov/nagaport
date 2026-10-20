#!/usr/bin/env python3
"""Offline bounded query proof, not compilation of the complete caller/PowerDetector owners."""
import argparse,collections,hashlib,importlib.util,json,pathlib,re,subprocess,tempfile,zipfile
p=argparse.ArgumentParser();p.add_argument('--java-home',type=pathlib.Path,required=True);p.add_argument('--classpath-file',type=pathlib.Path,required=True);p.add_argument('--original-jar',type=pathlib.Path,required=True);p.add_argument('--legacy-client',type=pathlib.Path,required=True);p.add_argument('--forge1614',type=pathlib.Path,required=True);p.add_argument('--verify-attribution',action='store_true');a=p.parse_args()
H=pathlib.Path(__file__).resolve().parent;ROOT=H.parents[2]
spec=importlib.util.spec_from_file_location('cube_source_guard',H/'check_source.py');g=importlib.util.module_from_spec(spec);spec.loader.exec_module(g);g.verify()
cp=a.classpath_file.read_text().strip();java=a.java_home/'bin/java';javac=a.java_home/'bin/javac';tools=a.java_home/'lib/tools.jar'
def call(command,**kw):
 r=subprocess.run(list(map(str,command)),text=True,stdout=subprocess.PIPE,stderr=subprocess.PIPE,**kw)
 if r.returncode:raise RuntimeError('Command failed: '+' '.join(map(str,command))+'\n'+r.stdout+r.stderr)
 return r
sha=lambda p:hashlib.sha256(pathlib.Path(p).read_bytes()).hexdigest()
inert=json.loads((H/'inert-zip-entries.json').read_text())
for archive,entries in inert.items():
 with zipfile.ZipFile(archive) as z:
  assert z.namelist()==['dummyThing'] and {n:hashlib.sha256(z.read(n)).hexdigest() for n in z.namelist()}==entries,'inert ZIP content drift'
expected=json.loads((H/'artifact-hashes.json').read_text());actual={str(p):sha(p) for p in [pathlib.Path(x) for x in cp.split(':') if pathlib.Path(x).is_file() and x not in inert]+[a.original_jar,a.legacy_client,a.forge1614]};assert actual==expected,'artifact drift'
for rel,digest in json.loads((H/'support-source-hashes.json').read_text()).items():assert sha(ROOT/rel)==digest,rel
with tempfile.TemporaryDirectory(prefix='native-normal-cube-') as temporary:
 d=pathlib.Path(temporary);classes=d/'classes';classes.mkdir();old=d/'old';old.mkdir();empty=d/'empty';empty.mkdir()
 production=[ROOT/'src/main/java'/s for s in ['com/hbm/blocks/generic/BlockNTMGlass.java','com/hbm/blocks/machine/BlockBeamBase.java','com/hbm/world/WorldBlockState.java','com/hbm/lib/ForgeDirection.java']]
 source=(ROOT/'src/main/java/com/hbm/blocks/machine/PowerDetector.java').read_text()
 method=re.search(r'public boolean canProvidePower\([^)]*\)\s*\{\s*return true;\s*}',source)[0]
 ctor=re.search(r'public PowerDetector\(Material mat\)\s*\{\s*super\(mat\);\s*}',source)[0].replace('PowerDetector(','PowerDetectorCapability(')
 projection=d/'PowerDetectorCapability.java';projection.write_text('import net.minecraft.block.BlockContainer;import net.minecraft.block.material.Material;import net.minecraft.tileentity.TileEntity;import net.minecraft.world.World; public class PowerDetectorCapability extends BlockContainer { '+ctor+' public TileEntity createNewTileEntity(World w,int m){throw new AssertionError("unused tile factory");} '+method+' }')
 sources=[f for f in H.glob('*.java') if f.name!='OriginalCube.java']
 call([javac,'-source','8','-target','8','-proc:none','-implicit:none','-sourcepath',empty,'-cp',cp+':'+str(tools),'-d',classes]+sources+production+[projection])
 call([javac,'-source','8','-target','8','-proc:none','-sourcepath',empty,'-cp',str(a.legacy_client),'-d',old,H/'OriginalCube.java'])
 print('PASS scoped javac: full actual BlockNTMGlass/BlockBeamBase and two production helpers; exact PowerDetector constructor/capability projection with unexercised tile-factory fixture; original callback matrix on actual engine classes')
 print(call([java,'-cp',str(classes)+':'+cp,'NormalCubeNativeTest',a.original_jar,a.legacy_client,old,H/'material-mapping.tsv'],cwd=d).stdout.strip())
 scan=call([java,'-cp',str(classes)+':'+str(tools),'NormalCubeSourceScan',ROOT/'src/main/java']).stdout
 rows=[r.split('\t') for r in scan.splitlines()];methods=[r for r in rows if r[0]=='METHOD'];assert not any(r[4]=='isNormalCube' for r in methods)
 powers=[r for r in methods if r[4]=='canProvidePower'];assert len(powers)==5 and all(r[3]=='net.minecraft.block.state.IBlockState' for r in powers)
 closure={'methods':[r[1:5]+[r[-1]] for r in rows if r[0]=='METHOD'],'classes':[r[1:] for r in rows if r[0]=='CLASS' and r[2].startswith('com.hbm.blocks.')]}
 assert closure==json.loads((H/'current-callback-closure.json').read_text()),'current secondary dispatch closure drift'
 print('PASS exact current secondary callback bodies and block inheritance closure; no NTM normal-cube override and all five power owners native')
 originals=d/'originals';paths=list(g.data['files'])
 for rel in paths:
  raw=g.reverse((ROOT/rel).read_bytes(),g.data['files'][rel]);target=originals/rel;target.parent.mkdir(parents=True,exist_ok=True);target.write_bytes(raw)
 if a.verify_attribution:
  (d/'files.txt').write_text('\n'.join(paths)+'\n')
  for mode,root in [('before',originals),('after',ROOT)]:
   result=call([java,'-Xmx2g','-cp',str(classes)+':'+cp+':'+str(tools),'NormalCubeAttribution',root,d/'files.txt',str(classes)+':'+cp+':'+str(a.original_jar),empty])
   assert result.stdout==(H/('attributed-'+mode+'.tsv')).read_text(),mode
   attributed=[r.split('\t') for r in result.stdout.splitlines()];query=[r for r in attributed if r[0]!='OWNER'];assert len(query)==47
   assert collections.Counter((r[0],int(r[1])) for r in query)==collections.Counter((r['file'],r[mode+'_line']) for r in g.data['callers'])
   for r in query:
    if mode=='before':assert r[2:4]==['0','net.minecraft.block.Block'],r
    elif r[2]=='0':assert r[3]=='net.minecraft.block.state.IBlockState' and r[5]=='isNormalCube()',r
    else:assert r[2:]==['1','net.minecraft.block.Block','net.minecraft.block.state.IBlockState','isNormalCube(net.minecraft.block.state.IBlockState)'],r
   owners=[r for r in attributed if r[0]=='OWNER'];assert len(owners)==1 and owners[0][-1]==str(mode=='after').lower()
   print(result.stderr.strip())
  print('PASS 47 exact before/after receiver/native method attributions and genuine PowerDetector override; unrelated partial diagnostics are not a successful project build')

print(call(['python3',H/'engine/scan.py']).stdout.strip())
print('PASS strict historical/current implementation closure and actual dependency bytecode regeneration; inert dummy ZIPs are content-pinned, all genuine dependencies byte-pinned')
