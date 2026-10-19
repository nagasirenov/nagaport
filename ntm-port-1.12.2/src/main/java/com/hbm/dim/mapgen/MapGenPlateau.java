package com.hbm.dim.mapgen;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.MapGenBase;
import net.minecraft.world.gen.NoiseGeneratorPerlin;

public class MapGenPlateau extends MapGenBase {

	public Block stoneBlock;
	public Block surfrock;
	public Block fillblock;

	public NoiseGeneratorPerlin plateauNoise;
	public double noiseScale = 0.05;
	public int maxPlateauAddition = 12;
	public int stepHeight = 6;
	public int topsoilThickness = 2;

	public Biome applyToBiome;

	public MapGenPlateau(World world) {
		this.plateauNoise = new NoiseGeneratorPerlin(world.rand, 4);
	}

	@Override
	public void generate(World world, int chunkX, int chunkZ, ChunkPrimer primer) {
		// This legacy pass ran once for the full chunk and consumes no MapGenBase neighbor RNG.
		int[][] plateauTops = new int[16][16];
		int[][] baseHeights = new int[16][16];
		for(int localX = 0; localX < 16; localX++) {
			for(int localZ = 0; localZ < 16; localZ++) {
				int baseHeight = getSurfaceHeight(primer, localX, localZ);
				baseHeights[localX][localZ] = baseHeight;
				double noiseVal = plateauNoise.func_151601_a((chunkX * 16 + localX) * noiseScale, (chunkZ * 16 + localZ) * noiseScale);
				int plateauAddition = (int)(((noiseVal + 1) / 2.0) * maxPlateauAddition);
				plateauAddition = (plateauAddition / stepHeight) * stepHeight;
				plateauTops[localX][localZ] = baseHeight + plateauAddition;
			}
		}

		for(int localX = 0; localX < 16; localX++) {
			for(int localZ = 0; localZ < 16; localZ++) {
				if(applyToBiome != null) {
					Biome biome = world.getBiome(new BlockPos(localX + chunkX * 16, 0, localZ + chunkZ * 16));
					if(biome != applyToBiome) continue;
				}
				int baseHeight = baseHeights[localX][localZ];
				int plateauTop = plateauTops[localX][localZ];
				for(int y = baseHeight + 1; y < 256; y++) {
					if(y < plateauTop - topsoilThickness) {
						primer.setBlockState(localX, y, localZ, getState(stoneBlock));
					} else if(y < plateauTop) {
						boolean sameLeft  = (localX - 1 < 0)    || (plateauTops[localX - 1][localZ] == plateauTop);
						boolean sameRight = (localX + 1 >= 16)  || (plateauTops[localX + 1][localZ] == plateauTop);
						boolean sameFront = (localZ - 1 < 0)    || (plateauTops[localX][localZ - 1] == plateauTop);
						boolean sameBack  = (localZ + 1 >= 16)  || (plateauTops[localX][localZ + 1] == plateauTop);

						if(y == plateauTop - 1 && sameLeft && sameRight && sameFront && sameBack) {
							primer.setBlockState(localX, y, localZ, getState(fillblock));
						} else {
							primer.setBlockState(localX, y, localZ, getState(surfrock));
						}
					} else {
						primer.setBlockState(localX, y, localZ, Blocks.AIR.getDefaultState());
					}
				}
			}
		}
	}

	private int getSurfaceHeight(ChunkPrimer primer, int localX, int localZ) {
		int baseHeight = 0;
		for(int y = 255; y >= 0; y--) {
			if(primer.getBlockState(localX, y, localZ).getBlock() != Blocks.AIR) {
				baseHeight = y;
				break;
			}
		}
		return baseHeight;
	}

	private IBlockState getState(Block block) {
		return block == null ? Blocks.AIR.getDefaultState() : block.getDefaultState();
	}

}
