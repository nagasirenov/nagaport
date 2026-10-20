package com.hbm.blocks.rail;


import com.hbm.blocks.BlockDummyable;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

public class RailStandardCurveWide7 extends RailStandardCurveBase {

	public RailStandardCurveWide7() {
		super();
		this.width = 6;
	}

	@Override
	protected boolean checkRequirement(World world, int x, int y, int z, ForgeDirection dir, int o) {
		
		ForgeDirection rot = dir.getRotation(ForgeDirection.UP);
		dir = dir.getOpposite();

		int dX = dir.offsetX;
		int dZ = dir.offsetZ;
		int rX = rot.offsetX;
		int rZ = rot.offsetZ;
		
		return world.getBlockState(new net.minecraft.util.math.BlockPos(x + dX, y, z + dZ)).getBlock().isReplaceable(world, new net.minecraft.util.math.BlockPos(x + dX, y, z + dZ)) &&
				world.getBlockState(new net.minecraft.util.math.BlockPos(x + rX, y, z + rZ)).getBlock().isReplaceable(world, new net.minecraft.util.math.BlockPos(x + rX, y, z + rZ)) &&
				world.getBlockState(new net.minecraft.util.math.BlockPos(x + dX + rX, y, z + dZ + rZ)).getBlock().isReplaceable(world, new net.minecraft.util.math.BlockPos(x + dX + rX, y, z + dZ + rZ)) &&
				world.getBlockState(new net.minecraft.util.math.BlockPos(x + dX + rX * 2, y, z + dZ + rZ * 2)).getBlock().isReplaceable(world, new net.minecraft.util.math.BlockPos(x + dX + rX * 2, y, z + dZ + rZ * 2)) &&
				world.getBlockState(new net.minecraft.util.math.BlockPos(x + dX * 2 + rX, y, z + dZ * 2 + rZ)).getBlock().isReplaceable(world, new net.minecraft.util.math.BlockPos(x + dX * 2 + rX, y, z + dZ * 2 + rZ)) &&
				world.getBlockState(new net.minecraft.util.math.BlockPos(x + dX * 2 + rX * 2, y, z + dZ * 2 + rZ * 2)).getBlock().isReplaceable(world, new net.minecraft.util.math.BlockPos(x + dX * 2 + rX * 2, y, z + dZ * 2 + rZ * 2)) &&
				world.getBlockState(new net.minecraft.util.math.BlockPos(x + dX * 3 + rX, y, z + dZ * 3 + rZ)).getBlock().isReplaceable(world, new net.minecraft.util.math.BlockPos(x + dX * 3 + rX, y, z + dZ * 3 + rZ)) &&
				world.getBlockState(new net.minecraft.util.math.BlockPos(x + dX * 3 + rX * 2, y, z + dZ * 3 + rZ * 2)).getBlock().isReplaceable(world, new net.minecraft.util.math.BlockPos(x + dX * 3 + rX * 2, y, z + dZ * 3 + rZ * 2)) &&
				world.getBlockState(new net.minecraft.util.math.BlockPos(x + dX * 2 + rX * 3, y, z + dZ * 2 + rZ * 3)).getBlock().isReplaceable(world, new net.minecraft.util.math.BlockPos(x + dX * 2 + rX * 3, y, z + dZ * 2 + rZ * 3)) &&
				world.getBlockState(new net.minecraft.util.math.BlockPos(x + dX * 3 + rX * 3, y, z + dZ * 3 + rZ * 3)).getBlock().isReplaceable(world, new net.minecraft.util.math.BlockPos(x + dX * 3 + rX * 3, y, z + dZ * 3 + rZ * 3)) &&
				world.getBlockState(new net.minecraft.util.math.BlockPos(x + dX * 4 + rX * 3, y, z + dZ * 4 + rZ * 3)).getBlock().isReplaceable(world, new net.minecraft.util.math.BlockPos(x + dX * 4 + rX * 3, y, z + dZ * 4 + rZ * 3)) &&
				world.getBlockState(new net.minecraft.util.math.BlockPos(x + dX * 3 + rX * 4, y, z + dZ * 3 + rZ * 4)).getBlock().isReplaceable(world, new net.minecraft.util.math.BlockPos(x + dX * 3 + rX * 4, y, z + dZ * 3 + rZ * 4)) &&
				world.getBlockState(new net.minecraft.util.math.BlockPos(x + dX * 4 + rX * 4, y, z + dZ * 4 + rZ * 4)).getBlock().isReplaceable(world, new net.minecraft.util.math.BlockPos(x + dX * 4 + rX * 4, y, z + dZ * 4 + rZ * 4));
	}

