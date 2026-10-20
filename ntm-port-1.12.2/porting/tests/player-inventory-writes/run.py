#!/usr/bin/env python3
from pathlib import Path
import argparse,collections,hashlib,importlib.util,json,os,re,subprocess,tempfile,zipfile
HERE=Path(__file__).resolve().parent;ROOT=HERE.parents[2]
p=argparse.ArgumentParser();p.add_argument('--java-home',type=Path,default=Path('/workspace/shared/nagaport-toolchain/java8'));p.add_argument('--classpath-file',type=Path,default=Path('/workspace/shared/nagaport-toolchain/native-classpath.txt'));p.add_argument('--attribute',action='store_true');p.add_argument('--composed-source',action='store_true',help='Require a separately run combined source inverse; still check every exact write token here, skip standalone full-file diagnostic equality');a=p.parse_args()
java=a.java_home/'bin/java';javac=a.java_home/'bin/javac';tools=a.java_home/'lib/tools.jar';cp=a.classpath_file.read_text().strip();manifest=json.loads((HERE/'source-manifest.json').read_text())
def run(cmd,**kw):return subprocess.run(list(map(str,cmd)),check=True,text=True,**kw)
def utf16(s):return len(s.encode('utf-16-le'))//2
def digest(b):return hashlib.sha256(b).hexdigest()
if a.composed_source:
 for rel,entry in manifest['files'].items():
  text=(ROOT/rel).read_bytes().decode()
  for e in entry['edits']:assert text.count(e['new'])==1,(rel,e['new'])
 print('Composed source mode: 21 exact write tokens checked; standalone whole-file guard and standalone diagnostic equality are NOT claimed. Run the combined inverse guard separately.')
