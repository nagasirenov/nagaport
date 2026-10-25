package com.hbm.dim.mapgen;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.gen.MapGenCaves;
import net.minecraft.world.chunk.ChunkPrimer;

public class MapGenVanillaCaves extends MapGenCaves {

	// Same as vanilla cavegen but supports celestial stone
	private final Block stoneBlock;

	private Block lavaBlock = Blocks.LAVA;

	public MapGenVanillaCaves(Block stoneBlock) {
		this.stoneBlock = stoneBlock;
	}

	public MapGenVanillaCaves withLava(Block lavaBlock) {
		this.lavaBlock = lavaBlock;
		return this;
	}

	protected void digBlock(ChunkPrimer primer, int x, int y, int z, int chunkX, int chunkZ, boolean foundTop, IBlockState top, IBlockState filler) {
		Block block = primer.getBlockState(x, y, z).getBlock();

		if(block == stoneBlock || block == filler.getBlock() || block == top.getBlock()) {
			if(y < 10) {
				primer.setBlockState(x, y, z, lavaBlock.getDefaultState());
			} else {
				primer.setBlockState(x, y, z, Blocks.AIR.getDefaultState());

				if(foundTop && primer.getBlockState(x, y - 1, z).getBlock() == filler.getBlock()) {
					primer.setBlockState(x, y - 1, z, top);
				}
			}
		}
	}

}
