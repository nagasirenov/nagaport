#!/usr/bin/env python3
from pathlib import Path
import re,json,hashlib,subprocess,tempfile,os
project=Path(__file__).resolve().parents[3]; folder=Path(__file__).parent
manifest=json.loads((folder/'source-manifest.json').read_text())
def extract(s,pattern):
 m=re.search(pattern,s); assert m,pattern
 opening=s.index('{',m.end()-1); end=opening+1; depth=1
 while depth:
  depth+=(s[end]=='{')-(s[end]=='}'); end+=1
 return s[m.start():end]
with tempfile.TemporaryDirectory(prefix='native-vortex-') as temp:
 t=Path(temp); units=[]; renderer_methods=[]
 for e in manifest:
  s=(project.parent/e['path']).read_text(); assert hashlib.sha256(s.encode()).hexdigest()==e['after_sha256']
  name=e['class_name']
  if name.startswith('Render'):
   statement=next(l.strip() for l in s.splitlines() if 'float size = ' in l)
   renderer_methods.append('static float '+name+'(Entity entity) { '+statement+' return size; }'); continue
  fields=[]; methods=[]
  if name=='EntityBlackHole':
   fields+=re.findall(r'protected static final DataParameter<Float> [^;]+;',s)
   fields.append(re.search(r'public boolean breaksBlocks = [^;]+;',s).group())
   selected=['entityInit','readEntityFromNBT','writeEntityToNBT','getBlackHoleSize','noBreak']
  elif name=='EntityVortex':
   fields.append(re.search(r'public float shrinkRate = [^;]+;',s).group()); selected=['readEntityFromNBT','writeEntityToNBT','setShrinkRate']
  elif name=='EntityRagingVortex':
   fields.append(re.search(r'int timer = [^;]+;',s).group()); selected=['readEntityFromNBT','writeEntityToNBT']
  else: selected=[]
  for method in selected: methods.append(extract(s,r'(?:public|protected) (?:void|float|'+name+r') '+method+r'\([^)]*\)\s*\{'))
  for m in re.finditer(r'public '+name+r'\([^)]*\)\s*\{',s): methods.append(extract(s[m.start():],r'public '+name+r'\([^)]*\)\s*\{'))
  if name in ['EntityVortex','EntityRagingVortex']:
   original=extract(s,r'public void onUpdate\(\)\s*\{')
   assert original.count('super.onUpdate();')==1
   prefix=original[:original.index('super.onUpdate();')].replace('public void onUpdate()', 'public void advanceSizeBeforeBaseUpdate()',1)+'}'
   # Exact original statements, only method label and omitted documented tail differ.
   methods.append(prefix)
  unit='import net.minecraft.entity.Entity;\nimport net.minecraft.world.World;\nimport net.minecraft.nbt.NBTTagCompound;\nimport net.minecraft.network.datasync.*;\nclass '+name+' extends '+e['superclass']+' {\n'+'\n'.join(fields+methods)+'\n}'
  (t/(name+'.java')).write_text(unit); units.append(str(t/(name+'.java')))
 (t/'RendererReads.java').write_text('import net.minecraft.entity.Entity; class RendererReads { '+'\n'.join(renderer_methods)+' }'); units.append(str(t/'RendererReads.java'))
 (t/'Probe.java').write_text((folder/'Probe.java').read_text()); units.append(str(t/'Probe.java'))
 cp=Path(os.environ.get('NAGAPORT_NATIVE_CP_FILE','/tmp/nagaport-ae2-native-classpath.txt')).read_text().strip(); java=Path(os.environ.get('JAVA_HOME','/workspace/shared/nagaport-toolchain/java8'))/'bin'
 subprocess.run([str(java/'javac'),'-cp',cp,'-d',temp]+units,check=True)
 for order in ['base-first','descendant-first']: subprocess.run([str(java/'java'),'-cp',temp+':'+cp,'Probe',order],check=True)
 print('PASS: exact actual synchronization/NBT/constructors, bounded pre-super update prefixes and two renderer-read expressions; no replacement custom parent')
