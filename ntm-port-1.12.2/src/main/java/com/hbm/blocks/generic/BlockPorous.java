package com.hbm.blocks.generic;

import net.minecraft.block.Block;
import net.minecraft.block.BlockStone;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class BlockPorous extends BlockStone {

	public BlockPorous() {
		super();
		this.setHardness(1.5F);
		this.setResistance(30.0F);
	}
	
	@Override
	protected ItemStack getSilkTouchDrop(net.minecraft.block.state.IBlockState state) {
		//in theory this should keep the block silk-harvestable, but dropping smooth stone instead
		return new ItemStack(Blocks.STONE);
	}
	
	@Override
	public boolean isReplaceableOreGen(net.minecraft.block.state.IBlockState state, net.minecraft.world.IBlockAccess world, net.minecraft.util.math.BlockPos pos, com.google.common.base.Predicate<net.minecraft.block.state.IBlockState> target) {
		return state.getBlock() == this || state.getBlock() == Blocks.STONE;
	}
}
