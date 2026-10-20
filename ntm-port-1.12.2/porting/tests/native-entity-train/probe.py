#!/usr/bin/env python3
"""Reconstructed native probe. Test-only bounded method slices, never replacement Minecraft APIs."""
from pathlib import Path
import re,json,hashlib,subprocess,tempfile,os
folder=Path(__file__).resolve().parent;root=folder.parents[3];src=root/'ntm-port-1.12.2/src/main/java';manifest=json.loads((folder/'source-manifest.json').read_text())
for e in manifest['entries']+manifest['unchanged_hierarchy_sources']:assert hashlib.sha256((root/e['path']).read_bytes()).hexdigest()==e.get('after_sha256',e.get('sha256'))
def extract(s,pattern):
 m=re.search(pattern,s);assert m,pattern
 opening=s.index('{',m.end()-1);end=opening+1;depth=1
 while depth:depth+=(s[end]=='{')-(s[end]=='}');end+=1
 return s[m.start():end]
def method(s,name):return extract(s,r'(?:public|protected) (?:void|int|float|boolean|String|Vec3d\[\]) '+name+r'\([^)]*\)\s*\{')
def ctor(s,name,args):return extract(s,r'public '+name+r'\('+args+r'\)\s*\{')
def keys(s):return re.findall(r'private static final DataParameter<\w+> [^;]+;',s)
imports='package com.hbm.entity.train;\nimport net.minecraft.entity.*; import net.minecraft.world.World; import net.minecraft.nbt.*; import net.minecraft.network.datasync.*; import net.minecraft.util.math.Vec3d; import net.minecraft.item.ItemStack;\n'
base=(src/'com/hbm/entity/train/EntityRailCarBase.java').read_text();ridable=(src/'com/hbm/entity/train/EntityRailCarRidable.java').read_text()
bound=extract(base,r'public static class BoundingBoxDummyEntity[^\n]*\{');seat=extract(ridable,r'public static class SeatDummyEntity[^\n]*\{')
def dummy(s,name,parent):
 parts=keys(s)+re.findall(r'(?:private (?:int|double)|public EntityRailCar\w+) \w+;',s)
 parts += [ctor(s,name,'World world'),ctor(s,name,'World world, '+parent+' train, '+('float width, float height' if name=='BoundingBoxDummyEntity' else 'int index'))]
 parts += [method(s,n) for n in ['entityInit','writeEntityToNBT','writeToNBTOptional','readEntityFromNBT','onUpdate']]
 if name=='BoundingBoxDummyEntity':parts += [method(s,n) for n in ['setSize','canBePushed','canBeCollidedWith']]
 for key in ['TRAIN_ID']+(['SEAT_INDEX'] if name=='SeatDummyEntity' else []):
  expression='this.dataManager.get('+key+')';assert expression in s
  parts.append('public int probeRead'+key+'() { return '+expression+'; }')
 return 'public static class '+name+' extends Entity {\n'+'\n'.join(parts)+'\n}'
cpfile=Path(os.environ.get('NAGAPORT_NATIVE_CP_FILE','/tmp/nagaport-ae2-native-classpath.txt'))
if not cpfile.exists():raise SystemExit('BLOCKED: native classpath file missing: '+str(cpfile)+'; no native validation executed')
cp=cpfile.read_text().strip();java=Path(os.environ.get('JAVA_HOME','/workspace/shared/nagaport-toolchain/java8'))/'bin'
if not (java/'javac').exists():raise SystemExit('BLOCKED: Java8 javac missing: '+str(java)+'; no native validation executed')
with tempfile.TemporaryDirectory(prefix='native-train-') as temp:
 t=Path(temp);units=[]
 def unit(name,text):
  p=t/(name+'.java');p.write_text(text);units.append(str(p))
 unit('EntityRailCarBase',imports+'public abstract class EntityRailCarBase extends Entity {\n'+ctor(base,'EntityRailCarBase','World world')+'\n'+'\n'.join(method(base,n) for n in ['entityInit','writeEntityToNBT','readEntityFromNBT'])+'\n'+dummy(bound,'BoundingBoxDummyEntity','EntityRailCarBase')+'\n}')
 unit('EntityRailCarCargo',(src/'com/hbm/entity/train/EntityRailCarCargo.java').read_text())
 parts=[ctor(ridable,'EntityRailCarRidable','World world'),'public double engineSpeed;','public SeatDummyEntity[] passengerSeats;','public abstract Vec3d[] getPassengerSeats();','public abstract boolean canAccelerate();','public void consumeFuel() { }',dummy(seat,'SeatDummyEntity','EntityRailCarRidable')]
 unit('EntityRailCarRidable',imports+'public abstract class EntityRailCarRidable extends EntityRailCarCargo {\n'+'\n'.join(parts)+'\n}')
 unit('EntityRailCarElectric',(src/'com/hbm/entity/train/EntityRailCarElectric.java').read_text())
 for name,parent in [('TrainCargoTram','EntityRailCarElectric'),('TrainCargoTramTrailer','EntityRailCarCargo')]:
  s=(src/('com/hbm/entity/train/'+name+'.java')).read_text();methods=['getSizeInventory','getInventoryName']+(['getPassengerSeats','getMaxPower','getPowerConsumption','hasChargeSlot','getChargeSlot'] if name=='TrainCargoTram' else [])
  unit(name,imports+'public class '+name+' extends '+parent+' {\n'+ctor(s,name,'World world')+'\n'+'\n'.join(method(s,n) for n in methods)+'\n}')
 # Sole registry collaborator needed to compile complete Electric; battery/rail tick is not executed.
 unit('ModItems','package com.hbm.items; public class ModItems { public static net.minecraft.item.Item battery_creative; }')
 path='ntm-port-1.12.2/src/main/java/api/hbm/energymk2/IBatteryItem.java';battery=(root/path).read_text() if (root/path).exists() else subprocess.check_output(['git','-C',str(root),'show',manifest['base']+':'+path]).decode()
 unit('IBatteryItem',battery);unit('Probe',(folder/'Probe.java').read_text())
 subprocess.run([str(java/'javac'),'-cp',cp,'-d',temp]+units,check=True)
 for order in ['train-first','dummies-first']:subprocess.run([str(java/'java'),'-cp',temp+':'+cp,'com.hbm.entity.train.Probe',order],check=True)
 print('PASS: both fresh-JVM load orders; exact constructor/init chain and dummy ticks; full Cargo NBT; bounded ancestors, not full train/game compilation')
