package com.hbm.tileentity.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.main.MainRegistry;
import com.hbm.util.fauxpointtwelve.DirPos;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import com.hbm.lib.ForgeDirection;

public class TileEntityMachineCompressor extends TileEntityMachineCompressorBase {
	
	public float fanSpin;
	public float prevFanSpin;
	public float piston;
	public float prevPiston;
	public boolean pistonDir;
	private float randSpeed = 0.1F;

	@Override
	public void update() {
		super.update();

		if(this.world.isRemote) {

			this.prevFanSpin = this.fanSpin;
			this.prevPiston = this.piston;

			if(this.isOn) {
				this.fanSpin += 15;

				if(this.fanSpin >= 360) {
					this.prevFanSpin -= 360;
					this.fanSpin -= 360;
				}

				if(this.pistonDir) {
					this.piston -= randSpeed;
					if(this.piston <= 0) {
						MainRegistry.proxy.playSoundClient(this.pos.getX(), this.pos.getY(), this.pos.getZ(), "hbm:item.boltgun", this.getVolume(0.5F), 0.75F);
						this.pistonDir = !this.pistonDir;
					}
				} else {
					this.piston += 0.05F;
					if(this.piston >= 1) {
						this.randSpeed = 0.085F + this.world.rand.nextFloat() * 0.03F;
						this.pistonDir = !this.pistonDir;
					}
				}

				this.piston = MathHelper.clamp(this.piston, 0F, 1F);
			}
		}
	}

	@Override
	public DirPos[] getConPos() {
		ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset);
		ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

		return new DirPos[] {
				new DirPos(this.pos.getX() + rot.offsetX * 2, this.pos.getY(), this.pos.getZ() + rot.offsetZ * 2, rot),
				new DirPos(this.pos.getX() - rot.offsetX * 2, this.pos.getY(), this.pos.getZ() - rot.offsetZ * 2, rot.getOpposite()),
				new DirPos(this.pos.getX() - dir.offsetX * 2, this.pos.getY(), this.pos.getZ() - dir.offsetZ * 2, dir.getOpposite()),
		};
	}

	AxisAlignedBB bb = null;

	@Override
	public AxisAlignedBB getRenderBoundingBox() {

		if(bb == null) {
			bb = AxisAlignedBB.getBoundingBox(
					this.pos.getX() - 2,
					this.pos.getY(),
					this.pos.getZ() - 2,
					this.pos.getX() + 3,
					this.pos.getY() + 9,
					this.pos.getZ() + 3
					);
		}

		return bb;
	}
}
