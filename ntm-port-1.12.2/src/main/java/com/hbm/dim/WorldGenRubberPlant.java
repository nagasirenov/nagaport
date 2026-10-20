package com.hbm.dim;

import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockRubberCacti.EnumBushType;
import com.hbm.blocks.generic.BlockRubberPlant.EnumRubberPlantType;
import com.hbm.dim.tekto.biome.BiomeGenBaseTekto;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.WorldGenerator;

public class WorldGenRubberPlant extends WorldGenerator {

	@Override
	public boolean generate(World world, Random rand, int x, int y, int z) {
		boolean flag = false;
		Biome currentBiome = world.getBiome(new BlockPos(x, 0, z));

		for (int l = 0; l < 64; ++l) {
			int px = x + rand.nextInt(8) - rand.nextInt(8);
			int py = y + rand.nextInt(4) - rand.nextInt(4);
			int pz = z + rand.nextInt(8) - rand.nextInt(8);



			if ((!world.provider.hasNoSky || py < 254) && com.hbm.world.WorldBlockState.read(world, px, py - 1, pz).getBlock() == ModBlocks.rubber_grass) {

				int plantType = rand.nextInt(3);  // 0, 1, or 2

				switch (plantType) {
					case 0:
						plantType = EnumRubberPlantType.GRASS.ordinal();
						break;
					case 1:
						plantType = EnumRubberPlantType.FERN.ordinal();
						break;
					case 2:
						plantType = EnumRubberPlantType.SHRUB.ordinal();
						break;
				}

				{ net.minecraft.world.World nativeRegisteredWriteWorld = world; int nativeRegisteredWriteX = px; int nativeRegisteredWriteY = py; int nativeRegisteredWriteZ = pz; net.minecraft.block.Block nativeRegisteredWriteBlock = ModBlocks.rubber_plant; int nativeRegisteredWriteMeta = plantType; int nativeRegisteredWriteFlags = 2; if (nativeRegisteredWriteWorld == null) throw new NullPointerException(); if (nativeRegisteredWriteX >= -30000000 && nativeRegisteredWriteX < 30000000 && nativeRegisteredWriteZ >= -30000000 && nativeRegisteredWriteZ < 30000000 && nativeRegisteredWriteY >= 0 && nativeRegisteredWriteY < 256) com.hbm.world.WorldBlockState.write(nativeRegisteredWriteWorld, nativeRegisteredWriteX, nativeRegisteredWriteY, nativeRegisteredWriteZ, nativeRegisteredWriteBlock.getStateFromMeta(nativeRegisteredWriteMeta), nativeRegisteredWriteFlags); }
				flag = true;

			} else if ((!world.provider.hasNoSky || py < 254) && com.hbm.world.WorldBlockState.read(world, px, py - 1, pz).getBlock() == ModBlocks.vinyl_sand) {
				if(rand.nextInt(64) == 0) {

					world.setBlock(px, world.getTopSolidOrLiquidBlock(px, pz), pz, ModBlocks.spike_cacti, EnumBushType.CACT.ordinal(), 2);

				}

			}
			if (currentBiome == BiomeGenBaseTekto.forest) {
				if ((!world.provider.hasNoSky || py < 254) && com.hbm.world.WorldBlockState.read(world, px, py - 1, pz).getBlock() == ModBlocks.rubber_grass) {
					int plantType = rand.nextInt(2);

					switch (plantType) {
						case 0:
							plantType = EnumBushType.BUSH.ordinal();
							break;
						case 1:
							plantType = EnumBushType.FLOWER.ordinal();
							break;
					}

					int OtherplantType = rand.nextInt(3);

					switch (OtherplantType) {
					case 0:
						OtherplantType = EnumRubberPlantType.GRASS.ordinal();
						break;
					case 1:
						OtherplantType = EnumRubberPlantType.FERN.ordinal();
						break;
					case 2:
						OtherplantType = EnumRubberPlantType.SHRUB.ordinal();
						break;
				}
					if(rand.nextInt(4) == 0) {
						world.setBlock(px, py, pz, ModBlocks.spike_cacti, plantType, 2);
					} else {
						{ net.minecraft.world.World nativeRegisteredWriteWorld = world; int nativeRegisteredWriteX = px; int nativeRegisteredWriteY = py; int nativeRegisteredWriteZ = pz; net.minecraft.block.Block nativeRegisteredWriteBlock = ModBlocks.rubber_plant; int nativeRegisteredWriteMeta = OtherplantType; int nativeRegisteredWriteFlags = 2; if (nativeRegisteredWriteWorld == null) throw new NullPointerException(); if (nativeRegisteredWriteX >= -30000000 && nativeRegisteredWriteX < 30000000 && nativeRegisteredWriteZ >= -30000000 && nativeRegisteredWriteZ < 30000000 && nativeRegisteredWriteY >= 0 && nativeRegisteredWriteY < 256) com.hbm.world.WorldBlockState.write(nativeRegisteredWriteWorld, nativeRegisteredWriteX, nativeRegisteredWriteY, nativeRegisteredWriteZ, nativeRegisteredWriteBlock.getStateFromMeta(nativeRegisteredWriteMeta), nativeRegisteredWriteFlags); }

					}
					if(rand.nextInt(10) == 0) {
						world.setBlock(px, py, pz, ModBlocks.spike_cacti, EnumBushType.CACT.ordinal(), 2);

					}
					flag = true;

				}

			}
		}

		return flag;
	}

}
