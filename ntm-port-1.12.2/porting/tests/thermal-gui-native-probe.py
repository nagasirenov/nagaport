#!/usr/bin/env python3
"""Complete thermal GUI/native base compilation with explicit NTM collaborators.
Native Minecraft/Forge/NEI/GL classes are never substituted. No GL client starts.
"""
import argparse, hashlib, json, os, re, subprocess, tempfile, zipfile
from pathlib import Path
p = argparse.ArgumentParser(); p.add_argument('--java-home', type=Path, required=True); p.add_argument('--classpath', required=True); a = p.parse_args()
root = Path(__file__).resolve().parents[2]; src = root / 'src/main/java'; exe = '.exe' if os.name == 'nt' else ''
if not (a.java_home / 'bin' / ('javac' + exe)).is_file() and (a.java_home.parent / 'bin' / ('javac' + exe)).is_file():
    a.java_home = a.java_home.parent
production = ['com/hbm/inventory/gui/GUIFurnace%s.java' % k for k in ('Brick', 'Iron', 'Steel')] + ['com/hbm/inventory/gui/GuiInfoContainer.java', 'com/hbm/inventory/SlotPattern.java', 'com/hbm/tileentity/IUpgradeInfoProvider.java', 'com/hbm/lib/RefStrings.java', 'com/hbm/items/machine/ItemMachineUpgrade.java']
collaborators = {
'com/hbm/util/BobMathUtil.java': 'package com.hbm.util; public class BobMathUtil { public static String getShortNumber(long n){return "short:"+n;} }',
'com/hbm/util/i18n/I18nUtil.java': 'package com.hbm.util.i18n; public class I18nUtil { public static String resolveKey(String s,Object...args){return s+java.util.Arrays.toString(args);} }',
'com/hbm/inventory/container/ContainerBase.java': 'package com.hbm.inventory.container; public class ContainerBase extends net.minecraft.inventory.Container { public net.minecraft.inventory.IInventory tile; public boolean canInteractWith(net.minecraft.entity.player.EntityPlayer p){return true;} }',
'com/hbm/packet/toserver/NBTControlPacket.java': 'package com.hbm.packet.toserver; public class NBTControlPacket { public NBTControlPacket(net.minecraft.nbt.NBTTagCompound n,int x,int y,int z){} }',
'com/hbm/packet/PacketDispatcher.java': 'package com.hbm.packet; public class PacketDispatcher { public static final Wrapper wrapper=new Wrapper(); public static class Wrapper { public void sendToServer(com.hbm.packet.toserver.NBTControlPacket p){} } }',
'com/hbm/module/ModuleBurnTime.java': 'package com.hbm.module; public class ModuleBurnTime { public java.util.List<String> lines=new java.util.ArrayList<>(); public int queries; public java.util.List<String> getTimeDesc(){queries++;return lines;} }',
}
upgrade_fields = sorted(set(re.findall(r'ModItems\.(\w+)', (src / 'com/hbm/items/machine/ItemMachineUpgrade.java').read_text())))
collaborators['com/hbm/items/ModItems.java'] = 'package com.hbm.items; public class ModItems {' + ''.join(' public static final net.minecraft.item.Item ' + name + '=new net.minecraft.item.Item();' for name in ['template_folder'] + upgrade_fields) + ' }'
inventory = '''
 public final net.minecraft.inventory.InventoryBasic inventory=new net.minecraft.inventory.InventoryBasic("fixture",false,6);
 public String name="container.fixture"; public boolean custom; public String getName(){return name;} public boolean hasCustomName(){return custom;} public net.minecraft.util.text.ITextComponent getDisplayName(){return new net.minecraft.util.text.TextComponentString(name);}
 public int getSizeInventory(){return inventory.getSizeInventory();} public boolean isEmpty(){return inventory.isEmpty();} public net.minecraft.item.ItemStack getStackInSlot(int i){return inventory.getStackInSlot(i);} public net.minecraft.item.ItemStack decrStackSize(int i,int n){return inventory.decrStackSize(i,n);} public net.minecraft.item.ItemStack removeStackFromSlot(int i){return inventory.removeStackFromSlot(i);} public void setInventorySlotContents(int i,net.minecraft.item.ItemStack s){inventory.setInventorySlotContents(i,s);} public int getInventoryStackLimit(){return inventory.getInventoryStackLimit();} public boolean isUsableByPlayer(net.minecraft.entity.player.EntityPlayer p){return true;} public void openInventory(net.minecraft.entity.player.EntityPlayer p){} public void closeInventory(net.minecraft.entity.player.EntityPlayer p){} public boolean isItemValidForSlot(int i,net.minecraft.item.ItemStack s){return true;} public int getField(int i){return 0;} public void setField(int i,int v){} public int getFieldCount(){return 0;} public void clear(){inventory.clear();}
'''
fields = {
'Brick': 'public int burnTime,maxBurnTime,progress;',
'Iron': 'public int burnTime,maxBurnTime,progress,processingTime; public boolean smelt; public com.hbm.module.ModuleBurnTime burnModule=new com.hbm.module.ModuleBurnTime(); public boolean canSmelt(){return smelt;}',
'Steel': 'public int[] progress=new int[3],bonus=new int[3]; public static final int processTime=40_000,maxHeat=100_000; public int heat; public boolean wasOn;',
}
for kind in fields:
    name = 'TileEntityFurnace' + kind
    collaborators['com/hbm/tileentity/machine/' + name + '.java'] = 'package com.hbm.tileentity.machine; public class ' + name + ' extends net.minecraft.tileentity.TileEntity implements net.minecraft.inventory.IInventory {' + fields[kind] + inventory + '}'
    slots = 'addSlotToContainer(new net.minecraft.inventory.Slot(t,0,53,17)); addSlotToContainer(new net.minecraft.inventory.Slot(t,1,53,53)); addSlotToContainer(new net.minecraft.inventory.Slot(t,2,71,53));' if kind == 'Iron' else ''
    collaborators['com/hbm/inventory/container/ContainerFurnace' + kind + '.java'] = 'package com.hbm.inventory.container; public class ContainerFurnace' + kind + ' extends ContainerBase { public final net.minecraft.entity.player.InventoryPlayer player; public ContainerFurnace' + kind + '(net.minecraft.entity.player.InventoryPlayer p,com.hbm.tileentity.machine.' + name + ' t){player=p;tile=t;' + slots + '} }'
