package com.hbm.blocks;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

/** Native layered-cube contract. Pass selection belongs to each model request, never static state. */
public interface IBlockMultiPass {
    int getPasses();
    default boolean shouldRenderItemMulti() { return false; }
    default MultipassContext getMultipassContext(IBlockState state, IBlockAccess world, BlockPos pos) {
        return MultipassContext.basic(state.getBlock().getMetaFromState(state));
    }
}
