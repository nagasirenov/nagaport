package com.hbm.blocks.machine;

import java.util.Random;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public abstract class BlockBeamBase extends BlockContainer {

	public static final PropertyInteger METADATA = PropertyInteger.create("metadata", 0, 15);
	private static final AxisAlignedBB EMPTY_BOUNDS = new AxisAlignedBB(0, 0, 0, 0, 0, 0);

	public BlockBeamBase() {
		super(Material.AIR);
		setDefaultState(blockState.getBaseState().withProperty(METADATA, 0));
		setLightLevel(1.0F);
		setLightOpacity(0);
		setHardness(-1);
		setResistance(1_000_000);
	}

	@Override protected BlockStateContainer createBlockState() { return new BlockStateContainer(this, METADATA); }
	@Override public IBlockState getStateFromMeta(int meta) { return getDefaultState().withProperty(METADATA, meta & 15); }
	@Override public int getMetaFromState(IBlockState state) { return state.getValue(METADATA); }
	@Override public boolean isOpaqueCube(IBlockState state) { return false; }
	@Override public boolean isFullCube(IBlockState state) { return false; }
	@Override public EnumBlockRenderType getRenderType(IBlockState state) { return EnumBlockRenderType.INVISIBLE; }
	@Override public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing side) { return BlockFaceShape.UNDEFINED; }
	@Override public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) { return EMPTY_BOUNDS; }

	@Override public Item getItemDropped(IBlockState state, Random rand, int fortune) { return Items.AIR; }
	@Override public int quantityDropped(Random rand) { return 0; }

	@Override public boolean isAir(IBlockState state, IBlockAccess world, BlockPos pos) { return true; }
	@Override public boolean canBeReplacedByLeaves(IBlockState state, IBlockAccess world, BlockPos pos) { return true; }
	@Override public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) { return NULL_AABB; }

	// This was taken from GregsLighting (cargo cult behaviour)
	// This is a bit screwy, but it's needed so that trees are not prevented from growing
	// near a floodlight beam.
	@Override public boolean isLeaves(IBlockState state, IBlockAccess world, BlockPos pos) { return true; }
}
