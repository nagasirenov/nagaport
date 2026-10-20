#!/usr/bin/env python3
"""Actual original/native read probes, complete selected expressions, and source ownership."""
from pathlib import Path
import argparse,collections,hashlib,json,os,re,subprocess,tempfile,zipfile
import check_source
H=Path(__file__).resolve().parent;R=H.parents[2]
p=argparse.ArgumentParser();p.add_argument('--java-home',type=Path,default=Path('/workspace/shared/nagaport-toolchain/java8'));p.add_argument('--classpath-file',type=Path,default=Path('/workspace/shared/nagaport-toolchain/native-classpath.txt'));p.add_argument('--legacy-client',type=Path,default=Path('/tmp/nagaport-legacy-client.jar'));p.add_argument('--attribute',action='store_true');p.add_argument('--composed-source',action='store_true');a=p.parse_args()
J=a.java_home;CP=a.classpath_file.read_text().strip();D=check_source.DATA
if a.composed_source:
 assert not a.attribute, 'isolated attribution offsets require the isolated family tree'
 subprocess.run(['python3',str(R/'porting/checkpoint87/check-family-composition.py')],check=True)
else:
 check_source.main()
rows=[e for row in D['files'].values() for e in row['edits']]
assert len(rows)==44

def method(path,name):
 s=(R/path).read_bytes().decode();m=re.search(r'public static (?:boolean|void) '+name+r'\([^)]*\)\s*\{',s);start=m.start();end=m.end();depth=1
 while depth:
  if s[end]=='{':depth+=1
  if s[end]=='}':depth-=1
  end+=1
 return s[start:end]
