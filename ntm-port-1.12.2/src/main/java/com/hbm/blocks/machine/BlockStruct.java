package com.hbm.blocks.machine;

import com.hbm.tileentity.machine.TileEntityMultiblock;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockStruct extends BlockContainer {
	// TESR ghosts skip the core cell; 1.7.10 type 0 cube body stays.
	@Override
	public net.minecraft.util.EnumBlockRenderType getRenderType(net.minecraft.block.state.IBlockState state) {
		return net.minecraft.util.EnumBlockRenderType.MODEL;
	}


	public BlockStruct(Material p_i45386_1_) {
		super(p_i45386_1_);
	}

	@Override
	public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) {
		return new TileEntityMultiblock();
	}
	
    public boolean isOpaqueCube(net.minecraft.block.state.IBlockState state) {
    	
        return false;
    }

}
