package com.hbm.blocks.machine;

import com.hbm.entity.logic.EntityNukeExplosionMK3;
import com.hbm.entity.logic.EntityNukeExplosionMK3.ATEntry;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.world.World;

import java.util.Random;

public class MachineFieldDisturber extends Block {

	public MachineFieldDisturber() {
		super(Material.IRON);
	}

	@Override
	public int tickRate(World world) {
		return 10;
	}

	@Override
	public void onBlockAdded(World world, int x, int y, int z) {
		if(!world.isRemote) world.scheduleUpdate(new net.minecraft.util.math.BlockPos(x, y, z), this, this.tickRate(world));
	}

	@Override
	public void updateTick(World world, net.minecraft.util.math.BlockPos nativeTickPos, net.minecraft.block.state.IBlockState nativeTickState, Random rand) {
		int x = nativeTickPos.getX(), y = nativeTickPos.getY(), z = nativeTickPos.getZ();

		if(!world.isRemote) {
			world.scheduleUpdate(new net.minecraft.util.math.BlockPos(x, y, z), this, this.tickRate(world));
			EntityNukeExplosionMK3.at.put(new ATEntry(world.provider.getDimension(), x, y, z),  world.getTotalWorldTime() + 100);
		}
	}
}
