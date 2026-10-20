#!/usr/bin/env python3
"""Compile actual bounded production against native Forge/Minecraft; no production stubs."""
import argparse,hashlib,json,os,subprocess,tempfile
from pathlib import Path
p=argparse.ArgumentParser();p.add_argument('--java-home',required=True,type=Path);p.add_argument('--classpath',required=True);p.add_argument('--legacy-jar',required=True,type=Path);p.add_argument('--resources',type=Path);a=p.parse_args()
here=Path(__file__).resolve().parent;root=here.parents[2];src=root/'src/main/java';resources=a.resources or root/'src/main/resources'
subprocess.run([os.sys.executable,str(here/'check-source.py')],check=True)
production=['com/hbm/lib/RefStrings.java','com/hbm/main/ModSoundEvents.java','com/hbm/sound/SoundCategoryResolver.java','com/hbm/sound/WorldSoundEffects.java','com/hbm/sound/WorldSoundCategories.java']
with tempfile.TemporaryDirectory(prefix='world-sound-native-') as d:
 out=Path(d);empty=out/'empty-sourcepath';empty.mkdir();files=[src/x for x in production]+[here/'WorldSoundRegistryCategoryTest.java',here/'NativeWorldSoundCallbackProbe.java',here/'PacketParityProbe.java',here/'ReceiverRangeTest.java',here.parent/'ForgeEventNativeLauncher.java']
 for f in files:print('INPUT',f.name,hashlib.sha256(f.read_bytes()).hexdigest(),flush=True)
 javac=[str(a.java_home/'bin/javac'),'-encoding','UTF-8','-source','8','-target','8','-sourcepath',str(empty),'-cp',a.classpath+os.pathsep+str(a.java_home/'lib/tools.jar'),'-d',d]
 subprocess.run(javac+list(map(str,files)),check=True)
 cp=d+os.pathsep+a.classpath+os.pathsep+str(a.java_home/'lib/tools.jar')
 # Native types and exact destination descriptor; legacy API and double pitch fail.
 signature=out/'WorldSoundCallSignature.java'
 body='import net.minecraft.world.World; public class WorldSoundCallSignature { static void play(World w,double x,double y,double z,String n,float v,float p) { %s; } }'
 signature.write_text(body%'com.hbm.sound.WorldSoundEffects.play(w,x,y,z,n,v,p)');subprocess.run(javac[:-4]+['-cp',cp,'-d',d,str(signature)],check=True)
 for label,call in [('legacy','w.playSoundEffect(x,y,z,n,v,p)'),('wrong-pitch','com.hbm.sound.WorldSoundEffects.play(w,x,y,z,n,v,(double)p)')]:
  signature.write_text(body%call);r=subprocess.run(javac[:-4]+['-cp',cp,'-d',d,str(signature)],text=True,capture_output=True);assert r.returncode!=0,(label,r.stdout,r.stderr);assert 'cannot find symbol' in r.stderr if label=='legacy' else 'possible lossy conversion' in r.stderr;print('PASS native compile negative control',label,flush=True)
 subprocess.run([str(a.java_home/'bin/java'),'-cp',cp,'ReceiverRangeTest',cp],cwd=d,check=True)
 java=[str(a.java_home/'bin/java'),'-Djava.awt.headless=true','-cp',cp,'ForgeEventNativeLauncher']
 subprocess.run(java+['WorldSoundRegistryCategoryTest',str(resources.resolve()),str(here/'source-manifest.json')],cwd=d,check=True)
 subprocess.run(java+['NativeWorldSoundCallbackProbe',str(resources.resolve())],cwd=d,check=True)
 subprocess.run(java+['PacketParityProbe',str(a.legacy_jar.resolve())],cwd=d,check=True)
 print('PASS complete bounded production native compile/probes; no full mod compile, launched game or audible verification',flush=True)
