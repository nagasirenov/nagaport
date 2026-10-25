package com.hbm.blocks.generic;

import java.util.Random;

import com.hbm.handler.radiation.ChunkRadiationManager;
import com.hbm.hazard.HazardRegistry;
import com.hbm.hazard.HazardSystem;

import net.minecraft.block.BlockFalling;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.state.IBlockState;

public class BlockHazardFalling extends BlockFalling {

	private float rad = 0.0F;

	private boolean beaconable = false;

	public BlockHazardFalling() {
		this(Material.SAND);
	}

	public BlockHazardFalling(Material mat) {
		super(mat);
	}

	/** NTM construction hook for the native protected sound setter. */
	public BlockHazardFalling withSoundType(net.minecraft.block.SoundType sound) {
		super.setSoundType(sound);
		return this;
	}

	public BlockHazardFalling makeBeaconable() {
		this.beaconable  = true;
		return this;
	}

	public boolean isBeaconBase(IBlockAccess worldObj, int x, int y, int z, int beaconX, int beaconY, int beaconZ) {
		return beaconable;
	}


		@Override
	public void updateTick(net.minecraft.world.World worldIn, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState state, java.util.Random rand) {
		updateTick(worldIn, pos.getX(), pos.getY(), pos.getZ(), rand);
	}

public void updateTick(World world, int x, int y, int z, Random rand) {

		if(this.rad > 0) {
			ChunkRadiationManager.proxy.incrementRad(world, x, y, z, rad);
			world.scheduleUpdate(new net.minecraft.util.math.BlockPos(x, y, z), this, this.tickRate(world));
		}

		super.updateTick(world, new net.minecraft.util.math.BlockPos(x, y, z), world.getBlockState(new net.minecraft.util.math.BlockPos(x, y, z)), rand);
	}

	@Override
	public int tickRate(World world) {

		if(this.rad > 0)
			return 20;

		return super.tickRate(world);
	}

	@Override
	public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
		super.onBlockAdded(world, pos, state);
		int x = pos.getX(), y = pos.getY(), z = pos.getZ();

		rad = HazardSystem.getHazardLevelFromStack(new ItemStack(this), HazardRegistry.RADIATION) * 0.1F;

		if(this.rad > 0)
			world.scheduleUpdate(new net.minecraft.util.math.BlockPos(x, y, z), this, this.tickRate(world));
	}
}
