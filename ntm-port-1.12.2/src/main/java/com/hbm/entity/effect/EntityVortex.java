package com.hbm.entity.effect;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class EntityVortex extends EntityBlackHole {
	
	public float shrinkRate = 0.0025F;

	public EntityVortex(World p_i1582_1_) {
		super(p_i1582_1_);
		this.ignoreFrustumCheck = true;
		this.isImmuneToFire = true;
	}

	public EntityVortex(World world, float size) {
		super(world);
		this.dataManager.set(BLACK_HOLE_SIZE, size);
	}
	
	public EntityVortex setShrinkRate(float shrinkRate) {
		this.shrinkRate = shrinkRate;
		return this;
	}
	
	@Override
	public void onUpdate() {
		
		this.dataManager.set(BLACK_HOLE_SIZE, this.dataManager.get(BLACK_HOLE_SIZE) - shrinkRate);
		if(this.dataManager.get(BLACK_HOLE_SIZE) <= 0) {
			this.setDead();
			return;
		}
		
		super.onUpdate();
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbt) {
		super.readEntityFromNBT(nbt);
		this.shrinkRate = nbt.getFloat("shrinkRate");
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbt) {
		super.writeEntityToNBT(nbt);
		nbt.setFloat("shrinkRate", this.shrinkRate);
	}
}
