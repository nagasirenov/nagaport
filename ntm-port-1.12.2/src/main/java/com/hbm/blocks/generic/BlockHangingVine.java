package com.hbm.blocks.generic;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import java.util.List;
import java.util.Collections;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.IShearable;
public class BlockHangingVine extends Block implements IShearable {
 public BlockHangingVine(Material material) { super(material); }
 @Override public void onEntityCollision(World world,BlockPos pos,IBlockState state,Entity entity) { entity.motionX*=.5;entity.motionY*=.5;entity.motionZ*=.5;entity.fallDistance=0; }
 @Override public boolean canPlaceBlockAt(World world,BlockPos pos) { return world.isSideSolid(pos.up(),EnumFacing.UP,false) || world.getBlockState(pos.up()).getBlock()==this; }
 public boolean canBlockStay(World world,int x,int y,int z) { return canPlaceBlockAt(world,new BlockPos(x,y,z)); }
 @Override public void neighborChanged(IBlockState state,World world,BlockPos pos,Block block,BlockPos fromPos) { super.neighborChanged(state,world,pos,block,fromPos);if(!canPlaceBlockAt(world,pos))world.setBlockToAir(pos); }
 @Override protected boolean canSilkHarvest() { return true; }
 @Override public boolean isShearable(ItemStack item,IBlockAccess world,BlockPos pos) { return true; }
 @Override public List<ItemStack> onSheared(ItemStack item,IBlockAccess world,BlockPos pos,int fortune) { return Collections.singletonList(new ItemStack(this)); }

 @Override public boolean isOpaqueCube(IBlockState state) { return false; }
 @Override public boolean isFullCube(IBlockState state) { return false; }
 @Override public BlockRenderLayer getRenderLayer() { return BlockRenderLayer.CUTOUT_MIPPED; }
 @Override public BlockFaceShape getBlockFaceShape(IBlockAccess world,IBlockState state,BlockPos pos,EnumFacing face) { return BlockFaceShape.UNDEFINED; }
 @Override public AxisAlignedBB getCollisionBoundingBox(IBlockState state,IBlockAccess world,BlockPos pos) { return NULL_AABB; }
 @Override protected BlockStateContainer createBlockState() { return PlantBlockState.create(this); }
 @Override public IBlockState getExtendedState(IBlockState state,IBlockAccess world,BlockPos pos) { return PlantBlockState.extend(state,world,pos); }
}
