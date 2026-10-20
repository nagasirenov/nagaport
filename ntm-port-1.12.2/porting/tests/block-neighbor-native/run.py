#!/usr/bin/env python3
import argparse,importlib.util,json,re,subprocess,tempfile
from pathlib import Path
H=Path(__file__).resolve().parent;M=H.parents[2];S=M/'src/main/java'
spec=importlib.util.spec_from_file_location('proof',H/'source-proof.py');proof=importlib.util.module_from_spec(spec);spec.loader.exec_module(proof)
def generate(t):
 d=json.loads((H/'selection.json').read_text());files=[];runs=[];names={}
 def out(name,body):p=t/name;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(body);files.append(p)
 imports='import net.minecraft.entity.EntityLivingBase;import net.minecraft.block.*;import net.minecraft.block.state.*;import net.minecraft.block.material.Material;import net.minecraft.world.World;import net.minecraft.util.math.BlockPos;import net.minecraft.init.Blocks;import com.hbm.lib.ForgeDirection;'
 out('NtmHookBoundary.java',imports+'''public class NtmHookBoundary extends Block {
 public World worldObj;public boolean fire;public static final net.minecraft.block.properties.PropertyInteger ORIENTATION=net.minecraft.block.properties.PropertyInteger.create("orientation",0,15);
 public NtmHookBoundary(){super(Material.ROCK);}
 @Override protected BlockStateContainer createBlockState(){return new BlockStateContainer(this,ORIENTATION);}
 @Override public IBlockState getStateFromMeta(int m){return getDefaultState().withProperty(ORIENTATION,Math.max(0,Math.min(15,m)));}
 @Override public int getMetaFromState(IBlockState s){return s.getValue(ORIENTATION);}
 public com.hbm.interfaces.IBomb.BombReturnCode explode(World w,int x,int y,int z){ProbeHooks.call("explode",w,x,y,z);return com.hbm.interfaces.IBomb.BombReturnCode.DETONATED;}
 public void updateState(World w,int x,int y,int z){ProbeHooks.call("piston",w,x,y,z);}
 public boolean shouldIgnite(World w,int x,int y,int z){ProbeHooks.call("fire",w,x,y,z);return fire;}
 public void prime(World w,int x,int y,int z,int meta,net.minecraft.entity.EntityLivingBase p){NeighborNativeTest.check(meta==1&&p==null,"original TNT prime metadata/entity contract");ProbeHooks.call("prime",w,x,y,z);}
 @Override public void onExplosionDestroy(World w,BlockPos p,net.minecraft.world.Explosion e){ProbeHooks.call("inherited-detonation",w,p.getX(),p.getY(),p.getZ());}
}''')
 parent=proof.method((S/'com/hbm/blocks/bomb/BlockDetonatable.java').read_text(),'public void neighborChanged(')
 out('NativeDetonatableBoundary.java',imports+'public class NativeDetonatableBoundary extends NtmHookBoundary {\n@Override '+parent+'\n}')
 for i,e in enumerate(d['selected']):
  name='NativeCase'+str(i);names[e['class']]=name;body=proof.method((S/e['path']).read_text(),'public void neighborChanged(');parent='NativeDetonatableBoundary' if e['class'] in ['ExplosiveCharge','BlockTNTBase','BlockPlasticExplosive'] else 'NtmHookBoundary';helpers=''
  if e['class']=='BlockChargeBase':
   text=(S/e['path']).read_text();helpers='\npublic static final net.minecraft.block.properties.PropertyInteger ORIENTATION = net.minecraft.block.properties.PropertyInteger.create("orientation", 0, 15);\n'+ '\n'.join(proof.method(text,sig) for sig in ['protected BlockStateContainer createBlockState(', 'public IBlockState getStateFromMeta(', 'public int getMetaFromState('])
  if e['class']=='BlockTNTBase':
   text=(S/e['path']).read_text();helpers='\n'+proof.method(text,'public void checkAndIgnite(')+'\n'+proof.method(text,'public void onBlockDestroyedByPlayer(')
  out(name+'.java',imports+'public class '+name+' extends '+parent+' {\n@Override '+body+helpers+'\n}')
  out('Counting'+name+'.java',imports+'public class Counting'+name+' extends '+name+' { @Override public void neighborChanged(IBlockState s,World w,BlockPos p,Block b,BlockPos from){NeighborNativeTest.enter(s,w,p,b,from);super.neighborChanged(s,w,p,b,from);} }')
  runs.append('runCase(new Counting'+name+'(),"'+e['class']+'");')
 out('ProbeHooks.java','''import java.util.*;import net.minecraft.world.World;public class ProbeHooks {public static final List<String> calls=new ArrayList<>();public static World world;public static int x,y,z;public static String throwing;public static final RuntimeException BOOM=new IllegalStateException("domain failure");public static void call(String event,World w,int a,int b,int c){calls.add(event);world=w;x=a;y=b;z=c;if(event.equals(throwing))throw BOOM;}public static void reset(){calls.clear();world=null;throwing=null;}}''')
 out('ModBlocks.java','public class ModBlocks { public static net.minecraft.block.Block therm_endo,therm_exo,crystal_virus; }')
 out('ExplosionChaos.java','import net.minecraft.world.World;public class ExplosionChaos {public static void hardenVirus(World w,int x,int y,int z,int radius){NeighborNativeTest.check(radius==10,"crystal original radius");ProbeHooks.call("crystal",w,x,y,z);}}')
 out('ExplosionThermo.java','import net.minecraft.world.World;public class ExplosionThermo {'+''.join('public static void '+method+'(World w,int x,int y,int z,int radius){NeighborNativeTest.check(radius=='+str(radius)+',"thermal original radius");ProbeHooks.call("'+method+'",w,x,y,z);}' for method,radius in [('freeze',15),('freezer',20),('scorch',15),('setEntitiesOnFire',20)])+'}')
 out('NeighborNativeTest.java',(H/'NeighborNativeTest.java.in').read_text().replace('RUN_CASES','\n'.join(runs)).replace('TNT_CASE',names['BlockTNTBase']).replace('CHARGE_CASE',names['BlockChargeBase']))
 files += [S/p for p in ['com/hbm/world/WorldBlockState.java','com/hbm/world/WorldAirRemoval.java','com/hbm/lib/FacingCompat.java','com/hbm/lib/ForgeDirection.java','com/hbm/interfaces/IBomb.java']]
 return files,d,names

