package com.hbm.tileentity.machine.rbmk;

import net.minecraft.entity.player.EntityPlayer;

/**
 * Base class for all RBMK components that have a GUI
 * @author hbm
 *
 */
public abstract class TileEntityRBMKActiveBase extends TileEntityRBMKBase {
	
	public abstract String getName();

	public boolean isUseableByPlayer(EntityPlayer player) {
		if(this.world.getTileEntity(new net.minecraft.util.math.BlockPos(this.pos.getX(), this.pos.getY(), this.pos.getZ())) != this) {
			return false;
		} else {
			return player.getDistanceSq(this.pos.getX() + 0.5D, this.pos.getY() + 0.5D, this.pos.getZ() + 0.5D) <= 128;
		}
	}
}
