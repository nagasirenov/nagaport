#!/usr/bin/env python3
from pathlib import Path
import argparse,hashlib,json,os,re,shutil,subprocess,tempfile,zipfile
import check_source
p=argparse.ArgumentParser();p.add_argument('--java-home',type=Path,required=True);p.add_argument('--classpath-file',type=Path,required=True);p.add_argument('--legacy-client',type=Path,required=True);p.add_argument('--legacy-srg',type=Path,required=True);p.add_argument('--verify-attribution',action='store_true');a=p.parse_args()
h=Path(__file__).resolve().parent;r=h.parents[2];java=str(a.java_home/'bin/java');javac=str(a.java_home/'bin/javac');cp=a.classpath_file.read_text().strip();tools=str(a.java_home/'lib/tools.jar')
assert hashlib.sha256(a.legacy_client.read_bytes()).hexdigest()=='a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce'
srg=zipfile.ZipFile(a.legacy_srg).read('joined.srg').decode()
for line in ['MD: sv/be ()Ladd; net/minecraft/entity/EntityLivingBase/func_70694_bm ()Lnet/minecraft/item/ItemStack;','MD: sw/be ()Ladd; net/minecraft/entity/EntityLiving/func_70694_bm ()Lnet/minecraft/item/ItemStack;','MD: yz/be ()Ladd; net/minecraft/entity/player/EntityPlayer/func_70694_bm ()Lnet/minecraft/item/ItemStack;']:assert line in srg
check_source.main()
def method(path,name):
 s=(r/path).read_text();m=re.search(r'public (?:static )?boolean '+name+r'\([^)]*\)\s*\{',s);start=m.start();end=m.end();depth=1
 while depth:
  if s[end]=='{':depth+=1
  if s[end]=='}':depth-=1
  end+=1
 return s[start:end]
