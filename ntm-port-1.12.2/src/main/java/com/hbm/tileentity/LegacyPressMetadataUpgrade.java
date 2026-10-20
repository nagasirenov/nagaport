package com.hbm.tileentity;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** Per-tile lifecycle scope used only by the two legacy press promotion callbacks. */
public final class LegacyPressMetadataUpgrade {

    private Expected expected;

    public void run(TileEntity tile, Block owner, World world, BlockPos pos, Runnable promotion) {
        // A reentrant scope must not borrow or replace an unconsumed outer permission.
        if (expected != null) {
            expected = null;
            promotion.run();
            return;
        }

        Expected attempt = null;
        if (world != null && tile != null && !tile.isInvalid()
                && tile.getWorld() == world && tile.getPos().equals(pos)
                && world.getTileEntity(pos) == tile) {
            IBlockState before = world.getBlockState(pos);
            if (before.getBlock() == owner) {
                int metadata = owner.getMetaFromState(before);
                if (metadata >= 0 && metadata < 16) {
                    // Match the existing owner's codec, including wrapped extra metadata.
                    IBlockState after = owner.getStateFromMeta(metadata + 10);
                    attempt = new Expected(tile, owner, world, pos.toImmutable(), before, after);
                }
            }
        }

        expected = attempt;
        try {
            promotion.run();
        } finally {
            // Also clear on a rejected/no-op write or an exception before shouldRefresh.
            expected = null;
        }
    }

    public boolean consume(TileEntity tile, World world, BlockPos pos, IBlockState before, IBlockState after) {
        Expected attempt = expected;
        // Consume before any callback/notification can perform a second write.
        expected = null;
        return attempt != null && attempt.thread == Thread.currentThread()
                && attempt.tile == tile && attempt.world == world && attempt.pos.equals(pos)
                && attempt.before == before && attempt.after == after
                && before.getBlock() == attempt.owner && after.getBlock() == attempt.owner
                && !tile.isInvalid() && tile.getWorld() == world && tile.getPos().equals(pos);
    }

    private static final class Expected {
        final TileEntity tile;
        final Block owner;
        final World world;
        final BlockPos pos;
        final IBlockState before;
        final IBlockState after;
        final Thread thread;

        Expected(TileEntity tile, Block owner, World world, BlockPos pos, IBlockState before, IBlockState after) {
            this.tile = tile;
            this.owner = owner;
            this.world = world;
            this.pos = pos;
            this.before = before;
            this.after = after;
            this.thread = Thread.currentThread();
        }
    }
}
