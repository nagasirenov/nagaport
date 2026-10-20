#!/usr/bin/env python3
"""Fail-closed whole-file inverse, pristine calls, typed closure and native platform audit."""
from pathlib import Path
import hashlib, json, os, re, subprocess, sys, tempfile
folder=Path(__file__).resolve().parent;root=folder.parents[3];m=json.loads((folder/'source-manifest.json').read_text());base=m['base'];src='ntm-port-1.12.2/src/main/java/'
def baseline(p):return subprocess.check_output(['git','-C',str(root),'show',base+':'+p]).decode()
def current(p):return (root/p).read_bytes().decode() if (root/p).exists() else baseline(p)
def sha(s):return hashlib.sha256(s.encode()).hexdigest()
def calls(s):
 result=[]
 for x in re.finditer(r'(?:\b\w+\.)?(?:dataWatcher|getDataWatcher\(\))\.\w+\(',s):
  end=x.end();depth=1
  while depth:depth+=(s[end]=='(')-(s[end]==')');end+=1
  result.append(s[x.start():end])
 return result
assert (folder/'production.sha256').read_text().strip()==hashlib.sha256((folder/'production.patch').read_bytes()).hexdigest()+'  production.patch'
assert set(re.findall(r'^diff --git a/(\S+) b/\S+$',(folder/'production.patch').read_text(),re.M))=={e['path'] for e in m['entries']},'Production patch scope changed'
count=0
for e in m['entries']:
 before=baseline(e['path']);after=current(e['path']);restored=after
 assert sha(before)==e['before_sha256'] and sha(after)==e['after_sha256'],e['path']
 assert calls(before)==[t['before'] for t in e['transformations']]
 for t in e['transformations']:
  assert before[t['before_offset']:].startswith(t['before']) and after[t['after_offset']:].startswith(t['after'])
  method=next(l.strip() for l in reversed(after[:t['after_offset']].splitlines()) if re.match(r'\s*(public|protected|private) [^=]*\(',l) and 'DataParameter' not in l)
  assert method==t['method'];assert any(k['key']==t['key'] and k['declaring_owner']==t['owner'] and k['legacy_id']==t['legacy_id'] and k['type']==t['type'] and k['serializer']==t['serializer'] for k in m['keys'])
 for text in e['insertions']:assert restored.count(text)==1;restored=restored.replace(text,'',1)
 for t in e['transformations']:assert t['after'] in restored;restored=restored.replace(t['after'],t['before'],1);count+=1
 assert restored==before,'Unrelated changes: '+e['path'];assert not calls(after)
 if len(sys.argv)>1 and not sys.argv[1].startswith('--'):
  original=(Path(sys.argv[1])/Path(e['path']).relative_to('ntm-port-1.12.2')).read_bytes().decode();assert sha(original)==e['original_sha256'];assert calls(original)==calls(before)
 # Every original registration has the exact same default, order and unconditional parent-first placement.
 block=re.search(r'protected void entityInit\(\) \{([^}]+)\}',after)[1]
 expected='\n\t\tsuper.entityInit();\n'+''.join('\t\tthis.dataManager.register('+k['key']+', '+k['default']+');\n' for k in m['keys'] if k['path']==e['path'])+'\t'
 assert block==expected
for k in m['keys']:
 s=current(k['path']);decl='private static final DataParameter<'+k['type']+'> '+k['key']+' = EntityDataManager.createKey('+k['declaring_owner']+'.class, DataSerializers.'+k['serializer']+');';assert s.count(decl)==1
names={h['class_name'] for h in m['hierarchy']};closure=[];classes={}
refs=subprocess.check_output(['git','-C',str(root),'grep','-l','-E',r'\b('+'|'.join(sorted(names))+r')\b',base,'--',src]).decode().splitlines()
for ref in refs:
 p=ref.split(':',1)[1];s=baseline(p);closure.append({'path':p,'sha256':sha(s),'references':[{'line':i+1,'source':l.strip()} for i,l in enumerate(s.splitlines()) if any(re.search(r'\b'+n+r'\b',l) for n in names)]})
 for x in re.finditer(r'\bclass (\w+) extends (\w+)',s):classes[x[1]]={'parent':x[2],'path':p}
 aliases=set(re.findall(r'\b(?:'+'|'.join(sorted(names))+r')\s+(\w+)\b',current(p)))
 for alias in aliases:assert not re.search(r'\b'+alias+r'\.(?:getDataWatcher\(\)|dataWatcher)\.',current(p)),(p,alias)
