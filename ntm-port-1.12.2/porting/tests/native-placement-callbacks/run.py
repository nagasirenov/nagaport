#!/usr/bin/env python3
"""Offline: run.py JAVA_HOME CLASSPATH_FILE LEGACY_CLIENT_JAR SRG_ZIP OUTPUT_DIRECTORY"""
from pathlib import Path
import subprocess,sys,json,csv,collections,tempfile,hashlib,zipfile,importlib.util
HERE=Path(__file__).resolve().parent;ROOT=HERE.parents[3];PROJECT=ROOT/'ntm-port-1.12.2'
java,cpf,legacy,srg,out=[Path(x).resolve() for x in sys.argv[1:]];out.mkdir(parents=True,exist_ok=True);cp=cpf.read_text().strip()
spec=importlib.util.spec_from_file_location('boundary',HERE/'verify.py');boundary=importlib.util.module_from_spec(spec);spec.loader.exec_module(boundary);boundary.verify()
manifest=boundary.MANIFEST;selected={f['owner']:f for f in manifest['files']}
def run(args):
 p=subprocess.run([str(x) for x in args],stdout=subprocess.PIPE,stderr=subprocess.STDOUT,text=True)
 if p.returncode:raise AssertionError(p.stdout)
 return p.stdout

def rows(tag,name):return list(csv.DictReader((out/tag/(name+'.tsv')).open(),delimiter='\t'))
classes=out/'compiler';classes.mkdir(exist_ok=True)
print(run([java/'bin/javac','-cp',java/'lib/tools.jar','-d',classes,HERE/'PlacementAttribution.java']))
# Unmodified files are linked, not copied; selected files recover exact sealed baseline bytes.
# Verify all source differences belong to the frozen production manifest before using links.
changed=run(['git','-C',ROOT,'diff',manifest['baseline'],'--name-only','--','ntm-port-1.12.2/src/main/java']).splitlines()
assert set(changed)==set(boundary.FILES)
with tempfile.TemporaryDirectory(prefix='placement-baseline-') as tmp:
 base=Path(tmp)
 for p in (PROJECT/'src/main/java').rglob('*.java'):
  relative=str(p.relative_to(ROOT));q=base/relative;q.parent.mkdir(parents=True,exist_ok=True)
  if relative in boundary.FILES:
   original=boundary.reverse_source(relative,p.read_bytes().decode());sealed=subprocess.check_output(['git','-C',str(ROOT),'show',manifest['baseline']+':'+relative]);assert original.encode()==sealed;q.write_bytes(sealed)
  else:q.symlink_to(p)
 for tag,tree in [('before',base/'ntm-port-1.12.2'),('after',PROJECT)]:
  log=run([java/'bin/java','-Xmx4g','-cp',str(classes)+':'+str(java/'lib/tools.jar'),'PlacementAttribution',tree,cpf,out/tag]);(out/(tag+'.log')).write_text(log);print(tag,log.strip())
before=rows('before','methods');after=rows('after','methods');bc={x['owner']:x for x in rows('before','classes')};ac={x['owner']:x for x in rows('after','classes')}
for owner,f in selected.items():
 b=[x for x in before if x['owner']==owner];a=[x for x in after if x['owner']==owner];assert len(a)==len(b)==1
 assert b[0]['native_override']=='false' and a[0]['native_override']=='true'
 assert bc[owner]['block_subtype']==ac[owner]['block_subtype']=='true'
 assert b[0]['parameters']=='net.minecraft.world.World || int || int || int || net.minecraft.entity.EntityLivingBase || net.minecraft.item.ItemStack'
 assert a[0]['parameters']=='net.minecraft.world.World || net.minecraft.util.math.BlockPos || net.minecraft.block.state.IBlockState || net.minecraft.entity.EntityLivingBase || net.minecraft.item.ItemStack'
# All declarations outside selected owners, including bridge/domain overloads, unchanged.
assert [(x['owner'],x['source']) for x in before if x['owner'] not in selected]==[(x['owner'],x['source']) for x in after if x['owner'] not in selected]
# Native conversion cannot silently strand a source-level explicit caller.
assert [(x['path'],x['source']) for x in rows('before','calls')]==[(x['path'],x['source']) for x in rows('after','calls')]
assert not any(x['method_owner'] in selected for x in rows('before','calls'))
def errs(tag):return collections.Counter((x[0],*x[2:]) for x in csv.reader((out/tag/'errors.tsv').open(),delimiter='\t'))
b,a=errs('before'),errs('after');assert not a-b;assert sum((b-a).values())==69;assert all(x[1]=='compiler.err.method.does.not.override.superclass' for x in b-a)
descendants=[]
for owner,c in bc.items():
 ancestors=[];current=c['superclass'].split('<')[0]
 while current in bc and current not in ancestors:ancestors.append(current);current=bc[current]['superclass'].split('<')[0]
 if any(x in selected for x in ancestors):descendants.append(dict(owner=owner,ancestry=ancestors,selected=owner in selected,callbacks=[x['signature'] for x in after if x['owner']==owner]))
summary=dict(baseline=manifest['baseline'],selected=72,excluded=manifest['exclusions'],native_overrides=72,removed_override_errors=69,added_errors=0,before_errors=sum(b.values()),after_errors=sum(a.values()),descendants=descendants,explicit_calls=rows('after','calls'))
(out/'summary.json').write_text(json.dumps(summary,indent=2)+'\n');print('PASS all selected native overrides, descendant audit, unchanged explicit calls, 69 removed errors and zero added errors')
source=(PROJECT/'src/main/java/com/hbm/blocks/machine/Floodlight.java').read_text();start=source.index('public void onBlockPlacedBy(');end=source.index('\n\t}',start)+3
projection=(HERE/'NativePlacementTest.java.in').read_text().replace('@CALLBACK@',source[start:end]);generated=out/'NativePlacementTest.java';generated.write_text(projection)
log=run([java/'bin/javac','-source','8','-target','8','-cp',cp,'-d',classes,generated]);(out/'runtime-compile.log').write_text(log)
log=run([java/'bin/java','-cp',str(classes)+':'+cp,'NativePlacementTest']);(out/'runtime.log').write_text(log);print(log.strip())
srgtext=zipfile.ZipFile(srg).read('joined.srg').decode();assert 'MD: aji/a (Lahb;IIILsv;Ladd;)V net/minecraft/block/Block/func_149689_a (Lnet/minecraft/world/World;IIILnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/item/ItemStack;)V' in srgtext
for name,cpath,cls in [('old-itemblock',str(legacy),'abh'),('native-itemblock',cp,'net.minecraft.item.ItemBlock')]:
 text=run([java/'bin/javap','-p','-c','-classpath',cpath,cls]);(out/(name+'.javap')).write_text(text)
old=(out/'old-itemblock.javap').read_text();native=(out/'native-itemblock.javap').read_text();assert 'aji.a:(Lahb;IIILsv;Ladd;)V' in old;assert 'Block.onBlockPlacedBy:(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/item/ItemStack;)V' in native
print('INPUT HASHES',json.dumps({p.name:hashlib.sha256(p.read_bytes()).hexdigest() for p in (legacy,srg)}));print('PASS bounded callback boundary only; remaining production body APIs and in-game parity NOT certified')
