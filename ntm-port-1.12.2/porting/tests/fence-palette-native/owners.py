#!/usr/bin/env python3
import re,importlib.util
from pathlib import Path
H=Path(__file__).resolve().parent;M=H.parents[2];SRC=M/'src/main/java'
spec=importlib.util.spec_from_file_location('ore',H.parent/'ore-callback-native/run.py');ore=importlib.util.module_from_spec(spec);spec.loader.exec_module(ore);extract=ore.extract

def generate(t):
 files=[]
 def put(path,text):
  p=t/path;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(text);files.append(p)
 put('com/hbm/blocks/ModBlocks.java','package com.hbm.blocks;public class ModBlocks {public static net.minecraft.block.Block fence_metal,fence_gate;}')
 put('com/hbm/main/MainRegistry.java','package com.hbm.main;public class MainRegistry {public static final net.minecraft.creativetab.CreativeTabs machineTab=net.minecraft.creativetab.CreativeTabs.REDSTONE;}')
 put('com/hbm/util/i18n/I18nUtil.java','package com.hbm.util.i18n;public class I18nUtil {public static String resolveKey(String key,Object...args){return key;}public static String[] resolveKeyArray(String key,Object...args){return new String[]{key};}}')
 put('com/hbm/util/CompatExternal.java','package com.hbm.util;public class CompatExternal {public static net.minecraft.tileentity.TileEntity getCoreFromPos(net.minecraft.world.World w,int x,int y,int z){return w.getTileEntity(new net.minecraft.util.math.BlockPos(x,y,z));}}')
 ctor=re.findall(r'^\s*fence_metal = .*;$',(SRC/'com/hbm/blocks/ModBlocks.java').read_text(),re.M);assert len(ctor)==1
 put('FenceConstruction.java','import com.hbm.blocks.ModBlocks;import com.hbm.blocks.generic.BlockMetalFence;import net.minecraft.block.material.Material;import com.hbm.main.MainRegistry;public class FenceConstruction {public static void initialize(){'+ctor[0].strip().replace('fence_metal =','ModBlocks.fence_metal =')+'}}')
 production=['com/hbm/blocks/generic/BlockMetalFence.java','com/hbm/blocks/IBlockMulti.java','com/hbm/items/block/ItemBlockBase.java','com/hbm/blocks/ITooltipProvider.java','com/hbm/blocks/IPersistentInfoProvider.java','com/hbm/tileentity/IPersistentNBT.java']
 return files+[SRC/p for p in production]+[H/'FenceNativeTest.java']
