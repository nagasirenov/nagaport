package com.hbm.blocks.network;

import com.hbm.render.block.ct.CTContext;
import com.hbm.render.block.ct.IBlockCT;
import com.hbm.tileentity.network.TileEntityCableBaseNT;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class WireCoated extends BlockContainer implements IBlockCT {

	public WireCoated(Material p_i45386_1_) {
		super(p_i45386_1_);
        setDefaultState(blockState.getBaseState().withProperty(CTContext.METADATA, 0));
	}

	@Override
	public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) {
		return new TileEntityCableBaseNT();
	}

    @Override protected net.minecraft.block.state.BlockStateContainer createBlockState() { return CTContext.stateContainer(this); }
    @Override public net.minecraft.block.state.IBlockState getStateFromMeta(int metadata) { return getDefaultState().withProperty(CTContext.METADATA, metadata & 15); }
    @Override public int getMetaFromState(net.minecraft.block.state.IBlockState state) { return state.getValue(CTContext.METADATA); }
    @Override public net.minecraft.util.EnumBlockRenderType getRenderType(net.minecraft.block.state.IBlockState state) { return net.minecraft.util.EnumBlockRenderType.MODEL; }
    @Override public net.minecraft.block.state.IBlockState getExtendedState(net.minecraft.block.state.IBlockState state, IBlockAccess world, net.minecraft.util.math.BlockPos pos) {
        return CTContext.extend(state, world, pos, this);
    }


}
