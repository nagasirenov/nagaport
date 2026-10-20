#!/usr/bin/env python3
"""Compile all migrated complete methods against genuine 1.12.2 classes; no platform facades."""
import argparse,importlib.util,json,re,subprocess,tempfile,zipfile
from pathlib import Path
H=Path(__file__).resolve().parent;M=H.parents[2];S=M/'src/main/java'
spec=importlib.util.spec_from_file_location('proof',H/'source-proof.py');proof=importlib.util.module_from_spec(spec);spec.loader.exec_module(proof)
def generate(t):
 d=json.loads((H/'selection.json').read_text());files=[];run=[];dispatch=[];groups=set()
 def out(name,body):p=t/name;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(body);files.append(p)
 helper=(S/'com/hbm/blocks/BlockDummyable.java').read_text()
 for declaration in ['public static final int offset = 10;', 'public static final int extra = 6;', 'public static final PropertyInteger METADATA = PropertyInteger.create("metadata", 0, 15);', 'List<ThreeInts> positions = new ArrayList<>();']:assert declaration in helper
 methods=[proof.method(helper,sig) for sig in ['protected BlockStateContainer createBlockState(', 'public IBlockState getStateFromMeta(', 'public int getMetaFromState(', 'private static int metadata(', 'public int[] findCore(', 'public int[] findCoreRec(', 'protected boolean standardOpenBehavior(']]
 out('ExactHelperFixture.java','''import java.util.*;import net.minecraft.block.*;import net.minecraft.block.material.Material;import net.minecraft.block.state.*;import net.minecraft.block.properties.PropertyInteger;import net.minecraft.world.*;import net.minecraft.util.math.BlockPos;import net.minecraft.entity.player.EntityPlayer;import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;import com.hbm.lib.ForgeDirection;import com.hbm.handler.ThreeInts;
public class ExactHelperFixture extends Block {
public static final PropertyInteger METADATA=PropertyInteger.create("metadata",0,15);
public static final int offset=10,extra=6;List<ThreeInts> positions=new ArrayList<>();
public ExactHelperFixture(){super(Material.ROCK);}
'''+ '\n'.join(methods)+'\n}')
 tiletypes=set(); attribution=proof.audit(); classes=attribution['classes']; selected_names={next(name for name,c in classes.items() if c['path']==e['path']):'NativeCase'+str(i) for i,e in enumerate(d['selected'])}
 for i,e in enumerate(d['selected']):
  name='NativeCase'+str(i); owner=next(n for n,c in classes.items() if c['path']==e['path']); parent=classes[owner]['parent']
  while parent in classes and parent not in selected_names:parent=classes[parent]['parent']
  native_parent=selected_names.get(parent,'ExactHelperFixture');body=proof.method((S/e['path']).read_text(),'public boolean onBlockActivated(');tiles=set(re.findall(r'\bTileEntity\w+',body));tiletypes|=tiles;tile=next(iter(tiles)) if tiles else ''
  out(name+'.java','import net.minecraft.world.World;import net.minecraft.entity.player.EntityPlayer;import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler; public class '+name+' extends '+native_parent+' {\n@Override '+body+'\n}')
  idexpr='face.getIndex()' if any('nativeFacing' in x for x in d['operations'][i]['used_locals']) else '-1' if re.search(r'player,\s*-1\)',body) else '0'
  run.append('runCase(new '+name+'(),"'+Path(e['path']).stem+'","'+e['group']+'",'+('null' if not tile else tile+'.class')+','+str(idexpr=='face.getIndex()').lower()+','+('-1' if idexpr=='-1' else '0')+');')
  if e['group'] not in groups:
   groups.add(e['group']);count='Counting'+name
   out(count+'.java','import net.minecraft.world.World;import net.minecraft.entity.player.EntityPlayer;import net.minecraft.util.*;import net.minecraft.util.math.BlockPos;import net.minecraft.block.state.IBlockState;public class '+count+' extends '+name+' { @Override public boolean onBlockActivated(World w,BlockPos p,IBlockState s,EntityPlayer pl,EnumHand h,EnumFacing f,float x,float y,float z){ActivationNativeTest.callbackCalls++;return super.onBlockActivated(w,p,s,pl,h,f,x,y,z);} }')
   dispatch.append('runDispatched(new '+count+'(),"'+e['group']+'",'+('null' if not tile else tile+'.class')+');')
 for tile in sorted(tiletypes):out(tile+'.java','public class '+tile+' extends net.minecraft.tileentity.TileEntity {}')
 out('MainRegistry.java','public class MainRegistry {public static final Object instance="hbm";}')
 out('ActivationNativeTest.java',(H/'ActivationNativeTest.java.in').read_text().replace('RUN_CASES','\n'.join(run)).replace('DISPATCH_CASES','\n'.join(dispatch)))
 files += [S/'com/hbm/lib/ForgeDirection.java',S/'com/hbm/handler/ThreeInts.java']
 return files,d

