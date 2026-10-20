package com.hbm.dim.moho.biome;

import java.util.Random;

import com.hbm.blocks.ModBlocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

public class BiomeGenMohoBasalt extends BiomeGenBaseMoho {

	public BiomeGenMohoBasalt(int id) {
		super(id, properties("Moho Basalt Deltas", 1.0F, 0.0F, 0.0F, 0.224F, true));

		this.topBlock = ModBlocks.basalt;
		this.fillerBlock = ModBlocks.basalt;
	}

	@Override
	protected boolean genTerrainBlocksLegacy(World world, Random rand, Block[] blocks, byte[] meta, int x, int z, double noise) {
		Block block = this.topBlock;
		byte b0 = 0;
		Block block1 = this.fillerBlock;
		int k = -1;
		int l = (int) (noise / 8.0D + 8.0D + rand.nextDouble() * 0.50D);

		for(int by = 255; by >= 0; --by) {
			int i = by;

			boolean contour = (noise > -0.25D && noise < 1.0D) || (noise > 1.5D && noise < 2.0D) || (noise > 2.5D && noise < 3.0D);
			double offset = contour ? 0 : Math.pow(rand.nextDouble(), 4) * 8;

			if(by <= 0 + rand.nextInt(5)) {
				blocks[i] = Blocks.BEDROCK;
			} else {
				Block block2 = blocks[i];

				if(block2 != null && block2.getDefaultState().getMaterial() != Material.AIR) {
					if(block2 == ModBlocks.moho_stone) {
						if(k == -1) {
							if(l <= 0) {
								block = null;
								b0 = 0;
								block1 = ModBlocks.moho_stone;
							} else if(by >= 59 && by <= 64) {
								block = this.topBlock;
								b0 = 0;
								block1 = this.fillerBlock;
							}

					if(by < 63 && (block == null || block.getDefaultState().getMaterial() == Material.AIR)) {
								if(this.getFloatTemperature(x, by, z) < 0.15F) {
									block = this.topBlock;
									b0 = 0;
								} else {
									block = this.topBlock;
									b0 = 0;
								}
							}

							k = l;

							if(by >= 62) {
								blocks[i] = block;
								meta[i] = b0;
							} else if(by < 56 - l) {
								block = null;
								block1 = ModBlocks.moho_stone;
								blocks[i] = ModBlocks.basalt;
							} else {
								blocks[i] = block1;
							}

							for(int oy = 0; oy < offset; oy++) {
								blocks[i+oy] = this.topBlock;
							}
						} else if(k > 0) {
							--k;
							blocks[i] = block1;
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
