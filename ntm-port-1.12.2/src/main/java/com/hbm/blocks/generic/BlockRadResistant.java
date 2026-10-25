package com.hbm.blocks.generic;

import com.hbm.blocks.IRadResistantBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockRadResistant extends Block implements IRadResistantBlock {

	public BlockRadResistant(Material mat) {
		super(mat);
	}

	@Override
	public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
		super.onBlockAdded(world, pos, state);
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		super.breakBlock(world, pos, state);
	}

	@Override
	public int getResistance() {
		return 1;
	}
}