# Every fragment is a verbatim production method suffix, never a translated model.
template = (root / 'porting/tests/thermal-gui-native-test.java.in').read_text()
for kind in fields:
    source = (src / ('com/hbm/inventory/gui/GUIFurnace' + kind + '.java')).read_text()
    body = source.split('protected void drawGuiContainerBackgroundLayer(', 1)[1].split('{', 1)[1].rsplit('\n\t}', 1)[0]
    match = re.search(r'(?:this\.)?drawTexturedModalRect\(guiLeft, guiTop, 0, 0, (?:this\.)?xSize, (?:this\.)?ySize\);', body)
    assert match
    tail = body[match.end():]
    template = template.replace('// @' + kind.upper() + '_BACKGROUND_TAIL@', tail)
    if kind != 'Brick':
        overlay = source.split('public void drawScreen(int x, int y, float interp) {', 1)[1].split('\n\t}', 1)[0]
        prefix = '\n\t\tsuper.drawScreen(x, y, interp);'
        assert overlay.startswith(prefix)
        template = template.replace('// @' + kind.upper() + '_OVERLAY_TAIL@', overlay[len(prefix):])
assert not re.search(r'// @\w+_TAIL@', template)
manifest = json.loads((root / 'porting/tests/thermal-gui-source-manifest.json').read_text())
with tempfile.TemporaryDirectory(prefix='hbm-thermal-gui-') as tmp:
    out = Path(tmp); files = []
    for relative, text in collaborators.items():
        assert relative.startswith('com/hbm/'), 'No substituted native or third-party APIs'
        target = out / relative; target.parent.mkdir(parents=True, exist_ok=True); target.write_text(text); files.append(str(target))
    for relative in production:
        assert relative not in collaborators
        raw = (src / relative).read_bytes(); target = out / relative; target.parent.mkdir(parents=True, exist_ok=True); target.write_bytes(raw); files.append(str(target))
        assert raw == target.read_bytes()
        print('COMPLETE INPUT', hashlib.sha256(raw).hexdigest(), relative, flush=True)
    test = out / 'ThermalGuiNativeTest.java'; test.write_text(template); files.append(str(test))
    with zipfile.ZipFile(out / 'thermal-gui-native.zip', 'w') as archive:
        for entry in manifest['textures']:
            raw = (root / entry['path']).read_bytes()
            assert hashlib.sha256(raw).hexdigest() == entry['sha256']
            archive.writestr(entry['path'].split('src/main/resources/', 1)[1], raw)
    print('Controlled NTM collaborators:', ', '.join(sorted(collaborators)), flush=True)
    subprocess.run([str(a.java_home / 'bin' / ('javac' + exe)), '-encoding', 'UTF-8', '-sourcepath', str(out), '-cp', a.classpath, '-d', str(out)] + files, check=True)
    subprocess.run([str(a.java_home / 'bin' / ('java' + exe)), '-Djava.awt.headless=true', '-cp', os.pathsep.join([str(out), a.classpath]), 'ThermalGuiNativeTest', str(root / 'src/main/resources'), str(out / 'thermal-gui-native.zip')] + [e['sha256'] for e in manifest['textures']], cwd=out, check=True)
