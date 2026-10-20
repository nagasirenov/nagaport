#!/usr/bin/env python3
"""Reconstructed finite44 native probe; emits only newly executed results.
Never downloads inputs. Complete production owners/context/helper are compiled.
Explicit NTM registry/tab/tile/Library collaborators are not full gameplay tests.
"""
import argparse,collections,hashlib,json,re,subprocess,tempfile,zipfile
from pathlib import Path
H=Path(__file__).resolve().parent
PRODUCTION=['com/hbm/blocks/network/BlockCable.java','com/hbm/blocks/network/WireCoated.java','com/hbm/blocks/network/ConnectionState.java','com/hbm/blocks/generic/BlockDecoCT.java','com/hbm/render/block/ct/CT.java','com/hbm/render/block/ct/CTContext.java','com/hbm/render/block/ct/IBlockCT.java','com/hbm/lib/ForgeDirection.java','api/hbm/energymk2/IEnergyConnectorBlock.java','api/hbm/energymk2/IEnergyConnectorMK2.java','com/hbm/world/generator/DungeonToolbox.java']
def digest(b):return hashlib.sha256(b).hexdigest()
def run(args,**kw):return subprocess.run(list(map(str,args)),check=True,**kw)
def extract(s,marker):
 start=s.index(marker);end=s.index('{',start)+1;depth=1
 while depth:depth+=(s[end]=='{')-(s[end]=='}');end+=1
 return s[start:end]
def attribute(t,rows,a,cp):
 """Real javac bindings for all44 selected original expressions, not merely syntax."""
 src=t/'attribution';src.mkdir();files=[]
 with zipfile.ZipFile(H/'sources.zip')as z:
  for f in z.namelist():
   b=z.read(f);assert digest(b)==json.loads((H/'source-hashes.json').read_text())[f]
   dest=src/f;dest.parent.mkdir(parents=True,exist_ok=True);dest.write_bytes(b);files.append(dest)
 fixture=src/'ModBlocks.java';fixture.write_text('package com.hbm.blocks;public class ModBlocks {public static net.minecraft.block.Block red_cable,red_wire_coated,deco_steel;}');files.append(fixture)
 tools=a.java_home/'lib/tools.jar';run([a.java_home/'bin/javac','-cp',tools,'-d',src,H/'PaletteOwner.java'])
 out=run([a.java_home/'bin/java','-Xmx700m','-cp',str(src)+':'+str(tools),'PaletteOwner',cp]+files,capture_output=True,text=True)
 records=[]
 for line in out.stdout.splitlines():
  r=json.loads(line)
  if not(r['world']and r['vanilla']and r['ints']and r['statement']and len(r['args'])==6 and r['args'][4:]==['0','3']):continue
  f=Path(r['file']);s=f.read_text();start=len(s.encode('utf-16-le')[:r['start']*2].decode('utf-16-le'));end=len(s.encode('utf-16-le')[:r['end']*2].decode('utf-16-le'))+1
  assert s[end-1]==';';r.update(file=str(f.relative_to(src)),start=start,end=end,old=s[start:end]);records.append(r)
 assert len(records)==44
 actual={(r['file'],r['start']):r for r in records};groups=collections.defaultdict(list)
 for r in rows:
  found=actual[(r['file'],r['start'])]
  assert all(found[k]==r[k]for k in ['end','old','args','receiver','block'])
  groups[(r['file'],found['method'])].append(r)
 assert len(groups)==4
 (H/'reconstructed-attribution-current.log').write_text('CURRENT RECONSTRUCTED ATTRIBUTION PASS:44 native World/static-owner/int/statement/literal bindings across four methods. Not whole-generator compilation.\n'+out.stderr)
 print('CURRENT RECONSTRUCTED ATTRIBUTION PASS:44 original-expression native bindings across four methods.',flush=True)
 return groups