else:run(['python3',HERE/'check-source.py'])
with tempfile.TemporaryDirectory(prefix='inventory-writes-native-')as td:
 t=Path(td);classes=t/'classes';classes.mkdir();edits=[(rel,e)for rel,es in manifest['files'].items()for e in es['edits']];exact=[(rel,e)for rel,e in edits if 'stack.stackSize'not in e['new']];assert len(exact)==20
 # The only collaborators are twelve named item identity fields and a tile payload field.
 fields=sorted(set(re.findall(r'ModItems\.(\w+)', '\n'.join(e['new']for rel,e in exact))))
 body='import net.minecraft.entity.player.EntityPlayer;import net.minecraft.item.*;import net.minecraft.block.Block;import net.minecraft.block.material.Material;import net.minecraft.init.Items; public final class ProductionWrites extends Block {public ProductionWrites(){super(Material.ROCK);} public static class Holder{ItemStack item;} static class ModItems{'+''.join('static final Item '+f+'=Items.STICK;'for f in fields)+'} '
 body+='public static final int COUNT=20; public static final boolean[] ARMOR={'+','.join(str(e['field']=='armorInventory').lower()for rel,e in exact)+'}; public static final String[] KIND={'+','.join(json.dumps(e['rhs_kind'])for rel,e in exact)+'}; public static final int[] INDEX={'+','.join(str({'i':2,'j':3,'player.inventory.currentItem':6,'pl.inventory.currentItem':6}.get(e['index'],e['index']))for rel,e in exact)+'}; '
 body+='public void write(int n,EntityPlayer player,int i,int j,Holder pedestal){EntityPlayer p=player,pl=player;switch(n){'+''.join('case '+str(i)+':'+e['new']+';break;'for i,(rel,e)in enumerate(exact))+'default:throw new AssertionError();}}'
 folder=next(e for rel,e in edits if 'ItemFolderPacket' in rel);body+='public static void folderBoundary(EntityPlayer player,int i,ItemStack replacement){'+folder['new'].replace(folder['rhs'],'replacement')+';}'
 copy=next(e for rel,e in edits if e['rhs_kind']=='METHOD_INVOCATION');body+='public void copyBoundary(EntityPlayer player,Holder pedestal){'+copy['new']+';}}'
 (t/'ProductionWrites.java').write_text(body)
 run([javac,'-source','8','-target','8','-sourcepath','','-cp',cp,'-d',classes,t/'ProductionWrites.java',HERE/'NativeWriteTest.java'])
 run([java,'-cp',str(classes)+os.pathsep+cp,'NativeWriteTest'],cwd=t)
 engine=run([a.java_home/'bin/javap','-p','-c','-classpath',cp,'net.minecraft.entity.player.InventoryPlayer','net.minecraft.util.NonNullList','net.minecraft.item.ItemStack'],capture_output=True).stdout
 selected=[]
 for signature in ['public net.minecraft.entity.player.InventoryPlayer(net.minecraft.entity.player.EntityPlayer);','public E set(int, E);','public net.minecraft.item.ItemStack copy();']:
  start=engine.index('  '+signature);end=engine.find('\n  public ',start+1)
  if end<0:end=engine.find('\n}',start+1)
  selected.append(engine[start:end].rstrip())
 declarations='\n'.join(line for line in engine.splitlines()if 'public final net.minecraft.util.NonNullList<net.minecraft.item.ItemStack>' in line)
 (HERE/'native-engine-bytecode.txt').write_text(declarations+'\n\n'+'\n\n'.join(selected)+'\n')
 assert 'public final net.minecraft.util.NonNullList<net.minecraft.item.ItemStack> mainInventory;'in engine and 'public final net.minecraft.util.NonNullList<net.minecraft.item.ItemStack> armorInventory;'in engine
 method=engine.split('public E set(int, E);')[1].split('\n  public')[0];assert method.index('Validate.notNull')<method.index('InterfaceMethod java/util/List.set')
 print('PASS real native final field declarations and NonNullList null-validation/set ordering')
 providers={}
 for name in ['net/minecraft/entity/player/InventoryPlayer.class','net/minecraft/util/NonNullList.class','net/minecraft/item/ItemStack.class']:
  for path in cp.split(os.pathsep):
   f=Path(path)
   if f.is_file() and zipfile.is_zipfile(f):
    with zipfile.ZipFile(f)as z:
     if name in z.namelist():providers[name]={'provider':str(f),'provider_sha256':digest(f.read_bytes()),'class_sha256':digest(z.read(name))};break
  assert name in providers
 pin=HERE/'native-runtime-inputs.json'
 if pin.exists():assert providers==json.loads(pin.read_text()),'native class provider drift'
 else:pin.write_text(json.dumps(providers,indent=2)+'\n')
 run([javac,'-source','8','-target','8','-cp',tools,'-d',classes,HERE/'InventoryWriteScanner.java'])
 def scan(files):
  inp=t/'sources';inp.write_text('\n'.join(map(str,files))+'\n');out=run([java,'-Xmx2g','-cp',str(classes)+os.pathsep+str(tools),'InventoryWriteScanner',inp,a.classpath_file],capture_output=True);return [json.loads(l)for l in out.stdout.splitlines()],out.stderr
 controls=t/'Controls.java';controls.write_text('''import net.minecraft.entity.player.*;import net.minecraft.item.*;class Controls {
 void main(EntityPlayer p,Item item){p.inventory.mainInventory[0]=new ItemStack(item);}
 void armor(EntityPlayer p){p.inventory.armorInventory[3]=null;}
 void copy(EntityPlayer p,ItemStack s){p.inventory.mainInventory[1]=s.copy();}
 Object value(EntityPlayer p){return p.inventory.mainInventory[0]=null;}
 void nested(EntityPlayer p){Object x=(p.inventory.armorInventory[0]=null);}
 void nullable(EntityPlayer p,ItemStack s){p.inventory.mainInventory[0]=s;}
 void offhand(EntityPlayer p){p.inventory.offHandInventory[0]=null;}
 static class Lookalike{ItemStack[] mainInventory;}
 void unrelated(Lookalike p){p.mainInventory[0]=null;}
 static class Shadow extends InventoryPlayer{ItemStack[] mainInventory;Shadow(){super(null);}}
 void shadow(Shadow p){p.mainInventory[0]=null;}
 void unknown(Missing p){p.mainInventory[0]=null;}
 void read(EntityPlayer p){Object x=p.inventory.mainInventory[0];}
 void nativeWrite(EntityPlayer p){p.inventory.mainInventory.set(0,ItemStack.EMPTY);}
}''')
 rows,log=scan([controls]);assert len([r for r in rows if r['mode']=='legacy'and r['accept']])==3;assert len([r for r in rows if r['mode']=='native'and r['accept']])==1
 assert all(not r['accept']for r in rows if any(x in r['source']for x in ['p.mainInventory','mainInventory[0] = s']))
 print('PASS native javac owner, shadow, unresolved, result-used, arbitrary-nullable-RHS, offhand and discarded-write controls')
 if a.attribute:
  before_root=t/'baseline';before_root.mkdir();before_files=[];post_files=[]
  with zipfile.ZipFile(HERE/'baseline.zip')as z:
   for rel in manifest['files']:
    f=before_root/rel;f.parent.mkdir(parents=True,exist_ok=True);f.write_bytes(z.read(rel));before_files.append(f);post_files.append(ROOT/rel)
  before,blog=scan(before_files);after,alog=scan(post_files)
  assert len(before)==21 and all(r['accept']and r['mode']=='legacy'for r in before)
  current={(str(Path(r['path']).relative_to(ROOT)),r['source']):r for r in after if r['mode']=='native'}
  assert len(current)==21 and all(current[(rel,e['new'])]['accept']for rel,e in edits)
  for row in before:
   rel=str(Path(row['path']).relative_to(before_root));entry=manifest['files'][rel];old=(before_root/rel).read_bytes().decode();e=next(e for e in entry['edits']if e['old']==row['source']);assert row['start']==utf16(old[:e['start']]);assert row['owner']=='net.minecraft.entity.player.InventoryPlayer' and row['discarded'];row['path']=rel
  for row in after:row['path']=str(Path(row['path']).relative_to(ROOT))
  (HERE/'attributed-before.jsonl').write_text(''.join(json.dumps(r)+'\n'for r in before));(HERE/'attributed-after.jsonl').write_text(''.join(json.dumps(r)+'\n'for r in after))
  if not a.composed_source:
   def errors(log,base):
    result=[]
    for line in log.splitlines():
     if line.startswith('ERROR\t'):
      _,path,pos,code,message=line.split('\t',4);result.append((str(Path(path).relative_to(base)),int(pos),code,message))
    return result
   be=errors(blog,before_root);ae=errors(alog,ROOT);target={(r['path'],r['list_end'])for r in before};removed=[d for d in be if (d[0],d[1])in target and d[2]=='compiler.err.array.req.but.found'];assert len(removed)==21
   def unshift(rel,pos):
    delta=0
    for e in manifest['files'][rel]['edits']:
     start=utf16((before_root/rel).read_bytes().decode()[:e['start']]);width=utf16(e['new']);oldwidth=utf16(e['old'])
     if pos<start+delta:break
     if pos<start+delta+width:
      within=pos-start-delta
      for text in [e['list'],e['index'],e['rhs']]:
       if text=='null':continue
       ns=utf16(e['new'][:e['new'].index(text)]);os_=utf16(e['old'][:e['old'].index(text)])
       if ns<=within<ns+utf16(text):return start+os_+within-ns
      return start+min(within,oldwidth)
     delta+=width-oldwidth
    return pos-delta
   mapped=[(rel,unshift(rel,pos),code,message)for rel,pos,code,message in ae]
   assert collections.Counter(be)==collections.Counter(mapped+removed),('diagnostic changes beyond selected bracket errors',collections.Counter(be)-collections.Counter(mapped+removed),collections.Counter(mapped+removed)-collections.Counter(be))
   (HERE/'diagnostic-proof.json').write_text(json.dumps({'before_errors':len(be),'after_errors':len(ae),'removed':removed,'unchanged_after_position_inverse':len(mapped),'claim':'bounded nine-owner attribution only, not successful whole classes/build'},indent=2)+'\n')
   print('PASS exact diagnostic position attribution:',len(be),'→',len(ae),'; exactly21 selected array-required errors removed, all other diagnostic codes/messages/positions preserved after offset inverse')
  print('PASS 21 before/after exact field and set-owner attribution sites; no class generation or whole-build claim')
