#!/usr/bin/env python3
"""Finite write-expression/native runtime proof, not complete NTM class compilation."""
from pathlib import Path
import argparse, collections, hashlib, importlib.util, io, json, os, re, shutil, subprocess, sys, tempfile, zipfile
HOME=Path(__file__).resolve().parent; ROOT=HOME.parents[2]
p=argparse.ArgumentParser();p.add_argument('--java-home',type=Path,required=True);p.add_argument('--classpath-file',type=Path,required=True);p.add_argument('--legacy-client',type=Path,required=True);p.add_argument('--forge-userdev',type=Path,required=True);p.add_argument('--attribute',action='store_true');p.add_argument('--composed-source',action='store_true');a=p.parse_args()
cp=a.classpath_file.read_text().strip();java=str(a.java_home/'bin/java');javac=str(a.java_home/'bin/javac');javap=str(a.java_home/'bin/javap');tools=str(a.java_home/'lib/tools.jar')
spec=importlib.util.spec_from_file_location('guard',HOME/'check-source.py');guard=importlib.util.module_from_spec(spec);spec.loader.exec_module(guard);m=guard.DATA
if a.composed_source:
 assert not a.attribute, 'isolated attribution offsets require the isolated family tree'
 subprocess.run(['python3',str(ROOT/'porting/checkpoint87/check-family-composition.py')],check=True)
else:
 guard.main()
assert hashlib.sha256(a.legacy_client.read_bytes()).hexdigest()=='a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce'
assert hashlib.sha256(a.forge_userdev.read_bytes()).hexdigest()=='6ed364c0defdb481a41ef2d99c02d382f16fc42a6ea0ea1c2b849848d8466e91'
rows=[e for f in m['files'].values() for e in f['edits']];assert len(rows)==27
# Audit the actual Forge baseline, rather than incorrectly treating vanilla's player setter as authoritative.
z=zipfile.ZipFile(a.forge_userdev);patches={n:zipfile.ZipFile(io.BytesIO(z.read(n))) for n in ('forgepatches.zip','fmlpatches.zip')}
forge=patches['forgepatches.zip'];playerpatch=forge.read('net/minecraft/entity/player/EntityPlayer.java.patch').decode()
hunk=next(x for x in re.split(r'(?=^@@)',playerpatch,flags=re.M) if 'public void func_70062_b' in x)
assert '-        this.field_71071_by.field_70460_b[p_70062_1_] = p_70062_2_;' in hunk
assert '+            this.field_71071_by.field_70462_a[this.field_71071_by.field_70461_c] = p_70062_2_;' in hunk
assert '+            this.field_71071_by.field_70460_b[p_70062_1_ - 1] = p_70062_2_;' in hunk
for package,q in patches.items():
 for name in ('net/minecraft/entity/EntityLiving.java.patch','net/minecraft/entity/monster/EntitySkeleton.java.patch','net/minecraft/client/entity/EntityOtherPlayerMP.java.patch'):
  if name in q.namelist():assert not any(k in q.read(name).decode() for k in ('func_70062_b','func_96120_a'))
