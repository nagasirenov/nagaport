package com.hbm.blocks.generic;

import com.hbm.render.block.ct.CTContext;
import com.hbm.render.block.ct.IBlockCT;

import net.minecraft.block.material.Material;
import net.minecraft.world.IBlockAccess;

public class BlockNTMGlassCT extends BlockNTMGlass implements IBlockCT {

	@Override
	public net.minecraft.block.Block setSoundType(net.minecraft.block.SoundType sound) {
		return super.setSoundType(sound);
	}

	public BlockNTMGlassCT(int layer, String name, Material material) {
		super(layer, name, material);
	}

	public BlockNTMGlassCT(int layer, String name, Material material, boolean doesDrop) {
		super(layer, name, material, doesDrop);
	}

    @Override protected net.minecraft.block.state.BlockStateContainer createBlockState() { return CTContext.stateContainer(this); }
    @Override public net.minecraft.block.state.IBlockState getStateFromMeta(int metadata) { return getDefaultState().withProperty(CTContext.METADATA, metadata & 15); }
    @Override public int getMetaFromState(net.minecraft.block.state.IBlockState state) { return state.getValue(CTContext.METADATA); }
    @Override public net.minecraft.util.EnumBlockRenderType getRenderType(net.minecraft.block.state.IBlockState state) { return net.minecraft.util.EnumBlockRenderType.MODEL; }
    @Override public net.minecraft.block.state.IBlockState getExtendedState(net.minecraft.block.state.IBlockState state, IBlockAccess world, net.minecraft.util.math.BlockPos pos) {
        return CTContext.extend(state, world, pos, this);
    }



	@Override
	@net.minecraftforge.fml.relauncher.SideOnly(net.minecraftforge.fml.relauncher.Side.CLIENT)
	public net.minecraft.util.BlockRenderLayer getRenderLayer() {
		return net.minecraft.util.BlockRenderLayer.CUTOUT;
	}
}
