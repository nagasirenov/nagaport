package com.hbm.blocks.generic;

import net.minecraft.block.BlockDoor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockFenceGate extends BlockModDoor {

	public BlockFenceGate(Material mat) {
		super(mat);
	}

	private static final AxisAlignedBB AABB_CLOSED_Z = new AxisAlignedBB(0.46875D, 0.0D, 0.0D, 0.53125D, 1.0D, 1.0D);
	private static final AxisAlignedBB AABB_CLOSED_X = new AxisAlignedBB(0.0D, 0.0D, 0.46875D, 1.0D, 1.0D, 0.53125D);

	private static final AxisAlignedBB AABB_OPEN_0_LEFT = new AxisAlignedBB(0.5D, 0.0D, 0.0D, 1.5D, 1.0D, 0.0625D);
	private static final AxisAlignedBB AABB_OPEN_0_RIGHT = new AxisAlignedBB(0.5D, 0.0D, 0.9375D, 1.5D, 1.0D, 1.0D);
	private static final AxisAlignedBB AABB_OPEN_1_LEFT = new AxisAlignedBB(0.9375D, 0.0D, 0.5D, 1.0D, 1.0D, 1.5D);
	private static final AxisAlignedBB AABB_OPEN_1_RIGHT = new AxisAlignedBB(0.0D, 0.0D, 0.5D, 0.0625D, 1.0D, 1.5D);
	private static final AxisAlignedBB AABB_OPEN_2_LEFT = new AxisAlignedBB(-0.5D, 0.0D, 0.9375D, 0.5D, 1.0D, 1.0D);
	private static final AxisAlignedBB AABB_OPEN_2_RIGHT = new AxisAlignedBB(-0.5D, 0.0D, 0.0D, 0.5D, 1.0D, 0.0625D);
	private static final AxisAlignedBB AABB_OPEN_3_LEFT = new AxisAlignedBB(0.0D, 0.0D, -0.5D, 0.0625D, 1.0D, 0.5D);
	private static final AxisAlignedBB AABB_OPEN_3_RIGHT = new AxisAlignedBB(0.9375D, 0.0D, -0.5D, 1.0D, 1.0D, 0.5D);

	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		state = state.getActualState(source, pos);
		EnumFacing facing = (EnumFacing) state.getValue(FACING);
		boolean open = ((Boolean) state.getValue(OPEN)).booleanValue();
		boolean rightHinge = state.getValue(HINGE) == BlockDoor.EnumHingePosition.RIGHT;

		if (!open) {
			return (facing == EnumFacing.EAST || facing == EnumFacing.WEST) ? AABB_CLOSED_Z : AABB_CLOSED_X;
		}

		switch (facing) {
		case EAST:
		default:
			return rightHinge ? AABB_OPEN_0_RIGHT : AABB_OPEN_0_LEFT;
		case SOUTH:
			return rightHinge ? AABB_OPEN_1_RIGHT : AABB_OPEN_1_LEFT;
		case WEST:
			return rightHinge ? AABB_OPEN_2_RIGHT : AABB_OPEN_2_LEFT;
		case NORTH:
			return rightHinge ? AABB_OPEN_3_RIGHT : AABB_OPEN_3_LEFT;
		}
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
		blockState = blockState.getActualState(worldIn, pos);
		if (((Boolean) blockState.getValue(OPEN)).booleanValue()) {
			return NULL_AABB;
		}
		return this.getBoundingBox(blockState, worldIn, pos);
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
		BlockPos pos = new BlockPos(x, y, z);
		return this.getCollisionBoundingBox(world.getBlockState(pos), world, pos);
	}

	@Override
	protected String getSFX() {
		return "hbm:block.fenceGate";
	}
}