bodies='\n'.join(method('src/main/java/com/hbm/util/ArmorUtil.java',n) for n in ['checkArmor','checkArmorPiece','checkArmorNull'])+'\n'+method('src/main/java/com/hbm/hazard/HazardSystem.java','updateLivingInventory')
s=(R/'src/main/java/com/hbm/util/DamageResistanceHandler.java').read_text();start=s.index('new Quartet(',s.index('Quartet wornSet ='));end=s.index(');',start)+1;quartet=s[start:end]
body_deps='static final class Quartet { final Item[] items; Quartet(Item... items){this.items=items;} }\nstatic java.util.List<ItemStack> seen=new java.util.ArrayList<ItemStack>(); static java.util.List<EntityLivingBase> owners=new java.util.ArrayList<EntityLivingBase>(); static void applyHazards(ItemStack stack,EntityLivingBase entity){seen.add(stack);owners.add(entity);}\n'
sig='EntityLivingBase entity, EntityLivingBase living, EntityLivingBase e, EntityPlayer player, EntityPlayer p, LivingEvent event, int i, int slot'
code=['import net.minecraft.entity.*; import net.minecraft.entity.player.*; import net.minecraft.entity.monster.EntityZombie; import net.minecraft.item.*; import net.minecraft.world.World; import net.minecraftforge.event.entity.living.LivingEvent;','public class ProductionExpressions extends EntityZombie { public ProductionExpressions(){super((World)null);}', 'public static final int COUNT=44;', 'public ItemStack read(int which, '+sig+') {switch(which){']
for i,row in enumerate(rows):code.append('case %d: return %s;'%(i,row['new']))
code+=['default: throw new AssertionError(which);}}',bodies,body_deps,'static Item[] worn(EntityLivingBase entity){return '+quartet+'.items;}', '}']
code='\n'.join(code)
oldcode=code.replace('ProductionExpressions','OriginalExpressions').replace('import net.minecraft.entity.*; import net.minecraft.entity.player.*; import net.minecraft.entity.monster.EntityZombie; import net.minecraft.item.*; import net.minecraft.world.World; import net.minecraftforge.event.entity.living.LivingEvent;','')
for row in rows:oldcode=oldcode.replace(row['new'],row['old'])
oldcode=oldcode.replace('.getEquipmentInSlot(','.q(').replace('.getCurrentArmor(','.r(').replace('.getItem()','.b()')
for name,obf in [('EntityLivingBase','sv'),('EntityPlayer','yz'),('EntityZombie','yq'),('ItemStack','add'),('Item','adb'),('World','ahb'),('LivingEvent','OriginalEvent')]:oldcode=re.sub(r'\b'+name+r'\b',obf,oldcode)
oldcode+='\nclass OriginalEvent { final sv entity; OriginalEvent(sv e){entity=e;} public sv getEntityLiving(){return entity;} }\n'
with tempfile.TemporaryDirectory(prefix='hbm-equipment-reads-') as td:
 d=Path(td);native=d/'native';native.mkdir();old=d/'legacy';old.mkdir()
 (native/'ProductionExpressions.java').write_text(code);(old/'OriginalExpressions.java').write_text(oldcode)
 # All original selected expressions must fail against real native types, independently of helper proof.
 baseline=code[:code.index('public static final int COUNT=')]+ 'public ItemStack read(int which, '+sig+') {switch(which){'+''.join('case %d:return %s;'%(i,e['old']) for i,e in enumerate(rows))+'default:throw new AssertionError();}}}'
 baseline=baseline.replace('ProductionExpressions','BaselineExpressions');(native/'BaselineExpressions.java').write_text(baseline)
 proc=subprocess.run([str(J/'bin/javac'),'-cp',CP,'-Xmaxerrs','1000','-d',str(native),str(native/'BaselineExpressions.java')],capture_output=True,text=True)
 assert proc.returncode!=0 and proc.stderr.count('error: cannot find symbol')==44,proc.stderr
 (H/'baseline-expression-diagnostics.log').write_text(proc.stderr.replace(str(d),'<probe>'))
 print('PASS all 44 old selected expressions fail independently against native engine signatures')
 (old/'OriginalInventory.java').write_text('public class OriginalInventory extends yx { public add result; public int calls,last; public OriginalInventory(yz p){super(p);} @Override public add d(int i){calls++;last=i;return result;} }')
 (old/'OriginalCustom.java').write_text('public class OriginalCustom extends yq { public add[] result; public int calls,last; public OriginalCustom(){super((ahb)null);} @Override public add q(int i){last=i;return result[calls++];} }')
 unsigned=d/'legacy-unsigned.jar'
 with zipfile.ZipFile(a.legacy_client) as iz,zipfile.ZipFile(unsigned,'w') as oz:
  for info in iz.infolist():
   if info.filename.upper().startswith('META-INF/') and info.filename.upper().endswith(('.SF','.RSA','.DSA')):continue
   oz.writestr(info,iz.read(info.filename))
 subprocess.run([str(J/'bin/javac'),'-cp',str(a.legacy_client),'-d',str(old),str(old/'OriginalExpressions.java'),str(old/'OriginalCustom.java'),str(old/'OriginalInventory.java')],check=True)
 subprocess.run([str(J/'bin/javac'),'-encoding','UTF-8','-source','8','-target','8','-cp',CP,'-d',str(native),str(R/D['helper']['file']),str(native/'ProductionExpressions.java'),str(H/'NativeEquipmentReadTest.java')],check=True)
 subprocess.run([str(J/'bin/java'),'-cp',str(native)+os.pathsep+CP,'NativeEquipmentReadTest',str(unsigned),str(old)],check=True,cwd=native)
 bytecode=subprocess.check_output([str(J/'bin/javap'),'-p','-c','-cp',str(native)+os.pathsep+CP,'com.hbm.util.LivingEquipmentRead'],text=True)
 assert bytecode.count('EntityLivingBase.getItemStackFromSlot:')==1 and bytecode.count('InventoryPlayer.armorItemInSlot:')==1
 assert 'isEmpty:' not in bytecode and 'OFFHAND:' not in bytecode and bytecode.count('Field net/minecraft/item/ItemStack.EMPTY:')==2
 (H/'helper-bytecode.txt').write_text(bytecode)
 print('PASS actual helper bytecode: one virtual equipment dispatch or one virtual inventory armor dispatch, canonical sentinel identity only')
 helper=(R/D['helper']['file']).read_text()
 mutations={
  'empty-normalization':helper.replace('stack == ItemStack.EMPTY','stack.isEmpty()'),
  'slot-order':helper.replace('EntityEquipmentSlot.FEET, EntityEquipmentSlot.LEGS','EntityEquipmentSlot.HEAD, EntityEquipmentSlot.LEGS').replace('EntityEquipmentSlot.CHEST, EntityEquipmentSlot.HEAD','EntityEquipmentSlot.CHEST, EntityEquipmentSlot.FEET'),
  'null-index-order':helper.replace('        Objects.requireNonNull(entity);\n',''),
  'armor-dispatch-bypass':helper.replace('player.inventory.armorItemInSlot(slot)','player.inventory.armorInventory.get(slot)'),
  'alias-copy':helper.replace('null : stack;','null : stack.copy();'),
 }
 for name,source in mutations.items():
  out=d/('mutant-'+name);out.mkdir();file=out/'LivingEquipmentRead.java';file.write_text(source)
  subprocess.run([str(J/'bin/javac'),'-cp',CP,'-d',str(out),str(file)],check=True,capture_output=True,text=True)
  trial=subprocess.run([str(J/'bin/java'),'-cp',str(out)+os.pathsep+str(native)+os.pathsep+CP,'NativeEquipmentReadTest',str(unsigned),str(old)],cwd=out,capture_output=True,text=True)
  assert trial.returncode!=0 and 'Exception in thread "main"' in trial.stderr,(name,trial.stdout,trial.stderr)
  print('PASS rejected semantic mutant:',name)

 if a.attribute:
  tools=str(J/'lib/tools.jar');scanner=d/'scanner';scanner.mkdir();subprocess.run([str(J/'bin/javac'),'-cp',tools,'-d',str(scanner),str(H/'EquipmentReadScanner.java')],check=True)
  import shutil
  before=d/'sources';shutil.copytree(R/'src/main/java',before)
  for rel,row in D['files'].items():(before/Path(rel).relative_to('src/main/java')).write_text(check_source.inverse(rel,(R/rel).read_bytes().decode()),newline='')
  (before/Path(D['helper']['file']).relative_to('src/main/java')).unlink()
  def scan(paths):
   inputs=d/'inputs';inputs.write_text('\n'.join(map(str,paths))+'\n');out=subprocess.run([str(J/'bin/java'),'-Xmx3g','-cp',str(scanner)+os.pathsep+tools,'EquipmentReadScanner',str(inputs),str(a.classpath_file)],text=True,capture_output=True,check=True)
   assert 'Override declaration audit:' not in out.stderr,out.stderr
   return list(map(json.loads,out.stdout.splitlines())),out.stderr
  found,log=scan(sorted(before.rglob('*.java')));expected={(str(Path(rel).relative_to('src/main/java')),e['start'],e['end'],e['type']) for rel,row in D['files'].items() for e in row['edits']}
  expected|={(str(Path(e['file']).relative_to('src/main/java')),e['start'],e['end'],e['type']) for e in D['excluded']}
  actual={(str(Path(e['path']).relative_to(before)),e['start'],e['end'],e['type']) for e in found if e['accept']};assert actual==expected and len(found)==44
  found,postlog=scan(sorted((R/'src/main/java').rglob('*.java')));new=[e for e in found if e.get('mode')=='native'];assert len(new)==44 and all(e['accept'] for e in new)
  expectednew=set()
  for rel,row in D['files'].items():
   delta=0
   for e in sorted(row['edits'],key=lambda x:x['start']):
    start=e['start']+delta;end=start+len(e['new'].encode('utf-16-le'))//2;expectednew.add((str(R/rel),start,end));delta+=end-start-(e['end']-e['start'])
  assert {(e['path'],e['start'],e['end']) for e in new}==expectednew
  (H/'attribution.log').write_text('CP86 before: '+log+'After: '+postlog+'Exact before: 44 owned calls (44 selected, none excluded). Exact after: 44 static helper signatures.\n')
  print('PASS complete CP86/native typed attribution: 44 owned old calls, exactly 44 native helper calls, all machine-energy surrounding bytes unchanged')
print('PASS bounded equipment read proof; no full mod, gameplay, energy, radiation formula or save acceptance claimed')
