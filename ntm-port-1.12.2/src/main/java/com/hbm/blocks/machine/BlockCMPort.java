package com.hbm.blocks.machine;

import com.hbm.tileentity.TileEntityProxyCombo;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockCMPort extends BlockCM implements ITileEntityProvider {

	public BlockCMPort(Material mat, Class<? extends Enum> theEnum, boolean multiName, boolean multiTexture) {
		super(mat, theEnum, multiName, multiTexture);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityProxyCombo().inventory().power().fluid();
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		super.breakBlock(world, pos, state);
		world.removeTileEntity(pos);
	}
}
