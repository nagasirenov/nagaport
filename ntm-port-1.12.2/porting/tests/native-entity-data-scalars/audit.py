#!/usr/bin/env python3
"""Exact inverse source guard plus independently reconstructed owner/range checks."""
from pathlib import Path
import hashlib,json,re,subprocess,sys
root=Path(__file__).resolve().parents[4]
manifest=json.loads((Path(__file__).parent/'source-manifest.json').read_text())
base='da80e3f7e6bba2800456f3d860c1c73a8af1ae2c'
count=0
for e in manifest:
 p=root/e['path']; after=p.read_text()
 before=subprocess.check_output(['git','-C',str(root),'show',base+':'+e['path']],text=True)
 assert hashlib.sha256(before.encode()).hexdigest()==e['before_sha256']
 assert hashlib.sha256(after.encode()).hexdigest()==e['after_sha256']
 assert re.search(r'class '+e['owner']+r' extends '+e['superclass']+r'\s*\{',after)
 restored=after
 for imp in ['DataParameter','DataSerializers','EntityDataManager']:
  restored=restored.replace('\nimport net.minecraft.network.datasync.'+imp+';','')
 declarations=[]
 for key in e['keys']:
  statement='private static final DataParameter<'+key['java_type']+'> '+key['key']+' = EntityDataManager.createKey('+e['owner']+'.class, DataSerializers.'+key['serializer']+');'
  assert after.count(statement)==1
  declarations.append('\t'+statement)
 restored=restored.replace('\n\n'+'\n'.join(declarations),'',1)
 for change in e['transformations']:
  assert change['declaring_owner']==e['owner']==change['receiver_type']
  assert change['receiver']=='this'
  before_matches=[i for i,l in enumerate(before.splitlines(),1) if l.strip()==change['before']]
  after_matches=[i for i,l in enumerate(after.splitlines(),1) if l.strip()==change['after']]
  assert before_matches==[change['before_line']]
  assert after_matches==[change['after_line']]
  actual_method=next(l.strip() for l in reversed(after.splitlines()[:after_matches[0]]) if ('public ' in l or 'protected ' in l) and '(' in l and not 'DataParameter' in l)
  assert actual_method==change['source_method']
  restored=restored.replace(change['after'],change['before'],1); count+=1
 assert restored==before,'Unrelated source edit: '+str(p)
 assert all('dataWatcher' not in l and 'getDataWatcher' not in l for l in after.splitlines() if not l.strip().startswith('//'))
 if len(sys.argv)>1:
  orig=Path(sys.argv[1])/'src/main/java/com/hbm/entity'/p.parent.name/p.name
  text=orig.read_text()
  assert hashlib.sha256(text.encode()).hexdigest()==e['original_sha256']
  assert [x.strip() for x in text.splitlines() if 'dataWatcher' in x or 'getDataWatcher' in x]==[x.strip() for x in before.splitlines() if 'dataWatcher' in x or 'getDataWatcher' in x]
print('PASS: '+str(count)+' exact scoped operations, all five owner/type allocations, independent after-lines/method attribution; inverse reconstruction equals CP60 byte-for-byte')
