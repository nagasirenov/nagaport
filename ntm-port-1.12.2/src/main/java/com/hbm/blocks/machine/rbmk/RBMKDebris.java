package com.hbm.blocks.machine.rbmk;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class RBMKDebris extends Block {

	public static final PropertyInteger METADATA = PropertyInteger.create("metadata", 0, 15);

	public RBMKDebris() {
		super(Material.IRON);
		setDefaultState(blockState.getBaseState().withProperty(METADATA, 0));
	}

	@Override protected BlockStateContainer createBlockState() { return new BlockStateContainer(this, METADATA); }
	@Override public IBlockState getStateFromMeta(int meta) { return getDefaultState().withProperty(METADATA, meta); }
	@Override public int getMetaFromState(IBlockState state) { return state.getValue(METADATA); }
	@Override public EnumBlockRenderType getRenderType(IBlockState state) { return EnumBlockRenderType.MODEL; }
	@Override public BlockRenderLayer getRenderLayer() { return BlockRenderLayer.CUTOUT_MIPPED; }
	@Override public boolean isOpaqueCube(IBlockState state) { return false; }
	@Override public boolean isFullCube(IBlockState state) { return false; }
	@Override public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing face) { return BlockFaceShape.UNDEFINED; }
}
