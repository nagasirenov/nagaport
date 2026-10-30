package com.hbm.blocks.machine;

import com.hbm.tileentity.machine.TileEntityFusionTorusStruct;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockFusionTorusStruct extends BlockContainer {
	// 1.7.10 type 0 cube; BlockContainer would make it INVISIBLE in 1.12 (render-type parity audit).
	@Override
	public net.minecraft.util.EnumBlockRenderType getRenderType(net.minecraft.block.state.IBlockState state) {
		return net.minecraft.util.EnumBlockRenderType.MODEL;
	}


	public BlockFusionTorusStruct(Material mat) { super(mat); }

	@Override public TileEntity createNewTileEntity(World world, int meta) { return new TileEntityFusionTorusStruct(); }
	@Override public boolean isOpaqueCube(net.minecraft.block.state.IBlockState state) { return false; }
}
