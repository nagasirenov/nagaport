package com.hbm.entity.projectile;

import java.lang.reflect.Field;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityMooshroom;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SPacketChangeGameState;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import com.hbm.blocks.bomb.BlockDetonatable;
import com.hbm.entity.grenade.EntityGrenadeTau;
import com.hbm.entity.mob.EntityCreeperNuclear;
import com.hbm.items.ModItems;
import com.hbm.lib.ModDamageSource;
import com.hbm.util.ArmorUtil;

import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityBullet extends Entity implements IProjectile {

	private double renderRangeWeight = 1.0D;

	/** Keep this entity's 1.7 render range independent of the global 1.12 multiplier. */
	@Override
	@net.minecraftforge.fml.relauncher.SideOnly(net.minecraftforge.fml.relauncher.Side.CLIENT)
	public boolean isInRangeToRenderDist(double distance) {
		double range = this.getEntityBoundingBox().getAverageEdgeLength();
		range *= 64.0D * this.renderRangeWeight;
		return distance < range * range;
	}

	private static final DataParameter<Byte> CRITICAL_FLAGS = EntityDataManager.createKey(EntityBullet.class, DataSerializers.BYTE);
	private static final DataParameter<Byte> TAU_FLAGS = EntityDataManager.createKey(EntityBullet.class, DataSerializers.BYTE);
	private static final DataParameter<Byte> CHOPPER_FLAGS = EntityDataManager.createKey(EntityBullet.class, DataSerializers.BYTE);
	private int tileX = -1;
	private int tileY = -1;
	private int tileZ = -1;
	public double gravity = 0.0D;
	private Block field_145790_g;
	private int inData;
	private boolean inGround;
	/** 1 if the player can pick up the arrow */
	public int canBePickedUp;
	/** Seems to be some sort of timer for animating an arrow. */
	public int arrowShake;
	/** The owner of this arrow. */
	public Entity shootingEntity;
	private int ticksInGround;
	private int ticksInAir;
	public double damage;
	/** The amount of knockback an arrow applies when it hits a mob. */
	private int knockbackStrength;
	private boolean instakill = false;
	private boolean rad = false;
	public boolean antidote = false;
	public boolean pip = false;
	public boolean fire = false;

	public EntityBullet(World p_i1753_1_) {
		super(p_i1753_1_);
		this.renderRangeWeight = 10.0D;
		this.setSize(0.5F, 0.5F);
	}

	public EntityBullet(World p_i1754_1_, double p_i1754_2_, double p_i1754_4_, double p_i1754_6_) {
		super(p_i1754_1_);
		this.renderRangeWeight = 10.0D;
		this.setSize(0.5F, 0.5F);
		this.setPosition(p_i1754_2_, p_i1754_4_, p_i1754_6_);
	}

	public EntityBullet(World p_i1755_1_, EntityLivingBase p_i1755_2_, EntityLivingBase p_i1755_3_, float p_i1755_4_,
			float p_i1755_5_) {
		super(p_i1755_1_);
		this.renderRangeWeight = 10.0D;
		this.shootingEntity = p_i1755_2_;

		if (p_i1755_2_ instanceof EntityPlayer) {
			this.canBePickedUp = 1;
		}

		this.posY = p_i1755_2_.posY + p_i1755_2_.getEyeHeight() - 0.10000000149011612D;
		double d0 = p_i1755_3_.posX - p_i1755_2_.posX;
		double d1 = p_i1755_3_.getEntityBoundingBox().minY + p_i1755_3_.height / 3.0F - this.posY;
		double d2 = p_i1755_3_.posZ - p_i1755_2_.posZ;
		double d3 = MathHelper.sqrt(d0 * d0 + d2 * d2);

		if (d3 >= 1.0E-7D) {
			float f2 = (float) (Math.atan2(d2, d0) * 180.0D / Math.PI) - 90.0F;
			float f3 = (float) (-(Math.atan2(d1, d3) * 180.0D / Math.PI));
			double d4 = d0 / d3;
			double d5 = d2 / d3;
			this.setLocationAndAngles(p_i1755_2_.posX + d4, this.posY, p_i1755_2_.posZ + d5, f2, f3);
			float f4 = 0;//(float) d3 * 0.2F;
			this.shoot(d0, d1 + f4, d2, p_i1755_4_, p_i1755_5_);
		}
	}

	public EntityBullet(World p_i1756_1_, EntityLivingBase p_i1756_2_, float p_i1756_3_, int dmgMin, int dmgMax,
			boolean instakill, boolean rad) {
		super(p_i1756_1_);
		this.renderRangeWeight = 10.0D;
		this.shootingEntity = p_i1756_2_;

		if (p_i1756_2_ instanceof EntityPlayer) {
			this.canBePickedUp = 1;
		}

		this.setSize(0.5F, 0.5F);
		this.setLocationAndAngles(p_i1756_2_.posX, p_i1756_2_.posY + p_i1756_2_.getEyeHeight(), p_i1756_2_.posZ,
				p_i1756_2_.rotationYaw, p_i1756_2_.rotationPitch);
		this.posX -= MathHelper.cos(this.rotationYaw / 180.0F * (float) Math.PI) * 0.16F;
		this.posY -= 0.10000000149011612D;
		this.posZ -= MathHelper.sin(this.rotationYaw / 180.0F * (float) Math.PI) * 0.16F;
		this.setPosition(this.posX, this.posY, this.posZ);
		this.motionX = -MathHelper.sin(this.rotationYaw / 180.0F * (float) Math.PI)
				* MathHelper.cos(this.rotationPitch / 180.0F * (float) Math.PI);
		this.motionZ = MathHelper.cos(this.rotationYaw / 180.0F * (float) Math.PI)
				* MathHelper.cos(this.rotationPitch / 180.0F * (float) Math.PI);
		this.motionY = (-MathHelper.sin(this.rotationPitch / 180.0F * (float) Math.PI));
		this.shoot(this.motionX, this.motionY, this.motionZ, p_i1756_3_ * 1.5F, 1.0F);

		// this.dmgMin = dmgMin;
		// this.dmgMax = dmgMax;
		this.instakill = instakill;
		this.rad = rad;
	}

	public EntityBullet(World p_i1756_1_, EntityLivingBase p_i1756_2_, float p_i1756_3_) {
		super(p_i1756_1_);
		this.renderRangeWeight = 10.0D;
		this.shootingEntity = p_i1756_2_;

		if (p_i1756_2_ instanceof EntityPlayer) {
			this.canBePickedUp = 1;
		}

		this.setSize(0.5F, 0.5F);
		this.setLocationAndAngles(p_i1756_2_.posX, p_i1756_2_.posY + p_i1756_2_.getEyeHeight(), p_i1756_2_.posZ,
				p_i1756_2_.rotationYaw, p_i1756_2_.rotationPitch);
		this.posX -= MathHelper.cos(this.rotationYaw / 180.0F * (float) Math.PI) * 0.16F;
		this.posY -= 0.10000000149011612D;
		this.posZ -= MathHelper.sin(this.rotationYaw / 180.0F * (float) Math.PI) * 0.16F;
		this.setPosition(this.posX, this.posY, this.posZ);
		this.motionX = -MathHelper.sin(this.rotationYaw / 180.0F * (float) Math.PI)
				* MathHelper.cos(this.rotationPitch / 180.0F * (float) Math.PI);
		this.motionZ = MathHelper.cos(this.rotationYaw / 180.0F * (float) Math.PI)
				* MathHelper.cos(this.rotationPitch / 180.0F * (float) Math.PI);
		this.motionY = (-MathHelper.sin(this.rotationPitch / 180.0F * (float) Math.PI));
		this.setThrowableHeading2(this.motionX, this.motionY, this.motionZ, p_i1756_3_ * 1.5F, 1.0F);
	}

	public EntityBullet(World p_i1756_1_, EntityLivingBase p_i1756_2_, float p_i1756_3_, int dmgMin, int dmgMax,
			boolean instakill, String isTau) {
		super(p_i1756_1_);
		this.renderRangeWeight = 10.0D;
		this.shootingEntity = p_i1756_2_;

		if (p_i1756_2_ instanceof EntityPlayer) {
			this.canBePickedUp = 1;
		}

		this.setSize(0.5F, 0.5F);
		if(p_i1756_2_ != null) this.setLocationAndAngles(p_i1756_2_.posX, p_i1756_2_.posY + p_i1756_2_.getEyeHeight(), p_i1756_2_.posZ, p_i1756_2_.rotationYaw, p_i1756_2_.rotationPitch);
		this.posX -= MathHelper.cos(this.rotationYaw / 180.0F * (float) Math.PI) * 0.16F;
		this.posY -= 0.10000000149011612D;
		this.posZ -= MathHelper.sin(this.rotationYaw / 180.0F * (float) Math.PI) * 0.16F;
		this.setPosition(this.posX, this.posY, this.posZ);
		this.motionX = -MathHelper.sin(this.rotationYaw / 180.0F * (float) Math.PI)
				* MathHelper.cos(this.rotationPitch / 180.0F * (float) Math.PI);
		this.motionZ = MathHelper.cos(this.rotationYaw / 180.0F * (float) Math.PI)
				* MathHelper.cos(this.rotationPitch / 180.0F * (float) Math.PI);
		this.motionY = (-MathHelper.sin(this.rotationPitch / 180.0F * (float) Math.PI));
		this.shoot(this.motionX, this.motionY, this.motionZ, p_i1756_3_ * 1.5F, 1.0F);
		this.setTau(isTau == "tauDay");
		this.setChopper(isTau == "chopper");
		this.setIsCritical(isTau != "chopper");
	}

	//why the living shit did i make isTau a string? who knows, who cares.
	public EntityBullet(World p_i1756_1_, EntityLivingBase p_i1756_2_, float p_i1756_3_, int dmgMin, int dmgMax,
			boolean instakill, String isTau, EntityGrenadeTau grenade) {
		super(p_i1756_1_);
		this.renderRangeWeight = 10.0D;
		this.shootingEntity = p_i1756_2_;

		this.setSize(0.5F, 0.5F);
		this.setLocationAndAngles(grenade.posX, grenade.posY + grenade.getEyeHeight(), grenade.posZ,
				grenade.rotationYaw, grenade.rotationPitch);
		this.posX -= MathHelper.cos(this.rotationYaw / 180.0F * (float) Math.PI) * 0.16F;
		this.posY -= 0.10000000149011612D;
		this.posZ -= MathHelper.sin(this.rotationYaw / 180.0F * (float) Math.PI) * 0.16F;
		this.setPosition(this.posX, this.posY, this.posZ);
		this.motionX = -MathHelper.sin(this.rotationYaw / 180.0F * (float) Math.PI)
				* MathHelper.cos(this.rotationPitch / 180.0F * (float) Math.PI);
		this.motionZ = MathHelper.cos(this.rotationYaw / 180.0F * (float) Math.PI)
				* MathHelper.cos(this.rotationPitch / 180.0F * (float) Math.PI);
		this.motionY = (-MathHelper.sin(this.rotationPitch / 180.0F * (float) Math.PI));
		this.shoot(this.motionX, this.motionY, this.motionZ, p_i1756_3_ * 1.5F, 1.0F);
		this.setTau(isTau == "tauDay");
		this.setIsCritical(true);
	}

	public EntityBullet(World world, int x, int y, int z, double mx, double my, double mz, double grav) {
		super(world);
		this.posX = x + 0.5F;
		this.posY = y + 0.5F;
		this.posZ = z + 0.5F;

		this.motionX = mx;
		this.motionY = my;
		this.motionZ = mz;

		this.gravity = grav;
	}

	@Override
	protected void entityInit() {
		this.dataManager.register(CRITICAL_FLAGS, Byte.valueOf((byte) 0));
		this.dataManager.register(TAU_FLAGS, Byte.valueOf((byte) 0));
		this.dataManager.register(CHOPPER_FLAGS, Byte.valueOf((byte) 0));
	}

	/**
	 * Similar to setArrowHeading, it's point the throwable entity to a x, y, z
	 * direction.
	 */
	@Override
	public void shoot(double p_70186_1_, double p_70186_3_, double p_70186_5_, float p_70186_7_,
			float p_70186_8_) {
		float f2 = MathHelper.sqrt(p_70186_1_ * p_70186_1_ + p_70186_3_ * p_70186_3_ + p_70186_5_ * p_70186_5_);
		p_70186_1_ /= f2;
		p_70186_3_ /= f2;
		p_70186_5_ /= f2;
		p_70186_1_ += this.rand.nextGaussian() * (this.rand.nextBoolean() ? -1 : 1) * 0.007499999832361937D
				* p_70186_8_;
		p_70186_3_ += this.rand.nextGaussian() * (this.rand.nextBoolean() ? -1 : 1) * 0.007499999832361937D
				* p_70186_8_;
		p_70186_5_ += this.rand.nextGaussian() * (this.rand.nextBoolean() ? -1 : 1) * 0.007499999832361937D
				* p_70186_8_;
		p_70186_1_ *= p_70186_7_;
		p_70186_3_ *= p_70186_7_;
		p_70186_5_ *= p_70186_7_;
		this.motionX = p_70186_1_;
		this.motionY = p_70186_3_;
		this.motionZ = p_70186_5_;
		float f3 = MathHelper.sqrt(p_70186_1_ * p_70186_1_ + p_70186_5_ * p_70186_5_);
		this.prevRotationYaw = this.rotationYaw = (float) (Math.atan2(p_70186_1_, p_70186_5_) * 180.0D / Math.PI);
		this.prevRotationPitch = this.rotationPitch = (float) (Math.atan2(p_70186_3_, f3) * 180.0D / Math.PI);
		this.ticksInGround = 0;
	}

	public void setThrowableHeading2(double p_70186_1_, double p_70186_3_, double p_70186_5_, float p_70186_7_,
			float p_70186_8_) {
		float f2 = MathHelper.sqrt(p_70186_1_ * p_70186_1_ + p_70186_3_ * p_70186_3_ + p_70186_5_ * p_70186_5_);
		p_70186_1_ /= f2;
		p_70186_3_ /= f2;
		p_70186_5_ /= f2;
		p_70186_1_ += this.rand.nextGaussian() * (this.rand.nextBoolean() ? -1 : 1) * 0.042499999832361937D
				* p_70186_8_;
		p_70186_3_ += this.rand.nextGaussian() * (this.rand.nextBoolean() ? -1 : 1) * 0.042499999832361937D
				* p_70186_8_;
		p_70186_5_ += this.rand.nextGaussian() * (this.rand.nextBoolean() ? -1 : 1) * 0.042499999832361937D
				* p_70186_8_;
		p_70186_1_ *= p_70186_7_;
		p_70186_3_ *= p_70186_7_;
		p_70186_5_ *= p_70186_7_;
		this.motionX = p_70186_1_;
		this.motionY = p_70186_3_;
		this.motionZ = p_70186_5_;
		float f3 = MathHelper.sqrt(p_70186_1_ * p_70186_1_ + p_70186_5_ * p_70186_5_);
		this.prevRotationYaw = this.rotationYaw = (float) (Math.atan2(p_70186_1_, p_70186_5_) * 180.0D / Math.PI);
		this.prevRotationPitch = this.rotationPitch = (float) (Math.atan2(p_70186_3_, f3) * 180.0D / Math.PI);
		this.ticksInGround = 0;
	}

	/**
	 * Sets the position and rotation. Only difference from the other one is no
	 * bounding on the rotation. Args: posX, posY, posZ, yaw, pitch
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public void setPositionAndRotationDirect(double p_70056_1_, double p_70056_3_, double p_70056_5_, float p_70056_7_,
			float p_70056_8_, int p_70056_9_, boolean teleport) {
		this.setPosition(p_70056_1_, p_70056_3_, p_70056_5_);
		this.setRotation(p_70056_7_, p_70056_8_);
	}

	/**
	 * Sets the velocity to the args. Args: x, y, z
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public void setVelocity(double p_70016_1_, double p_70016_3_, double p_70016_5_) {
		this.motionX = p_70016_1_;
		this.motionY = p_70016_3_;
		this.motionZ = p_70016_5_;

		if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F) {
			float f = MathHelper.sqrt(p_70016_1_ * p_70016_1_ + p_70016_5_ * p_70016_5_);
			this.prevRotationYaw = this.rotationYaw = (float) (Math.atan2(p_70016_1_, p_70016_5_) * 180.0D / Math.PI);
			this.prevRotationPitch = this.rotationPitch = (float) (Math.atan2(p_70016_3_, f) * 180.0D / Math.PI);
			this.prevRotationPitch = this.rotationPitch;
			this.prevRotationYaw = this.rotationYaw;
			this.setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationYaw, this.rotationPitch);
			this.ticksInGround = 0;
		}
	}

	/**
	 * Called to update the entity's position/logic.
	 */
	@Override
	public void onUpdate() {
		super.onUpdate();

		if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F) {
			MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
			this.prevRotationYaw = this.rotationYaw = (float) (Math.atan2(this.motionX, this.motionZ) * 180.0D
					/ Math.PI);
			// this.prevRotationPitch = this.rotationPitch =
			// (float)(Math.atan2(this.motionY, (double)f) * 180.0D / Math.PI);
		}

		net.minecraft.util.math.BlockPos hitPos = new net.minecraft.util.math.BlockPos(this.tileX, this.tileY, this.tileZ);
		net.minecraft.block.state.IBlockState state = this.world.getBlockState(hitPos);
		Block block = state.getBlock();

		if (state.getMaterial() != Material.AIR) {
			AxisAlignedBB axisalignedbb = state.getCollisionBoundingBox(this.world, hitPos);

			if (axisalignedbb != null
					&& axisalignedbb.offset(hitPos).contains(new Vec3d(this.posX, this.posY, this.posZ))
					&& !this.getIsCritical()) {
				this.inGround = true;
			}

			if(block instanceof BlockDetonatable) {
				((BlockDetonatable) block).onShot(this.world, this.tileX, this.tileY, this.tileZ);
			}

			if (block == Blocks.GLASS || block == Blocks.STAINED_GLASS || block == Blocks.GLASS_PANE
					|| block == Blocks.STAINED_GLASS_PANE) {
				com.hbm.world.WorldBlockState.write(this.world, this.tileX, this.tileY, this.tileZ, Blocks.AIR.getStateFromMeta(0), 3);
				this.world.playSound(null, new BlockPos(this.tileX, this.tileY, this.tileZ), SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.BLOCKS, 1.0F, 1.0F);
			}
		}

		if (this.arrowShake > 0) {
			--this.arrowShake;
		}

		if (this.inGround && !this.getIsCritical()) {
			this.setDead();

		} else {
			++this.ticksInAir;
			Vec3d vec31 = new Vec3d(this.posX, this.posY, this.posZ);
			Vec3d vec3 = new Vec3d(this.posX + this.motionX, this.posY + this.motionY,
					this.posZ + this.motionZ);
			RayTraceResult movingobjectposition = this.world.rayTraceBlocks(vec31, vec3, false, true, false);
			vec31 = new Vec3d(this.posX, this.posY, this.posZ);
			vec3 = new Vec3d(this.posX + this.motionX, this.posY + this.motionY,
					this.posZ + this.motionZ);

			if (movingobjectposition != null) {
				vec3 = new Vec3d(movingobjectposition.hitVec.x, movingobjectposition.hitVec.y,
						movingobjectposition.hitVec.z);
			}

			Entity entity = null;
			List list = this.world.getEntitiesWithinAABBExcludingEntity(this,
					this.getEntityBoundingBox().expand(this.motionX, this.motionY, this.motionZ).grow(1.0D));
			double d0 = 0.0D;
			int i;
			float f1;

			for (i = 0; i < list.size(); ++i) {
				Entity entity1 = (Entity) list.get(i);

				if (entity1.canBeCollidedWith() && (entity1 != this.shootingEntity || this.ticksInAir >= 5)) {
					f1 = 0.3F;
					AxisAlignedBB axisalignedbb1 = entity1.getEntityBoundingBox().expand(f1, f1, f1);
					RayTraceResult movingobjectposition1 = axisalignedbb1.calculateIntercept(vec31, vec3);

					if (movingobjectposition1 != null) {
						double d1 = vec31.distanceTo(movingobjectposition1.hitVec);

						if (d1 < d0 || d0 == 0.0D) {
							entity = entity1;
							d0 = d1;
						}
					}
				}
			}

			if (entity != null) {
				movingobjectposition = new RayTraceResult(entity);
			}

			if (movingobjectposition != null && movingobjectposition.entityHit != null
					&& movingobjectposition.entityHit instanceof EntityPlayer) {
				EntityPlayer entityplayer = (EntityPlayer) movingobjectposition.entityHit;

				if (entityplayer.capabilities.disableDamage || this.shootingEntity instanceof EntityPlayer
						&& !((EntityPlayer) this.shootingEntity).canAttackPlayer(entityplayer)) {
					movingobjectposition = null;
				}
			}

			float f2;
			float f4;

			if (movingobjectposition != null) {
				if (movingobjectposition.entityHit != null) {
					//TODO: Remove test feature in release version
					if (!(movingobjectposition.entityHit instanceof EntityItemFrame)
							|| movingobjectposition.entityHit instanceof EntityItemFrame
									&& (((EntityItemFrame) movingobjectposition.entityHit).getDisplayedItem() == null
											|| ((EntityItemFrame) movingobjectposition.entityHit)
													.getDisplayedItem() != null
													&& ((EntityItemFrame) movingobjectposition.entityHit)
															.getDisplayedItem().getItem() != ModItems.flame_pony)) {
						f2 = MathHelper.sqrt(this.motionX * this.motionX + this.motionY * this.motionY
								+ this.motionZ * this.motionZ);
						int k = MathHelper.ceil(f2 * this.damage);

						if (this.getIsCritical()) {
							k += this.rand.nextInt(k / 2 + 2);
						}

						DamageSource damagesource = null;

						//L: Crit
						//R: Chop
						//X: NOT
						//O: Direct

						//   X X   Bullet
						//    \|
						//   O-X   Tau
						//   |/
						//   X-O   Displacer

						if (!this.getIsCritical() && !this.getIsChopper()) {
							if (this.shootingEntity == null) {
								damagesource = ModDamageSource.causeBulletDamage(this, this);
							} else {
								damagesource = ModDamageSource.causeBulletDamage(this, shootingEntity);
							}
						} else if(!this.getIsChopper()) {
							if (this.shootingEntity == null) {
								damagesource = ModDamageSource.causeTauDamage(this, this);
							} else {
								damagesource = ModDamageSource.causeTauDamage(this, shootingEntity);
							}
						} else if(!this.getIsCritical()) {
							if (this.shootingEntity == null) {
								damagesource = ModDamageSource.causeDisplacementDamage(this, this);
							} else {
								damagesource = ModDamageSource.causeDisplacementDamage(this, shootingEntity);
							}
						}

						if (fire || this.isBurning() && !(movingobjectposition.entityHit instanceof EntityEnderman)) {
							movingobjectposition.entityHit.setFire(5);
						}

						if (movingobjectposition.entityHit.attackEntityFrom(damagesource, (float) damage)) {
							if (movingobjectposition.entityHit instanceof EntityLivingBase) {
								EntityLivingBase entitylivingbase = (EntityLivingBase) movingobjectposition.entityHit;

								if (rad) {
									if (entitylivingbase instanceof EntityPlayer
											&& ArmorUtil.checkForHazmat((EntityPlayer) entitylivingbase)) {
									} else if (entitylivingbase instanceof EntityCreeper) {
										EntityCreeperNuclear creep = new EntityCreeperNuclear(this.world);
										creep.setLocationAndAngles(entitylivingbase.posX, entitylivingbase.posY, entitylivingbase.posZ,
												entitylivingbase.rotationYaw, entitylivingbase.rotationPitch);
										if (!entitylivingbase.isDead)
											if (!this.world.isRemote)
												this.world.spawnEntity(creep);
										entitylivingbase.setDead();
									} else if (entitylivingbase instanceof EntityVillager) {
										EntityZombie creep = new EntityZombie(this.world);
										creep.setLocationAndAngles(entitylivingbase.posX, entitylivingbase.posY, entitylivingbase.posZ,
												entitylivingbase.rotationYaw, entitylivingbase.rotationPitch);
										entitylivingbase.setDead();
										if (!this.world.isRemote)
											this.world.spawnEntity(creep);
									} else if (entitylivingbase instanceof EntityLivingBase
											&& !(entitylivingbase instanceof EntityCreeperNuclear)
											&& !(entitylivingbase instanceof EntityMooshroom)
											&& !(entitylivingbase instanceof EntityZombie)) {
										entitylivingbase.addPotionEffect(new PotionEffect(net.minecraft.init.MobEffects.POISON, 2 * 60 * 20, 2));
										entitylivingbase.addPotionEffect(new PotionEffect(net.minecraft.init.MobEffects.WITHER, 20, 4));
										entitylivingbase.addPotionEffect(new PotionEffect(net.minecraft.init.MobEffects.SLOWNESS, 1 * 60 * 20, 1));
									}
								}

								if(antidote)
									entitylivingbase.clearActivePotions();

								if (this.knockbackStrength > 0) {
									f4 = MathHelper
											.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);

									if (f4 > 0.0F) {
										movingobjectposition.entityHit.addVelocity(
												this.motionX * this.knockbackStrength * 0.6000000238418579D / f4, 0.1D,
												this.motionZ * this.knockbackStrength * 0.6000000238418579D / f4);
									}
								}

								if (this.shootingEntity != null && this.shootingEntity instanceof EntityLivingBase) {
									EnchantmentHelper.applyThornEnchantments(entitylivingbase, this.shootingEntity);
									EnchantmentHelper.applyArthropodEnchantments((EntityLivingBase) this.shootingEntity,
											entitylivingbase);
								}

								if (this.shootingEntity != null && movingobjectposition.entityHit != this.shootingEntity
										&& movingobjectposition.entityHit instanceof EntityPlayer
										&& this.shootingEntity instanceof EntityPlayerMP) {
									((EntityPlayerMP) this.shootingEntity).connection
											.sendPacket(new SPacketChangeGameState(6, 0.0F));
								}
							}

							if (!(movingobjectposition.entityHit instanceof EntityEnderman)) {
								if (!this.world.isRemote) {
									if (!instakill || movingobjectposition.entityHit instanceof EntityPlayer) {
										// movingobjectposition.entityHit.attackEntityFrom(DamageSource.GENERIC,
										// dmgMin + rand.nextInt(dmgMax -
										// dmgMin));
									} else if (movingobjectposition.entityHit instanceof EntityLivingBase) {
										((EntityLivingBase) movingobjectposition.entityHit).setHealth(0.0F);
									}
								}
								if (!this.getIsCritical())
									//this.setDead();
									;
							}
						} else {

							if(movingobjectposition.entityHit instanceof EntityLivingBase) {

								try {
									Field lastDamage = ReflectionHelper.findField(EntityLivingBase.class, "lastDamage", "field_110153_bc");

									float dmg = (float) damage + lastDamage.getFloat(movingobjectposition.entityHit);

									movingobjectposition.entityHit.attackEntityFrom(damagesource, dmg);
								} catch (Exception x) { }
							}

						}





						/* else {
							if (movingobjectposition.entityHit instanceof EntityLivingBase && !(movingobjectposition.entityHit instanceof EntityHunterChopper)) {
								EntityLivingBase target = (EntityLivingBase) movingobjectposition.entityHit;
								target.setHealth((float) (target.getHealth() - damage));
							}
						}*/
					} else {
						this.setDead();
					}
				} else if (!this.getIsCritical()) {
					this.tileX = movingobjectposition.getBlockPos().getX();
					this.tileY = movingobjectposition.getBlockPos().getY();
					this.tileZ = movingobjectposition.getBlockPos().getZ();
					this.field_145790_g = com.hbm.world.WorldBlockState.read(this.world, this.tileX, this.tileY,
							this.tileZ).getBlock();
					this.inData = com.hbm.world.WorldBlockState.readMetadata(this.world, this.tileX, this.tileY,
							this.tileZ);
					this.motionX = ((float) (movingobjectposition.hitVec.x - this.posX));
					this.motionY = ((float) (movingobjectposition.hitVec.y - this.posY));
					this.motionZ = ((float) (movingobjectposition.hitVec.z - this.posZ));
					f2 = MathHelper.sqrt(
							this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);
					this.posX -= this.motionX / f2 * 0.05000000074505806D;
					this.posY -= this.motionY / f2 * 0.05000000074505806D;
					this.posZ -= this.motionZ / f2 * 0.05000000074505806D;
					this.inGround = true;
					this.arrowShake = 7;

					if (this.field_145790_g.getDefaultState().getMaterial() != Material.AIR) {
						BlockPos bpos = new BlockPos(this.tileX, this.tileY, this.tileZ);
								this.field_145790_g.onEntityCollision(this.world, bpos, this.world.getBlockState(bpos), this);
					}
				}
			}

			if (this.getIsCritical()) {
				for (i = 0; i < 8; ++i) {
					if (!this.getIsTau())
						this.world.spawnParticle(net.minecraft.util.EnumParticleTypes.FIREWORKS_SPARK, this.posX + this.motionX * i / 8.0D,
								this.posY + this.motionY * i / 8.0D,
								this.posZ + this.motionZ * i / 8.0D, 0, 0,
								0/*-this.motionX, -this.motionY + 0.2D, -this.motionZ*/);
					else
						this.world.spawnParticle(net.minecraft.util.EnumParticleTypes.REDSTONE, this.posX + this.motionX * i / 8.0D,
								this.posY + this.motionY * i / 8.0D,
								this.posZ + this.motionZ * i / 8.0D, 0, 0,
								0/*-this.motionX, -this.motionY + 0.2D, -this.motionZ*/);
				}
			}

			this.posX += this.motionX;
			this.posY += this.motionY;
			this.posZ += this.motionZ;
			f2 = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
			this.rotationYaw = (float) (Math.atan2(this.motionX, this.motionZ) * 180.0D / Math.PI);

			// for (this.rotationPitch = (float)(Math.atan2(this.motionY,
			// (double)f2) * 180.0D / Math.PI); this.rotationPitch -
			// this.prevRotationPitch < -180.0F; this.prevRotationPitch -=
			// 360.0F)
			{
				;
			}

			/*
			 * while (this.rotationPitch - this.prevRotationPitch >= 180.0F) {
			 * this.prevRotationPitch += 360.0F; }
			 *
			 * while (this.rotationYaw - this.prevRotationYaw < -180.0F) {
			 * this.prevRotationYaw -= 360.0F; }
			 *
			 * while (this.rotationYaw - this.prevRotationYaw >= 180.0F) {
			 * this.prevRotationYaw += 360.0F; }
			 */

			// this.rotationPitch = this.prevRotationPitch + (this.rotationPitch
			// - this.prevRotationPitch) * 0.2F;
			// this.rotationYaw = this.prevRotationYaw + (this.rotationYaw -
			// this.prevRotationYaw) * 0.2F;
			float f3 = 0.99F;
			f1 = 0.05F;

			if (this.isInWater()) {
				for (int l = 0; l < 4; ++l) {
					f4 = 0.25F;
					this.world.spawnParticle(net.minecraft.util.EnumParticleTypes.WATER_BUBBLE, this.posX - this.motionX * f4, this.posY - this.motionY * f4,
							this.posZ - this.motionZ * f4, this.motionX, this.motionY, this.motionZ);
				}

				f3 = 0.8F;
			}

			if (this.isWet()) {
				this.extinguish();
			}

			this.motionX *= f3;
			this.motionY *= f3;
			this.motionZ *= f3;
			this.motionY -= gravity;
			this.setPosition(this.posX, this.posY, this.posZ);
			this.doBlockCollisions();
		}

		if (this.ticksExisted > 250)
			this.setDead();
	}

	/**
	 * (abstract) Protected helper method to write subclass entity data to NBT.
	 */
	@Override
	public void writeEntityToNBT(NBTTagCompound p_70014_1_) {
		p_70014_1_.setShort("xTile", (short) this.tileX);
		p_70014_1_.setShort("yTile", (short) this.tileY);
		p_70014_1_.setShort("zTile", (short) this.tileZ);
		p_70014_1_.setShort("life", (short) this.ticksInGround);
		p_70014_1_.setByte("inTile", (byte) Block.getIdFromBlock(this.field_145790_g));
		p_70014_1_.setByte("inData", (byte) this.inData);
		p_70014_1_.setByte("shake", (byte) this.arrowShake);
		p_70014_1_.setByte("inGround", (byte) (this.inGround ? 1 : 0));
		p_70014_1_.setByte("pickup", (byte) this.canBePickedUp);
		p_70014_1_.setDouble("damage", this.damage);
	}

	/**
	 * (abstract) Protected helper method to read subclass entity data from NBT.
	 */
	@Override
	public void readEntityFromNBT(NBTTagCompound p_70037_1_) {
		this.tileX = p_70037_1_.getShort("xTile");
		this.tileY = p_70037_1_.getShort("yTile");
		this.tileZ = p_70037_1_.getShort("zTile");
		this.ticksInGround = p_70037_1_.getShort("life");
		this.field_145790_g = Block.getBlockById(p_70037_1_.getByte("inTile") & 255);
		this.inData = p_70037_1_.getByte("inData") & 255;
		this.arrowShake = p_70037_1_.getByte("shake") & 255;
		this.inGround = p_70037_1_.getByte("inGround") == 1;

		if (p_70037_1_.hasKey("damage", 99)) {
			this.damage = p_70037_1_.getDouble("damage");
		}

		if (p_70037_1_.hasKey("pickup", 99)) {
			this.canBePickedUp = p_70037_1_.getByte("pickup");
		} else if (p_70037_1_.hasKey("player", 99)) {
			this.canBePickedUp = p_70037_1_.getBoolean("player") ? 1 : 0;
		}
	}

	/**
	 * Called by a player entity when they collide with an entity
	 */
	@Override
	public void onCollideWithPlayer(EntityPlayer p_70100_1_) {
		if (!this.world.isRemote && this.inGround && this.arrowShake <= 0) {
			boolean flag = this.canBePickedUp == 1 || this.canBePickedUp == 2 && p_70100_1_.capabilities.isCreativeMode;

			if (flag) {
				p_70100_1_.onItemPickup(this, 1);
				this.setDead();
			}
		}
	}

	/**
	 * returns if this entity triggers Block.onEntityWalking on the blocks they
	 * walk on. used for spiders and wolves to prevent them from trampling crops
	 */
	@Override
	protected boolean canTriggerWalking() {
		return false;
	}

	@SideOnly(Side.CLIENT)
	public float getShadowSize() {
		return 0.0F;
	}

	public void setDamage(double p_70239_1_) {
		this.damage = p_70239_1_;
	}

	public double getDamage() {
		return this.damage;
	}

	/**
	 * Sets the amount of knockback the arrow applies when it hits a mob.
	 */
	public void setKnockbackStrength(int p_70240_1_) {
		this.knockbackStrength = p_70240_1_;
	}

	/**
	 * If returns false, the item will not inflict any damage against entities.
	 */
	@Override
	public boolean canBeAttackedWithItem() {
		return false;
	}

	/**
	 * Whether the arrow has a stream of critical hit particles flying behind
	 * it.
	 */
	public void setIsCritical(boolean p_70243_1_) {
		byte b0 = this.dataManager.get(CRITICAL_FLAGS);

		if (p_70243_1_) {
			this.dataManager.set(CRITICAL_FLAGS, Byte.valueOf((byte) (b0 | 1)));
		} else {
			this.dataManager.set(CRITICAL_FLAGS, Byte.valueOf((byte) (b0 & -2)));
		}
	}

	public void setTau(boolean p_70243_1_) {
		byte b0 = this.dataManager.get(TAU_FLAGS);

		if (p_70243_1_) {
			this.dataManager.set(TAU_FLAGS, Byte.valueOf((byte) (b0 | 1)));
		} else {
			this.dataManager.set(TAU_FLAGS, Byte.valueOf((byte) (b0 & -2)));
		}
	}

	public void setChopper(boolean p_70243_1_) {
		byte b0 = this.dataManager.get(CHOPPER_FLAGS);

		if (p_70243_1_) {
			this.dataManager.set(CHOPPER_FLAGS, Byte.valueOf((byte) (b0 | 1)));
		} else {
			this.dataManager.set(CHOPPER_FLAGS, Byte.valueOf((byte) (b0 & -2)));
		}
	}

	/**
	 * Whether the arrow has a stream of critical hit particles flying behind
	 * it.
	 */
	public boolean getIsCritical() {
		byte b0 = this.dataManager.get(CRITICAL_FLAGS);
		return (b0 & 1) != 0;
	}

	public boolean getIsTau() {
		byte b0 = this.dataManager.get(TAU_FLAGS);
		return (b0 & 1) != 0;
	}

	public boolean getIsChopper() {
		byte b0 = this.dataManager.get(CHOPPER_FLAGS);
		return (b0 & 1) != 0;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getBrightnessForRender() {
		if(this.getIsCritical() || this.getIsChopper())
			return 15728880;
		else
			return super.getBrightnessForRender();
	}

	@Override
	public float getBrightness() {
		if(this.getIsCritical() || this.getIsChopper())
			return 1.0F;
		else
			return super.getBrightness();
	}
}
