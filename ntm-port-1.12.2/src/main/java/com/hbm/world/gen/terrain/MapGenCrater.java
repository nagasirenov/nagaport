package com.hbm.world.gen.terrain;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.MapGenBase;

public class MapGenCrater extends MapGenBase {

	private int frequency = 100;
	private int minSize = 8;
	private int maxSize = 64;

	public Block regolith;
	public Block rock;

	public Biome targetBiome;

	public MapGenCrater(int frequency) {
		this.frequency = frequency;
	}

	public void setSize(int minSize, int maxSize) {
		this.minSize = minSize;
		this.maxSize = maxSize;

		this.range = (maxSize / 8) + 1;
	}

	private double depthFunc(double x, double rad, double depth) {
		return -Math.pow(x, 2) / Math.pow(rad, 2) * depth + depth;
	}

	@Override
	protected void recursiveGenerate(World world, int offsetX, int offsetZ, int chunkX, int chunkZ, ChunkPrimer primer) {
		if(rand.nextInt(frequency) == 0 && (targetBiome == null || targetBiome == world.getBiome(new BlockPos(offsetX * 16, 0, offsetZ * 16)))) {
			int xCoord = -offsetX + chunkX;
			int zCoord = -offsetZ + chunkZ;

			double radius = rand.nextInt(maxSize - minSize) + minSize;
			double depth = radius * 0.35D;

			for(int bx = 15; bx >= 0; bx--) { // bx, bz is the coordinate of the block we're modifying, relative to the generating chunk origin
				for(int bz = 15; bz >= 0; bz--) {
					for(int y = 127; y >= 0; y--) {
						IBlockState state = primer.getBlockState(bx, y, bz);

						if(state.isOpaqueCube() || state.getMaterial().isLiquid()) {
							// x, z are the coordinates relative to the target virtual chunk origin
							int x = xCoord * 16 + bx;
							int z = zCoord * 16 + bz;

							// y is at the current height now
							double r = Math.sqrt(x * x + z * z);

							if(r - rand.nextInt(3) <= radius) {
								// Carve out to intended depth
								int dep = (int)MathHelper.clamp_double(depthFunc(r, radius, depth), 0, y - 1);
								for(int i = 0; i < dep; i++) {
									primer.setBlockState(bx, y - i, bz, Blocks.AIR.getDefaultState());
								}

								y -= dep;

								dep = Math.min(3, y - 1);

								// Fill back in
								if(r + rand.nextInt(3) <= radius / 3D) {
									for(int i = 0; i < dep; i++) {
										primer.setBlockState(bx, y - i, bz, getState(regolith));
									}
								} else {
									for(int i = 0; i < dep; i++) {
										primer.setBlockState(bx, y - i, bz, getState(rock));
									}
								}
							}

							break;
						}
					}
				}
			}
		}
	}

	private IBlockState getState(Block block) {
		return block == null ? Blocks.AIR.getDefaultState() : block.getDefaultState();
	}

}
