package com.hbm.entity.mob.botprime;

import java.util.List;

import com.google.common.base.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;

public abstract class EntityWormBaseNT extends EntityBurrowingNT {

	/** Keep this entity's 1.7 render range independent of the global 1.12 multiplier. */
	@Override
	@net.minecraftforge.fml.relauncher.SideOnly(net.minecraftforge.fml.relauncher.Side.CLIENT)
	public boolean isInRangeToRenderDist(double distance) {
		double range = this.getEntityBoundingBox().getAverageEdgeLength();
		range *= 64.0D * 15.0D;
		return distance < range * range;
	}

	public int aggroCooldown = 0;
	public int courseChangeCooldown = 0;
	public double waypointX;
	public double waypointY;
	public double waypointZ;
	protected Entity targetedEntity = null;
	protected boolean canFly = false;
	protected int dmgCooldown = 0;
	protected boolean wasNearGround;
	protected BlockPos.MutableBlockPos spawnPoint = new BlockPos.MutableBlockPos();
	protected double attackRange;
	protected double maxSpeed;
	protected double fallSpeed;
	protected double rangeForParts;
	protected EntityLivingBase followed;
	protected int surfaceY;
	private int headID;
	private int partNum;
	protected boolean didCheck;
	protected double maxBodySpeed;
	protected double segmentDistance;
	protected double knockbackDivider;

	public static final Predicate<Entity> wormSelector = new Predicate<Entity>() {

		@Override
		public boolean apply(Entity target) {
			return target instanceof EntityWormBaseNT;
		}
	};

	public EntityWormBaseNT(World world) {
		super(world);
		this.setSize(1.0F, 1.0F);
		this.surfaceY = 60;
	}

	public int getPartNumber() {
		return this.partNum;
	}

	public void setPartNumber(int num) {
		this.partNum = num;
	}

	public Entity getHead() {
		return this.world.getEntityByID(this.headID);
	}

	public int getHeadID() {
		return this.headID;
	}

	public void setHeadID(int id) {
		this.headID = id;
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {

		if(this.isEntityInvulnerable(null) || source == DamageSource.DROWN || source == DamageSource.IN_WALL || ((source.getTrueSource() instanceof EntityWormBaseNT) && ((EntityWormBaseNT) source.getTrueSource()).getHeadID() == this.getHeadID())) {
			return false;
		} else {

			this.velocityChanged = true;

			if(this.getIsHead()) {
				return super.attackEntityFrom(source, amount);
			}

			Entity head = this.targetedEntity;

			if(head != null) {
				return head.attackEntityFrom(source, amount);
			} else {
				return super.attackEntityFrom(source, amount);
			}
		}
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		if(!this.world.isRemote) this.updateEntityActionStateNT();
	}

	protected void updateEntityActionStateNT() {

		if(!this.world.isRemote && this.world.getDifficulty() == EnumDifficulty.PEACEFUL) {
			setDead();
		}
		if((this.targetedEntity != null) && (this.targetedEntity.isDead)) {
			this.targetedEntity = null;
		}
		if(this.posY < -10.0D) {
			this.motionY = 1D;
		} else if(this.posY < 3.0D) {
			this.motionY = 0.3D;
		}

		if(this.ticksExisted % 5 == 0) {
			attackEntitiesInList(this.world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().grow(0.5D, 0.5D, 0.5D)));
		}
	}

	protected void attackEntitiesInList(List<Entity> targets) {

		for(Entity target : targets) {
			if((target instanceof EntityLivingBase) && canAttackClass(target.getClass()) && (!(target instanceof EntityWormBaseNT) || ((EntityWormBaseNT) target).getHeadID() != this.getHeadID())) {
				attackEntityAsMob(target);
			}
		}
	}

	@Override
	public boolean canAttackClass(Class clazz) {
		return true;
	}

