package com.hbm.blocks.network;

import com.hbm.blocks.ITooltipProvider;
import com.hbm.tileentity.network.TileEntityPylonBase;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public abstract class PylonBase extends BlockContainer implements ITooltipProvider {

	protected PylonBase(Material p_i45386_1_) {
		super(p_i45386_1_);
	}

	@Override
	public void breakBlock(World world, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState state) {

		TileEntity te = world.getTileEntity(pos);

		if(te instanceof TileEntityPylonBase) {
			((TileEntityPylonBase)te).disconnectAll();
		}

		super.breakBlock(world, pos, state);
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

	@Override
	public boolean onBlockActivated(World world, net.minecraft.util.math.BlockPos nativePos, net.minecraft.block.state.IBlockState nativeState, EntityPlayer player, net.minecraft.util.EnumHand nativeHand, net.minecraft.util.EnumFacing nativeFacing, float hitX, float hitY, float hitZ) {
		int x = nativePos.getX(), y = nativePos.getY(), z = nativePos.getZ();
		int side = nativeFacing != null ? nativeFacing.getIndex() : 0;

		if(world.isRemote) {
			return true;
		} else if(!player.isSneaking()) {
			TileEntityPylonBase te = (TileEntityPylonBase) world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));
			return te.setColor(com.hbm.util.NativeHeldItem.getMainHandNullable(player));
		} else {
			return false;
		}
	}
}
