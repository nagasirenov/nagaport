package com.hbm.dim.duna;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.WorldConfig;
import com.hbm.dim.CelestialBody;
import com.hbm.dim.ChunkProviderCelestial;
import com.hbm.dim.duna.biome.BiomeGenBaseDuna;
import com.hbm.dim.mapgen.ExperimentalCaveGenerator;
import com.hbm.dim.mapgen.MapGenPlateau;
import com.hbm.world.gen.terrain.MapGenBubble;

import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;

public class ChunkProviderDuna extends ChunkProviderCelestial {

	private ExperimentalCaveGenerator caveGenSmall = new ExperimentalCaveGenerator(2, 12, 0.12F);
	private ExperimentalCaveGenerator caveGenV2 = new ExperimentalCaveGenerator(2, 40, 3.0F);

	private MapGenPlateau genPlateau = new MapGenPlateau(worldObj);

	private MapGenBubble oil = new MapGenBubble(WorldConfig.dunaOilSpawn);

	public ChunkProviderDuna(World world, long seed, boolean hasMapFeatures) {
		super(world, seed, hasMapFeatures);
		stoneBlock = ModBlocks.duna_rock;

		caveGenV2.lavaBlock = ModBlocks.basalt;
		caveGenV2.stoneBlock = ModBlocks.duna_cobble;
		caveGenSmall.lavaBlock = ModBlocks.duna_sands;
		caveGenSmall.stoneBlock = ModBlocks.duna_rock;

		caveGenSmall.smallCaveSize = 0.1F;

		caveGenV2.onlyBiome = BiomeGenBaseDuna.dunaLowlands;
		caveGenSmall.ignoreBiome = BiomeGenBaseDuna.dunaLowlands;

		genPlateau.surfrock = ModBlocks.duna_sands;
		genPlateau.stoneBlock = ModBlocks.duna_rock;
		genPlateau.fillblock = ModBlocks.duna_sands;
		genPlateau.applyToBiome = BiomeGenBaseDuna.dunaHills;

		oil.block = ModBlocks.ore_oil;
		oil.meta = (byte)CelestialBody.getMeta(world);
		oil.replace = ModBlocks.duna_rock;
		oil.setSize(8, 16);
	}

	@Override
	public ChunkPrimer getChunkPrimer(int x, int z) {
		// Instead of calling super, we want to do some generation before _and_ after block replacement, for shaping!
		ChunkPrimer primer = new ChunkPrimer();
		generateBlocks(x, z, primer);
		biomesForGeneration = worldObj.getBiomeProvider().getBiomes(null, x * 16, z * 16, 16, 16);

		boolean hasLowlands = false;
		boolean hasNotLowlands = false;
		boolean hasPlateau = false;

		for(int i = 0; i < biomesForGeneration.length; i++) {
			if(biomesForGeneration[i] == BiomeGenBaseDuna.dunaLowlands) hasLowlands = true; else hasNotLowlands = true;
			if(biomesForGeneration[i] == BiomeGenBaseDuna.dunaHills) hasPlateau = true;
			if(hasLowlands && hasNotLowlands && hasPlateau) break;
		}

		// Pre-biome blocks
		if(hasPlateau) genPlateau.generate(worldObj, x, z, primer);

		replaceBlocksForBiome(x, z, primer, biomesForGeneration);

		// Post-biome blocks
		if(hasLowlands) caveGenV2.generate(worldObj, x, z, primer);
		if(hasNotLowlands) caveGenSmall.generate(worldObj, x, z, primer);

		oil.generate(worldObj, x, z, primer);

		return primer;
	}

}