assert 'func_70062_b' not in patches['fmlpatches.zip'].read('net/minecraft/entity/player/EntityPlayer.java.patch').decode()
(HOME/'forge-player-setter-correction.patch').write_text(hunk)
print('PASS exact Forge1614 player setter correction and untouched mob/skeleton/drop contracts')
mod_items=sorted({name for row in rows for name in re.findall(r'ModItems\.(\w+)',row['rhs'])})
actual=(ROOT/'src/main/java/com/hbm/items/ModItems.java').read_text()
for name in mod_items:assert re.search(r'public static Item '+re.escape(name)+r'\s*;',actual),name
# Explicitly instrument only NTM ModItems dependencies with same-typed native Item fields.
# Minecraft/Forge classes, slot storage, ItemStack constructors and dispatch are real.
imports='import net.minecraft.entity.*; import net.minecraft.entity.monster.EntityZombie; import net.minecraft.inventory.EntityEquipmentSlot; import net.minecraft.item.*; import net.minecraft.init.*; import net.minecraft.world.World;\n'
def source(name,key):
 s=[imports,'public class '+name+' extends EntityZombie {','public '+name+'(){super((World)null);}',
    'static final class ModItems { '+''.join('static Item '+n+'=new Item();' for n in mod_items)+' }',
    'public static final int COUNT=27;',
    'public static final int[] SLOT={'+','.join(str(x['slot']) for x in rows)+'};',
    'public static final boolean[] DROP={'+','.join(str(x['method']=='setEquipmentDropChance').lower() for x in rows)+'};',
    'public void write(int which,EntityZombie zombie,EntityLiving entity){switch(which){']
 for i,row in enumerate(rows):s.append('case '+str(i)+':{'+row[key]+';return;}')
 s+=['default:throw new AssertionError(which);}}','public static Item expectedItem(int which){switch(which){']
 for i,row in enumerate(rows):
  if row['method']=='setCurrentItemOrArmor':
   match=re.fullmatch(r'new ItemStack\((.*)\)',row['rhs']);assert match,row
   arg=match.group(1);s.append('case '+str(i)+':return '+('Item.getItemFromBlock('+arg+')' if arg.startswith('Blocks.') else arg)+';')
 s+=['default:return null;}}','public static float expectedChance(int which){switch(which){']
 for i,row in enumerate(rows):
  if row['method']=='setEquipmentDropChance':s.append('case '+str(i)+':return '+row['rhs']+';')
 s+=['default:throw new AssertionError(which);}}',
  'public int setterCalls,dropCalls; public EntityEquipmentSlot lastSlot; public ItemStack lastStack; public float lastChance;',
  '@Override public void setItemStackToSlot(EntityEquipmentSlot slot,ItemStack stack){setterCalls++;lastSlot=slot;lastStack=stack;super.setItemStackToSlot(slot,stack);}',
  '@Override public void setDropChance(EntityEquipmentSlot slot,float chance){dropCalls++;lastSlot=slot;lastChance=chance;super.setDropChance(slot,chance);}','}']
 return '\n'.join(s)+'\n'
