package com.hbm.render.block;

import java.util.List;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;

/** Common-side shape contract consumed by native 1.12 baked models. */
public interface ISBRHUniversal {
    List<AxisAlignedBB> getRenderBoxes(IBlockState state, boolean inventory);
    default double getInventoryYOffset(IBlockState state) { return 0; }
    default boolean renderAllFaces() { return true; }
}
