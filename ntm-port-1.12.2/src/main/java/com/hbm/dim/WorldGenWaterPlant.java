package com.hbm.dim;

import java.util.Random;

import com.hbm.blocks.ModBlocks;

import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

public class WorldGenWaterPlant extends WorldGenerator {

	public int seaLevel = 64;

	@Override
	public boolean generate(World world, Random rand, BlockPos origin) {
		int x = origin.getX();
		int y = origin.getY();
		int z = origin.getZ();
		boolean flag = false;

		for(int l = 0; l < 128; ++l) {
			int px = x + rand.nextInt(8) - rand.nextInt(8);
			int py = y + rand.nextInt(4) - rand.nextInt(4);
			int pz = z + rand.nextInt(8) - rand.nextInt(8);

			BlockPos pos = new BlockPos(px, py, pz);
			if(py < seaLevel - 1 && world.getBlockState(pos).getBlock() == Blocks.WATER && world.getBlockState(pos.down()).getBlock() == ModBlocks.laythe_silt) {
				int type = rand.nextInt(7);

				switch(type) {
				case 0:
				case 1:
				case 2:
					world.setBlockState(pos, ModBlocks.laythe_short.getStateFromMeta(0), 2);
					break;
				case 3:
					world.setBlockState(pos, ModBlocks.laythe_glow.getStateFromMeta(0), 2);
					break;
				case 4:
				case 5:
					if(py < seaLevel - 2) {
						world.setBlockState(pos, ModBlocks.plant_tall_laythe.getStateFromMeta(0), 2);
						world.setBlockState(pos.up(), ModBlocks.plant_tall_laythe.getStateFromMeta(8), 2);
					}
					break;
				case 6:
					if(py < seaLevel - 4) {
						int height = 2 + rand.nextInt(Math.min(8, seaLevel - py - 2)); 
						for(int h = 0; h < height; ++h) {
							world.setBlockState(pos.up(h), ModBlocks.laythe_kelp.getStateFromMeta(0), 1);
						}	                	
					}
					break;
				}

				flag = true;
			}
		}

		return flag;
	}

}
