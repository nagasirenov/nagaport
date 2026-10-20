package com.hbm.entity.mob.glyphid;

import com.hbm.entity.mob.glyphid.GlyphidStats.StatBundle;
import com.hbm.entity.projectile.EntityAcidBomb;
import com.hbm.main.ResourceManager;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntityGlyphidBombardier extends EntityGlyphid {

	public EntityGlyphidBombardier(World world) {
		super(world);
	}
	
	public ResourceLocation getSkin() {
		return ResourceManager.glyphid_bombardier_tex;
	}
	
	protected Entity lastTarget;
	protected double lastX;
	protected double lastY;
	protected double lastZ;

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(GlyphidStats.getStats().getBombardier().health);
		this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(GlyphidStats.getStats().getBombardier().speed);
		this.getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(GlyphidStats.getStats().getBombardier().damage);
	}
	
	public StatBundle getStats() {
		return GlyphidStats.getStats().statsBombardier;
	}
	
	@Override
	public void onUpdate() {
		super.onUpdate();
		Entity e = this.getEntityToAttack();
		if(!this.world.isRemote && e instanceof EntityLivingBase) {

			if(this.ticksExisted % 20 == 0) {
				this.lastTarget = e;
				this.lastX = e.posX;
				this.lastY = e.posY;
				this.lastZ = e.posZ;
			}
			
			if(this.ticksExisted % 60 == 1) {
				
				boolean topAttack = false;

				double velX = e.posX - lastX;
				double velY = e.posY - lastY;
				double velZ = e.posZ - lastZ;
				
				if(this.lastTarget != e || new Vec3d(velX, velY, velZ).length() > 30) {
					velX = velY = velZ = 0;
				}

				if (this.getDistanceToEntity(e) > 20) {
					topAttack = true;
				}

				int prediction = topAttack ? 60 : 20;
				Vec3d delta = new Vec3d(e.posX - posX + velX * prediction, (e.posY + e.height / 2) - (posY + 1) + velY * prediction, e.posZ - posZ + velZ * prediction);
				double len = delta.length();
				if(len < 3) return;
				double targetYaw = -Math.atan2(delta.x, delta.z);
				
				double x = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
				double y = delta.y;
				double v0 = getV0();
				double v02 = v0 * v0;
				double g = 0.04D;
				double upperLower = topAttack ? 1 : -1;
				double targetPitch = Math.atan((v02 + Math.sqrt(v02*v02 - g*(g*x*x + 2*y*v02)) * upperLower) / (g*x));
				
				if(!Double.isNaN(targetPitch)) {
					
					com.hbm.util.Vec3NT rotatedFireVec = new com.hbm.util.Vec3NT(v0, 0, 0);
					rotatedFireVec.rotateAroundZRad((float) -targetPitch);
					rotatedFireVec.rotateAroundYRad((float) -(targetYaw + Math.PI * 0.5));
					Vec3d fireVec = rotatedFireVec.toVec3d();
					
					for(int i = 0; i < getBombCount(); i++) {
						EntityAcidBomb bomb = new EntityAcidBomb(this.world, posX, posY + 1, posZ);
						bomb.setThrower(this);
						bomb.setThrowableHeading(fireVec.x, fireVec.y, fireVec.z, (float) v0, i * getSpreadMult());
						bomb.damage = getBombDamage();
						this.world.spawnEntityInWorld(bomb);
					}
					
					this.swingItem();
				}
			}
		}
	}
	
	public float getBombDamage() {
		return 5F;
	}
	
	public int getBombCount() {
		return 5;
	}
	
	public float getSpreadMult() {
		return 1F;
	}
	
	public double getV0() {
		return 1D;
	}
}
