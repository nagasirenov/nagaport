#!/usr/bin/env python3
import argparse,importlib.util,json,re,subprocess,tempfile,zipfile
from pathlib import Path
H=Path(__file__).resolve().parent;M=H.parents[2];S=M/'src/main/java'
spec=importlib.util.spec_from_file_location('proof',H/'source-proof.py');proof=importlib.util.module_from_spec(spec);spec.loader.exec_module(proof)
def method(path,name):
 s=(S/path).read_text();start=s.index(name);start=s.rfind('\n',0,start)+1;brace=s.index('{',start);i=brace+1;n=1
 while n:n+=(s[i]=='{')-(s[i]=='}');i+=1
 return s[start:i]
def generate(t):
 d=json.loads((H/'selection.json').read_text());files=[]
 def out(name,s):p=t/name;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(s);files.append(p)
 imports='import net.minecraft.world.World;import net.minecraft.tileentity.TileEntity;import com.hbm.util.fauxpointtwelve.BlockPos;import com.hbm.util.fauxpointtwelve.DirPos;import java.util.*;'
 expr=[];calls=[]
 for i,o in enumerate(d['operations']):
  typ=o['locals'].get('pos',{}).get('type','com.hbm.util.fauxpointtwelve.BlockPos')
  if 'pos.get' not in o['new'].replace('this.pos.get',''):typ='com.hbm.util.fauxpointtwelve.BlockPos'
  expr.append('public boolean case'+str(i)+'(World world,int x,int y,int z,DirPos input){World p_149695_1_=world;int p_149695_2_=x,p_149695_3_=y,p_149695_4_=z,x1=x,y1=y,z1=z,sx=x,sy=y,sz=z;'+typ+' pos=input;return '+o['new']+';}')
  args=o['arguments'];kind='local' if args[0]=='pos.getX()' else 'offset' if args[0]=='this.pos.getX() + x' else 'tile' if args[0].startswith('this.pos') else 'scalar';delta=6 if args[1].endswith('+ 6') else 1 if args[1].endswith('+ 1') else 0
  calls.append('expression(e,'+str(i)+',"'+kind+'",'+str(delta)+','+str(o['receiver']=='this.world').lower()+');')
 out('ExpressionCases.java',imports+'public class ExpressionCases extends TileEntity {public void rawPos(net.minecraft.util.math.BlockPos p){this.pos=p;}\n'+'\n'.join(expr)+'\n}')
 fixtures=[('MultiMode','com/hbm/tileentity/machine/storage/TileEntityBatteryBase.java','public short getRelevantMode(', 'public short redHigh=3,redLow=1;private short modeCache=0;public BlockPos[] ports;public int portsCalls;public BlockPos[] getPortPos(){portsCalls++;return ports;}'),('SingleMode','com/hbm/tileentity/machine/storage/TileEntityMachineBattery.java','public short getRelevantMode(', 'public short redHigh=3,redLow=1;private short modeCache=0;'),('Dipole','com/hbm/tileentity/machine/albion/TileEntityPADipole.java','checkRedstone() {','public DirPos[] ports;public int portsCalls;public DirPos[] getConPos(){portsCalls++;return ports;}'),('MiningLaser','com/hbm/tileentity/machine/TileEntityMachineMiningLaser.java','isMultiblockRedstonePowered() {','public DirPos[] ports;public DirPos[] getConPos(){return ports;}public boolean invoke(){return isMultiblockRedstonePowered();}'),('DoorPower','com/hbm/tileentity/TileEntityDoorGeneric.java','updateRedstonePower(','public int redstonePower;public Set<BlockPos> activatedBlocks=new HashSet<>(4);'),('Chlorine','com/hbm/tileentity/machine/TileEntityChlorineSeal.java','public void update()','public int spreads;public int[] xyz;private void spread(int x,int y,int z,int index){PowerNativeTest.check(index==0,"original spread seed");spreads++;xyz=new int[]{x,y,z};}'),('BlastDoor','com/hbm/tileentity/machine/TileEntityBlastDoor.java','public void update()','public boolean isOpening=false;public int state=0;private int timer=0;public boolean redstoned=false,locked=false;public int toggles,locks;public boolean isLocked(){locks++;return locked;}public void tryToggle(){toggles++;}public void removeDummy(int x,int y,int z){throw new AssertionError("out-of-scope animation");}public void placeDummy(int x,int y,int z){throw new AssertionError("out-of-scope animation");}public void finishOpen(){throw new AssertionError("out-of-scope animation");}public void finishClose(){throw new AssertionError("out-of-scope animation");}')]
 for name,path,sig,fields in fixtures:out(name+'.java',imports+'import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;public class '+name+' extends TileEntity '+('implements net.minecraft.util.ITickable ' if name in ['Chlorine','BlastDoor'] else '')+'{'+fields+'\n'+method(path,sig)+'\n}')
 # Complete REDSTONE field initializer, original NTM type name retained as a declared domain boundary.
 o=next(o for o in d['operations'] if o['path'].endswith('LogicBlockConditions.java') and 'return world.' in (o['field'] or ''));field=o['field'].replace(o['old'],o['new'])
 out('LogicBlock.java','import net.minecraft.tileentity.TileEntity;public class LogicBlock {public static class TileEntityLogicBlock extends TileEntity {}}')
 out('LogicCondition.java',imports+'import java.util.function.Function;public class LogicCondition {'+field+';}')
 out('PacketDispatcher.java','public class PacketDispatcher {public static int packets;public static final Sink wrapper=new Sink();public static class Sink {public void sendToAllAround(TEVaultPacket p,net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint point){packets++;}}}')
 out('TEVaultPacket.java','public class TEVaultPacket {public TEVaultPacket(int x,int y,int z,boolean opening,int state,int a,int b){}}')
 out('PowerNativeTest.java',(H/'PowerNativeTest.java.in').read_text().replace('RUN_EXPRESSIONS','\n'.join(calls)).replace('LOCAL_CASE',str(next(i for i,o in enumerate(d['operations']) if o['arguments'][0]=='pos.getX()'))).replace('SCALAR_CASE',str(next(i for i,o in enumerate(d['operations']) if o['receiver']=='world' and o['arguments']==['x','y','z']))).replace('PLUS_ONE_CASE',str(next(i for i,o in enumerate(d['operations']) if o['arguments'][1].endswith('+ 1')))).replace('PLUS_SIX_CASE',str(next(i for i,o in enumerate(d['operations']) if o['arguments'][1].endswith('+ 6')))).replace('OFFSET_CASE',str(next(i for i,o in enumerate(d['operations']) if o['arguments'][0]=='this.pos.getX() + x'))).replace('TILE_CASE',str(next(i for i,o in enumerate(d['operations']) if o['arguments']==['this.pos.getX()', 'this.pos.getY()', 'this.pos.getZ()']))))
 files += [S/p for p in ['com/hbm/util/fauxpointtwelve/BlockPos.java','com/hbm/util/fauxpointtwelve/DirPos.java','com/hbm/util/fauxpointtwelve/Rotation.java','com/hbm/lib/ForgeDirection.java','com/hbm/interfaces/Spaghetti.java']]
 return files

