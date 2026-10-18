package com.hbm.lib;

import net.minecraft.block.state.IBlockState;
import net.minecraft.block.Block;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/** Boundary adapters for remaining NTM code that uses the local legacy direction type. */
public final class FacingCompat {

	private FacingCompat() {}

	public static boolean isBlockSideSolid(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
		EnumFacing facing = side == null ? null : side.toEnumFacing();
		if (facing == null) return false;

		BlockPos pos = new BlockPos(x, y, z);
		IBlockState state = world.getBlockState(pos);
		return state.getBlock().isSideSolid(state, world, pos, facing);
	}

	public static boolean isWorldSideSolid(World world, int x, int y, int z, ForgeDirection side) {
		return isWorldSideSolid(world, x, y, z, side, false);
	}

	public static boolean isWorldSideSolid(World world, int x, int y, int z, ForgeDirection side, boolean fallback) {
		EnumFacing facing = side == null ? null : side.toEnumFacing();
		return facing == null
				? fallback
				: world.isSideSolid(new BlockPos(x, y, z), facing, fallback);
	}

	public static boolean isFlammable(Block block, IBlockAccess world, int x, int y, int z, ForgeDirection side) {
		EnumFacing facing = side == null ? null : side.toEnumFacing();
		if (facing == null) return false;
		return block.isFlammable(world, new BlockPos(x, y, z), facing);
	}
}
