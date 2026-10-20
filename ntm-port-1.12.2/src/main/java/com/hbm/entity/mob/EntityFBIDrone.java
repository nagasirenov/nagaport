package com.hbm.entity.mob;

import com.hbm.entity.grenade.EntityGrenadeStrong;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntityFBIDrone extends EntityUFOBase {

	private int attackCooldown;

	public EntityFBIDrone(World world) {
		super(world);
	}

	@Override
	protected void updateEntityActionState() {
		super.updateEntityActionState();
		if(this.courseChangeCooldown > 0) this.courseChangeCooldown--;
		if(this.scanCooldown > 0) this.scanCooldown--;

		if(!this.world.isRemote) {
			
			if(attackCooldown > 0) attackCooldown--;
			
			if(this.target != null && attackCooldown <= 0) {
				
				Vec3d vec = new Vec3d(posX - target.posX, posY - target.posY, posZ - target.posZ);
				if(Math.abs(vec.x) < 5 && Math.abs(vec.z) < 5 && vec.y > 3) {
					attackCooldown = 60;
					EntityGrenadeStrong grenade = new EntityGrenadeStrong(this.world);
					grenade.setPosition(posX, posY, posZ);
					this.world.spawnEntityInWorld(grenade);
				}
			}
		}
		
		if(this.courseChangeCooldown > 0) {
			approachPosition(this.target == null ? 0.25D : 0.5D);
		}
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(35.0D);
	}

	@Override
	protected int getScanRange() {
		return 100;
	}

	@Override
	protected int targetHeightOffset() {
		return 7 + rand.nextInt(4);
	}

	@Override
	protected int wanderHeightOffset() {
		return 7 + rand.nextInt(4);
	}
}
