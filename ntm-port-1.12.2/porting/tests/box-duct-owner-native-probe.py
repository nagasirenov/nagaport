#!/usr/bin/env python3
"""Compile the complete native boxed duct/cable owner classes against real 1.12.2.
Only unrelated NTM collaborators are controlled. The pristine collision source
is executed as an independent oracle, and production connectivity is retained.
"""
import argparse, hashlib, os, re, subprocess, tempfile
from pathlib import Path
p=argparse.ArgumentParser()
p.add_argument('--java-home',type=Path,required=True)
p.add_argument('--classpath',required=True)
p.add_argument('--root',type=Path,default=Path(__file__).resolve().parents[2])
p.add_argument('--original-root',type=Path,default=Path('/workspace/shared/nagaport/ntm-original-pristine'))
a=p.parse_args();root=a.root;src=root/'src/main/java';tests=Path(__file__).resolve().parent
exe='.exe' if os.name=='nt' else ''
if not (a.java_home/'bin'/('javac'+exe)).is_file():a.java_home=a.java_home.parent
def method(text,name):
 m=re.search(r'public (?:static )?[^\n]+\b'+name+r'\([^)]*\)\s*\{',text);assert m,name
 end=m.end();depth=1
 while depth:depth+=(text[end]=='{')-(text[end]=='}');end+=1
 return text[m.end():end-1]
pinned={'FluidDuctBox':'950eca35e7463d23e5b2df983a1f187bef5bc8ce09254a781af8617034728866','PowerCableBox':'20f4996a35e0a45e613d102c927bc102e44916b48ace58309187b07a256e4767','FluidDuctBoxExhaust':'3c5ac6aa9abd601494adea4b345de0b62864a32850a10fbc011f875e68484afa'}
originals={}
for name,want in pinned.items():
 f=a.original_root/('src/main/java/com/hbm/blocks/network/'+name+'.java');got=hashlib.sha256(f.read_bytes()).hexdigest();assert got==want, f'Pristine source changed: {name}: {got}'
 originals[name]=f.read_text(); print('PINNED ORIGINAL',name,got,flush=True)
 expected=method(originals[name],'canConnectTo');current=method((src/('com/hbm/blocks/network/'+name+'.java')).read_text(),'canConnectTo')
 compact=lambda value: re.sub(r'\s+','',re.sub(r'\bdir\b','direction',value))
 assert compact(expected)==compact(current), 'Routing changed beyond local variable name: '+name
 print('EXACT PRISTINE ROUTING',name,flush=True)
flags='boolean pX=(mask&32)!=0,nX=(mask&16)!=0,pY=(mask&8)!=0,nY=(mask&4)!=0,pZ=(mask&2)!=0,nZ=(mask&1)!=0;int count=Integer.bitCount(mask);'
oracle='import java.util.*;import net.minecraft.util.math.*;public final class OriginalBoxDuctOracle {'
for name,label in [('FluidDuctBox','fluid'),('PowerCableBox','cable')]:
 old=originals[name]
 collision=method(old,'addCollisionBoxesToList')
 setup=collision[collision.index('double lower'):collision.index('boolean nX')].replace('int meta = world.getBlockMetadata(x, y, z);','')
 body=collision[collision.index('if(mask == 0)'):collision.index('for(AxisAlignedBB bb')].replace('AxisAlignedBB.getBoundingBox(', 'new AxisAlignedBB(')
 oracle+='public static List<AxisAlignedBB> '+label+'Collisions(int meta,int mask,BlockPos pos){int x=pos.getX(),y=pos.getY(),z=pos.getZ();List<AxisAlignedBB> bbs=new ArrayList<>();'+setup+flags+body+'return bbs;}'
 selection=method(old,'setBlockBoundsBasedOnState')
 setup=selection[selection.index('float lower'):selection.index('boolean nX')].replace('int meta = world.getBlockMetadata(x, y, z);','')
 body=selection[selection.index('if(mask == 0)'):].replace('this.setBlockBounds(', 'return new AxisAlignedBB(')
 oracle+='public static AxisAlignedBB '+label+'Bounds(int meta,int mask){'+setup+flags+body+'}'
