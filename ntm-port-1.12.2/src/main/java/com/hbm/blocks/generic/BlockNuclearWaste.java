package com.hbm.blocks.generic;

import java.util.Random;

import com.hbm.blocks.ModBlocks;

import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

public class BlockNuclearWaste extends BlockHazard {


		@Override
	public void updateTick(net.minecraft.world.World worldIn, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState state, java.util.Random rand) {
		updateTick(worldIn, pos.getX(), pos.getY(), pos.getZ(), rand);
	}

public void updateTick(World world, int x, int y, int z, Random rand) {

		ForgeDirection dir = ForgeDirection.getOrientation(rand.nextInt(6));

		if(rand.nextInt(2) == 0 && com.hbm.world.WorldBlockState.read(world, x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ).getBlock() == Blocks.AIR) {
			com.hbm.world.WorldBlockState.writeLegacyBlock(world, x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ, ModBlocks.gas_radon_dense);
		}

		super.updateTick(world, x, y, z, rand);
	}
}
