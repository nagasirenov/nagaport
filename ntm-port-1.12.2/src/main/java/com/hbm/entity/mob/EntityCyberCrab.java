package com.hbm.entity.mob;

import com.hbm.entity.projectile.EntityBullet;
import com.hbm.lib.ModDamageSource;

import api.hbm.entity.ISuffocationImmune;
import api.hbm.entity.IRadiationImmune;
import com.google.common.base.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.SharedMonsterAttributes;
import com.hbm.entity.mob.ai.EntityAIRangedAttackNT;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public class EntityCyberCrab extends EntityMob implements IRangedAttackMob, IRadiationImmune, ISuffocationImmune {

	@Override
	public void setSwingingArms(boolean swingingArms) { }

	private static final Predicate<Entity> selector = new Predicate<Entity>() {
		public boolean apply(Entity p_82704_1_) {
			return !(p_82704_1_ instanceof EntityCyberCrab || p_82704_1_ instanceof EntityCreeper);
		}
	};

	public EntityCyberCrab(World p_i1733_1_) {
		super(p_i1733_1_);
		this.setSize(0.75F, 0.35F);

		if(!(this instanceof EntityTaintCrab))
			this.tasks.addTask(0, new EntityAIPanic(this, 0.75D));

		this.tasks.addTask(1, new EntityAIWander(this, 0.5F));
		this.tasks.addTask(4, arrowAI());
		this.targetTasks.addTask(1, new EntityAINearestAttackableTarget(this, EntityPlayer.class, true));
		this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityLiving.class, 0, true, true, selector));
	}

	protected EntityAIRangedAttackNT arrowAI() {
		return new EntityAIRangedAttackNT(this, 0.5D, 60, 80, 15.0F);
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(4.0D);
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.75F);
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {

		if(ModDamageSource.getIsTau(source))
			return false;

		return super.attackEntityFrom(source, amount);
	}

	/**
	 * Returns true if the newer Entity AI code should be run
	 */
	public boolean isAIEnabled() {
		return true;
	}

	/**
	 * The number of iterations PathFinder.getSafePoint will execute before
	 * giving up.
	 */
	public int getMaxSafePointTries() {
		return this.getAttackTarget() == null ? 3 : 3 + (int) (this.getHealth() - 1.0F);
	}

	@Override
	protected void entityInit() {
		super.entityInit();
	}

	/**
	 * (abstract) Protected helper method to write subclass entity data to NBT.
	 */
	@Override
	public void writeEntityToNBT(NBTTagCompound p_70014_1_) {
		super.writeEntityToNBT(p_70014_1_);
	}

	/**
	 * (abstract) Protected helper method to read subclass entity data from NBT.
	 */
	@Override
	public void readEntityFromNBT(NBTTagCompound p_70037_1_) {
		super.readEntityFromNBT(p_70037_1_);
	}

	/**
	 * Called to update the entity's position/logic.
	 */
	@Override
	public void onUpdate() {
		super.onUpdate();

		if(this.isInWater() || this.isWet() || this.isBurning())
			this.attackEntityFrom(DamageSource.GENERIC, 10F);

		if(this.getHealth() <= 0) {
			this.setDead();

			if(this instanceof EntityTaintCrab)
				this.world.createExplosion(this, this.posX, this.posY, this.posZ, 3F, false);
			else
				this.world.createExplosion(this, this.posX, this.posY, this.posZ, 0.1F, false);
		}
	}

	/**
	 * Returns the sound this mob makes when it is hurt.
	 */
	@Override
	protected net.minecraft.util.SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return com.hbm.main.ModSoundEvents.resolve("hbm:entity.cybercrab");
	}

	/**
	 * Returns the sound this mob makes on death.
	 */
	@Override
	protected net.minecraft.util.SoundEvent getDeathSound() {
		return com.hbm.main.ModSoundEvents.resolve("hbm:entity.cybercrab");
	}

	/**
	 * Called when the mob's health reaches 0.
	 */
	@Override
	public void onDeath(DamageSource p_70645_1_) {
		super.onDeath(p_70645_1_);
	}

	@Override
	public boolean attackEntityAsMob(Entity p_70652_1_) {
		return true;
	}

	@Override
	public void attackEntityWithRangedAttack(EntityLivingBase entity, float f) {
		EntityBullet bullet = new EntityBullet(this.world, this, entity, 1.6F, 2);
		bullet.setIsCritical(true);
		bullet.setTau(true);
		bullet.damage = 3;
		this.world.spawnEntity(bullet);
		this.playSound(com.hbm.main.ModSoundEvents.resolve("hbm:weapon.sawShoot"), 1.0F, 2.0F);
	}
}
