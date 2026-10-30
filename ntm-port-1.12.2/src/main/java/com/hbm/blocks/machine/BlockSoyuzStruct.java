package com.hbm.blocks.machine;

import com.hbm.tileentity.machine.TileEntitySoyuzStruct;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockSoyuzStruct extends BlockContainer {
	// TESR ghosts never cover the core cell; 1.7.10 type 0 cube body stays.
	@Override
	public net.minecraft.util.EnumBlockRenderType getRenderType(net.minecraft.block.state.IBlockState state) {
		return net.minecraft.util.EnumBlockRenderType.MODEL;
	}


	public BlockSoyuzStruct(Material mat) {
		super(mat);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntitySoyuzStruct();
	}
	
    public boolean isOpaqueCube(net.minecraft.block.state.IBlockState state) {
    	
        return false;
    }
}
