#!/usr/bin/env python3
"""Offline bounded tests. Neither scoped projections nor partial attribution are a whole-mod build."""
import argparse,base64,hashlib,importlib.util,io,json,os,pathlib,subprocess,tempfile,zipfile
p=argparse.ArgumentParser();p.add_argument('--java-home',type=pathlib.Path,required=True);p.add_argument('--classpath-file',type=pathlib.Path,required=True);p.add_argument('--original-jar',type=pathlib.Path,required=True);p.add_argument('--forge1614',type=pathlib.Path,required=True);p.add_argument('--verify-attribution',action='store_true');a=p.parse_args()
H=pathlib.Path(__file__).resolve().parent;ROOT=H.parents[2]
spec=importlib.util.spec_from_file_location('source_guard',H/'check_source.py');g=importlib.util.module_from_spec(spec);spec.loader.exec_module(g);g.verify()
cp=a.classpath_file.read_text().strip();java=a.java_home/'bin/java';javac=a.java_home/'bin/javac';tools=a.java_home/'lib/tools.jar'
def call(command,**kw):
 r=subprocess.run(list(map(str,command)),text=True,stdout=subprocess.PIPE,stderr=subprocess.PIPE,**kw)
 if r.returncode: raise RuntimeError('Command failed: '+' '.join(map(str,command))+'\n'+r.stdout+r.stderr)
 return r
hash=lambda p:hashlib.sha256(pathlib.Path(p).read_bytes()).hexdigest()
expected=json.loads((H/'artifact-hashes.json').read_text());actual={str(p):hash(p) for p in [pathlib.Path(x) for x in cp.split(':') if pathlib.Path(x).is_file()]+[a.original_jar,a.forge1614]};assert actual==expected,'artifact drift'
for rel,digest in json.loads((H/'support-source-hashes.json').read_text()).items():assert hash(ROOT/rel)==digest,rel
with zipfile.ZipFile(a.forge1614) as userdev:
 with zipfile.ZipFile(io.BytesIO(userdev.read('forgepatches.zip'))) as patches:
  original_patch=patches.read('net/minecraft/block/Block.java.patch').decode()
  excerpt=(H/'legacy-base-patch.txt').read_text().split('\n',1)[1].rstrip('\n')
  assert excerpt in original_patch