with tempfile.TemporaryDirectory(prefix='equipment-write-proof-') as td:
 d=Path(td);(d/'ProductionWrites.java').write_text(source('ProductionWrites','new'));(d/'BaselineWrites.java').write_text(source('BaselineWrites','old'))
 before=subprocess.run([javac,'-source','8','-target','8','-Xmaxerrs','1000','-cp',cp,'-d',str(d),str(d/'BaselineWrites.java')],capture_output=True,text=True)
 assert before.returncode!=0
 errors=collections.Counter(x.split('error: ',1)[1] for x in before.stderr.splitlines() if 'error: ' in x);assert errors=={'cannot find symbol':27},errors
 (HOME/'baseline-expression-diagnostics.log').write_text(before.stderr.replace(str(d),'<probe>'));print('PASS all 27 exact original expressions fail against native classes; no unrelated expression omitted')
 unsigned=d/'original-runtime.jar'
 with zipfile.ZipFile(a.legacy_client) as inz,zipfile.ZipFile(unsigned,'w') as out:
  for info in inz.infolist():
   if info.filename.upper().startswith('META-INF/') and info.filename.upper().endswith(('.SF','.RSA','.DSA')):continue
   out.writestr(info,inz.read(info.filename))
 original_classes=d/'original';original_classes.mkdir()
 (original_classes/'OriginalWriteTracking.java').write_text('public class OriginalWriteTracking extends yq { public int calls,slot; public add stack; public OriginalWriteTracking(){super((ahb)null);} @Override public void c(int slot,add stack){calls++;this.slot=slot;this.stack=stack;super.c(slot,stack);} }')
 subprocess.run([javac,'-cp',str(a.legacy_client),'-d',str(original_classes),str(original_classes/'OriginalWriteTracking.java')],check=True)
 subprocess.run([javac,'-source','8','-target','8','-cp',cp,'-d',str(d),str(d/'ProductionWrites.java'),str(HOME/'NativeEquipmentWriteTest.java')],check=True)
 subprocess.run([java,'-cp',str(d)+os.pathsep+cp,'NativeEquipmentWriteTest',str(unsigned),str(original_classes)],check=True,cwd=d)
 contract=subprocess.check_output([javap,'-c','-p','-classpath',cp,'net.minecraft.entity.EntityLiving','net.minecraft.entity.player.EntityPlayer','net.minecraft.entity.monster.AbstractSkeleton'],text=True)
 oldcontract=subprocess.check_output([javap,'-c','-p','-classpath',str(a.legacy_client),'sw','yz','yl'],text=True)
 def method(s,name):
  start=s.index(name);end=s.find('\n\n',start);return s[start:end]
 snippets=[method(contract,n) for n in ['public void setItemStackToSlot(', 'public void setDropChance(']]
 # Include each player's and skeleton's distinct overriding method, not merely the first inherited implementation.
 for name in ['net.minecraft.entity.player.EntityPlayer','net.minecraft.entity.monster.AbstractSkeleton']:
  s=subprocess.check_output([javap,'-c','-p','-classpath',cp,name],text=True);snippets.append(method(s,'public void setItemStackToSlot('))
 for name in ['sw','yz','yl']:
  s=subprocess.check_output([javap,'-c','-p','-classpath',str(a.legacy_client),name],text=True);snippets.append(name+'\n'+method(s,'public void c(int, add);'))
 (HOME/'engine-setter-contract.javap.txt').write_text('\n\n'.join(snippets)+'\n')
 if a.attribute:
  classes=d/'scanner';classes.mkdir();subprocess.run([javac,'-cp',tools,'-d',str(classes),str(HOME/'EquipmentWriteScanner.java')],check=True)
  def scan(paths):
   inputs=d/'inputs';inputs.write_text('\n'.join(map(str,paths))+'\n');r=subprocess.run([java,'-Xmx3g','-cp',str(classes)+os.pathsep+tools,'EquipmentWriteScanner',str(inputs),str(a.classpath_file)],capture_output=True,text=True,check=True);return [json.loads(x) for x in r.stdout.splitlines()],r.stderr
  fixtures=[
   ('class T {void f(EntityZombie z){z.setCurrentItemOrArmor(0,ItemStack.EMPTY);}}',1),
   ('class T {void f(EntityLiving z){z.setCurrentItemOrArmor(4,ItemStack.EMPTY);}}',1),
   ('class T {void f(EntityLivingBase z){z.setCurrentItemOrArmor(4,ItemStack.EMPTY);}}',0),
   ('class T {void f(net.minecraft.entity.player.EntityPlayer z){z.setCurrentItemOrArmor(4,ItemStack.EMPTY);}}',0),
   ('class T {void f(EntityZombie z,int i){z.setCurrentItemOrArmor(i,ItemStack.EMPTY);}}',0),
   ('class T {void f(Object z){z.setCurrentItemOrArmor(0,ItemStack.EMPTY);}}',0),
   ('class T extends EntityZombie {T(){super((World)null);} public void setCurrentItemOrArmor(int i,ItemStack s){} void f(){this.setCurrentItemOrArmor(0,ItemStack.EMPTY);}}',0),
   ('class T {void f(){EntityZombie.setCurrentItemOrArmor(0,ItemStack.EMPTY);}}',0),
   ('class T extends EntityZombie {T(){super((World)null);} void f(){super.setCurrentItemOrArmor(0,ItemStack.EMPTY);}}',0),
   ('class T {void f(EntityZombie z,int net){z.setCurrentItemOrArmor(0,ItemStack.EMPTY);}}',0),
   ('class T<E extends EntityZombie> {void f(E z){z.setCurrentItemOrArmor(0,ItemStack.EMPTY);}}',0),
   (r'class T {void f(EntityZombie z){z.setCurrent\u0049temOrArmor(0,ItemStack.EMPTY);}}',0),
   ('class T {void f(EntityZombie z){z.setItemStackToSlot(EntityEquipmentSlot.HEAD,ItemStack.EMPTY);}}',1),
   ('class T {void f(EntityLiving z){z.setDropChance(EntityEquipmentSlot.MAINHAND,Float.NaN);}}',1),
   ('class T {void f(EntityZombie z){z.setItemStackToSlot(EntityEquipmentSlot.OFFHAND,ItemStack.EMPTY);}}',0),
   ('class T extends EntityZombie {T(){super((World)null);} public void setItemStackToSlot(EntityEquipmentSlot s,ItemStack i){} void f(){this.setItemStackToSlot(EntityEquipmentSlot.HEAD,ItemStack.EMPTY);}}',0),
   ('class T {void f(EntityZombie z){z.setCurrentItemOrArmor(5,ItemStack.EMPTY);}}',0),
   ('class T {void f(EntityZombie z){z.setEquipmentDropChance(-1,0F);}}',0),
   ('class T {void f(EntityLivingBase z){z.setEquipmentDropChance(0,0F);}}',0),
   ('class T {void f(EntityZombie z){z.setEquipmentDropChance(0,0F);}}',1),
  ]
  fixture_paths=[]
  for i,(text,wanted) in enumerate(fixtures):
   path=d/('T'+str(i)+'.java');path.write_text(imports+re.sub(r'\bT\b','T'+str(i),text));fixture_paths.append(path)
  tested,tlog=scan(fixture_paths)
  for i,(_,wanted) in enumerate(fixtures):assert sum(x['accept'] for x in tested if x['path']==str(fixture_paths[i]))==wanted,(i,wanted,tested)
  print('PASS 20 attributed owner/generic/shadow/override/static/super/Unicode/slot mutation fixtures')
  original=d/'source';shutil.copytree(ROOT/'src/main/java',original)
  for path,row in m['files'].items():p=original/Path(path).relative_to('src/main/java');p.write_bytes(guard.reverse_source(path,p.read_bytes().decode()).encode())
  before,blog=scan(sorted(original.rglob('*.java')));assert 'DECLARATION' not in blog,blog
  selected={(str(Path(x['path']).relative_to(original)),x['start'],x['end'],x['type']) for x in before if x['accept']}
  expected={(str(Path(p).relative_to('src/main/java')),e['start'],e['end'],e['type']) for p,f in m['files'].items() for e in f['edits']};assert selected==expected
  assert collections.Counter((x['method'],x['accept']) for x in before)=={('setCurrentItemOrArmor',True):24,('setCurrentItemOrArmor',False):7,('setEquipmentDropChance',True):3}
  after,alog=scan(sorted((ROOT/'src/main/java').rglob('*.java')));assert 'DECLARATION' not in alog,alog
  actual={(str(Path(x['path']).relative_to(ROOT)),x['start'],x['end']) for x in after if x['mode']=='native'};expected=set()
  for path,f in m['files'].items():
   delta=0
   for e in sorted(f['edits'],key=lambda x:x['start']):
    start=e['start']+delta;end=start+len(e['new'].encode('utf-16-le'))//2;expected.add((path,start,end));delta+=len(e['new'].encode('utf-16-le'))//2-(e['end']-e['start'])
  assert actual==expected;assert collections.Counter((x['mode'],x['accept']) for x in after)=={('native',True):27,('old',False):7}
  print('PASS full '+str(m['source_units'])+'-unit before/after javac typed ownership; exact 27-position bijection; no owned setter/drop/sound overrides; 7 deferred expressions')
print('PASS complete 27 native write expressions compiled/executed with explicit native Item dependency fixtures. Complete NTM classes/gameplay are not claimed.')
