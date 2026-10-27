package com.hbm.blocks.machine;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.world.IBlockAccess;

public class BlockCMGlass extends BlockCM {

	public BlockCMGlass(Material mat, Class<? extends Enum> theEnum, boolean multiName, boolean multiTexture) {
		super(mat, theEnum, multiName, multiTexture);
	}

	@Override
	public boolean isOpaqueCube(net.minecraft.block.state.IBlockState state) {
		return false;
	}

	@Override
	public boolean isFullCube(net.minecraft.block.state.IBlockState state) {
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean shouldSideBeRendered(net.minecraft.block.state.IBlockState state, IBlockAccess world, net.minecraft.util.math.BlockPos pos, net.minecraft.util.EnumFacing side) {
		net.minecraft.block.state.IBlockState neighbor = world.getBlockState(pos.offset(side));
		return neighbor.getBlock() == this ? false : super.shouldSideBeRendered(state, world, pos, side);
	}

	@Override
	@net.minecraftforge.fml.relauncher.SideOnly(net.minecraftforge.fml.relauncher.Side.CLIENT)
	public net.minecraft.util.BlockRenderLayer getRenderLayer() {
		return net.minecraft.util.BlockRenderLayer.CUTOUT;
	}
}
