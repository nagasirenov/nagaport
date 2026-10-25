package com.hbm.blocks.machine.rbmk;

import com.hbm.handler.BossSpawnHandler;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.rbmk.TileEntityRBMKStorage;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class RBMKStorage extends RBMKBase {

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {

		if(meta >= this.offset)
			return new TileEntityRBMKStorage();

		return new TileEntityProxyCombo(true, false, false);
	}

	@Override
	public int getRenderType(){
		return this.renderIDPassive;
	}

	@Override
	public boolean onBlockActivated(World world, net.minecraft.util.math.BlockPos nativePos, net.minecraft.block.state.IBlockState nativeState, EntityPlayer player, net.minecraft.util.EnumHand nativeHand, net.minecraft.util.EnumFacing nativeFacing, float hitX, float hitY, float hitZ) {
		int x = nativePos.getX(), y = nativePos.getY(), z = nativePos.getZ();
		int side = nativeFacing != null ? nativeFacing.getIndex() : 0;

		BossSpawnHandler.markFBI(player);
		return openInv(world, x, y, z, player);
	}
}
