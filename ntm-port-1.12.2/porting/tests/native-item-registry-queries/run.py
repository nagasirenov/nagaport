#!/usr/bin/env python3
"""Offline proof: run.py JAVA_HOME CLASSPATH_FILE OLD_CLIENT_JAR OLD_SRG_ZIP FIELDS_CSV"""
from pathlib import Path
import sys,subprocess,tempfile,json,hashlib,zipfile
here=Path(__file__).resolve().parent; root=here.parents[3]
java,cpf,old,srg,fields=map(Path,sys.argv[1:]); cp=cpf.read_text().strip();m=json.loads((here/'manifest.json').read_text())
def run(a):
 p=subprocess.run(list(map(str,a)),capture_output=True,text=True);assert p.returncode==0,p.stdout+p.stderr;return p.stdout
assert 'MD: cw/c (Ljava/lang/Object;)Ljava/lang/String; net/minecraft/util/RegistryNamespaced/func_148750_c (Ljava/lang/Object;)Ljava/lang/String;' in zipfile.ZipFile(srg).read('joined.srg').decode()
assert 'FD: adb/e net/minecraft/item/Item/field_150901_e' in zipfile.ZipFile(srg).read('joined.srg').decode()
assert 'field_150901_e,REGISTRY,' in fields.read_text()
assert hashlib.sha256(old.read_bytes()).hexdigest()=='a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce'
with tempfile.TemporaryDirectory(prefix='item-registry-query-') as td:
 t=Path(td);sources=[]
 for n,f in enumerate(m['files']):
  a=subprocess.check_output(['git','-C',str(root),'show',m['baseline']+':'+f['path']]);assert hashlib.sha256(a).hexdigest()==f['before_sha256'];b=a.decode();delta=0;reverse=[]
  for e in f['edits']:
   assert b[e['start']:e['end']]==e['old'];reverse.append((e['start']+delta,e['start']+delta+len(e['new']),e['old']));delta+=len(e['new'])-len(e['old'])
  for e in reversed(f['edits']):b=b[:e['start']]+e['new']+b[e['end']:]
  assert b.encode()==(root/f['path']).read_bytes();assert hashlib.sha256(b.encode()).hexdigest()==f['after_sha256']
  for s,e,oldtext in reversed(reverse):b=b[:s]+oldtext+b[e:]
  assert b.encode()==a
  d=t/str(n);d.mkdir();p=d/Path(f['path']).name;p.write_bytes(a);sources.append(p)
 run([java/'bin/javac','-cp',java/'lib/tools.jar','-d',t,here/'ItemRegistryOwners.java'])
 attributed=run([java/'bin/java','-cp',str(t)+':'+str(java/'lib/tools.jar'),'ItemRegistryOwners',cp]+sources)
 positions={(p,int(s),int(e)) for p,s,e,_ in (l.split('\t') for l in attributed.splitlines())}
 for p,f in zip(sources,m['files']):
  for e in f['edits']: assert (str(p),e['start']+5,e['start']+17) in positions
 controls=t/'Controls.java';controls.write_text('class Controls { static class Item {static Object itemRegistry;} Object a=Item.itemRegistry; net.minecraft.item.Item value; Object b=value.itemRegistry; Object c=net.minecraft.item.Item.itemRegistry; String s="Item.itemRegistry"; }')
 assert len(run([java/'bin/java','-cp',str(t)+':'+str(java/'lib/tools.jar'),'ItemRegistryOwners',cp,controls]).splitlines())==1
 unsigned=t/'old.jar'
 with zipfile.ZipFile(old) as a,zipfile.ZipFile(unsigned,'w') as b:
  for z in a.infolist():
   if not z.filename.startswith('META-INF/'):b.writestr(z,a.read(z))
 runtime=cp+':'+str(unsigned)
 # Compile verbatim complete selected serialization statements, not replacement platform types.
 production=root/'ntm-port-1.12.2/src/main/java/com/hbm'
 nbt=next(l.strip() for l in (production/'saveddata/AnnihilatorSavedData.java').read_text().splitlines() if 'nbt.setString("item", java.util.Objects.toString(Item.REGISTRY.getNameForObject(item)' in l)
 js=next(l.strip() for l in (production/'config/ItemPoolConfigJSON.java').read_text().splitlines() if 'writer.value(java.util.Objects.toString(Item.REGISTRY.getNameForObject(stack.getItem())' in l)
 generated=t/'ProductionSavedNames.java'
 generated.write_text('import net.minecraft.item.*;import net.minecraft.nbt.*;import com.google.gson.stream.*;class ProductionSavedNames { static NBTTagCompound nbt(Item item){NBTTagCompound nbt=new NBTTagCompound();'+nbt+'return nbt;} static void json(JsonWriter writer,ItemStack stack)throws Exception{'+js+'}}')
 run([java/'bin/javac','-cp',runtime,'-d',t,generated,here/'RegistryQueryTest.java'])
 # A blind field-token edit cannot satisfy the genuine String sink.
 bad=t/'BadName.java';bad.write_text('import net.minecraft.item.*;class BadName {String x=Item.REGISTRY.getNameForObject(new Item());}')
 assert subprocess.run([str(java/'bin/javac'),'-cp',cp,'-d',str(t),str(bad)],capture_output=True).returncode!=0
 # Each selected edit is independently source guarded; reverting any single edit is rejected.
 for f in m['files']:
  current=(root/f['path']).read_bytes().decode();offset=0
  for e in f['edits']:
   pos=e['start']+offset;mutated=current[:pos]+e['old']+current[pos+len(e['new']):]
   assert hashlib.sha256(mutated.encode()).hexdigest()!=f['after_sha256'];offset+=len(e['new'])-len(e['old'])
 print(run([java/'bin/java','-cp',str(t)+':'+runtime,'RegistryQueryTest']))
print('PASS exact source reversal, class-qualifier attribution and shadow/value/string negative controls')
