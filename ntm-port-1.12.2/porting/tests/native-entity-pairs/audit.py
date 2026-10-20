#!/usr/bin/env python3
from pathlib import Path
import re,json,hashlib,subprocess,sys
root=Path(__file__).resolve().parents[4];folder=Path(__file__).parent;source=root/'ntm-port-1.12.2/src/main/java'; manifest=json.loads((folder/'source-manifest.json').read_text());count=0
def calls(s):
 out=[]
 for m in re.finditer(r'(?:\b\w+\.)?(?:dataWatcher|getDataWatcher\(\))\.\w+\(',s):
  end=m.end();depth=1
  while depth:depth+=(s[end]=='(')-(s[end]==')');end+=1
  out.append(s[m.start():end])
 return out
for dependency in manifest['unchanged_hierarchy_sources']:
 assert hashlib.sha256((root/dependency['path']).read_bytes()).hexdigest()==dependency['sha256']
 assert subprocess.check_output(['git','-C',str(root),'show',manifest['base']+':'+dependency['path']])==(root/dependency['path']).read_bytes()
for e in manifest['entries']:
 before=subprocess.check_output(['git','-C',str(root),'show',manifest['base']+':'+e['path']],text=True);after=(root/e['path']).read_text();restored=after
 assert re.search(r'class '+e['class_name']+r' extends '+e['superclass']+r'\b',after)
 for label,s in [('before',before),('after',after)]:assert hashlib.sha256(s.encode()).hexdigest()==e[label+'_sha256']
 for insertion in e['insertions']:assert restored.count(insertion)==1;restored=restored.replace(insertion,'',1)
 for t in e['transformations']:
  assert before[t['before_offset']:].startswith(t['before']) and after[t['after_offset']:].startswith(t['after'])
  method=next(l.strip() for l in reversed(after[:t['after_offset']].splitlines()) if re.match(r'\s*(public|protected|private) [^=]*\(',l) and 'DataParameter' not in l)
  assert method==t['source_method'];assert t['declaring_owner'] in ['EntityCog','EntitySawblade','EntityMist','EntityDisperserCanister']
  if e['keys']:
   assert t['receiver_static_type']==e['class_name']==t['declaring_owner'];assert t['before'].startswith('this.dataWatcher.') and t['after'].startswith('this.dataManager.')
  restored=restored.replace(t['after'],t['before'],1);count+=1
 assert restored==before,'Unrelated source edits: '+e['path']
 assert not calls(after)
 for k in e['keys']:assert after.count('EntityDataManager.createKey('+e['class_name']+'.class, DataSerializers.'+k['serializer']+')')==sum(x['serializer']==k['serializer'] for x in e['keys'])
 if len(sys.argv)>1:
  original=(Path(sys.argv[1])/Path(e['path']).relative_to('ntm-port-1.12.2')).read_text();assert hashlib.sha256(original.encode()).hexdigest()==e['original_sha256'];assert calls(original)==calls(before)
owners=[e['class_name'] for e in manifest['entries'] if e['keys']];closure=[]
for p in source.rglob('*.java'):
 s=p.read_text();assert not re.search(r'extends ('+'|'.join(owners)+r')\b',s),p
 if any(x in s for x in owners):
  closure.append({'path':str(p.relative_to(root)),'references':[{'line':i+1,'source':l.strip()} for i,l in enumerate(s.splitlines()) if any(x in l for x in owners)],'legacy_calls':calls(s)})
# Only named entity owners and their dedicated renderers used raw slots; other callers use existing typed methods.
for name in ['RenderCog','RenderSawblade','RenderMist','RenderGenericGrenade']:
 p=next(source.rglob(name+'.java'));assert not calls(p.read_text()),p
proxy=(source/'com/hbm/main/ClientProxy.java').read_text()
for name in ['Cog','Sawblade']:
 assert proxy.count('new Render'+name+'(')==1
 assert 'registerEntityRenderingHandler(Entity'+name+'.class, renderManager -> new Render'+name+'(renderManager))' in proxy
assert not re.search(r'entityInit\s*\(',next(source.rglob('EntityGrenadeBase.java')).read_text())
assert not re.search(r'entityInit\s*\(',next(source.rglob('EntityThrowableInterp.java')).read_text())
assert 'DataParameter<Byte> STUCK_FACE = EntityDataManager.createKey(EntityThrowableNT.class, DataSerializers.BYTE)' in next(source.rglob('EntityThrowableNT.java')).read_text()
expected=json.loads((folder/'consumer-closure.json').read_text());assert closure==expected
print('PASS: '+str(count)+' original calls; byte-exact inverse; owner/source ranges and nine keys; renderer registration closure; no descendants; native inherited key retained')
