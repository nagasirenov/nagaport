#!/usr/bin/env python3
"""Exact source guard, full graph attribution, and dual-platform complete-hook projections."""
import argparse, hashlib, io, json, re, subprocess, tempfile, zipfile
from pathlib import Path
H=Path(__file__).resolve().parent; ROOT=H.parents[2]; REPO=ROOT.parent
BASE='fc2cd2dba34caba0dba74ed288f95107ab4dc979'
J=Path('/workspace/shared/nagaport-toolchain/java8'); CPFILE=Path('/workspace/shared/nagaport-toolchain/native-classpath.txt'); CP=CPFILE.read_text().strip(); OLD=Path('/tmp/nagaport-legacy-client.jar'); PATCH=Path('/tmp/nagaport-forge1614-userdev.jar')
OWNERS=['tool/ItemModMinecart','block/ItemBlockNamedMeta','block/ItemBlockColoredConcrete','weapon/ItemAmmoArty','weapon/ItemAmmoHIMARS','special/ItemMineralOre','special/ItemAutogen','machine/ItemMetaUpgrade','machine/ItemGear','machine/ItemStampBook','food/ItemCrayon']
FILES=['src/main/java/com/hbm/items/'+n+'.java' for n in OWNERS]
def git(path,original=False):return subprocess.check_output(['git','-C',str(REPO),'show',BASE+':'+('ntm-original-pristine/' if original else 'ntm-port-1.12.2/')+path])
def extract(s,start):
 b=s.index(start);p=s.index('{',b);d=1;e=p+1
 while d:
  if s[e]=='{':d+=1
  elif s[e]=='}':d-=1
  e+=1
 return s[b:e]
def sha(b):return hashlib.sha256(b).hexdigest()
def transform(path,b):
 assert b.count(b'public String getUnlocalizedName(ItemStack stack)')==1
 b=b.replace(b'public String getUnlocalizedName(ItemStack stack)',b'public String getTranslationKey(ItemStack stack)')
 if path.endswith('/ItemAutogen.java'):b=b.replace(b'super.getUnlocalizedName(stack)',b'super.getTranslationKey(stack)')
 if path.endswith('/ItemBlockColoredConcrete.java'):
  b=b.replace(b'import net.minecraft.item.ItemDye;',b'import net.minecraft.item.EnumDyeColor;')
  b=b.replace(b'ItemDye.field_150923_a[BlockConcreteColored.func_150032_b(stack.getItemDamage())]',b'EnumDyeColor.byDyeDamage(BlockConcreteColored.func_150032_b(stack.getItemDamage())).getTranslationKey()')
 return b

def audit():
 rows=[]
 for p in FILES:
  b=git(p);a=(ROOT/p).read_bytes();assert a==transform(p,b),p
  original=extract(git(p,True).decode(),'public String getUnlocalizedName(ItemStack stack)')
  before=extract(b.decode(),'public String getUnlocalizedName(ItemStack stack)')
  assert original.replace('super.getUnlocalizedName()','super.getTranslationKey()').replace('this.getUnlocalizedName()','this.getTranslationKey()').replace('stack.stackTagCompound','stack.getTagCompound()')==before,p
  rows.append({'path':p,'before_sha256':sha(b),'after_sha256':sha(a),'pristine_hook_sha256':sha(original.encode())})
 unchanged=['src/main/java/com/hbm/'+s for s in ['items/special/ItemBookLore.java','items/machine/ItemScraps.java','items/machine/ItemStamp.java','items/machine/ItemMachineUpgrade.java','items/machine/ItemChemicalDye.java','items/block/ItemBlockMeta.java','items/block/ItemBlockBlastInfo.java','items/block/ItemBlockBase.java','blocks/IBlockMulti.java','blocks/generic/BlockConcreteColored.java','util/EnumUtil.java']]
 for p in unchanged:assert (ROOT/p).read_bytes()==git(p);rows.append({'path':p,'unchanged_sha256':sha(git(p))})
 z=zipfile.ZipFile(PATCH);p=zipfile.ZipFile(io.BytesIO(z.read('forgepatches.zip')))
 patchrows=[]
 for name in ['Item','ItemStack','ItemBlock','ItemDye']:
  b=p.read('net/minecraft/item/'+name+'.java.patch');patchrows.append({'name':name,'sha256':sha(b),'source':b.decode()})
 (H/'forge1614-patches.json').write_text(json.dumps(patchrows,indent=2)+'\n')
 native=next(Path(p) for p in CP.split(':') if 'forgeBin-' in p)
 manifest={'baseline':BASE,'scope':'11 native Item stack-key overrides; ItemBookLore excluded, one same-contract super call, one bounded 16-variant dye lookup; no dynamic ItemStack callers','production':rows,'binaries':[{'path':str(p),'sha256':sha(p.read_bytes())} for p in [OLD,native,PATCH]],'forge1614_patches':[{'name':r['name'],'sha256':r['sha256']} for r in patchrows]}
 (H/'manifest.json').write_text(json.dumps(manifest,indent=2)+'\n')
 print('PASS exact 11-file inverse, 11 pristine-hook equality checks, 11 unchanged collaborator/descendant files; original Forge patches retained',flush=True)

