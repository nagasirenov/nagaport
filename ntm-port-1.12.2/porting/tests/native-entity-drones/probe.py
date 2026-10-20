#!/usr/bin/env python3
from pathlib import Path
import re,json,hashlib,subprocess,tempfile,os
folder=Path(__file__).parent;root=folder.resolve().parents[3];src=root/'ntm-port-1.12.2/src/main/java';manifest=json.loads((folder/'source-manifest.json').read_text())
def extract(s,pattern):
 m=re.search(pattern,s);assert m,pattern
 opening=s.index('{',m.end()-1);end=opening+1;depth=1
 while depth:depth+=(s[end]=='{')-(s[end]=='}');end+=1
 return s[m.start():end]
def method(s,name):return extract(s,r'(?:public|protected) (?:void|int|byte|double) '+name+r'\([^)]*\)\s*\{')
imports='import java.util.*; import net.minecraft.entity.*; import net.minecraft.item.ItemStack; import net.minecraft.world.World; import net.minecraft.nbt.*; import net.minecraft.network.datasync.*;'
with tempfile.TemporaryDirectory(prefix='native-drones-') as temp:
 t=Path(temp);units=[]
 for e in manifest['entries']:
  if e['class_name'] not in ['EntityDroneBase','EntityRequestDrone','EntityDeliveryDrone']:continue
  raw=(root/e['path']).read_bytes();assert hashlib.sha256(raw).hexdigest()==e['after_sha256'];s=raw.decode().replace('\r\n','\n');name=e['class_name'];parts=re.findall(r'(?:protected|private) static final DataParameter<Byte> [^;]+;',s)+[extract(s,r'public '+name+r'\(World world\)\s*\{')]
  if name=='EntityDroneBase':
   parts += re.findall(r'public double target\w+ = [^;]+;',s)+[method(s,n) for n in ['entityInit','setAppearance','getAppearance','setTarget','writeEntityToNBT','readEntityFromNBT']]
  else:
   if name=='EntityRequestDrone':
    parts += [re.search(r'public ItemStack heldItem;',s).group(),re.search(r'public List program = [^;]+;',s).group(),re.search(r'int nextActionTimer = [^;]+;',s).group(),method(s,'setTarget')]
   else:
    parts += [re.search(r'protected ItemStack\[\] slots = [^;]+;',s).group(),re.search(r'protected boolean chunkLoading = [^;]+;',s).group(),method(s,'getSizeInventory')]+[method(s,n) for n in ['entityInit','getSpeed','getLoad','setLoad']]
   for op,direction in [('read','From'),('write','To')]:
    full=method(s,op+'Entity'+direction+'NBT');lines=[l for l in full.splitlines() if 'super.'+op+'Entity' in l or 'this.dataManager.' in l];assert len(lines)==2
    # Bounded, noncontiguous scalar calls explicitly exclude held-item/program/inventory/fluid and chunk ticket work.
    parts.append('public void '+op+'ScalarCalls(NBTTagCompound nbt) {'+'\n'.join(lines)+'}')
    parts.append('protected void '+op+'Entity'+direction+'NBT(NBTTagCompound nbt) {throw new AssertionError("Full payload/ticket NBT outside scalar probe");}')
  p=t/(name+'.java');p.write_text(imports+'\n'+('abstract ' if name=='EntityDroneBase' else '')+'class '+name+' extends '+('Entity' if name=='EntityDroneBase' else 'EntityDroneBase')+' {\n'+'\n'.join(parts)+'\n}');units.append(str(p))
 item=next(src.rglob('ItemDrone.java')).read_text();setter=next(l.strip() for l in item.splitlines() if ').setLoad(' in l)
 render=next(src.rglob('RenderDeliveryDrone.java')).read_text();branch=extract(render,r'if\(entity instanceof EntityRequestDrone\)\s*\{');assert 'else if(((EntityDeliveryDrone) entity).getLoad() == 1)' in render
 # Native renderer expression tested with its original exclusive request guard; texture calls themselves need GL and are out of scope.
 (t/'Consumers.java').write_text(imports+'class Consumers { static void express(Entity toSpawn) {'+setter+'} static boolean expressTexture(Entity entity) {if(entity instanceof EntityRequestDrone) {return false;} else if(((EntityDeliveryDrone) entity).getLoad() == 1) return true;return false;} }');units.append(str(t/'Consumers.java'))
 (t/'Probe.java').write_text((folder/'Probe.java').read_text());units.append(str(t/'Probe.java'))
 cp=Path(os.environ.get('NAGAPORT_NATIVE_CP_FILE','/tmp/nagaport-ae2-native-classpath.txt')).read_text().strip();java=Path(os.environ.get('JAVA_HOME','/workspace/shared/nagaport-toolchain/java8'))/'bin'
 subprocess.run([str(java/'javac'),'-cp',cp,'-d',temp]+units,check=True);subprocess.run([str(java/'java'),'-cp',temp+':'+cp,'Probe'],check=True)
 print('PASS: actual World constructor/init hierarchy, complete base NBT, explicit child scalar calls; payload/chunk-ticket operations not simulated')
