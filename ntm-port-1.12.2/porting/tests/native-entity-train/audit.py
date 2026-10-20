#!/usr/bin/env python3
"""Recovered evidence: exact migration reversal, original calls, native-key ownership and closure."""
from pathlib import Path
import re,json,hashlib,subprocess,sys
folder=Path(__file__).resolve().parent;root=folder.parents[3];manifest=json.loads((folder/'source-manifest.json').read_text());prefix='ntm-port-1.12.2/src/main/java/'
def source(path):
 p=root/path
 return p.read_bytes() if p.exists() else subprocess.check_output(['git','-C',str(root),'show',manifest['base']+':'+path])
def calls(s):
 result=[]
 for m in re.finditer(r'this\.dataWatcher\.\w+\(',s):
  end=m.end();depth=1
  while depth:depth+=(s[end]=='(')-(s[end]==')');end+=1
  result.append(s[m.start():end])
 return result
count=0
for e in manifest['entries']:
 after=source(e['path']).decode();restored=after
 assert hashlib.sha256(after.encode()).hexdigest()==e['after_sha256']
 for insertion in e['insertions']:assert restored.count(insertion)==1;restored=restored.replace(insertion,'',1)
 for t in e['transformations']:
  assert after[t['after_offset']:].startswith(t['after'])
  assert after.count('\n',0,t['after_offset'])+1==t['after_line']
  assert t['declaring_owner']==e['owner']==t['receiver_static_type']
  assert after.count(t['after'])==1;restored=restored.replace(t['after'],t['before'],1);count+=1
 assert hashlib.sha256(restored.encode()).hexdigest()==e['before_sha256'],'Unrelated production changes: '+e['path']
 before=restored
 for t in e['transformations']:
  assert before[t['before_offset']:].startswith(t['before'])
  assert before.count('\n',0,t['before_offset'])+1==t['before_line']
 # Baseline byte hashes remain sufficient if the recovered temporary tree is not present in a recipient checkout.
 if subprocess.run(['git','-C',str(root),'cat-file','-e',manifest['base']+':'+e['path']],stdout=subprocess.DEVNULL,stderr=subprocess.DEVNULL).returncode==0:
  assert before==subprocess.check_output(['git','-C',str(root),'show',manifest['base']+':'+e['path']]).decode()
 assert calls(before)==[t['before'] for t in e['transformations']];assert 'dataWatcher' not in after
 if len(sys.argv)>1:
  original=(Path(sys.argv[1])/Path(e['path']).relative_to('ntm-port-1.12.2')).read_bytes().decode()
  assert hashlib.sha256(original.encode()).hexdigest()==e['original_sha256'];assert calls(original)==calls(before)
for e in manifest['unchanged_hierarchy_sources']:assert hashlib.sha256(source(e['path'])).hexdigest()==e['sha256']
base=source(prefix+'com/hbm/entity/train/EntityRailCarBase.java').decode();cargo=source(prefix+'com/hbm/entity/train/EntityRailCarCargo.java').decode();ride=source(prefix+'com/hbm/entity/train/EntityRailCarRidable.java').decode();electric=source(prefix+'com/hbm/entity/train/EntityRailCarElectric.java').decode()
for outer,owner,fields in [(base,'BoundingBoxDummyEntity',[('TRAIN_ID','Integer','VARINT'),('WIDTH','Float','FLOAT'),('HEIGHT','Float','FLOAT')]),(ride,'SeatDummyEntity',[('TRAIN_ID','Integer','VARINT'),('SEAT_INDEX','Integer','VARINT')]),(electric,'EntityRailCarElectric',[('POWER','Integer','VARINT')])]:
 for key,typ,serializer in fields:assert 'private static final DataParameter<'+typ+'> '+key+' = EntityDataManager.createKey('+owner+'.class, DataSerializers.'+serializer+');' in outer
assert 'class BoundingBoxDummyEntity extends Entity implements ILookOverlay' in base
assert 'class SeatDummyEntity extends Entity' in ride
assert 'super.entityInit();\n\t\tthis.dataManager.register(OCCUPIED_SLOTS, 0);' in cargo
assert 'super.entityInit();\n\t\tthis.dataManager.register(POWER, new Integer(0));' in electric
assert 'entityInit()' not in ride.split('public static class SeatDummyEntity')[0]
refs=['EntityRailCarBase','EntityRailCarCargo','EntityRailCarRidable','EntityRailCarElectric','BoundingBoxDummyEntity','SeatDummyEntity']
paths=subprocess.check_output(['git','-C',str(root),'ls-files','--cached','--others','--exclude-standard','--',prefix]).decode().splitlines();closure=[];children=[]
for path in sorted(set(paths)):
 if not path.endswith('.java'):continue
 s=source(path).decode()
 if re.search(r'\bclass \w+ extends (?:'+'|'.join(refs)+r')\b',s):children.append(Path(path).name)
 if path.startswith(prefix+'com/hbm/entity/train/'):assert not re.search(r'dataWatcher|getDataWatcher',s)
 if any(n in s for n in refs):closure.append({'path':path,'references':[{'line':i+1,'source':line.strip()} for i,line in enumerate(s.splitlines()) if any(n in line for n in refs)]})
assert sorted(children)==['EntityRailCarCargo.java','EntityRailCarElectric.java','EntityRailCarRidable.java','TrainCargoTram.java','TrainCargoTramTrailer.java'],children
if '--write-closure' in sys.argv:(folder/'consumer-closure.json').write_text(json.dumps(closure,indent=2)+'\n')
expected=json.loads((folder/'consumer-closure.json').read_text())
def domains(entries):return {e['path']:sorted(set(n for r in e['references'] for n in refs if n in r['source'])) for e in entries}
assert domains(closure)==domains(expected),'Named family consumer domains changed'
for e in closure:
 s=source(e['path']).decode()
 for alias in re.findall(r'\b(?:'+ '|'.join(refs)+r')\s+(\w+)\s*(?:[=;,\)])',s):
  assert not re.search(r'\b'+re.escape(alias)+r'\.(?:dataWatcher\b|getDataWatcher\(\))',s),'Untyped train metadata consumer: '+e['path']
assert 'readEntityFromNBT' not in electric and 'writeEntityToNBT' not in electric
assert 'return true;\n\t\t//return this.getPower() >= this.getPowerConsumption();' in electric
assert '//this.setPower(this.getPower() - this.getPowerConsumption());' in electric
print('PASS: '+str(count)+' original calls; exact full-file inverse against recovered CP76 tree; six owner keys; five descendants; '+str(len(closure))+' consumer domains; unchanged formulas/constructors/NBT')
