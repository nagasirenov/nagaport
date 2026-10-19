package com.hbm.dim.Ike;

import java.util.List;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.WorldConfig;
import com.hbm.dim.CelestialBody;
import com.hbm.dim.ChunkProviderCelestial;
import com.hbm.dim.mapgen.MapGenTiltedSpires;
import com.hbm.world.gen.terrain.MapGenBubble;
import com.hbm.world.gen.terrain.MapGenCrater;

import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.MapGenBase;
import net.minecraft.world.gen.MapGenCaves;

public class ChunkProviderIke extends ChunkProviderCelestial {

	private MapGenBase caveGenerator = new MapGenCaves();
	private MapGenTiltedSpires spires = new MapGenTiltedSpires(6, 6, 0F);

	private MapGenBubble brine = new MapGenBubble(WorldConfig.ikeBrineSpawn);
	private MapGenCrater sellafield = new MapGenCrater(WorldConfig.radfreq / 10);

	public ChunkProviderIke(World world, long seed, boolean hasMapFeatures) {
		super(world, seed, hasMapFeatures);

		spires.rock = ModBlocks.ike_stone;
		spires.regolith = ModBlocks.ike_regolith;
		spires.mid = 86;

		brine.block = ModBlocks.ore_brine;
		brine.meta = (byte)CelestialBody.getMeta(world);
		brine.replace = ModBlocks.ike_stone;
		brine.setSize(8, 16);

		sellafield.regolith = ModBlocks.sellafield;
		sellafield.rock = ModBlocks.sellafield_slaked;

		stoneBlock = ModBlocks.ike_stone;
	}

	@Override
	public ChunkPrimer getChunkPrimer(int x, int z) {
		ChunkPrimer primer = super.getChunkPrimer(x, z);

		spires.generate(worldObj, x, z, primer);
		caveGenerator.generate(worldObj, x, z, primer);
		brine.generate(worldObj, x, z, primer);
		sellafield.generate(worldObj, x, z, primer);

		return primer;
	}

	// man fuck Ike, why you gotta be spawning shit again
	@Override
	public List<Biome.SpawnListEntry> getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos) {
        return null;
	}

}
