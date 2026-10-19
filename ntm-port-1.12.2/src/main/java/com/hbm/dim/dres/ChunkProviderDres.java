package com.hbm.dim.dres;

import com.hbm.blocks.ModBlocks;
import com.hbm.dim.ChunkProviderCelestial;
import com.hbm.dim.mapgen.MapGenCrater;
import com.hbm.dim.mapgen.MapGenVanillaCaves;

import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.MapGenBase;

public class ChunkProviderDres extends ChunkProviderCelestial {
	
	private MapGenBase caveGenerator = new MapGenVanillaCaves(ModBlocks.dres_rock);

	private MapGenCrater smallCrater = new MapGenCrater(6);
	private MapGenCrater largeCrater = new MapGenCrater(64);

	public ChunkProviderDres(World world, long seed, boolean hasMapFeatures) {
		super(world, seed, hasMapFeatures);

		smallCrater.setSize(8, 32);
		largeCrater.setSize(96, 128);

		smallCrater.regolith = largeCrater.regolith = ModBlocks.dres_rock;
		smallCrater.rock = largeCrater.rock = ModBlocks.dres_rock;

		stoneBlock = ModBlocks.dres_rock;
	}
	
	@Override
	public ChunkPrimer getChunkPrimer(int x, int z) {
		ChunkPrimer primer = super.getChunkPrimer(x, z);

		caveGenerator.generate(worldObj, x, z, primer);
		smallCrater.generate(worldObj, x, z, primer);
		largeCrater.generate(worldObj, x, z, primer);
		
		return primer;
	}

}
