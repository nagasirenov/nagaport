#!/usr/bin/env python3
"""917-site sequence + complete DungeonToolbox. Explicit bounded Library/MetaBlock fixtures."""
import argparse,hashlib,importlib.util,json,re,subprocess,tempfile,zipfile,struct
from pathlib import Path
H=Path(__file__).resolve().parent

def extract(s,signature):
 start=s.index(signature);i=s.index('{',start)+1;depth=1
 while depth:
  if s[i]=='{':depth+=1
  if s[i]=='}':depth-=1
  i+=1
 return s[start:i]
def run(cmd,**kw):return subprocess.run(list(map(str,cmd)),check=True,**kw)
def main():
 p=argparse.ArgumentParser();p.add_argument('--integrated',action='store_true',help='Check exact selected-method bytes while allowing independently migrated other methods');p.add_argument('--module',type=Path,default=H.parents[2]);p.add_argument('--prerequisites',type=Path,required=True);p.add_argument('--java-home',type=Path,required=True);p.add_argument('--classpath-file',type=Path,required=True);p.add_argument('--legacy-client',type=Path,required=True);a=p.parse_args();java=a.java_home/'bin/java';javac=a.java_home/'bin/javac';cp=a.classpath_file.read_text().strip();src=a.module/'src/main/java';prev=a.prerequisites/'concrete-placement-native';owner=a.prerequisites/'generic-metadata-native'
 assert hashlib.sha256(a.legacy_client.read_bytes()).hexdigest()=='a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce'
 rows=json.loads((H/'selection.json').read_text());assert len(rows)==917
 dependencies=json.loads((H/'dependency-hashes.json').read_text())
 for relative,digest in dependencies.items():
  assert hashlib.sha256((a.prerequisites/relative).read_bytes()).hexdigest()==digest, 'Prerequisite changed: '+relative
 with zipfile.ZipFile(H/'baseline.zip') as z:old=z.read('Silo.java').decode();oldbox=z.read('DungeonToolbox.java').decode()
 helper=(H/'helper.txt').read_text();actual=(src/'com/hbm/world/generator/DungeonToolbox.java').read_text();assert actual==oldbox.replace('public class DungeonToolbox {','public class DungeonToolbox {\n'+helper,1)
 # Inverse byte proof permits sibling independent edits outside the selected method.
 original_method=extract(old,'public boolean generate_r0(');expected=original_method
 for r in rows:
  assert expected.count(r['old'])==1;expected=expected.replace(r['old'],r['new'],1)
 current=(src/'com/hbm/world/dungeon/Silo.java').read_text()
 assert extract(current,'public boolean generate_r0(')==expected
 if not a.integrated: assert current==old.replace(original_method,expected,1), 'Bytes outside selected method changed'
 for path,hashes in json.loads((H/'manifest.json').read_text())['files'].items():
  if not a.integrated: assert hashlib.sha256((a.module/path).read_bytes()).hexdigest()==hashes['after']
 assert 'static {' not in actual and not re.search(r'^\s*(?:public |private |protected )?static [^\n(]+=',actual,re.M)
 with tempfile.TemporaryDirectory(prefix='silo-concrete-') as td:
  t=Path(td);(t/'Silo.java').write_text(old);tools=a.java_home/'lib/tools.jar'
  run([javac,'-cp',tools,'-d',t,prev/'PlacementOwner.java'])
  attributed=run([java,'-cp',str(t)+':'+str(tools),'PlacementOwner',cp,t/'Silo.java',src/'com/hbm/lib/Library.java'],capture_output=True,text=True)
  rs=[json.loads(l) for l in attributed.stdout.splitlines()];rs=[r for r in rs if r['method']=='generate_r0' and r['world'] and r['vanilla'] and r['ints'] and r['statement'] and r['args'][4:]==['0','3']]
  assert len(rs)==917 and [old[r['start']:r['end']]+';' for r in rs]==[r['old'] for r in rows]
  print('PASS 917 exact original sites attributed to native World, int coordinates, static Library selector, statement-only metadata0/flags3.')
  spec=importlib.util.spec_from_file_location('owner',owner/'run-native.py');m=importlib.util.module_from_spec(spec);spec.loader.exec_module(m);generated=m.generate_slices(t)
  # Real owner classes reused from the independently tested prerequisite, never fake platform APIs.
  production=[m.JAVA/x for x in ['com/hbm/blocks/BlockBase.java','com/hbm/blocks/generic/BlockGeneric.java','com/hbm/lib/RefStrings.java','com/hbm/render/icon/IIcon.java','com/hbm/render/icon/IIconRegister.java','com/hbm/lib/ForgeDirection.java']]
  lib=src/'com/hbm/lib/Library.java';library=lib.read_text();fields=re.findall(r'^\s*(?:public static final ForgeDirection \w+ = ForgeDirection\.\w+;|static Random rand = new Random\(\);)',library,re.M);assert len(fields)==7 and 'static {' not in library
  with zipfile.ZipFile(H/'baseline.zip') as archive: assert lib.read_bytes()==archive.read('Library.java'), 'Library/static-init closure changed'
  assert extract(library,'public static Block getRandomConcrete()')==m.selected_sources()[2]
  package=t/'com/hbm/lib';package.mkdir(parents=True)
  con=t/'ConcreteConstructionSlice.java';(package/con.name).write_text('package com.hbm.lib;\n'+con.read_text());con.unlink();(t/'ConcreteRandomSlice.java').unlink()
  (package/'Library.java').write_text('package com.hbm.lib;\nimport java.util.Random; import net.minecraft.block.Block;\npublic class Library {\n'+'\n'.join(fields)+'\n'+extract(library,'public static Block getRandomConcrete()').replace('ModBlocks.','ConcreteConstructionSlice.')+'\npublic static void seed(long n){rand.setSeed(n);} public static long nextLong(){return rand.nextLong();}\n}')
  # Complete real nested class body, not a behavioral stub; outer RecipesCommon is not claimed compiled.
  rec=(m.JAVA/'com/hbm/inventory/RecipesCommon.java').read_text();inv=t/'com/hbm/inventory';inv.mkdir(parents=True)
  (inv/'RecipesCommon.java').write_text('package com.hbm.inventory; import net.minecraft.block.Block; public class RecipesCommon {\n'+extract(rec,'public static class MetaBlock {')+'\n}')
  # The complete production DungeonToolbox source is compiled verbatim.
  generated=[package/'ConcreteConstructionSlice.java',package/'Library.java',inv/'RecipesCommon.java',t/'com/hbm/main/MainRegistry.java',src/'com/hbm/world/generator/DungeonToolbox.java']
  oldproof=(prev/'NativeProof.java').read_text();prefix=oldproof[:oldproof.index('GeneratedProof.run();')];prefix=prefix.replace('import net.minecraft.block.Block;','import com.hbm.lib.ConcreteConstructionSlice;\nimport net.minecraft.block.Block;');(t/'NativeProof.java').write_text(prefix+'GeneratedProof.run(); EvaluationProof.run(); System.out.println("PASS "+checks+" checks; "+events+" ordered native placement events; 917-site method compiled unsplit."); }}')
  (t/'GeneratedProof.java').write_text('import net.minecraft.world.World; import com.hbm.lib.Library; public class GeneratedProof extends NativeProof {\nstatic void nativeRun(World world,int x,int y,int z){\n'+'\n'.join(r['new'] for r in rows)+'\n}\nstatic void originalRun(World world,int x,int y,int z){\n'+'\n'.join(r['old'].replace('world.setBlock(','legacy(world, ') for r in rows)+'\n}\nstatic void run(){for(int[] o:origins){RecordingWorld old=new RecordingWorld(),w=new RecordingWorld();Library.seed(981723);originalRun(old,o[0],o[1],o[2]);long rng=Library.nextLong();Library.seed(981723);nativeRun(w,o[0],o[1],o[2]);check(rng==Library.nextLong()&&old.trace.equals(w.trace),"917 ordered event and RNG parity");events+=w.trace.size();Library.seed(761);try{originalRun(null,o[0],o[1],o[2]);throw new AssertionError();}catch(NullPointerException ok){}rng=Library.nextLong();Library.seed(761);try{nativeRun(null,o[0],o[1],o[2]);throw new AssertionError();}catch(NullPointerException ok){}check(rng==Library.nextLong(),"null RNG parity");}}}')
  ev=(H/'EvaluationProof.java').read_text();(t/'EvaluationProof.java').write_text(ev)
  mutants=[('if(world == null) throw new NullPointerException();',''),('x < 30000000','x <= 30000000'),('y < 256','y <= 256'),('concrete.getStateFromMeta(0), 3','concrete.getStateFromMeta(0), 2'),('Block concrete = com.hbm.lib.Library.getRandomConcrete();','Block concrete = com.hbm.lib.Library.getRandomConcrete(); com.hbm.lib.Library.getRandomConcrete();'),('Block concrete = com.hbm.lib.Library.getRandomConcrete();','if(world == null) throw new NullPointerException(); Block concrete = com.hbm.lib.Library.getRandomConcrete();')]
  for n,(before,after) in enumerate(mutants):
   assert actual.count(before)==1
   path=t/('Mutant'+str(n)+'.java');path.write_text(actual.replace('package com.hbm.world.generator;','').replace('public class DungeonToolbox','public class Mutant'+str(n)).replace(before,after));generated.append(path)
  run([javac,'-encoding','UTF-8','-sourcepath',t,'-cp',cp,'-d',t,t/'NativeProof.java',t/'GeneratedProof.java',t/'EvaluationProof.java']+generated+production)
  # Native API attribution and initialization closure verified on actual compiled class.
  dis=run([a.java_home/'bin/javap','-c','-p','-classpath',t,'com.hbm.world.generator.DungeonToolbox'],capture_output=True,text=True).stdout
  assert 'static {}' not in dis
  block=dis.split('public static void placeRandomConcrete(')[1].split('public static void generateBox(')[0]
  assert block.count('com/hbm/lib/Library.getRandomConcrete:()Lnet/minecraft/block/Block;')==1
  assert 'net/minecraft/world/World.setBlockState:(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;I)Z' in block
  assert 'net/minecraft/block/Block.getStateFromMeta:(I)Lnet/minecraft/block/state/IBlockState;' in block
  (H/'attribution.log').write_text(block.rstrip()+'\n')
  disrun=run([a.java_home/'bin/javap','-c','-p','-classpath',t,'GeneratedProof'],capture_output=True,text=True).stdout
  code=disrun.split('static void nativeRun(')[1].split('static void originalRun(')[0];assert code.count('DungeonToolbox.placeRandomConcrete:')==917
  size=int(re.findall(r'\n\s*(\d+): return',code)[-1])+1;assert size<65536
  print('PASS actual complete DungeonToolbox native API attribution, no <clinit>; unsplit 917-sequence Code length',size,'bytes.')
  run([java,'-Xmx700m','-cp',str(t)+':'+cp,'NativeProof',a.legacy_client],cwd=t)
 print('Scope: complete DungeonToolbox + selected Silo sequence; Library selector/static fields and MetaBlock are explicit source slices. Full Silo/mod/client compilation NOT claimed.')
if __name__=='__main__':main()
