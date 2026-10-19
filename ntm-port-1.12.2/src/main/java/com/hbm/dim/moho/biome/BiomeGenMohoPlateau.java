package com.hbm.dim.moho.biome;

import com.hbm.blocks.ModBlocks;


public class BiomeGenMohoPlateau extends BiomeGenBaseMoho {

	public BiomeGenMohoPlateau(int id) {
		super(id, properties("Moho Plateau", 1.0F, 0.0F, 0.255F, 0.432F, true));

		this.topBlock = ModBlocks.moho_regolith;
		this.fillerBlock = ModBlocks.moho_regolith; // thiccer regolith due to uhhhhhh...................
	}

}
