package com.hbm.dim.thatmo;

import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.dim.BiomeGenBaseCelestial;
import com.hbm.entity.mob.EntityMoonCow;
import com.hbm.entity.mob.EntityTankbot;
import com.hbm.entity.mob.EntityWarBehemoth;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeDecorator;

public class BiomeGenThatmo extends BiomeGenBaseCelestial {

	public static Biome biome;

	public BiomeGenThatmo(int id) {
		super(id, properties("Thatmo", 0.5F, 0.5F, 0.125F, 0.05F, true));
		this.creatures.add(new Biome.SpawnListEntry(EntityTankbot.class, 6, 1, 1));
		this.creatures.add(new Biome.SpawnListEntry(EntityWarBehemoth.class, 2, 1, 1));
		this.creatures.add(new Biome.SpawnListEntry(EntityMoonCow.class, 10, 1, 1));

		this.decorator.generateLakes = false;

		this.topBlock = Blocks.SNOW;
		this.fillerBlock = Blocks.SNOW;
	}

	@Override
	protected boolean genTerrainBlocksLegacy(World world, Random rand, Block[] blocks, byte[] meta, int x, int z, double noise) {
		Block block = this.topBlock;
		byte b0 = 0;
		Block block1 = this.fillerBlock;
		int k = -1;
		int l = (int) (noise / 6.0D + 6.0D + rand.nextDouble() * 0.85D);

		for (int l1 = 255; l1 >= 0; --l1) {
			int i2 = l1;

			if (l1 <= 0 + rand.nextInt(5)) {
				blocks[i2] = Blocks.BEDROCK;
			} else {
				Block block2 = blocks[i2];

				if (block2 != null && block2.getDefaultState().getMaterial() != Material.AIR) {
					if (block2 == ModBlocks.basalt) {
						if (k == -1) {
							if (l <= 0) {
								block = null;
								b0 = 0;
								block1 = ModBlocks.basalt;
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
							} else if (l1 < 62) {
								block = null;
								block1 =  ModBlocks.basalt;
								if (Math.random() > 0.4) {
									blocks[i2] =  ModBlocks.basalt;
								} else {
									blocks[i2] = ModBlocks.sellafield_slaked;
								}
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