def attribute(out):
 subprocess.run([str(J/'bin/javac'),'-cp',str(J/'lib/tools.jar'),'-d',str(out),str(H/'HookScanner.java')],check=True)
 allfiles=sorted((ROOT/'src/main/java').rglob('*.java'))
 for side in ['before','after']:
  sources=[]
  for p in allfiles:
   rel=str(p.relative_to(ROOT))
   if side=='before' and rel in FILES:
    q=out/'before'/rel;q.parent.mkdir(parents=True,exist_ok=True);q.write_bytes(git(rel));sources.append(str(q))
   else:sources.append(str(p))
  listing=out/(side+'-sources.txt');listing.write_text('\n'.join(sources)+'\n')
  with (H/(side+'.tsv')).open('w') as o,(H/(side+'.log')).open('w') as e:subprocess.run([str(J/'bin/java'),'-Xmx1800m','-cp',str(out)+':'+str(J/'lib/tools.jar'),'HookScanner',str(listing),str(CPFILE)],stdout=o,stderr=e,check=True)
 before=(H/'before.tsv').read_text().splitlines();after=(H/'after.tsv').read_text().splitlines()
 bclasses=[s for s in before if s.startswith('CLASS\t')];aclasses=[s for s in after if s.startswith('CLASS\t')];assert bclasses==aclasses and len(aclasses)==12,aclasses
 defs=[s for s in after if s.startswith('DECL\ttrue\t')];assert len(defs)==11 and all('#getTranslationKey(net.minecraft.item.ItemStack):java.lang.String\ttrue\t' in s for s in defs),defs
 assert not any(s.startswith('CALL\t') and '#getUnlocalizedName(net.minecraft.item.ItemStack)' in s for s in after)
 print('PASS complete graph: 11 real Item overrides, 12-class descendant closure, unchanged separate block domain hooks; no selected legacy destination remains',flush=True)

