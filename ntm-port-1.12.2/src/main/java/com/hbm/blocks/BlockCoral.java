package com.hbm.blocks;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import java.util.Random;
import com.hbm.blocks.generic.PlantBlockState;
import com.hbm.items.ModItems;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.block.properties.PropertyInteger;
public class BlockCoral extends Block implements IBlockMulti {

	@Override
	public BlockCoral setSoundType(net.minecraft.block.SoundType sound) {
		super.setSoundType(sound);
		return this;
	}
 public enum EnumCoral { TUBE,BRAIN,BUBBLE,FIRE,HORN }
 public static final PropertyInteger META=PropertyInteger.create("meta",0,15);
 public BlockCoral() { super(Material.WATER);setDefaultState(blockState.getBaseState().withProperty(META,0)); }
 @Override public IBlockState getStateFromMeta(int meta) { return getDefaultState().withProperty(META,meta&15); }
 @Override public int getMetaFromState(IBlockState state) { return state.getValue(META); }
 @Override public int getSubCount() { return 5; }
 @Override public void getSubBlocks(CreativeTabs tab,NonNullList<ItemStack> items) { for(int i=0;i<5;i++)items.add(new ItemStack(this,1,i)); }
 @Override public boolean canPlaceBlockAt(World world,BlockPos pos) { return super.canPlaceBlockAt(world,pos) && world.getBlockState(pos.up()).getMaterial().isLiquid(); }
 @Override public Item getItemDropped(IBlockState state,Random random,int fortune) { return ModItems.powder_calcium; }
 @Override public int damageDropped(IBlockState state) { return 0; }

 @Override public boolean isOpaqueCube(IBlockState state) { return false; }
 @Override public boolean isFullCube(IBlockState state) { return false; }
 @Override public BlockRenderLayer getRenderLayer() { return BlockRenderLayer.CUTOUT_MIPPED; }
 private static final AxisAlignedBB CORAL_AABB = new AxisAlignedBB(0.1D, 0.0D, 0.1D, 0.9D, 0.8D, 0.9D);
 @Override public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) { return CORAL_AABB; }
 @Override public BlockFaceShape getBlockFaceShape(IBlockAccess world,IBlockState state,BlockPos pos,EnumFacing face) { return BlockFaceShape.UNDEFINED; }
 @Override public AxisAlignedBB getCollisionBoundingBox(IBlockState state,IBlockAccess world,BlockPos pos) { return NULL_AABB; }
 @Override protected BlockStateContainer createBlockState() { return PlantBlockState.create(this,META); }
 @Override public IBlockState getExtendedState(IBlockState state,IBlockAccess world,BlockPos pos) { return PlantBlockState.extend(state,world,pos); }
}
