#!/usr/bin/env python3
"""Compile exact production callback bodies and bounded representative constructors against genuine native APIs."""
import argparse,hashlib,json,re,subprocess,tempfile
from pathlib import Path
HERE=Path(__file__).resolve().parent;ROOT=HERE.parents[2]
p=argparse.ArgumentParser();p.add_argument('--java-home',type=Path,required=True);p.add_argument('--classpath-file',type=Path,required=True);p.add_argument('--legacy-jar',type=Path,required=True);p.add_argument('--mutant',choices=['old-signature','true-body','state-dependent']);a=p.parse_args();cp=a.classpath_file.read_text().strip()
assert hashlib.sha256(a.legacy_jar.read_bytes()).hexdigest()=='a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce'
rows=json.loads((HERE/'sites.json').read_text())
def body(s,pattern):
 m=re.search(pattern,s);assert m,pattern;end=m.end();depth=1
 while depth:
  if s[end]=='{':depth+=1
  if s[end]=='}':depth-=1
  end+=1
 return s[m.start():end]
with tempfile.TemporaryDirectory(prefix='opacity-native-') as d:
 t=Path(d);files=[]
 def write(n,s):
  f=t/n;f.parent.mkdir(parents=True,exist_ok=True);f.write_text(s);files.append(f);return f
 fixtures=[]
 for i,r in enumerate(rows):
  s=(ROOT/r['path']).read_text();method=body(s,r'public boolean isOpaqueCube\(net.minecraft.block.state.IBlockState state\)\s*\{');assert method==r['after'].replace('\r\n','\n')
  if a.mutant=='old-signature':method=method.replace('net.minecraft.block.state.IBlockState state','')
  if a.mutant=='true-body':method=method.replace('return false;','return true;')
  if a.mutant=='state-dependent':method=method.replace('return false;','return state.getValue(FLAG);')
  fixtures.append('public static class C'+str(i)+' extends StateFixture { '+method+' }')
 write('CallbackBank.java','import net.minecraft.block.*; import net.minecraft.block.state.*; import net.minecraft.block.properties.*; import net.minecraft.block.material.*; public class CallbackBank { static final PropertyInteger META=PropertyInteger.create("meta",0,15); static final PropertyBool FLAG=PropertyBool.create("flag"); public static class StateFixture extends Block { public StateFixture(){super(Material.ROCK);} protected BlockStateContainer createBlockState(){return new BlockStateContainer(this,META,FLAG);} } '+'\n'.join(fixtures)+' static Block[] create(){ return new Block[]{'+','.join('new C'+str(i)+'()' for i in range(len(rows)))+'}; } static Block[] descendants(){ return new Block[]{'+','.join('new C'+str(i)+'(){}' for i in range(len(rows)))+'}; }}')
 # Six complete actual constructor bodies and callback methods. Unrelated callbacks are not synthesized.
 reps=[('BlockWaterPlant','com/hbm/blocks','Block',''),('BlockPlasma','com/hbm/blocks/generic','Block',''),('BlockVacuum','com/hbm/blocks/gas','Block',''),('WasteLeaves','com/hbm/blocks/generic','Block',''),('TurretSentry','com/hbm/blocks/turret','BlockContainer','public net.minecraft.tileentity.TileEntity createNewTileEntity(net.minecraft.world.World w,int m){throw new AssertionError("Unexercised tile factory boundary");}'),('BlockRubberLeaves','com/hbm/blocks','BlockLeaves','public net.minecraft.block.BlockPlanks.EnumType getWoodType(int m){throw new AssertionError("Unexercised leaf wood-type boundary");} public boolean isShearable(net.minecraft.item.ItemStack s,net.minecraft.world.IBlockAccess w,net.minecraft.util.math.BlockPos p){throw new AssertionError("Unexercised shearing boundary");} public java.util.List<net.minecraft.item.ItemStack> onSheared(net.minecraft.item.ItemStack s,net.minecraft.world.IBlockAccess w,net.minecraft.util.math.BlockPos p,int f){throw new AssertionError("Unexercised shearing boundary");}')]
 for name,pkg,parent,extra in reps:
  s=(ROOT/'src/main/java'/pkg/(name+'.java')).read_text();ctor=body(s,r'(?:public|protected) '+name+r'\([^)]*\)\s*\{');method=body(s,r'public boolean isOpaqueCube\([^)]*\)\s*\{')
  write(pkg+'/'+name+'.java','package '+pkg.replace('/','.')+'; import net.minecraft.block.*; import net.minecraft.block.material.Material; public class '+name+' extends '+parent+' {'+ctor+method+extra+'}')
 # Both custom state factories in the complete selected source hierarchy, verbatim.
 for owner,relative in [('BlockChargeBase','com/hbm/blocks/bomb'),('BlockNTMFlower','com/hbm/blocks/generic')]:
  s=(ROOT/'src/main/java'/relative/(owner+'.java')).read_text()
  prop=re.search(r'public static final PropertyInteger \w+ = PropertyInteger.create\([^;]+;',s)[0]
  factory=body(s,r'protected BlockStateContainer createBlockState\(\)\s*\{')
  method=body(s,r'public boolean isOpaqueCube\([^)]*\)\s*\{')
  write(owner+'StateSlice.java','import net.minecraft.block.*;import net.minecraft.block.material.*;import net.minecraft.block.state.*;import net.minecraft.block.properties.*; public class '+owner+'StateSlice extends Block { public '+owner+'StateSlice(){super(Material.ROCK);} '+prop+factory+method+'}')
 # Original engine differential and explicit stateful-callback counterexample.
 old=write('OldOpacityFixture.java','public class OldOpacityFixture extends aji { public OldOpacityFixture(){super(awt.e);} public boolean c(){return false;} public static class SideEffect extends aji { public static int calls; public SideEffect(){super(awt.e);} public boolean c(){calls++;return false;} } }')
 subprocess.run([str(a.java_home/'bin/javac'),'-cp',str(a.legacy_jar),'-d',d,str(old)],check=True);files.remove(old)
 files += [HERE/'OpacityNativeTest.java',HERE/'LegacyFixtureRelocator.java',HERE.parent/'ForgeEventNativeLauncher.java']
 subprocess.run([str(a.java_home/'bin/javac'),'-encoding','UTF-8','-cp',cp,'-sourcepath',str(t/'empty'),'-d',d]+list(map(str,files)),check=True)
 subprocess.run([str(a.java_home/'bin/java'),'-cp',d+':'+cp,'LegacyFixtureRelocator',d],check=True)
 subprocess.run([str(a.java_home/'bin/java'),'-Djava.awt.headless=true','-cp',d+':'+cp,'ForgeEventNativeLauncher','OpacityNativeTest',str(a.legacy_jar),d],cwd=d,check=True)
