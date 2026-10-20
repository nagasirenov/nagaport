#!/usr/bin/env python3
"""Fail-closed pristine invocation, owner, all-source inverse and descendant/caller audit."""
from pathlib import Path
import re,json,hashlib,subprocess,tempfile,sys,os,zipfile,io
folder=Path(__file__).resolve().parent;root=folder.parents[3];m=json.loads((folder/'source-manifest.json').read_text());base=m['base'];src='ntm-port-1.12.2/src/main/java/'
def baseline(p):return subprocess.check_output(['git','-C',str(root),'show',base+':'+p]).decode()
def current(p):return (root/p).read_bytes().decode() if (root/p).exists() else baseline(p)
def sha(s):return hashlib.sha256(s.encode()).hexdigest()
def calls(s):
 out=[]
 for x in re.finditer(r'(?:\b\w+\.)?(?:dataWatcher|getDataWatcher\(\))\.\w+\(',s):
  end=x.end();depth=1
  while depth:depth+=(s[end]=='(')-(s[end]==')');end+=1
  out.append(s[x.start():end])
 return out
count=0
for e in m['entries']:
 before=baseline(e['path']);after=current(e['path']);restored=after
 assert sha(before)==e['before_sha256'] and sha(after)==e['after_sha256'],e['path']
 assert calls(before)==[t['before'] for t in e['transformations']]
 for t in e['transformations']:
  assert before[t['before_offset']:].startswith(t['before']) and after[t['after_offset']:].startswith(t['after'])
  method=next(l.strip() for l in reversed(after[:t['after_offset']].splitlines()) if re.match(r'\s*(public|protected|private) [^=]*\(',l) and 'DataParameter' not in l)
  assert method==t['method'];assert any(k['key']==t['key'] and k['declaring_owner']==t['owner'] and k['type']==t['type'] and k['serializer']==t['serializer'] and k['legacy_id']==t['legacy_id'] for k in m['keys'])
 for text in e['insertions']:assert restored.count(text)==1;restored=restored.replace(text,'',1)
 for t in e['replacements']:assert restored.count(t['after'])==1;restored=restored.replace(t['after'],t['before'],1)
 for t in e['transformations']:assert t['after'] in restored;restored=restored.replace(t['after'],t['before'],1);count+=1
 assert restored==before,'Unrelated bytes changed: '+e['path'];assert not calls(after)
 if len(sys.argv)>1 and not sys.argv[1].startswith('--'):
  original=(Path(sys.argv[1])/Path(e['path']).relative_to('ntm-port-1.12.2')).read_bytes().decode();assert sha(original)==e['original_sha256'];assert calls(original)==calls(before)
for k in m['keys']:
 s=current(k['path']);decl='private static final DataParameter<'+k['type']+'> '+k['key']+' = EntityDataManager.createKey('+k['declaring_owner']+'.class, DataSerializers.'+k['serializer']+');'
 assert s.count(decl)==1;assert len(re.findall(r'(?:this\.dataManager|this\.getDataManager\(\))\.register\('+k['key']+', '+re.escape(k['default'])+r'\)',s))==1
names={e['class_name'] for e in m['hierarchy']};closure=[];classes={}
refs=subprocess.check_output(['git','-C',str(root),'grep','-l','-E',r'\b('+'|'.join(sorted(names))+r')\b',base,'--',src]).decode().splitlines()
for ref in refs:
 p=ref.split(':',1)[1];s=baseline(p);closure.append({'path':p,'sha256':sha(s),'references':[{'line':i+1,'source':l.strip()} for i,l in enumerate(s.splitlines()) if any(re.search(r'\b'+n+r'\b',l) for n in names)]})
 for x in re.finditer(r'\bclass (\w+) extends (\w+)',s):classes[x[1]]={'parent':x[2],'path':p}
 actual=current(p);aliases=set(re.findall(r'\b(?:'+'|'.join(sorted(names))+r')\s+(\w+)\b',actual))
 for alias in aliases:assert not re.search(r'\b'+alias+r'\.(?:getDataWatcher\(\)|dataWatcher)\.',actual),(p,alias)
assert closure==json.loads((folder/'consumer-closure.json').read_text())
family={k['declaring_owner'] for k in m['keys']}
while True:
 new=family|{n for n,e in classes.items() if e['parent'] in family}
 if new==family:break
 family=new
