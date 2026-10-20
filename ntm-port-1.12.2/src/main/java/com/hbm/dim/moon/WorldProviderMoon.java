package com.hbm.dim.moon;

import com.hbm.blocks.ModBlocks;
import com.hbm.dim.WorldProviderCelestial;

import net.minecraft.block.Block;
import net.minecraft.world.biome.BiomeProviderSingle;
import net.minecraft.world.gen.IChunkGenerator;

public class WorldProviderMoon extends WorldProviderCelestial {

	@Override
	public void init() {
		super.init();
		this.biomeProvider = new BiomeProviderSingle(BiomeGenMoon.biome);
	}

	@Override
	public String getDimensionName() {
		return "Mun";
	}

	@Override
	public IChunkGenerator createChunkGenerator() {
		return new ChunkProviderMoon(this.world, this.getSeed(), false);
	}

	@Override
	public Block getStone() {
		return ModBlocks.moon_rock;
	}

}
