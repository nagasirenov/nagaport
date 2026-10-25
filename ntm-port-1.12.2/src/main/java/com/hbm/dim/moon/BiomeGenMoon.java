package com.hbm.dim.moon;

import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.MobConfig;
import com.hbm.dim.BiomeGenBaseCelestial;
import com.hbm.entity.mob.EntityMoonCow;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

public class BiomeGenMoon extends BiomeGenBaseCelestial {

	public static Biome biome;

	public BiomeGenMoon(int id) {
		super(id, properties("Mun", 0.5F, 0.5F, 0.125F, 0.05F, true));

		if(MobConfig.enableMunCows) {
			this.creatures.add(new Biome.SpawnListEntry(EntityMoonCow.class, 10, 4, 4));
		}


		this.topBlock = ModBlocks.moon_turf;
		this.fillerBlock = ModBlocks.moon_rock;
	}

	@Override
	public float getSpawningChance() {
		return 0.008F;
	}

	@Override
	protected boolean genTerrainBlocksLegacy(World world, Random rand, Block[] blocks, byte[] meta, int x, int z, double noise) {
		// boolean flag = true;
		Block block = this.topBlock;
		byte b0 = 0;
		Block block1 = this.fillerBlock;
		int k = -1;
		int l = (int) (noise / 3.0D + 3.0D + rand.nextDouble() * 0.25D);

		for (int l1 = 255; l1 >= 0; --l1) {
			int i2 = l1;

			if (l1 <= 0 + rand.nextInt(5)) {
				blocks[i2] = Blocks.BEDROCK;
			} else {
				Block block2 = blocks[i2];

				if (block2 != null && block2.getDefaultState().getMaterial() != Material.AIR) {
					if (block2 == ModBlocks.moon_rock) {
						if (k == -1) {
							if (l <= 0) {
								block = null;
								b0 = 0;
								block1 = ModBlocks.moon_rock;
							} else if (l1 >= 59 && l1 <= 64) {
								block = this.topBlock;
								b0 = 0;
								block1 = this.fillerBlock;
							}

					if (l1 < 63 && (block == null || block.getDefaultState().getMaterial() == Material.AIR)) {
								if (this.getFloatTemperature(x, l1, z) < 0.15F) {
									block = this.topBlock;
									b0 = 0;
								} else {
									block = this.topBlock;
									b0 = 0;
								}
							}

							k = l;

							if (l1 >= 62) {
								blocks[i2] = block;
								meta[i2] = b0;
							} else if (l1 < 56 - l) {
								block = null;
								block1 = ModBlocks.moon_rock;
								blocks[i2] = Blocks.GRAVEL;
							} else {
								blocks[i2] = block1;
							}
						} else if (k > 0) {
							--k;
							blocks[i2] = block1;

							if (k == 0 && block1 == Blocks.SAND) {
								k = rand.nextInt(4) + Math.max(0, l1 - 63);
								block1 = Blocks.SANDSTONE;
							}
						}
					}
				} else {
					k = -1;
				}
			}
		}
		return true;
	}

}
