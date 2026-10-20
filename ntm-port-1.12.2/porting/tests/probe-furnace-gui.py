#!/usr/bin/env python3
"""Compile COMPLETE GUIHandler/GuiInfoContainer/GUIMachineElectricFurnace and SlotPattern.
Native Minecraft/Forge/NEI APIs; only explicitly listed NTM collaborators are controlled.
No GL context. Runtime exercises non-GL complete production methods and a verbatim
furnace background tail, beginning after GL color/texture/background binding.
"""
import argparse, os, subprocess, tempfile, zipfile, hashlib, json
from pathlib import Path
p=argparse.ArgumentParser();p.add_argument('--java-home',type=Path,required=True);p.add_argument('--classpath',required=True);a=p.parse_args()
root=Path(__file__).resolve().parents[2];src=root/'src/main/java';exe='.exe' if os.name=='nt' else ''
if not (a.java_home/'bin'/('javac'+exe)).is_file() and (a.java_home.parent/'bin'/('javac'+exe)).is_file():
 a.java_home=a.java_home.parent
collaborators={
'com/hbm/items/ModItems.java':'package com.hbm.items; public class ModItems { public static final net.minecraft.item.Item template_folder=new net.minecraft.item.Item(); }',
'com/hbm/util/BobMathUtil.java':'package com.hbm.util; public class BobMathUtil { public static String getShortNumber(long n){return "short:"+n;} }',
'com/hbm/util/i18n/I18nUtil.java':'package com.hbm.util.i18n; public class I18nUtil { public static String resolveKey(String s,Object...args){return s+java.util.Arrays.toString(args);} }',
'com/hbm/inventory/container/ContainerBase.java':'''package com.hbm.inventory.container; public class ContainerBase extends net.minecraft.inventory.Container { public net.minecraft.inventory.IInventory tile; public boolean canInteractWith(net.minecraft.entity.player.EntityPlayer p){return true;} public void slot(net.minecraft.inventory.Slot s){addSlotToContainer(s);} }''',
'com/hbm/inventory/container/ContainerElectricFurnace.java':'''package com.hbm.inventory.container; public class ContainerElectricFurnace extends ContainerBase { public ContainerElectricFurnace(net.minecraft.entity.player.InventoryPlayer p,com.hbm.tileentity.machine.TileEntityMachineElectricFurnace t){tile=t;} }''',
'com/hbm/packet/toserver/NBTControlPacket.java':'''package com.hbm.packet.toserver; public class NBTControlPacket { public final net.minecraft.nbt.NBTTagCompound tag; public final int x,y,z; public NBTControlPacket(net.minecraft.nbt.NBTTagCompound n,int x,int y,int z){tag=n;this.x=x;this.y=y;this.z=z;} }''',
'com/hbm/packet/PacketDispatcher.java':'''package com.hbm.packet; public class PacketDispatcher { public static final RecordingWrapper wrapper=new RecordingWrapper(); public static class RecordingWrapper { public com.hbm.packet.toserver.NBTControlPacket packet; public int sends; public void sendToServer(com.hbm.packet.toserver.NBTControlPacket p){packet=p;sends++;} } }''',
'com/hbm/blocks/ModBlocks.java':'package com.hbm.blocks; public class ModBlocks { public static final net.minecraft.block.Block machine_electric_furnace_on=net.minecraft.init.Blocks.LIT_FURNACE; }',
'com/hbm/tileentity/machine/TileEntityMachineElectricFurnace.java':'''package com.hbm.tileentity.machine;
public class TileEntityMachineElectricFurnace extends net.minecraft.tileentity.TileEntity implements net.minecraft.inventory.IInventory {
 public long power,maxPower=100; public int progress,lastProgressScale; public long lastPowerScale; public boolean powered; public String name="container.test"; public boolean custom;
 public boolean hasPower(){return powered;} public long getPowerScaled(long s){lastPowerScale=s;return power*s/maxPower;} public int getProgressScaled(int s){lastProgressScale=s;return progress;}
 public String getName(){return name;} public boolean hasCustomName(){return custom;} public net.minecraft.util.text.ITextComponent getDisplayName(){return new net.minecraft.util.text.TextComponentString(name);}
 public int getSizeInventory(){return 4;} public boolean isEmpty(){return true;} public net.minecraft.item.ItemStack getStackInSlot(int i){return net.minecraft.item.ItemStack.EMPTY;} public net.minecraft.item.ItemStack decrStackSize(int i,int n){return net.minecraft.item.ItemStack.EMPTY;} public net.minecraft.item.ItemStack removeStackFromSlot(int i){return net.minecraft.item.ItemStack.EMPTY;} public void setInventorySlotContents(int i,net.minecraft.item.ItemStack s){} public int getInventoryStackLimit(){return 64;} public boolean isUsableByPlayer(net.minecraft.entity.player.EntityPlayer p){return true;} public void openInventory(net.minecraft.entity.player.EntityPlayer p){} public void closeInventory(net.minecraft.entity.player.EntityPlayer p){} public boolean isItemValidForSlot(int i,net.minecraft.item.ItemStack s){return true;} public int getField(int i){return 0;} public void setField(int i,int v){} public int getFieldCount(){return 0;} public void clear(){}
}'''
}
production=['com/hbm/handler/GUIHandler.java','com/hbm/inventory/gui/GuiInfoContainer.java','com/hbm/inventory/gui/GUIMachineElectricFurnace.java','com/hbm/inventory/SlotPattern.java','com/hbm/tileentity/IGUIProvider.java','com/hbm/tileentity/IUpgradeInfoProvider.java','com/hbm/lib/RefStrings.java','com/hbm/items/machine/ItemMachineUpgrade.java']
# UpgradeType is the complete parallel-owned production class, never a reordered substitute.
import re
upgrade_fields = sorted(set(re.findall(r'ModItems\.(\w+)', (src/'com/hbm/items/machine/ItemMachineUpgrade.java').read_text(encoding='utf-8'))))
collaborators['com/hbm/items/ModItems.java'] = 'package com.hbm.items; public class ModItems {' + ''.join(' public static final net.minecraft.item.Item '+name+'=new net.minecraft.item.Item();' for name in ['template_folder']+upgrade_fields) + ' }'
furnace=(src/production[2]).read_text(encoding='utf-8');tail=furnace.split('\t\t//failsafe TE clone',1)[1].rsplit('\n\t}',1)[0]
# Only the pre-GL binding prefix is excluded. All refresh/power/flame/progress/panel code is verbatim.
template=(root/'porting/tests/FurnaceGuiNativeTest.java').read_text(encoding='utf-8').replace('// @FURNACE_BACKGROUND_TAIL@',tail)
with tempfile.TemporaryDirectory(prefix='hbm-furnace-gui-') as tmp:
 out=Path(tmp);files=[]
 for relative,text in collaborators.items():
  target=out/relative;target.parent.mkdir(parents=True,exist_ok=True);target.write_text(text,encoding='utf-8');files.append(str(target))
 test=out/'FurnaceGuiNativeTest.java';test.write_text(template,encoding='utf-8');files.append(str(test))
 for relative in production:
  assert relative not in collaborators, ('production class replaced by collaborator', relative)
  raw_source=(src/relative).read_bytes();target=out/relative;target.parent.mkdir(parents=True,exist_ok=True);target.write_bytes(raw_source);files.append(str(target))
  assert target.read_bytes()==raw_source
  print('COMPLETE INPUT',hashlib.sha256(raw_source).hexdigest(),relative,flush=True)
 resource=root/'src/main/resources/assets/hbm/textures/gui/guielectricfurnace.png';raw=resource.read_bytes()
 for name,entry in [('native.zip','assets/hbm/textures/gui/guielectricfurnace.png'),('legacy.zip','assets/hbm/textures/gui/GUIElectricFurnace.png')]:
  with zipfile.ZipFile(out/name,'w') as z:z.writestr(entry,raw)
 (out/'legacy/assets/hbm/textures/gui').mkdir(parents=True);(out/'legacy/assets/hbm/textures/gui/GUIElectricFurnace.png').write_bytes(raw)
 print('Complete production classes:',', '.join(production),flush=True)
 print('Controlled NTM collaborators:',', '.join(collaborators),flush=True)
 subprocess.run([str(a.java_home/'bin'/('javac'+exe)),'-encoding','UTF-8','-cp',a.classpath,'-d',tmp]+files,check=True)
 subprocess.run([str(a.java_home/'bin'/('java'+exe)),'-Djava.awt.headless=true','-cp',os.pathsep.join([tmp,a.classpath]),'FurnaceGuiNativeTest',str(root/'src/main/resources'),str(out),hashlib.sha256(raw).hexdigest()],cwd=tmp,check=True)
