#!/usr/bin/env python3
import argparse,collections,hashlib,importlib.util,json,re,subprocess,tempfile,zipfile
from pathlib import Path
H=Path(__file__).resolve().parent;M=H.parents[2];SRC=M/'src/main/java'
def run(args,**kw):return subprocess.run(list(map(str,args)),check=True,**kw)
def extract(s,sig):
 a=s.index(sig);i=s.index('{',a)+1;n=1
 while n:
  n+=(s[i]=='{')-(s[i]=='}');i+=1
 return s[a:i]
def main():
 p=argparse.ArgumentParser();p.add_argument('--java-home',type=Path,required=True);p.add_argument('--classpath-file',type=Path,required=True);p.add_argument('--legacy-client',type=Path,required=True);a=p.parse_args();cp=a.classpath_file.read_text().strip();java=a.java_home/'bin/java';javac=a.java_home/'bin/javac'
 assert hashlib.sha256(a.legacy_client.read_bytes()).hexdigest()=='a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce'
 for path,digest in json.loads((H/'dependency-hashes.json').read_text()).items():assert hashlib.sha256((M/path).read_bytes()).hexdigest()==digest, 'Prerequisite changed: '+path
 rows=json.loads((H/'selection.json').read_text());assert len(rows)==1298;groups=collections.defaultdict(list)
 for r in rows:groups[(r['file'],r['method'])].append(r)
 with zipfile.ZipFile(H/'helper-baseline.zip') as z:oldbox=z.read('DungeonToolbox.java').decode()
 helper=(H/'helper.txt').read_text();actual=(SRC/'com/hbm/world/generator/DungeonToolbox.java').read_text();assert actual==oldbox.replace('public class DungeonToolbox {','public class DungeonToolbox {\n'+helper,1)
 assert extract(actual,'public static void placeRandomConcrete(')==extract(oldbox,'public static void placeRandomConcrete(')
 assert 'static {' not in actual
 spec=importlib.util.spec_from_file_location('owners',H/'run-owners.py');owners=importlib.util.module_from_spec(spec);spec.loader.exec_module(owners);owners.check_source()
 with tempfile.TemporaryDirectory(prefix='structure-palette-') as td:
  t=Path(td);generated=owners.generate_slices(t)
  # Complete DungeonToolbox against the exact bounded Library fields/selector and nested MetaBlock.
  lib=(SRC/'com/hbm/lib/Library.java').read_text();fields=re.findall(r'^\s*(?:public static final ForgeDirection \w+ = ForgeDirection\.\w+;|static Random rand = new Random\(\);)',lib,re.M);assert len(fields)==7 and 'static {' not in lib
  package=t/'com/hbm/lib';package.mkdir(parents=True);con=t/'ConcreteConstructionSlice.java';(package/con.name).write_text('package com.hbm.lib;\n'+con.read_text())
  (package/'Library.java').write_text('package com.hbm.lib; import java.util.Random; import net.minecraft.block.Block; public class Library {\n'+'\n'.join(fields)+'\n'+extract(lib,'public static Block getRandomConcrete()').replace('ModBlocks.','ConcreteConstructionSlice.')+'\n}')
  inv=t/'com/hbm/inventory';inv.mkdir(parents=True);(inv/'RecipesCommon.java').write_text('package com.hbm.inventory; import net.minecraft.block.Block; public class RecipesCommon {\n'+extract((SRC/'com/hbm/inventory/RecipesCommon.java').read_text(),'public static class MetaBlock {')+'\n}')
  generated += [package/'Library.java',package/con.name,inv/'RecipesCommon.java',SRC/'com/hbm/world/generator/DungeonToolbox.java']
  production=[SRC/p for p in ['com/hbm/blocks/BlockBase.java','com/hbm/blocks/generic/BlockGeneric.java','com/hbm/lib/RefStrings.java','com/hbm/render/icon/IIcon.java','com/hbm/render/icon/IIconRegister.java','com/hbm/lib/ForgeDirection.java']]
  parts=['import net.minecraft.world.World; import net.minecraft.block.Block; public class GeneratedProof extends NativeProof {'];runs=[]
  for i,((file,method),rs) in enumerate(groups.items()):
   parts.append('static class Fixture'+str(i)+' { World world; int x,y,z; Block Block3=ConcreteConstructionSlice.reinforced_sand, Block6=ConcreteConstructionSlice.brick_light; void nativeRun(){\n'+'\n'.join(r['new'] for r in rs)+'\n} void originalRun(){\n'+'\n'.join(r['old'].replace('world.setBlock(','legacy(world, ') for r in rs)+'\n}}')
   runs.append('''{FixtureN f=new FixtureN(); for(int permutation=0;permutation<2;permutation++){f.Block3=ConcreteConstructionSlice.blocks()[4+permutation];f.Block6=ConcreteConstructionSlice.blocks()[5-permutation];for(int[] o:origins){f.x=o[0];f.y=o[1];f.z=o[2];RecordingWorld old=new RecordingWorld(),w=new RecordingWorld();f.world=old;f.originalRun();f.world=w;f.nativeRun();check(old.trace.equals(w.trace),"full selected ordered stream");events+=w.trace.size();f.world=null;boolean oe=false,ne=false;try{f.originalRun();}catch(NullPointerException e){oe=true;}try{f.nativeRun();}catch(NullPointerException e){ne=true;}check(oe&&ne,"null receiver including invalid coordinates");}}}'''.replace('FixtureN','Fixture'+str(i)))
  parts.append('static void run(){'+''.join(runs)+'}}');(t/'GeneratedProof.java').write_text('\n'.join(parts))
  (t/'NativeProof.java').write_text((H.parent/'dungeon-palette-native/NativeProof.java').read_text())
  ev=(H/'EvaluationProof.java').read_text();(t/'EvaluationProof.java').write_text(ev)
  changes=[('if(world == null) throw new NullPointerException();',''),('x < 30000000','x <= 30000000'),('y < 256','y <= 256'),('block.getStateFromMeta(0), 3','block.getStateFromMeta(0), 2'),('block.getStateFromMeta(0), 3','net.minecraft.init.Blocks.STONE.getDefaultState(), 3'),('world.setBlockState(new BlockPos(x, y, z), block.getStateFromMeta(0), 3);','')]
  for n,(old,new) in enumerate(changes):
   assert helper.count(old)==1
   (t/('Mutant'+str(n)+'.java')).write_text(ev.replace('public class EvaluationProof','public class Mutant'+str(n)).replace('com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock','placeUnorientedStructureBlock').replace('static void run(){',helper.replace(old,new)+'\n static void run(){'));generated.append(t/('Mutant'+str(n)+'.java'))
  run([javac,'-encoding','UTF-8','-sourcepath',t,'-cp',cp,'-d',t,t/'NativeProof.java',t/'GeneratedProof.java',t/'EvaluationProof.java']+generated+production)
  dis=run([a.java_home/'bin/javap','-c','-p','-classpath',t,'com.hbm.world.generator.DungeonToolbox'],capture_output=True,text=True).stdout;assert 'static {}' not in dis
  b=dis.split('public static void placeUnorientedStructureBlock(')[1].split('public static void placeRandomConcrete(')[0];assert b.count('net/minecraft/world/World.setBlockState:')==1 and b.count('net/minecraft/block/Block.getStateFromMeta:')==1;(H/'helper-attribution.log').write_text(b.rstrip()+'\n')
  sizes=[]
  for i,rs in enumerate(groups.values()):
   dis=run([a.java_home/'bin/javap','-c','-p','-classpath',t,'GeneratedProof$Fixture'+str(i)],capture_output=True,text=True).stdout;b=dis.split('void nativeRun();')[1].split('void originalRun();')[0];assert b.count('DungeonToolbox.placeUnorientedStructureBlock:')==len(rs);size=int(re.findall(r'\n\s*(\d+): return',b)[-1])+1;sizes.append(size)
  print('PASS unsplit selected native method Code sizes:',sizes);(H/'method-sizes.json').write_text(json.dumps(sizes)+'\n')
  run([java,'-Xmx700m','-cp',str(t)+':'+cp,'NativeProof',a.legacy_client],cwd=t)
  # Demonstrate real JVM method-size rejection for the same largest selected sequence if expanded directly.
  spec=importlib.util.spec_from_file_location('prior',H.parent/'vanilla-placement-expanded/migrate.py');prior=importlib.util.module_from_spec(spec);spec.loader.exec_module(prior)
  rs=max(groups.values(),key=len);(t/'DirectExpansion.java').write_text('import net.minecraft.world.World; import net.minecraft.block.Block; class DirectExpansion {Block Block3,Block6; void run(World world,int x,int y,int z){\n'+'\n'.join(prior.replacement(r) for r in rs)+'\n}}')
  direct=subprocess.run(list(map(str,[javac,'-encoding','UTF-8','-sourcepath',t,'-cp',cp,'-d',t,t/'DirectExpansion.java'])),capture_output=True,text=True);assert direct.returncode!=0 and 'code too large' in direct.stderr; (H/'direct-size-rejection.log').write_text(direct.stderr);print('PASS actual Java8 direct expansion rejected as code too large:',len(rs),'sites.')
if __name__=='__main__':main()
