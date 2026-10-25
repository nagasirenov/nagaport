package com.hbm.dim.orbit;

import java.util.List;

import com.hbm.util.Compat;
import com.hbm.world.WorldUtil;

import net.minecraftforge.fml.common.Loader;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.IChunkGenerator;

public class ChunkProviderOrbit implements IChunkGenerator {

	protected World worldObj;

	public ChunkProviderOrbit(World world) {
		this.worldObj = world;
	}
	
	@Override
	public Chunk generateChunk(int x, int z) {
		Chunk chunk = new Chunk(worldObj, x, z);
		int orbitBiomeId = Biome.getIdForBiome(BiomeGenOrbit.biome);
		if (orbitBiomeId < 0) {
			throw new IllegalStateException("Orbit biome is not registered");
		}

		if(Loader.isModLoaded(Compat.MOD_EIDS)) {
			short[] biomes = WorldUtil.getBiomeShortArray(chunk);
			for(int k = 0; k < biomes.length; ++k) {
				biomes[k] = (short) orbitBiomeId;
			} 
		} else {
			byte[] biomes = chunk.getBiomeArray();
			for(int k = 0; k < biomes.length; ++k) {
				biomes[k] = (byte) orbitBiomeId;
			}
		}

		chunk.generateSkylightMap();
		return chunk;
	}

	/**
	 * Populates chunk with ores etc etc
	 */
	@Override
	public void populate(int x, int z) {
		
	}

	/**
	 * Returns a list of creatures of the specified type that can spawn at the given
	 * location.
	 */
	@Override
	public List<Biome.SpawnListEntry> getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos) {
		return null;
	}

	@Override
	public BlockPos getNearestStructurePos(World world, String structureName, BlockPos position, boolean findUnexplored) {
		return null;
	}

	@Override
	public boolean generateStructures(Chunk chunk, int x, int z) {
		return false;
	}

	@Override
	public void recreateStructures(Chunk chunk, int x, int z) {

	}

	@Override
	public boolean isInsideStructure(World world, String structureName, BlockPos pos) {
		return false;
	}

}
