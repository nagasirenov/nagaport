package com.hbm.world.generator;

import java.util.List;
import java.util.Random;

import com.hbm.inventory.RecipesCommon.MetaBlock;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockMatcher;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenMinable;

public class DungeonToolbox {

	/** Places an audited, unoriented structure-palette block.
	 * The caller passes its current palette field, never a cached replacement.
	 */
	public static void placeUnorientedStructureBlock(World world, int x, int y, int z, Block block) {
		if(world == null) throw new NullPointerException();
		// Preserve the original pre-chunk bounds before resolving the native state.
		if(x >= -30000000 && z >= -30000000 && x < 30000000 && z < 30000000 && y >= 0 && y < 256) {
			world.setBlockState(new BlockPos(x, y, z), block.getStateFromMeta(0), 3);
		}
	}


	/** Places one of the original four concrete bricks, using Library's shared selector. */
	public static void placeRandomConcrete(World world, int x, int y, int z) {
		Block concrete = com.hbm.lib.Library.getRandomConcrete();
		// Selection precedes the receiver check, just as it did at the call sites.
		if(world == null) throw new NullPointerException();
		// Keep the original pre-chunk bounds, including draws for rejected placements.
		if(x >= -30000000 && z >= -30000000 && x < 30000000 && z < 30000000 && y >= 0 && y < 256) {
			world.setBlockState(new BlockPos(x, y, z), concrete.getStateFromMeta(0), 3);
		}
	}


	public static void generateBox(World world, int x, int y, int z, int sx, int sy, int sz, List<MetaBlock> blocks) {

		if(blocks.isEmpty())
			return;

		for(int i = x; i < x + sx; i++) {
			for(int j = y; j < y + sy; j++) {
				for(int k = z; k < z + sz; k++) {
					MetaBlock b = getRandom(blocks, world.rand);
					world.setBlockState(new BlockPos(i, j, k), b.block.getStateFromMeta(b.meta), 2);
				}
			}
		}
	}

	public static void generateBox(World world, int x, int y, int z, int sx, int sy, int sz, Block block) {
		generateBox(world, x, y, z, sx, sy, sz, new MetaBlock(block));
	}

	//i know it's copy paste, but it's a better strat than using a wrapper and generating single-entry lists for no good reason
	public static void generateBox(World world, int x, int y, int z, int sx, int sy, int sz, MetaBlock block) {

		for(int i = x; i < x + sx; i++) {

			for(int j = y; j < y + sy; j++) {

				for(int k = z; k < z + sz; k++) {

					world.setBlockState(new BlockPos(i, j, k), block.block.getStateFromMeta(block.meta), 2);
				}
			}
		}
	}

	//now with vectors to provide handy rotations
	public static void generateBox(World world, int x, int y, int z, Vec3d size, List<MetaBlock> blocks) {

		generateBox(world, x, y, z, (int)size.x, (int)size.y, (int)size.z, blocks);
	}

	public static <T> T getRandom(List<T> list, Random rand) {

		if(list.isEmpty())
			return null;

		return list.get(rand.nextInt(list.size()));
	}

	public static void generateOre(World world, Random rand, int chunkX, int chunkZ, int veinCount, int amount, int minHeight, int variance, Block ore) {
		generateOre(world, rand, chunkX, chunkZ, veinCount, amount, minHeight, variance, ore, 0, Blocks.STONE);
	}

	public static void generateOre(World world, Random rand, int chunkX, int chunkZ, int veinCount, int amount, int minHeight, int variance, Block ore, int meta) {
		generateOre(world, rand, chunkX, chunkZ, veinCount, amount, minHeight, variance, ore, meta, Blocks.STONE);
	}

	public static void generateOre(World world, Random rand, int chunkX, int chunkZ, int veinCount, int amount, int minHeight, int variance, Block ore, Block target) {
		generateOre(world, rand, chunkX, chunkZ, veinCount, amount, minHeight, variance, ore, 0, target);
	}

	public static void generateOre(World world, Random rand, int chunkX, int chunkZ, int veinCount, int amount, int minHeight, int variance, Block ore, int meta, Block target) {

		for(int i = 0; i < veinCount; i++) {

			int x = chunkX + rand.nextInt(16);
			int y = minHeight + (variance > 0 ? rand.nextInt(variance) : 0);
			int z = chunkZ + rand.nextInt(16);

			(new WorldGenMinable(ore.getStateFromMeta(meta), amount, BlockMatcher.forBlock(target))).generate(world, rand, new BlockPos(x, y, z));
		}
	}

	public static void generateFlowers(World world, Random rand, int chunkX, int chunkZ, Block flower, int meta) {
		int x = chunkX + rand.nextInt(16) + 8;
		int z = chunkZ + rand.nextInt(16) + 8;
		int y = world.getHeight(x, z);
		IBlockState state = flower.getStateFromMeta(meta);

		// 1.12 WorldGenFlowers only accepts BlockFlower. Keep the legacy scatter
		// for NTM's ordinary Block subclasses, including reeds, without a cast.
		for(int attempt = 0; attempt < 64; attempt++) {
			int flowerX = x + rand.nextInt(8) - rand.nextInt(8);
			int flowerY = y + rand.nextInt(4) - rand.nextInt(4);
			int flowerZ = z + rand.nextInt(8) - rand.nextInt(8);
			BlockPos pos = new BlockPos(flowerX, flowerY, flowerZ);
			if(world.isAirBlock(pos) && (world.provider.hasSkyLight() || flowerY < 255) && flower.canPlaceBlockAt(world, pos)) {
				world.setBlockState(pos, state, 2);
			}
		}
	}
}
