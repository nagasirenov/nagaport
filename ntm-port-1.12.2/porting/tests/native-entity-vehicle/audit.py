#!/usr/bin/env python3
from pathlib import Path
import hashlib,json,re,subprocess,sys
root=Path(__file__).resolve().parents[4]; source=root/'ntm-port-1.12.2/src/main/java'; folder=Path(__file__).parent; manifest=json.loads((folder/'source-manifest.json').read_text())
base='36387bc1c10fa9c36fbb27f5e9d44b45d474eb18'; count=0
def calls(text):
 result=[]
 for m in re.finditer(r'(?:\b\w+\.)?(?:dataWatcher|getDataWatcher\(\))\.\w+\(',text):
  end=m.end();depth=1
  while depth: depth+=(text[end]=='(')-(text[end]==')');end+=1
  result.append(text[m.start():end])
 return result
for e in manifest:
 p=root/e['path']; after=p.read_text(); before=subprocess.check_output(['git','-C',str(root),'show',base+':'+e['path']],text=True)
 assert hashlib.sha256(before.encode()).hexdigest()==e['before_sha256'] and hashlib.sha256(after.encode()).hexdigest()==e['after_sha256']
 assert re.search(r'class '+e['class_name']+r' extends '+e['superclass']+r'\b',after)
 restored=after
 if e['keys']:
  for imp in ['DataParameter','DataSerializers','EntityDataManager']:restored=restored.replace('\nimport net.minecraft.network.datasync.'+imp+';','')
  declarations='\n\n'+'\n'.join('\tprivate static final DataParameter<'+k['java_type']+'> '+k['key']+' = EntityDataManager.createKey('+e['class_name']+'.class, DataSerializers.'+k['serializer']+');' for k in e['keys'])
  assert restored.count(declarations)==1;restored=restored.replace(declarations,'',1)
 if e['class_name']=='EntityMinerRocket':
  accessors='\tpublic int getFlightMode() {\n\t\treturn this.dataManager.get(FLIGHT_MODE);\n\t}\n\n\tpublic int getSatelliteFrequency() {\n\t\treturn this.dataManager.get(SATELLITE_FREQUENCY);\n\t}\n\n\tpublic void setSatelliteFrequency(int frequency) {\n\t\tthis.dataManager.set(SATELLITE_FREQUENCY, frequency);\n\t}\n\n'
  assert restored.count(accessors)==1;restored=restored.replace(accessors,'',1)
 if e['class_name']=='RenderSoyuz':restored=restored.replace('\nimport com.hbm.entity.missile.EntitySoyuz;','',1)
 for c in e['transformations']:
  assert before.splitlines()[c['before_line']-1].strip()==c['before'] and after.splitlines()[c['after_line']-1].strip()==c['after']
  method=next(l.strip() for l in reversed(after.splitlines()[:c['after_line']]) if re.match(r'\s*(public|protected|private) [^=]*\(',l) and 'DataParameter' not in l)
  assert method==c['source_method']
  if e['keys']:assert c['declaring_owner']==c['receiver_static_type']==e['class_name']
  restored=restored.replace(c['after'],c['before'],1);count+=c['operation_count']
 assert restored==before,'Unrelated source changed: '+str(p)
 assert 'dataWatcher' not in after and 'getDataWatcher' not in after
 if len(sys.argv)>1:
  original=(Path(sys.argv[1])/Path(e['path']).relative_to('ntm-port-1.12.2')).read_text()
  assert hashlib.sha256(original.encode()).hexdigest()==e['original_sha256']
  assert calls(original)==calls(before),'Original call mismatch: '+str(p)
# Verify external static receiver evidence and current registration domains, independently.
dock=(source/'com/hbm/tileentity/machine/TileEntityMachineSatDock.java').read_text()
assert 'EntityMinerRocket rocket = new EntityMinerRocket(this.world)' in dock
assert 'for(EntityMinerRocket rocket : list)' in dock
proxy=(source/'com/hbm/main/ClientProxy.java').read_text();assert 'registerEntityRenderingHandler(EntitySoyuz.class, renderManager -> new RenderSoyuz(renderManager))' in proxy
owners=[e['class_name'] for e in manifest if e['keys']]
for p in source.rglob('*.java'):
 text=p.read_text()
 assert not re.search(r'extends ('+'|'.join(owners)+r')\b',text),p
 assert not re.search(r'\bDataWatcher\b',text),p
print('PASS: '+str(count)+' exact operations, seven owner keys, original invocation parity, independent source ranges/receiver domains and byte-exact CP62 inverse source')
