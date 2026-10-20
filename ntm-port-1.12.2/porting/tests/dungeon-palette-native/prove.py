#!/usr/bin/env python3
import argparse,collections,hashlib,importlib.util,json,re,subprocess,tempfile
from pathlib import Path
HERE=Path(__file__).resolve().parent
from migrate import replacement

def main():
 p=argparse.ArgumentParser();p.add_argument('--java-home',type=Path,required=True);p.add_argument('--classpath',required=True);p.add_argument('--legacy-client',required=True);a=p.parse_args();assert hashlib.sha256(Path(a.legacy_client).read_bytes()).hexdigest()=='a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce';rows=json.loads((HERE/'selection.json').read_text());groups=collections.defaultdict(list)
 for path,digest in json.loads((HERE/'dependency-hashes.json').read_text()).items():assert hashlib.sha256((HERE.parents[2]/path).read_bytes()).hexdigest()==digest, 'Prerequisite drift: '+path
 for r in rows:groups[(r['file'],r['method'])].append(r)
 with tempfile.TemporaryDirectory(prefix='placement-proof-')as td:
  t=Path(td);parts=['import net.minecraft.world.World; import net.minecraft.init.Blocks; import net.minecraft.util.math.BlockPos; public final class GeneratedProof extends NativeProof {'];runs=[]
  for num,((file,method),rs)in enumerate(groups.items()):
   name='Fixture'+str(num);parts.append('static class '+name+' {');coords=' '.join(a for r in rs for a in r['args'][:3]);words=set(re.findall(r'\b[A-Za-z_$][\w$]*\b',coords))-{'this','int','getX','getY','getZ','pos'};worlds={r['receiver'].removeprefix('this.')for r in rs};assert not worlds&words
   parts.append('net.minecraft.block.Block Block1=ConcreteConstructionSlice.brick_concrete, Block2=ConcreteConstructionSlice.brick_concrete_cracked, Block3=ConcreteConstructionSlice.brick_concrete_broken;');parts.append('World '+', '.join(sorted(worlds))+';');parts.append('int '+', '.join(sorted(words))+';') if words else None
   if 'pos' in coords:parts.append(('int[] pos;'if 'pos['in coords else 'BlockPos pos;'))
   parts.append('void init(RecordingWorld w,int x,int y,int z) {'+' '.join(k+'=w;'for k in sorted(worlds)))
   for word in sorted(words):
    # Coordinate variable perturbations exercise every field, not whole surrounding control flow.
    axis='y'if ('y'in word.lower()or word in('j','b'))else 'z'if ('z'in word.lower()or word in('k','c'))else 'x'
    parts.append('this.'+word+'='+axis+';')
   if 'pos'in coords:parts.append('pos='+('new int[]{x,y,z}'if 'pos['in coords else 'new BlockPos(x,y,z)')+';')
   parts.append('}')
   # Keep all of each file in a single native method: catches GeyserLarge method size rather than hiding it by splitting.
   parts.append('void nativeRun(){'+'\n'.join(r['new'].replace('Library.getRandomConcrete()', 'ConcreteRandomSlice.getRandomConcrete()')for r in rs)+'}')
   parts.append('void originalRun(){'+'\n'.join('legacy('+r['receiver']+', '+', '.join(r['args']).replace('Library.getRandomConcrete()', 'ConcreteRandomSlice.getRandomConcrete()')+');'for r in rs)+'}')
   parts.append('}')
   runs.append('{ '+name+' f=new '+name+'(); for(int permutation=0;permutation<4;permutation++){f.Block1=ConcreteConstructionSlice.blocks()[permutation];f.Block2=ConcreteConstructionSlice.blocks()[(permutation+1)%4];f.Block3=ConcreteConstructionSlice.blocks()[(permutation+2)%4];for(int[] o:origins){RecordingWorld old=new RecordingWorld(),actual=new RecordingWorld();f.init(old,o[0],o[1],o[2]);ConcreteRandomSlice.seed(981723);f.originalRun();long expectedRng=ConcreteRandomSlice.nextLong();f.init(actual,o[0],o[1],o[2]);ConcreteRandomSlice.seed(981723);f.nativeRun();check(expectedRng==ConcreteRandomSlice.nextLong(),"same RNG state");check(old.trace.equals(actual.trace),"'+file+'");events+=actual.trace.size(); f.init(null,o[0],o[1],o[2]);ConcreteRandomSlice.seed(761);boolean oldThrew=false;try{f.originalRun();}catch(NullPointerException ex){oldThrew=true;}long nullRng=ConcreteRandomSlice.nextLong();ConcreteRandomSlice.seed(761);boolean threw=false;try{f.nativeRun();}catch(NullPointerException ex){threw=true;}check(oldThrew&&threw&&nullRng==ConcreteRandomSlice.nextLong(),"null receiver even outside bounds; same RNG consumption before throw"); }}}')
  parts.append('static void run(){'+''.join(runs)+'}}');(t/'GeneratedProof.java').write_text('\n'.join(parts))
  # Adversarial evaluation fixtures use the same source template as every production site.
  r={'receiver':'receiver()','args':['arg(1)','arg(2)','arg(3)','block()','arg(5)','arg(6)']}
  ev='''import net.minecraft.world.World;import net.minecraft.block.Block;import net.minecraft.init.Blocks;
public class EvaluationProof extends NativeProof {
 static java.util.Random random;static StringBuilder order;static int fail,invalid;static boolean nil;static RecordingWorld w;
 static void step(int n){random.nextInt();order.append(n);if(fail==n)throw new IllegalStateException("failure"+n);}
 static World receiver(){step(0);return nil?null:w;} static int arg(int n){step(n);return n==2?(invalid==1?256:64):n==1?(invalid==2?30000000:0):n==5?0:n==6?3:0;}
 static Block block(){step(4);return Blocks.STONE;}
 static void original(){legacy(receiver(),arg(1),arg(2),arg(3),block(),arg(5),arg(6));}
 static void nativeCall(){REPLACE}
 static World holder;static RecordingWorld other;static int mutate(){holder=other;return 0;}
 static void originalMutation(){legacy(holder,mutate(),64,0,Blocks.AIR);}
 static void nativeMutation(){MUTATION}
 static void run(){for(int n=0;n<2;n++){RecordingWorld target=new RecordingWorld();other=new RecordingWorld();holder=n==0?target:null;String oe="";try{originalMutation();}catch(RuntimeException ex){oe=ex.getClass().getName();}java.util.List<String> expected=new java.util.ArrayList<>(target.trace);target=new RecordingWorld();other=new RecordingWorld();holder=n==0?target:null;String ne="";try{nativeMutation();}catch(RuntimeException ex){ne=ex.getClass().getName();}check(oe.equals(ne)&&expected.equals(target.trace)&&other.trace.isEmpty(),"receiver captured before coordinate mutation");}for(invalid=0;invalid<3;invalid++)for(int nullCase=0;nullCase<2;nullCase++)for(fail=-1;fail<=6;fail++){nil=nullCase!=0;random=new java.util.Random(918273);w=new RecordingWorld();order=new StringBuilder();String oldEx="";try{original();}catch(RuntimeException e){oldEx=e.getClass().getName();}long rngExpected=random.nextLong();String expectedOrder=order.toString();java.util.List<String>expected=new java.util.ArrayList<>(w.trace);random=new java.util.Random(918273);w=new RecordingWorld();order=new StringBuilder();String newEx="";try{nativeCall();}catch(RuntimeException e){newEx=e.getClass().getName();}check(oldEx.equals(newEx)&&expectedOrder.equals(order.toString())&&expected.equals(w.trace)&&rngExpected==random.nextLong(),"left-to-right exactly-once failure/null/bounds "+invalid+"/"+nil+"/"+fail);}}
}'''.replace('REPLACE',replacement(r)).replace('MUTATION',replacement({'receiver':'holder','args':['mutate()','64','0','Blocks.AIR']}));(t/'EvaluationProof.java').write_text(ev)
  mutants=[]
  changes=[('if (nativePlacementWorld == null) throw new NullPointerException(); ', ''),('nativePlacementX < 30000000','nativePlacementX <= 30000000'),('nativePlacementY < 256','nativePlacementY <= 256'),('int nativePlacementFlags = arg(6);','int nativePlacementFlags = 2;'),('net.minecraft.block.Block nativePlacementBlock = block();','net.minecraft.block.Block nativePlacementBlock = block(); block();'),('nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags','nativePlacementBlock.getStateFromMeta(nativePlacementMeta), 2')]
  for index,(old,new) in enumerate(changes):
   assert old in ev
   path=t/('Mutant'+str(index)+'.java');path.write_text(ev.replace('public class EvaluationProof','public class Mutant'+str(index)).replace(old,new));mutants.append(path)
  spec=importlib.util.spec_from_file_location('concrete_states',HERE.parent/'generic-metadata-native/run-native.py');owner=importlib.util.module_from_spec(spec);spec.loader.exec_module(owner);generated=owner.generate_slices(t);production=[HERE.parents[2]/'src/main/java'/x for x in ['com/hbm/blocks/BlockBase.java','com/hbm/blocks/generic/BlockGeneric.java','com/hbm/lib/RefStrings.java','com/hbm/render/icon/IIcon.java','com/hbm/render/icon/IIconRegister.java']]
  javac=str(a.java_home/'bin/javac');java=str(a.java_home/'bin/java');subprocess.run([javac,'-encoding','UTF-8','-sourcepath',td,'-cp',a.classpath,'-d',td,str(HERE/'NativeProof.java'),str(t/'GeneratedProof.java'),str(t/'EvaluationProof.java')]+list(map(str,generated+production+mutants)),check=True)
  dis=subprocess.check_output([str(a.java_home/'bin/javap'),'-c','-p','-classpath',td,'GeneratedProof$Fixture0','GeneratedProof$Fixture1'],text=True); (HERE/'bytecode.log').write_text(dis)
  for b,rs in zip(dis.split('void nativeRun();')[1:],groups.values()):
   native=b.split('void originalRun()')[0];assert native.count('net/minecraft/world/World.setBlockState:')==len(rs) and native.count('net/minecraft/block/Block.getStateFromMeta:')==len(rs);assert native.count('getfield')>=len(rs)
  sizes=[int(re.findall(r'\n\s*(\d+): return',b.split('void originalRun()')[0])[-1])+1 for b in dis.split('void nativeRun();')[1:]]; assert len(sizes)==2 and all(s<65536 for s in sizes); print('Unsplit selected native sequences Code bytes:',sizes)
  subprocess.run([java,'-Xmx700m','-cp',td+':'+a.classpath,'NativeProof',a.legacy_client],check=True,cwd=td)
if __name__=='__main__':main()
