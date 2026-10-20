#!/usr/bin/env python3
"""Bounded pristine-source preservation guard, not a substitute for native execution."""
import argparse, hashlib, json, re
from pathlib import Path
from side_rotation_fixtures import pristine_root
p=argparse.ArgumentParser();p.add_argument('--original',type=Path,default=None);a=p.parse_args();a.original=pristine_root(a.original)
root=Path(__file__).resolve().parents[2]
names=['Jigsaw','Tandem','Logic','Loot']
def methods(s):
 result={}
 for m in re.finditer(r'(?:public|protected|private)\s+(?:static\s+)?[\w<>\[\].?]+\s+(\w+)\s*\([^;{}]*\)\s*(?:throws IOException\s*)?\{',s):
  start=m.end();i=start;depth=1
  while depth:
   depth+=(s[i]=='{')-(s[i]=='}');i+=1
  result['update' if m.group(1)=='updateEntity' else m.group(1)]=s[start:i-1]
 return result

def normalized(s):
 s=re.sub(r'/\*.*?\*/|//[^\n]*','',s,flags=re.S)
 s=s.replace('worldObj','world').replace('fontRendererObj','fontRenderer').replace('getUnlocalizedName','getTranslationKey').replace('updateEntity','update')
 s=s.replace('Blocks.air','Blocks.AIR').replace('EnumChatFormatting','TextFormatting')
 s=re.sub(r'GameRegistry.findUniqueIdentifierFor\((\w+(?:\.\w+)*)\)',r'\1.getRegistryName()',s)
 s=s.replace('MathHelper.clamp_int','MathHelper.clamp').replace('MathHelper.wrapAngleTo180_float','MathHelper.wrapDegrees')
 s=s.replace('jigsaw.getPos().getX()','jigsaw.xCoord').replace('jigsaw.getPos().getY()','jigsaw.yCoord').replace('jigsaw.getPos().getZ()','jigsaw.zCoord')
 s=re.sub(r'new GuiTextField\(\d+, fontRenderer,','new GuiTextField(fontRenderer,',s)
 s=s.replace('jointToggle.drawButton(mc, mouseX, mouseY, partialTicks)','jointToggle.drawButton(mc, mouseX, mouseY)')
 s=s.replace('getMetaFromState(world.getBlockState(new BlockPos(x, y, z)))','world.getBlockMetadata(x, y, z)')
 s=s.replace('world.getTileEntity(new BlockPos(x, y, z))','world.getTileEntity(x, y, z)')
 s=s.replace('this.pos.getX()', 'xCoord').replace('this.pos.getY()', 'yCoord').replace('this.pos.getZ()', 'zCoord')
 s=s.replace('this.world', 'world')
 s=s.replace('world.getBlockState(this.pos).getBlock()', 'world.getBlock(xCoord, yCoord, zCoord)')
 s=s.replace('world.getTileEntity(this.pos)', 'world.getTileEntity(xCoord, yCoord, zCoord)')
 s=s.replace('world.setTileEntity(this.pos, te)', 'world.setTileEntity(xCoord, yCoord, zCoord, te)')
 s=s.replace('world.setBlockState(this.pos, replaceBlock.getStateFromMeta(replaceMeta), 2)', 'world.setBlock(xCoord, yCoord, zCoord, replaceBlock, replaceMeta, 2)')
 s=s.replace('world.setBlockState(this.pos, ModBlocks.logic_block.getDefaultState())', 'world.setBlock(xCoord, yCoord, zCoord, ModBlocks.logic_block)')
 s=s.replace('ModBlocks.wand_logic.createTileEntity(world, ModBlocks.wand_logic.getStateFromMeta(0))', 'ModBlocks.wand_logic.createTileEntity(world, 0)')
 s=s.replace('replaceBlock.createTileEntity(world, replaceBlock.getStateFromMeta(replaceMeta))', 'replaceBlock.createTileEntity(world, replaceMeta)')
 s=s.replace('replaceBlock.onBlockPlacedBy(world, this.pos, world.getBlockState(this.pos), fakePlayer, fakeStack)', 'replaceBlock.onBlockPlacedBy(world, xCoord, yCoord, zCoord, fakePlayer, fakeStack)')
 s=s.replace('return nbt;','')
 return re.sub(r'\s+','',s)
checked=0
for name in names:
 rel='src/main/java/com/hbm/blocks/generic/BlockWand'+name+'.java';old=(a.original/rel).read_text();new=(root/rel).read_text();before=methods(old);after=methods(new)
 for method in ['update','replace','tryGenerate','getRotationFromSide','transformMeta','serialize','deserialize','writeToNBT','readFromNBT','hasPermission','receiveControl','onScrew','transformTE','getSettings','pasteSettings','getPoolNames','directionOffsetToCoordBase','initGui','drawScreen','onGuiClosed','keyTyped','mouseClicked','doesGuiPauseGame','addInformation']:
  if method in before:
   assert method in after,(name,'missing original method',method)
   assert normalized(before[method])==normalized(after[method]),(name,'non-native behavior drift',method)
   checked+=1
 oldstrings=re.findall(r'"(?:\\.|[^"\\])*"',old);newstrings=re.findall(r'"(?:\\.|[^"\\])*"',new)
 oldstrings=[s for s in oldstrings if not s.startswith('\":wand_')];newstrings=[s for s in newstrings if not s.startswith('\"blocks/wand_')]
 assert oldstrings==newstrings,(name,'original user-facing text/NBT keys/string controls differ')
 for forbidden in ['IIcon','registerBlockIcons','getIcon(', 'setBlockMetadataWithNotify','getHeldItem()','stackTagCompound','worldObj','xCoord','yCoord','zCoord','fontRendererObj','Material.iron','Blocks.air','getUnlocalizedName','findUniqueIdentifierFor']:
  assert forbidden not in new,(name,'legacy API residue',forbidden)
 assert new.count('boolean shouldRefresh(')==1 and 'return oldState.getBlock() != newState.getBlock();' in new
 print('PRESERVED',name,'source',hashlib.sha256(old.encode()).hexdigest(),'native',hashlib.sha256(new.encode()).hexdigest())
print('PASS editor pristine preservation:',checked,'complete behavior/GUI methods plus all original string literals and finite native residue gate.')
