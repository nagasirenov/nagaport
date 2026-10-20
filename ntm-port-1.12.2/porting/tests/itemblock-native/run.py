#!/usr/bin/env python3
import pathlib,subprocess,json,os
import argparse,tempfile,re
p=argparse.ArgumentParser();p.add_argument('--module',type=pathlib.Path,required=True);p.add_argument('--java-home',type=pathlib.Path,required=True);p.add_argument('--classpath',required=True);p.add_argument('--source-root',type=pathlib.Path);p.add_argument('--classes-dir',type=pathlib.Path);p.add_argument('--compile-only',action='store_true');a=p.parse_args()
r=a.module.resolve();s=(a.source_root or r).resolve();test=pathlib.Path(__file__).resolve().parent
scratch=tempfile.TemporaryDirectory(prefix='native-itemblock-');out=a.classes_dir or pathlib.Path(scratch.name)/'classes';out.mkdir(exist_ok=True,parents=True);fixtures=pathlib.Path(scratch.name)/'fixtures';fixtures.mkdir()
cp=a.classpath;java=str(a.java_home/'bin')+os.sep
stubs={
'com/hbm/blocks/ModBlocks.java':'package com.hbm.blocks; public class ModBlocks { public static net.minecraft.block.Block fence_gate,meteor_battery,gravel_diamond,block_euphemium,block_euphemium_cluster,plasma; }',
'com/hbm/util/CompatExternal.java':'package com.hbm.util; public class CompatExternal { public static net.minecraft.tileentity.TileEntity getCoreFromPos(net.minecraft.world.World w,int x,int y,int z){ return w.getTileEntity(new net.minecraft.util.math.BlockPos(x,y,z)); } }',
'com/hbm/main/MainRegistry.java':'''package com.hbm.main; public class MainRegistry { public static final Proxy proxy=new Proxy(); public static class Proxy { public com.hbm.util.i18n.ITranslate getI18n(){return new com.hbm.util.i18n.I18nClient();} } }'''}
stubs.update({
'com/hbm/blocks/BlockRemap.java':'package com.hbm.blocks; public class BlockRemap extends net.minecraft.block.Block { public net.minecraft.block.Block remapBlock; public int remapMeta; public BlockRemap(net.minecraft.block.Block b,int m){super(net.minecraft.block.material.Material.TNT);remapBlock=b;remapMeta=m;} }',
'com/hbm/blocks/generic/RedBarrel.java':'package com.hbm.blocks.generic; public class RedBarrel extends net.minecraft.block.Block {public RedBarrel(){super(net.minecraft.block.material.Material.IRON);} }',
'com/hbm/blocks/generic/BlockOreFluid.java':'package com.hbm.blocks.generic; public class BlockOreFluid extends net.minecraft.block.Block {public BlockOreFluid(){super(net.minecraft.block.material.Material.ROCK);} }'
})
files=[]
for name,body in stubs.items():
 p=fixtures/name;p.parent.mkdir(exist_ok=True,parents=True);p.write_text(body);files.append(str(p))
production=['com/hbm/blocks/IBlockMulti.java','com/hbm/blocks/ITooltipProvider.java','com/hbm/blocks/IPersistentInfoProvider.java','com/hbm/blocks/generic/BlockScaffold.java','com/hbm/blocks/generic/BlockMetalFence.java','com/hbm/tileentity/IPersistentNBT.java','com/hbm/util/i18n/I18nUtil.java','com/hbm/util/i18n/I18nClient.java','com/hbm/util/i18n/ITranslate.java']
files += [str(r/'src/main/java'/x) for x in production]
files += [str(s/'src/main/java'/x) for x in ['com/hbm/items/block/ItemBlockBase.java','com/hbm/items/block/ItemBlockBlastInfo.java','com/hbm/items/block/ItemBlockLore.java','com/hbm/items/block/ItemBlockRemap.java','com/hbm/main/NativeItemBlockColorHandler.java']]
files += [str(test/'NativeItemBlockTest.java')]
# Preserve every literal/branch in the changed descendants against the pre-migration source.
for name in ['ItemBlockLore','ItemBlockRemap']:
 rel='src/main/java/com/hbm/items/block/'+name+'.java'
 original=(r.parent/'ntm-original-pristine'/rel).read_text()
 expected=original.replace('field_150939_a','block').replace('getUnlocalizedName()','getTranslationKey()').replace('stack.stackSize','stack.getCount()')
 for rarity in ['rare','epic','common']:expected=expected.replace('EnumRarity.'+rarity,'EnumRarity.'+rarity.upper())
 # Existing CP source has already modernized ChatFormatting imports.
 expected=expected.replace('net.minecraft.util.EnumChatFormatting','net.minecraft.util.text.TextFormatting').replace('EnumChatFormatting','TextFormatting')
 assert (s/rel).read_text()==expected, 'Unexpected descendant body change: '+name
 if name=='ItemBlockLore':
  gravel=original[original.index('if(this.field_150939_a == ModBlocks.gravel_diamond)'):original.index('public EnumRarity')]
  strings=re.findall(r'list.add\(("(?:[^"\\]|\\.)*")\);',gravel)
  oracle=fixtures/'LoreExpected.java';oracle.write_text('public class LoreExpected { static final java.util.List<String> GRAVEL=java.util.Arrays.asList('+','.join(strings)+'); }');files.append(str(oracle))
subprocess.run([java+'javac' ,'-source','8','-target','8','-cp',cp,'-d',str(out)]+files,check=True)
if not a.compile_only: subprocess.run([java+'java','-Djava.awt.headless=true','-cp',str(out)+os.pathsep+cp,'NativeItemBlockTest'],cwd=str(s),check=True)
