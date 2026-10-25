package com.hbm.world.feature;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.WorldGenerator;

public class WorldGenSurfaceSpot extends WorldGenerator {
	
	private Block block;
	private int radius;
	private float chance;

	public WorldGenSurfaceSpot(Block block, int radius, float chance) {
		this.block = block;
		this.radius = radius;
		this.chance = chance;
	}

	public boolean generate(World world, Random rand, BlockPos pos) {
		int x = pos.getX(), y = pos.getY(), z = pos.getZ();
		int r = rand.nextInt(this.radius - 2) + 2;
		byte depth = 2;

		for(int iX = x - r; iX <= x + r; ++iX) {
			for(int iZ = z - r; iZ <= z + r; ++iZ) {
				int k1 = iX - x;
				int l1 = iZ - z;

				if(k1 * k1 + l1 * l1 <= r * r) {
					for(int iY = y - depth; iY <= y + depth; ++iY) {
						Block block = world.getBlockState(new BlockPos(iX, iY, iZ)).getBlock();

						if(block == Blocks.DIRT || block == Blocks.GRASS) {
							if(rand.nextFloat() < this.chance) world.setBlockState(new BlockPos(iX, iY, iZ), this.block.getStateFromMeta(0), 2);
						}
					}
				}
			}
		}

		return true;
	}
}
