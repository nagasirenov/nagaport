#!/usr/bin/env python3
"""Replay original/native event, name transport and player-coordinate audit."""
import argparse, os, pathlib, subprocess, tempfile
HERE=pathlib.Path(__file__).resolve().parent
p=argparse.ArgumentParser();p.add_argument('--root',type=pathlib.Path,default=HERE.parents[3]);p.add_argument('--java-home',default=os.environ.get('JAVA_HOME','/workspace/shared/nagaport-toolchain/java8'));p.add_argument('--classpath-file',default='/tmp/nagaport-ae2-native-classpath.txt');p.add_argument('--legacy-jar',default='/tmp/nagaport-legacy-client.jar');a=p.parse_args()
work=pathlib.Path(tempfile.mkdtemp(prefix='entity-sound-engine-'));out=work/'classes';out.mkdir();java=pathlib.Path(a.java_home);cp=pathlib.Path(a.classpath_file).read_text().strip();src=a.root/'ntm-port-1.12.2/src/main/java'
production=['com/hbm/lib/RefStrings.java','com/hbm/main/ModSoundEvents.java','com/hbm/sound/SoundCategoryResolver.java','com/hbm/sound/WorldSoundEffects.java','com/hbm/sound/WorldSoundCategories.java']
files=[src/x for x in production]+[HERE.parent/'native-world-sound/NativeWorldSoundCallbackProbe.java',HERE.parent/'ForgeEventNativeLauncher.java']
tests=['NativeEntitySoundEventProbe','NativeCustomSoundProbe','PlayerSoundCoordinateProbe']
files += [HERE/'engine-audit'/(x+'.java') for x in tests]
subprocess.run([str(java/'bin/javac'),'-encoding','UTF-8','-source','8','-target','8','-cp',cp,'-d',str(out)]+list(map(str,files)),check=True)
for test in tests:subprocess.run([str(java/'bin/java'),'-Djava.awt.headless=true','-cp',str(out)+':'+cp,'ForgeEventNativeLauncher',test,a.legacy_jar],cwd=work,check=True)
print('PASS engine audits, not call activation or audible gameplay. Fresh work directory: '+str(work))
