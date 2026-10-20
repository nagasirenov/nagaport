#!/usr/bin/env python3
"""Portable pristine-method and geometry/overlay arithmetic audit for the complete rebar owner."""
import hashlib,pathlib,re
r=pathlib.Path(__file__).resolve().parents[2]
o=(r.parent/'ntm-original-pristine/src/main/java/com/hbm/blocks/generic/BlockRebar.java').read_text()
n=(r/'src/main/java/com/hbm/blocks/generic/BlockRebar.java').read_text()
assert hashlib.sha256(o.encode()).hexdigest()=='0ce00dd4a37e0aaf3fbaa39524b4b455463babd9573bfa2fa9f1e597cf81c7bc','original source changed'
def method(text,signature):
 start=text.index(signature);brace=text.index('{',start);end=brace+1;depth=1
 while depth:depth+=(text[end]=='{')-(text[end]=='}');end+=1
 return text[start:end]
def norm(text):return re.sub(r'\s+','',text)
def modern(text):
 text=text.replace('void updateEntity()', 'void update()').replace('worldObj','this.world')
 for axis in 'XYZ':text=text.replace('tile.'+axis.lower()+'Coord','tile.getPos().get'+axis+'()')
 for axis in 'XYZ':text=re.sub(r'\b'+axis.lower()+r'Coord\b','this.pos.get'+axis+'()',text)
 text=text.replace('this.world.markTileEntityChunkModified(this.pos.getX(), this.pos.getY(), this.pos.getZ(), this)','this.world.markChunkDirty(this.pos, this)')
 text=text.replace('this.world.setBlock(this.pos.getX(), this.pos.getY(), this.pos.getZ(), concrete, concreteMeta, 3)','this.world.setBlockState(this.pos, concrete.getStateFromMeta(concreteMeta), 3)')
 text=text.replace('this.world.setBlock(this.pos.getX(), this.pos.getY(), this.pos.getZ(), ModBlocks.concrete_rebar)','this.world.setBlockState(this.pos, ModBlocks.concrete_rebar.getDefaultState())')
 text=text.replace('this.world.getTileEntity(node.positions[0].getX(), y, node.positions[0].getZ())','this.world.getTileEntity(new net.minecraft.util.math.BlockPos(node.positions[0].getX(), y, node.positions[0].getZ()))')
 if 'public void writeToNBT' in text:text=text.replace('public void writeToNBT','public NBTTagCompound writeToNBT');text=text[:-1]+'return nbt;}'
 return text
signatures=['public TileEntity createNewTileEntity(','public TileEntityRebar setup(','public void updateEntity(','public void invalidate(','public void serialize(','public void deserialize(','public void readFromNBT(','public void writeToNBT(','public RebarNode createNode(','public FluidTank[] getAllTanks(','public long transferFluid(','public long getDemand(','public RebarNode(INetworkProvider','public RebarNode setConnections(']
for signature in signatures:
 expected=modern(method(o,signature));actual=method(n,signature.replace('updateEntity','update').replace('void writeToNBT','NBTTagCompound writeToNBT'))
 assert norm(expected)==norm(actual), 'original complete method differs beyond explicit native API mapping: '+signature
# Original neighbor algorithm remains intact with the native callback and BlockPos signature.
old=method(o,'public void onNeighborBlockChange(');old=old[old.index('{'):]
old=old.replace('world.getTileEntity(x, y, z)','world.getTileEntity(pos)')
for axis in 'xyz':old=re.sub(r'\b'+axis+r'\b','pos.get'+axis.upper()+'()',old)
new=method(n,'public void neighborChanged(');new=new[new.index('{'):]
assert norm(old)==norm(new),'neighbor discovery/order changed'
# Compare all original ordered world cuboid bounds; inventory is that same layout with the two exact limits changed.
world=method(o,'public boolean renderWorldBlock(').split('/*TileEntity')[0]
oldboxes=re.findall(r'renderer.setRenderBounds\(([^;]+)\);',world)
newboxes=re.findall(r'boxes.add\(new AxisAlignedBB\(([^;]+)\)\);',method(n,'public List<AxisAlignedBB> getRenderBoxes('))
assert list(map(norm,oldboxes))==list(map(norm,newboxes)) and len(oldboxes)==15,'original 3/12 rod box order/bounds changed'
inventory=re.findall(r'renderer.setRenderBounds\(([^;]+)\);',method(o,'public void renderInventoryBlock('))
assert [norm(x.replace('min','0D').replace('max','1D')) for x in newboxes[3:]]==list(map(norm,inventory)),'inventory bounds changed'
assert 'if(!inventory && ClientConfig.RENDER_REBAR_SIMPLE.get())' in n,'inventory incorrectly inherits simple-world choice'
# Exact line order, min/max formulas, interpolation, selection admission and original count overflow behavior.
oldrender=method(o,'public static void renderRebar(');newrender=method(n,'public static void renderRebar(')
oldvertices=re.findall(r'tess.addVertex\(([^;]+)\);',oldrender)
newvertices=re.findall(r'buffer.pos\(([^;]+)\)\.color\(255, 255, 255, 255\)\.tex\(0, 0\)\.lightmap\(0, 240\)\.endVertex\(\);',method(n,'private static void appendSelectionBox('))
assert oldvertices==newvertices and len(oldvertices)==24,'original24 overlay vertices/white/packed240 changed'
for marker,end in [('List<TileEntityRebar> rebars','Minecraft mc'),('double dx','if(!rebars.isEmpty())'),('double minX','GL11.glPushMatrix();'),('int rebarLeft','\n\t\t}')]:
 left=oldrender[oldrender.index(marker):oldrender.index(end,oldrender.index(marker))]
 right=newrender[newrender.index(marker):newrender.index(end,newrender.index(marker))]
 assert norm(left.replace('EnumChatFormatting','TextFormatting'))==norm(right),'render selection/count/interpolation arithmetic changed: '+marker
assert 'new AxisAlignedBB(0, 0, 0, 1, progress / 1000D, 1)' in n,'concrete height clamped or changed'
assert 'return oldState.getBlock() != newState.getBlock();' in n,'native same-block metadata retention missing'
assert not re.search(r'\bRenderBlocks(?:NT)?\b|\bIIcon(?:Register)?\b|\bTessellator.instance\b|\bthePlayer\b|\btheWorld\b|\b[xyz]Coord\b',n),'removed API remains'
print('PASS14 original complete methods + native neighbor body,15world/12inventory ordered cuboids,24overlay vertices and exact selection/limit/interpolation/count arithmetic; only enumerated API migrations and native metadata retention adaptation')
