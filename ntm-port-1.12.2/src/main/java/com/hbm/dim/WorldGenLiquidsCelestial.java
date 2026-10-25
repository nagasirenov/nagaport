package com.hbm.dim;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

public class WorldGenLiquidsCelestial extends WorldGenerator {

	// Identical to WorldGenLiquids except you can specify the stone block to replace

	private final Block liquidBlock;
	private final Block targetBlock;

	public WorldGenLiquidsCelestial(Block liquidBlock, Block targetBlock) {
		this.liquidBlock = liquidBlock;
		this.targetBlock = targetBlock;
	}

	public boolean generate(World world, Random rand, BlockPos origin) {
		BlockPos pos = origin;

		if(world.getBlockState(pos.up()).getBlock() != targetBlock) {
			return false;
		} else if(world.getBlockState(pos.down()).getBlock() != targetBlock) {
			return false;
		} else if(world.getBlockState(pos).getMaterial() != Material.AIR && world.getBlockState(pos).getBlock() != targetBlock) {
			return false;
		} else {
			int l = 0;

			if(world.getBlockState(pos.west()).getBlock() == targetBlock) {
				++l;
			}

			if(world.getBlockState(pos.east()).getBlock() == targetBlock) {
				++l;
			}

			if(world.getBlockState(pos.north()).getBlock() == targetBlock) {
				++l;
			}

			if(world.getBlockState(pos.south()).getBlock() == targetBlock) {
				++l;
			}

			int i1 = 0;

			if(world.isAirBlock(pos.west())) {
				++i1;
			}

			if(world.isAirBlock(pos.east())) {
				++i1;
			}

			if(world.isAirBlock(pos.north())) {
				++i1;
			}

			if(world.isAirBlock(pos.south())) {
				++i1;
			}

			if(l == 3 && i1 == 1) {
				IBlockState liquidState = this.liquidBlock.getStateFromMeta(0);
				world.setBlockState(pos, liquidState, 2);
				world.immediateBlockTick(pos, liquidState, rand);
			}

			return true;
		}
	}
}
