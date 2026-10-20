#!/usr/bin/env python3
"""Independent owner/consumer attribution plus exact inverse production guard."""
from pathlib import Path
import hashlib,json,re,subprocess,sys
root=Path(__file__).resolve().parents[4]; source=root/'ntm-port-1.12.2/src/main/java'
folder=Path(__file__).parent; manifest=json.loads((folder/'source-manifest.json').read_text())
base='c22f7048a82784fe0e8246d38aef102c43f1df70'; count=0
for e in manifest:
 p=root/e['path']; after=p.read_text(); before=subprocess.check_output(['git','-C',str(root),'show',base+':'+e['path']],text=True)
 assert hashlib.sha256(before.encode()).hexdigest()==e['before_sha256']
 assert hashlib.sha256(after.encode()).hexdigest()==e['after_sha256']
 assert re.search(r'class '+e['class_name']+r' extends '+e['superclass']+r'\s*\{',after)
 restored=after
 if e['class_name']=='EntityBlackHole':
  for imp in ['DataParameter','DataSerializers','EntityDataManager']: restored=restored.replace('\nimport net.minecraft.network.datasync.'+imp+';','')
  declaration='\n\n\tprotected static final DataParameter<Float> BLACK_HOLE_SIZE = EntityDataManager.createKey(EntityBlackHole.class, DataSerializers.FLOAT);'
  assert after.count(declaration)==1; restored=restored.replace(declaration,'',1)
  getter='\tpublic float getBlackHoleSize() {\n\t\treturn this.dataManager.get(BLACK_HOLE_SIZE);\n\t}\n\n'
  assert after.count(getter)==1; restored=restored.replace(getter,'',1)
 elif e['class_name'].startswith('Render'):
  restored=restored.replace('\nimport com.hbm.entity.effect.EntityBlackHole;','',1)
  assert e['receiver_static_type']=='Entity' and e['receiver']=='entity'
 else:
  assert 'DataParameter' not in after and 'EntityDataManager.createKey' not in after
  assert e['receiver_static_type']==e['class_name'] and e['receiver']=='this'
 for change in e['transformations']:
  bm=[i for i,l in enumerate(before.splitlines(),1) if l.strip()==change['before']]
  am=[i for i,l in enumerate(after.splitlines(),1) if l.strip()==change['after']]
  assert bm==[change['before_line']] and am==[change['after_line']]
  method=next(l.strip() for l in reversed(after.splitlines()[:am[0]]) if ('public ' in l or 'protected ' in l) and '(' in l and not 'DataParameter' in l)
  assert method==change['source_method']
  restored=restored.replace(change['after'],change['before'],1); count+=change['operation_count']
 assert restored==before,'Unrelated edit: '+str(p)
 assert 'dataWatcher' not in after and 'getDataWatcher' not in after
 if len(sys.argv)>1:
  orig=Path(sys.argv[1])/Path(e['path']).relative_to('ntm-port-1.12.2')
  text=orig.read_text(); assert hashlib.sha256(text.encode()).hexdigest()==e['original_sha256']
  assert [x.strip() for x in text.splitlines() if 'dataWatcher' in x or 'getDataWatcher' in x]==[x.strip() for x in before.splitlines() if 'dataWatcher' in x or 'getDataWatcher' in x]
# Independent full-tree inheritance and all literal float-slot16 consumer closure.
children=[]; remaining=[]
for p in source.rglob('*.java'):
 s=p.read_text()
 for m in re.finditer(r'class (\w+) extends (EntityBlackHole|EntityVortex|EntityRagingVortex|EntityQuasar)\b',s): children.append([m[1],m[2]])
 if re.search(r'getWatchableObjectFloat\(\s*16\s*\)',s): remaining.append(str(p))
assert sorted(children)==sorted([['EntityVortex','EntityBlackHole'],['EntityRagingVortex','EntityBlackHole'],['EntityQuasar','EntityBlackHole']])
assert not remaining,remaining
original_consumers=subprocess.check_output(['git','-C',str(root),'grep','-n','getWatchableObjectFloat(16)',base,'--','ntm-port-1.12.2/src/main/java'],text=True)
for line in original_consumers.splitlines():
 path=line.split(':')[1]; assert path in [e['path'] for e in manifest],line
# Existing registration domain is all four base/subclasses, proving both renderer casts.
proxy=source/'com/hbm/main/ClientProxy.java'; proxy_text=proxy.read_text()
expected=['EntityBlackHole.class, new RenderBlackHole()','EntityVortex.class, new RenderBlackHole()','EntityRagingVortex.class, new RenderBlackHole()','EntityQuasar.class, new RenderQuasar()']
actual=[line.strip() for line in proxy_text.splitlines() if 'registerEntityRenderingHandler' in line and ('new RenderBlackHole(' in line or 'new RenderQuasar(' in line)]
assert len(actual)==4 and all(any(x in line for line in actual) for x in expected)
print('PASS: '+str(count)+' scoped operations across six files; inherited owner, all literal float16 consumers, renderer registration domains and exact inverse baseline source verified')
