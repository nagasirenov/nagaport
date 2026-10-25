package com.hbm.dim;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;

public abstract class BiomeGenBaseCelestial extends Biome implements CelestialBiomeMarker {

	protected final ArrayList<SpawnListEntry> creatures = new ArrayList<SpawnListEntry>();
	protected final ArrayList<SpawnListEntry> monsters = new ArrayList<SpawnListEntry>();
	protected final ArrayList<SpawnListEntry> waterCreatures = new ArrayList<SpawnListEntry>();
	protected final ArrayList<SpawnListEntry> caveCreatures = new ArrayList<SpawnListEntry>();
	// Retain the source biome definitions' Block-oriented terrain algorithm API.
	// The corresponding 1.12 Biome fields are synchronized to block states before registration.
	protected Block topBlock;
	protected Block fillerBlock;

	private final int configuredId;

	protected BiomeGenBaseCelestial(int configuredId, BiomeProperties properties) {
		super(properties);
		this.configuredId = configuredId;
	}

	public final int getConfiguredId() {
		return configuredId;
	}

	public final void synchronizeTerrainBlockStates() {
		if(topBlock != null) super.topBlock = topBlock.getDefaultState();
		if(fillerBlock != null) super.fillerBlock = fillerBlock.getDefaultState();
	}

	protected static BiomeProperties properties(String name, float temperature, float rainfall,
			float baseHeight, float heightVariation, boolean rainDisabled) {
		BiomeProperties properties = new BiomeProperties(name)
				.setTemperature(temperature)
				.setRainfall(rainfall)
				.setBaseHeight(baseHeight)
				.setHeightVariation(heightVariation);
		if(rainDisabled) properties.setRainDisabled();
		return properties;
	}

	protected final float getFloatTemperature(int x, int y, int z) {
		return getTemperature(new BlockPos(x, y, z));
	}

	protected static BiomeProperties properties(String name, float temperature, float rainfall,
			float baseHeight, float heightVariation, boolean rainDisabled, int waterColor) {
		return properties(name, temperature, rainfall, baseHeight, heightVariation, rainDisabled)
				.setWaterColor(waterColor);
	}

	/**
	 * Bridge for the legacy per-column terrain algorithms while their storage is
	 * represented by the 1.12 ChunkPrimer API.
	 */
	@Override
	public void genTerrainBlocks(World world, Random random, ChunkPrimer primer, int x, int z, double noise) {
		Block[] blocks = new Block[256];
		byte[] metadata = new byte[256];
		for(int y = 0; y < 256; y++) {
			net.minecraft.block.state.IBlockState state = primer.getBlockState(x & 15, y, z & 15);
			Block block = state.getBlock();
			blocks[y] = block;
			metadata[y] = (byte)state.getBlock().getMetaFromState(state);
		}

		if(!genTerrainBlocksLegacy(world, random, blocks, metadata, x, z, noise)) {
			super.genTerrainBlocks(world, random, primer, x, z, noise);
			return;
		}

		for(int y = 0; y < 256; y++) {
			Block block = blocks[y];
			primer.setBlockState(x & 15, y, z & 15,
					block == null ? Blocks.AIR.getDefaultState() : block.getStateFromMeta(metadata[y] & 255));
		}
	}

	protected boolean genTerrainBlocksLegacy(World world, Random random, Block[] blocks, byte[] metadata,
			int x, int z, double noise) {
		return false;
	}

	// Returns copies so callers cannot mutate the biome's spawn lists.
	@Override
	public List<SpawnListEntry> getSpawnableList(EnumCreatureType type) {
		if(type == EnumCreatureType.MONSTER) return new ArrayList<SpawnListEntry>(monsters);
		if(type == EnumCreatureType.CREATURE) return new ArrayList<SpawnListEntry>(creatures);
		if(type == EnumCreatureType.WATER_CREATURE) return new ArrayList<SpawnListEntry>(waterCreatures);
		if(type == EnumCreatureType.AMBIENT) return new ArrayList<SpawnListEntry>(caveCreatures);
		return new ArrayList<SpawnListEntry>();
	}
}
