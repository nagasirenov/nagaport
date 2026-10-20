#!/usr/bin/env python3
"""Extract exact scalar synchronization/NBT production methods into native entity shells."""
from pathlib import Path
import hashlib,json,os,re,subprocess,tempfile
project=Path(__file__).resolve().parents[3]
manifest=json.loads((Path(__file__).parent/'source-manifest.json').read_text())
def extract(s, pattern):
 m=re.search(pattern,s); assert m,pattern
 start=m.start(); opening=s.index('{',m.end()-1); depth=1; end=opening+1
 while depth:
  depth+=(s[end]=='{')-(s[end]=='}'); end+=1
 return s[start:end]
with tempfile.TemporaryDirectory(prefix='native-entity-scalars-') as temp:
 t=Path(temp); units=[]
 for entry in manifest:
  path=project.parent/entry['path']; s=path.read_text(); name=path.stem
  assert hashlib.sha256(s.encode()).hexdigest()==entry['after_sha256'],path
  methods=[]
  selected={'EntityFireLingering':['entityInit','setArea','setType','getType','setDuration','readEntityFromNBT','writeEntityToNBT','writeToNBTOptional'], 'EntityWaypoint':['entityInit','setWaypointType','getWaypointType','readEntityFromNBT','writeEntityToNBT'], 'EntitySiegeLaser':['entityInit','setColor','getColor','readEntityFromNBT','writeEntityToNBT','setDamage','setExplosive','setBreakChance','setIncendiary']}[name]
  for method in selected:
   methods.append(extract(s,r'(?:protected|public) (?:void|boolean|int|'+name+r') '+method+r'\([^)]*\)\s*\{'))
  fields=re.findall(r'private static final DataParameter<\w+> [^;]+;',s)
  if name=='EntityFireLingering': fields.append(re.search(r'public int maxAge = [^;]+;',s).group())
  if name=='EntitySiegeLaser':
   fields+=re.findall(r'private (?:float|boolean) \w+ = [^;]+;',s)
   methods.append('protected void onImpact(net.minecraft.util.math.RayTraceResult result) { throw new AssertionError("Impact gameplay outside bounded probe"); }')
  unit='import net.minecraft.entity.Entity;\nimport net.minecraft.entity.projectile.EntityThrowable;\nimport net.minecraft.world.World;\nimport net.minecraft.nbt.NBTTagCompound;\nimport net.minecraft.network.datasync.*;\nclass '+name+' extends '+entry['superclass']+' {\n'+ '\n'.join(fields)+ '\npublic '+name+'(World w) { super(w); }\n'+'\n'.join(methods)+'\n}'
  (t/(name+'.java')).write_text(unit); units.append(str(t/(name+'.java')))
 (t/'Probe.java').write_text((Path(__file__).parent/'Probe.java').read_text())
 cp=Path(os.environ.get('NAGAPORT_NATIVE_CP_FILE','/tmp/nagaport-ae2-native-classpath.txt')).read_text().strip()
 java=Path(os.environ.get('JAVA_HOME','/workspace/shared/nagaport-toolchain/java8'))/'bin'
 subprocess.run([str(java/'javac'),'-cp',cp,'-d',temp]+units+[str(t/'Probe.java')],check=True)
 subprocess.run([str(java/'java'),'-cp',temp+':'+cp,'Probe'],check=True)
 subprocess.run([str(java/'java'),'-cp',temp+':'+cp,'Probe','reverse-load'],check=True)
 print('PASS: three exact bounded production fixtures, native Entity / EntityThrowable parents; source SHA256 verified')
