#!/usr/bin/env python3
"""Real compiler rejection controls; no fake original/native engine classes."""
from pathlib import Path
import subprocess,sys
java,cp,legacy,out=sys.argv[1:];out=Path(out);neg=out/'negative';neg.mkdir()
checks=0
for old in (True,False):
 for label in ('double-volume','object-receiver'):
  name=('Old' if old else 'Native')+label.title().replace('-','')+'Negative'
  world=('ahb' if old else 'net.minecraft.world.World') if label!='object-receiver' else 'Object'
  volume='1D' if label=='double-volume' else '1F'
  args='1, 2, 3, "hbm:player.vomit", '+volume+', 1F'
  call='w.a('+args+')' if old else 'com.hbm.sound.WorldSoundEffects.play(w, '+args+')'
  source=neg/(name+'.java');source.write_text('class '+name+' { static void test('+world+' w) { '+call+'; } }\n')
  result=subprocess.run([java+'/bin/javac','-encoding','UTF-8','-source','8','-target','8','-sourcepath',str(out/'empty-sourcepath'),'-cp',str(out/'classes')+':'+cp+(':'+legacy if old else ''),'-d',str(neg),str(source)],capture_output=True,text=True)
  (neg/(name+'.log')).write_text(result.stdout+result.stderr)
  assert result.returncode!=0,(name,'accepted illegal conversion/receiver')
  assert 'possible lossy conversion' in result.stderr if label=='double-volume' else ('cannot find symbol' in result.stderr or 'incompatible types' in result.stderr),(name,result.stderr)
  checks+=1
print('PASS '+str(checks)+' original/native compile-negative controls: no double-to-float narrowing and no Object receiver accepted.')
