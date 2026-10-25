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
	public void move(net.minecraft.entity.MoverType type, double moX, double moY, double moZ) {
		if(this.isInWeb) {
			this.isInWeb = false;
		}

		double initMoX = moX;
		double initMoZ = moZ;

		super.move(type, moX, moY, moZ);

		if(initMoX != this.motionX && this.collidedHorizontally) {
			this.motionX *= -0.75D;
		}
		if(initMoZ != this.motionZ && this.collidedHorizontally) {
			this.motionZ *= -0.75D;
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean isInRangeToRenderDist(double dist) {

		int range = 128;
		return dist < range * range;
	}

}
