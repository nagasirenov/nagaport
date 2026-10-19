package com.hbm.dim.mapgen;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.gen.MapGenRavine;
import net.minecraft.world.chunk.ChunkPrimer;

public class MapgenRavineButBased extends MapGenRavine {

	public Block stoneBlock;

	public MapgenRavineButBased() {
		super();
		this.stoneBlock = Blocks.STONE;
	}

	@Override
	protected void digBlock(ChunkPrimer primer, int x, int y, int z, int chunkX, int chunkZ, boolean foundTop, IBlockState top, IBlockState filler) {
		Block block = primer.getBlockState(x, y, z).getBlock();

		if(block == stoneBlock || block == filler.getBlock() || block == top.getBlock()) {
			if(y < 10) {
				primer.setBlockState(x, y, z, Blocks.FLOWING_LAVA.getDefaultState());
			} else {
				primer.setBlockState(x, y, z, Blocks.AIR.getDefaultState());

				if (foundTop && primer.getBlockState(x, y - 1, z).getBlock() == filler.getBlock()) {
					primer.setBlockState(x, y - 1, z, top);
				}
			}
		}
	}

}
