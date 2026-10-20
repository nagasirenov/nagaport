package com.hbm.blocks.machine.rbmk;

import java.util.Random;

import com.hbm.blocks.ModBlocks;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

public class RBMKDebrisDigamma extends RBMKDebris {


	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
		
		if(!world.isRemote) {
			
			for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
				Block b = world.getBlockState(pos.add(dir.offsetX, dir.offsetY, dir.offsetZ)).getBlock();
				if((b instanceof RBMKDebris  && b != this) || b == ModBlocks.corium_block || b == ModBlocks.block_corium)
					world.setBlockState(pos.add(dir.offsetX, dir.offsetY, dir.offsetZ), getDefaultState());
			}
		}
	}

	@Override
	public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
		super.onBlockAdded(world, pos, state);
		world.scheduleUpdate(pos, this, 2);
	}
}