oracle+='}'
lib=(src/'com/hbm/lib/Library.java').read_text();energy=method(lib,'canConnect');fluid=method(lib,'canConnectFluid')
collaborators={
 'com/hbm/main/MainRegistry.java':'package com.hbm.main;public final class MainRegistry {public static final MainRegistry instance=new MainRegistry();public final java.util.Random rand=new java.util.Random(12345L);}',
 'com/hbm/lib/Library.java':'''package com.hbm.lib;import net.minecraft.world.IBlockAccess;import net.minecraft.block.Block;import net.minecraft.tileentity.TileEntity;import com.hbm.inventory.fluid.FluidType;import api.hbm.energymk2.*;import api.hbm.fluidmk2.*;public final class Library {public static final ForgeDirection POS_X=ForgeDirection.EAST,NEG_X=ForgeDirection.WEST,POS_Y=ForgeDirection.UP,NEG_Y=ForgeDirection.DOWN,POS_Z=ForgeDirection.SOUTH,NEG_Z=ForgeDirection.NORTH;public static boolean canConnect(IBlockAccess world,int x,int y,int z,ForgeDirection dir){'''+energy+'}public static boolean canConnectFluid(IBlockAccess world,int x,int y,int z,ForgeDirection dir,FluidType type){'+fluid+'}}',
 'com/hbm/inventory/fluid/FluidType.java':'''package com.hbm.inventory.fluid;public class FluidType {private final int id,color;private final String name;public FluidType(int id,int color,String name){this.id=id;this.color=color;this.name=name;}public int getID(){return id;}public int getColor(){return color;}public String getLocalizedName(){return name;}public Object getNetworkProvider(){return this;}}''',
 'com/hbm/inventory/fluid/Fluids.java':'''package com.hbm.inventory.fluid;public final class Fluids {public static final FluidType NONE=new FluidType(0,0xffffff,"None"),SMOKE=new FluidType(1,0x333333,"Smoke"),SMOKE_LEADED=new FluidType(2,0x444444,"Leaded smoke"),SMOKE_POISON=new FluidType(3,0x555555,"Poison smoke");}''',
 'com/hbm/tileentity/network/TileEntityPipeBaseNT.java':'''package com.hbm.tileentity.network;import com.hbm.inventory.fluid.*;import com.hbm.lib.ForgeDirection;public class TileEntityPipeBaseNT extends net.minecraft.tileentity.TileEntity implements api.hbm.fluidmk2.IFluidConnectorMK2 {public FluidType type=Fluids.NONE;public int writes;public FluidType getType(){return type;}public void setType(FluidType type){this.type=type;writes++;}public boolean canConnect(FluidType type,ForgeDirection dir){return dir!=ForgeDirection.UNKNOWN&&type==this.type;}}''',
 'com/hbm/items/ModItems.java':'''package com.hbm.items;public final class ModItems {public static net.minecraft.item.Item fluid_duct;}''',
 'com/hbm/handler/HbmKeybinds.java':'''package com.hbm.handler;public final class HbmKeybinds {public enum EnumKeybind{TOOL_CTRL,TOOL_ALT;}}''',
 'com/hbm/extprop/HbmPlayerProps.java':'''package com.hbm.extprop;import com.hbm.handler.HbmKeybinds.EnumKeybind;public class HbmPlayerProps {public static final HbmPlayerProps INSTANCE=new HbmPlayerProps();public boolean ctrl,alt;public static HbmPlayerProps getData(net.minecraft.entity.player.EntityPlayer p){return INSTANCE;}public boolean getKeyPressed(EnumKeybind key){return key==EnumKeybind.TOOL_CTRL?ctrl:alt;}}''',
 'com/hbm/items/machine/ItemFluidIDMulti.java':'''package com.hbm.items.machine;import com.hbm.inventory.fluid.*;import net.minecraft.item.*;import net.minecraft.world.World;public class ItemFluidIDMulti extends Item implements IItemFluidIdentifier {public FluidType type=Fluids.NONE;public static int writes;public static boolean lastFlag;public FluidType getType(World w,int x,int y,int z,ItemStack stack){return type;}public static void setType(ItemStack stack,FluidType t,boolean flag){((ItemFluidIDMulti)stack.getItem()).type=t;writes++;lastFlag=flag;}}''',
 'com/hbm/uninos/UniNodespace.java':'''package com.hbm.uninos;public final class UniNodespace {public static Object node,provider;public static net.minecraft.util.math.BlockPos pos;public static Object getNode(net.minecraft.world.World world,int x,int y,int z,Object network){pos=new net.minecraft.util.math.BlockPos(x,y,z);provider=network;return node;}}''',
 'api/hbm/fluidmk2/FluidNode.java':'''package api.hbm.fluidmk2;public class FluidNode {public FluidNetMK2 net;}''',
 'api/hbm/fluidmk2/FluidNetMK2.java':'''package api.hbm.fluidmk2;public class FluidNetMK2 {public java.util.List<Object> links=new java.util.ArrayList<>(),receiverEntries=new java.util.ArrayList<>(),providerEntries=new java.util.ArrayList<>();public long fluidTracker;}''',
 'com/hbm/blocks/ILookOverlay.java':'''package com.hbm.blocks;import java.util.*;import net.minecraft.world.World;import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;public interface ILookOverlay {void printHook(Pre event,World world,int x,int y,int z);public static class Capture{public static String title;public static int titleColor,backgroundColor,calls;public static List<String> text;}static void printGeneric(Pre event,String title,int titleColor,int backgroundColor,List<String> text){Capture.title=title;Capture.titleColor=titleColor;Capture.backgroundColor=backgroundColor;Capture.text=text;Capture.calls++;}}''',
 'com/hbm/util/i18n/I18nUtil.java':'''package com.hbm.util.i18n;public class I18nUtil {public static String resolveKey(String key){return key;}}''',
}
collaborators['com/hbm/tileentity/network/TileEntityCableBaseNT.java']='package com.hbm.tileentity.network;public class TileEntityCableBaseNT extends net.minecraft.tileentity.TileEntity implements api.hbm.energymk2.IEnergyConnectorMK2 {}'
collaborators['com/hbm/tileentity/network/TileEntityPipeExhaust.java']='package com.hbm.tileentity.network;import com.hbm.inventory.fluid.*;import com.hbm.lib.ForgeDirection;public class TileEntityPipeExhaust extends net.minecraft.tileentity.TileEntity implements api.hbm.fluidmk2.IFluidConnectorMK2 {public boolean canConnect(FluidType type,ForgeDirection dir){return dir!=ForgeDirection.UNKNOWN&&(type==Fluids.SMOKE||type==Fluids.SMOKE_LEADED||type==Fluids.SMOKE_POISON);}}'
lighten=method((src/'com/hbm/util/ColorUtil.java').read_text(),'lightenColor')
collaborators['com/hbm/util/ColorUtil.java']='package com.hbm.util;import java.awt.Color;public final class ColorUtil {public static int lightenColor(int hex,double percent){'+lighten+'}}'
production=[src/rel for rel in ['com/hbm/blocks/network/BoxDuctRenderState.java','com/hbm/blocks/network/FluidDuctBase.java','com/hbm/blocks/network/FluidDuctBox.java','com/hbm/blocks/network/PowerCableBox.java','com/hbm/blocks/network/FluidDuctBoxExhaust.java','com/hbm/blocks/network/ConnectionState.java','com/hbm/blocks/network/FluidConnectionState.java','com/hbm/blocks/network/IBlockFluidDuct.java','com/hbm/blocks/IAnalyzable.java','com/hbm/blocks/IBlockMulti.java','com/hbm/items/machine/IItemFluidIdentifier.java','com/hbm/lib/ForgeDirection.java','api/hbm/energymk2/IEnergyConnectorBlock.java','api/hbm/energymk2/IEnergyConnectorMK2.java','api/hbm/fluidmk2/IFluidConnectorMK2.java','api/hbm/fluidmk2/IFluidConnectorBlockMK2.java']]
production += [src/rel for rel in ['com/hbm/blocks/ModSoundType.java','com/hbm/blocks/ModSoundTypes.java','com/hbm/main/ModSoundEvents.java','com/hbm/lib/RefStrings.java']]
with tempfile.TemporaryDirectory(prefix='box-duct-owner-native-') as tmp:
 out=Path(tmp);files=[]
 for rel,content in collaborators.items():
  assert rel.startswith(('com/hbm/','api/hbm/')), 'No Minecraft/Forge facades allowed'
  target=out/rel;target.parent.mkdir(parents=True,exist_ok=True);target.write_text(content);files.append(str(target))
 print('EXTRACTED production Library.canConnect SHA256',hashlib.sha256(energy.encode()).hexdigest(),flush=True)
 print('EXTRACTED production Library.canConnectFluid SHA256',hashlib.sha256(fluid.encode()).hexdigest(),flush=True)
 print('EXTRACTED production ColorUtil.lightenColor SHA256',hashlib.sha256(lighten.encode()).hexdigest(),flush=True)
 for source in production:
  print('PRODUCTION',source,hashlib.sha256(source.read_bytes()).hexdigest(),flush=True);files.append(str(source))
 for name,content in [('OriginalBoxDuctOracle',oracle),('BoxDuctOwnerNativeTest',(tests/'box-duct-owner-native-test.java.in').read_text())]:
  f=out/(name+'.java');f.write_text(content);files.append(str(f))
 subprocess.run([str(a.java_home/'bin'/('javac'+exe)),'-encoding','UTF-8','-source','8','-target','8','-cp',a.classpath,'-d',tmp]+files,check=True)
 subprocess.run([str(a.java_home/'bin'/('java'+exe)),'-Djava.awt.headless=true','-cp',os.pathsep.join([tmp,a.classpath]),'BoxDuctOwnerNativeTest'],cwd=tmp,check=True)
