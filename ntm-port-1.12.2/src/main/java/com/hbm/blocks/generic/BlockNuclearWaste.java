package com.hbm.blocks.generic;

import java.util.Random;

import com.hbm.blocks.ModBlocks;

import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

public class BlockNuclearWaste extends BlockHazard {

	@Override
	public void updateTick(World world, int x, int y, int z, Random rand) {
		
		ForgeDirection dir = ForgeDirection.getOrientation(rand.nextInt(6));
		
		if(rand.nextInt(2) == 0 && com.hbm.world.WorldBlockState.read(world, x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ).getBlock() == Blocks.AIR) {
			world.setBlock(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ, ModBlocks.gas_radon_dense);
		}
		
		super.updateTick(world, x, y, z, rand);
	}
}
