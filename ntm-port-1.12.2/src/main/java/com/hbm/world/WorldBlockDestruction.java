package com.hbm.world;

import java.util.Objects;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** Native destruction within the original World coordinate domain. */
public final class WorldBlockDestruction {
    private WorldBlockDestruction() { }

    public static boolean destroy(World world, int x, int y, int z, boolean dropBlock) {
        Objects.requireNonNull(world);
        // Original World.getBlock rejected these coordinates before chunk access.
        if (x < -30000000 || x >= 30000000 || z < -30000000 || z >= 30000000
                || y < 0 || y >= 256) return false;
        return world.destroyBlock(new BlockPos(x, y, z), dropBlock);
    }
}
