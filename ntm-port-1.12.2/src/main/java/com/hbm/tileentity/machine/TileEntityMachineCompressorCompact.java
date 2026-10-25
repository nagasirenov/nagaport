package com.hbm.tileentity.machine;

import com.hbm.util.fauxpointtwelve.DirPos;

import net.minecraft.util.math.AxisAlignedBB;
import com.hbm.lib.ForgeDirection;

public class TileEntityMachineCompressorCompact extends TileEntityMachineCompressorBase {

	public float fanSpin;
	public float prevFanSpin;

	@Override
	public void update() {
		super.update();

		if(this.world.isRemote) {

			this.prevFanSpin = this.fanSpin;

			if(this.isOn) {
				this.fanSpin += 45;

				if(this.fanSpin >= 360) {
					this.prevFanSpin -= 360;
					this.fanSpin -= 360;
				}
			}
		}
	}

	@Override
	public DirPos[] getConPos() {

		ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10);
		ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

		return new DirPos[] {
				new DirPos(this.pos.getX() + rot.offsetX * 4, this.pos.getY() + 1, this.pos.getZ() + rot.offsetZ * 4, rot),
				new DirPos(this.pos.getX() - rot.offsetX * 4, this.pos.getY() + 1, this.pos.getZ() - rot.offsetZ * 4, rot.getOpposite()),
				new DirPos(this.pos.getX() + dir.offsetX * 2 - rot.offsetX, this.pos.getY() + 1, this.pos.getZ() + dir.offsetZ * 2 - rot.offsetZ, dir),
				new DirPos(this.pos.getX() + dir.offsetX * 2 + rot.offsetX, this.pos.getY() + 1, this.pos.getZ() + dir.offsetZ * 2 + rot.offsetZ, dir),
				new DirPos(this.pos.getX() - dir.offsetX * 2 - rot.offsetX, this.pos.getY() + 1, this.pos.getZ() - dir.offsetZ * 2 - rot.offsetZ, dir.getOpposite()),
				new DirPos(this.pos.getX() - dir.offsetX * 2 + rot.offsetX, this.pos.getY() + 1, this.pos.getZ() - dir.offsetZ * 2 + rot.offsetZ, dir.getOpposite())
		};
	}

	AxisAlignedBB bb = null;

	@Override
	public AxisAlignedBB getRenderBoundingBox() {

		if(bb == null) {
			bb = new AxisAlignedBB(
					this.pos.getX() - 3,
					this.pos.getY(),
					this.pos.getZ() - 3,
					this.pos.getX() + 4,
					this.pos.getY() + 3,
					this.pos.getZ() + 4
					);
		}

		return bb;
	}
}
