package com.hbm.entity.mob.ai;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumHand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

/** FBI melee task retaining the original player-only start, Forge path penalties, and attack cadence. */
public class EntityAIFBIMelee extends EntityAIBase {

	World worldObj;
	EntityCreature attacker;
	int attackTick;
	double speedTowardsTarget;
	boolean longMemory;
	Path entityPathEntity;
	Class classTarget;
	private int pathTimer;
	private double lastX;
	private double lastY;
	private double lastZ;

	private int failedPathFindingPenalty;

	public EntityAIFBIMelee(EntityCreature owner) {
		this.attacker = owner;
		this.worldObj = owner.getEntityWorld();
		this.speedTowardsTarget = 1.0D;
		this.longMemory = true;
		this.classTarget = EntityPlayer.class;
		this.setMutexBits(3);
	}

	@Override
	public boolean shouldExecute() {
		EntityLivingBase entitylivingbase = this.attacker.getAttackTarget();

		if(entitylivingbase == null) {
			return false;

		} else if(!entitylivingbase.isEntityAlive()) {
			return false;

		} else if(this.classTarget != null && !this.classTarget.isAssignableFrom(entitylivingbase.getClass())) {
			return false;

		} else {

			if(--this.pathTimer <= 0) {

				this.entityPathEntity = this.attacker.getNavigator().getPathToEntityLiving(entitylivingbase);
				this.pathTimer = 4 + this.attacker.getRNG().nextInt(7);
				return this.entityPathEntity != null;

			} else {
				return true;
			}
		}
	}


	@Override
	public boolean shouldContinueExecuting() {

		EntityLivingBase entitylivingbase = this.attacker.getAttackTarget();

		return entitylivingbase == null ? false
				: (!entitylivingbase.isEntityAlive() ? false
				: (!this.longMemory ? !this.attacker.getNavigator().noPath()
				: this.attacker.isWithinHomeDistanceFromPosition(new BlockPos(MathHelper.floor(entitylivingbase.posX), MathHelper.floor(entitylivingbase.posY), MathHelper.floor(entitylivingbase.posZ)))));
	}


	@Override
	public void startExecuting() {
		this.attacker.getNavigator().setPath(this.entityPathEntity, this.speedTowardsTarget);
		this.pathTimer = 0;
	}


	@Override
	public void resetTask() {
		this.attacker.getNavigator().clearPath();
	}


	@Override
	public void updateTask() {

		EntityLivingBase entitylivingbase = this.attacker.getAttackTarget();
		this.attacker.getLookHelper().setLookPositionWithEntity(entitylivingbase, 30.0F, 30.0F);
		double d0 = this.attacker.getDistanceSq(entitylivingbase.posX, entitylivingbase.getEntityBoundingBox().minY, entitylivingbase.posZ);

		double d1 = (double) ((this.attacker.width * 2.0F) * (this.attacker.width * 2.0F) + entitylivingbase.width);
		this.pathTimer--;

		if((this.longMemory || this.attacker.getEntitySenses().canSee(entitylivingbase)) && this.pathTimer <= 0
				&& (this.lastX == 0.0D && this.lastY == 0.0D && this.lastZ == 0.0D
				|| entitylivingbase.getDistanceSq(this.lastX, this.lastY, this.lastZ) >= 1.0D
				|| this.attacker.getRNG().nextFloat() < 0.05F)) {

			this.lastX = entitylivingbase.posX;
			this.lastY = entitylivingbase.getEntityBoundingBox().minY;
			this.lastZ = entitylivingbase.posZ;
			this.pathTimer = failedPathFindingPenalty + 4 + this.attacker.getRNG().nextInt(7);

			if(this.attacker.getNavigator().getPath() != null) {

				PathPoint finalPathPoint = this.attacker.getNavigator().getPath().getFinalPathPoint();
				if(finalPathPoint != null && entitylivingbase.getDistanceSq(finalPathPoint.x, finalPathPoint.y, finalPathPoint.z) < 1) {
					failedPathFindingPenalty = 0;

				} else {
					failedPathFindingPenalty += 10;
				}

			} else {
				failedPathFindingPenalty += 10;
			}

			if(d0 > 1024.0D) {
				this.pathTimer += 10;
			} else if(d0 > 256.0D) {
				this.pathTimer += 5;
			}

			if(!this.attacker.getNavigator().tryMoveToEntityLiving(entitylivingbase, this.speedTowardsTarget)) {
				this.pathTimer += 15;
			}
		}

		this.attackTick = Math.max(this.attackTick - 1, 0);

		// The original test is <= 20, not the native melee task's <= 0.
		if(d0 <= d1 && this.attackTick <= 20) {
			this.attackTick = 20;

			if(!this.attacker.getHeldItemMainhand().isEmpty()) {
				this.attacker.swingArm(EnumHand.MAIN_HAND);
			}

			this.attacker.attackEntityAsMob(entitylivingbase);
		}
	}

}
