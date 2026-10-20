#!/usr/bin/env python3
import argparse,subprocess,tempfile,struct
from pathlib import Path
import check_source
H=check_source.HERE;R=check_source.ROOT
p=argparse.ArgumentParser();p.add_argument('--java-home',type=Path,required=True);p.add_argument('--classpath',required=True);p.add_argument('--original-client',type=Path,required=True);p.add_argument('--mutants',action='store_true');a=p.parse_args();check_source.check()
def run(cmd,**kw):return subprocess.run(list(map(str,cmd)),check=True,**kw)
javac=a.java_home/'bin/javac';java=a.java_home/'bin/java';helper=R/'src/main/java/com/hbm/world/WorldHeight.java'
with tempfile.TemporaryDirectory(prefix='height-native-') as t:
 t=Path(t);classes=t/'classes';classes.mkdir();cp=str(classes)+':'+a.classpath
 run([javac,'-cp',a.classpath,'-d',classes,helper,H/'HeightNativeTest.java'])
 run([java,'-cp',cp,'HeightNativeTest',a.original_client],cwd=t)
 run([javac,'-cp',str(a.original_client)+':'+a.classpath,'-d',classes,H/'OriginalHeightEvaluation.java'])
 raw=(classes/'OriginalHeightEvaluation.class').read_bytes();old=b'OriginalHeightEvaluation';new=b'proof/OriginalHeightEvaluation';needle=b'\1'+struct.pack('>H',len(old))+old;replacement=b'\1'+struct.pack('>H',len(new))+new;assert raw.count(needle)==1
 dst=classes/'proof/OriginalHeightEvaluation.class';dst.parent.mkdir();dst.write_bytes(raw.replace(needle,replacement));(classes/'OriginalHeightEvaluation.class').unlink()
 run([java,'-cp',cp+':'+str(a.original_client),'proof.OriginalHeightEvaluation'],cwd=t)
 run([javac,'-cp',a.java_home/'lib/tools.jar','-d',classes,H/'HeightScanner.java'])
 controls=t/'Controls.java';controls.write_bytes((H/'negative-controls.java.in').read_bytes());inputs=t/'inputs';inputs.write_text(str(controls));cpf=t/'cp';cpf.write_text(a.classpath)
 r=run([java,'-cp',str(classes)+':'+str(a.java_home/'lib/tools.jar'),'HeightScanner',inputs,cpf],stdout=subprocess.PIPE,universal_newlines=True);assert len(r.stdout.splitlines())==2,r.stdout;print('PASS: two positive and eight rejected receiver/type/arity/error height-attribution controls')
 run([javac,'-cp',a.java_home/'lib/tools.jar','-d',classes,H/'DestinationScanner.java'])
 scope=t/'Scope.java';scope.write_text('import net.minecraft.world.World; class Scope {World world; void onUpdate(){if(this.world.isRemote){com.hbm.world.WorldHeight.read(this.world,1,2);} if(!this.world.isRemote){com.hbm.world.WorldHeight.read(this.world,1,2);} com.hbm.world.WorldHeight.read(this.world,1,2);}}')
 inputs.write_text(str(scope)+'\n'+str(helper));r=run([java,'-cp',str(classes)+':'+str(a.java_home/'lib/tools.jar'),'DestinationScanner',inputs,cpf],stdout=subprocess.PIPE,universal_newlines=True);assert [v.split('\t')[-1] for v in r.stdout.splitlines()]==['true','false','false'],r.stdout
 print('PASS: client true-branch scope accepted; negated and unguarded scope mutants rejected')
 if a.mutants:
  s=helper.read_text();variants=[s.replace('x < -30000000','x <= -30000000'),s.replace('z >= 30000000','z > 30000000'),s.replace('return 64;','return world.getSeaLevel() + 1;'),s.replace('Objects.requireNonNull(world);',''),s.replace('world.getHeight(x, z)','world.getHeight(z, x)'),s.replace('return world.getHeight(x, z);','return 0;')]
  mutant=t/'WorldHeight.java'
  for i,v in enumerate(variants):
   mutant.write_text(v);run([javac,'-cp',a.classpath,'-d',classes,mutant]);result=subprocess.run([str(java),'-cp',cp,'HeightNativeTest',str(a.original_client)],cwd=t,stdout=subprocess.PIPE,stderr=subprocess.STDOUT);assert result.returncode!=0,('survived',i)
  print('PASS: six bounds/sea-level/null/coordinate/height mutants rejected')