def source(path,old):return (git(path,True).decode() if old else (ROOT/path).read_text())
def runtime(out):
 generated=[];projection=[]
 for old in [False,True]:
  prefix='Legacy' if old else 'Native'
  def maptext(s):
   # Exact production bodies; only symbols needed by the actual old platform are remapped.
   for n in ['ItemBlockNamedMeta','ItemBlockColoredConcrete','ItemBlockMeta','ItemBlockBlastInfo','ItemBlockBase','ItemMachineUpgrade','ItemMetaUpgrade','ItemStampBook','ItemStamp','ItemAutogen','ItemScraps','ItemModMinecart','ItemGear','ItemAmmoArty','ItemAmmoHIMARS','ItemMineralOre','ItemCrayon','ItemBookLore','BlockConcreteColored','EnumUtil','IBlockMulti']:s=re.sub(r'\b'+n+r'\b',prefix+n,s)
   if old:
    for a,b in [('ItemDye.field_150923_a','acj.a'),('getUnlocalizedName','a'),('getTranslationKey','a'),('getItemDamage','k'),('getItem','b'),('hasTagCompound','p'),('getTagCompound','q'),('getString','j'),('stackTagCompound','d')]:s=s.replace(a,b)
    for a,b in [('ItemStack','add'),('Item','adb'),('ItemBlock','abh'),('ItemFood','acx'),('Block','aji')]:s=re.sub(r'\b'+a+r'\b',b,s)
   return s
  imports='import java.util.*;\n'+('' if old else 'import net.minecraft.item.*; import net.minecraft.block.Block;\n')
  def emit(name,body):
   p=out/(prefix+name+'.java');p.write_text(imports+maptext(body));generated.append(p)
  def prod(rel):return source('src/main/java/com/hbm/'+rel,old)
  # Named projections deliberately contain only the transitive naming read surface.
  emit('EnumUtil',prod('util/EnumUtil.java').split('public class',1)[1].join(['public class','']))
  emit('BlockConcreteColored','class BlockConcreteColored {\n'+extract(prod('blocks/generic/BlockConcreteColored.java'),'public static int func_150032_b')+'\n'+extract(prod('blocks/generic/BlockConcreteColored.java'),'public static int func_150031_c')+'\n}')
  emit('IBlockMulti',prod('blocks/IBlockMulti.java').split('public interface',1)[1].join(['public interface','']))
  for n,parent in [('ItemBlockMeta','ItemBlock'),('ItemBlockBase','ItemBlock'),('ItemBlockBlastInfo','ItemBlockBase')]:
   members='public '+n+'(Block block) { super(block); }'
   # No stack-key method on this projection: selected ColoredConcrete calls only the inherited zero-argument native ItemBlock getter.
   emit(n,'class '+n+' extends '+parent+' {\n'+members+'\n}')
  emit('ItemMachineUpgrade','class ItemMachineUpgrade extends Item {}')
  st=prod('items/machine/ItemStamp.java');emit('ItemStamp','class ItemStamp extends Item { protected StampType type;\n'+extract(st,'public StampType getStampType')+'\n'+extract(st,'public static enum StampType')+'\n}')
  for owner,p in zip(OWNERS,FILES):
   s=source(p,old);name=owner.split('/')[-1];parent=re.search(r'public class '+name+r' extends (\w+)',s).group(1)
   members='@Override\n'+extract(s,'public String '+('getUnlocalizedName' if old else 'getTranslationKey')+'(ItemStack stack)')
   if name in ['ItemBlockNamedMeta','ItemBlockColoredConcrete']:members+='\npublic '+name+'(Block block) { super(block); }'
   elif name=='ItemModMinecart':members+='\n'+extract(s,'public static enum EnumCartBase')+'\n'+extract(s,'public static enum EnumMinecart')
   elif name=='ItemCrayon':
    members+='\n'+extract(prod('items/machine/ItemChemicalDye.java'),'public static enum EnumChemDye')
    members+='\npublic ItemCrayon() { super(3, false); }'
   elif name=='ItemAutogen':members+='\nprivate String overrideUnlocalizedName = null;\n'+extract(s,'public ItemAutogen oun')
   elif name=='ItemStampBook':members+='\n'+extract(s,'public StampType getStampType')
   elif name in ['ItemAmmoArty','ItemAmmoHIMARS','ItemMineralOre']:
    typ={'ItemAmmoArty':'ArtilleryShell','ItemAmmoHIMARS':'HIMARSRocket','ItemMineralOre':'Mineral'}[name]
    count=int(re.search(r'itemTypes\s*=\s*new '+typ+r'\[\s*/\* >>> \*/\s*(\d+)',s).group(1))
    constants=dict(re.findall(r'public (?:static )?final int (\w+) = (\d+);',s)); entries=re.findall(r'this.itemTypes\[(\w+)\] = new '+typ+r'\("([^"]*)"',s)
    assert len(entries)==count
    names=[None]*count
    for c,value in entries:names[int(constants[c])]=value
    assert all(n is not None for n in names)
    members+='\npublic static class '+typ+' { public String name; public '+typ+'(String name) { this.name=name; } }\npublic static '+typ+'[] itemTypes = new '+typ+'[] {'+','.join('new '+typ+'('+json.dumps(n)+')' for n in names)+'};'
    projection.append({'mode':prefix,'owner':name,'array_names_in_index_order':names,'scope':'fixture read-surface projection only; no gameplay initialization or nested constructors certified'})
   emit(name,'public class '+name+' extends '+parent+' {\n'+members+'\n}')
  emit('ItemScraps','public class ItemScraps extends ItemAutogen {}')
 (H/'projection-manifest.json').write_text(json.dumps(projection,indent=2)+'\n')
 # Additionally compile the genuinely complete small owner and parent, unchanged outside its one hook token.
 generated += [ROOT/'src/main/java/com/hbm/items/block/ItemBlockMeta.java',ROOT/'src/main/java/com/hbm/items/block/ItemBlockNamedMeta.java',H/'HookOracle.java',ROOT/'porting/tests/ForgeEventNativeLauncher.java']
 cp=CP+':'+str(OLD)
 with (H/'native-compile.log').open('w') as log:subprocess.run([str(J/'bin/javac'),'-encoding','UTF-8','-source','8','-target','8','-sourcepath',str(out/'empty'),'-cp',cp,'-d',str(out)]+list(map(str,generated)),stdout=log,stderr=log,check=True)
 with (H/'runtime.log').open('w') as log:subprocess.run([str(J/'bin/java'),'-Djava.awt.headless=true','-cp',str(out)+':'+cp,'ForgeEventNativeLauncher','HookOracle'],cwd=out,stdout=log,stderr=log,check=True)
 print((H/'runtime.log').read_text().splitlines()[-1],flush=True)
if __name__=='__main__':
 p=argparse.ArgumentParser();p.add_argument('--attribute',action='store_true');p.add_argument('--audit-only',action='store_true');a=p.parse_args();audit()
 if not a.audit_only:
  with tempfile.TemporaryDirectory(prefix='item-stack-key-hooks-') as d:
   out=Path(d)
   if a.attribute:attribute(out)
   runtime(out)
