#!/usr/bin/env python3
"""Whole six production block owners plus whole TileEntityCraneBase against native 1.12.2.
External concrete crane engines, MachineBase and unrelated NTM interfaces/utilities are bounded collaborators.
No Minecraft/Forge class or production owner/base-tile method is substituted.
"""
import argparse,hashlib,os,pathlib,re,subprocess,tempfile
from side_rotation_fixtures import pristine_root
p=argparse.ArgumentParser();p.add_argument('--classpath',required=True);p.add_argument('--java-home',type=pathlib.Path,required=True);p.add_argument('--pristine',type=pathlib.Path,default=None);a=p.parse_args();a.pristine=pristine_root(a.pristine)
r=pathlib.Path(__file__).resolve().parents[2];s=r/'src/main/java';t=r/'porting/tests'
names=['BlockCraneBase','CraneBoxer','CraneExtractor','CraneGrabber','CraneInserter','CraneUnboxer']
production=['com/hbm/blocks/network/'+n+'.java' for n in names]+['com/hbm/tileentity/network/TileEntityCraneBase.java','com/hbm/blocks/IBlockSideRotation.java','com/hbm/blocks/SideRotationModelState.java','com/hbm/blocks/ITooltipProvider.java','com/hbm/lib/ForgeDirection.java','com/hbm/lib/RefStrings.java','api/hbm/block/IToolable.java']+['api/hbm/conveyor/'+n+'.java' for n in ['IConveyorItem','IConveyorPackage','IEnterableBlock']]
collaborators={
'com/hbm/inventory/RecipesCommon.java':'package com.hbm.inventory;public class RecipesCommon {public static class ComparableStack{public ComparableStack(net.minecraft.item.ItemStack s){}}}',
'com/hbm/util/i18n/I18nUtil.java':'package com.hbm.util.i18n;public class I18nUtil{public static String[] resolveKeyArray(String s){return new String[]{s};}}',
'com/hbm/items/tool/ItemTooling.java':'package com.hbm.items.tool;public class ItemTooling extends net.minecraft.item.Item{}',
'com/hbm/items/tool/ItemConveyorWand.java':'package com.hbm.items.tool;public class ItemConveyorWand extends net.minecraft.item.Item{}',
'com/hbm/main/MainRegistry.java':'package com.hbm.main;public class MainRegistry{public static final Object instance=new Object();}',
'com/hbm/util/InventoryUtil.java':'''package com.hbm.util;public class InventoryUtil {public static int calls,lastSide; public static int[] masquerade(net.minecraft.inventory.ISidedInventory s,int side){calls++;lastSide=side;return s instanceof net.minecraft.tileentity.TileEntityFurnace?new int[]{1,0}:s.getSlotsForFace(com.hbm.lib.ForgeDirection.getOrientation(side).toEnumFacing());}}''',
'com/hbm/interfaces/ICopiable.java':'''package com.hbm.interfaces;public interface ICopiable{net.minecraft.nbt.NBTTagCompound getSettings(net.minecraft.world.World w,int x,int y,int z);void pasteSettings(net.minecraft.nbt.NBTTagCompound n,int i,net.minecraft.world.World w,net.minecraft.entity.player.EntityPlayer p,int x,int y,int z);String[] infoForDisplay(net.minecraft.world.World w,int x,int y,int z);}''',
'com/hbm/tileentity/IControlReceiverFilter.java':'''package com.hbm.tileentity;public interface IControlReceiverFilter {int[] getFilterSlots();void nextMode(int i);}''',
'com/hbm/tileentity/TileEntityMachineBase.java':'''package com.hbm.tileentity;
import net.minecraft.inventory.ISidedInventory;import net.minecraft.item.ItemStack;import net.minecraft.nbt.*;import net.minecraft.util.EnumFacing;import net.minecraft.entity.player.EntityPlayer;
public abstract class TileEntityMachineBase extends net.minecraft.tileentity.TileEntity implements ISidedInventory,net.minecraft.util.ITickable {
 public final ItemStack[] slots; public int limit=64,dirty;public boolean insert=true,valid=true;public int[] access;public EnumFacing lastFace;public final java.util.List<Integer> insertCalls=new java.util.ArrayList<>(),validCalls=new java.util.ArrayList<>();
 public TileEntityMachineBase(int count){slots=new ItemStack[count];access=new int[count];for(int i=0;i<count;i++){slots[i]=ItemStack.EMPTY;access[i]=i;}}
 public int getSizeInventory(){return slots.length;}public boolean isEmpty(){for(ItemStack s:slots)if(s!=null&&!s.isEmpty())return false;return true;}public ItemStack getStackInSlot(int i){return slots[i];}
 public ItemStack decrStackSize(int i,int n){return slots[i].splitStack(n);}public ItemStack removeStackFromSlot(int i){ItemStack s=slots[i];slots[i]=ItemStack.EMPTY;return s;}public void setInventorySlotContents(int i,ItemStack s){slots[i]=s;}
 public int getInventoryStackLimit(){return limit;}public boolean isUsableByPlayer(EntityPlayer p){return true;}public void openInventory(EntityPlayer p){}public void closeInventory(EntityPlayer p){}public boolean isItemValidForSlot(int i,ItemStack s){validCalls.add(i);return valid;}
 public int getField(int id){return 0;}public void setField(int id,int v){}public int getFieldCount(){return 0;}public void clear(){java.util.Arrays.fill(slots,ItemStack.EMPTY);}public String getName(){return "crane collaborator";}public boolean hasCustomName(){return false;}public net.minecraft.util.text.ITextComponent getDisplayName(){return new net.minecraft.util.text.TextComponentString(getName());}
 public int[] getSlotsForFace(EnumFacing f){lastFace=f;return access;}public boolean canInsertItem(int i,ItemStack s,EnumFacing f){insertCalls.add(i);lastFace=f;return insert;}public boolean canExtractItem(int i,ItemStack s,EnumFacing f){return true;}
 @Override public void markDirty(){dirty++;super.markDirty();}
 @Override public NBTTagCompound writeToNBT(NBTTagCompound n){super.writeToNBT(n);NBTTagList l=new NBTTagList();for(int i=0;i<slots.length;i++)if(slots[i]!=null&&!slots[i].isEmpty()){NBTTagCompound q=new NBTTagCompound();q.setInteger("slot",i);slots[i].writeToNBT(q);l.appendTag(q);}n.setTag("collaboratorInventory",l);return n;}
 @Override public void readFromNBT(NBTTagCompound n){super.readFromNBT(n);clear();NBTTagList l=n.getTagList("collaboratorInventory",10);for(int i=0;i<l.tagCount();i++){NBTTagCompound q=l.getCompoundTagAt(i);slots[q.getInteger("slot")]=new ItemStack(q);}}
}'''
}
for n,count in [('Boxer',21),('Extractor',20),('Grabber',11),('Inserter',21),('Unboxer',23)]:
 collaborators['com/hbm/tileentity/network/TileEntityCrane'+n+'.java']='package com.hbm.tileentity.network;public class TileEntityCrane'+n+' extends TileEntityCraneBase {public boolean destroyer;public TileEntityCrane'+n+'(){super('+str(count)+');}}'
