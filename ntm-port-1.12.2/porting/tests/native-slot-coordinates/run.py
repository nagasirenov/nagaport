from pathlib import Path
import subprocess,tempfile,zipfile,struct,hashlib,os,json,re,io
import check_source
H=Path(__file__).resolve().parent;R=H.parents[2];J=Path('/workspace/shared/nagaport-toolchain/java8');CPF=Path('/workspace/shared/nagaport-toolchain/native-classpath.txt');CP=CPF.read_text().strip();OLD=Path('/tmp/nagaport-legacy-client.jar');MOD=Path('/workspace/shared/nagaport/mods1710/HBM-NTM-.1.0.27_X5645_H261.jar')
assert hashlib.sha256(OLD.read_bytes()).hexdigest()=='a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce'
assert hashlib.sha256(MOD.read_bytes()).hexdigest()=='3f5629077628b5956ff7ff4e254c25ef0e1bc51f5b6668fde77badd5f784f67b'
forge=Path('/tmp/nagaport-forge1614-userdev.jar');assert hashlib.sha256(forge.read_bytes()).hexdigest()=='6ed364c0defdb481a41ef2d99c02d382f16fc42a6ea0ea1c2b849848d8466e91'
with zipfile.ZipFile(forge) as z:
 for name in ['forgepatches.zip','fmlpatches.zip']:
  with zipfile.ZipFile(io.BytesIO(z.read(name))) as q:
   for f in q.namelist():
    if f.endswith('/Slot.java.patch'):
     text=q.read(f).decode();changed=[line for line in text.splitlines() if line.startswith(('+','-')) and not line.startswith(('+++','---'))];assert not any('field_75223_e' in line or 'field_75221_f' in line for line in changed)
     (H/'original-forge-slot.patch').write_text(text)
srg=zipfile.ZipFile('/tmp/nagaport1710-srg.zip').read('joined.srg').decode()
for line in ['FD: aay/h net/minecraft/inventory/Slot/field_75223_e','FD: aay/i net/minecraft/inventory/Slot/field_75221_f','MD: aay/a (Ladd;)Z net/minecraft/inventory/Slot/func_75214_a (Lnet/minecraft/item/ItemStack;)Z']:assert line in srg,line
# Symbolic linkage only: retain all method instructions and non-pool class data exactly.
def relink(b):
 assert b[:4]==b'\xca\xfe\xba\xbe';count=struct.unpack('>H',b[8:10])[0];out=bytearray(b[:10]);at=10;i=1;changes=[]
 maps={'com/hbm/inventory/SlotLayer':'LegacySlotLayer','net/minecraft/inventory/Slot':'aay','net/minecraft/inventory/IInventory':'rb','net/minecraft/item/ItemStack':'add','field_75221_f':'i','func_75214_a':'a'}
 while i<count:
  tag=b[at];out.append(tag);at+=1
  if tag==1:
   size=struct.unpack('>H',b[at:at+2])[0];at+=2;s=b[at:at+size].decode();at+=size;t=s
   for old,new in maps.items():t=t.replace(old,new)
   if t!=s:changes.append([s,t])
   z=t.encode();out+=struct.pack('>H',len(z))+z
  else:
   size={3:4,4:4,5:8,6:8,7:2,8:2,9:4,10:4,11:4,12:4,15:3,16:2,18:4}[tag];out+=b[at:at+size];at+=size
   if tag in (5,6):i+=1
  i+=1
 out+=b[at:];assert bytes(out[-len(b[at:]):])==b[at:]
 (H/'original-linkage.json').write_text(json.dumps({'original_class_sha256':hashlib.sha256(b).hexdigest(),'unchanged_post_constant_pool_sha256':hashlib.sha256(b[at:]).hexdigest(),'symbol_changes':changes},indent=2)+'\n');return out
check_source.main()
def run(c):subprocess.run(list(map(str,c)),check=True)
with tempfile.TemporaryDirectory(prefix='native-slot-coordinates-') as temp:
 d=Path(temp);unsigned=d/'legacy.jar'
 with zipfile.ZipFile(OLD) as z,zipfile.ZipFile(unsigned,'w') as out:
  for i in z.infolist():
   if i.filename.upper().startswith('META-INF/') and i.filename.upper().endswith(('.SF','.RSA','.DSA')):continue
   out.writestr(i,z.read(i.filename))
 original=zipfile.ZipFile(MOD).read('com/hbm/inventory/SlotLayer.class');(d/'LegacySlotLayer.class').write_bytes(relink(original))
 cp=str(d)+os.pathsep+CP+os.pathsep+str(unsigned)
 source=R/'src/main/java/com/hbm/inventory/SlotLayer.java'
 run([J/'bin/javac','-cp',cp,'-d',d,source,H/'NativeSlotTest.java']);run([J/'bin/java','-cp',cp,'NativeSlotTest'])
 for old,new in [('this.yPos = 9999','this.yPos = 9998'),('this.yPos = originalYPosition','this.yPos = originalYPosition + 1'),('if(this.layer == layer)','if(this.layer != layer)')]:
  s=source.read_text();assert old in s;p=d/'SlotLayer.java';p.write_text(s.replace(old,new));run([J/'bin/javac','-cp',cp,'-d',d,p]);r=subprocess.run([str(J/'bin/java'),'-cp',cp,'NativeSlotTest'],capture_output=True,text=True);assert r.returncode!=0 and 'AssertionError' in r.stderr,r.stderr
 print('PASS three executable SlotLayer semantic mutants rejected')
