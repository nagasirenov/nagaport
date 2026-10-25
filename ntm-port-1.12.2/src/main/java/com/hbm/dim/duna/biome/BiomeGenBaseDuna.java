
package com.hbm.dim.duna.biome;

import com.hbm.blocks.ModBlocks;
import com.hbm.dim.BiomeDecoratorCelestial;
import com.hbm.dim.BiomeGenBaseCelestial;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeDecorator;

public abstract class BiomeGenBaseDuna extends BiomeGenBaseCelestial {

	public static Biome dunaPlains;
	public static Biome dunaLowlands;
	public static Biome dunaPolar;
	public static Biome dunaHills;
	public static Biome dunaPolarHills;

	public BiomeGenBaseDuna(int id, BiomeProperties properties) {
		super(id, properties);

		this.topBlock = ModBlocks.duna_sands;
		this.fillerBlock = ModBlocks.duna_rock;
	}

	@Override
	public BiomeDecorator createBiomeDecorator() {
		BiomeDecoratorCelestial decorator = new BiomeDecoratorCelestial(ModBlocks.duna_rock);
		decorator.generateLakes = false;
		return decorator;
	}
}