def method(text,start):
 b=text.index('{',start)+1;e=b;d=1
 while d:d+=(text[e]=='{')-(text[e]=='}');e+=1
 return text[start:e]
# Pristine face selectors, rotations and texture mappings; legacy icons are only named strings.
oracle='''import com.hbm.lib.ForgeDirection;import com.hbm.lib.RefStrings;final class LegacyCraneFaceOracle {
static class World {int meta;ForgeDirection outputOverride;int getBlockMetadata(int x,int y,int z){return meta;}}
static class Icons {String registerIcon(String s){return s.replace(":",":blocks/");}}
'''
for name in names:
 src=a.pristine/'src/main/java/com/hbm/blocks/network'/(name+'.java');orig=src.read_text();print('PRISTINE',name,hashlib.sha256(src.read_bytes()).hexdigest(),flush=True)
 oracle+='static class '+name+(' extends BlockCraneBase' if name!='BlockCraneBase' else '')+' {\n'
 if name=='BlockCraneBase':
  fields=['blockIcon']+re.findall(r'protected IIcon (\w+);',orig)
  oracle+='String '+','.join(fields)+';\nForgeDirection getInputSide(World w,int x,int y,int z){return ForgeDirection.getOrientation(w.meta);}\nForgeDirection getOutputSideOverride(World w,int x,int y,int z){return w.outputOverride;}\nForgeDirection getOutputSide(World w,int x,int y,int z){return w.outputOverride==ForgeDirection.UNKNOWN?getInputSide(w,x,y,z).getOpposite():w.outputOverride;}\n'
 oracle+=name+'(){registerBlockIcons(new Icons());}\n'
 for m in re.finditer(r'public (?:void|IIcon|int) (registerBlockIcons|getIcon|getRotationFromSide)\(',orig):
  body=method(orig,m.start()).replace('IIconRegister','Icons').replace('IIcon','String').replace('IBlockAccess','World');oracle+=body+'\n'
 oracle+='}\n'
oracle+='}\n'
print('ORACLE_SHA256',hashlib.sha256(oracle.encode()).hexdigest(),flush=True)
with tempfile.TemporaryDirectory(prefix='native-crane-owners-') as d:
 out=pathlib.Path(d);files=[s/path for path in production]+[t/'NativeCraneOwnersTest.java',t/'ForgeEventNativeLauncher.java']
 for path,body in collaborators.items():
  f=out/'fixtures'/path;f.parent.mkdir(parents=True,exist_ok=True);f.write_text(body);files.append(f)
 f=out/'LegacyCraneFaceOracle.java';f.write_text(oracle);files.append(f)
 subprocess.run([str(a.java_home/'bin/javac'),'-encoding','UTF-8','-source','8','-target','8','-sourcepath',str(out/'fixtures'),'-cp',a.classpath,'-d',d]+list(map(str,files)),check=True)
 print('PASS six whole production owners, actual crane tile base, native state and interfaces compile.',flush=True)
 subprocess.run([str(a.java_home/'bin/java'),'-cp',d+os.pathsep+a.classpath,'ForgeEventNativeLauncher','NativeCraneOwnersTest'],cwd=d,check=True)
 for path in production:print('PRODUCTION',hashlib.sha256((s/path).read_bytes()).hexdigest(),path,flush=True)
print('BOUNDARY: real native registry/World/Chunk/Block/ItemStack/NBT/Container/redstone, bounded chunk provider/light skip/entity output; explicit NTM MachineBase/concrete tile engines/copy/filter/InventoryUtil/tool/registry collaborators. No production tile-engine tick/save/network transport/GUI/GL/full-mod acceptance.',flush=True)