assert closure==json.loads((folder/'consumer-closure.json').read_text());assert len(closure)==17
assert not any(e['parent'] in names for e in classes.values()),'New descendant must be reviewed'
assert {h['class_name'] for h in m['hierarchy']}==set(classes)&names
for h in m['hierarchy']:assert classes[h['class_name']]=={k:v for k,v in h.items() if k!='class_name'}
proxy=current(src+'com/hbm/main/ClientProxy.java')
for owner,renderer in [('EntityUFO','RenderUFO'),('EntityBFAngel','RenderBFA'),('EntityHunterChopper','RenderHunterChopper')]:assert 'RenderingRegistry.registerEntityRenderingHandler('+owner+'.class, renderManager -> new '+renderer+'(renderManager));' in proxy
for p in ['com/hbm/entity/boss/INtmBossDisplay.java','com/hbm/render/util/ClientBossDisplay.java']:assert current(src+p)==baseline(src+p),'Boss HUD is excluded'
# Full-tree inventory exposes every remaining numeric watcher and rejects hidden DataWatcher typed aliases.
paths=subprocess.check_output(['git','-C',str(root),'ls-tree','-r','--name-only',base,src]).decode().splitlines();inventory=[]
for p in paths:
 if not p.endswith('.java'):continue
 s=current(p)
 assert not re.search(r'\bDataWatcher\s+\w+\s*[;=,)]',s),'Undeclared alias review needed: '+p
 for i,l in enumerate(s.splitlines()):
  if re.search(r'(?:dataWatcher|getDataWatcher\(\))\.',l):inventory.append({'path':p,'line':i+1,'source':l.strip()})
if '--record-inventory' in sys.argv:(folder/'remaining-watcher-inventory.json').write_text(json.dumps(inventory,indent=2)+'\n')
assert inventory==json.loads((folder/'remaining-watcher-inventory.json').read_text())
at=json.loads((folder/'compiler-attribution.json').read_text());log=baseline(at['log_path']);assert sha(log)==at['log_sha256'] and at['base']==base;records=[];other=[];changed={e['path'] for e in m['entries']}
for x in re.finditer(r'([^\n]+\.java):(\d+): error:([^\n]+)\n([\s\S]*?)(?=\n[^\n]+\.java:\d+: error:|\Z)',log):
 p=src+x[1].split('/java/')[-1]
 if p in changed:
  e={'path':p,'line':int(x[2]),'message':x[3].strip(),'detail':x[4].rstrip()};(records if 'dataWatcher' in x[4] or 'getDataWatcher' in x[4] else other).append(e)
assert records==at['watcher_diagnostics'] and other==at['other_same_file_diagnostics'];assert len(records)==count==33 and len(other)==66
for e in records:assert baseline(e['path']).splitlines()[e['line']-1].strip() in e['detail']
with tempfile.TemporaryDirectory(prefix='flying-boss-inverse-') as d:
 for e in m['entries']:p=Path(d)/e['path'];p.parent.mkdir(parents=True,exist_ok=True);p.write_bytes(current(e['path']).encode())
 for patch,which in [('source-inverse.patch',baseline),('production.patch',current)]:
  subprocess.run(['git','apply','--check',str(folder/patch)],cwd=d,check=True);subprocess.run(['git','apply',str(folder/patch)],cwd=d,check=True)
  for e in m['entries']:assert (Path(d)/e['path']).read_bytes()==which(e['path']).encode()
platform=json.loads((folder/'platform-reference.json').read_text());prior=folder.parent/platform['directory']
for name,digest in platform['sha256'].items():assert hashlib.sha256((prior/name).read_bytes()).hexdigest()==digest
if '--platform' in sys.argv:
 p=json.loads((prior/'platform-manifest.json').read_text());java=Path(os.environ.get('JAVA_HOME','/workspace/shared/nagaport-toolchain/java8'))/'bin/javap';cp=Path(os.environ.get('NAGAPORT_NATIVE_CP_FILE','/workspace/shared/nagaport-toolchain/native-classpath.txt')).read_text().strip()
 for path,digest in p['input_sha256'].items():assert hashlib.sha256(Path(path).read_bytes()).hexdigest()==digest
 for name,classpath,names in [('legacy-bytecode.txt','/tmp/nagaport-legacy-client.jar',p['legacy_classes']),('native-bytecode.txt',cp,p['native_classes'])]:assert subprocess.check_output([str(java),'-cp',classpath,'-p','-c']+names)==(prior/name).read_bytes()
 import zipfile,io
 z=zipfile.ZipFile('/tmp/nagaport-forge1614-userdev.jar');patches=zipfile.ZipFile(io.BytesIO(z.read('forgepatches.zip')));joined=''
 for name in p['forge_patch_entries']:joined+='===== '+name+' =====\n'+patches.read(name).decode()+'\n'
 assert joined==(prior/'forge1614-parent-patches.txt').read_text()
print('PASS flying bosses: 3 owners / 12 private native keys / 33 pristine calls and CP88 diagnostics / 17-file typed consumer closure; exact whole-file inverse; 66 unrelated same-file diagnostics retained')