def main():
 p=argparse.ArgumentParser();p.add_argument('--module',type=Path,default=H.parents[2]);p.add_argument('--java-home',type=Path,required=True);p.add_argument('--classpath-file',type=Path,required=True);p.add_argument('--legacy-client',type=Path,required=True);p.add_argument('--mutants',action='store_true');a=p.parse_args();M=a.module;S=M/'src/main/java';cp=a.classpath_file.read_text().strip()
 assert a.legacy_client.is_file(),'Pinned original1.7.10 client jar required; no downloads performed'
 assert digest(a.legacy_client.read_bytes())=='a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce'
 for f,want in json.loads((H/'reconstructed-native-dependencies.json').read_text()).items():assert digest((M/f).read_bytes())==want,'Pinned production/probe input drift: '+f
 for f,r in json.loads((H/'manifest.json').read_text())['files'].items():assert digest((M/f).read_bytes())==r['after'],'Frozen44 production drift: '+f
 rows=json.loads((H/'selection.json').read_text());assert len(rows)==44
 constructors=json.loads((H/'constructors.json').read_text());registry=(S/'com/hbm/blocks/ModBlocks.java').read_text()
 for name,constructor in constructors.items():assert len(re.findall(r'\b'+name+r'\s*=(?!=)',registry))==1 and constructor in registry
 controls=run(['python3',H/'ownership.py','--java-home',a.java_home,'--classpath-file',a.classpath_file],capture_output=True,text=True);(H/'reconstructed-attribution-controls-current.log').write_text(controls.stdout+controls.stderr);print(controls.stdout,end='',flush=True)
 with tempfile.TemporaryDirectory(prefix='reconstructed-cable-palette-') as td:
  t=Path(td);groups=attribute(t,rows,a,cp);files=[S/n for n in PRODUCTION]
  def put(n,s):
   f=t/n;f.parent.mkdir(parents=True,exist_ok=True);f.write_text(s);files.append(f)
  put('com/hbm/blocks/ModBlocks.java','package com.hbm.blocks;import net.minecraft.block.Block;import net.minecraft.block.material.Material;import com.hbm.blocks.network.*;import com.hbm.blocks.generic.BlockDecoCT;import com.hbm.main.MainRegistry;public class ModBlocks {public static Block red_cable,red_wire_coated,deco_steel,deco_rusty_steel;public static void initialize(){'+''.join(constructors.values())+'for(Block b:new Block[]{red_cable,red_wire_coated,deco_steel})b.setRegistryName("hbm",b.getTranslationKey());}}')
  put('com/hbm/main/MainRegistry.java','package com.hbm.main;import net.minecraft.creativetab.CreativeTabs;public class MainRegistry {public static final CreativeTabs machineTab=CreativeTabs.REDSTONE,blockTab=CreativeTabs.BUILDING_BLOCKS;}')
  put('com/hbm/tileentity/network/TileEntityCableBaseNT.java','package com.hbm.tileentity.network;/** Bounded NTM tile only, no production energy-node execution. */public class TileEntityCableBaseNT extends net.minecraft.tileentity.TileEntity implements api.hbm.energymk2.IEnergyConnectorMK2 {}')
  lib=(S/'com/hbm/lib/Library.java').read_text();fields=re.findall(r'^\s*public static final ForgeDirection \w+ = ForgeDirection\.\w+;',lib,re.M);assert len(fields)==6;can=extract(lib,'public static boolean canConnect(')
  put('com/hbm/lib/Library.java','package com.hbm.lib;import net.minecraft.block.Block;import net.minecraft.world.IBlockAccess;import net.minecraft.tileentity.TileEntity;import api.hbm.energymk2.*;public class Library {'+'\n'.join(fields)+can+'public static Block getRandomConcrete(){throw new AssertionError("Unexercised concrete selector");}}')
  put('com/hbm/inventory/RecipesCommon.java','package com.hbm.inventory;import net.minecraft.block.Block;public class RecipesCommon {'+extract((S/'com/hbm/inventory/RecipesCommon.java').read_text(),'public static class MetaBlock {')+'}')
  native=(M/'porting/tests/dungeon-palette-native/NativeProof.java').read_text();native=native[:native.index(' public static void main(')]+''' public static void main(String[]args)throws Exception {CableStateTest.main(args);loadBounds(args[0]);GeneratedProof.run();EvaluationProof.run();System.out.println("CURRENT RECONSTRUCTED NATIVE PASS "+checks+" checks and "+events+" exact ordered native/original-expression palette events.");}\n}\n''';put('NativeProof.java',native)
  ev=(M/'porting/tests/ct-palette-native/EvaluationProof.java').read_text().replace('ConcreteConstructionSlice.deco_steel','com.hbm.blocks.ModBlocks.red_cable').replace('ConcreteConstructionSlice.deco_lead','com.hbm.blocks.ModBlocks.red_wire_coated');put('EvaluationProof.java',ev);files.append(H/'CableStateTest.java')
  parts=['import net.minecraft.world.World;import net.minecraft.block.Block;import com.hbm.blocks.ModBlocks;public class GeneratedProof extends NativeProof {'];runs=[]
  for i,((file,method),rs) in enumerate(groups.items()):
   parts.append('static class Fixture'+str(i)+' {World world;int x,y,z;void nativeRun(){'+'\n'.join(r['new']for r in rs)+'}void originalRun(){'+'\n'.join(r['old'].replace('world.setBlock(','legacy(world, ')for r in rs)+'}}')
   runs.append(('{FixtureN f=new FixtureN();for(int permutation=0;permutation<3;permutation++){ModBlocks.red_cable=original[permutation];ModBlocks.red_wire_coated=original[(permutation+1)%3];ModBlocks.deco_steel=original[(permutation+2)%3];for(int[]o:origins){f.x=o[0];f.y=o[1];f.z=o[2];RecordingWorld old=new RecordingWorld(),w=new RecordingWorld();f.world=old;f.originalRun();f.world=w;f.nativeRun();check(old.trace.equals(w.trace),"exact ordered mutable field stream");events+=w.trace.size();f.world=null;boolean oe=false,ne=false;try{f.originalRun();}catch(NullPointerException e){oe=true;}try{f.nativeRun();}catch(NullPointerException e){ne=true;}check(oe&&ne,"null receiver even outside bounds");}}}').replace('FixtureN','Fixture'+str(i)))
  parts.append('static void run(){Block[] original={ModBlocks.red_cable,ModBlocks.red_wire_coated,ModBlocks.deco_steel};'+''.join(runs)+'ModBlocks.red_cable=original[0];ModBlocks.red_wire_coated=original[1];ModBlocks.deco_steel=original[2];}}');put('GeneratedProof.java','\n'.join(parts))
  run([a.java_home/'bin/javac','-encoding','UTF-8','-sourcepath',t,'-cp',cp,'-d',t]+files)
  sizes=[]
  for i,rs in enumerate(groups.values()):
   dis=run([a.java_home/'bin/javap','-c','-p','-classpath',t,'GeneratedProof$Fixture'+str(i)],capture_output=True,text=True).stdout;b=dis.split('void nativeRun();')[1].split('void originalRun();')[0];assert b.count('DungeonToolbox.placeUnorientedStructureBlock:')==len(rs);sizes.append(int(re.findall(r'\n\s*(\d+): return',b)[-1])+1)
  (H/'reconstructed-method-sizes-current.json').write_text(json.dumps(sizes)+'\n');print('CURRENT RECONSTRUCTED NATIVE PASS actual unsplit selected-expression Code sizes:',sizes,flush=True)
  command=[a.java_home/'bin/java','-Xmx500m','-cp',str(t)+':'+cp,'NativeProof',a.legacy_client];run(command,cwd=t)
  if a.mutants:
   src=(S/'com/hbm/world/generator/DungeonToolbox.java').read_text();mut=t/'com/hbm/world/generator/DungeonToolbox.java';mut.parent.mkdir(parents=True,exist_ok=True)
   changes=[('if(world == null) throw new NullPointerException();','if(world == null) return;'),('x < 30000000','x <= 30000000'),('y < 256','y <= 256'),('block.getStateFromMeta(0), 3','block.getStateFromMeta(1), 3'),('block.getStateFromMeta(0), 3','block.getStateFromMeta(0), 2'),('world.setBlockState(new BlockPos(x, y, z), block.getStateFromMeta(0), 3);',';')]
   for i,(old,new) in enumerate(changes):
    assert old in src;mut.write_text(src.replace(old,new,1));run([a.java_home/'bin/javac','-encoding','UTF-8','-sourcepath',t,'-cp',str(t)+':'+cp,'-d',t,mut],stdout=subprocess.DEVNULL,stderr=subprocess.DEVNULL)
    result=subprocess.run(list(map(str,command)),cwd=t,capture_output=True,text=True);assert result.returncode!=0 and 'java.lang.AssertionError:'in result.stderr,(i,result.stdout,result.stderr)
    print('CURRENT RECONSTRUCTED NATIVE PASS helper mutation rejected:',i,next(line for line in result.stderr.splitlines()if 'java.lang.AssertionError:'in line),flush=True)
if __name__=='__main__':main()
