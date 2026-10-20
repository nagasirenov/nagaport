package com.hbm.blocks.network;

import com.hbm.tileentity.network.TileEntityCableBaseNT;
import com.hbm.tileentity.network.TileEntityPipeBaseNT;
import com.hbm.tileentity.network.TileEntityPipeExhaust;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

/** Exact owned registry/class boundaries; shared network tiles must not claim unrelated blocks. */
public final class BoxDuctRenderState {
    private BoxDuctRenderState() { }
    public static int family(Block block) {
        if(block==null) return -1;
        ResourceLocation name=block.getRegistryName();
        if(name==null || !"hbm".equals(name.getNamespace())) return -1;
        if(block.getClass()==FluidDuctBox.class && name.getPath().equals("tile.fluid_duct_box")) return 0;
        if(block.getClass()==FluidDuctBoxExhaust.class && name.getPath().equals("tile.fluid_duct_exhaust")) return 1;
        if(block.getClass()==PowerCableBox.class && name.getPath().equals("tile.red_cable_box")) return 2;
        return -1;
    }
    public static boolean isNativeBreakingOwner(TileEntity tile,IBlockState state) {
        if(tile==null || state==null) return false;
        int family=family(state.getBlock());
        return family==0&&tile.getClass()==TileEntityPipeBaseNT.class
                || family==1&&tile.getClass()==TileEntityPipeExhaust.class
                || family==2&&tile.getClass()==TileEntityCableBaseNT.class;
    }
}
