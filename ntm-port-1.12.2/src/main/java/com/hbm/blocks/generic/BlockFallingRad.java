package com.hbm.blocks.generic;

import java.util.Random;

import com.hbm.handler.radiation.ChunkRadiationManager;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.material.Material;
import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.state.IBlockState;

public class BlockFallingRad extends BlockFalling {

	private float radIn = 0.0F;
	public BlockFallingRad(Material mat, float rad, float max) {
		super(mat);
		this.setTickRandomly(true);
		radIn = rad;
	}


		@Override
	public void updateTick(net.minecraft.world.World worldIn, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState state, java.util.Random rand) {
		updateTick(worldIn, pos.getX(), pos.getY(), pos.getZ(), rand);
	}

public void updateTick(World world, int x, int y, int z, Random rand) {
		super.updateTick(world, new net.minecraft.util.math.BlockPos(x, y, z), world.getBlockState(new net.minecraft.util.math.BlockPos(x, y, z)), rand);
		ChunkRadiationManager.proxy.incrementRad(world, x, y, z, radIn);

		world.scheduleUpdate(new net.minecraft.util.math.BlockPos(x, y, z), this, this.tickRate(world));
	}

	@Override
	public int tickRate(World world) {
		return 20;
	}

	@Override
	public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
		super.onBlockAdded(world, pos, state);
		int x = pos.getX(), y = pos.getY(), z = pos.getZ();
		world.scheduleUpdate(new net.minecraft.util.math.BlockPos(x, y, z), this, this.tickRate(world));
	}

}
