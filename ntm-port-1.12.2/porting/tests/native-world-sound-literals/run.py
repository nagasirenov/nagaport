#!/usr/bin/env python3
import argparse,hashlib,json,subprocess
from pathlib import Path
p=argparse.ArgumentParser();p.add_argument('--project',type=Path,required=True);p.add_argument('--baseline-project',type=Path);p.add_argument('--resources',type=Path,required=True);p.add_argument('--classpath-file',type=Path,default=Path('/tmp/nagaport-ae2-native-classpath.txt'));p.add_argument('--java-home',type=Path,default=Path('/workspace/shared/nagaport-toolchain/java8'));p.add_argument('--legacy',type=Path,default=Path('/tmp/nagaport-legacy-client.jar'));p.add_argument('--out',type=Path,required=True);a=p.parse_args();e=Path(__file__).resolve().parent;r=a.baseline_project or a.project;old=r/'porting/tests/native-world-sound';a.out.mkdir(parents=True,exist_ok=True);classes=a.out/'classes';classes.mkdir(exist_ok=True);legacy=a.out/'legacy';legacy.mkdir(exist_ok=True);generated=a.out/'generated';generated.mkdir(exist_ok=True);cp=a.classpath_file.read_text().strip();j=a.java_home/'bin'
subprocess.run(['python3',str(e/'generate-fixtures.py'),str(generated)],check=True)
production=[a.project/'src/main/java'/x for x in ['com/hbm/lib/RefStrings.java','com/hbm/main/ModSoundEvents.java','com/hbm/sound/WorldSoundEffects.java','com/hbm/sound/WorldSoundCategories.java','com/hbm/sound/SoundCategoryResolver.java']]
files=production+[e/'DoorMethodProbe.java',e/'WorldSoundRegistryCategoryTest.java',e/'NumericWideningProbe.java',generated/'NativeNumericFixture.java',old/'NativeWorldSoundCallbackProbe.java',old/'PacketParityProbe.java',r/'porting/tests/ForgeEventNativeLauncher.java']
(a.out/'input-hashes.json').write_text(json.dumps({str(x):hashlib.sha256(x.read_bytes()).hexdigest() for x in files+[a.legacy,e/'generate-fixtures.py',a.resources/'assets/hbm/sounds.json']},indent=2)+'\n')
with (a.out/'compile.log').open('w') as log:
 subprocess.run([str(j/'javac'),'-encoding','UTF-8','-source','8','-target','8','-sourcepath',str(a.out/'empty'),'-cp',cp,'-d',str(classes)]+list(map(str,files)),stdout=log,stderr=subprocess.STDOUT,check=True)
 subprocess.run([str(j/'javac'),'-encoding','UTF-8','-source','8','-target','8','-sourcepath',str(a.out/'empty'),'-cp',str(a.legacy)+':'+cp,'-d',str(legacy),str(generated/'LegacyNumericFixture.java')],stdout=log,stderr=subprocess.STDOUT,check=True)
java=[str(j/'java'),'-Xmx400m','-Djava.awt.headless=true','-cp',str(classes)+':'+cp,'ForgeEventNativeLauncher']
for name,args in [('WorldSoundRegistryCategoryTest',[str(a.resources),str(e/'registry-manifest.json')]),('NumericWideningProbe',[str(a.legacy),str(legacy)]),('NativeWorldSoundCallbackProbe',[str(a.resources)]),('PacketParityProbe',[str(a.legacy)]),('DoorMethodProbe',[])]:
 with (a.out/(name+'.log')).open('w') as log:subprocess.run(java+[name]+args,stdout=log,stderr=subprocess.STDOUT,cwd=a.out,check=True)
 print('\n'.join(x for x in (a.out/(name+'.log')).read_text().splitlines() if x.startswith('PASS')))
