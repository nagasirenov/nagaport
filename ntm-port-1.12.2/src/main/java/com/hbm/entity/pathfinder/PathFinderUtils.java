package com.hbm.entity.pathfinder;

import net.minecraft.entity.Entity;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.World;

public class PathFinderUtils {

	public static Path getPathEntityToEntityPartial(World world, Entity fromEntity, Entity toEntity, float maxDist, boolean allowDoors, boolean allowBlocked, boolean allowWater, boolean canDrown) {
		world.profiler.startSection("pathfind");
		int startX = MathHelper.floor(fromEntity.posX);
		int startY = MathHelper.floor(fromEntity.posY + 1.0D);
		int startZ = MathHelper.floor(fromEntity.posZ);
		int maxDistEff = (int) (maxDist + 16.0F);
		int minX = startX - maxDistEff;
		int minY = startY - maxDistEff;
		int minZ = startZ - maxDistEff;
		int maxX = startX + maxDistEff;
		int maxY = startY + maxDistEff;
		int maxZ = startZ + maxDistEff;
		ChunkCache chunkcache = new ChunkCache(world, new BlockPos(minX, minY, minZ), new BlockPos(maxX, maxY, maxZ), 0);
		
		Vec3d vec = new Vec3d(toEntity.posX - fromEntity.posX, toEntity.posY - fromEntity.posY, toEntity.posZ - fromEntity.posZ);
		vec = vec.normalize().scale(maxDist);

		int x = (int) Math.floor(fromEntity.posX + vec.x);
		int y = (int) Math.floor(fromEntity.posY + vec.y);
		int z = (int) Math.floor(fromEntity.posZ + vec.z);
		
		//this part will adjust the end of the path so it's actually on the ground, it being unreachable causes mobs to slow down
		boolean solid = false;
		
		for(int i = y; i > y - 10; i--) {
			if(!world.getBlockState(new BlockPos(x, i, z)).getMaterial().blocksMovement() && NTMPathFinder.isOriginalNormalCube(world.getBlockState(new BlockPos(x, i - 1, z)))) {
				solid = true;
				y = i;
				break;
			}
			
		}
		
		if(!solid) for(int i = y + 10; i > y; i--) {
			if(!world.getBlockState(new BlockPos(x, i, z)).getMaterial().blocksMovement() && NTMPathFinder.isOriginalNormalCube(world.getBlockState(new BlockPos(x, i - 1, z)))) {
				solid = true;
				y = i;
				break;
			}
		}

		Path pathentity = (new NTMPathFinder(chunkcache, allowDoors, allowBlocked, allowWater, canDrown)).createEntityPathTo(fromEntity, x, y, z, maxDist);
		world.profiler.endSection();
		return pathentity;
	}

	public static Path getPathEntityToCoordPartial(World world, Entity fromEntity, int posX, int posY, int posZ, float maxDist, boolean allowDoors, boolean allowBlocked, boolean allowWater, boolean canDrown) {
		world.profiler.startSection("pathfind");
		int startX = MathHelper.floor(fromEntity.posX);
		int startY = MathHelper.floor(fromEntity.posY + 1.0D);
		int startZ = MathHelper.floor(fromEntity.posZ);
		int maxDistEff = (int) (maxDist + 16.0F);
		int minX = startX - maxDistEff;
		int minY = startY - maxDistEff;
		int minZ = startZ - maxDistEff;
		int maxX = startX + maxDistEff;
		int maxY = startY + maxDistEff;
		int maxZ = startZ + maxDistEff;
		ChunkCache chunkcache = new ChunkCache(world, new BlockPos(minX, minY, minZ), new BlockPos(maxX, maxY, maxZ), 0);

		Vec3d vec = new Vec3d(posX - fromEntity.posX, posY - fromEntity.posY, posZ - fromEntity.posZ);
		vec = vec.normalize().scale(maxDist);

		int x = (int) Math.floor(fromEntity.posX + vec.x);
		int y = (int) Math.floor(fromEntity.posY + vec.y);
		int z = (int) Math.floor(fromEntity.posZ + vec.z);

		//this part will adjust the end of the path so it's actually on the ground, it being unreachable causes mobs to slow down
		boolean solid = false;

		for(int i = y; i > y - 10; i--) {
			if(!world.getBlockState(new BlockPos(x, i, z)).getMaterial().blocksMovement() && NTMPathFinder.isOriginalNormalCube(world.getBlockState(new BlockPos(x, i - 1, z)))) {
				solid = true;
				y = i;
				break;
			}

		}

		if(!solid) for(int i = y + 10; i > y; i--) {
			if(!world.getBlockState(new BlockPos(x, i, z)).getMaterial().blocksMovement() && NTMPathFinder.isOriginalNormalCube(world.getBlockState(new BlockPos(x, i - 1, z)))) {
				solid = true;
				y = i;
				break;
			}
		}

		Path pathentity = (new NTMPathFinder(chunkcache, allowDoors, allowBlocked, allowWater, canDrown)).createEntityPathTo(fromEntity, x, y, z, maxDist);
		world.profiler.endSection();
		return pathentity;
	}
}
