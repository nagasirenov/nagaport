package com.hbm.dim.minmus;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.WorldConfig;
import com.hbm.dim.CelestialBody;
import com.hbm.dim.ChunkProviderCelestial;
import com.hbm.dim.mapgen.MapGenCrater;
import com.hbm.dim.mapgen.MapGenVanillaCaves;
import com.hbm.world.gen.terrain.MapGenBubble;

import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.MapGenBase;

public class ChunkProviderMinmus extends ChunkProviderCelestial {

	private MapGenBase caveGenerator = new MapGenVanillaCaves(ModBlocks.minmus_stone).withLava(ModBlocks.minmus_smooth);

	private MapGenCrater smallCrater = new MapGenCrater(5);
	private MapGenCrater largeCrater = new MapGenCrater(96);

	private MapGenBubble brine = new MapGenBubble(WorldConfig.minmusBrineSpawn);

	public ChunkProviderMinmus(World world, long seed, boolean hasMapFeatures) {
		super(world, seed, hasMapFeatures);

		smallCrater.setSize(6, 24);
		largeCrater.setSize(64, 128);

		smallCrater.regolith = largeCrater.regolith = ModBlocks.minmus_regolith;
		smallCrater.rock = largeCrater.rock = ModBlocks.minmus_stone;

		brine.block = ModBlocks.ore_brine;
		brine.meta = (byte)CelestialBody.getMeta(world);
		brine.replace = ModBlocks.minmus_stone;
		brine.setSize(8, 16);

		stoneBlock = ModBlocks.minmus_stone;
		seaBlock = ModBlocks.minmus_smooth;
		seaLevel = 63;
	}

	@Override
	public ChunkPrimer getChunkPrimer(int x, int z) {
		ChunkPrimer primer = super.getChunkPrimer(x, z);

		caveGenerator.generate(worldObj, x, z, primer);
		smallCrater.generate(worldObj, x, z, primer);
		largeCrater.generate(worldObj, x, z, primer);
		brine.generate(worldObj, x, z, primer);

		return primer;
	}

}
