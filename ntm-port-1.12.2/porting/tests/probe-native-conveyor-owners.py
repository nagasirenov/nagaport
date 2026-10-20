#!/usr/bin/env python3
"""Compile complete conveyor owners against cached native 1.12.2 APIs and compare movement
with verbatim pristine 1.7.10 method bodies in an explicitly labeled mutable-vector oracle.
No Minecraft/Forge classes or production owner methods are stubbed or source-extracted.
"""
import argparse, hashlib, os, re, subprocess, tempfile
from pathlib import Path
p=argparse.ArgumentParser();p.add_argument('--java-home',type=Path,required=True);p.add_argument('--classpath',required=True);p.add_argument('--pristine',type=Path,default=Path('/workspace/shared/nagaport/ntm-original-pristine'));a=p.parse_args()
r=Path(__file__).resolve().parents[2];s=r/'src/main/java';exe='.exe' if os.name=='nt' else ''
names=['Base','Bendable','','Double','Triple','Express','Lift','Chute','Classic']
collaborators={
 'com/hbm/blocks/ModBlocks.java':'package com.hbm.blocks; public final class ModBlocks {public static net.minecraft.block.Block conveyor, conveyor_lift, conveyor_chute;}',
 'com/hbm/items/ModItems.java':'package com.hbm.items; public final class ModItems {public static net.minecraft.item.Item conveyor_wand;}',
 'com/hbm/inventory/RecipesCommon.java':'package com.hbm.inventory; public class RecipesCommon {public static class ComparableStack {public ComparableStack(net.minecraft.item.ItemStack s){}}}',
 'com/hbm/util/i18n/I18nUtil.java':'package com.hbm.util.i18n; public class I18nUtil {public static String[] resolveKeyArray(String s){return new String[]{s};}}',
 'com/hbm/conveyorprobe/Events.java':'package com.hbm.conveyorprobe; public final class Events {public static final java.util.List<String> events=new java.util.ArrayList<>();}',
 'com/hbm/entity/item/EntityMovingItem.java':'''package com.hbm.entity.item;
public class EntityMovingItem extends net.minecraft.entity.Entity {
 public net.minecraft.item.ItemStack stack;
 public EntityMovingItem(net.minecraft.world.World w){super(w);com.hbm.conveyorprobe.Events.events.add("construct");}
 public void setItemStack(net.minecraft.item.ItemStack s){stack=s;com.hbm.conveyorprobe.Events.events.add("stack");}
 @Override public void setPositionAndRotation(double x,double y,double z,float yaw,float pitch){super.setPositionAndRotation(x,y,z,yaw,pitch);com.hbm.conveyorprobe.Events.events.add("position");}
 protected void entityInit(){} protected void readEntityFromNBT(net.minecraft.nbt.NBTTagCompound n){} protected void writeEntityToNBT(net.minecraft.nbt.NBTTagCompound n){}
}'''
}
production=['com/hbm/blocks/network/BlockConveyor'+n+'.java' for n in names]+['com/hbm/blocks/network/ConveyorModelState.java','com/hbm/blocks/ITooltipProvider.java','com/hbm/lib/ForgeDirection.java','api/hbm/block/IToolable.java']+['api/hbm/conveyor/'+n+'.java' for n in ['IConveyorBelt','IEnterableBlock','IConveyorItem','IConveyorPackage']]
def method(text,start):
 b=text.index('{',start)+1;e=b;depth=1
 while depth:depth+=(text[e]=='{')-(text[e]=='}');e+=1
 return text[start:e]
oracle='''import com.hbm.lib.ForgeDirection; import api.hbm.conveyor.*; import net.minecraft.block.Block; import net.minecraft.util.math.MathHelper; import net.minecraft.util.math.BlockPos; import net.minecraft.world.World;
final class LegacyConveyorOwnerOracle {
 static final class OracleWorld {final World world;OracleWorld(World w){world=w;}int getBlockMetadata(int x,int y,int z){net.minecraft.block.state.IBlockState s=world.getBlockState(new BlockPos(x,y,z));return s.getBlock().getMetaFromState(s);}Block getBlock(int x,int y,int z){return world.getBlockState(new BlockPos(x,y,z)).getBlock();}}
 static final class MutableVec {double xCoord,yCoord,zCoord;MutableVec(double x,double y,double z){xCoord=x;yCoord=y;zCoord=z;}static MutableVec createVectorHelper(double x,double y,double z){return new MutableVec(x,y,z);}double lengthVector(){return MathHelper.sqrt(xCoord*xCoord+yCoord*yCoord+zCoord*zCoord);}}
'''
for n in names:
 path=a.pristine/'src/main/java/com/hbm/blocks/network'/('BlockConveyor'+n+'.java');original=path.read_text();print('PRISTINE',path.name,hashlib.sha256(path.read_bytes()).hexdigest(),flush=True)
 parent={'Bendable':'Base','':'Bendable','Double':'Bendable','Triple':'Bendable','Express':'Bendable','Lift':'Base','Chute':'Base'}.get(n)
 oracle+='static '+('abstract ' if n in ['Base','Bendable'] else '')+'class Oracle'+(n or 'Standard')+(' extends Oracle'+parent if parent else '')+' {\n'
 for m in re.finditer(r'(?:public|protected) (?:boolean|Vec3|ForgeDirection|int) (canItemStay|getTravelLocation|getClosestSnappingPosition|getTravelDirection|getInputDirection|getOutputDirection|getPathDirection)\(',original):
  body=method(original,m.start()).replace('World world','OracleWorld world').replace('Vec3','MutableVec').replace('MathHelper.clamp_double','MathHelper.clamp')
  oracle+=body+'\n'
 oracle+='}\n'
oracle+='}\n'
with tempfile.TemporaryDirectory(prefix='native-conveyor-owners-') as d:
 out=Path(d);files=[]
 for rel,text in collaborators.items():
  f=out/rel;f.parent.mkdir(parents=True,exist_ok=True);f.write_text(text);files.append(str(f))
 f=out/'LegacyConveyorOwnerOracle.java';f.write_text(oracle);files.append(str(f))
 for rel in production:
  f=s/rel;print('PRODUCTION',rel,hashlib.sha256(f.read_bytes()).hexdigest(),flush=True);files.append(str(f))
 files.append(str(r/'porting/tests/ConveyorOwnersNativeTest.java'))
 subprocess.run([str(a.java_home/'bin'/('javac'+exe)),'-encoding','UTF-8','-source','8','-target','8','-sourcepath',str(out),'-cp',a.classpath,'-d',d]+files,check=True)
 print('PASS complete nine conveyor owners and actual native state/helper/API dependencies compiled.',flush=True)
 subprocess.run([str(a.java_home/'bin'/('java'+exe)),'-cp',os.pathsep.join([d,a.classpath]),'ConveyorOwnersNativeTest'],cwd=d,check=True)
print('PASS bounded real-World/Chunk conveyor owners; no actual EntityMovingItem tick, render/GL, save/server or full-mod acceptance.',flush=True)
