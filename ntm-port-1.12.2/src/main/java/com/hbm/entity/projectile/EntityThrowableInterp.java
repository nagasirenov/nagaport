package com.hbm.entity.projectile;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public abstract class EntityThrowableInterp extends EntityThrowableNT {
	
	protected int turnProgress;
	protected double syncPosX;
	protected double syncPosY;
	protected double syncPosZ;
	protected double syncYaw;
	protected double syncPitch;
	@SideOnly(Side.CLIENT)
	protected double velocityX;
	@SideOnly(Side.CLIENT)
	protected double velocityY;
	@SideOnly(Side.CLIENT)
	protected double velocityZ;

	public EntityThrowableInterp(World world) {
		super(world);
	}

	public EntityThrowableInterp(World world, double x, double y, double z) {
		super(world, x, y, z);
	}
	
	@Override
	public void onUpdate() {
		
		if(!this.world.isRemote) {
			super.onUpdate();
		} else {
			this.lastTickPosX = this.posX;
			this.lastTickPosY = this.posY;
			this.lastTickPosZ = this.posZ;
			if(this.turnProgress > 0) {
				double interpX = this.posX + (this.syncPosX - this.posX) / (double) this.turnProgress;
				double interpY = this.posY + (this.syncPosY - this.posY) / (double) this.turnProgress;
				double interpZ = this.posZ + (this.syncPosZ - this.posZ) / (double) this.turnProgress;
				double d = MathHelper.wrapDegrees(this.syncYaw - (double) this.rotationYaw);
				this.rotationYaw = (float) ((double) this.rotationYaw + d / (double) this.turnProgress);
				this.rotationPitch = (float)((double)this.rotationPitch + (this.syncPitch - (double)this.rotationPitch) / (double)this.turnProgress);
				--this.turnProgress;
				this.setPosition(interpX, interpY, interpZ);
			} else {
				this.setPosition(this.posX, this.posY, this.posZ);
			}
		}
	}
	
	@SideOnly(Side.CLIENT)
	public void setVelocity(double velX, double velY, double velZ) {
		this.velocityX = this.motionX = velX;
		this.velocityY = this.motionY = velY;
		this.velocityZ = this.motionZ = velZ;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void setPositionAndRotationDirect(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean teleport) {
		if(teleport) {
			this.setPosition(x, y, z);
			this.setRotation(yaw, pitch);
			this.turnProgress = 0;
		} else {
			this.syncPosX = x;
			this.syncPosY = y;
			this.syncPosZ = z;
			this.syncYaw = yaw;
			this.syncPitch = pitch;
			this.turnProgress = posRotationIncrements + approachNum();
			this.motionX = this.velocityX;
			this.motionY = this.velocityY;
			this.motionZ = this.velocityZ;
		}
	}

	@SideOnly(Side.CLIENT)
	public void setPositionAndRotation2(double x, double y, double z, float yaw, float pitch, int theNumberThree) {
		setPositionAndRotationDirect(x, y, z, yaw, pitch, theNumberThree, false);
	}
	
	/**
	 * @return a number added to the basic "3" of the approach progress value. Larger numbers make the approach smoother, but lagging behind the true value more.
	 */
	public int approachNum() {
		return 0;
	}
}
