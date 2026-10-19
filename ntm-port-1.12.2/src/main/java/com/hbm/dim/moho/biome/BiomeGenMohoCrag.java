package com.hbm.dim.moho.biome;

import com.hbm.blocks.ModBlocks;


public class BiomeGenMohoCrag extends BiomeGenBaseMoho {

	public BiomeGenMohoCrag(int id) {
		super(id, properties("Moho Crag", 1.0F, 0.0F, 0.275F, 0.666F, true));

		this.topBlock = ModBlocks.moho_regolith;
		this.fillerBlock = ModBlocks.moho_regolith; // thiccer regolith due to uhhhhhh...................
	}

}
