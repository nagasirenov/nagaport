package com.hbm.render.tileentity;

import com.hbm.blocks.network.CraneSplitter;
import com.hbm.tileentity.network.TileEntityCraneSplitter;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;

/** Original splitter damage pass retained its own atlas icons, not destroy-stage replacement UVs. */
public final class SplitterBreakingRenderer extends NetworkBreakingRenderer<TileEntityCraneSplitter> {
    public SplitterBreakingRenderer() { super(false); }
    @Override public boolean owns(TileEntity tile, IBlockState state) {
        return tile != null && tile.getClass() == TileEntityCraneSplitter.class && state != null
                && CraneSplitter.isNativeModelOwner(state.getBlock());
    }
}
