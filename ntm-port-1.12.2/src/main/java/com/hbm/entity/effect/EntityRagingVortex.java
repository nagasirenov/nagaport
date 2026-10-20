package com.hbm.entity.effect;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class EntityRagingVortex extends EntityBlackHole {
	
	int timer = 0;

	public EntityRagingVortex(World p_i1582_1_) {
		super(p_i1582_1_);
		this.ignoreFrustumCheck = true;
		this.isImmuneToFire = true;
	}

	public EntityRagingVortex(World world, float size) {
		super(world);
		this.dataManager.set(BLACK_HOLE_SIZE, size);
	}
	
	@Override
	public void onUpdate() {
		
		timer++;
		
		if(timer <= 20)
			timer -= 20;
		
		float pulse = (float)(Math.sin(timer) * Math.PI / 20D) * 0.35F;
		
		float dec = 0.0F;
		
		if(rand.nextInt(100) == 0) {
			dec = 0.1F;
			this.world.createExplosion(null, posX, posY, posZ, 10F, false);
		}
		
		this.dataManager.set(BLACK_HOLE_SIZE, this.dataManager.get(BLACK_HOLE_SIZE) - pulse - dec);
		if(this.dataManager.get(BLACK_HOLE_SIZE) <= 0) {
			this.setDead();
			return;
		}
		
		super.onUpdate();
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbt) {
		super.readEntityFromNBT(nbt);
		
		timer = nbt.getInteger("vortexTimer");
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbt) {
		super.writeEntityToNBT(nbt);
		
		nbt.setInteger("vortexTimer", timer);
	}
}