#!/usr/bin/env python3
"""Complete electric furnace block on native Minecraft/Forge APIs.
NTM registry identities, GUI owner and inventory tile are controlled collaborators.
World.setBlockState, Chunk lifecycle, native tile removal and item entities are real.
This does not establish production GUI/network/server/save/GL acceptance.
"""
import argparse, hashlib, os, re, subprocess, tempfile
from pathlib import Path
p=argparse.ArgumentParser();p.add_argument('--java-home',type=Path,required=True);p.add_argument('--classpath',required=True);a=p.parse_args()
root=Path(__file__).resolve().parents[2];src=root/'src/main/java';exe='.exe' if os.name=='nt' else ''
if not (a.java_home/'bin'/('javac'+exe)).is_file() and (a.java_home.parent/'bin'/('javac'+exe)).is_file():a.java_home=a.java_home.parent
collaborators={
'com/hbm/blocks/ModBlocks.java':'''package com.hbm.blocks; public final class ModBlocks { public static net.minecraft.block.Block machine_electric_furnace_off, machine_electric_furnace_on; }''',
'com/hbm/main/MainRegistry.java':'''package com.hbm.main; public final class MainRegistry { public static final Object instance = new Object(); }''',
'com/hbm/tileentity/machine/TileEntityMachineElectricFurnace.java':'''package com.hbm.tileentity.machine;
public class TileEntityMachineElectricFurnace extends net.minecraft.tileentity.TileEntity {
 public final net.minecraft.item.ItemStack[] slots = new net.minecraft.item.ItemStack[4];
 public String customName; public int validates, invalidates;
 public int getSizeInventory() { return slots.length; }
 public net.minecraft.item.ItemStack getStackInSlot(int slot) { return slots[slot] == null ? net.minecraft.item.ItemStack.EMPTY : slots[slot]; }
 public void setCustomName(String value) { customName=value; }
 // @FURNACE_REFRESH@
 @Override public void validate() { super.validate(); validates++; }
 @Override public void invalidate() { super.invalidate(); invalidates++; }
}'''}
# Use the exact production refresh method, not a fabricated Minecraft/Forge behavior.
tile_source=(src/'com/hbm/tileentity/machine/TileEntityMachineElectricFurnace.java').read_text(encoding='utf-8')
match=re.search(r'public boolean shouldRefresh\([^)]*\)\s*\{',tile_source);assert match,'furnace-only metadata lifecycle closure required'
end=match.end();depth=1
while depth:
 depth+=(tile_source[end]=='{')-(tile_source[end]=='}');end+=1
refresh=tile_source[match.start():end]
key='com/hbm/tileentity/machine/TileEntityMachineElectricFurnace.java'
collaborators[key]=collaborators[key].replace('package com.hbm.tileentity.machine;','package com.hbm.tileentity.machine; import net.minecraft.world.World;').replace('// @FURNACE_REFRESH@','@Override '+refresh)
print('EXTRACTED production furnace shouldRefresh SHA256',hashlib.sha256(refresh.encode('utf-8')).hexdigest(),flush=True)
production=['com/hbm/blocks/machine/MachineElectricFurnace.java','com/hbm/lib/RefStrings.java']
with tempfile.TemporaryDirectory(prefix='hbm-furnace-block-') as tmp:
 out=Path(tmp);files=[]
 for relative,text in collaborators.items():
  f=out/relative;f.parent.mkdir(parents=True,exist_ok=True);f.write_text(text,encoding='utf-8');files.append(str(f))
 for f in production:
  path=src/f;print('PRODUCTION',f,hashlib.sha256(path.read_bytes()).hexdigest(),flush=True);files.append(str(path))
 files.append(str(root/'porting/tests/FurnaceBlockNativeTest.java'))
 subprocess.run([str(a.java_home/'bin'/('javac'+exe)),'-encoding','UTF-8','-cp',a.classpath,'-d',tmp]+files,check=True)
 subprocess.run([str(a.java_home/'bin'/('java'+exe)),'-cp',os.pathsep.join([tmp,a.classpath]),'FurnaceBlockNativeTest'],cwd=tmp,check=True)
