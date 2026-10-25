

package com.hbm.dim.eve.biome;

import com.hbm.blocks.ModBlocks;
import com.hbm.dim.BiomeDecoratorCelestial;
import com.hbm.dim.BiomeGenBaseCelestial;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeDecorator;

public abstract class BiomeGenBaseEve extends BiomeGenBaseCelestial {

	public static Biome evePlains;
	public static Biome eveOcean;
	public static Biome eveMountains;
	public static Biome eveSeismicPlains;
	public static Biome eveRiver;

	public BiomeGenBaseEve(int id, BiomeProperties properties) {
		super(id, properties);

		this.topBlock = ModBlocks.eve_silt;
		this.fillerBlock = ModBlocks.eve_rock;
	}

	@Override
	public BiomeDecorator createBiomeDecorator() {
		return new BiomeDecoratorCelestial(ModBlocks.eve_rock);
	}
}
