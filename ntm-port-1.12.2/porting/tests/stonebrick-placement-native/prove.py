#!/usr/bin/env python3
import argparse,collections,hashlib,importlib.util,json,re,subprocess,tempfile,zipfile
from pathlib import Path
H=Path(__file__).resolve().parent;M=H.parents[2]
def run(args,**kw):return subprocess.run(list(map(str,args)),check=True,**kw)
def main():
 p=argparse.ArgumentParser();p.add_argument('--java-home',type=Path,required=True);p.add_argument('--classpath-file',type=Path,required=True);p.add_argument('--legacy-client',type=Path,required=True);a=p.parse_args();cp=a.classpath_file.read_text().strip()
 assert hashlib.sha256(a.legacy_client.read_bytes()).hexdigest()=='a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce'
 spec=importlib.util.spec_from_file_location('ore',H.parent/'ore-callback-native/run.py');ore=importlib.util.module_from_spec(spec);spec.loader.exec_module(ore)
 rows=json.loads((H/'selection.json').read_text());assert len(rows)==415;groups=collections.defaultdict(list)
 for r in rows:groups[(r['file'],r['method'])].append(r)
 helper=(H/'helper.txt').read_text();production=(M/'src/main/java/com/hbm/world/dungeon/LibraryDungeon.java').read_text();assert helper in production;selector=ore.extract(production,'public int getBrick(Random rand)')
 with zipfile.ZipFile(H/'sources.zip')as z:assert selector==ore.extract(z.read('src/main/java/com/hbm/world/dungeon/LibraryDungeon.java').decode(),'public int getBrick(Random rand)')
 with tempfile.TemporaryDirectory(prefix='stonebrick-placement-')as td:
  t=Path(td);files=[H/'StoneBrickNativeTest.java']
  def put(name,text):f=t/name;f.write_text(text);files.append(f)
  native=(H.parent/'dungeon-palette-native/NativeProof.java').read_text();native=native[:native.index(' public static void main(')]+''' public static void main(String[]args)throws Exception {StoneBrickNativeTest.main(args);loadBounds(args[0]);GeneratedProof.run();EvaluationProof.run();for(int i=0;i<6;i++){boolean rejected=false;try{Class.forName("Mutant"+i).getDeclaredMethod("run").invoke(null);}catch(java.lang.reflect.InvocationTargetException ex){if(ex.getCause() instanceof AssertionError)rejected=true;else throw ex;}check(rejected,"helper mutation rejected "+i);}System.out.println("PASS "+checks+" total owner/placement checks and "+events+" exact ordered stone-brick events.");}\n}\n''';put('NativeProof.java',native)
  ev=(H/'EvaluationProof.java').read_text();put('EvaluationProof.java',ev.replace('HELPER',helper.replace('private static','static')))
  changes=[('if(world == null) throw new NullPointerException();',''),('x < 30000000','x <= 30000000'),('y < 256','y <= 256'),('brick.getStateFromMeta(metadata)','brick.getStateFromMeta(0)'),('brick.getStateFromMeta(metadata), 3','brick.getStateFromMeta(metadata), 2'),('world.setBlockState(new BlockPos(x, y, z), brick.getStateFromMeta(metadata), 3);','')]
  for i,(old,new) in enumerate(changes):
   assert helper.count(old)==1;put('Mutant'+str(i)+'.java',ev.replace('public class EvaluationProof','public class Mutant'+str(i)).replace('HELPER',helper.replace('private static','static').replace(old,new)))
  parts=['import net.minecraft.world.World;import net.minecraft.block.Block;import net.minecraft.init.Blocks;import net.minecraft.util.math.BlockPos;import java.util.Random;public class GeneratedProof extends NativeProof {',helper];runs=[]
  for i,((file,method),rs)in enumerate(groups.items()):
   parts.append('static class Fixture'+str(i)+' {World world;int x,y,z;Random rand;'+selector+' void nativeRun(){'+'\n'.join(r['new']for r in rs)+'}void originalRun(){'+'\n'.join(r['old'].replace(r['receiver']+'.setBlock(','legacy('+r['receiver']+', ')for r in rs)+'}}')
   runs.append(('{FixtureN f=new FixtureN();for(int seed=0;seed<12;seed++)for(int[]o:origins){f.x=o[0];f.y=o[1];f.z=o[2];RecordingWorld old=new RecordingWorld(),w=new RecordingWorld();f.rand=new Random(seed);f.world=old;f.originalRun();long rng=f.rand.nextLong();f.rand=new Random(seed);f.world=w;f.nativeRun();check(old.trace.equals(w.trace)&&rng==f.rand.nextLong(),"exact ordered stream and RNG continuation");events+=w.trace.size();f.world=null;f.rand=new Random(seed);boolean oe=false,ne=false;try{f.originalRun();}catch(NullPointerException e){oe=true;}rng=f.rand.nextLong();f.rand=new Random(seed);try{f.nativeRun();}catch(NullPointerException e){ne=true;}check(oe&&ne&&rng==f.rand.nextLong(),"null receiver after selector draw, including invalid bounds");}}').replace('FixtureN','Fixture'+str(i)))
  parts.append('static void run(){'+''.join(runs)+'}}');put('GeneratedProof.java','\n'.join(parts))
  run([a.java_home/'bin/javac','-encoding','UTF-8','-sourcepath',t,'-cp',cp,'-d',t]+files)
  sizes=[]
  for i,rs in enumerate(groups.values()):
   dis=run([a.java_home/'bin/javap','-c','-p','-classpath',t,'GeneratedProof$Fixture'+str(i)],capture_output=True,text=True).stdout;b=dis.split('void nativeRun();')[1].split('void originalRun();')[0]
   if rs[0]['file'].endswith('LibraryDungeon.java'):assert b.count('getBrick:')==414 and b.count('GeneratedProof.access$')==414
   sizes.append(int(re.findall(r'\n\s*(\d+): return',b)[-1])+1)
  (H/'method-sizes.json').write_text(json.dumps(sizes)+'\n');print('PASS actual unsplit selected-expression Code sizes:',sizes,flush=True)
  run([a.java_home/'bin/java','-Xmx500m','-cp',str(t)+':'+cp,'NativeProof',a.legacy_client],cwd=t)
  # Measure actual compiler output for direct expansion rather than claiming an unmeasured size failure.
  spec=importlib.util.spec_from_file_location('prior',H.parent/'vanilla-placement-expanded/migrate.py');prior=importlib.util.module_from_spec(spec);spec.loader.exec_module(prior);rs=[r for r in rows if r['file'].endswith('LibraryDungeon.java')]
  direct=t/'DirectSequence.java';direct.write_text('import java.util.Random;import net.minecraft.world.World;import net.minecraft.init.Blocks;class DirectSequence {World world;int x,y,z;Random rand;'+selector+' void run(){'+'\n'.join(prior.replacement(r)for r in rs)+'}}')
  result=subprocess.run(list(map(str,[a.java_home/'bin/javac','-cp',cp,'-d',t,direct])),capture_output=True,text=True)
  if result.returncode:assert 'code too large' in result.stderr;report='Actual unsplit direct expansion rejected: '+result.stderr
  else:
   dis=run([a.java_home/'bin/javap','-c','-p','-classpath',t,'DirectSequence'],capture_output=True,text=True).stdout;size=int(re.findall(r'\n\s*(\d+): return',dis)[-1])+1;report='Actual unsplit direct-expansion Code size: '+str(size)+' bytes. No claim of full-generator bytecode size; remaining generator source is unported.\n'
  (H/'direct-size.log').write_text(report);print(report,flush=True)
if __name__=='__main__':main()
