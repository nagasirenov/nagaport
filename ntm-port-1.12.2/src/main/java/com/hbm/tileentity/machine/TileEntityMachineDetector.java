package com.hbm.tileentity.machine;

import com.hbm.tileentity.TileEntityLoadedBase;

import api.hbm.energymk2.IEnergyReceiverMK2;
import com.hbm.lib.ForgeDirection;

public class TileEntityMachineDetector extends TileEntityLoadedBase implements IEnergyReceiverMK2, net.minecraft.util.ITickable {

	long power;

	@Override
	public void update() {

		if(!this.world.isRemote) {

			this.updateConnections();

			int meta = this.getBlockMetadata();
			int state = 0;

			if(power > 0) {
				state = 1;
				power--;
			}

			if(meta != state) {
				com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ(), state, 3);
				this.markDirty();
			}
		}
	}

	private void updateConnections() {

		for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
			this.trySubscribe(this.world, this.pos.getX() + dir.offsetX, this.pos.getY() + dir.offsetY, this.pos.getZ() + dir.offsetZ, dir);
	}

	@Override
	public void setPower(long i) {
		power = i;
	}

	@Override
	public long getPower() {
		return power;
	}

	@Override
	public long getMaxPower() {
		return 5;
	}

	@Override
	public ConnectionPriority getPriority() {
		return ConnectionPriority.HIGH;
	}
}
