#!/usr/bin/env python3
"""Pinned whole-source inverse, pristine calls, descendant/renderer closure and compiler attribution."""
from pathlib import Path
import hashlib,json,re,subprocess,sys,tempfile
folder=Path(__file__).resolve().parent;root=folder.parents[3];manifest=json.loads((folder/'source-manifest.json').read_text());base=manifest['base'];src='ntm-port-1.12.2/src/main/java/'
def baseline(p):return subprocess.check_output(['git','-C',str(root),'show',base+':'+p]).decode()
def current(p):return (root/p).read_bytes().decode() if (root/p).exists() else baseline(p)
def sha(s):return hashlib.sha256(s.encode()).hexdigest()
def calls(s):
 out=[]
 for m in re.finditer(r'(?:(?:\w+\.)*\b(?:dataWatcher|getDataWatcher\(\)))\.\w+\(',s):
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
  assert (t['owner'] in e['path'] and t['key'] in t['after']) or any(x in t['after'] for x in ['.setSynchronizedScale(','.setBlockMetadata(','.getBlockMetadata()'])
 for text in e['insertions']:assert restored.count(text)==1;restored=restored.replace(text,'',1)
 for t in e['transformations']:assert t['after'] in restored;restored=restored.replace(t['after'],t['before'],1);count+=1
 assert restored==before,'Unrelated bytes changed: '+e['path'];assert not calls(after)
 if len(sys.argv)>1:
  original=(Path(sys.argv[1])/Path(e['path']).relative_to('ntm-port-1.12.2')).read_bytes().decode();assert sha(original)==e['original_sha256'];assert calls(original)==e['pristine_calls']
  expected=e['pristine_calls']
  if e['path'].endswith('/NukeCustom.java'):
   assert expected==['bomb.getDataWatcher().updateObject(20, (byte)world.getBlockMetadata(x, y, z))']
   expected=[expected[0].replace('world.getBlockMetadata(x, y, z)','com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) )')]
  assert expected==calls(before),'Pristine watcher mismatch beyond pinned existing native world-state prerequisite'
for k in manifest['keys']:
 s=current(k['path']);declaration='private static final DataParameter<'+k['type']+'> '+k['key']+' = EntityDataManager.createKey('+k['owner']+'.class, DataSerializers.'+k['serializer']+');'
 assert s.count(declaration)==1;assert s.count('this.dataManager.register('+k['key']+', '+k['default']+');')==1
 # None of these four original init overrides delegated to the parent. Preserve that omission.
 body=s.split('protected void entityInit() {',1)[1].split('}',1)[0];assert 'super.entityInit' not in body
names={e['class_name'] for e in manifest['hierarchy']}|{'RenderChemical','RenderFallingNuke','RenderTorex','RenderEmpty'};classes={};closure=[]
refs=subprocess.check_output(['git','-C',str(root),'grep','-l','-E',r'\b('+'|'.join(sorted(names))+r')\b',base,'--',src]).decode().splitlines()
for ref in refs:
 p=ref.split(':',1)[1];s=baseline(p);closure.append({'path':p,'sha256':sha(s),'references':[{'line':i+1,'source':l.strip()} for i,l in enumerate(s.splitlines()) if any(re.search(r'\b'+n+r'\b',l) for n in names)],'baseline_legacy_calls':calls(s)})
 for m in re.finditer(r'\bclass (\w+) extends (\w+)',s):classes[m[1]]=m[2]
 actual=current(p);aliases=set(re.findall(r'\b(?:'+'|'.join(sorted(names))+r')\s+(\w+)\b',actual))
 for alias in aliases:assert not re.search(r'\b'+alias+r'\.(?:getDataWatcher\(\)|dataWatcher)\.',actual),(p,alias)
assert closure==json.loads((folder/'consumer-closure.json').read_text())
family={e['class_name'] for e in manifest['hierarchy']}
while True:
 new=family|{n for n,p in classes.items() if p in family}
 if new==family:break
 family=new
assert sorted([{'class_name':n,'parent':classes[n]} for n in family],key=lambda e:e['class_name'])==sorted(manifest['hierarchy'],key=lambda e:e['class_name'])
proxy=current(src+'com/hbm/main/ClientProxy.java')
for owner,renderer in [('EntityChemical','RenderChemical'),('EntityFallingNuke','RenderFallingNuke'),('EntityNukeTorex','RenderTorex'),('EntityDepress','RenderEmpty')]:
 assert re.search(r'registerEntityRenderingHandler\('+owner+r'\.class,.*new '+renderer+r'\(',proxy)
 for e in closure:
  if e['path'].endswith('/'+renderer+'.java'):assert not calls(current(e['path'])),'raw erased renderer watcher'
attribution=json.loads((folder/'compiler-attribution.json').read_text());log=baseline(attribution['log_path']);assert sha(log)==attribution['log_sha256'];assert attribution['base']==base
records=[];other=[];paths={e['path'] for e in manifest['entries']}
for m in re.finditer(r'([^\n]+\.java):(\d+): error:([^\n]+)\n([\s\S]*?)(?=\n[^\n]+\.java:\d+: error:|\Z)',log):
 p=src+m[1].split('/java/')[-1]
 if p in paths:
  record={'path':p,'line':int(m[2]),'message':m[3].strip(),'detail':m[4].rstrip()}
  (records if 'dataWatcher' in m[4] or 'getDataWatcher' in m[4] else other).append(record)
assert records==attribution['watcher_diagnostics'] and other==attribution['other_same_file_diagnostics'];assert len(records)==count==16
for r in records:
 line=baseline(r['path']).splitlines()[r['line']-1].strip();assert line in r['detail'];assert 'dataWatcher' in line or 'getDataWatcher' in line
with tempfile.TemporaryDirectory(prefix='projectile-utility-inverse-') as d:
 for e in manifest['entries']:
  p=Path(d)/e['path'];p.parent.mkdir(parents=True,exist_ok=True);p.write_bytes(current(e['path']).encode())
 subprocess.run(['git','apply','--check',str(folder/'source-inverse.patch')],cwd=d,check=True);subprocess.run(['git','apply',str(folder/'source-inverse.patch')],cwd=d,check=True)
 for e in manifest['entries']:assert (Path(d)/e['path']).read_bytes()==baseline(e['path']).encode()
 subprocess.run(['git','apply','--check',str(folder/'production.patch')],cwd=d,check=True);subprocess.run(['git','apply',str(folder/'production.patch')],cwd=d,check=True)
 for e in manifest['entries']:assert (Path(d)/e['path']).read_bytes()==current(e['path']).encode()
print('PASS projectile/utility metadata: '+str(count)+' pristine calls/CP82 diagnostics; exact owner/default/serializer/full-source inverse; '+str(len(family))+' owners and '+str(len(closure))+' consumer files; '+str(len(other))+' unrelated same-file diagnostics retained')
