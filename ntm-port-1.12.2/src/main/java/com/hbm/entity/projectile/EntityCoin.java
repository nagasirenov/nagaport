package com.hbm.entity.projectile;

import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class EntityCoin extends EntityThrowableInterp {

	public EntityCoin(World world) {
		super(world);
		this.setSize(1F, 1F);
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
	}

	public void setPosition(double x, double y, double z) {
		this.posX = x;
		this.posY = y;
		this.posZ = z;
		float f = this.width / 2.0F;
		this.setEntityBoundingBox(new net.minecraft.util.math.AxisAlignedBB(x - f, y, z - f, x + f, y + this.height, z + f));
	}

	@Override
	protected void onImpact(RayTraceResult mop) {
		if(mop.typeOfHit == RayTraceResult.Type.BLOCK) this.setDead();
	}

	@Override
	protected float getAirDrag() {
		return 1F;
	}

	@Override
	public double getGravityVelocity() {
		return 0.02D;
	}

	@Override
	public boolean canBeCollidedWith() {
		return true;
	}

	public boolean canAttackWithItem() {
		return true;
	}
}