	@Override
	protected void fillSpace(World world, int x, int y, int z, ForgeDirection dir, int o) {
		
		BlockDummyable.safeRem = true;
		
		ForgeDirection rot = dir.getRotation(ForgeDirection.UP);
		dir = dir.getOpposite();

		int dX = dir.offsetX;
		int dZ = dir.offsetZ;
		int rX = rot.offsetX;
		int rZ = rot.offsetZ;

		world.setBlockState(new net.minecraft.util.math.BlockPos(x + dX, y, z + dZ), this.getStateFromMeta(dir.ordinal()), 3);
		world.setBlockState(new net.minecraft.util.math.BlockPos(x + dX * 2, y, z + dZ * 2), this.getStateFromMeta(dir.ordinal()), 3);
		world.setBlockState(new net.minecraft.util.math.BlockPos(x + rX, y, z + rZ), this.getStateFromMeta(rot.ordinal()), 3);
		world.setBlockState(new net.minecraft.util.math.BlockPos(x + dX + rX, y, z + dZ + rZ), this.getStateFromMeta(rot.ordinal()), 3);
		world.setBlockState(new net.minecraft.util.math.BlockPos(x + dX * 2 + rX, y, z + dZ * 2 + rZ), this.getStateFromMeta(rot.ordinal()), 3);
		world.setBlockState(new net.minecraft.util.math.BlockPos(x + dX * 3 + rX, y, z + dZ * 3 + rZ), this.getStateFromMeta(dir.ordinal()), 3);
		world.setBlockState(new net.minecraft.util.math.BlockPos(x + dX * 4 + rX, y, z + dZ * 4 + rZ), this.getStateFromMeta(dir.ordinal()), 3);
		world.setBlockState(new net.minecraft.util.math.BlockPos(x + dX * 2 + rX * 2, y, z + dZ * 2 + rZ * 2), this.getStateFromMeta(rot.ordinal()), 3);
		world.setBlockState(new net.minecraft.util.math.BlockPos(x + dX * 3 + rX * 2, y, z + dZ * 3 + rZ * 2), this.getStateFromMeta(dir.ordinal()), 3);
		world.setBlockState(new net.minecraft.util.math.BlockPos(x + dX * 4 + rX * 2, y, z + dZ * 4 + rZ * 2), this.getStateFromMeta(dir.ordinal()), 3);
		world.setBlockState(new net.minecraft.util.math.BlockPos(x + dX * 5 + rX * 2, y, z + dZ * 5 + rZ * 2), this.getStateFromMeta(dir.ordinal()), 3);
		world.setBlockState(new net.minecraft.util.math.BlockPos(x + dX * 3 + rX * 3, y, z + dZ * 3 + rZ * 3), this.getStateFromMeta(rot.ordinal()), 3);
		world.setBlockState(new net.minecraft.util.math.BlockPos(x + dX * 4 + rX * 3, y, z + dZ * 4 + rZ * 3), this.getStateFromMeta(dir.ordinal()), 3);
		world.setBlockState(new net.minecraft.util.math.BlockPos(x + dX * 5 + rX * 3, y, z + dZ * 5 + rZ * 3), this.getStateFromMeta(dir.ordinal()), 3);
		world.setBlockState(new net.minecraft.util.math.BlockPos(x + dX * 4 + rX * 4, y, z + dZ * 4 + rZ * 4), this.getStateFromMeta(rot.ordinal()), 3);
		world.setBlockState(new net.minecraft.util.math.BlockPos(x + dX * 5 + rX * 4, y, z + dZ * 5 + rZ * 4), this.getStateFromMeta(dir.ordinal()), 3);
		world.setBlockState(new net.minecraft.util.math.BlockPos(x + dX * 6 + rX * 4, y, z + dZ * 6 + rZ * 4), this.getStateFromMeta(dir.ordinal()), 3);
		world.setBlockState(new net.minecraft.util.math.BlockPos(x + dX * 5 + rX * 5, y, z + dZ * 5 + rZ * 5), this.getStateFromMeta(rot.ordinal()), 3);
		world.setBlockState(new net.minecraft.util.math.BlockPos(x + dX * 5 + rX * 6, y, z + dZ * 5 + rZ * 6), this.getStateFromMeta(rot.ordinal()), 3);
		world.setBlockState(new net.minecraft.util.math.BlockPos(x + dX * 6 + rX * 5, y, z + dZ * 6 + rZ * 5), this.getStateFromMeta(rot.ordinal()), 3);
		world.setBlockState(new net.minecraft.util.math.BlockPos(x + dX * 6 + rX * 6, y, z + dZ * 6 + rZ * 6), this.getStateFromMeta(rot.ordinal()), 3);
		
		BlockDummyable.safeRem = false;
	}


}
