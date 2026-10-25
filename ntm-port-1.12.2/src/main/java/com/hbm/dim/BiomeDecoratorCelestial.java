package com.hbm.dim;

import static net.minecraftforge.event.terraingen.DecorateBiomeEvent.Decorate.EventType.*;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeDecorator;
import java.util.Random;
import net.minecraft.world.gen.feature.WorldGenLakes;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.event.terraingen.TerrainGen;

public class BiomeDecoratorCelestial extends BiomeDecorator {

	// Same as BiomeDecorator, but skips any vanilla plant life and supports different stone types

	public boolean generateLakes = true;

	public int lavaCount = 20;

	public int waterPlantsPerChunk = 0;
	public int rubberPlantsPerChunk = 0;
	public int coralPerChunk = 0;

	public int seaLevel = 63;
	public WorldGenWaterPlant genPlants;
	public WorldGenRubberPlant genRPlants;
	public WorldGenWaterCoral genCoral;

	// ACTUAL lakes, not the single block stuff
	// honestly MCP couldja give things better names pls?
	public int lakeChancePerChunk = 0;
	public Block lakeBlock = Blocks.WATER;

	private final Block stoneBlock;

	public BiomeDecoratorCelestial(Block stoneBlock) {
		this.stoneBlock = stoneBlock;
		this.genPlants = new WorldGenWaterPlant();
		this.genRPlants = new WorldGenRubberPlant();
		this.genCoral = new WorldGenWaterCoral();
	}

	@Override
	protected void genDecorations(Biome biome, World world, Random random) {
		BlockPos chunkPos = this.chunkPos;
		int chunkX = chunkPos.getX();
		int chunkZ = chunkPos.getZ();
		MinecraftForge.EVENT_BUS.post(new DecorateBiomeEvent.Pre(world, random, chunkPos));
		this.generateOres(world, random);

		boolean doGen = TerrainGen.decorate(world, random, chunkPos, LAKE_LAVA);
		if(doGen && this.generateLakes) {
			for (int i = 0; i < lavaCount; ++i) {
				int x = chunkX + random.nextInt(16) + 8;
				int y = random.nextInt(random.nextInt(random.nextInt(240) + 8) + 8);
				int z = chunkZ + random.nextInt(16) + 8;
				(new WorldGenLiquidsCelestial(Blocks.FLOWING_LAVA, stoneBlock)).generate(world, random, new BlockPos(x, y, z));
			}
		}

		if(doGen && this.lakeChancePerChunk > 0) {
			if(random.nextInt(lakeChancePerChunk) == 0) {
				int x = chunkX + random.nextInt(16) + 8;
				int y = random.nextInt(256);
				int z = chunkZ + random.nextInt(16) + 8;
				(new WorldGenLakes(lakeBlock)).generate(world, random, new BlockPos(x, y, z));
			}
		}

		doGen = TerrainGen.decorate(world, random, chunkPos, REED);
		if(doGen && this.waterPlantsPerChunk > 0) {
			for (int i = 0; i < waterPlantsPerChunk; ++i) {
				int x = chunkX + random.nextInt(16) + 8;
				int z = chunkZ + random.nextInt(16) + 8;
				int y = random.nextInt(seaLevel);
				genPlants.seaLevel = seaLevel;
				genPlants.generate(world, random, new BlockPos(x, y, z));
			}
		}
		doGen = TerrainGen.decorate(world, random, chunkPos, REED);
		if(doGen && this.rubberPlantsPerChunk > 0) {
			for (int i = 0; i < rubberPlantsPerChunk; ++i) {
				int x = chunkX + random.nextInt(16) + 8;
				int z = chunkZ + random.nextInt(16) + 8;
				int y = random.nextInt(80);
				genRPlants.generate(world, random, new BlockPos(x, y, z));
			}
		}

		doGen = TerrainGen.decorate(world, random, chunkPos, REED);
		if(doGen && this.coralPerChunk > 0) {
			for (int i = 0; i < coralPerChunk; ++i) {
				int x = chunkX + random.nextInt(16) + 8;
				int z = chunkZ + random.nextInt(16) + 8;
				int y = random.nextInt(seaLevel);
				genCoral.seaLevel = seaLevel;
				genCoral.generate(world, random, new BlockPos(x, y, z));
			}
		}

		MinecraftForge.EVENT_BUS.post(new DecorateBiomeEvent.Post(world, random, chunkPos));
	}

}
