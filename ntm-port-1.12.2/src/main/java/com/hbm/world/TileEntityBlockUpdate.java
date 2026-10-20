package com.hbm.world;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** Publishes an NTM tile-only change at an already loaded, unchanged block. */
public final class TileEntityBlockUpdate {
    private TileEntityBlockUpdate() {}

    /**
     * The owning interaction has already read this block and obtained its tile.
     * Publish the actual stored state twice: paint lives in the tile, not metadata.
     * This is not a block write, neighbor notification, or unloaded-world API.
     */
    public static void notifyAt(World world, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        IBlockState state = world.getBlockState(pos);
        world.notifyBlockUpdate(pos, state, state, 2);
    }
}
