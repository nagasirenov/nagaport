package com.hbm.blocks.machine;

import com.hbm.tileentity.machine.TileEntityMicrowave;

import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class MachineMicrowave extends BlockMachineBase {

	public MachineMicrowave(Material mat) {
		super(mat, 0);
		this.rotatable = true;
	}

	@Override
	public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) {
		return new TileEntityMicrowave();
	}
	
	@Override
	public net.minecraft.util.EnumBlockRenderType getRenderType(net.minecraft.block.state.IBlockState state) {
		return net.minecraft.util.EnumBlockRenderType.INVISIBLE;
	}
	
	@Override
	public boolean isOpaqueCube(net.minecraft.block.state.IBlockState state) {
		return false;
	}
	
	@Override
	public boolean isFullCube(net.minecraft.block.state.IBlockState state) {
		return false;
	}
}
