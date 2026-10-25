package com.hbm.world;

import java.util.List;
import java.util.Objects;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/** Native states within NTM's original integer-coordinate world domain. */
public final class WorldBlockState {
    private WorldBlockState() {}

    public static IBlockState read(IBlockAccess world, int x, int y, int z) {
        // An instance invocation checked its receiver after evaluating all coordinates,
        // even when those coordinates were outside the accepted world domain.
        Objects.requireNonNull(world);
        if (x < -30000000 || z < -30000000 || x >= 30000000 || z >= 30000000 || y < 0 || y >= 256) {
            return Blocks.AIR.getDefaultState();
        }
        return world.getBlockState(new BlockPos(x, y, z));
    }

    /** Legacy IBlockAccess#getBlockMetadata(x, y, z) semantics for bounded integer coordinates. */
    public static int readMetadata(IBlockAccess world, int x, int y, int z) {
        return metadata(read(world, x, y, z));
    }

    /** Legacy World#markBlockForUpdate(x, y, z) semantics for bounded integer coordinates. */
    public static void markBlockForUpdate(World world, int x, int y, int z) {
        Objects.requireNonNull(world);
        if (x < -30000000 || z < -30000000 || x >= 30000000 || z >= 30000000 || y < 0 || y >= 256) {
            return;
        }
        BlockPos pos = new BlockPos(x, y, z);
        IBlockState state = world.getBlockState(pos);
        world.notifyBlockUpdate(pos, state, state, 3);
    }

    /** Legacy World#setBlockMetadataWithNotify(x, y, z, meta, flags) semantics. */
    public static boolean setBlockMetadataWithNotify(World world, int x, int y, int z, int meta, int flags) {
        Objects.requireNonNull(world);
        if (x < -30000000 || z < -30000000 || x >= 30000000 || z >= 30000000 || y < 0 || y >= 256) {
            return false;
        }
        BlockPos pos = new BlockPos(x, y, z);
        IBlockState state = world.getBlockState(pos);
        return world.setBlockState(pos, state.getBlock().getStateFromMeta(meta), flags);
    }

    /**
     * Write an already resolved native state without expanding the original world domain.
     * Callers supply states whose conversion is total and side-effect-free; this is not
     * a legacy Block/metadata adapter.
     */
    public static boolean write(World world, int x, int y, int z, IBlockState state, int flags) {
        Objects.requireNonNull(world);
        if (x < -30000000 || z < -30000000 || x >= 30000000 || z >= 30000000 || y < 0 || y >= 256) {
            return false;
        }
        return world.setBlockState(new BlockPos(x, y, z), state, flags);
    }

    /** Legacy World#setBlock(x, y, z, block) semantics for bounded integer coordinates. */
    public static void dropBlockAsItem(Block block, World world, int x, int y, int z, int meta, int fortune) {
        BlockPos pos = new BlockPos(x, y, z);
        IBlockState state = block.getStateFromMeta(meta);
        block.dropBlockAsItem(world, pos, state, fortune);
    }

    public static void dropBlockAsItem(Block block, World world, int x, int y, int z, ItemStack stack) {
        Block.spawnAsEntity(world, new BlockPos(x, y, z), stack);
    }

    public static void dropBlockAsItemWithChance(Block block, World world, int x, int y, int z, int meta, float chance, int fortune) {
        BlockPos pos = new BlockPos(x, y, z);
        IBlockState state = block.getStateFromMeta(meta);
        block.dropBlockAsItemWithChance(world, pos, state, chance, fortune);
    }

    public static List<ItemStack> getDrops(Block block, IBlockAccess world, int x, int y, int z, int meta, int fortune) {
        NonNullList<ItemStack> drops = NonNullList.create();
        BlockPos pos = new BlockPos(x, y, z);
        IBlockState state = block.getStateFromMeta(meta);
        block.getDrops(drops, world, pos, state, fortune);
        return drops;
    }
    public static TileEntity getTileEntity(IBlockAccess world, int x, int y, int z) {
        return world.getTileEntity(new BlockPos(x, y, z));
    }

    public static boolean isReplaceableOreGen(Block block, World world, int x, int y, int z, Block target) {
        BlockPos pos = new BlockPos(x, y, z);
        IBlockState state = world.getBlockState(pos);
        return block.isReplaceableOreGen(state, world, pos, net.minecraft.block.state.pattern.BlockMatcher.forBlock(target));
    }


    public static boolean writeLegacyBlock(World world, int x, int y, int z, Block block) {
        return writeLegacyBlock(world, x, y, z, block, 0, 3);
    }

    public static boolean writeLegacyBlock(World world, int x, int y, int z, Block block, int meta) {
        return writeLegacyBlock(world, x, y, z, block, meta, 3);
    }

    /** Legacy World#setBlock(x, y, z, block, meta, flags) semantics. */
    public static boolean writeLegacyBlock(World world, int x, int y, int z, Block block, int meta, int flags) {
        Objects.requireNonNull(world);
        if (x < -30000000 || z < -30000000 || x >= 30000000 || z >= 30000000 || y < 0 || y >= 256) {
            return false;
        }
        return world.setBlockState(new BlockPos(x, y, z), block.getStateFromMeta(meta), flags);
    }

    /** Legacy Block#isAir(world, x, y, z) semantics for bounded integer coordinates. */
    public static boolean isAir(IBlockAccess world, int x, int y, int z) {
        Objects.requireNonNull(world);
        if (x < -30000000 || z < -30000000 || x >= 30000000 || z >= 30000000 || y < 0 || y >= 256) {
            return true;
        }
        BlockPos pos = new BlockPos(x, y, z);
        IBlockState state = world.getBlockState(pos);
        return state.getBlock().isAir(state, world, pos);
    }

    /** Legacy Block#getBlockHardness(world, x, y, z) semantics for bounded integer coordinates. */
    public static float getBlockHardness(World world, int x, int y, int z) {
        Objects.requireNonNull(world);
        if (x < -30000000 || z < -30000000 || x >= 30000000 || z >= 30000000 || y < 0 || y >= 256) {
            return -1.0F;
        }
        BlockPos pos = new BlockPos(x, y, z);
        return world.getBlockState(pos).getBlockHardness(world, pos);
    }

    /** Metadata belongs to the actual state owner, never an assumed caller block. */
    public static int metadata(IBlockState state) {
        return state.getBlock().getMetaFromState(state);
    }
}
