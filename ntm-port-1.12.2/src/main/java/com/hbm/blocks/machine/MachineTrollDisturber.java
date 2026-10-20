package com.hbm.blocks.machine;

import java.util.Random;

import com.hbm.entity.logic.EntityNukeExplosionMK3;
import com.hbm.entity.logic.EntityNukeExplosionMK3.ATEntry;
import com.hbm.entity.logic.EntityNukeExplosionMK5;
import com.hbm.explosion.ExplosionNukeSmall;
import com.hbm.handler.radiation.ChunkRadiationManager;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.world.World;

public class MachineTrollDisturber extends Block {
	float absorb = 0;
	public MachineTrollDisturber(float ab) {
		super(Material.IRON);
		this.setTickRandomly(true);
		absorb = ab;
	}
	
	@Override
	public int tickRate(World world) {
		return 10;
	}

	@Override
	public void onBlockAdded(World world, int x, int y, int z) {
		if(!world.isRemote) world.scheduleUpdate(new net.minecraft.util.math.BlockPos(x, y, z), this, this.tickRate(world));
		ChunkRadiationManager.proxy.decrementRad(world, x, y, z, absorb);
	}

	@Override
	public void updateTick(World world, net.minecraft.util.math.BlockPos nativeTickPos, net.minecraft.block.state.IBlockState nativeTickState, Random rand) {
		int x = nativeTickPos.getX(), y = nativeTickPos.getY(), z = nativeTickPos.getZ();
		super.onBlockAdded(world, x, y, z);

		if(!world.isRemote) {
			world.scheduleUpdate(new net.minecraft.util.math.BlockPos(x, y, z), this, this.tickRate(world));
			EntityNukeExplosionMK3.at.put(new ATEntry(world.provider.getDimension(), x, y, z),  world.getTotalWorldTime() + 100);
		}
	}
}
