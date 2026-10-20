#!/usr/bin/env python3
import argparse,collections,hashlib,importlib.util,json,re,subprocess,tempfile
from pathlib import Path
H=Path(__file__).resolve().parent;M=H.parents[2];SRC=M/'src/main/java'
def run(args,**kw):return subprocess.run(list(map(str,args)),check=True,**kw)
def main():
 p=argparse.ArgumentParser();p.add_argument('--java-home',type=Path,required=True);p.add_argument('--classpath-file',type=Path,required=True);p.add_argument('--legacy-client',type=Path,required=True);p.add_argument('--mutants',action='store_true');a=p.parse_args();cp=a.classpath_file.read_text().strip()
 assert hashlib.sha256(a.legacy_client.read_bytes()).hexdigest()=='a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce'
 for path,digest in json.loads((H/'dependency-hashes.json').read_text()).items():assert hashlib.sha256((M/path).read_bytes()).hexdigest()==digest,path
 spec=importlib.util.spec_from_file_location('ore',H/'owners.py');ore=importlib.util.module_from_spec(spec);spec.loader.exec_module(ore)
 rows=json.loads((H/'selection.json').read_text());assert len(rows)==134;groups=collections.defaultdict(list)
 for r in rows:groups[(r['file'],r['method'])].append(r)
 with tempfile.TemporaryDirectory(prefix='barbed-palette-') as td:
  t=Path(td);files=ore.generate(t)
  def put(name,source):
   f=t/name;f.parent.mkdir(parents=True,exist_ok=True);f.write_text(source);files.append(f)
  # Exact bounded Library fields; random concrete is not exercised and fails closed.
  lib=(SRC/'com/hbm/lib/Library.java').read_text();fields=re.findall(r'^\s*public static final ForgeDirection \w+ = ForgeDirection\.\w+;',lib,re.M);assert len(fields)==6
  put('com/hbm/lib/Library.java','package com.hbm.lib;import net.minecraft.block.Block;public class Library {'+'\n'.join(fields)+' public static Block getRandomConcrete(){throw new AssertionError("Unexercised concrete selector");}}')
  put('com/hbm/inventory/RecipesCommon.java','package com.hbm.inventory;import net.minecraft.block.Block;public class RecipesCommon {'+ore.extract((SRC/'com/hbm/inventory/RecipesCommon.java').read_text(),'public static class MetaBlock {')+'}')
  files.extend([SRC/'com/hbm/lib/ForgeDirection.java',SRC/'com/hbm/world/generator/DungeonToolbox.java'])
  native=(H.parent/'dungeon-palette-native/NativeProof.java').read_text();native=native[:native.index(' public static void main(')]+''' public static void main(String[]args)throws Exception {BarbedNativeTest.main(args);loadBounds(args[0]);GeneratedProof.run();EvaluationProof.run();System.out.println("PASS "+checks+" checks and "+events+" exact ordered barbed native/original-expression placement events.");}\n}\n''';put('NativeProof.java',native)
  ev=(H.parent/'ct-palette-native/EvaluationProof.java').read_text().replace('ConcreteConstructionSlice.deco_steel','com.hbm.blocks.ModBlocks.barbed_wire').replace('ConcreteConstructionSlice.deco_lead','com.hbm.blocks.ModBlocks.barbed_wire');put('EvaluationProof.java',ev)
  parts=['import net.minecraft.world.World;import net.minecraft.block.Block;import com.hbm.blocks.ModBlocks;public class GeneratedProof extends NativeProof {'];runs=[]
  for i,((file,method),rs) in enumerate(groups.items()):
   parts.append('static class Fixture'+str(i)+' {World world;int x,y,z;void nativeRun(){'+'\n'.join(r['new'] for r in rs)+'} void originalRun(){'+'\n'.join(r['old'].replace('world.setBlock(','legacy(world, ')for r in rs)+'}}')
   runs.append(('{FixtureN f=new FixtureN();for(Block current:new Block[]{original,net.minecraft.init.Blocks.STONE}){com.hbm.blocks.ModBlocks.barbed_wire=current;for(int[]o:origins){f.x=o[0];f.y=o[1];f.z=o[2];RecordingWorld old=new RecordingWorld(),w=new RecordingWorld();f.world=old;f.originalRun();f.world=w;f.nativeRun();check(old.trace.equals(w.trace),"exact ordered mutable field stream");events+=w.trace.size();f.world=null;boolean oe=false,ne=false;try{f.originalRun();}catch(NullPointerException e){oe=true;}try{f.nativeRun();}catch(NullPointerException e){ne=true;}check(oe&&ne,"null receiver even outside bounds");}}}').replace('FixtureN','Fixture'+str(i)))
  parts.append('static void run(){Block original=com.hbm.blocks.ModBlocks.barbed_wire;'+''.join(runs)+'com.hbm.blocks.ModBlocks.barbed_wire=original;}}');put('GeneratedProof.java','\n'.join(parts))
  run([a.java_home/'bin/javac','-encoding','UTF-8','-sourcepath',t,'-cp',cp,'-d',t]+files)
  sizes=[]
  for i,rs in enumerate(groups.values()):
   dis=run([a.java_home/'bin/javap','-c','-p','-classpath',t,'GeneratedProof$Fixture'+str(i)],capture_output=True,text=True).stdout;b=dis.split('void nativeRun();')[1].split('void originalRun();')[0];assert b.count('DungeonToolbox.placeUnorientedStructureBlock:')==len(rs);sizes.append(int(re.findall(r'\n\s*(\d+): return',b)[-1])+1)
  (H/'method-sizes.json').write_text(json.dumps(sizes)+'\n');print('PASS actual unsplit selected-expression Code sizes:',sizes,flush=True)
  run([a.java_home/'bin/java','-Xmx500m','-cp',str(t)+':'+cp,'NativeProof',a.legacy_client],cwd=t)
  if a.mutants:
   source=(M/'src/main/java/com/hbm/blocks/generic/BarbedWire.java').read_text();mutant=t/'com/hbm/blocks/generic/BarbedWire.java';mutant.parent.mkdir(parents=True,exist_ok=True)
   changes=[('withProperty(ORIENTATION, 0)','withProperty(ORIENTATION, 1)'),('return 0;','return 1;'),('return null;','return FULL_BLOCK_AABB;')]
   for i,(old,new) in enumerate(changes):
    assert source.count(old)==1;mutant.write_text(source.replace(old,new));run([a.java_home/'bin/javac','-encoding','UTF-8','-sourcepath',t,'-cp',str(t)+':'+cp,'-d',t,mutant],stdout=subprocess.DEVNULL,stderr=subprocess.DEVNULL)
    result=subprocess.run(list(map(str,[a.java_home/'bin/java','-Xmx500m','-cp',str(t)+':'+cp,'NativeProof',a.legacy_client])),cwd=t,capture_output=True,text=True);assert result.returncode!=0 and 'java.lang.AssertionError:'in result.stderr
    print('PASS owner mutation rejected:',i,next(line for line in result.stderr.splitlines()if 'java.lang.AssertionError:'in line),flush=True)
if __name__=='__main__':main()
