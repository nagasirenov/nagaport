package com.hbm.dim;

import java.util.List;
import java.util.Random;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.IntCache;

public class WorldChunkManagerCelestial extends BiomeProvider {

	private final GenLayer biomeLayer;
	private final GenLayer biomeDetailLayer;

	public WorldChunkManagerCelestial(BiomeGenLayers layers) {
		this.biomeLayer = layers.biomeLayer;
		this.biomeDetailLayer = layers.biomeDetailLayer;
	}

	// getBiomesToSpawnIn is never called for celestial bodies, so fuck that noise lmao

	public int[] getRainfall(int[] values, int x, int z, int width, int length) {
		IntCache.resetIntCache();

		if(values == null || values.length < width * length) {
			values = new int[width * length];
		}

		int[] biomeIds = biomeDetailLayer.getInts(x, z, width, length);

		for(int i = 0; i < width * length; ++i) {
			float rainfall = Biome.getBiome(biomeIds[i]).getRainfall();

			if(rainfall > 1.0F) rainfall = 1.0F;

			values[i] = (int) (rainfall * 65536.0F);
		}

		return values;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public float getTemperatureAtHeight(float temperature, int y) {
		return temperature;
	}

	@Override
	public Biome[] getBiomesForGeneration(Biome[] biomes, int x, int z, int width, int length) {
		IntCache.resetIntCache();

		if(biomes == null || biomes.length < width * length) {
			biomes = new Biome[width * length];
		}

		int[] biomeIds = biomeLayer.getInts(x, z, width, length);

		for(int i = 0; i < width * length; ++i) {
			biomes[i] = Biome.getBiome(biomeIds[i]);
		}

		return biomes;
	}

	@Override
	public Biome[] getBiomes(Biome[] biomes, int x, int z, int width, int length) {
		return getBiomes(biomes, x, z, width, length, true);
	}

	@Override
	public Biome[] getBiomes(Biome[] biomes, int x, int z, int width, int length, boolean cacheFlag) {
		IntCache.resetIntCache();

		if(biomes == null || biomes.length < width * length) {
			biomes = new Biome[width * length];
		}

		if(cacheFlag && width == 16 && length == 16 && (x & 15) == 0 && (z & 15) == 0) {
			for(int i = 0; i < width * length; ++i) {
				biomes[i] = super.getBiome(new BlockPos(x + i % width, 0, z + i / width));
			}

			return biomes;
		}

		int[] biomeIds = biomeDetailLayer.getInts(x, z, width, length);

		for(int i = 0; i < width * length; ++i) {
			biomes[i] = Biome.getBiome(biomeIds[i]);
		}

		return biomes;
	}

	// Used by structures, generally, to find if a given chunk contains any of the specified biomes
	@Override
	public boolean areBiomesViable(int x, int z, int size, List<Biome> biomes) {
		IntCache.resetIntCache();

		int l = x - size >> 2;
		int i1 = z - size >> 2;
		int j1 = x + size >> 2;
		int k1 = z + size >> 2;
		int l1 = j1 - l + 1;
		int i2 = k1 - i1 + 1;
		int[] biomeIds = this.biomeLayer.getInts(l, i1, l1, i2);

		for(int j2 = 0; j2 < l1 * i2; ++j2) {
			Biome biome = Biome.getBiome(biomeIds[j2]);

			if(!biomes.contains(biome)) {
				return false;
			}
		}

		return true;
	}

	// Finds a chunk in a given area that has one of the specified biomes
	@Override
	public BlockPos findBiomePosition(int x, int z, int size, List<Biome> biomes, Random rand) {
		IntCache.resetIntCache();

		int l = x - size >> 2;
		int i1 = z - size >> 2;
		int j1 = x + size >> 2;
		int k1 = z + size >> 2;
		int l1 = j1 - l + 1;
		int i2 = k1 - i1 + 1;
		int[] biomeIds = this.biomeLayer.getInts(l, i1, l1, i2);
		BlockPos position = null;
		int matches = 0;

		for(int k2 = 0; k2 < l1 * i2; ++k2) {
			int xPos = l + k2 % l1 << 2;
			int zPos = i1 + k2 / l1 << 2;
			Biome biome = Biome.getBiome(biomeIds[k2]);

			if(biomes.contains(biome) && (position == null || rand.nextInt(matches + 1) == 0)) {
				position = new BlockPos(xPos, 0, zPos);
				++matches;
			}
		}

		return position;
	}

	@Override
	public void cleanupCache() {
		super.cleanupCache();
	}

	public static class BiomeGenLayers {

		private final GenLayer biomeLayer;
		private final GenLayer biomeDetailLayer;

		public BiomeGenLayers(GenLayer biomeLayer, GenLayer biomeDetailLayer, long seed) {
			this.biomeLayer = biomeLayer;
			this.biomeDetailLayer = biomeDetailLayer;

			biomeLayer.initWorldGenSeed(seed);
			biomeDetailLayer.initWorldGenSeed(seed);
		}

	}

}
