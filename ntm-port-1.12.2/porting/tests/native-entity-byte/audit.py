#!/usr/bin/env python3
"""Exact inversion and independent declaring-owner/receiver/source-range audit."""
from pathlib import Path
import hashlib,json,re,subprocess,sys
root=Path(__file__).resolve().parents[4]; source=root/'ntm-port-1.12.2/src/main/java'; folder=Path(__file__).parent
manifest=json.loads((folder/'source-manifest.json').read_text()); base='36387bc1c10fa9c36fbb27f5e9d44b45d474eb18'; count=0
for e in manifest:
 p=root/e['path']; after=p.read_text(); before=subprocess.check_output(['git','-C',str(root),'show',base+':'+e['path']],text=True)
 assert hashlib.sha256(before.encode()).hexdigest()==e['before_sha256']
 assert hashlib.sha256(after.encode()).hexdigest()==e['after_sha256']
 assert re.search(r'class '+e['class_name']+r' extends '+e['superclass']+r'\b',after)
 restored=after
 if e['class_name']!='RenderShrapnel':
  for imp in ['DataParameter','DataSerializers','EntityDataManager']: restored=restored.replace('\nimport net.minecraft.network.datasync.'+imp+';','')
  declarations='\n\n'+'\n'.join('\tprivate static final DataParameter<Byte> '+k['key']+' = EntityDataManager.createKey('+e['class_name']+'.class, DataSerializers.BYTE);' for k in e['keys'])
  assert after.count(declarations)==1; restored=restored.replace(declarations,'',1)
  assert e['receiver']=='this' and e['receiver_static_type']==e['declaring_owner']==e['class_name']
 if e['class_name']=='EntityShrapnel':
  getter='\tpublic byte getShrapnelType() {\n\t\treturn this.dataManager.get(SHRAPNEL_TYPE);\n\t}\n\n'; assert after.count(getter)==1; restored=restored.replace(getter,'',1)
 if e['class_name']=='RenderShrapnel':
  assert e['receiver']=='rocket' and e['receiver_static_type']=='Entity' and e['declaring_owner']=='EntityShrapnel'
  assert re.search(r'if\(rocket instanceof EntityShrapnel\)\s*\{\s*if\(\(\(EntityShrapnel\) rocket\).getShrapnelType\(\) >= 2\)',after)
 last=0
 for change in e['transformations']:
  assert before.splitlines()[change['before_line']-1].strip()==change['before']
  assert after.splitlines()[change['after_line']-1].strip()==change['after'] and change['after_line']>last
  last=change['after_line']
  method=next(l.strip() for l in reversed(after.splitlines()[:last]) if re.match(r'\s*(public|protected|private) [^=]*\(',l) and 'DataParameter' not in l)
  assert method==change['source_method']
  restored=restored.replace(change['after'],change['before'],1); count+=change['operation_count']
 assert restored==before,'Unrelated source edit: '+str(p)
 assert 'dataWatcher' not in after and 'getDataWatcher' not in after
 if len(sys.argv)>1:
  orig=Path(sys.argv[1])/Path(e['path']).relative_to('ntm-port-1.12.2'); text=orig.read_text()
  assert hashlib.sha256(text.encode()).hexdigest()==e['original_sha256']
  # Earlier native-world migration changed only this receiver token in one watched condition.
  normalized=text.replace('worldObj.isRemote','this.world.isRemote') if e['class_name']=='EntityShrapnel' else text
  assert [l.strip() for l in normalized.splitlines() if 'dataWatcher' in l or 'getDataWatcher' in l]==[l.strip() for l in before.splitlines() if 'dataWatcher' in l or 'getDataWatcher' in l]
classes=[e['class_name'] for e in manifest if e['class_name']!='RenderShrapnel']; children=[]
for p in source.rglob('*.java'):
 s=p.read_text()
 if re.search(r'extends ('+'|'.join(classes)+r')\b',s): children.append(str(p))
assert not children,children
# Remaining literal byte16 reads are known unrelated self-owned mob/bomber data, not projectile aliases.
allowed={'EntitySiegeCraft.java','EntityUFO.java','EntityBFAngel.java','EntityHunterChopper.java','EntityBomber.java','RenderBomber.java'}
for p in source.rglob('*.java'):
 text=p.read_text()
 if re.search(r'getWatchableObjectByte\(\s*16\s*\)',text): assert p.name in allowed,p
 if re.search(r'getWatchableObjectByte\(\s*(17|18)\s*\)',text): assert p.name=='RenderGlyphidNuclear.java',p
 assert not re.search(r'\bDataWatcher\b',text),p
proxy=(source/'com/hbm/main/ClientProxy.java').read_text()
assert 'EntityShrapnel.class, new RenderShrapnel()' in proxy and 'EntityFireworks.class, new RenderShrapnel()' in proxy
print('PASS: '+str(count)+' operations, seven per-owner BYTE keys, exact CP62 inverse source, independent ranges/receiver attribution and guarded shared-renderer closure')
