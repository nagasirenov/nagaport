package com.hbm.entity.pathfinder;

import com.hbm.blocks.generic.BlockMetalFence;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.BlockWall;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathHeap;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.IntHashMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/**
 * The original NTM mobs' four-neighbour search, using native 1.12 path objects.
 * Native PathFinder is not interchangeable: its costs, diagonals, priorities and
 * iteration bound would change the routes chosen by the original mobs.
 * This helper is deliberately limited to the search used by PathFinderUtils;
 * it does not replace native navigation or implement the removed mob lifecycle.
 */
public final class NTMPathFinder {
    private final IBlockAccess blockAccess;
    private final PathHeap open = new PathHeap();
    private final IntHashMap<PathPoint> points = new IntHashMap<>();
    private final PathPoint[] options = new PathPoint[32];
    private final boolean allowDoors;
    private final boolean allowBlocked;
    private boolean avoidsWater;
    private final boolean canSwim;

    public NTMPathFinder(IBlockAccess blockAccess, boolean allowDoors, boolean allowBlocked,
            boolean avoidsWater, boolean canSwim) {
        this.blockAccess = blockAccess;
        this.allowDoors = allowDoors;
        this.allowBlocked = allowBlocked;
        this.avoidsWater = avoidsWater;
        this.canSwim = canSwim;
    }

    public Path createEntityPathTo(Entity entity, int x, int y, int z, float maxDistance) {
        // The original integer overload adds in float precision before widening.
        return createPath(entity, (double) ((float) x + 0.5F),
                (double) ((float) y + 0.5F), (double) ((float) z + 0.5F), maxDistance);
    }

    private Path createPath(Entity entity, double x, double y, double z, float maxDistance) {
        open.clearPath();
        points.clearMap();
        boolean previousAvoidance = avoidsWater;
        int startY = MathHelper.floor(entity.getEntityBoundingBox().minY + 0.5D);
        if(canSwim && entity.isInWater()) {
            startY = (int) entity.getEntityBoundingBox().minY;
            Block block = blockAccess.getBlockState(new BlockPos(MathHelper.floor(entity.posX), startY,
                    MathHelper.floor(entity.posZ))).getBlock();
            while(block == Blocks.FLOWING_WATER || block == Blocks.WATER) {
                ++startY;
                block = blockAccess.getBlockState(new BlockPos(MathHelper.floor(entity.posX), startY,
                        MathHelper.floor(entity.posZ))).getBlock();
            }
            previousAvoidance = avoidsWater;
            avoidsWater = false;
        } else {
            startY = MathHelper.floor(entity.getEntityBoundingBox().minY + 0.5D);
        }
        PathPoint start = openPoint(MathHelper.floor(entity.getEntityBoundingBox().minX), startY,
                MathHelper.floor(entity.getEntityBoundingBox().minZ));
        PathPoint target = openPoint(MathHelper.floor(x - (double) (entity.width / 2.0F)),
                MathHelper.floor(y), MathHelper.floor(z - (double) (entity.width / 2.0F)));
        PathPoint size = new PathPoint(MathHelper.floor(entity.width + 1.0F),
                MathHelper.floor(entity.height + 1.0F), MathHelper.floor(entity.width + 1.0F));
        Path result = search(entity, start, target, size, maxDistance);
        avoidsWater = previousAvoidance;
        return result;
    }

    private Path search(Entity entity, PathPoint start, PathPoint target, PathPoint size, float maxDistance) {
        start.totalPathDistance = 0.0F;
        start.distanceToNext = start.distanceToSquared(target);
        start.distanceToTarget = start.distanceToNext;
        open.clearPath();
        open.addPoint(start);
        PathPoint closest = start;
        while(!open.isPathEmpty()) {
            PathPoint current = open.dequeue();
            if(current.equals(target)) return buildPath(target);
            if(current.distanceToSquared(target) < closest.distanceToSquared(target)) closest = current;
            current.visited = true;
            int count = findOptions(entity, current, size, target, maxDistance);
            for(int i = 0; i < count; ++i) {
                PathPoint next = options[i];
                float distance = current.totalPathDistance + current.distanceToSquared(next);
                if(!next.isAssigned() || distance < next.totalPathDistance) {
                    next.previous = current;
                    next.totalPathDistance = distance;
                    next.distanceToNext = next.distanceToSquared(target);
                    if(next.isAssigned()) {
                        open.changeDistance(next, next.totalPathDistance + next.distanceToNext);
                    } else {
                        next.distanceToTarget = next.totalPathDistance + next.distanceToNext;
                        open.addPoint(next);
                    }
                }
            }
        }
        return closest == start ? null : buildPath(closest);
    }

