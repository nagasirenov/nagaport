package com.hbm.dim.laythe;

import java.util.ArrayList;
import java.util.List;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.WorldConfig;
import com.hbm.dim.CelestialBody;
import com.hbm.dim.ChunkProviderCelestial;
import com.hbm.dim.laythe.biome.BiomeGenBaseLaythe;
import com.hbm.dim.mapgen.MapGenGreg;
import com.hbm.dim.mapgen.MapGenTiltedSpires;
import com.hbm.entity.mob.EntityCreeperFlesh;
import com.hbm.world.gen.terrain.MapGenBubble;

import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;

public class ChunkProviderLaythe extends ChunkProviderCelestial {

	private MapGenGreg caveGenV3 = new MapGenGreg();

	private MapGenTiltedSpires spires = new MapGenTiltedSpires(2, 14, 0.75F);
	private MapGenTiltedSpires snowires = new MapGenTiltedSpires(2, 14, 0.75F);

	private MapGenBubble oil = new MapGenBubble(WorldConfig.laytheOilSpawn);

	private List<Biome.SpawnListEntry> spawnedOfFlesh = new ArrayList<Biome.SpawnListEntry>();

	public ChunkProviderLaythe(World world, long seed, boolean hasMapFeatures) {
		super(world, seed, hasMapFeatures);

		snowires.rock = Blocks.PACKED_ICE;
		snowires.regolith = Blocks.SNOW;
		spires.rock = Blocks.STONE;
		spires.regolith = ModBlocks.laythe_silt;

		spires.curve = snowires.curve = true;
		spires.maxPoint = snowires.maxPoint = 6.0F;
		spires.maxTilt = snowires.maxTilt = 3.5F;

		oil.block = ModBlocks.ore_oil;
		oil.meta = (byte)CelestialBody.getMeta(world);
		oil.replace = Blocks.STONE;
		oil.setSize(8, 16);

		seaBlock = Blocks.WATER;
		// seaLevel = 96;

		spawnedOfFlesh.add(new Biome.SpawnListEntry(EntityCreeperFlesh.class, 10, 4, 4));
	}

	@Override
	public ChunkPrimer getChunkPrimer(int x, int z) {
		ChunkPrimer primer = super.getChunkPrimer(x, z);

		if(biomesForGeneration[0] == BiomeGenBaseLaythe.laythePolar) {
			snowires.generate(worldObj, x, z, primer);
		} else {
			spires.generate(worldObj, x, z, primer);
		}
		caveGenV3.generate(worldObj, x, z, primer);
		oil.generate(worldObj, x, z, primer);

		return primer;
	}

	@Override
	public List<Biome.SpawnListEntry> getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos) {
		if(creatureType == EnumCreatureType.MONSTER && worldObj.getBlockState(pos.down()).getBlock() == ModBlocks.tumor)
			return spawnedOfFlesh;

		return super.getPossibleCreatures(creatureType, pos);
	}

}
