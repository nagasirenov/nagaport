package com.hbm.blocks.generic;

import java.util.Locale;
import java.util.Random;

import com.hbm.blocks.BlockEnumMulti;
import com.hbm.blocks.ModBlocks;
import com.hbm.items.ModItems;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockRubberCacti extends BlockEnumMulti {

	public static final PropertyEnum<EnumBushType> TYPE = PropertyEnum.create("type", EnumBushType.class);

	public BlockRubberCacti(Material material) {
		super(Material.PLANTS, EnumBushType.class, false, true);
		this.setDefaultState(this.blockState.getBaseState().withProperty(TYPE, EnumBushType.CACT));
	}

	public static enum EnumBushType implements IStringSerializable {
		CACT,
		BUSH,
		FLOWER;

		@Override
		public String getName() {
			return name().toLowerCase(Locale.ROOT);
		}
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, TYPE);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		EnumBushType[] types = EnumBushType.values();
		return getDefaultState().withProperty(TYPE, types[Math.floorMod(meta, types.length)]);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(TYPE).ordinal();
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.CUTOUT;
	}

	@Override
	public boolean canPlaceBlockAt(World world, BlockPos pos) {
		return super.canPlaceBlockAt(world, pos) && canBlockStay(world, pos, getDefaultState());
	}

	protected boolean canPlaceBlockOn(Block block) {
		return block == ModBlocks.vinyl_sand || block == ModBlocks.rubber_grass || block == ModBlocks.rubber_silt || block == ModBlocks.rubber_farmland;
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
		super.neighborChanged(state, world, pos, block, fromPos);
		checkAndDropBlock(world, pos, state);
	}

	protected void checkAndDropBlock(World world, BlockPos pos, IBlockState state) {
		if(!canBlockStay(world, pos, state)) {
			dropBlockAsItem(world, pos, state, 0);
			world.setBlockState(pos, Blocks.AIR.getDefaultState(), 2);
		}
	}

	public boolean canBlockStay(World world, BlockPos pos, IBlockState state) {
		return canPlaceBlockOn(world.getBlockState(pos.down()).getBlock());
	}

	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY,
			float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
		return getStateFromMeta(meta);
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		world.setBlockState(pos, getStateFromMeta(stack.getMetadata()), 2);
	}

	@Override
	public int damageDropped(IBlockState state) {
		return getMetaFromState(state);
	}

	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		if(state.getValue(TYPE) == EnumBushType.FLOWER) {
			return ModItems.paraffin_seeds;
		}
		return Item.getItemFromBlock(this);
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
		return null;
	}

}
