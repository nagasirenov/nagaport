#!/usr/bin/env python3
"""Complete production thermal blocks and baked models on real Forge/Minecraft.
Only the listed NTM registry, GUI owner, inventory tile, sealability and proxy/helper
collaborators are controlled. Never generates net.minecraft or net.minecraftforge classes.
"""
import argparse, hashlib, os, re, subprocess, sys, tempfile, zipfile
from pathlib import Path
p=argparse.ArgumentParser(); p.add_argument('--java-home',type=Path,required=True); p.add_argument('--classpath',required=True)
a=p.parse_args(); root=Path(__file__).resolve().parents[2]; src=root/'src/main/java'; tests=root/'porting/tests'
subprocess.run([sys.executable,str(tests/'thermal-block-source-test.py')],check=True)
exe='.exe' if os.name=='nt' else ''
if not (a.java_home/'bin'/('javac'+exe)).is_file(): a.java_home=a.java_home.parent
names=['MachineBrickFurnace','MachineRtgFurnace','MachineDiFurnaceRTG','MachineDiFurnace','MachineDiFurnaceExtension']
tiles=['TileEntityFurnaceBrick','TileEntityRtgFurnace','TileEntityDiFurnaceRTG','TileEntityDiFurnace']
ids=['machine_furnace_brick_off','machine_furnace_brick_on','machine_rtg_furnace_off','machine_rtg_furnace_on','machine_difurnace_rtg_off','machine_difurnace_rtg_on','machine_difurnace_off','machine_difurnace_on','machine_difurnace_extension']
collaborators={
'com/hbm/blocks/ModBlocks.java':'package com.hbm.blocks; public final class ModBlocks { public static net.minecraft.block.Block '+','.join(ids)+'; }',
'com/hbm/main/MainRegistry.java':'package com.hbm.main; public final class MainRegistry { public static final Object instance=new Object(); }',
'com/hbm/handler/atmosphere/IBlockSealable.java':'package com.hbm.handler.atmosphere; public interface IBlockSealable { boolean isSealed(net.minecraft.world.World world,int x,int y,int z); }',
'com/hbm/util/Compat.java':'package com.hbm.util; public final class Compat { public static net.minecraft.tileentity.TileEntity getTileStandard(net.minecraft.world.World w,int x,int y,int z) { return w.getTileEntity(new net.minecraft.util.math.BlockPos(x,y,z)); } }',
'com/hbm/util/ItemStackUtil.java':'''package com.hbm.util; public final class ItemStackUtil {
 public static int spills; public static void spillItems(net.minecraft.world.World w,int x,int y,int z,net.minecraft.block.Block b,java.util.Random r) { spills++; }
 public static void spillItems(net.minecraft.world.World w,net.minecraft.util.math.BlockPos p,net.minecraft.block.Block b,java.util.Random r) { spills++; }
}''',
'com/hbm/tileentity/TileEntityProxyCombo.java':'''package com.hbm.tileentity; public class TileEntityProxyCombo extends net.minecraft.tileentity.TileEntity {
 public boolean inventoryEnabled,fluidEnabled;
 public TileEntityProxyCombo inventory(){inventoryEnabled=true;return this;}
 public TileEntityProxyCombo fluid(){fluidEnabled=true;return this;}
}''',
'com/hbm/tileentity/machine/ThermalTileFixture.java':'''package com.hbm.tileentity.machine;
import net.minecraft.item.ItemStack; import net.minecraft.entity.player.EntityPlayer; import net.minecraft.util.EnumFacing;
public class ThermalTileFixture extends net.minecraft.tileentity.TileEntity implements net.minecraft.inventory.ISidedInventory {
 public final ItemStack[] slots=new ItemStack[8]; public String customName; public int validates,invalidates;
 public void setCustomName(String n){customName=n;} public String getName(){return customName==null?"thermal":customName;}
 public boolean hasCustomName(){return customName!=null;} public net.minecraft.util.text.ITextComponent getDisplayName(){return new net.minecraft.util.text.TextComponentString(getName());}
 public int getSizeInventory(){return slots.length;} public boolean isEmpty(){for(ItemStack s:slots)if(s!=null&&!s.isEmpty())return false;return true;}
 public ItemStack getStackInSlot(int i){return slots[i]==null?ItemStack.EMPTY:slots[i];}
 public ItemStack decrStackSize(int i,int n){return getStackInSlot(i).splitStack(n);} public ItemStack removeStackFromSlot(int i){ItemStack s=getStackInSlot(i);slots[i]=ItemStack.EMPTY;return s;}
 public void setInventorySlotContents(int i,ItemStack s){slots[i]=s;} public int getInventoryStackLimit(){return 64;}
 public boolean isUsableByPlayer(EntityPlayer p){return true;} public void openInventory(EntityPlayer p){} public void closeInventory(EntityPlayer p){}
 public boolean isItemValidForSlot(int i,ItemStack s){return true;} public int getField(int id){return 0;} public void setField(int id,int v){} public int getFieldCount(){return 0;}
 public void clear(){java.util.Arrays.fill(slots,ItemStack.EMPTY);} public int[] getSlotsForFace(EnumFacing f){return new int[]{0,1,2,3,4,5,6,7};}
 public boolean canInsertItem(int i,ItemStack s,EnumFacing f){return true;} public boolean canExtractItem(int i,ItemStack s,EnumFacing f){return true;}
 @Override public void validate(){super.validate();validates++;} @Override public void invalidate(){super.invalidate();invalidates++;}
}'''
}
for tile in tiles:
 source=(src/('com/hbm/tileentity/machine/'+tile+'.java')).read_text()
 match=re.search(r'public boolean shouldRefresh\([^)]*\)\s*\{',source)
 refresh=''
 if match:
  end=match.end(); depth=1
  while depth: depth+=(source[end]=='{')-(source[end]=='}');end+=1
  refresh='@Override '+source[match.start():end]
  print('EXTRACTED production '+tile+' shouldRefresh SHA256',hashlib.sha256(refresh.encode()).hexdigest(),flush=True)
 collaborators['com/hbm/tileentity/machine/'+tile+'.java']='package com.hbm.tileentity.machine; import net.minecraft.world.World; import net.minecraft.util.math.BlockPos; import net.minecraft.block.state.IBlockState; public class '+tile+' extends ThermalTileFixture { '+refresh+' }'
