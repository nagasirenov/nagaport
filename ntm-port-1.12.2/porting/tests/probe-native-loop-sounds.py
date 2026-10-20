#!/usr/bin/env python3
"""Complete native loop-sound family with declared NTM-only collaborator fixtures."""
import argparse,hashlib,json,os,subprocess,tempfile,zipfile
from pathlib import Path
p=argparse.ArgumentParser();p.add_argument('--classpath',required=True);p.add_argument('--java-home',type=Path,required=True);p.add_argument('--legacy-jar',type=Path);a=p.parse_args()
r=Path(__file__).resolve().parents[2];src=r/'src/main/java';t=r/'porting/tests';resources=r/'src/main/resources';original=r.parent/'ntm-original-pristine'
subprocess.run([os.sys.executable,str(t/'check-native-loop-sound-source.py')],check=True)
resource_scope=json.loads((t/'native-loop-sound-resources.json').read_text());original_json=json.loads((original/'src/main/resources/assets/hbm/sounds.json').read_text())
sha=lambda b:hashlib.sha256(b).hexdigest()
collaborators={
'com/hbm/entity/mob/EntityHunterChopper.java':'''package com.hbm.entity.mob; public class EntityHunterChopper extends net.minecraft.entity.Entity { public boolean dying; public EntityHunterChopper(){super(null);}public boolean getIsDying(){return dying;}protected void entityInit(){}protected void readEntityFromNBT(net.minecraft.nbt.NBTTagCompound n){}protected void writeEntityToNBT(net.minecraft.nbt.NBTTagCompound n){} }''',
'com/hbm/tileentity/machine/TileEntityMachineSiren.java':'''package com.hbm.tileentity.machine;public class TileEntityMachineSiren extends net.minecraft.tileentity.TileEntity {}''',
'com/hbm/items/machine/ItemCassette.java':'''package com.hbm.items.machine;public class ItemCassette {public enum SoundType {LOOP,PASS,SOUND;}}'''}
with tempfile.TemporaryDirectory(prefix='native-loop-sounds-') as d:
 out=Path(d);files=[]
 for rel,body in collaborators.items():
  f=out/'fixtures'/rel;f.parent.mkdir(parents=True,exist_ok=True);f.write_text(body);files.append(f);print('FIXTURE',rel,flush=True)
 for f in sorted((src/'com/hbm/sound').glob('*.java')):files.append(f);print('PRODUCTION',f.relative_to(r),sha(f.read_bytes()),flush=True)
 files += [src/'com/hbm/main/ModSoundEvents.java',src/'com/hbm/lib/RefStrings.java']
 files += [t/'LoopSoundNativeTest.java',t/'LegacyLoopSoundOracle.java',t/'ForgeEventNativeLauncher.java']
 jar=out/'loop-resources.jar'
 with zipfile.ZipFile(jar,'w') as z:
  z.write(resources/'assets/hbm/sounds.json','assets/hbm/sounds.json')
  for e in resource_scope['samples']:f=r/e['path'];z.write(f,f.relative_to(resources).as_posix())
 manifest=out/'events.tsv';manifest.write_text(''.join(key+'\t'+original_json[key]['category']+'\t'+str(len(original_json[key]['sounds']))+'\n' for key in resource_scope['events']))
 subprocess.run([str(a.java_home/'bin/javac'),'-encoding','UTF-8','-source','8','-target','8','-sourcepath',str(out/'fixtures'),'-cp',a.classpath,'-d',d]+list(map(str,files)),check=True)
 cp=d+os.pathsep+a.classpath
 subprocess.run([str(a.java_home/'bin/java'),'-Djava.awt.headless=true','-cp',cp,'ForgeEventNativeLauncher','com.hbm.sound.LoopSoundNativeTest',str(resources),str(jar),str(manifest)],cwd=d,check=True)
 if a.legacy_jar:
  assert sha(a.legacy_jar.read_bytes())=='a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce'
  subprocess.run([str(a.java_home/'bin/java'),'-cp',cp,'LegacyLoopSoundOracle',str(a.legacy_jar.resolve()),str(original/'src/main/resources/assets/hbm/sounds.json')],cwd=d,check=True)
print('PASS complete native loop family; controlled client/player/tile and audio boundary fixtures, not launched game or audible output',flush=True)