    private int findOptions(Entity entity, PathPoint current, PathPoint size, PathPoint target, float maxDistance) {
        int count = 0;
        int step = collision(entity, current.x, current.y + 1, current.z, size) == 1 ? 1 : 0;
        // Preserve both evaluation and insertion order, including callback order.
        PathPoint south = safePoint(entity, current.x, current.y, current.z + 1, size, step);
        PathPoint west = safePoint(entity, current.x - 1, current.y, current.z, size, step);
        PathPoint east = safePoint(entity, current.x + 1, current.y, current.z, size, step);
        PathPoint north = safePoint(entity, current.x, current.y, current.z - 1, size, step);
        if(south != null && !south.visited && south.distanceTo(target) < maxDistance) options[count++] = south;
        if(west != null && !west.visited && west.distanceTo(target) < maxDistance) options[count++] = west;
        if(east != null && !east.visited && east.distanceTo(target) < maxDistance) options[count++] = east;
        if(north != null && !north.visited && north.distanceTo(target) < maxDistance) options[count++] = north;
        return count;
    }

    private PathPoint safePoint(Entity entity, int x, int y, int z, PathPoint size, int step) {
        PathPoint point = null;
        int type = collision(entity, x, y, z, size);
        if(type == 2) return openPoint(x, y, z);
        if(type == 1) point = openPoint(x, y, z);
        if(point == null && step > 0 && type != -3 && type != -4
                && collision(entity, x, y + step, z, size) == 1) {
            point = openPoint(x, y + step, z);
            y += step;
        }
        if(point != null) {
            int fallen = 0;
            int below = 0;
            while(y > 0) {
                below = collision(entity, x, y - 1, z, size);
                if(avoidsWater && below == -1) return null;
                if(below != 1) break;
                if(fallen++ >= entity.getMaxFallHeight()) return null;
                --y;
                if(y > 0) point = openPoint(x, y, z);
            }
            if(below == -2) return null;
        }
        return point;
    }

    private PathPoint openPoint(int x, int y, int z) {
        // Preserve the original packed-key aliasing, not a new coordinate map.
        int key = PathPoint.makeHash(x, y, z);
        PathPoint point = points.lookup(key);
        if(point == null) {
            point = new PathPoint(x, y, z);
            points.addKey(key, point);
        }
        return point;
    }

    private int collision(Entity entity, int x, int y, int z, PathPoint size) {
        boolean special = false;
        // The original collision routine reads the entity's live world, NOT the
        // ChunkCache used by the initial water-surface scan.
        World world = entity.world;
        for(int bx = x; bx < x + size.x; ++bx) {
            for(int by = y; by < y + size.y; ++by) {
                for(int bz = z; bz < z + size.z; ++bz) {
                    BlockPos pos = new BlockPos(bx, by, bz);
                    IBlockState state = world.getBlockState(pos);
                    Block block = state.getBlock();
                    if(state.getMaterial() == Material.AIR) continue;
                    if(block == Blocks.TRAPDOOR) {
                        special = true;
                    } else if(block == Blocks.FLOWING_WATER || block == Blocks.WATER) {
                        if(avoidsWater) return -1;
                        special = true;
                    } else if(!allowDoors && block == Blocks.OAK_DOOR) {
                        return 0;
                    }
                    // Original render type 9 was rail. Keep the second world
                    // read and the two ordered source-position checks.
                    if(world.getBlockState(pos).getBlock() instanceof BlockRailBase) {
                        int ex = MathHelper.floor(entity.posX);
                        int ey = MathHelper.floor(entity.posY);
                        int ez = MathHelper.floor(entity.posZ);
                        if(world.getBlockState(new BlockPos(ex, ey, ez)).getBlock() instanceof BlockRailBase
                                || world.getBlockState(new BlockPos(ex, ey - 1, ez)).getBlock() instanceof BlockRailBase) continue;
                        return -3;
                    }
                    // These two vanilla callbacks changed between versions.
                    // Keep the old path decision locally, not global collisions:
                    // snow layers were passable at every thickness; cauldrons
                    // inherited the solid-material rejection.
                    if(block == Blocks.SNOW_LAYER || block != Blocks.CAULDRON && block.isPassable(world, pos)) continue;
                    if(allowBlocked && block == Blocks.OAK_DOOR) continue;
                    // The NTM metal fence used a custom render ID, NOT vanilla
                    // type 11. Treating every BlockFence as -3 changes step-up.
                    if(block instanceof BlockFence && !(block instanceof BlockMetalFence)
                            || block == Blocks.OAK_FENCE_GATE || block instanceof BlockWall) return -3;
                    if(block == Blocks.TRAPDOOR) return -4;
                    if(state.getMaterial() == Material.LAVA) {
                        if(entity.isInLava()) continue;
                        return -2;
                    }
                    return 0;
                }
            }
        }
        return special ? 2 : 1;
    }

    /** Original wrapper support test; end-portal frames ceased being normal
     * cubes in 1.12, but the old partial-target ground search accepted them. */
    public static boolean isOriginalNormalCube(IBlockState state) {
        return state.getBlock() == Blocks.END_PORTAL_FRAME || state.isNormalCube();
    }

    private Path buildPath(PathPoint end) {
        int length = 1;
        for(PathPoint point = end; point.previous != null; point = point.previous) ++length;
        PathPoint[] route = new PathPoint[length];
        PathPoint point = end;
        route[--length] = point;
        while(point.previous != null) {
            point = point.previous;
            route[--length] = point;
        }
        return new Path(route);
    }
}