production=[src/('com/hbm/blocks/machine/'+n+'.java') for n in names+['BlockThermalFurnace']]
production += [src/'com/hbm/lib/RefStrings.java',src/'com/hbm/blocks/IProxyController.java',src/'com/hbm/main/ThermalFurnaceModelHandler.java']
production += sorted((src/'com/hbm/render/block/nativeblock').glob('*.java'))
production += [src/('com/hbm/render/mesh/'+n+'.java') for n in ['ObjMesh','ObjMeshParser']]
with tempfile.TemporaryDirectory(prefix='thermal-block-native-') as tmp:
 out=Path(tmp); files=[]
 for rel,content in collaborators.items():
  assert rel.startswith('com/hbm/'), 'Only explicit NTM collaborators permitted'
  target=out/rel; target.parent.mkdir(parents=True,exist_ok=True); target.write_text(content); files.append(str(target))
 for source in production:
  print('PRODUCTION',source.relative_to(src),hashlib.sha256(source.read_bytes()).hexdigest(),flush=True);files.append(str(source))
 for stem in ['ThermalBlockNativeTest','ThermalBlockModelNativeTest']:
  f=out/(stem+'.java');f.write_text((tests/('thermal-block-'+('lifecycle' if stem=='ThermalBlockNativeTest' else 'models')+'-test.java.in')).read_text());files.append(str(f))
 subprocess.run([str(a.java_home/'bin'/('javac'+exe)),'-encoding','UTF-8','-source','8','-target','8','-cp',a.classpath,'-d',tmp]+files,check=True)
 resources=root/'src/main/resources'; jar=out/'thermal-resources.jar'
 with zipfile.ZipFile(jar,'w') as z:
  for path in (resources/'assets/hbm').rglob('*'):
   if path.is_file() and (path.suffix=='.png' and path.parent.name=='blocks' or path.name=='difurnace_extension.obj' or path.suffix=='.json' and path.parent.name=='blockstates' and any(i in path.name for i in ids)):
    z.write(path,path.relative_to(resources).as_posix())
 for main in ['ThermalBlockNativeTest','ThermalBlockModelNativeTest']:
  subprocess.run([str(a.java_home/'bin'/('java'+exe)),'-Djava.awt.headless=true','-cp',os.pathsep.join([tmp,a.classpath]),main,str(resources),str(jar)],cwd=tmp,check=True)
