package com.hbm.entity.mob;

import java.util.List;

import api.hbm.entity.ISuffocationImmune;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityFlying;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;


public abstract class EntityUFOBase extends EntityFlying implements IMob, ISuffocationImmune {
	private static final DataParameter<Integer> WAYPOINT_X = EntityDataManager.createKey(EntityUFOBase.class, DataSerializers.VARINT);
	private static final DataParameter<Integer> WAYPOINT_Y = EntityDataManager.createKey(EntityUFOBase.class, DataSerializers.VARINT);
	private static final DataParameter<Integer> WAYPOINT_Z = EntityDataManager.createKey(EntityUFOBase.class, DataSerializers.VARINT);


	protected int scanCooldown;
	protected int courseChangeCooldown;
	protected Entity target;

	public EntityUFOBase(World world) {
		super(world);
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		//XYZ
		this.getDataManager().register(WAYPOINT_X, 0);
		this.getDataManager().register(WAYPOINT_Y, 0);
		this.getDataManager().register(WAYPOINT_Z, 0);
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		if(!this.world.isRemote) this.updateEntityActionStateNT();
	}

	protected void updateEntityActionStateNT() {

		if(!this.world.isRemote) {

			if(this.world.getDifficulty() == EnumDifficulty.PEACEFUL) {
				this.setDead();
				return;
			}
		}

		this.motionX = 0;
		this.motionY = 0;
		this.motionZ = 0;

		if(this.target != null && !this.target.isEntityAlive()) {
			this.target = null;
		}

		scanForTarget();

		if(this.courseChangeCooldown <= 0) {
			this.setCourse();
		}

		/*
		 * Make sure to invoke super.updateEntityActionState(); in the beginning of the child's override
		 * Motion is set to 0 and the targeting should optimally be handled before anything else
		 */
	}

	/**
	 * Standard implementation for choosing single player targets
	 * Keeps the check delay in mind and resets it too, simply call this every update
	 */
	protected void scanForTarget() {

		int range = getScanRange();

		if(this.scanCooldown <= 0) {
			List<Entity> entities = this.world.getEntitiesWithinAABB(Entity.class, this.getEntityBoundingBox().grow(range, range / 2, range));
			this.target = null;

			for(Entity entity : entities) {

				if(!entity.isEntityAlive() || !canAttackClass((Class<? extends net.minecraft.entity.EntityLivingBase>) entity.getClass()))
					continue;

				if(entity instanceof EntityPlayer) {

					if(((EntityPlayer)entity).capabilities.isCreativeMode)
						continue;

					if(((EntityPlayer)entity).isPotionActive(net.minecraft.init.MobEffects.INVISIBILITY))
						continue;

					if(this.target == null) {
						this.target = entity;
					} else {
						if(this.getDistanceSq(entity) < this.getDistanceSq(this.target)) {
							this.target = entity;
						}
					}
				}
			}

			this.scanCooldown = getScanDelay();
		}
	}

	protected int getScanRange() {
		return 50;
	}

	protected int getScanDelay() {
		return 100;
	}

	protected boolean isCourseTraversable(double p_70790_1_, double p_70790_3_, double p_70790_5_, double p_70790_7_) {

		double d4 = (this.getX() - this.posX) / p_70790_7_;
		double d5 = (this.getY() - this.posY) / p_70790_7_;
		double d6 = (this.getZ() - this.posZ) / p_70790_7_;
		AxisAlignedBB axisalignedbb = this.getEntityBoundingBox();

		for(int i = 1; i < p_70790_7_; ++i) {
			axisalignedbb = axisalignedbb.offset(d4, d5, d6);

			if(!this.world.getCollisionBoxes(this, axisalignedbb).isEmpty()) {
				return false;
			}
		}

		return true;
	}

	protected void approachPosition(double speed) {

		double deltaX = this.getX() - this.posX;
		double deltaY = this.getY() - this.posY;
		double deltaZ = this.getZ() - this.posZ;
		Vec3d delta = new Vec3d(deltaX, deltaY, deltaZ);
		double len = delta.length();

		if(len > 5) {
			if(this.isCourseTraversable(this.getX(), this.getY(), this.getZ(), len)) {
				this.motionX = delta.x * speed / len;
				this.motionY = delta.y * speed / len;
				this.motionZ = delta.z * speed / len;
			} else {
				this.courseChangeCooldown = 0;
			}
		}
	}

	protected void setCourse() {

		if(this.target != null) {
			this.setCourseForTaget();
			this.courseChangeCooldown = 20 + rand.nextInt(20);
		} else {
			this.setCourseWithoutTaget();
			this.courseChangeCooldown = 60 + rand.nextInt(20);
		}
	}

	protected void setCourseForTaget() {
		Vec3d vec = new Vec3d(this.posX - this.target.posX, 0, this.posZ - this.target.posZ);
		vec = vec.rotateYaw((float)Math.PI * 2 * rand.nextFloat());

		double length = vec.length();
		double overshoot = 10 + rand.nextDouble() * 10;

		int wX = (int)Math.floor(this.target.posX - vec.x / length * overshoot);
		int wZ = (int)Math.floor(this.target.posZ - vec.z / length * overshoot);

		this.setWaypoint(wX, Math.max(this.world.getHeight(wX, wZ), (int) this.target.posY) + targetHeightOffset(),  wZ);
	}

	protected int targetHeightOffset() {
		return 2 + rand.nextInt(2);
	}

	protected int wanderHeightOffset() {
		return 2 + rand.nextInt(3);
	}

	protected void setCourseWithoutTaget() {
		int x = (int) Math.floor(posX + rand.nextGaussian() * 5);
		int z = (int) Math.floor(posZ + rand.nextGaussian() * 5);
		this.setWaypoint(x, this.world.getHeight(x, z) + wanderHeightOffset(),  z);
	}

	public void setWaypoint(int x, int y, int z) {
		this.dataManager.set(WAYPOINT_X, x);
		this.dataManager.set(WAYPOINT_Y, y);
		this.dataManager.set(WAYPOINT_Z, z);
	}

	public int getX() {
		return this.dataManager.get(WAYPOINT_X);
	}

	public int getY() {
		return this.dataManager.get(WAYPOINT_Y);
	}

	public int getZ() {
		return this.dataManager.get(WAYPOINT_Z);
	}
}
