package com.hbm.dim.minmus.biome;

import com.hbm.blocks.ModBlocks;

public class BiomeGenMinmusBasin extends BiomeGenBaseMinmus {

	public BiomeGenMinmusBasin(int id) {
		super(id, properties("Minmus Basins", -1.0F, 0.0F, -1.0F, 0.02F, true));
		this.topBlock = ModBlocks.minmus_smooth;
		this.fillerBlock = ModBlocks.minmus_regolith;
	}

}
