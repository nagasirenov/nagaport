#!/usr/bin/env python3
from pathlib import Path
import re,json,hashlib,subprocess,sys
root=Path(__file__).resolve().parents[4];folder=Path(__file__).parent;src=root/'ntm-port-1.12.2/src/main/java';manifest=json.loads((folder/'source-manifest.json').read_text())
def calls(s):
 out=[]
 for m in re.finditer(r'(?:\b\w+\.)?(?:dataWatcher|getDataWatcher\(\))\.\w+\(',s):
  end=m.end();depth=1
  while depth:depth+=(s[end]=='(')-(s[end]==')');end+=1
  out.append(s[m.start():end])
 return out
count=0
for e in manifest['entries']:
 before=subprocess.check_output(['git','-C',str(root),'show',manifest['base']+':'+e['path']]).decode();after=(root/e['path']).read_bytes().decode();restored=after
 for label,s in [('before',before),('after',after)]:assert hashlib.sha256(s.encode()).hexdigest()==e[label+'_sha256']
 for insertion in e['insertions']:assert restored.count(insertion)==1;restored=restored.replace(insertion,'',1)
 for t in e['transformations']:
  assert before[t['before_offset']:].startswith(t['before']) and after[t['after_offset']:].startswith(t['after'])
  method=next(l.strip() for l in reversed(after[:t['after_offset']].splitlines()) if re.match(r'\s*(public|protected|private) [^=]*\(',l) and 'DataParameter' not in l);assert method==t['method']
  assert t['declaring_owner']==('EntityMissileBaseNT' if t['slot']==3 else 'EntityMissileCustom')
  if t['before'].startswith('this.'):assert t['receiver_static_type']==e['class_name']
  restored=restored.replace(t['after'],t['before'],1);count+=1
 assert restored==before,'Unrelated bytes changed: '+e['path'];assert not calls(after)
 if len(sys.argv)>1:
  original=(Path(sys.argv[1])/Path(e['path']).relative_to('ntm-port-1.12.2')).read_bytes();assert hashlib.sha256(original).hexdigest()==e['original_sha256'];original_calls=calls(original.decode()); baseline_calls=calls(before)
  if e['class_name']=='TileEntityLaunchPadBase':
   assert original_calls==['missile.getDataWatcher().updateObject(3, (byte) MathHelper.clamp_int(this.getBlockMetadata() - 10, 2, 5))']
   original_calls=[c.replace('MathHelper.clamp_int(', 'MathHelper.clamp(') for c in original_calls]
  assert original_calls==baseline_calls
for d in manifest['unchanged_dependencies']:
 assert hashlib.sha256((root/d['path']).read_bytes()).hexdigest()==d['sha256']
rawbase=next(src.rglob('EntityMissileBaseNT.java')).read_bytes().decode()
for name,boundary in json.loads((folder/'ticket-boundary.json').read_text()).items():
 assert boundary['after'] in rawbase and hashlib.sha256(boundary['after'].encode()).hexdigest()==boundary['after_sha256']
 if name!='entityInit':assert boundary['before']==boundary['after']
 else:assert boundary['before'].replace('this.dataWatcher.addObject(3, new Byte((byte) 5))','this.dataManager.register(DIRECTION, new Byte((byte) 5))')==boundary['after']
base=next(src.rglob('EntityMissileBaseNT.java')).read_text();custom=next(src.rglob('EntityMissileCustom.java')).read_text()
assert 'super.entityInit();\n\t\tinit(ForgeChunkManager.requestTicket(MainRegistry.instance, this.world, Type.ENTITY));\n\t\tthis.dataManager.register(DIRECTION, new Byte((byte) 5));' in base
assert 'public int health = 50;' in base and 'this.dataManager.register(HEALTH_SNAPSHOT, Integer.valueOf(this.health));' in custom
proxy=(src/'com/hbm/main/ClientProxy.java').read_text();assert proxy.count('new RenderMissileCustom(')==1;assert 'registerEntityRenderingHandler(EntityMissileCustom.class, renderManager -> new RenderMissileCustom(renderManager))' in proxy
for name in ['Generic','Huge','Nuclear','Strong','Taint']:
 s=next(src.rglob('RenderMissile'+name+'.java')).read_text();assert 'if(entity instanceof EntityMissileBaseNT) switch(((EntityMissileBaseNT) entity).getDirection())' in s
launcher=next(src.rglob('TileEntityLaunchPadBase.java')).read_text();assert 'EntityMissileBaseNT missile = clazz.getConstructor(' in launcher;assert 'missile.setDirection((byte) MathHelper.clamp(this.getBlockMetadata() - 10, 2, 5));' in launcher
classes={}
for p in src.rglob('*.java'):
 for m in re.finditer(r'\bclass (\w+) extends (\w+)',p.read_text()):classes[m[1]]=(m[2],str(p.relative_to(root)))
family={'EntityMissileBaseNT'}
while True:
 new=family|{c for c,(parent,p) in classes.items() if parent in family}
 if new==family:break
 family=new
assert [{'class_name':c,'parent':classes[c][0],'path':classes[c][1]} for c in sorted(family)]==manifest['hierarchy']
for p in {classes[c][1] for c in family}:assert not calls((root/p).read_text()),p
closure=[]
for p in src.rglob('*.java'):
 s=p.read_text()
 if any(re.search(r'\b'+n+r'\b',s) for n in family):closure.append({'path':str(p.relative_to(root)),'references':[{'line':i+1,'source':l.strip()} for i,l in enumerate(s.splitlines()) if any(re.search(r'\b'+n+r'\b',l) for n in family)],'legacy_calls':calls(s)})
assert closure==json.loads((folder/'consumer-closure.json').read_text())
print('PASS: '+str(count)+' original calls; byte-exact inverse/ticket order; six owner keys;35-class hierarchy and typed receiver/consumer closure')
