package com.hbm.world.gen.terrain;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockDeadPlant.EnumDeadPlantType;
import com.hbm.blocks.generic.BlockNTMFlower.EnumFlowerType;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.MapGenBase;

public class MapGenBedrockOil extends MapGenBase {

	/**
	 * Similar to oil bubbles, but with a few more behaviours, like adding oily dirt
	 * no porous stone don't @ me
	 */

	private final int frequency;

	public Block block = ModBlocks.ore_bedrock_oil;
	public Block replace = Blocks.STONE;
	public byte meta = 0;

	public int spotWidth = 5;
	public int spotCount = 50;
	public boolean addWillows = true;

	public MapGenBedrockOil(int frequency) {
		this.frequency = frequency;
		this.range = 4;
	}

	@Override
	protected void recursiveGenerate(World world, int offsetX, int offsetZ, int chunkX, int chunkZ, ChunkPrimer primer) {
		if(rand.nextInt(frequency) == frequency - 2) {
			int xCoord = (chunkX - offsetX) * 16 + rand.nextInt(16);
			int zCoord = (chunkZ - offsetZ) * 16 + rand.nextInt(16);

			// Add the bedrock oil spot
			for(int bx = 15; bx >= 0; bx--)
			for(int bz = 15; bz >= 0; bz--)
			for(int y = 0; y < 5; y++) {
				Block target = primer.getBlockState(bx, y, bz).getBlock();

				if(target == replace || target == Blocks.BEDROCK) {
					// x, z are the coordinates relative to the target virtual chunk origin
					int x = xCoord + bx;
					int z = zCoord + bz;

					if(Math.abs(x) < 5 && Math.abs(z) < 5 && Math.abs(x) + Math.abs(y) + Math.abs(z) <= 6) {
						setBlock(primer, bx, y, bz, block.getStateFromMeta(meta));
					}
				}
			}

			int deadMetaCount = EnumDeadPlantType.values().length;

			// Add oil spot damage
			for(int i = 0; i < spotCount; i++) {
				int rx = (int)(rand.nextGaussian() * spotWidth) - xCoord;
				int rz = (int)(rand.nextGaussian() * spotWidth) - zCoord;

				if(rx >= 0 && rx < 16 && rz >= 0 && rz < 16) {
					// find ground level
					for(int y = 127; y >= 0; y--) {
						IBlockState ground = primer.getBlockState(rx, y, rz);

						if(ground.isOpaqueCube()) {
							for(int oy = 1; oy > -3; oy--) {
								int subY = y + oy;
								Block subBlock = primer.getBlockState(rx, subY, rz).getBlock();

								if(subBlock == Blocks.GRASS || subBlock == Blocks.DIRT) {
									Block dirt = rand.nextInt(10) == 0 ? ModBlocks.dirt_oily : ModBlocks.dirt_dead;
									setBlock(primer, rx, subY, rz, dirt.getDefaultState());

					if(addWillows && oy == 0 && rand.nextInt(50) == 0) {
						int flowerMeta = EnumFlowerType.CD0.ordinal();
										setBlock(primer, rx, subY + 1, rz, ModBlocks.plant_flower.getStateFromMeta(flowerMeta));
									}

									// this generation occurs BEFORE decoration, so we have no plants to modify
									// so we'll instead just add some new ones right now
					if(oy == 0 && rand.nextInt(20) == 0) {
						int deadMeta = rand.nextInt(deadMetaCount);
										setBlock(primer, rx, subY + 1, rz, ModBlocks.plant_dead.getStateFromMeta(deadMeta));
									}

									break;
								} else if(subBlock == Blocks.SAND || subBlock == ModBlocks.ore_oil_sand) {
									if(getMeta(primer, rx, subY, rz) == 1) {
										setBlock(primer, rx, subY, rz, ModBlocks.sand_dirty_red.getDefaultState());
									} else {
										setBlock(primer, rx, subY, rz, ModBlocks.sand_dirty.getDefaultState());
									}
									break;
								} else if(subBlock == Blocks.STONE) {
									setBlock(primer, rx, subY, rz, ModBlocks.stone_cracked.getDefaultState());
									break;
								} else if(subBlock == ModBlocks.rubber_silt || subBlock == ModBlocks.rubber_grass || subBlock == ModBlocks.vinyl_sand) {
									setBlock(primer, rx, subY, rz, ModBlocks.sellafield_slaked.getDefaultState());
									break;
								}
							}

							break;
						}
					}
				}
			}
		}
	}

	private void setBlock(ChunkPrimer primer, int x, int y, int z, IBlockState state) {
		primer.setBlockState(x, y, z, state);
	}

	private int getMeta(ChunkPrimer primer, int x, int y, int z) {
		IBlockState state = primer.getBlockState(x, y, z);
		return state.getBlock().getMetaFromState(state);
	}

}
