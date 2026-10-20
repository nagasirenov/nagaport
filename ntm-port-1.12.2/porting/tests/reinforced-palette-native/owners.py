from pathlib import Path
import importlib.util,re
H=Path(__file__).resolve().parent;M=H.parents[2];SRC=M/'src/main/java'
spec=importlib.util.spec_from_file_location('ore',H.parent/'ore-callback-native/run.py');o=importlib.util.module_from_spec(spec);spec.loader.exec_module(o);extract=o.extract
NAMES=['reinforced_glass','reinforced_brick']
def generate(t):
 files=[]
 def put(name,source):
  p=t/name;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(source);files.append(p)
 put('com/hbm/main/MainRegistry.java','package com.hbm.main;public class MainRegistry {public static final net.minecraft.creativetab.CreativeTabs blockTab=net.minecraft.creativetab.CreativeTabs.BUILDING_BLOCKS;}')
 mod=(SRC/'com/hbm/blocks/ModBlocks.java').read_text();statements=[]
 for name in NAMES:
  found=re.findall(r'^\s*'+name+r' = .*;$',mod,re.M);assert len(found)==1;statements.append(found[0].strip())
 put('ConcreteConstructionSlice.java','import com.hbm.blocks.BlockBase;import com.hbm.blocks.generic.*;import com.hbm.lib.RefStrings;import com.hbm.main.MainRegistry;import net.minecraft.block.Block;import net.minecraft.block.material.Material;public class ConcreteConstructionSlice {public static Block reinforced_glass,reinforced_brick;public static void initialize(){'+'\n'.join(statements)+'reinforced_glass.setRegistryName("hbm",reinforced_glass.getTranslationKey());}public static Block[] blocks(){return new Block[]{reinforced_glass,reinforced_brick};}}')
 production=['com/hbm/blocks/BlockBase.java','com/hbm/blocks/generic/BlockGeneric.java','com/hbm/blocks/generic/BlockNTMGlass.java','com/hbm/blocks/generic/BlockNTMGlassCT.java','com/hbm/render/block/ct/CTContext.java','com/hbm/render/block/ct/CT.java','com/hbm/render/block/ct/IBlockCT.java','com/hbm/render/icon/IIcon.java','com/hbm/render/icon/IIconRegister.java','com/hbm/lib/RefStrings.java']
 return files+[SRC/f for f in production]+[H/'ReinforcedNativeTest.java']
