package com.hbm.world.feature;

import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.bomb.BlockCrashedBomb.EnumDudType;
import com.hbm.config.GeneralConfig;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

public class Dud extends WorldGenerator {
	
	protected Block[] GetValidSpawnBlocks() {
		return new Block[] {
			Blocks.GRASS,
			Blocks.DIRT,
			Blocks.STONE,
			Blocks.SAND,
			Blocks.SANDSTONE,
		};
	}

	public boolean LocationIsValidSpawn(World world, int x, int y, int z) {

		Block checkBlock = com.hbm.world.WorldBlockState.read(world, x, y - 1, z).getBlock();
		Block blockAbove = com.hbm.world.WorldBlockState.read(world, x, y, z).getBlock();
		Block blockBelow = com.hbm.world.WorldBlockState.read(world, x, y - 2, z).getBlock();

		for(Block i : GetValidSpawnBlocks()) {
			if(blockAbove != Blocks.AIR) {
				return false;
			}
			if(checkBlock == i) {
				return true;
			} else if(checkBlock == Blocks.SNOW_LAYER && blockBelow == i) {
				return true;
			} else if(checkBlock.getDefaultState().getMaterial() == Material.PLANTS && blockBelow == i) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean generate(World world, Random rand, int x, int y, int z) {
		int i = rand.nextInt(1);

		if(i == 0) {
			generate_r0(world, rand, x, y, z);
		}

		return true;

	}

	public boolean generate_r0(World world, Random rand, int x, int y, int z) {
		if(!LocationIsValidSpawn(world, x, y, z))
			return false;

		{ net.minecraft.world.World nativeRegisteredWriteWorld = world; int nativeRegisteredWriteX = x; int nativeRegisteredWriteY = y; int nativeRegisteredWriteZ = z; net.minecraft.block.Block nativeRegisteredWriteBlock = ModBlocks.crashed_balefire; int nativeRegisteredWriteMeta = rand.nextInt(EnumDudType.values().length); int nativeRegisteredWriteFlags = 3; if (nativeRegisteredWriteWorld == null) throw new NullPointerException(); if (nativeRegisteredWriteX >= -30000000 && nativeRegisteredWriteX < 30000000 && nativeRegisteredWriteZ >= -30000000 && nativeRegisteredWriteZ < 30000000 && nativeRegisteredWriteY >= 0 && nativeRegisteredWriteY < 256) com.hbm.world.WorldBlockState.write(nativeRegisteredWriteWorld, nativeRegisteredWriteX, nativeRegisteredWriteY, nativeRegisteredWriteZ, nativeRegisteredWriteBlock.getStateFromMeta(nativeRegisteredWriteMeta), nativeRegisteredWriteFlags); }

		if(GeneralConfig.enableDebugMode)
			System.out.print("[Debug] Successfully spawned dud at " + x + " " + y + " " + z + "\n");
		return true;

	}
}
