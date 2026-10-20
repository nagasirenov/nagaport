package com.hbm.world.biome;

import static net.minecraftforge.event.terraingen.DecorateBiomeEvent.Decorate.EventType.*;

import com.hbm.blocks.ModBlocks;
import com.hbm.world.feature.WorldGenSurfaceSpot;

import net.minecraft.init.Blocks;
import net.minecraft.world.biome.BiomeDecorator;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;
import java.util.Random;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;
import net.minecraft.world.gen.feature.WorldGenDeadBush;
import net.minecraft.world.gen.feature.WorldGenLiquids;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.event.terraingen.TerrainGen;

public class BiomeDecoratorNoMansLand extends BiomeDecorator {

	public WorldGenerator sellafiteGen;
	public WorldGenerator gravelGen;
	public int sellafitePerChunk;
	public int gravelPerChunk;
	
	public BiomeDecoratorNoMansLand() {
		super();

		//TODO: instead of multiple localized shitty generators, make one that covers everything
		this.sellafiteGen = new WorldGenSurfaceSpot(ModBlocks.sellafield_slaked, 6, 0.15F);
		this.gravelGen = new WorldGenSurfaceSpot(ModBlocks.sellafield_slaked, 6, 0.15F);
		this.sellafitePerChunk = 2;
		this.gravelPerChunk = 2;
	}

	@Override
	protected void genDecorations(Biome biome, World world, Random rand) {
		MinecraftForge.EVENT_BUS.post(new DecorateBiomeEvent.Pre(world, rand, this.chunkPos));
		this.generateOres(world, rand);
		
		int x;
		int y;
		int z;

		/// SAND IN WATER ///
		boolean doGen = TerrainGen.decorate(world, rand, this.chunkPos, SAND);
		for(int i = 0; doGen && i < this.sandPatchesPerChunk; ++i) {
			x = this.chunkPos.getX() + rand.nextInt(16) + 8;
			z = this.chunkPos.getZ() + rand.nextInt(16) + 8;
			this.sandGen.generate(world, rand, world.getTopSolidOrLiquidBlock(new BlockPos(x, 0, z)));
		}

		/// GRAVEL IN WATER ///
		doGen = TerrainGen.decorate(world, rand, this.chunkPos, SAND_PASS2);
		for(int i = 0; doGen && i < this.gravelPatchesPerChunk; ++i) {
			x = this.chunkPos.getX() + rand.nextInt(16) + 8;
			z = this.chunkPos.getZ() + rand.nextInt(16) + 8;
			super.gravelGen.generate(world, rand, world.getTopSolidOrLiquidBlock(new BlockPos(x, 0, z)));
		}
		
		/// SELLAFITE ///
		for(int i = 0; i < this.sellafitePerChunk; ++i) {
			x = this.chunkPos.getX() + rand.nextInt(16) + 8;
			z = this.chunkPos.getZ() + rand.nextInt(16) + 8;
			this.sellafiteGen.generate(world, rand, world.getTopSolidOrLiquidBlock(new BlockPos(x, 0, z)));
		}
		
		/// GRAVEL ///
		for(int i = 0; i < this.gravelPerChunk; ++i) {
			x = this.chunkPos.getX() + rand.nextInt(16) + 8;
			z = this.chunkPos.getZ() + rand.nextInt(16) + 8;
			this.gravelGen.generate(world, rand, world.getTopSolidOrLiquidBlock(new BlockPos(x, 0, z)));
		}

		int trees = this.treesPerChunk;

		if(rand.nextInt(10) == 0) trees++;

		/// TREES ///
		doGen = TerrainGen.decorate(world, rand, this.chunkPos, TREE);
		for(int i = 0; doGen && i < trees; ++i) {
			x = this.chunkPos.getX() + rand.nextInt(16) + 8;
			z = this.chunkPos.getZ() + rand.nextInt(16) + 8;
			y = world.getHeight(new BlockPos(x, 0, z)).getY();
			WorldGenAbstractTree worldgenabstracttree = biome.getRandomTreeFeature(rand);
			worldgenabstracttree.setDecorationDefaults();

			if(worldgenabstracttree.generate(world, rand, new BlockPos(x, y, z))) {
				worldgenabstracttree.generateSaplings(world, rand, new BlockPos(x, y, z));
			}
		}

		/// TALL GRASS ///
		doGen = TerrainGen.decorate(world, rand, this.chunkPos, GRASS);
		for(int i = 0; doGen && i < this.grassPerChunk; ++i) {
			x = this.chunkPos.getX() + rand.nextInt(16) + 8;
			z = this.chunkPos.getZ() + rand.nextInt(16) + 8;
			y = nextInt(rand, world.getHeight(new BlockPos(x, 0, z)).getY() * 2);
			WorldGenerator worldgenerator = biome.getRandomWorldGenForGrass(rand);
			worldgenerator.generate(world, rand, new BlockPos(x, y, z));
		}

		/// SHRUBBERY ///
		doGen = TerrainGen.decorate(world, rand, this.chunkPos, DEAD_BUSH);
		for(int i = 0; doGen && i < this.deadBushPerChunk; ++i) {
			x = this.chunkPos.getX() + rand.nextInt(16) + 8;
			z = this.chunkPos.getZ() + rand.nextInt(16) + 8;
			y = nextInt(rand, world.getHeight(new BlockPos(x, 0, z)).getY() * 2);
			(new WorldGenDeadBush()).generate(world, rand, new BlockPos(x, y, z));
		}

		/// LAKES ///
		doGen = TerrainGen.decorate(world, rand, this.chunkPos, LAKE_WATER);
		if(doGen && this.generateFalls) {
			for(int i = 0; i < 50; ++i) {
				x = this.chunkPos.getX() + rand.nextInt(16) + 8;
				y = rand.nextInt(rand.nextInt(248) + 8);
				z = this.chunkPos.getZ() + rand.nextInt(16) + 8;
				(new WorldGenLiquids(Blocks.FLOWING_WATER)).generate(world, rand, new BlockPos(x, y, z));
			}
		}

		MinecraftForge.EVENT_BUS.post(new DecorateBiomeEvent.Post(world, rand, this.chunkPos));
	}

	private int nextInt(Random rand, int i) {
		if(i <= 1)
			return 0;
		return rand.nextInt(i);
	}
}
