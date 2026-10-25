package com.hbm.blocks.generic;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import java.util.Random;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
public class BlockReeds extends Block {
	@Override
	public net.minecraft.block.Block setSoundType(net.minecraft.block.SoundType sound) {
		return super.setSoundType(sound);
	}

 public BlockReeds() { super(Material.PLANTS); }
 @Override public boolean canPlaceBlockAt(World world,BlockPos pos) { Block below=world.getBlockState(pos.down()).getBlock(); return below==Blocks.WATER || below==Blocks.FLOWING_WATER; }
 public boolean canBlockStay(World world,int x,int y,int z) { return canPlaceBlockAt(world,new BlockPos(x,y,z)); }
 @Override public void neighborChanged(IBlockState state,World world,BlockPos pos,Block block,BlockPos fromPos) { super.neighborChanged(state,world,pos,block,fromPos); if(!canPlaceBlockAt(world,pos)) world.setBlockToAir(pos); }
 @Override public Item getItemDropped(IBlockState state,Random random,int fortune) { return Items.STICK; }

 @Override public boolean isOpaqueCube(IBlockState state) { return false; }
 @Override public boolean isFullCube(IBlockState state) { return false; }
 @Override public BlockRenderLayer getRenderLayer() { return BlockRenderLayer.CUTOUT_MIPPED; }
 @Override public BlockFaceShape getBlockFaceShape(IBlockAccess world,IBlockState state,BlockPos pos,EnumFacing face) { return BlockFaceShape.UNDEFINED; }
 @Override public AxisAlignedBB getCollisionBoundingBox(IBlockState state,IBlockAccess world,BlockPos pos) { return NULL_AABB; }
 @Override protected BlockStateContainer createBlockState() { return PlantBlockState.create(this); }
 @Override public IBlockState getExtendedState(IBlockState state,IBlockAccess world,BlockPos pos) { return PlantBlockState.extend(state,world,pos); }
}