def main():
 p=argparse.ArgumentParser();p.add_argument('--java-home',type=Path,required=True);p.add_argument('--classpath-file',type=Path,required=True);p.add_argument('--keep',type=Path);p.add_argument('--mutants',action='store_true');a=p.parse_args();proof.audit();cp=a.classpath_file.read_text().strip()
 with tempfile.TemporaryDirectory(prefix='native-activation-') as tmp:
  t=a.keep or Path(tmp);t.mkdir(parents=True,exist_ok=True);files,d=generate(t);classes=t/'classes';classes.mkdir(exist_ok=True)
  jc=[str(a.java_home/'bin/javac'),'-encoding','UTF-8','-sourcepath',str(t),'-cp',str(classes)+':'+cp,'-d',str(classes)]
  subprocess.run(jc+list(map(str,files)),check=True)
  cmd=[str(a.java_home/'bin/java'),'-Xmx400m','-cp',str(classes)+':'+cp,'ActivationNativeTest'];subprocess.run(cmd,check=True,cwd=t)
  dis=subprocess.check_output([str(a.java_home/'bin/javap'),'-p','-c','-classpath',str(classes)]+['NativeCase'+str(i) for i in range(105)]+['ExactHelperFixture'],text=True);(H/'native-attribution.log').write_text(dis)
  if a.mutants:
   face=next('NativeCase'+str(i)+'.java' for i,x in enumerate(d['operations']) if any('nativeFacing' in local for local in x['used_locals']));gui=next('NativeCase'+str(i)+'.java' for i,x in enumerate(d['selected']) if x['group']=='gui')
   muts=[(face,'nativeFacing.getIndex()','0','lost face GUI id'),(gui,'nativePos.getX()','nativePos.getZ()','wrong coordinate'),('ExactHelperFixture.java','FMLNetworkHandler.openGui(player, MainRegistry.instance, id, world, pos[0], pos[1], pos[2]);','FMLNetworkHandler.openGui(player, MainRegistry.instance, id, world, pos[0], pos[1], pos[2]); FMLNetworkHandler.openGui(player, MainRegistry.instance, id, world, pos[0], pos[1], pos[2]);','duplicate GUI dispatch'),(gui,'if(world.isRemote)', 'if(!world.isRemote)','lost remote short circuit')]
   for file,before,after,label in muts:
    path=t/file;orig=path.read_text();assert orig.count(before)==1;path.write_text(orig.replace(before,after));subprocess.run(jc+[str(path)],check=True,capture_output=True);res=subprocess.run(cmd,cwd=t,capture_output=True,text=True);assert res.returncode and 'AssertionError' in res.stderr,(label,res.stdout,res.stderr);print('PASS runtime mutant rejected: '+label);path.write_text(orig);subprocess.run(jc+[str(path)],check=True,capture_output=True)
   path=t/'NativeCase0.java';orig=path.read_text();native=d['operations'][0]['new'].replace('\r\n','\n');old=d['operations'][0]['old'].replace('\r\n','\n');assert native in orig;path.write_text(orig.replace(native,old));res=subprocess.run(jc+[str(path)],capture_output=True,text=True);assert res.returncode and 'does not override' in res.stderr;path.write_text(orig);subprocess.run(jc+[str(path)],check=True,capture_output=True);print('PASS compiler mutant rejected: old scalar method is not a native override')

if __name__=='__main__':main()
