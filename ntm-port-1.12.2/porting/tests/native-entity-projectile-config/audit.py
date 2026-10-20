#!/usr/bin/env python3
"""Byte-exact whole-source inverse, pristine invocation parity and family/consumer closure."""
from pathlib import Path
import re,json,hashlib,subprocess,sys,tempfile
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
 for i in e['insertions']:assert restored.count(i)==1;restored=restored.replace(i,'',1)
 for t in e['transformations']:
  assert before[t['before_offset']:].startswith(t['before']) and after[t['after_offset']:].startswith(t['after'])
  method=next(l.strip() for l in reversed(after[:t['after_offset']].splitlines()) if re.match(r'\s*(public|protected|private) [^=]*\(',l) and 'DataParameter' not in l);assert method==t['method'],(e['path'],method,t['method'])
  assert t['declaring_owner']==('EntityArtilleryRocket' if e['class_name']=='RenderArtilleryRocket' else e['class_name'])
  assert t['receiver_static_type']==t['declaring_owner']
  restored=restored.replace(t['after'],t['before'],1);count+=1
 assert restored==before,'Unrelated bytes changed: '+e['path'];assert not calls(after)
 if len(sys.argv)>1:
  original=(Path(sys.argv[1])/Path(e['path']).relative_to('ntm-port-1.12.2')).read_bytes().decode();assert sha(original)==e['original_sha256'];assert calls(original)==calls(before)
 if 'key' in e:
  assert after.count('DataParameter<Integer> '+e['key']+' = EntityDataManager.createKey('+e['class_name']+'.class, DataSerializers.VARINT);')==1
  assert 'this.dataManager.register('+e['key']+', '+e['default']+');' in after
for d in manifest['unchanged_dependencies']:assert sha(current(d['path']))==d['sha256'],d['path']
# Independent hierarchy closure over pinned entire tree, not just changed files.
paths=subprocess.check_output(['git','-C',str(root),'ls-tree','-r','--name-only',base,'--',src]).decode().splitlines();classes={}
# Source-wide consumer closure itself locates all named children as they reference their parent.
names={e['class_name'] for e in manifest['hierarchy']}
refs=subprocess.check_output(['git','-C',str(root),'grep','-l','-E',r'\b('+'|'.join(sorted(names))+r')\b',base,'--',src]).decode().splitlines()
closure=[]
for ref in refs:
 p=ref.split(':',1)[1];s=baseline(p)
 closure.append({'path':p,'sha256':sha(s),'references':[{'line':i+1,'source':l.strip()} for i,l in enumerate(s.splitlines()) if any(re.search(r'\b'+n+r'\b',l) for n in names)],'baseline_legacy_calls':calls(s)})
 for m in re.finditer(r'\bclass (\w+) extends (\w+)',s):classes[m[1]]=m[2]
 actual=current(p)
 # Fail any raw watcher use through an explicitly typed family receiver, including casts.
 aliases=set(re.findall(r'\b(?:'+'|'.join(sorted(names))+r')\s+(\w+)\b',actual))
 for alias in aliases:assert not re.search(r'\b'+alias+r'\.(?:getDataWatcher\(\)|dataWatcher)\.',actual),(p,alias)
 assert not re.search(r'\(\('+r'(?:'+'|'.join(sorted(names))+r')'+r'\)[^;\n]+\)\.(?:getDataWatcher\(\)|dataWatcher)\.',actual),p
assert closure==json.loads((folder/'consumer-closure.json').read_text())
family={'EntityBulletBaseMK4','EntityBulletBeamBase','EntityArtilleryRocket','EntityArtilleryShell'}
while True:
 new=family|{n for n,p in classes.items() if p in family}
 if new==family:break
 family=new
assert [{'class_name':n,'parent':classes[n]} for n in sorted(family)]==sorted(manifest['hierarchy'],key=lambda e:e['class_name'])
for n in family:assert not calls(current(src+'com/hbm/entity/projectile/'+n+'.java'))
rocket=current(src+'com/hbm/entity/projectile/EntityArtilleryRocket.java');shell=current(src+'com/hbm/entity/projectile/EntityArtilleryShell.java');cl=current(src+'com/hbm/entity/projectile/EntityBulletBaseMK4CL.java')
assert 'protected void entityInit() {\n\t\tinit(ForgeChunkManager.requestTicket(MainRegistry.instance, this.world, Type.ENTITY));\n\t\tthis.dataManager.register(AMMO_TYPE, new Integer(0));' in rocket.replace('\r\n','\n')
assert 'super.entityInit();\n\t\tinit(ForgeChunkManager.requestTicket(MainRegistry.instance, this.world, Type.ENTITY));\n\t\tthis.dataManager.register(AMMO_TYPE, new Integer(0));' in shell.replace('\r\n','\n')
assert 'super.entityInit();\n\t\tinit(ForgeChunkManager.requestTicket(MainRegistry.instance, this.world, Type.ENTITY));' in cl.replace('\r\n','\n')
renderer=current(src+'com/hbm/render/entity/projectile/RenderArtilleryRocket.java')
assert 'EntityArtilleryRocket rocket = (EntityArtilleryRocket) entity;\n\t\treturn ItemAmmoHIMARS.itemTypes[rocket.getTypeId()].texture;' in renderer
# Apply the delivered exact inverse in an isolated temporary tree, prove all baseline bytes.
with tempfile.TemporaryDirectory(prefix='projectile-source-inverse-') as d:
 for e in manifest['entries']:
  p=Path(d)/e['path'];p.parent.mkdir(parents=True,exist_ok=True);p.write_bytes(current(e['path']).encode())
 subprocess.run(['git','apply','--check',str(folder/'source-inverse.patch')],cwd=d,check=True)
 subprocess.run(['git','apply',str(folder/'source-inverse.patch')],cwd=d,check=True)
 for e in manifest['entries']:assert (Path(d)/e['path']).read_bytes()==baseline(e['path']).encode()
print('PASS: '+str(count)+' pristine-parity calls; 4 owner-local keys; byte-exact full-source inverse; 5-class hierarchy, 52 consumer files; original ticket/super order and raw-renderer semantics')
