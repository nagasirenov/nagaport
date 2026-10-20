#!/usr/bin/env python3
from pathlib import Path
import re,json,hashlib,subprocess,tempfile,os
project=Path(__file__).resolve().parents[3]; folder=Path(__file__).parent; manifest=json.loads((folder/'source-manifest.json').read_text())
def extract(s,pattern):
 m=re.search(pattern,s); assert m,pattern
 opening=s.index('{',m.end()-1); end=opening+1; depth=1
 while depth:
  depth+=(s[end]=='{')-(s[end]=='}'); end+=1
 return s[m.start():end]
with tempfile.TemporaryDirectory(prefix='native-vehicle-') as temp:
 t=Path(temp); units=[]
 for e in manifest:
  s=(project.parent/e['path']).read_text(); assert hashlib.sha256(s.encode()).hexdigest()==e['after_sha256']; name=e['class_name']
  if not e['keys']: continue
  fields=re.findall(r'private static final DataParameter<\w+> [^;]+;',s); selected=['entityInit']; extra=[]
  if name=='EntityBoatRubber': selected+=['setDamageTaken','getDamageTaken','setTimeSinceHit','getTimeSinceHit','setForwardDirection','getForwardDirection','readEntityFromNBT','writeEntityToNBT']
  if name=='EntityMinerRocket':
   selected+=['getFlightMode','getSatelliteFrequency','setSatelliteFrequency','readEntityFromNBT','writeEntityToNBT'];fields.append(re.search(r'public int timer = [^;]+;',s).group())
  if name=='EntityCombatDropPod':
   selected+=['setColor','getColor','setPayload','readEntityFromNBT','writeEntityToNBT']; fields+=re.findall(r'private (?:NBTTagCompound|int) \w+;',s)
   extra.append('protected void onImpact(net.minecraft.util.math.RayTraceResult result) { throw new AssertionError("Outside scalar probe"); }')
  if name=='EntitySoyuz':
   selected+=['setSkin','getSkin']; fields+=re.findall(r'public int (?:mode|targetX|targetZ);',s)
   for method,label in [('readEntityFromNBT','readScalarPrefix'),('writeEntityToNBT','writeScalarPrefix')]:
    complete=extract(s,r'public void '+method+r'\([^)]*\)\s*\{'); assert complete.count('for (int i =')==1
    prefix=complete[:complete.index('for (int i =')].replace(method,label,1)+'}'; extra.append(prefix)
   # No fake payload serializer: original read/write methods remain untouched and uncompiled here.
   extra+=['protected void readEntityFromNBT(NBTTagCompound nbt) { throw new AssertionError("Payload NBT outside bounded prefix"); }','protected void writeEntityToNBT(NBTTagCompound nbt) { throw new AssertionError("Payload NBT outside bounded prefix"); }']
  methods=[extract(s,r'(?:public|protected) (?:void|float|int) '+method+r'\([^)]*\)\s*\{') for method in selected]
  unit='import net.minecraft.entity.Entity; import net.minecraft.entity.projectile.EntityThrowable; import net.minecraft.world.World; import net.minecraft.nbt.*; import net.minecraft.network.datasync.*;\nclass '+name+' extends '+e['superclass']+' { public '+name+'(World w) { super(w); }\n'+'\n'.join(fields+methods+extra)+'\n}'
  (t/(name+'.java')).write_text(unit); units.append(str(t/(name+'.java')))
 dock=(project/'src/main/java/com/hbm/tileentity/machine/TileEntityMachineSatDock.java').read_text()
 renderer=(project/'src/main/java/com/hbm/render/entity/rocket/RenderSoyuz.java').read_text()
 setter=next(l.strip() for l in dock.splitlines() if 'rocket.setSatelliteFrequency(freq);' in l)
 predicate=re.search(r'if\((rocket.getFlightMode\(\) == 1 && rocket.timer == 50)\)',dock).group(1)
 read=next(l.strip() for l in renderer.splitlines() if 'int type = ' in l)
 assert 'rocket.getSatelliteFrequency()' in dock
 (t/'ConsumerReads.java').write_text('import net.minecraft.entity.Entity; class ConsumerReads { static void setFrequency(EntityMinerRocket rocket,int freq) { '+setter+' } static int frequency(EntityMinerRocket rocket) { return rocket.getSatelliteFrequency(); } static boolean unload(EntityMinerRocket rocket) { return '+predicate+'; } static int skin(Entity entity) { '+read+' return type; } }')
 units.append(str(t/'ConsumerReads.java'))
 (t/'Probe.java').write_text((folder/'Probe.java').read_text()); units.append(str(t/'Probe.java'))
 cp=Path(os.environ.get('NAGAPORT_NATIVE_CP_FILE','/tmp/nagaport-ae2-native-classpath.txt')).read_text().strip(); java=Path(os.environ.get('JAVA_HOME','/workspace/shared/nagaport-toolchain/java8'))/'bin'
 subprocess.run([str(java/'javac'),'-cp',cp,'-d',temp]+units,check=True); subprocess.run([str(java/'java'),'-cp',temp+':'+cp,'Probe'],check=True)
 print('PASS: exact scalar methods and documented Soyuz pre-payload NBT prefixes; no payload conversion')
