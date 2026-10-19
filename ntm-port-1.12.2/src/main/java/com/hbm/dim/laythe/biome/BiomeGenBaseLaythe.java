/*******************************************************************************
 * Copyright 2015 SteveKunG - More Planets Mod
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International Public License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/.
 ******************************************************************************/

package com.hbm.dim.laythe.biome;

import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.dim.BiomeDecoratorCelestial;
import com.hbm.dim.BiomeGenBaseCelestial;
import com.hbm.entity.mob.EntityScutterfish;
import com.hbm.entity.mob.EntitySifterEel;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeDecorator;

public abstract class BiomeGenBaseLaythe extends BiomeGenBaseCelestial {

	public static Biome laytheIsland;
	public static Biome laytheOcean;
	public static Biome laythePolar;
	public static Biome laytheCoast;

	public BiomeGenBaseLaythe(int id, BiomeProperties properties) {
		super(id, properties);

		this.waterCreatures.add(new Biome.SpawnListEntry(EntityScutterfish.class, 10, 4, 4));
		this.waterCreatures.add(new Biome.SpawnListEntry(EntitySifterEel.class, 10, 1, 3));
		
		this.topBlock = ModBlocks.laythe_silt;
		this.fillerBlock = ModBlocks.laythe_silt;
	}

	@Override
	protected BiomeDecorator createBiomeDecorator() {
		BiomeDecoratorCelestial decorator = new BiomeDecoratorCelestial(Blocks.STONE);
		decorator.waterPlantsPerChunk = 32;
		decorator.generateLakes = false;
		return decorator;
	}

	// Same as vanilla but without the gravel
	@Override
	protected boolean genTerrainBlocksLegacy(World world, Random rand, Block[] blocks, byte[] meta, int x, int z, double noise) {
		Block block = this.topBlock;
		byte b0 = 0;
		Block block1 = this.fillerBlock;
		int k = -1;
		int l = (int) (noise / 3.0D + 3.0D + rand.nextDouble() * 0.25D);
		int i1 = x & 15;
		int j1 = z & 15;

		for(int l1 = 255; l1 >= 0; --l1) {
			int i2 = l1;

			if(l1 <= 0 + rand.nextInt(5)) {
				blocks[i2] = Blocks.BEDROCK;
			} else {
				Block block2 = blocks[i2];

				if(block2 != null && block2.getMaterial() != Material.AIR) {
					if(block2 == Blocks.STONE) {
						if(k == -1) {
							if(l <= 0) {
								block = null;
								b0 = 0;
								block1 = Blocks.STONE;
							} else if(l1 >= 59 && l1 <= 64) {
								block = this.topBlock;
								b0 = 0;
								block1 = this.fillerBlock;
							}

							if(l1 < 63 && (block == null || block.getMaterial() == Material.AIR)) {
								if(this.getFloatTemperature(x, l1, z) < 0.15F) {
									block = Blocks.ICE;
									b0 = 0;
								} else {
									block = Blocks.WATER;
									b0 = 0;
								}
							}

							k = l;

							if(l1 >= 62) {
								blocks[i2] = block;
								meta[i2] = b0;
							} else if(l1 < 56 - l) {
								block = null;
								block1 = Blocks.STONE;
								blocks[i2] = ModBlocks.laythe_silt;
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
}
