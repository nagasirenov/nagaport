#!/usr/bin/env python3
import argparse,collections,hashlib,json,re,subprocess,tempfile,zipfile
from pathlib import Path
H=Path(__file__).resolve().parent;M=H.parents[2];SRC=M/'src/main/java'
def run(args,**kw):return subprocess.run(list(map(str,args)),check=True,**kw)
def main():
 p=argparse.ArgumentParser();p.add_argument('--java-home',type=Path,required=True);p.add_argument('--classpath-file',type=Path,required=True);p.add_argument('--legacy-client',type=Path,required=True);a=p.parse_args();cp=a.classpath_file.read_text().strip()
 assert hashlib.sha256(a.legacy_client.read_bytes()).hexdigest()=='a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce'
 for path,digest in json.loads((H/'dependency-hashes.json').read_text()).items():assert hashlib.sha256((M/path).read_bytes()).hexdigest()==digest,path
 def extract(s,sig):
  a=s.index(sig);i=s.index('{',a)+1;n=1
  while n:
   n+=(s[i]=='{')-(s[i]=='}');i+=1
  return s[a:i]
 with zipfile.ZipFile(H/'baseline.zip') as baseline:
  for path in ['src/main/java/com/hbm/blocks/generic/YellowBarrel.java','src/main/java/com/hbm/blocks/generic/BlockFlammable.java','src/main/java/com/hbm/blocks/bomb/BlockDetonatable.java','src/main/java/com/hbm/blocks/BlockBase.java','src/main/java/com/hbm/blocks/ModBlocks.java','src/main/java/com/hbm/world/generator/DungeonToolbox.java']:
   assert (M/path).read_bytes()==baseline.read(path),'Production dependency changed: '+path
 assert 'yellow_barrel = new YellowBarrel(Material.IRON)' in (SRC/'com/hbm/blocks/ModBlocks.java').read_text()
 for cls in ['DesertAtom001','DesertAtom002','DesertAtom003']:
  assert 'Block Block2 = ModBlocks.yellow_barrel;' in (SRC/('com/hbm/world/dungeon/'+cls+'.java')).read_text()
 run(['python3',H/'migrate.py','--java-home',a.java_home,'--classpath-file',a.classpath_file])
 rows=json.loads((H/'selection.json').read_text());assert len(rows)==105;groups=collections.defaultdict(list)
 for r in rows:groups[(r['file'],r['method'])].append(r)
 with tempfile.TemporaryDirectory(prefix='yellow-palette-') as td:
  t=Path(td);files=[]
  def put(name,source):
   f=t/name;f.parent.mkdir(parents=True,exist_ok=True);f.write_text(source);files.append(f)
  for path,source in json.loads((H/'collaborators.json').read_text()).items():put(path,source)
  production=['com/hbm/blocks/generic/YellowBarrel.java', 'com/hbm/blocks/generic/BlockFlammable.java', 'com/hbm/blocks/bomb/BlockDetonatable.java', 'com/hbm/blocks/BlockBase.java', 'com/hbm/render/icon/IIcon.java', 'com/hbm/render/icon/IIconRegister.java', 'api/hbm/block/IFuckingExplode.java', 'com/hbm/lib/RefStrings.java', 'com/hbm/lib/ForgeDirection.java', 'com/hbm/world/generator/DungeonToolbox.java']
  files.extend(SRC/p for p in production if p not in ['com/hbm/world/generator/DungeonToolbox.java','com/hbm/lib/ForgeDirection.java'])
  # Exact bounded Library fields; random concrete is not exercised and fails closed.
  lib=(SRC/'com/hbm/lib/Library.java').read_text();fields=re.findall(r'^\s*public static final ForgeDirection \w+ = ForgeDirection\.\w+;',lib,re.M);assert len(fields)==6
  put('com/hbm/lib/Library.java','package com.hbm.lib;import net.minecraft.block.Block;public class Library {'+'\n'.join(fields)+' public static Block getRandomConcrete(){throw new AssertionError("Unexercised concrete selector");}}')
  put('com/hbm/inventory/RecipesCommon.java','package com.hbm.inventory;import net.minecraft.block.Block;public class RecipesCommon {'+extract((SRC/'com/hbm/inventory/RecipesCommon.java').read_text(),'public static class MetaBlock {')+'}')
  files.extend([SRC/'com/hbm/lib/ForgeDirection.java',SRC/'com/hbm/world/generator/DungeonToolbox.java'])
  native=(H.parent/'dungeon-palette-native/NativeProof.java').read_text();native=native[:native.index(' public static void main(')]+''' public static void main(String[]args)throws Exception {YellowStateProof.run();loadBounds(args[0]);GeneratedProof.run();EvaluationProof.run();for(int mutant=0;mutant<6;mutant++){boolean rejected=false;try{Class.forName("Mutant"+mutant).getDeclaredMethod("run").invoke(null);}catch(java.lang.reflect.InvocationTargetException ex){if(ex.getCause() instanceof AssertionError)rejected=true;else throw ex;}check(rejected,"behavioral mutant rejected "+mutant);}System.out.println("PASS "+checks+" checks and "+events+" exact ordered yellow-barrel native/original-expression placement events.");}\n}\n''';put('NativeProof.java',native)
  files.append(H/'YellowStateProof.java')
  ev=(H.parent/'ct-palette-native/EvaluationProof.java').read_text().replace('ConcreteConstructionSlice.deco_steel','com.hbm.blocks.ModBlocks.yellow_barrel').replace('ConcreteConstructionSlice.deco_lead','com.hbm.blocks.ModBlocks.yellow_barrel');put('EvaluationProof.java',ev)
  helper=extract((SRC/'com/hbm/world/generator/DungeonToolbox.java').read_text(),'public static void placeUnorientedStructureBlock(')
  changes=[('if(world == null) throw new NullPointerException();',''),('x < 30000000','x <= 30000000'),('y < 256','y <= 256'),('block.getStateFromMeta(0), 3','block.getStateFromMeta(0), 2'),('block.getStateFromMeta(0), 3','net.minecraft.init.Blocks.STONE.getDefaultState(), 3'),('world.setBlockState(new BlockPos(x, y, z), block.getStateFromMeta(0), 3);','')]
  for n,(old,new) in enumerate(changes):
   assert helper.count(old)==1
   put('Mutant'+str(n)+'.java',ev.replace('public class EvaluationProof','public class Mutant'+str(n)).replace('com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock','placeUnorientedStructureBlock').replace('static void run(){',helper.replace(old,new)+'\n static void run(){'))
  parts=['import net.minecraft.world.World;import net.minecraft.block.Block;public class GeneratedProof extends NativeProof {'];runs=[]
  for i,((file,method),rs) in enumerate(groups.items()):
   parts.append('static class Fixture'+str(i)+' {World world;int x,y,z;Block Block2;void nativeRun(){'+'\n'.join(r['new'] for r in rs)+'} void originalRun(){'+'\n'.join(r['old'].replace('world.setBlock(','legacy(world, ')for r in rs)+'}}')
   runs.append(('{FixtureN f=new FixtureN();for(Block current:new Block[]{com.hbm.blocks.ModBlocks.yellow_barrel,net.minecraft.init.Blocks.STONE}){f.Block2=current;for(int[]o:origins){f.x=o[0];f.y=o[1];f.z=o[2];RecordingWorld old=new RecordingWorld(),w=new RecordingWorld();f.world=old;f.originalRun();f.world=w;f.nativeRun();check(old.trace.equals(w.trace),"exact ordered mutable field stream");events+=w.trace.size();f.world=null;boolean oe=false,ne=false;try{f.originalRun();}catch(NullPointerException e){oe=true;}try{f.nativeRun();}catch(NullPointerException e){ne=true;}check(oe&&ne,"null receiver even outside bounds");}}}').replace('FixtureN','Fixture'+str(i)))
  parts.append('static void run(){'+''.join(runs)+'}}');put('GeneratedProof.java','\n'.join(parts))
  run([a.java_home/'bin/javac','-encoding','UTF-8','-sourcepath',t,'-cp',cp,'-d',t]+files)
  dis=run([a.java_home/'bin/javap','-c','-p','-classpath',t,'com.hbm.world.generator.DungeonToolbox'],capture_output=True,text=True).stdout
  assert 'static {}' not in dis
  body=dis.split('public static void placeUnorientedStructureBlock(')[1].split('public static void placeRandomConcrete(')[0]
  assert body.count('net/minecraft/world/World.setBlockState:')==1 and body.count('net/minecraft/block/Block.getStateFromMeta:')==1
  (H/'helper-bytecode.log').write_text(body)
  sizes=[]
  for i,rs in enumerate(groups.values()):
   dis=run([a.java_home/'bin/javap','-c','-p','-classpath',t,'GeneratedProof$Fixture'+str(i)],capture_output=True,text=True).stdout;b=dis.split('void nativeRun();')[1].split('void originalRun();')[0];assert b.count('DungeonToolbox.placeUnorientedStructureBlock:')==len(rs);sizes.append(int(re.findall(r'\n\s*(\d+): return',b)[-1])+1)
  (H/'method-sizes.json').write_text(json.dumps(sizes)+'\n');print('PASS actual unsplit selected-expression Code sizes:',sizes,flush=True)
  run([a.java_home/'bin/java','-Xmx500m','-cp',str(t)+':'+cp,'NativeProof',a.legacy_client],cwd=t)
if __name__=='__main__':main()
