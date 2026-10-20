#!/usr/bin/env python3
"""Whole Satellite/core classes on real MC/JEI. NTM registry collaborators
are temporary fixtures; ItemStackUtil uses its exact production tooltip method.
This does not establish full-mod compilation or rendered/gameplay behavior.
"""
import argparse, os, subprocess, tempfile
from pathlib import Path
p=argparse.ArgumentParser()
p.add_argument('--java-home',type=Path,required=True)
p.add_argument('--classpath',required=True)
a=p.parse_args()
root=Path(__file__).resolve().parents[2]
src=root/'src/main/java/com/hbm'
text=(src/'util/ItemStackUtil.java').read_text()
start=text.index('public static ItemStack addTooltipToStack(')
brace=text.index('{',start); depth=1; end=brace+1
while depth:
    depth += (text[end]=='{') - (text[end]=='}'); end+=1
method=text[start:end]
fixtures={
'items/ModItems.java': 'package com.hbm.items; import net.minecraft.item.Item; import net.minecraft.init.Items; public class ModItems { public static final Item sat_miner=Items.MINECART, sat_lunar_miner=Items.CHEST_MINECART; }',
'blocks/ModBlocks.java': 'package com.hbm.blocks; import net.minecraft.block.Block; import net.minecraft.init.Blocks; public class ModBlocks { public static final Block sat_dock=Blocks.IRON_BLOCK; }',
'itempool/ItemPoolsSatellite.java': 'package com.hbm.itempool; public class ItemPoolsSatellite { public static final String POOL_SAT_MINER="miner", POOL_SAT_LUNAR="lunar"; }',
'itempool/ItemPool.java': 'package com.hbm.itempool; import java.util.*; import com.hbm.lib.WeightedRandomChestContent; public class ItemPool { public static final Map<String,WeightedRandomChestContent[]> pools=new HashMap<>(); public static WeightedRandomChestContent[] getPool(String name) { return pools.get(name); } }',
'saveddata/satellites/SatelliteMiner.java': 'package com.hbm.saveddata.satellites; import java.util.*; import net.minecraft.item.Item; public class SatelliteMiner { public static final Map<Item,String> cargo=new HashMap<>(); public static String getCargoForItem(Item item) { return cargo.get(item); } }',
'util/ItemStackUtil.java': 'package com.hbm.util; import net.minecraft.item.ItemStack; import net.minecraft.nbt.*; import net.minecraft.util.text.TextFormatting; public class ItemStackUtil {'+method+'}'
}
files=[src/'handler/jei'/f'{n}.java' for n in ('MachineRecipeCategory','MachineRecipeView','NativeRecipeLookup','RecipeStackMatcher')]
files += [src/'handler/nei/SatelliteHandler.java',src/'lib/WeightedRandomChestContent.java',src/'lib/LegacyWeightedLoot.java',src/'lib/RefStrings.java',src/'interfaces/NotableComments.java',root/'porting/tests/JeiCoreNativeTest.java',root/'porting/tests/jei-satellite-native-test.java']
with tempfile.TemporaryDirectory(prefix='jei-satellite-native-') as tmp:
    tmp=Path(tmp); out=tmp/'classes'; out.mkdir()
    for name,content in fixtures.items():
        f=tmp/'fixtures/com/hbm'/name; f.parent.mkdir(parents=True,exist_ok=True); f.write_text(content); files.append(f)
    subprocess.run([str(a.java_home/'bin/javac'),'-source','8','-target','8','-proc:none','-implicit:none','-encoding','UTF-8','-cp',a.classpath,'-d',str(out)]+list(map(str,files)),check=True)
    subprocess.run([str(a.java_home/'bin/java'),'-cp',str(out)+os.pathsep+a.classpath,'JeiSatelliteNativeTest'],cwd=tmp,check=True)
