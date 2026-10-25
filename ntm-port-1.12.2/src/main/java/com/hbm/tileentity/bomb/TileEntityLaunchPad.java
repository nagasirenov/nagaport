package com.hbm.tileentity.bomb;

import java.util.List;

import com.hbm.entity.missile.EntityMissileBaseNT;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.util.fauxpointtwelve.DirPos;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import com.hbm.lib.ForgeDirection;

public class TileEntityLaunchPad extends TileEntityLaunchPadBase {

	@Override public boolean isReadyForLaunch() { return delay <= 0; }
	@Override public double getLaunchOffset() { return 1D; }

	public int delay = 0;

	@Override
	public void update() {

		if(!this.world.isRemote) {

			if(this.delay > 0) delay--;

			if(!this.isMissileValid() || !this.hasFuel()) {
				this.delay = 100;
			}

			if(!this.hasFuel() || !this.isMissileValid()) {
				this.state = this.STATE_MISSING;
			} else {
				if(this.delay > 0) {
					this.state = this.STATE_LOADING;
				} else {
					this.state = this.STATE_READY;
				}
			}

		} else {

			List<EntityMissileBaseNT> entities = this.world.getEntitiesWithinAABB(EntityMissileBaseNT.class, new AxisAlignedBB(this.pos.getX() - 0.5, this.pos.getY(), this.pos.getZ() - 0.5, this.pos.getX() + 1.5, this.pos.getY() + 10, this.pos.getZ() + 1.5));

			if(!entities.isEmpty()) {
				for(int i = 0; i < 15; i++) {

					ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10);
					if(this.world.rand.nextBoolean()) dir = dir.getOpposite();
					if(this.world.rand.nextBoolean()) dir = dir.getRotation(ForgeDirection.UP);
					float moX = (float) (this.world.rand.nextGaussian() * 0.15F + 0.75) * dir.offsetX;
					float moZ = (float) (this.world.rand.nextGaussian() * 0.15F + 0.75) * dir.offsetZ;

					NBTTagCompound data = new NBTTagCompound();
					data.setDouble("posX", this.pos.getX() + 0.5);
					data.setDouble("posY", this.pos.getY() + 0.25);
					data.setDouble("posZ", this.pos.getZ() + 0.5);
					data.setString("type", "launchSmoke");
					data.setDouble("moX", moX);
					data.setDouble("moY", 0);
					data.setDouble("moZ", moZ);
					MainRegistry.proxy.effectNT(data);
				}
			}
		}

		super.update();
	}

	@Override
	public void finalizeLaunch(Entity missile) {
		super.finalizeLaunch(missile);
		this.delay = 100;
	}

	@Override
	public DirPos[] getConPos() {
		return new DirPos[] {
				new DirPos(this.pos.getX() + 2, this.pos.getY(), this.pos.getZ() - 1, Library.POS_X),
				new DirPos(this.pos.getX() + 2, this.pos.getY(), this.pos.getZ() + 1, Library.POS_X),
				new DirPos(this.pos.getX() - 2, this.pos.getY(), this.pos.getZ() - 1, Library.NEG_X),
				new DirPos(this.pos.getX() - 2, this.pos.getY(), this.pos.getZ() + 1, Library.NEG_X),
				new DirPos(this.pos.getX() - 1, this.pos.getY(), this.pos.getZ() + 2, Library.POS_Z),
				new DirPos(this.pos.getX() + 1, this.pos.getY(), this.pos.getZ() + 2, Library.POS_Z),
				new DirPos(this.pos.getX() - 1, this.pos.getY(), this.pos.getZ() - 2, Library.NEG_Z),
				new DirPos(this.pos.getX() + 1, this.pos.getY(), this.pos.getZ() - 2, Library.NEG_Z)
		};
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.delay = nbt.getInteger("delay");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setInteger("delay", delay);
		return nbt;
	}

	AxisAlignedBB bb = null;

	@Override
	public AxisAlignedBB getRenderBoundingBox() {

		if(bb == null) {
			bb = new AxisAlignedBB(
					this.pos.getX() - 2,
					this.pos.getY(),
					this.pos.getZ() - 2,
					this.pos.getX() + 3,
					this.pos.getY() + 15,
					this.pos.getZ() + 3
					);
		}

		return bb;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}
}
