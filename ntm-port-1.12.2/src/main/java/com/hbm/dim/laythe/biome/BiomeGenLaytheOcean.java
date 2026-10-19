package com.hbm.dim.laythe.biome;

import com.hbm.entity.mob.EntityDepthSquid;
import com.hbm.entity.mob.EntityScrapFish;
import com.hbm.entity.mob.EntitySifterEel;

import net.minecraft.world.biome.Biome;

public class BiomeGenLaytheOcean extends BiomeGenBaseLaythe {

	public BiomeGenLaytheOcean(int id) {
		super(id, properties("Sagan Sea", 0.2F, 0.2F, -1.8F, 0.24F, false, 0x5b009a));

		this.waterCreatures.add(new Biome.SpawnListEntry(EntityScrapFish.class, 2, 1, 4));
		this.waterCreatures.add(new Biome.SpawnListEntry(EntitySifterEel.class, 1, 1, 4));
		this.waterCreatures.add(new Biome.SpawnListEntry(EntityDepthSquid.class, 1, 1, 4));
	}
}
