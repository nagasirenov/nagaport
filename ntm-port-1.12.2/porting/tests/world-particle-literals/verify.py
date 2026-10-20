#!/usr/bin/env python3
"""Exact CP79 inverse plus genuine native World dispatch and extracted explosion callback."""
import argparse,json,pathlib,subprocess,tempfile,os
p=argparse.ArgumentParser();p.add_argument('--classpath',required=True);p.add_argument('--java-home',required=True);a=p.parse_args()
t=pathlib.Path(__file__).resolve().parent;r=t.parents[2];m=json.loads((t/'scope.json').read_text())
for name,changes in m['files'].items():
 path='ntm-port-1.12.2/src/main/java/'+name
 baseline=subprocess.check_output(['git','show',m['base']+':'+path],cwd=r)
 actual=(r/'src/main/java'/name).read_bytes(); inverse=actual
 for c in changes:
  before=c['before'].encode();after=c['after'].encode()
  assert inverse.count(after)>=1,(name,c['line'],'missing selected call')
  inverse=inverse.replace(after,before,1)
 assert inverse==baseline,(name,'non-token change')
print('PASS exact inverse: 115 name-token changes across 67 files; all other bytes equal CP79',flush=True)
s=(r/'src/main/java/com/hbm/explosion/vanillant/standard/ExplosionEffectStandard.java').read_text();start=s.index('public static void performClient(');i=s.index('{',start)+1;depth=1
while depth:
 if s[i]=='{':depth+=1
 elif s[i]=='}':depth-=1
 i+=1
callback='import net.minecraft.world.World;import net.minecraft.util.math.*;import java.util.*;public class SelectedExplosionCallback {'+s[start:i]+'}'
rows=',\n'.join('{"'+k+'","'+v+'"}' for k,v in m['mapping'].items())
source=(t/'WorldParticleLiteralTest.java').read_text().replace('/*MAPPING*/',rows)
with tempfile.TemporaryDirectory(prefix='native-world-particle-literals-') as out:
 out=pathlib.Path(out);(out/'SelectedExplosionCallback.java').write_text(callback);(out/'WorldParticleLiteralTest.java').write_text(source)
 subprocess.run([a.java_home+'/bin/javac','-source','8','-target','8','-cp',a.classpath,'-d',str(out),str(out/'SelectedExplosionCallback.java'),str(out/'WorldParticleLiteralTest.java')],check=True)
 subprocess.run([a.java_home+'/bin/java','-cp',str(out)+os.pathsep+a.classpath,'WorldParticleLiteralTest'],cwd=out,check=True)