def main():
 p=argparse.ArgumentParser();p.add_argument('--java-home',type=Path,required=True);p.add_argument('--classpath-file',type=Path,required=True);p.add_argument('--keep',type=Path);p.add_argument('--mutants',action='store_true');a=p.parse_args();proof.audit();cp=a.classpath_file.read_text().strip()
 with tempfile.TemporaryDirectory(prefix='native-neighbors-') as td:
  t=a.keep or Path(td);t.mkdir(parents=True,exist_ok=True);files,d,names=generate(t);classes=t/'classes';classes.mkdir(exist_ok=True);jc=[str(a.java_home/'bin/javac'),'-encoding','UTF-8','-sourcepath',str(t),'-cp',str(classes)+':'+cp,'-d',str(classes)];subprocess.run(jc+list(map(str,files)),check=True);cmd=[str(a.java_home/'bin/java'),'-Xmx400m','-cp',str(classes)+':'+cp,'NeighborNativeTest'];subprocess.run(cmd,check=True,cwd=t)
  dis=subprocess.check_output([str(a.java_home/'bin/javap'),'-p','-c','-classpath',str(classes)]+list(names.values())+['NativeDetonatableBoundary'],text=True);(H/'native-attribution.log').write_text(dis)
  if a.mutants:
   muts=[(names['NukeCustom']+'.java','if(world.isBlockPowered(', 'if(!world.isBlockPowered(', 'inverted power predicate'),(names['PistonInserter']+'.java','nativePos.getX()','nativePos.getZ()','wrong coordinate'),(names['BlockTNTBase']+'.java','this.onBlockDestroyedByPlayer(world, x, y, z, 1);','this.onBlockDestroyedByPlayer(world, x, y, z, 1); this.onBlockDestroyedByPlayer(world, x, y, z, 1);','double prime'),(names['BlockChargeBase']+'.java','if(!com.hbm.lib.FacingCompat','if(com.hbm.lib.FacingCompat','inverted attachment predicate')]
   for file,before,after,label in muts:
    path=t/file;orig=path.read_text();assert before in orig;path.write_text(orig.replace(before,after));subprocess.run(jc+[str(path)],check=True,capture_output=True);res=subprocess.run(cmd,cwd=t,capture_output=True,text=True);assert res.returncode and 'AssertionError' in res.stderr,(label,res.stdout,res.stderr);print('PASS runtime mutant rejected: '+label);path.write_text(orig);subprocess.run(jc+[str(path)],check=True,capture_output=True)
   path=t/(names['PistonInserter']+'.java');orig=path.read_text();op=next(o for o in d['operations'] if o['path'].endswith('/PistonInserter.java'));assert op['new'].replace('\r\n','\n') in orig;path.write_text(orig.replace(op['new'].replace('\r\n','\n'),op['old'].replace('\r\n','\n')));res=subprocess.run(jc+[str(path)],capture_output=True,text=True);assert res.returncode and 'does not override' in res.stderr;path.write_text(orig);print('PASS compiler mutant rejected: scalar callback cannot override native Block')
if __name__=='__main__':main()
