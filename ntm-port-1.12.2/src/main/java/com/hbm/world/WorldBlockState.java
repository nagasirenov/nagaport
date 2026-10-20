package com.hbm.world;

import java.util.Objects;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** Native state reads within NTM's original integer-coordinate world domain. */
public final class WorldBlockState {
    private WorldBlockState() {}

    public static IBlockState read(World world, int x, int y, int z) {
        // An instance invocation checked its receiver after evaluating all coordinates,
        // even when those coordinates were outside the accepted world domain.
        Objects.requireNonNull(world);
        if (x < -30000000 || z < -30000000 || x >= 30000000 || z >= 30000000 || y < 0 || y >= 256) {
            return Blocks.AIR.getDefaultState();
        }
        return world.getBlockState(new BlockPos(x, y, z));
    }

    /** Metadata belongs to the actual state owner, never an assumed caller block. */
    public static int metadata(IBlockState state) {
        return state.getBlock().getMetaFromState(state);
    }
}
