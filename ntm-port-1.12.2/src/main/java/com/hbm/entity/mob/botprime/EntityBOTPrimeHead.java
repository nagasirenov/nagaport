package com.hbm.entity.mob.botprime;

import java.util.List;

import com.hbm.entity.mob.ai.EntityAINearestAttackableTargetNT;
import com.hbm.items.ModItems;
import com.hbm.main.MainRegistry;

import net.minecraft.entity.Entity;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import com.hbm.entity.boss.INtmBossDisplay;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class EntityBOTPrimeHead extends EntityBOTPrimeBase implements INtmBossDisplay {

	/*   ___   _   _   _   ___           ___           _____ ___  ___ _  _       ___ ___  _ _   _ ___
	 *  | _ ) /_\ | | | | / __|   ___   |   |   ___   |_   _| _ )|   | \| |     | _ \ _ )| | \ / | __|
	 *  | _ \/ _ \| |_| |_\__ \  |___|  | | |  |___|    | | |   \| | |    |     |  _/   \| |  V  | _|
	 *  |___/_/ \_\___|___|___/         |___|           |_| |_|\_\___|_|\_|     |_| |_|\_\_|_|V|_|___|
	 */

	//TODO: clean-room implementation of the movement behavior classes (again)

	public EntityBOTPrimeHead(World world) {
		super(world);
		this.experienceValue = 1000;
		this.wasNearGround = false;
		this.attackRange = 150.0D;
		this.setSize(3.0F, 3.0F);
		this.maxSpeed = 1.0D;
		this.fallSpeed = 0.006D;
		this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
		this.targetTasks.addTask(2, new EntityAINearestAttackableTargetNT(this, EntityPlayer.class, 0, false, false, null, 128.0D));
		this.ignoreFrustumCheck = true;
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.15D);
	}

	@Override
	public boolean getIsHead() {
		return true;
	}

	@Override
	public boolean attackEntityFrom(DamageSource par1DamageSource, float par2) {

		if(super.attackEntityFrom(par1DamageSource, par2)) {
			this.dmgCooldown = 10;
			return true;
		}

		return false;
	}

    public IEntityLivingData onInitialSpawn(net.minecraft.world.DifficultyInstance difficulty, IEntityLivingData data) {

    	//TODO: check if this is even needed
    	setHeadID(this.getEntityId());

    	int x = MathHelper.floor(this.posX);
        int y = MathHelper.floor(this.posY);
        int z = MathHelper.floor(this.posZ);

        for (int i = 0; i < 74; i++) {

          EntityBOTPrimeBody bodyPart = new EntityBOTPrimeBody(this.world);
          bodyPart.setPartNumber(i);
          bodyPart.setPosition(x, y, z);
          bodyPart.setHeadID(getEntityId());
          this.world.spawnEntity(bodyPart);
        }

        setPosition(x, y, z);
        this.spawnPoint.setPos(x, y, z);

        return super.onInitialSpawn(difficulty, data);
    }

	@Override
	protected void updateAITasks() {

		this.updateEntityActionState();
		super.updateAITasks();

		updateHeadMovement();

		if((getHealth() < getMaxHealth()) && (this.ticksExisted % 6 == 0)) {
			if(this.targetedEntity != null) {
				heal(1.0F);
			} else if(this.recentlyHit == 0) {
				heal(4.0F);
			}
		}
		if((this.targetedEntity != null) && (this.targetedEntity.getDistanceSq(this) < this.attackRange * this.attackRange)) {
			if(canEntityBeSeenThroughNonSolids(this.targetedEntity)) {

				this.attackCounter ++;

				if(this.attackCounter == 30) {

					laserAttack(this.targetedEntity, true);
					this.attackCounter = 0;
				}
			} else {
				this.attackCounter = 0;
			}
		} else {
			this.attackCounter = 0;
		}
	}

	@Override
	public void onUpdate() {

		super.onUpdate();

		double dx = motionX;
		double dy = motionY;
		double dz = motionZ;
		float f3 = MathHelper.sqrt(dx * dx + dz * dz);
		this.prevRotationYaw = this.rotationYaw = (float) (Math.atan2(dx, dz) * 180.0D / Math.PI);
		this.prevRotationPitch = this.rotationPitch = (float) (Math.atan2(dy, f3) * 180.0D / Math.PI);
	}

    @Override
	public void onDeath(DamageSource p_70645_1_) {
		super.onDeath(p_70645_1_);

		List<EntityPlayer> players = this.world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().grow(200, 200, 200));

		for(EntityPlayer player : players) {
			com.hbm.advancement.NtmAdvancements.award(player, MainRegistry.bossWorm);
			player.inventory.addItemStackToInventory(new ItemStack(ModItems.coin_worm));
		}
	}

	@Override
	public float getAttackStrength(Entity target) {
		return 1000;
	}

	public void writeEntityToNBT(NBTTagCompound nbt) {
		super.writeEntityToNBT(nbt);
		nbt.setInteger("spawnX", this.spawnPoint.getX());
		nbt.setInteger("spawnY", this.spawnPoint.getY());
		nbt.setInteger("spawnZ", this.spawnPoint.getZ());
	}

	public void readEntityFromNBT(NBTTagCompound nbt) {
		super.readEntityFromNBT(nbt);
		this.spawnPoint.setPos(nbt.getInteger("spawnX"), nbt.getInteger("spawnY"), nbt.getInteger("spawnZ"));
	}

	protected void updateHeadMovement() {

		double deltaX = this.waypointX - this.posX;
		double deltaY = this.waypointY - this.posY;
		double deltaZ = this.waypointZ - this.posZ;
		double deltaSq = deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ;

		if(this.courseChangeCooldown-- <= 0) {

			this.courseChangeCooldown += this.getRNG().nextInt(5) + 2;
			deltaSq = MathHelper.sqrt(deltaSq);

			if(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ < this.maxSpeed) {

				if(!this.isCourseTraversable()) {
					deltaSq *= 8.0D;
				}

				double moverSpeed = this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getBaseValue();
				this.motionX += deltaX / deltaSq * moverSpeed;
				this.motionY += deltaY / deltaSq * moverSpeed;
				this.motionZ += deltaZ / deltaSq * moverSpeed;
			}
		}

		if(!this.isCourseTraversable()) {
			this.motionY -= this.fallSpeed;
		}

		if(this.dmgCooldown > 0) {
			this.dmgCooldown -= 1;
		}

		this.aggroCooldown -= 1;

		if(this.getAttackTarget() != null) {

			if(this.aggroCooldown <= 0) {
				this.targetedEntity = this.getAttackTarget();
				this.aggroCooldown = 20;
			}

		} else if(this.targetedEntity == null) {
			this.waypointX = this.spawnPoint.getX() - 50 + this.getRNG().nextInt(100);
			this.waypointY = this.spawnPoint.getY() - 30 + this.getRNG().nextInt(60);
			this.waypointZ = this.spawnPoint.getZ() - 50 + this.getRNG().nextInt(100);
		}

		this.rotationYaw = -(float) -(Math.atan2(this.motionX, this.motionZ) * 180.0F / Math.PI);
		this.rotationPitch = (float) -(Math.atan2(this.motionY, MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ)) * 180.0D / Math.PI);

		if(this.targetedEntity != null && this.targetedEntity .getDistanceSq(this) < this.attackRange * this.attackRange) {

			if(this.wasNearGround || this.canFly) {

				this.waypointX = this.targetedEntity.posX;
				this.waypointY = this.targetedEntity.posY;
				this.waypointZ = this.targetedEntity.posZ;

				if(this.getRNG().nextInt(80) == 0 && this.posY > this.surfaceY && !this.isCourseTraversable()) {
					this.wasNearGround = false;
				}

			} else {

				this.waypointX = this.targetedEntity.posX;
				this.waypointY = 10.0D;
				this.waypointZ = this.targetedEntity.posZ;

				if(this.posY < 15.0D) {
					this.wasNearGround = true;
				}
			}
		}
	}
}
