package com.hbm.blocks.machine;

import com.hbm.tileentity.machine.TileEntityHatch;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockHatch extends BlockContainer {
	// 1.7.10 type 0 cube; BlockContainer would make it INVISIBLE in 1.12 (render-type parity audit).
	@Override
	public net.minecraft.util.EnumBlockRenderType getRenderType(net.minecraft.block.state.IBlockState state) {
		return net.minecraft.util.EnumBlockRenderType.MODEL;
	}


	public BlockHatch(Material p_i45386_1_) {
		super(p_i45386_1_);
	}

	@Override
	public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) {
		return new TileEntityHatch();
	}
	
	public void setControllerPos(World world, int x, int y, int z, int cX, int cY, int cZ) {
		
		TileEntity te = world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));
		
		if(te != null && te instanceof TileEntityHatch) {
			((TileEntityHatch)te).setControllerPos(cX, cY, cZ);
		}
		
	}

}