with tempfile.TemporaryDirectory(prefix='native-replaceability-') as temporary:
 d=pathlib.Path(temporary);classes=d/'classes';classes.mkdir();empty=d/'empty';empty.mkdir();generated=d/'generated';generated.mkdir()
 projections=['import net.minecraft.block.Block;import net.minecraft.block.material.Material;import net.minecraft.world.IBlockAccess; public final class OwnerProjections {']
 ownerlist=[]
 for owner in g.data['owners']:
  name=owner['owner'].split('.')[-1];old=owner['old_method'];method=old.replace('IBlockAccess world, int x, int y, int z','IBlockAccess world, net.minecraft.util.math.BlockPos pos')
  if '@Override' not in method:method='@Override\n'+method
  assert method.replace('@Override\n','').strip() in (ROOT/owner['file']).read_text(),name
  material='ROCK' if owner['expected'] else 'AIR'
  projections.append('public static class '+name+' extends Block { public '+name+'(){super(Material.'+material+');}\n'+method+'\n}\npublic static class Descendant_'+name+' extends '+name+' {}')
  ownerlist.append(owner['owner']+'\t'+str(owner['expected']).lower())
 projections.append('}')
 (generated/'OwnerProjections.java').write_text('\n'.join(projections));(d/'owners.tsv').write_text('\n'.join(ownerlist)+'\n')
 fixtures={
  'com/hbm/blocks/ModBlocks.java':'package com.hbm.blocks; public final class ModBlocks {public static net.minecraft.block.Block foam_layer,leaves_layer,oil_spill;}',
  'com/hbm/blocks/machine/ZirnoxDestroyed.java':'package com.hbm.blocks.machine; public class ZirnoxDestroyed extends net.minecraft.block.Block {public ZirnoxDestroyed(){super(net.minecraft.block.material.Material.IRON);}}',
  'com/hbm/blocks/machine/rbmk/RBMKDebris.java':'package com.hbm.blocks.machine.rbmk; public class RBMKDebris extends net.minecraft.block.Block {public RBMKDebris(){super(net.minecraft.block.material.Material.IRON);}}'}
 for rel,source in fixtures.items():f=generated/rel;f.parent.mkdir(parents=True,exist_ok=True);f.write_text(source)
 production=[ROOT/'src/main/java'/s for s in ['com/hbm/world/WorldBlockState.java','com/hbm/world/WorldAirRemoval.java','com/hbm/lib/ForgeDirection.java','com/hbm/blocks/generic/BlockLayering.java']]
 result=call([javac,'-source','8','-target','8','-proc:none','-implicit:none','-sourcepath',empty,'-cp',cp+':'+str(tools),'-d',classes]+list(generated.rglob('*.java'))+list(H.glob('*.java'))+production)
 print('PASS scoped javac: genuine BlockLayering and three production helpers; six exact method projections; three explicitly test-only NTM registry/type fixtures')
 print(call([java,'-cp',str(classes)+':'+cp,'ReplaceabilityNativeTest',a.original_jar,d/'owners.tsv'],cwd=d).stdout.strip())
 scan=call([java,'-cp',str(classes)+':'+str(tools),'QuerySourceScan',ROOT/'src/main/java']).stdout
 rows=[r.split('\t') for r in scan.splitlines()];methods=[r for r in rows if r[0]=='METHOD'];calls=[r for r in rows if r[0]=='CALL'];assert len(methods)==7
 assert all(r[3] in ['IBlockAccess,net.minecraft.util.math.BlockPos','IBlockAccess,BlockPos'] for r in methods)
 assert sorted((r[1],int(r[2])) for r in calls)==sorted((r['file'].replace('src/main/java/',''),r['line']) for r in g.data['excluded'])
 classes_source={r[2]:r[3] for r in rows if r[0]=='CLASS'}
 (d/'original-cp.txt').write_text(str(a.original_jar)+'\n')
 old=call([java,'-cp',str(classes)+':'+cp,'QueryBytecodeScan',d/'original-cp.txt']).stdout
 oldrows=[r.split('\t') for r in old.splitlines()]
 assert '\n'.join('\t'.join(r) for r in oldrows if r[0]!='CLASS')+'\n'==(H/'original-methods.tsv').read_text()
 oldclasses={r[1].replace('/','.'):r[2].replace('/','.') for r in oldrows if r[0]=='CLASS'}
 def closure(mapping):
  result={};owners={o['owner'] for o in g.data['owners']}
  for name in sorted(mapping):
   n=name;seen=set()
   while n and n not in seen:
    seen.add(n)
    if n in owners:result[name]=n;break
    n=mapping.get(n,'')
  return result
 assert closure(classes_source)==closure(oldclasses)==json.loads((H/'owner-descendants.json').read_text())
 native=call([java,'-cp',str(classes)+':'+cp,'QueryBytecodeScan',a.classpath_file]).stdout
 assert native==(H/'native-methods.tsv').read_text()
 print('PASS full-source syntax and original-JAR owner closure: seven override owners, 16 migrated owner/descendant types, two explicitly excluded OilWell calls; exact native dependency method inventory')
 if a.verify_attribution:
  originals=d/'originals';paths=list(g.data['files'])
  support=['src/main/java/com/hbm/tileentity/machine/oil/TileEntityOilDrillBase.java','src/main/java/com/hbm/tileentity/TileEntityMachineBase.java','src/main/java/com/hbm/tileentity/TileEntityLoadedBase.java'];paths+=support
  for rel in paths:
   raw=(ROOT/rel).read_bytes()
   if rel in g.data['files']:raw=g.reverse(raw,g.data['files'][rel])
   path=originals/rel;path.parent.mkdir(parents=True,exist_ok=True);path.write_bytes(raw)
  (d/'files.txt').write_text('\n'.join(paths)+'\n')
  # Compile helpers alone so the test-only ModBlocks fixture never contaminates attribution.
  attribution=d/'attribution';attribution.mkdir()
  call([javac,'-proc:none','-sourcepath',empty,'-cp',cp,'-d',attribution,ROOT/'src/main/java/com/hbm/world/WorldBlockState.java',ROOT/'src/main/java/com/hbm/lib/ForgeDirection.java'])
  for mode,root in [('before',originals),('after',ROOT)]:
   result=call([java,'-cp',str(classes)+':'+cp+':'+str(tools),'QueryAttribution',root,d/'files.txt',str(attribution)+':'+cp+':'+str(a.original_jar),empty])
   assert result.stdout==(H/('attributed-'+mode+'.tsv')).read_text(),mode
   # Unrelated diagnostics can differ under later integrated inputs; they are never treated as successful compilation.
   indexed={(r[0],int(r[1])):r for r in (line.split('\t') for line in result.stdout.splitlines())}
   for c in g.data['callers']:
    row=indexed[(c['file'],c['line'])];assert row[2]==('4' if mode=='before' else '2') and row[3]=='net.minecraft.block.Block',row
   print(result.stderr.strip())
  print('PASS all 30 exact Block receiver attributions, before and after; original artifact is only an ancestry/type dependency, not native runtime code')
