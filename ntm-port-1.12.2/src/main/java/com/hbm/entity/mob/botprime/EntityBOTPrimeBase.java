package com.hbm.entity.mob.botprime;

import com.hbm.entity.projectile.EntityBulletBaseNT;
import com.hbm.handler.BulletConfigSyncingUtil;

import api.hbm.entity.IRadiationImmune;
import com.google.common.base.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public abstract class EntityBOTPrimeBase extends EntityWormBaseNT implements IRadiationImmune {

	public int attackCounter = 0;

	protected final Predicate<Entity> selector = new Predicate<Entity>() {

		@Override
		public boolean apply(Entity ent) {

			if(ent instanceof EntityWormBaseNT && ((EntityWormBaseNT) ent).getHeadID() == EntityBOTPrimeBase.this.getHeadID())
				return false;

			return true;
		}

	};

	public EntityBOTPrimeBase(World world) {
		super(world);
		this.setSize(2.0F, 2.0F);
		this.isImmuneToFire = true;
		this.isAirBorne = true;
		this.noClip = true;
		this.dragInAir = 0.995F;
		this.dragInGround = 0.98F;
		this.knockbackDivider = 1.0D;
	}

	public boolean canEntityBeSeenThroughNonSolids(Entity p_70685_1_) {
		return this.world.rayTraceBlocks(new Vec3d(this.posX, this.posY + (double) this.getEyeHeight(), this.posZ), new Vec3d(p_70685_1_.posX, p_70685_1_.posY + (double) p_70685_1_.getEyeHeight(), p_70685_1_.posZ), false, true, false) == null;
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(15000.0D);
		this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(1.0D);
	}

	protected boolean isAIEnabled() {
		return true;
	}

	@Override
	protected boolean canDespawn() {
		return false;
	}

	protected String getHurtSound() {
		return "mob.blaze.hit";
	}

	@Override
	protected net.minecraft.util.SoundEvent getDeathSound() {
		return com.hbm.main.ModSoundEvents.resolve("hbm:entity.bombDet");
	}

	protected void laserAttack(Entity target, boolean head) {

		if(!(target instanceof EntityLivingBase))
			return;

		EntityLivingBase living = (EntityLivingBase) target;

		if(head) {

			for(int i = 0; i < 5; i++) {

				EntityBulletBaseNT bullet = new EntityBulletBaseNT(this.world, BulletConfigSyncingUtil.WORM_LASER, this, living, 1.0F, i * 0.05F);
				this.world.spawnEntity(bullet);
			}

			this.world.playSound(null, this.posX, this.posY, this.posZ, com.hbm.main.ModSoundEvents.resolve("hbm:weapon.ballsLaser"), net.minecraft.util.SoundCategory.NEUTRAL, 5.0F, 0.75F);

		} else {
			EntityBulletBaseNT bullet = new EntityBulletBaseNT(this.world, BulletConfigSyncingUtil.WORM_BOLT, this, living, 0.5F, 0.125F);
			this.world.spawnEntity(bullet);
			this.world.playSound(null, this.posX, this.posY, this.posZ, com.hbm.main.ModSoundEvents.resolve("hbm:weapon.ballsLaser"), net.minecraft.util.SoundCategory.NEUTRAL, 5.0F, 1.0F);
		}
	}
}
