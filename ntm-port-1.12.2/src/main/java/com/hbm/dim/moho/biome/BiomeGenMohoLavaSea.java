package com.hbm.dim.moho.biome;

import com.hbm.blocks.ModBlocks;


public class BiomeGenMohoLavaSea extends BiomeGenBaseMoho {

	public BiomeGenMohoLavaSea(int id) {
		super(id, properties("Moho Lava Sea", 1.0F, 0.0F, -0.8F, 0.01F, true));

		this.topBlock = ModBlocks.moho_regolith;
		this.fillerBlock = ModBlocks.moho_regolith; // thiccer regolith due to uhhhhhh...................
	}

}
