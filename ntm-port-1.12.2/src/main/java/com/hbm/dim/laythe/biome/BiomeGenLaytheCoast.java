package com.hbm.dim.laythe.biome;

import com.hbm.dim.BiomeDecoratorCelestial;
import com.hbm.entity.mob.EntityScuttlecrab;

import net.minecraft.world.biome.Biome;

public class BiomeGenLaytheCoast extends BiomeGenBaseLaythe {

	public BiomeGenLaytheCoast(int id) {
		super(id, properties("Laythe Reef", 0.2F, 0.2F, -0.4F, 0.01F, false, 0x5b009a));

		this.waterCreatures.add(new Biome.SpawnListEntry(EntityScuttlecrab.class, 4, 1, 3));

		((BiomeDecoratorCelestial)this.decorator).waterPlantsPerChunk = 16;
		((BiomeDecoratorCelestial)this.decorator).coralPerChunk = 32;
	}
}
