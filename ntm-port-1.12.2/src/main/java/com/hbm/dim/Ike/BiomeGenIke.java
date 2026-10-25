package com.hbm.dim.Ike;

import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.dim.BiomeDecoratorCelestial;
import com.hbm.dim.BiomeGenBaseCelestial;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeDecorator;

public class BiomeGenIke extends BiomeGenBaseCelestial {

	public static Biome biome;

	public BiomeGenIke(int id) {
		super(id, properties("Ike", 0.5F, 0.5F, 0.325F, 0.05F, true));

		this.topBlock = ModBlocks.ike_regolith;
		this.fillerBlock = ModBlocks.ike_regolith; // thiccer regolith due to uhhhhhh...................
	}

	@Override
	protected boolean genTerrainBlocksLegacy(World world, Random rand, Block[] blocks, byte[] meta, int x, int z, double noise) {
		Block block = this.topBlock;
		byte b0 = 0;
		Block block1 = this.fillerBlock;
		int k = -1;
		int l = (int) (noise / 8.0D + 8.0D + rand.nextDouble() * 0.50D);

		for(int l1 = 255; l1 >= 0; --l1) {
			int i2 = l1;

			if(l1 <= 0 + rand.nextInt(5)) {
				blocks[i2] = Blocks.BEDROCK;
			} else {
				Block block2 = blocks[i2];

				if(block2 != null && block2.getDefaultState().getMaterial() != Material.AIR) {
					if(block2 == ModBlocks.ike_stone) {
						if(k == -1) {
							if(l <= 0) {
								block = null;
								b0 = 0;
								block1 = ModBlocks.ike_stone;
							} else if(l1 >= 59 && l1 <= 64) {
								block = this.topBlock;
								b0 = 0;
								block1 = this.fillerBlock;
							}

					if(l1 < 63 && (block == null || block.getDefaultState().getMaterial() == Material.AIR)) {
								if(this.getFloatTemperature(x, l1, z) < 0.15F) {
									block = this.topBlock;
									b0 = 0;
								} else {
									block = this.topBlock;
									b0 = 0;
								}
							}

							k = l;

							if(l1 >= 62) {
								blocks[i2] = block;
								meta[i2] = b0;
							} else if(l1 < 56 - l) {
								block = null;
								block1 = ModBlocks.ike_stone;
								blocks[i2] = Blocks.GRAVEL;
							} else {
								blocks[i2] = block1;
							}
						} else if(k > 0) {
							--k;
							blocks[i2] = block1;

							if(k == 0 && block1 == Blocks.SAND) {
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

	@Override
	public BiomeDecorator createBiomeDecorator() {
		BiomeDecoratorCelestial decorator = new BiomeDecoratorCelestial(ModBlocks.ike_stone);
		decorator.lakeChancePerChunk = 8;
		decorator.lakeBlock = ModBlocks.bromine_block;
		decorator.generateLakes = false;
		return decorator;
	}
}