assert [{'class_name':n,**classes[n]} for n in sorted(family)]==m['hierarchy']
for e in m['hierarchy']:assert not calls(current(e['path']))
refs=subprocess.check_output(['git','-C',str(root),'grep','-l','cart_base_id',base,'--',src]).decode().splitlines();assert len(refs)==1 and refs[0].endswith('/EntityMinecartNTM.java')
ntm=next(e for e in m['entries'] if e['path'].endswith('/EntityMinecartNTM.java'));assert ntm['transformations'][-1]['before']=='this.dataWatcher.updateObject(23, nbt.getInteger("base"))';assert ntm['transformations'][-1]['after']=='this.dataManager.set(CART_BASE, nbt.getInteger("base"))'
# Exact registration/consumer type domains; no casts added to production.
proxy=current(src+'com/hbm/main/ClientProxy.java')
for owner,renderer in [('EntityMinecartNTM','RenderNeoCart'),('EntityMagnusCartus','RenderMagnusCartus'),('EntitySiegeCraft','RenderSiegeCraft'),('EntitySiegeUFO','RenderSiegeUFO'),('EntitySiegeTunneler','RenderSiegeTunneler')]:assert 'RenderingRegistry.registerEntityRenderingHandler('+owner+'.class, renderManager -> new '+renderer+'(renderManager));' in proxy
at=json.loads((folder/'compiler-attribution.json').read_text());log=baseline(at['log_path']);assert sha(log)==at['log_sha256'] and at['base']==base;records=[];other=[];paths={e['path'] for e in m['entries']}
for x in re.finditer(r'([^\n]+\.java):(\d+): error:([^\n]+)\n([\s\S]*?)(?=\n[^\n]+\.java:\d+: error:|\Z)',log):
 p=src+x[1].split('/java/')[-1]
 if p in paths:
  e={'path':p,'line':int(x[2]),'message':x[3].strip(),'detail':x[4].rstrip()};(records if 'dataWatcher' in x[4] or 'getDataWatcher' in x[4] else other).append(e)
assert records==at['watcher_diagnostics'] and other==at['other_same_file_diagnostics'];assert len(records)==count==38
for e in records:assert baseline(e['path']).splitlines()[e['line']-1].strip() in e['detail']
with tempfile.TemporaryDirectory(prefix='cart-siege-source-inverse-') as d:
 for e in m['entries']:p=Path(d)/e['path'];p.parent.mkdir(parents=True,exist_ok=True);p.write_bytes(current(e['path']).encode())
 for patch,which in [('source-inverse.patch',baseline),('production.patch',current)]:
  subprocess.run(['git','apply','--check',str(folder/patch)],cwd=d,check=True);subprocess.run(['git','apply',str(folder/patch)],cwd=d,check=True)
  for e in m['entries']:assert (Path(d)/e['path']).read_bytes()==which(e['path']).encode()
platform=json.loads((folder/'platform-manifest.json').read_text())
for name,digest in platform['evidence_sha256'].items():assert hashlib.sha256((folder/name).read_bytes()).hexdigest()==digest
if '--platform' in sys.argv:
 for path,digest in platform['input_sha256'].items():assert hashlib.sha256(Path(path).read_bytes()).hexdigest()==digest
 java=Path(os.environ.get('JAVA_HOME','/workspace/shared/nagaport-toolchain/java8'))/'bin/javap';cp=Path(os.environ.get('NAGAPORT_NATIVE_CP_FILE','/workspace/shared/nagaport-toolchain/native-classpath.txt')).read_text().strip()
 for name,classpath,names in [('legacy-bytecode.txt','/tmp/nagaport-legacy-client.jar',platform['legacy_classes']),('native-bytecode.txt',cp,platform['native_classes'])]:assert subprocess.check_output([str(java),'-cp',classpath,'-p','-c']+names)==(folder/name).read_bytes()
 z=zipfile.ZipFile('/tmp/nagaport-forge1614-userdev.jar');patches=zipfile.ZipFile(io.BytesIO(z.read('forgepatches.zip')));joined=''
 for name in platform['forge_patch_entries']:joined+='===== '+name+' =====\n'+patches.read(name).decode()+'\n'
 assert joined==(folder/'forge1614-parent-patches.txt').read_text()
print('PASS cart/siege: 6 owners / 12 owned native keys / 38 pristine calls and CP87 diagnostics / 13 named classes / 28 consumer files; exact complete source inverse; 48 unrelated same-file diagnostics retained')
