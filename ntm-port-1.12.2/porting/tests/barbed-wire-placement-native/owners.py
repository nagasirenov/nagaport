#!/usr/bin/env python3
import re,importlib.util
from pathlib import Path
H=Path(__file__).resolve().parent;M=H.parents[2];SRC=M/'src/main/java'
spec=importlib.util.spec_from_file_location('ore',H.parent/'ore-callback-native/run.py');ore=importlib.util.module_from_spec(spec);spec.loader.exec_module(ore);extract=ore.extract

def generate(t):
 files=[]
 def put(path,text):
  p=t/path;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(text);files.append(p)
 put('com/hbm/blocks/ModBlocks.java','package com.hbm.blocks;public class ModBlocks {public static net.minecraft.block.Block barbed_wire,barbed_wire_fire,barbed_wire_poison,barbed_wire_acid,barbed_wire_wither,barbed_wire_ultradeath;}')
 put('com/hbm/main/MainRegistry.java','package com.hbm.main;public class MainRegistry {public static final net.minecraft.creativetab.CreativeTabs blockTab=net.minecraft.creativetab.CreativeTabs.BUILDING_BLOCKS;}')
 put('com/hbm/util/ArmorUtil.java','package com.hbm.util;public class ArmorUtil {public static void damageSuit(net.minecraft.entity.player.EntityPlayer p,int slot,int damage){throw new AssertionError("Unexercised acid variant");}}')
 put('com/hbm/lib/ModDamageSource.java','package com.hbm.lib;public class ModDamageSource {public static final net.minecraft.util.DamageSource pc=new net.minecraft.util.DamageSource("pc");}')
 put('com/hbm/potion/HbmPotion.java','package com.hbm.potion;public class HbmPotion {public static net.minecraft.potion.Potion radiation;}')
 factory=extract((SRC/'com/hbm/blocks/ModBlocks.java').read_text(),'private static Block createBarbedWire(')
 put('BarbedConstruction.java','import com.hbm.blocks.ModBlocks;import com.hbm.blocks.generic.BarbedWire;import net.minecraft.block.Block;import net.minecraft.block.material.Material;import com.hbm.main.MainRegistry;import net.minecraft.util.ResourceLocation;import com.hbm.lib.RefStrings;public class BarbedConstruction {'+factory+'public static void initialize(){ModBlocks.barbed_wire=createBarbedWire("barbed_wire");}}')
 production=['com/hbm/blocks/generic/BarbedWire.java','com/hbm/world/gen/nbt/INBTBlockTransformable.java','com/hbm/render/icon/IIcon.java','com/hbm/render/icon/IIconRegister.java','com/hbm/lib/RefStrings.java']
 return files+[SRC/p for p in production]+[H/'BarbedNativeTest.java']
