#!/usr/bin/env python3
import argparse,subprocess,tempfile,json,hashlib
from pathlib import Path
H=Path(__file__).resolve().parent;R=H.parents[2]
p=argparse.ArgumentParser();p.add_argument('--java-home',type=Path,required=True);p.add_argument('--classpath',required=True);p.add_argument('--original-client',type=Path,required=True);p.add_argument('--mutants',action='store_true');a=p.parse_args()
for rel,sha in json.loads((H/'reused-inputs.json').read_text()).items():assert hashlib.sha256((R/rel).read_bytes()).hexdigest()==sha,rel
java=a.java_home/'bin/java';javac=a.java_home/'bin/javac'
def run(cmd,**kw):return subprocess.run(list(map(str,cmd)),check=True,**kw)
with tempfile.TemporaryDirectory(prefix='owned-height-assessment-') as t:
 t=Path(t);classes=t/'classes';classes.mkdir();cp=str(classes)+':'+a.classpath
 run([javac,'-cp',a.classpath,'-d',classes,R/'src/main/java/com/hbm/world/WorldHeight.java',H.parent/'native-world-height/HeightNativeTest.java',H/'OwnedHeightCandidate.java',H/'OwnedHeightProbe.java'])
 run([java,'-cp',cp,'OwnedHeightProbe',a.original_client],cwd=t)
 if a.mutants:
  s=(H/'OwnedHeightCandidate.java').read_text();variants=[s.replace('provider.loadedChunks.get(key)','provider.getLoadedChunk(x>>4,z>>4)'),s.replace('if(world.getClass()!=WorldServer.class&&world.getClass()!=WorldServerMulti.class)return OptionalInt.empty();',''),s.replace('provider.getClass()!=ChunkProviderServer.class||',''),s.replace('if(!provider.loadedChunks.containsKey(key))return OptionalInt.of(0);',''),s.replace('if(chunk==null)return OptionalInt.empty();','if(chunk==null)return OptionalInt.of(0);'),s.replace('chunk.getHeightValue(x&15,z&15)','chunk.getHeightValue(z&15,x&15)')]
  path=t/'OwnedHeightCandidate.java'
  for i,v in enumerate(variants):
   path.write_text(v);run([javac,'-cp',a.classpath,'-d',classes,path]);r=subprocess.run([str(java),'-cp',cp,'OwnedHeightProbe',str(a.original_client)],cwd=t,stdout=subprocess.PIPE,stderr=subprocess.STDOUT);assert r.returncode!=0,('mutant survived',i)
  print('PASS: six queue/World-owner/provider-owner/absent-key/null-value/coordinate mutants rejected')
