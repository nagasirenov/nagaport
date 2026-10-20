#!/usr/bin/env python3
from pathlib import Path
import re,json,hashlib,subprocess,tempfile,os
folder=Path(__file__).parent;root=folder.resolve().parents[3];src=root/'ntm-port-1.12.2/src/main/java'; manifest=json.loads((folder/'source-manifest.json').read_text())
def extract(s,pattern):
 m=re.search(pattern,s);assert m,pattern
 opening=s.index('{',m.end()-1);end=opening+1;depth=1
 while depth:depth+=(s[end]=='{')-(s[end]=='}');end+=1
 return s[m.start():end]
def method(s,name):return extract(s,r'(?:public|protected) (?:void|int|float|Entity\w+) '+name+r'\([^)]*\)\s*\{')
def constructor(s,name):return extract(s,r'public '+name+r'\(World (?:world|p_i1773_1_)\)\s*\{')
imports='import net.minecraft.entity.*; import net.minecraft.entity.projectile.EntityThrowable; import net.minecraft.world.World; import net.minecraft.nbt.*; import net.minecraft.network.datasync.*;'
failNBT='protected void readEntityFromNBT(NBTTagCompound nbt) {throw new AssertionError("Parent/registry NBT outside bounded probe");} protected void writeEntityToNBT(NBTTagCompound nbt) {throw new AssertionError("Parent/registry NBT outside bounded probe");}'
for dependency in manifest['unchanged_hierarchy_sources']:
 assert hashlib.sha256((root/dependency['path']).read_bytes()).hexdigest()==dependency['sha256']
with tempfile.TemporaryDirectory(prefix='native-pairs-') as temp:
 t=Path(temp);units=[]
 def unit(name,parent,parts,abstract=False):
  p=t/(name+'.java');p.write_text(imports+'\n'+('abstract ' if abstract else '')+'class '+name+' extends '+parent+' {\n'+'\n'.join(parts)+'\n}');units.append(str(p))
 # Actual World-only constructors and initialization path. No substituted/no-op entityInit on custom parents.
 nt=next(src.rglob('EntityThrowableNT.java')).read_text();interp=next(src.rglob('EntityThrowableInterp.java')).read_text();grenade=next(src.rglob('EntityGrenadeBase.java')).read_text()
 unit('EntityThrowableNT','Entity',[re.search(r'private static final DataParameter<Byte> STUCK_FACE[^;]+;',nt).group(),constructor(nt,'EntityThrowableNT'),method(nt,'entityInit'),method(nt,'setStuckIn'),method(nt,'getStuckIn'),failNBT],True)
 unit('EntityThrowableInterp','EntityThrowableNT',[constructor(interp,'EntityThrowableInterp')],True)
 unit('EntityGrenadeBase','EntityThrowable',[constructor(grenade,'EntityGrenadeBase')],True)
 for e in manifest['entries']:
  if not e['keys']:continue
  s=(root/e['path']).read_text();assert hashlib.sha256(s.encode()).hexdigest()==e['after_sha256'];name=e['class_name'];parts=re.findall(r'private static final DataParameter<\w+> [^;]+;',s)+[constructor(s,name),method(s,'entityInit')]
  if name in ['EntityCog','EntitySawblade']:
   parts += [method(s,n) for n in ['setOrientation','getOrientation','getMeta']+(['setMeta'] if name=='EntityCog' else [])]
   for op in ['read','write']:
    full=method(s,op+'Entity'+('From' if op=='read' else 'To')+'NBT');line='super.'+op+'Entity'+('From' if op=='read' else 'To')+'NBT(nbt);';assert full.count(line)==1
    # Execute exact child-only tail, not an invented successful parent serializer.
    tail=full[full.index(line)+len(line):];parts.append('public void '+op+'ScalarTail(NBTTagCompound nbt) {'+tail)
   for sign,label in [('+','fold'),('-','unfold')]:
    call='this.dataManager.set(ORIENTATION, orientation '+sign+' 6);';assert s.count(call)==1
    parts.append('public void '+label+'() { int orientation = this.dataManager.get(ORIENTATION); '+call+' }')
  elif name=='EntityMist':
   parts += [method(s,'setArea'),method(s,'setDuration'),method(s,'getMaxAge'),method(s,'setPosition'),re.search(r'public int maxAge = [^;]+;',s).group(),failNBT]
   # Exact scalar statements only. Fluid registry resolution explicitly not replaced by a fake Fluids class.
   for op in ['read','write']:
    full=method(s,op+'Entity'+('From' if op=='read' else 'To')+'NBT');lines=[l for l in full.splitlines() if 'this.setArea(' in l or 'nbt.setFloat(' in l];assert len(lines)==(1 if op=='read' else 2)
    parts.append('public void '+op+'Area(NBTTagCompound nbt) {'+'\n'.join(lines)+'}')
  else:
   parts += [method(s,n) for n in ['setFluid','setType','readEntityFromNBT','writeEntityToNBT']]
   parts.append('protected void onImpact(net.minecraft.util.math.RayTraceResult r) {throw new AssertionError("Impact outside bounded probe");}')
  unit(name,e['superclass'],parts)
 consumers=[]
 for name in ['Cog','Sawblade']:
  s=next(src.rglob('Render'+name+'.java')).read_text()
  for variable,accessor in [('cog','getOrientation')]+([('entity','getMeta')] if name=='Cog' else []):
   statement=next(l.strip() for l in s.splitlines() if ').'+accessor+'()' in l);consumers.append('static int '+name+accessor+'(Entity '+variable+') {'+statement+' return '+('meta' if accessor=='getMeta' else 'orientation')+';}')
 (t/'Consumers.java').write_text(imports+'class Consumers {'+'\n'.join(consumers)+'}');units.append(str(t/'Consumers.java'))
 (t/'Probe.java').write_text((folder/'Probe.java').read_text());units.append(str(t/'Probe.java'))
 cp=Path(os.environ.get('NAGAPORT_NATIVE_CP_FILE','/tmp/nagaport-ae2-native-classpath.txt')).read_text().strip();java=Path(os.environ.get('JAVA_HOME','/workspace/shared/nagaport-toolchain/java8'))/'bin'
 subprocess.run([str(java/'javac'),'-cp',cp,'-d',temp]+units,check=True);subprocess.run([str(java/'java'),'-cp',temp+':'+cp,'Probe'],check=True)
 print('PASS: actual World constructors and init chain; bounded scalar accessor/callback/NBT tails, no fake registry or parent NBT')