with tempfile.TemporaryDirectory(prefix='ntm-held-proof-') as td:
 d=Path(td);runtime=d/'runtime';runtime.mkdir();legacy=d/'original';legacy.mkdir();scanner=d/'scanner';scanner.mkdir()
 library=method('src/main/java/com/hbm/lib/Library.java','checkForHeld');crate=method('src/main/java/com/hbm/items/block/ItemBlockStorageCrate.java','isUseableByPlayer')
 s=(r/'src/main/java/com/hbm/items/weapon/ItemCrucible.java').read_text();gate=next(x.strip()[3:-3] for x in s.splitlines() if 'if(com.hbm.util.NativeHeldItem' in x)
 s=(r/'src/main/java/com/hbm/blocks/machine/BlockTransporterRocket.java').read_text();machine=next(x.strip()[7:-1] for x in s.splitlines() if x.strip().startswith('return com.hbm.util.NativeHeldItem'))
 assert 'ModItems.transporter_linker' in machine;machine=machine.replace('ModItems.transporter_linker','expected') # explicit native Item dependency argument, not an invented ModItems implementation
 body='public ItemStack target; '+library+'\n'+crate+'\npublic static boolean weaponGate(EntityPlayer player){return '+gate+';}\npublic static boolean machineGate(EntityPlayer player,Item expected){return '+machine+';}'
 (runtime/'ProductionBodies.java').write_text('import net.minecraft.entity.player.EntityPlayer; import net.minecraft.item.*; public class ProductionBodies {'+body+'}')
 oldbody=body.replace('com.hbm.util.NativeHeldItem.getMainHandNullable(player)','player.be()').replace('.getItem()','.b()').replace('.getItemDamage()','.k()').replace('.getMaxDamage()','.l()')
 for name,obf in [('EntityPlayer','yz'),('ItemStack','add'),('Item','adb')]:oldbody=re.sub(r'\b'+name+r'\b',obf,oldbody)
 (legacy/'OriginalBodies.java').write_text('public class OriginalBodies {'+oldbody+'}')
 unsigned=d/'original-runtime.jar'
 with zipfile.ZipFile(a.legacy_client) as inz,zipfile.ZipFile(unsigned,'w') as out:
  for info in inz.infolist():
   if info.filename.upper().startswith('META-INF/') and info.filename.upper().endswith(('.SF','.RSA','.DSA')):continue
   out.writestr(info,inz.read(info.filename))
 (legacy/'OriginalCustom.java').write_text('public class OriginalCustom extends yq { public add result; public int calls; public OriginalCustom(){super((ahb)null);} @Override public add be(){calls++;return result;} }')
 subprocess.run([javac,'-cp',str(a.legacy_client),'-d',str(legacy),str(legacy/'OriginalBodies.java'),str(legacy/'OriginalCustom.java')],check=True)
 subprocess.run([javac,'-cp',cp,'-d',str(runtime),str(r/'src/main/java/com/hbm/util/NativeHeldItem.java'),str(h/'NativeHeldTest.java'),str(runtime/'ProductionBodies.java')],check=True)
 subprocess.run([java,'-cp',str(runtime)+os.pathsep+cp,'NativeHeldTest',str(unsigned),str(legacy)],check=True,cwd=runtime)
 bytecode=subprocess.check_output([str(a.java_home/'bin/javap'),'-cp',str(runtime)+os.pathsep+cp,'-c','com.hbm.util.NativeHeldItem'],text=True)
 assert bytecode.count('EntityLivingBase.getHeldItemMainhand:')==1 and 'ItemStack.EMPTY:' in bytecode and 'isEmpty' not in bytecode and 'Offhand' not in bytecode
 print('PASS real helper bytecode: one virtual native main-hand call, canonical EMPTY identity only, no offhand/isEmpty normalization')
 subprocess.run([javac,'-cp',tools,'-d',str(scanner),str(h/'HeldItemScanner.java')],check=True)
 def scan(paths):
  f=d/'inputs';f.write_text('\n'.join(map(str,paths))+'\n');return list(map(json.loads,subprocess.check_output([java,'-Xmx3g','-cp',str(scanner)+os.pathsep+tools,'HeldItemScanner',str(f),str(a.classpath_file)],text=True).splitlines()))
 prefix='import net.minecraft.entity.*; import net.minecraft.entity.monster.EntityZombie; import net.minecraft.entity.player.EntityPlayer; import net.minecraft.item.ItemStack; import net.minecraft.world.World; '
 cases=[('class T {Object f(EntityPlayer p){return p.getHeldItem();}}',1),('class T {Object f(EntityLivingBase p){return p.getHeldItem();}}',1),('class T {Object f(EntityZombie p){return p.getHeldItem();}}',1),('class T {Object f(Object p){return p.getHeldItem();}}',0),('class T {Object f(Missing p){return p.getHeldItem();}}',0),('class T {Object getHeldItem(){return null;} Object f(T p){return p.getHeldItem();}}',0),('class T extends EntityZombie {T(){super((World)null);} public ItemStack getHeldItem(){return null;} Object f(){return this.getHeldItem();}}',0),('class T {Object f(EntityPlayer p){return p.getHeldItem(net.minecraft.util.EnumHand.OFF_HAND);}}',0),('class T {Object f(EntityPlayer p){return p.getHeldItemMainhand();}}',0),('class T {Object f(EntityPlayer p){return ((EntityLivingBase)p).getHeldItem();}}',1),('class T {Object f(){return ((EntityPlayer)null).getHeldItem();}}',1),('class T<E extends EntityPlayer> {Object f(E p){return p.getHeldItem();}}',0),('class T {Object f(){return EntityPlayer.getHeldItem();}}',0),('class T extends EntityZombie {T(){super((World)null);} Object f(){return super.getHeldItem();}}',0),('class T {Object f(EntityPlayer p){String s="😀";return p.getHeldItem();}}',1),('class T {Object f(EntityPlayer p){return p.\\u0067etHeldItem();}}',0),('class T {Object f(EntityPlayer p,int com){return p.getHeldItem();}}',0),('class T<com> {Object f(EntityPlayer p){return p.getHeldItem();}}',0),('class T extends EntityZombie {T(){super((World)null);} Object f(){return this.getHeldItem();}}',1),('class T extends EntityZombie {T(){super((World)null);} public ItemStack getHeldItemMainhand(){return ItemStack.EMPTY;} Object f(){return this.getHeldItem();}}',1)]
 cases += [
 ('class T extends EntityZombie {T(){super((World)null);} public ItemStack getHeldItem(Object... x){return null;} Object f(){return this.getHeldItem();}}',0),
 ('class T extends EntityZombie {T(){super((World)null);} public static ItemStack getHeldItem(){return null;} Object f(){return this.getHeldItem();}}',0),
 ('abstract class T extends EntityZombie implements HeldShadow {T(){super((World)null);} Object f(){return this.getHeldItem();}} interface HeldShadow {ItemStack getHeldItem();}',0)]
 paths=[]
 for i,(src,n) in enumerate(cases):
  f=d/('T'+str(i)+'.java');f.write_text(prefix+re.sub(r'\bT\b','T'+str(i),src));paths.append(f)
 rows=scan(paths)
 for i,(s,n) in enumerate(cases):assert sum(x['accept'] for x in rows if x['path']==str(paths[i]))==n,(i,n)
 print('PASS',len(cases),'actual javac owner, shadow, generic, static, explicit-hand, super and Unicode fixtures')
 if a.verify_attribution:
  original=d/'sources';shutil.copytree(r/'src/main/java',original)
  entries=json.loads((h/'source-manifest.json').read_text())
  for e in entries:(original/Path(e['path']).relative_to('src/main/java')).write_bytes(check_source.inverse((r/e['path']).read_bytes(),e))
  rows=scan(sorted(original.rglob('*.java')));selected={(str(Path(x['path']).relative_to(original)),x['start'],x['end'],x['type']) for x in rows if x['accept']}
  expected={(str(Path(e['path']).relative_to('src/main/java')),x['start'],x['end'],x['receiver_type']) for e in entries for x in e['edits']};assert selected==expected,(selected-expected,expected-selected)
  print('PASS full native javac re-attribution equality:',len(selected),'calls,',len(entries),'production files; unrelated port diagnostics remain expected')
  native=scan(sorted((r/'src/main/java').rglob('*.java')));native=[x for x in native if x.get('mode')=='native'];assert len(native)==len(expected) and all(x['accept'] for x in native)
  expected_native=set()
  for e in entries:
   delta=0
   for edit in sorted(e['edits'],key=lambda x:x['start']):
    start=edit['start']+delta;end=start+len(edit['after'].encode('utf-16-le'))//2;expected_native.add((str(r/e['path']),start,end));delta+=len(edit['after'].encode('utf-16-le'))//2-(edit['end']-edit['start'])
  assert {(x['path'],x['start'],x['end']) for x in native}==expected_native
  print('PASS exact post-edit native static owner/EntityLivingBase signature attribution:',len(native),'calls')
