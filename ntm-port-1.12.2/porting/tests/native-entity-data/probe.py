#!/usr/bin/env python3
"""Exact bounded production synchronization/NBT methods, native 1.12.2 runtime.
Not a complete entity/mod compile; omitted unrelated gameplay is not a production exclusion.
"""
from pathlib import Path
import hashlib,json,os,re,subprocess,tempfile
project=Path(__file__).resolve().parents[3]
src=project/'src/main/java/com/hbm/entity'
manifest=json.loads((Path(__file__).parent/'source-manifest.json').read_text())
def extract(s, pattern):
 m=re.search(pattern,s); assert m,pattern
 start=m.start(); opening=s.index('{',m.end()-1); depth=1; end=opening+1
 while depth:
  depth+=(s[end]=='{')-(s[end]=='}'); end+=1
 return s[start:end]
with tempfile.TemporaryDirectory(prefix='native-entity-data-') as temp:
 t=Path(temp); units=[]
 for entry in manifest:
  path=project.parent/entry['path']; s=path.read_text(); name=path.stem
  assert hashlib.sha256(s.encode()).hexdigest()==entry['after_sha256'],path
  assert 'dataWatcher' not in s and 'getDataWatcher' not in s
  methods=[]
  for method in ['entityInit','readEntityFromNBT','writeEntityToNBT','setMaxAge','getMaxAge','setType','getType']:
   pattern=r'(?:protected|public) (?:void|int|DebrisType) '+method+r'\([^)]*\)\s*\{'
   if re.search(pattern,s): methods.append(extract(s,pattern))
  parent='EntityDebrisBase' if name in ['EntityRBMKDebris','EntityZirnoxDebris'] else 'Entity'
  fields=re.findall(r'(?:protected|private) static final DataParameter<Integer> [^;]+;',s)
  if name=='EntityDebrisBase': fields.append('public float rot, lastRot;')
  if name.startswith('EntityCloud'):
   fields.append('public int age; public float scale;')
  enum=extract(s,r'public static enum DebrisType\s*\{') if 'enum DebrisType' in s else ''
  unit='import net.minecraft.entity.Entity;\nimport net.minecraft.world.World;\nimport net.minecraft.nbt.NBTTagCompound;\nimport net.minecraft.network.datasync.*;\nclass '+name+' extends '+parent+' {\n'+ '\n'.join(fields)+ '\npublic '+name+'(World w) { super(w); }\n'+'\n'.join(methods)+'\n'+enum+'\n}'
  (t/(name+'.java')).write_text(unit); units.append(str(t/(name+'.java')))
 (t/'Probe.java').write_text((Path(__file__).parent/'Probe.java').read_text())
 cp=Path(os.environ.get('NAGAPORT_NATIVE_CP_FILE','/tmp/nagaport-ae2-native-classpath.txt')).read_text().strip()
 java=Path(os.environ.get('JAVA_HOME','/workspace/shared/nagaport-toolchain/java8'))/'bin'
 subprocess.run([str(java/'javac'),'-cp',cp,'-d',temp]+units+[str(t/'Probe.java')],check=True)
 subprocess.run([str(java/'java'),'-cp',temp+':'+cp,'Probe'],check=True)
 print('PASS: 7 production owners, exact bounded methods; SHA256 source manifest verified')