	@Override
	public boolean attackEntityAsMob(Entity target) {

		boolean var2 = target.attackEntityFrom(DamageSource.causeMobDamage(this), getAttackStrength(target));

		if(var2) {

			double tx = (this.getEntityBoundingBox().minX + this.getEntityBoundingBox().maxX) / 2.0D;
			double tz = (this.getEntityBoundingBox().minZ + this.getEntityBoundingBox().maxZ) / 2.0D;
			double ty = (this.getEntityBoundingBox().minY + this.getEntityBoundingBox().maxY) / 2.0D;
			double deltaX = target.posX - tx;
			double deltaZ = target.posZ - tz;
			double deltaY = target.posY - ty;
			double knockback = this.knockbackDivider * (deltaX * deltaX + deltaZ * deltaZ + deltaY * deltaY + 0.1D);
			target.addVelocity(deltaX / knockback, deltaY / knockback, deltaZ / knockback);
		}

		return var2;
	}

	public abstract float getAttackStrength(Entity paramsa);

	@Override
	public void addVelocity(double x, double y, double z) { }

	@Override
	public void faceEntity(Entity entity, float yaw, float pitch) { }

	protected boolean isCourseTraversable() {
		return (this.canFly) || (isEntityInsideOpaqueBlock());
	}

	@Override
	protected float getSoundVolume() {
		return 5.0F;
	}

	@Override
	public void setDead() {
		playSound(getDeathSound(), getSoundVolume(), getSoundPitch());
		super.setDead();
	}

	public void writeEntityToNBT(NBTTagCompound nbt) {
		super.writeEntityToNBT(nbt);
		nbt.setInteger("wormID", this.getHeadID());
	}

	public void readEntityFromNBT(NBTTagCompound nbt) {
		super.readEntityFromNBT(nbt);
		setHeadID(nbt.getInteger("wormID"));
	}

	protected void updateMovement() {

		double targetingRange = 128.0D;

		if(this.targetedEntity != null && this.targetedEntity.getDistanceSq(this) < targetingRange * targetingRange) {
			this.waypointX = this.targetedEntity.posX;
			this.waypointY = this.targetedEntity.posY;
			this.waypointZ = this.targetedEntity.posZ;
		}

		if((this.ticksExisted % 60 == 0 || this.ticksExisted == 1) && (this.targetedEntity == null || this.followed == null)) {
			findEntityToFollow(this.world.getEntitiesWithinAABB(EntityWormBaseNT.class, this.getEntityBoundingBox().grow(this.rangeForParts, this.rangeForParts, this.rangeForParts), EntityWormBaseNT.wormSelector));
		}

		double deltaX = this.waypointX - this.posX;
		double deltaY = this.waypointY - this.posY;
		double deltaZ = this.waypointZ - this.posZ;
		double deltaDist = MathHelper.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);

		if(this.targetedEntity != null) {
			this.faceEntity(this.targetedEntity, 180.0F, 180.0F);
		}

		double speed = Math.max(0.0D, Math.min(deltaDist - this.segmentDistance, this.maxBodySpeed));

		if(deltaDist < this.segmentDistance * 0.895D) {
			this.motionX *= 0.8D;
			this.motionY *= 0.8D;
			this.motionZ *= 0.8D;
		} else {
			this.motionX = (deltaX / deltaDist * speed);
			this.motionY = (deltaY / deltaDist * speed);
			this.motionZ = (deltaZ / deltaDist * speed);
		}
	}

	protected void findEntityToFollow(List<EntityWormBaseNT> segments) {

		for(EntityWormBaseNT segment : segments) {

			if(segment.getHeadID() == this.getHeadID()) {

				if(segment.getIsHead()) {
					if(this.getPartNumber() == 0) {
						this.targetedEntity = ((Entity) segment);
					}
					this.followed = ((EntityLivingBase) segment);

				} else if(segment.getPartNumber() == this.getPartNumber() - 1) {
					this.targetedEntity = ((Entity) segment);
				}
			}
		}
		this.didCheck = true;
	}
}
