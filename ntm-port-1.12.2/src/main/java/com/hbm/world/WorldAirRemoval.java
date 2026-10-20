package com.hbm.world;

import java.util.Objects;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** Air removal in the original generators' and machines' coordinate domain. */
public final class WorldAirRemoval {
    private WorldAirRemoval() { }

    public static boolean remove(World world, int x, int y, int z) {
        Objects.requireNonNull(world);
        // Native setBlockState no longer rejects the original horizontal bounds.
        // Reject before touching a chunk; retain native removal and its boolean.
        if (x < -30000000 || x >= 30000000 || z < -30000000 || z >= 30000000
                || y < 0 || y >= 256) return false;
        return world.setBlockToAir(new BlockPos(x, y, z));
    }
}
