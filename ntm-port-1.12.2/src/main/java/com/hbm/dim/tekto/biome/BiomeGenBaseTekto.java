

package com.hbm.dim.tekto.biome;

import com.hbm.blocks.ModBlocks;
import com.hbm.dim.BiomeDecoratorCelestial;
import com.hbm.dim.BiomeGenBaseCelestial;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeDecorator;

public abstract class BiomeGenBaseTekto extends BiomeGenBaseCelestial {

	public static Biome polyvinylPlains;
	public static Biome halogenHills;
	public static Biome tetrachloricRiver;
	public static Biome forest;
	public static Biome vinylsands;

	public BiomeGenBaseTekto(int id, BiomeProperties properties) {
		super(id, properties);

		this.topBlock = ModBlocks.vinyl_sand;
		this.fillerBlock = ModBlocks.vinyl_sand;
	}

	@Override
	public BiomeDecorator createBiomeDecorator() {
		BiomeDecoratorCelestial decorator = new BiomeDecoratorCelestial(ModBlocks.basalt);
		decorator.rubberPlantsPerChunk = 64;
		decorator.generateLakes = false;
		return decorator;
	}
}
