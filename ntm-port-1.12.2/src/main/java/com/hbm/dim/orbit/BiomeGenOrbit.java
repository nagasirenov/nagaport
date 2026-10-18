package com.hbm.dim.orbit;

import java.util.Random;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;

public class BiomeGenOrbit extends Biome implements OrbitBiomeMarker {

	public static BiomeGenOrbit biome;

	public BiomeGenOrbit() {
		super(new Biome.BiomeProperties("Space").setRainDisabled());
		this.spawnableCreatureList.clear();
		this.spawnableMonsterList.clear();
		this.spawnableWaterCreatureList.clear();
		this.spawnableCaveCreatureList.clear();
	}

	@Override
	public void genTerrainBlocks(World world, Random rand, ChunkPrimer primer, int x, int z, double noise) {
		// Orbit has no biome terrain replacement.
	}

	@Override
	public void decorate(World world, Random rand, BlockPos pos) {
		// Orbit has no biome decoration.
	}
}
