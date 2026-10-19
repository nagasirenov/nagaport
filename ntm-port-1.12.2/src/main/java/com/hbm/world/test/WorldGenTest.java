package com.hbm.world.test;

import java.util.Random;

import net.minecraftforge.fml.common.IWorldGenerator;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.util.math.ChunkPos;

public class WorldGenTest implements IWorldGenerator {
	
	static MapGenTest gen = new MapGenTest();
	
	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
		gen.generate(world, chunkX, chunkZ, null);
		gen.generateStructure(world, random, new ChunkPos(chunkX, chunkZ));
	}
}

