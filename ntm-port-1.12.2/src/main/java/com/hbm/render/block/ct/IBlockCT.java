package com.hbm.render.block.ct;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.IBlockAccess;

/** Connectivity belongs to the owner; models receive only an immutable result. */
public interface IBlockCT {
    default boolean canConnect(IBlockAccess world, int x, int y, int z, Block neighbor) { return this == neighbor; }
    default int getCTMaterial(IBlockState state) { return 0; }
}
