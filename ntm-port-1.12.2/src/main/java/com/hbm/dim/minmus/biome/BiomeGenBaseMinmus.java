

package com.hbm.dim.minmus.biome;

import com.hbm.blocks.ModBlocks;
import com.hbm.dim.BiomeDecoratorCelestial;
import com.hbm.dim.BiomeGenBaseCelestial;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeDecorator;

public abstract class BiomeGenBaseMinmus extends BiomeGenBaseCelestial {

	public static Biome minmusPlains;
	public static Biome minmusCanyon;

	public BiomeGenBaseMinmus(int id, BiomeProperties properties) {
		super(id, properties);

		this.topBlock = ModBlocks.minmus_regolith; //remind me to send roadhog his daily modmail
		this.fillerBlock = ModBlocks.minmus_regolith;
	}

	@Override
	protected BiomeDecorator createBiomeDecorator() {
		BiomeDecoratorCelestial decorator = new BiomeDecoratorCelestial(ModBlocks.minmus_regolith);
		decorator.generateLakes = false;
		return decorator;
	}
}
