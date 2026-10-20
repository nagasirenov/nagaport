#!/usr/bin/env python3
from pathlib import Path
import re,json,hashlib,subprocess,sys
root=Path(__file__).resolve().parents[4];folder=Path(__file__).parent;src=root/'ntm-port-1.12.2/src/main/java';manifest=json.loads((folder/'source-manifest.json').read_text())
def calls(s):
 result=[]
 for m in re.finditer(r'(?:this\.)?(?:dataWatcher|getDataWatcher\(\))\.\w+\(',s):
  end=m.end();depth=1
  while depth:depth+=(s[end]=='(')-(s[end]==')');end+=1
  result.append(s[m.start():end])
 return result
count=0
for e in manifest['entries']:
 before=subprocess.check_output(['git','-C',str(root),'show',manifest['base']+':'+e['path']]).decode();after=(root/e['path']).read_bytes().decode();restored=after
 for label,s in [('before',before),('after',after)]:assert hashlib.sha256(s.encode()).hexdigest()==e[label+'_sha256']
 for insertion in e['insertions']:assert restored.count(insertion)==1;restored=restored.replace(insertion,'',1)
 for t in e['transformations']:
  assert before[t['before_offset']:].startswith(t['before']) and after[t['after_offset']:].startswith(t['after'])
  method=next(l.strip() for l in reversed(after[:t['after_offset']].splitlines()) if re.match(r'\s*(public|protected|private) [^=]*\(',l) and 'DataParameter' not in l);assert method==t['method']
  owner='EntityDroneBase' if t['key']=='APPEARANCE' else 'EntityDeliveryDrone';assert t['declaring_owner']==owner
  if e['class_name'] not in ['ItemDrone','RenderDeliveryDrone']:assert t['receiver_static_type']==e['class_name'] and t['after'].startswith('this.dataManager.')
  elif e['class_name']=='ItemDrone':assert t['receiver_static_type']=='EntityDeliveryDrone' and '((EntityDeliveryDrone) toSpawn)' in t['after']
  restored=restored.replace(t['after'],t['before'],1);count+=1
 assert restored==before,'Unrelated bytes changed: '+e['path'];assert not calls(after)
 if len(sys.argv)>1:
  original=(Path(sys.argv[1])/Path(e['path']).relative_to('ntm-port-1.12.2')).read_bytes().decode();assert hashlib.sha256(original.encode()).hexdigest()==e['original_sha256'];assert calls(original)==calls(before)
base=next(src.rglob('EntityDroneBase.java')).read_text();request=next(src.rglob('EntityRequestDrone.java')).read_text();delivery=next(src.rglob('EntityDeliveryDrone.java')).read_text()
assert 'protected static final DataParameter<Byte> APPEARANCE = EntityDataManager.createKey(EntityDroneBase.class, DataSerializers.BYTE)' in base
assert 'private static final DataParameter<Byte> LOAD = EntityDataManager.createKey(EntityDeliveryDrone.class, DataSerializers.BYTE)' in delivery
assert 'entityInit(' not in request and 'DataParameter' not in request
assert 'super.entityInit();\n\t\tthis.dataManager.register(LOAD, new Byte((byte) 0));' in delivery
assert 'class EntityDroneBase extends Entity {' in base
renderer=next(src.rglob('RenderDeliveryDrone.java')).read_text();proxy=(src/'com/hbm/main/ClientProxy.java').read_text()
assert 'if(entity instanceof EntityRequestDrone)' in renderer and 'else if(((EntityDeliveryDrone) entity).getLoad() == 1)' in renderer
assert proxy.count('new RenderDeliveryDrone(')==2
for owner in ['EntityRequestDrone','EntityDeliveryDrone']:assert 'registerEntityRenderingHandler('+owner+'.class, renderManager -> new RenderDeliveryDrone(renderManager))' in proxy
closure=[];children=[]
for p in src.rglob('*.java'):
 s=p.read_text()
 if re.search(r'extends (EntityDroneBase|EntityRequestDrone|EntityDeliveryDrone)\b',s):children.append(p.name)
 if any(n in s for n in ['EntityDroneBase','EntityRequestDrone','EntityDeliveryDrone']):closure.append({'path':str(p.relative_to(root)),'references':[{'line':i+1,'source':l.strip()} for i,l in enumerate(s.splitlines()) if any(n in l for n in ['EntityDroneBase','EntityRequestDrone','EntityDeliveryDrone'])],'legacy_calls':calls(s)})
assert sorted(children)==['EntityDeliveryDrone.java','EntityRequestDrone.java']
assert closure==json.loads((folder/'consumer-closure.json').read_text())
# All direct family consumers, including renderers, use typed methods after this migration.
for e in closure:assert not e['legacy_calls'],e['path']
print('PASS: '+str(count)+' original calls; byte-exact inverse including mixed CRLF; inherited/private owner keys; exact receiver/method ranges; complete named-consumer closure')
