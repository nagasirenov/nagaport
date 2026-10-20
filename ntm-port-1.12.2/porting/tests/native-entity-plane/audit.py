#!/usr/bin/env python3
"""Fail-closed byte inverse, pristine watcher, inheritance, consumer and compiler attribution audit."""
from pathlib import Path
import hashlib,json,re,subprocess,sys,tempfile
folder=Path(__file__).resolve().parent;root=folder.parents[3];manifest=json.loads((folder/'source-manifest.json').read_text());base=manifest['base'];src='ntm-port-1.12.2/src/main/java/'
def baseline(p):return subprocess.check_output(['git','-C',str(root),'show',base+':'+p]).decode()
def current(p):return (root/p).read_bytes().decode() if (root/p).exists() else baseline(p)
def sha(s):return hashlib.sha256(s.encode()).hexdigest()
def calls(s):
 out=[]
 for m in re.finditer(r'(?:\b\w+\.)?(?:dataWatcher|getDataWatcher\(\))\.\w+\(',s):
  end=m.end();depth=1
  while depth:depth+=(s[end]=='(')-(s[end]==')');end+=1
  out.append(s[m.start():end])
 return out
count=0
for e in manifest['entries']:
 before=baseline(e['path']);after=current(e['path']);restored=after
 assert sha(before)==e['before_sha256'] and sha(after)==e['after_sha256'],e['path']
 assert calls(before)==[t['before'] for t in e['transformations']]
 for t in e['transformations']:
  assert before[t['before_offset']:].startswith(t['before']) and after[t['after_offset']:].startswith(t['after'])
  actual_method=next(l.strip() for l in reversed(after[:t['after_offset']].splitlines()) if re.match(r'\s*(public|protected|private) [^=]*\(',l) and 'DataParameter' not in l)
  assert actual_method==t['method'],(actual_method,t)
  assert t['owner']==('EntityPlaneBase' if 'PLANE_HEALTH' in t['after'] else 'EntityBomber')
 for text in e['insertions']:assert restored.count(text)==1;restored=restored.replace(text,'',1)
 for t in e['transformations']:assert t['after'] in restored;restored=restored.replace(t['after'],t['before'],1);count+=1
 assert restored==before,'Unrelated bytes changed: '+e['path'];assert not calls(after)
 if len(sys.argv)>1:
  original=(Path(sys.argv[1])/Path(e['path']).relative_to('ntm-port-1.12.2')).read_bytes().decode();assert sha(original)==e['original_sha256'];assert calls(original)==calls(before),'Pristine metadata mismatch'
for k in manifest['keys']:
 s=current(src+'com/hbm/entity/logic/'+k['declaring_owner']+'.java')
 declaration=k['visibility']+' static final DataParameter<'+k['type']+'> '+k['key']+' = EntityDataManager.createKey('+k['declaring_owner']+'.class, DataSerializers.'+k['serializer']+');'
 assert s.count(declaration)==1;assert s.count('this.dataManager.register('+k['key']+', '+k['default']+');')==1
names={e['class_name'] for e in manifest['hierarchy']}|{'RenderBomber'};classes={};closure=[]
refs=subprocess.check_output(['git','-C',str(root),'grep','-l','-E',r'\b('+'|'.join(sorted(names))+r')\b',base,'--',src]).decode().splitlines()
for ref in refs:
 p=ref.split(':',1)[1];s=baseline(p);closure.append({'path':p,'sha256':sha(s),'references':[{'line':i+1,'source':l.strip()} for i,l in enumerate(s.splitlines()) if any(re.search(r'\b'+n+r'\b',l) for n in names)],'baseline_legacy_calls':calls(s)})
 for m in re.finditer(r'\bclass (\w+) extends (\w+)',s):classes[m[1]]=m[2]
 actual=current(p);aliases=set(re.findall(r'\b(?:'+'|'.join(sorted(names))+r')\s+(\w+)\b',actual))
 for alias in aliases:assert not re.search(r'\b'+alias+r'\.(?:getDataWatcher\(\)|dataWatcher)\.',actual),(p,alias)
assert closure==json.loads((folder/'consumer-closure.json').read_text())
family={'EntityPlaneBase'}
while True:
 new=family|{n for n,p in classes.items() if p in family}
 if new==family:break
 family=new
assert [{'class_name':n,'parent':classes[n]} for n in sorted(family)]==manifest['hierarchy']
proxy=current(src+'com/hbm/main/ClientProxy.java');assert 'RenderingRegistry.registerEntityRenderingHandler(EntityBomber.class, renderManager -> new RenderBomber(renderManager));' in proxy
renderer=current(src+'com/hbm/render/entity/item/RenderBomber.java');assert 'int i = ((EntityBomber) entity).getBomberStyle();' in renderer
# The renderer's erased Entity receiver is covered explicitly, not inferred from a typed alias search.
assert len([x for x in closure if 'new RenderBomber(' in baseline(x['path'])])==1
attribution=json.loads((folder/'compiler-attribution.json').read_text());log=baseline(attribution['log_path']);assert sha(log)==attribution['log_sha256'];assert attribution['base']==base
records=[];other=[];paths={e['path'] for e in manifest['entries']}
for m in re.finditer(r'([^\n]+\.java):(\d+): error:([^\n]+)\n([\s\S]*?)(?=\n[^\n]+\.java:\d+: error:|\Z)',log):
 p=src+m[1].split('/java/')[-1]
 if p in paths:
  record={'path':p,'line':int(m[2]),'message':m[3].strip(),'detail':m[4].rstrip()}
  (records if 'dataWatcher' in m[4] or 'getDataWatcher' in m[4] else other).append(record)
assert records==attribution['watcher_diagnostics'] and other==attribution['other_same_file_diagnostics'];assert len(records)==count==19
for r in records:
 line=baseline(r['path']).splitlines()[r['line']-1].strip();assert line in r['detail'];assert 'dataWatcher' in line or 'getDataWatcher' in line
with tempfile.TemporaryDirectory(prefix='plane-source-inverse-') as d:
 for e in manifest['entries']:
  p=Path(d)/e['path'];p.parent.mkdir(parents=True,exist_ok=True);p.write_bytes(current(e['path']).encode())
 subprocess.run(['git','apply','--check',str(folder/'source-inverse.patch')],cwd=d,check=True);subprocess.run(['git','apply',str(folder/'source-inverse.patch')],cwd=d,check=True)
 for e in manifest['entries']:assert (Path(d)/e['path']).read_bytes()==baseline(e['path']).encode()
 subprocess.run(['git','apply','--check',str(folder/'production.patch')],cwd=d,check=True);subprocess.run(['git','apply',str(folder/'production.patch')],cwd=d,check=True)
 for e in manifest['entries']:assert (Path(d)/e['path']).read_bytes()==current(e['path']).encode()
print('PASS plane-metadata: '+str(count)+' pristine calls/CP81 diagnostics; exact owner/default/serializer/full-source inverse; '+str(len(family))+' classes and '+str(len(closure))+' consumer files; '+str(len(other))+' unrelated same-file diagnostics retained')
