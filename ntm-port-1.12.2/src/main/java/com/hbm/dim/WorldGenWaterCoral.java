package com.hbm.dim;

import java.util.Random;

import com.hbm.blocks.BlockCoral;
import com.hbm.blocks.ModBlocks;

import net.minecraft.block.material.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

public class WorldGenWaterCoral extends WorldGenerator {

	public int seaLevel = 64;

	@Override
	public boolean generate(World world, Random rand, BlockPos origin) {
		int x = origin.getX();
		int y = origin.getY();
		int z = origin.getZ();
		boolean flag = false;

		for(int l = 0; l < 16; ++l) {
			int px = x + rand.nextInt(8) - rand.nextInt(8);
			int py = y + rand.nextInt(4) - rand.nextInt(4);
			int pz = z + rand.nextInt(8) - rand.nextInt(8);

			BlockPos pos = new BlockPos(px, py, pz);
			if(!world.isBlockLoaded(pos)) continue;

			if(py < seaLevel - 1 && world.getBlockState(pos).getMaterial() == Material.WATER && world.getBlockState(pos.down()).getBlock() == ModBlocks.laythe_silt) {

				int meta = rand.nextInt(BlockCoral.EnumCoral.values().length) + 8;

				world.setBlockState(pos.down(), ModBlocks.laythe_coral_block.getStateFromMeta(meta), 2);

				int oy = 0;
				int ox = 0;
				int oz = 0;
				while(oy < 8) {
					if(py + oy > seaLevel - 3) break;
					if(rand.nextBoolean()) break;

					if(rand.nextBoolean()) {
						if(rand.nextBoolean()) {
							ox += rand.nextBoolean() ? 1 : -1;
						} else {
							oz += rand.nextBoolean() ? 1 : -1;
						}
					}

					BlockPos branchPos = new BlockPos(px + ox, py + oy, pz + oz);
					if(world.isBlockLoaded(branchPos)) world.setBlockState(branchPos, ModBlocks.laythe_coral_block.getStateFromMeta(meta), 2);
					if(rand.nextBoolean() && world.isBlockLoaded(branchPos.east())) world.setBlockState(branchPos.east(), ModBlocks.laythe_coral.getStateFromMeta(meta), 2);
					if(rand.nextBoolean() && world.isBlockLoaded(branchPos.west())) world.setBlockState(branchPos.west(), ModBlocks.laythe_coral.getStateFromMeta(meta), 2);
					if(rand.nextBoolean() && world.isBlockLoaded(branchPos.south())) world.setBlockState(branchPos.south(), ModBlocks.laythe_coral.getStateFromMeta(meta), 2);
					if(rand.nextBoolean() && world.isBlockLoaded(branchPos.north())) world.setBlockState(branchPos.north(), ModBlocks.laythe_coral.getStateFromMeta(meta), 2);

					oy++;
				}
				
				BlockPos tipPos = new BlockPos(px + ox, py + oy, pz + oz);
				if(world.isBlockLoaded(tipPos)) world.setBlockState(tipPos, ModBlocks.laythe_coral.getStateFromMeta(meta), 2);

				flag = true;
			}
		}

		return flag;
	}

}
