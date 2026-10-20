#!/usr/bin/env python3
"""Exact byte synchronization and NBT methods; native parents only."""
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
with tempfile.TemporaryDirectory(prefix='native-byte-') as temp:
 t=Path(temp); units=[]
 for e in manifest:
  s=(project.parent/e['path']).read_text(); assert hashlib.sha256(s.encode()).hexdigest()==e['after_sha256']; name=e['class_name']
  if name=='RenderShrapnel':
   assert 'if(rocket instanceof EntityShrapnel) {' in s
   expression=re.search(r'if\((\(\(EntityShrapnel\) rocket\).getShrapnelType\(\) >= 2)\)',s).group(1)
   (t/'RendererRead.java').write_text('import net.minecraft.entity.Entity; class RendererRead { static boolean large(Entity rocket) { if(rocket instanceof EntityShrapnel) { if('+expression+') return true; } return false; } }'); units.append(str(t/'RendererRead.java')); continue
  fields=re.findall(r'private static final DataParameter<Byte> [^;]+;',s)
  selected=['entityInit','readEntityFromNBT']
  if name=='EntityShrapnel': selected+=['getShrapnelType','setTrail','setVolcano','setWatz','setRadVolcano','writeToNBTOptional']
  else:
   selected+=['setIsCritical','getIsCritical','writeEntityToNBT']
   if name=='EntityBullet': selected+=['setTau','getIsTau','setChopper','getIsChopper']
   field_names=['tileX','tileY','tileZ'] if name=='EntityBullet' else ['field_145791_d','field_145792_e','field_145789_f']
   field_names+=['field_145790_g','inData','inGround','canBePickedUp','arrowShake','ticksInGround','damage']
   for f in field_names: fields.append(re.search(r'(?:public|private) (?:int|Block|boolean|double) '+f+r'\b[^;]*;',s).group())
  methods=[extract(s,r'(?:public|protected) (?:void|boolean|byte) '+method+r'\([^)]*\)\s*\{') for method in selected]
  if name=='EntityShrapnel': methods.append('protected void onImpact(net.minecraft.util.math.RayTraceResult result) { throw new AssertionError("Impact outside bounded byte probe"); }')
  unit='import net.minecraft.entity.Entity; import net.minecraft.entity.projectile.EntityThrowable; import net.minecraft.block.Block; import net.minecraft.world.World; import net.minecraft.nbt.NBTTagCompound; import net.minecraft.network.datasync.*;\nclass '+name+' extends '+e['superclass']+' {\npublic '+name+'(World world) { super(world); }\n'+'\n'.join(fields+methods)+'\n}'
  (t/(name+'.java')).write_text(unit); units.append(str(t/(name+'.java')))
 (t/'Probe.java').write_text((folder/'Probe.java').read_text()); units.append(str(t/'Probe.java'))
 cp=Path(os.environ.get('NAGAPORT_NATIVE_CP_FILE','/tmp/nagaport-ae2-native-classpath.txt')).read_text().strip(); java=Path(os.environ.get('JAVA_HOME','/workspace/shared/nagaport-toolchain/java8'))/'bin'
 subprocess.run([str(java/'javac'),'-cp',cp,'-d',temp]+units,check=True)
 subprocess.run([str(java/'java'),'-cp',temp+':'+cp,'Probe'],check=True)
 print('PASS: five actual synchronization/NBT method fixtures with native Entity/EntityThrowable parents and exact renderer condition')
