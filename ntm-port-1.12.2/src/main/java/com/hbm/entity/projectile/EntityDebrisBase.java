package com.hbm.entity.projectile;

import java.util.List;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.ReportedException;
import net.minecraft.world.World;

abstract public class EntityDebrisBase extends Entity {

	protected static final DataParameter<Integer> DEBRIS_TYPE = EntityDataManager.createKey(EntityDebrisBase.class, DataSerializers.VARINT);

	public float rot;
	public float lastRot;
	protected boolean hasSizeSet = false;

	public EntityDebrisBase(World world) {
		super(world);
	}

	@Override
	protected void entityInit() {
		this.dataManager.register(DEBRIS_TYPE, 0);
		this.rot = this.lastRot = this.rand.nextFloat() * 360;
	}

	@Override
	public boolean canBeCollidedWith() {
		return true;
	}

	abstract public boolean interactFirst(EntityPlayer player);

	abstract protected int getLifetime();

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbt) {
		this.dataManager.set(DEBRIS_TYPE, nbt.getInteger("debtype"));
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbt) {
		nbt.setInteger("debtype", this.dataManager.get(DEBRIS_TYPE));
	}

	@Override
	public void moveEntity(double moX, double moY, double moZ) {

		this.world.profiler.startSection("move");
		this.ySize *= 0.4F;

		if(this.isInWeb) {
			this.isInWeb = false;
		}

		double initMoX = moX;
		double initMoY = moY;
		double initMoZ = moZ;
		AxisAlignedBB axisalignedbb = this.getEntityBoundingBox().copy();

		List list = this.world.getCollidingBoundingBoxes(this, this.getEntityBoundingBox().addCoord(moX, moY, moZ));

		for(int i = 0; i < list.size(); ++i) {
			moY = ((AxisAlignedBB) list.get(i)).calculateYOffset(this.getEntityBoundingBox(), moY);
		}

		this.getEntityBoundingBox().offset(0.0D, moY, 0.0D);

		if(!this.field_70135_K && initMoY != moY) {
			moZ = 0.0D;
			moY = 0.0D;
			moX = 0.0D;
		}

		boolean isGoingDown = this.onGround || initMoY != moY && initMoY < 0.0D;
		int j;

		for(j = 0; j < list.size(); ++j) {
			moX = ((AxisAlignedBB) list.get(j)).calculateXOffset(this.getEntityBoundingBox(), moX);
		}

		this.getEntityBoundingBox().offset(moX, 0.0D, 0.0D);

		if(!this.field_70135_K && initMoX != moX) {
			moZ = 0.0D;
			moY = 0.0D;
			moX = 0.0D;
		}

		for(j = 0; j < list.size(); ++j) {
			moZ = ((AxisAlignedBB) list.get(j)).calculateZOffset(this.getEntityBoundingBox(), moZ);
		}

		this.getEntityBoundingBox().offset(0.0D, 0.0D, moZ);

		if(!this.field_70135_K && initMoZ != moZ) {
			moZ = 0.0D;
			moY = 0.0D;
			moX = 0.0D;
		}

		double d10;
		double d11;
		int k;
		double d12;

		if(this.stepHeight > 0.0F && isGoingDown && this.ySize < 0.05F && (initMoX != moX || initMoZ != moZ)) {
			d12 = moX;
			d10 = moY;
			d11 = moZ;
			moX = initMoX;
			moY = (double) this.stepHeight;
			moZ = initMoZ;
			AxisAlignedBB axisalignedbb1 = this.getEntityBoundingBox().copy();
			this.getEntityBoundingBox().setBB(axisalignedbb);
			list = this.world.getCollidingBoundingBoxes(this, this.getEntityBoundingBox().addCoord(initMoX, moY, initMoZ));

			for(k = 0; k < list.size(); ++k) {
				moY = ((AxisAlignedBB) list.get(k)).calculateYOffset(this.getEntityBoundingBox(), moY);
			}

			this.getEntityBoundingBox().offset(0.0D, moY, 0.0D);

			if(!this.field_70135_K && initMoY != moY) {
				moZ = 0.0D;
				moY = 0.0D;
				moX = 0.0D;
			}

			for(k = 0; k < list.size(); ++k) {
				moX = ((AxisAlignedBB) list.get(k)).calculateXOffset(this.getEntityBoundingBox(), moX);
			}

			this.getEntityBoundingBox().offset(moX, 0.0D, 0.0D);

			if(!this.field_70135_K && initMoX != moX) {
				moZ = 0.0D;
				moY = 0.0D;
				moX = 0.0D;
			}

			for(k = 0; k < list.size(); ++k) {
				moZ = ((AxisAlignedBB) list.get(k)).calculateZOffset(this.getEntityBoundingBox(), moZ);
			}

			this.getEntityBoundingBox().offset(0.0D, 0.0D, moZ);

			if(!this.field_70135_K && initMoZ != moZ) {
				moZ = 0.0D;
				moY = 0.0D;
				moX = 0.0D;
			}

			if(!this.field_70135_K && initMoY != moY) {
				moZ = 0.0D;
				moY = 0.0D;
				moX = 0.0D;
			} else {
				moY = (double) (-this.stepHeight);

				for(k = 0; k < list.size(); ++k) {
					moY = ((AxisAlignedBB) list.get(k)).calculateYOffset(this.getEntityBoundingBox(), moY);
				}

				this.getEntityBoundingBox().offset(0.0D, moY, 0.0D);
			}

			if(d12 * d12 + d11 * d11 >= moX * moX + moZ * moZ) {
				moX = d12;
				moY = d10;
				moZ = d11;
				this.getEntityBoundingBox().setBB(axisalignedbb1);
			}
		}

		this.world.profiler.endSection();
		this.world.profiler.startSection("rest");
		this.posX = (this.getEntityBoundingBox().minX + this.getEntityBoundingBox().maxX) / 2.0D;
		this.posY = this.getEntityBoundingBox().minY + (double) this.yOffset - (double) this.ySize;
		this.posZ = (this.getEntityBoundingBox().minZ + this.getEntityBoundingBox().maxZ) / 2.0D;
		this.collidedHorizontally = initMoX != moX || initMoZ != moZ;
		this.collidedVertically = initMoY != moY;
		this.onGround = initMoY != moY && initMoY < 0.0D;
		this.collided = this.collidedHorizontally || this.collidedVertically;
		this.updateFallState(moY, this.onGround);

		if(initMoX != moX) {
			//this.motionX = 0.0D;
			this.motionX *= -0.75D;

		}

		if(initMoY != moY) {
			this.motionY = 0.0D;
		}

		if(initMoZ != moZ) {
			//this.motionZ = 0.0D;
			this.motionZ *= -0.75D;
		}

		try {
			this.func_145775_I();
		} catch(Throwable throwable) {
			CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Checking entity block collision");
			CrashReportCategory crashreportcategory = crashreport.makeCategory("Entity being checked for collision");
			this.addEntityCrashInfo(crashreportcategory);
			throw new ReportedException(crashreport);
		}

		this.world.profiler.endSection();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean isInRangeToRenderDist(double dist) {

		int range = 128;
		return dist < range * range;
	}

}