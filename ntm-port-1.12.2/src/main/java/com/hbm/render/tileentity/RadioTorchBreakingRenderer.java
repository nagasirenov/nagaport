package com.hbm.render.tileentity;

import com.hbm.blocks.network.RadioTorchBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;

/** Original RTTY damage pass retained its own icons and UVs; no destroy-stage retexture. */
public class RadioTorchBreakingRenderer<T extends TileEntity> extends NetworkBreakingRenderer<T> {
    public RadioTorchBreakingRenderer() { super(false); }
    @Override public boolean owns(TileEntity tile, IBlockState state) {
        return RadioTorchBase.isNativeBreakingOwner(tile, state);
    }
}