def pin_inputs(java_home,classpath_file):
 cp=classpath_file.read_text().strip();world='net/minecraft/world/World.class';found=None
 for entry in cp.split(':'):
  p=Path(entry)
  if p.is_dir():assert not (p/world).exists(),('unapproved World class directory',p)
  elif p.is_file():
   with zipfile.ZipFile(p) as z:
    if world in z.namelist():found=p;break
 assert found and proof.sha(found.read_bytes())=='8c75037188f87bc62939eefa03c2480551509034f069728de012c09276f8f49e'
 inputs={str(p):proof.sha(p.read_bytes()) for p in [found,classpath_file,java_home/'bin/java',java_home/'bin/javac',java_home/'bin/javap']}
 collaborators={p:proof.sha((S/p).read_bytes()) for p in ['com/hbm/util/fauxpointtwelve/BlockPos.java','com/hbm/util/fauxpointtwelve/DirPos.java','com/hbm/util/fauxpointtwelve/Rotation.java','com/hbm/lib/ForgeDirection.java','com/hbm/interfaces/Spaghetti.java']}
 (H/'input-sha256.json').write_text(json.dumps({'inputs':inputs,'complete_coordinate_collaborators':collaborators},indent=2)+'\n');return cp

def main():
 p=argparse.ArgumentParser();p.add_argument('--java-home',type=Path,required=True);p.add_argument('--classpath-file',type=Path,required=True);p.add_argument('--mutants',action='store_true');a=p.parse_args();proof.audit();cp=pin_inputs(a.java_home,a.classpath_file)
 with tempfile.TemporaryDirectory(prefix='native-power-') as td:
  t=Path(td);files=generate(t);classes=t/'classes';classes.mkdir();jc=[str(a.java_home/'bin/javac'),'-encoding','UTF-8','-sourcepath',str(t),'-cp',str(classes)+':'+cp,'-d',str(classes)];subprocess.run(jc+list(map(str,files)),check=True);cmd=[str(a.java_home/'bin/java'),'-Xmx400m','-cp',str(classes)+':'+cp,'PowerNativeTest'];subprocess.run(cmd,check=True,cwd=t)
  names=['ExpressionCases','MultiMode','SingleMode','Dipole','MiningLaser','DoorPower','Chlorine','BlastDoor','LogicCondition'];dis=subprocess.check_output([str(a.java_home/'bin/javap'),'-p','-c','-classpath',str(classes)]+names,text=True);(H/'native-attribution.log').write_text(dis);api=subprocess.check_output([str(a.java_home/'bin/javap'),'-p','-c','-classpath',cp,'net.minecraft.tileentity.TileEntity','net.minecraft.util.math.BlockPos','net.minecraft.world.World'],text=True);assert 'protected net.minecraft.world.World world;' in api and 'protected net.minecraft.util.math.BlockPos pos;' in api and 'public boolean isBlockPowered(net.minecraft.util.math.BlockPos);' in api and 'isBlockIndirectlyGettingPowered' not in api
  (H/'native-api-receipt.json').write_text(json.dumps({'native_api_javap_sha256':proof.sha(api.encode()),'expression_and_complete_body_native_invocations':dis.count('Method net/minecraft/world/World.isBlockPowered:'),'native_TileEntity_fields_verified':True,'World_scalar_method_absent':True},indent=2)+'\n')
  if a.mutants:
   muts=[('SingleMode.java','? this.redHigh : this.redLow','? this.redLow : this.redHigh','inverted mode'),('ExpressionCases.java','isBlockPowered(new net.minecraft.util.math.BlockPos(x, y, z))','isBlockPowered(new net.minecraft.util.math.BlockPos(z, y, x))','swapped coordinates'),('BlastDoor.java','!isLocked() &&','!isLocked() ||','changed short circuit'),('Dipole.java','if(this.world.isBlockPowered','if(!this.world.isBlockPowered','inverted multi-port predicate')]
   for file,before,after,label in muts:
    path=t/file;orig=path.read_text();assert before in orig;path.write_text(orig.replace(before,after));subprocess.run(jc+[str(path)],check=True,capture_output=True);res=subprocess.run(cmd,cwd=t,capture_output=True,text=True);assert res.returncode and 'AssertionError' in res.stderr,(label,res.stdout,res.stderr);print('PASS runtime mutant rejected: '+label);path.write_text(orig);subprocess.run(jc+[str(path)],check=True,capture_output=True)
   path=t/'SingleMode.java';orig=path.read_text();path.write_text(orig.replace('isBlockPowered(new net.minecraft.util.math.BlockPos(this.pos.getX(), this.pos.getY(), this.pos.getZ()))','isBlockIndirectlyGettingPowered(this.pos.getX(), this.pos.getY(), this.pos.getZ())'));res=subprocess.run(jc+[str(path)],capture_output=True,text=True);assert res.returncode and 'cannot find symbol' in res.stderr;print('PASS compiler mutant rejected: no old scalar World API')
if __name__=='__main__':main()
