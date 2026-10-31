package com.hbm.entity.mob.siege;

import java.util.List;

import com.hbm.entity.mob.EntityUFOBase;
import com.hbm.entity.projectile.EntitySiegeLaser;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.lib.ModDamageSource;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.util.ContaminationUtil;
import com.hbm.util.ContaminationUtil.ContaminationType;
import com.hbm.util.ContaminationUtil.HazardType;

import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import com.hbm.entity.boss.INtmBossDisplay;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;


public class EntitySiegeCraft extends EntityUFOBase implements INtmBossDisplay {
	private static final DataParameter<Integer> SIEGE_TIER = EntityDataManager.createKey(EntitySiegeCraft.class, DataSerializers.VARINT);
	private static final DataParameter<Float> LOCKON_X = EntityDataManager.createKey(EntitySiegeCraft.class, DataSerializers.FLOAT);
	private static final DataParameter<Float> LOCKON_Y = EntityDataManager.createKey(EntitySiegeCraft.class, DataSerializers.FLOAT);
	private static final DataParameter<Float> LOCKON_Z = EntityDataManager.createKey(EntitySiegeCraft.class, DataSerializers.FLOAT);
	private static final DataParameter<Byte> BEAM = EntityDataManager.createKey(EntitySiegeCraft.class, DataSerializers.BYTE);


	private int attackCooldown;
	private int beamCountdown;

	private int lifetime = 0;
	private boolean isRetreating = false;
	private static final int MAX_LIFETIME = 60 * 4 * 20;

	public EntitySiegeCraft(World world) {
		super(world);
		this.setSize(7F, 1F);
		this.isImmuneToFire = true;
		this.ignoreFrustumCheck = true;
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float damage) {

		if(this.isEntityInvulnerable(source))
			return false;

		SiegeTier tier = this.getTier();

		if(tier.fireProof && source.isFireDamage()) {
			this.extinguish();
			return false;
		}

		//noFF can't be harmed by other mobs
		if(tier.noFriendlyFire && source instanceof EntityDamageSource && !(((EntityDamageSource) source).getTrueSource() instanceof EntityPlayer))
			return false;

		damage -= tier.dt;

		if(damage < 0) {
			this.playSound(net.minecraft.init.SoundEvents.ENTITY_ITEM_BREAK, 5F, 1.0F + rand.nextFloat() * 0.5F);
			return false;
		}

		damage *= (1F - tier.dr);

		return super.attackEntityFrom(source, damage);
	}

	@Override
	protected void onDeathUpdate() {

		this.beamCountdown = 200;
		this.setBeam(false);

		this.motionY -= 0.05D;

		if(this.deathTime == 19 && !this.world.isRemote) {

			NBTTagCompound data = new NBTTagCompound();
			data.setString("type", "tinytot");
			PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, posX, posY + 0.5, posZ), new TargetPoint(this.dimension, posX, posY, posZ, 250));
			com.hbm.sound.WorldSoundEffects.play(this.world, posX, posY, posZ, "hbm:weapon.mukeExplosion", 15.0F, 1.0F);
		}

		super.onDeathUpdate();
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.getDataManager().register(SIEGE_TIER, (int) 0);
		this.getDataManager().register(LOCKON_X, 0F);
		this.getDataManager().register(LOCKON_Y, 0F);
		this.getDataManager().register(LOCKON_Z, 0F);
		this.getDataManager().register(BEAM, (byte) 0);
	}

	public void setTier(SiegeTier tier) {
		this.getDataManager().set(SIEGE_TIER, tier.id);

		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(tier.speedMod);
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(tier.health * 25);
		this.setHealth(this.getMaxHealth());
	}

	public SiegeTier getTier() {
		SiegeTier tier = SiegeTier.tiers[this.getDataManager().get(SIEGE_TIER)];
		return tier != null ? tier : SiegeTier.CLAY;
	}

	public void setBeam(boolean beam) {
		this.getDataManager().set(BEAM, beam ? (byte) 1 : (byte) 0);
	}

	public boolean getBeam() {
		return this.getDataManager().get(BEAM) == 1;
	}

	public void setLockon(double x, double y, double z) {
		this.getDataManager().set(LOCKON_X, (float) x);
		this.getDataManager().set(LOCKON_Y, (float) y);
		this.getDataManager().set(LOCKON_Z, (float) z);
	}

	public Vec3d getLockon() {
		return new Vec3d(
				this.getDataManager().get(LOCKON_X),
				this.getDataManager().get(LOCKON_Y),
				this.getDataManager().get(LOCKON_Z)
				);
	}

	@Override
	protected int getScanRange() {
		return 100;
	}

	@Override
	protected int targetHeightOffset() {
		return 7 + rand.nextInt(5);
	}

	@Override
	protected int wanderHeightOffset() {
		return 10 + rand.nextInt(2);
	}

	@Override
	protected void updateEntityActionStateNT() {
		super.updateEntityActionStateNT();

		if(this.courseChangeCooldown > 0) {
			this.courseChangeCooldown--;
		}
		if(this.scanCooldown > 0) {
			this.scanCooldown--;
		}

		if(!this.world.isRemote) {

			lifetime++;
			if(lifetime > MAX_LIFETIME) {
				isRetreating = true;
			}

			if(isRetreating) {
				this.target = null;
				this.setWaypoint((int)posX, 255, (int)posZ);
				approachPosition(20);
				if(this.posY > 250 || lifetime > MAX_LIFETIME + 300) {
					this.setDead();
					return;
				}
			}


			if(this.attackCooldown > 0) {
				this.attackCooldown--;
			}
			if(this.beamCountdown > 0) {
				this.beamCountdown--;
			}

			if(rand.nextInt(50) == 0) {

				NBTTagCompound dPart = new NBTTagCompound();
				dPart.setString("type", "tau");
				dPart.setByte("count", (byte)(2 + rand.nextInt(3)));
				PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(dPart, posX + rand.nextGaussian() * 2, posY + rand.nextGaussian(), posZ + rand.nextGaussian() * 2), new TargetPoint(this.world.provider.getDimension(), posX, posY, posZ, 50));
			}

			boolean beam = false;

			if(this.target == null || this.beamCountdown <= 0) {
				this.beamCountdown = 300; //200 - 100: nothing, 100 - 40: update lockon, 40 - 20: fix lockon, 20 - 0: beam
			} else {

				if(this.beamCountdown >= 60 && this.beamCountdown < 120) {
					double x = this.target.posX;
					double y = this.target.posY + this.target.height * 0.5;
					double z = this.target.posZ;
					this.setLockon(x, y, z);

					if(this.beamCountdown == 110) {
						this.target.playSound(com.hbm.main.ModSoundEvents.resolve("hbm:weapon.stingerLockOn"), 2F, 0.75F);
					}
				}

				if(this.beamCountdown >= 40 && this.beamCountdown < 100) {

					Vec3d lockon = this.getLockon();
					NBTTagCompound fx = new NBTTagCompound();
					fx.setString("type", "vanillaburst");
					fx.setString("mode", "reddust");
					fx.setDouble("motion", 0.2D);
					fx.setInteger("count", 5);
					PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(fx, lockon.x, lockon.y, lockon.z), new TargetPoint(this.dimension, lockon.x, lockon.y, lockon.z, 100));
				}

				if(this.beamCountdown < 40) {

					Vec3d lockon = this.getLockon();

					if(this.beamCountdown == 39) {
						com.hbm.sound.WorldSoundEffects.play(this.world, lockon.x, lockon.y, lockon.z, "hbm:entity.ufoBlast", 5.0F, 0.9F + this.world.rand.nextFloat() * 0.2F);
					}

					List<Entity> entities = this.world.getEntitiesWithinAABBExcludingEntity(this, new AxisAlignedBB(lockon.x, lockon.y, lockon.z, lockon.x, lockon.y, lockon.z).grow(2, 2, 2));

					for(Entity e : entities) {
						if(e instanceof EntityLivingBase && this.canAttackClass(((EntityLivingBase) e).getClass())) {
							e.attackEntityFrom(ModDamageSource.causeCombineDamage(this, e), 1000F);
							e.setFire(5);

							if(e instanceof EntityLivingBase)
								ContaminationUtil.contaminate((EntityLivingBase)e, HazardType.RADIATION, ContaminationType.CREATIVE, 5F);
						}
					}


					NBTTagCompound data = new NBTTagCompound();
					data.setString("type", "plasmablast");
					data.setFloat("r", 0.0F);
					data.setFloat("g", 0.75F);
					data.setFloat("b", 1.0F);
					data.setFloat("pitch", -90 + rand.nextFloat() * 180);
					data.setFloat("yaw", rand.nextFloat() * 180F);
					data.setFloat("scale", 5F);
					PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, lockon.x, lockon.y, lockon.z),  new TargetPoint(dimension, lockon.x, lockon.y, lockon.z, 150));
					beam = true;
				}
			}

			this.setBeam(beam);

			if(this.attackCooldown == 0 && this.target != null) {
				this.attackCooldown = 30 + rand.nextInt(10);

				double x = posX;
				double y = posY;
				double z = posZ;

				Vec3d vec = new Vec3d(target.posX - x, target.posY + target.height * 0.5 - y, target.posZ - z).normalize();
				SiegeTier tier = this.getTier();

				float health = getHealth() / getMaxHealth();

				int r = (int)(0xff * (1 - health));
				int g = (int)(0xff * health);
				int b = 0;
				int color = (r << 16) | (g << 8) | b;

				for(int i = 0; i < 7; i++) {

					Vec3d copy = new Vec3d(vec.x, vec.y, vec.z);

					copy = copy.rotateYaw((float)Math.PI / 180F * (i - 3) * 5F);

					EntitySiegeLaser laser = new EntitySiegeLaser(this.world, this);
					laser.setPosition(x, y, z);
					laser.shoot(copy.x, copy.y, copy.z, 1F, 0.0F);
					laser.setColor(color);
					laser.setDamage(tier.damageMod);
					laser.setBreakChance(tier.laserBreak * 2);
					if(tier.laserIncendiary) laser.setIncendiary();
					this.world.spawnEntity(laser);
				}

				this.playSound(com.hbm.main.ModSoundEvents.resolve("hbm:weapon.ballsLaser"), 2.0F, 1.0F);
			}
		}

		if(this.courseChangeCooldown > 0) {
			    double speed = this.target == null ? 0.5D : 1.0D + (this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue() * 3.0D);

			    if(isRetreating) speed *= 2.0D;

			    approachPosition(speed);
			    }
	}

	@Override
	protected void setCourseWithoutTaget() {
		int x = (int) Math.floor(posX + rand.nextGaussian() * 15);
		int z = (int) Math.floor(posZ + rand.nextGaussian() * 15);
		this.setWaypoint(x, this.world.getHeight(x, z) + 5 + rand.nextInt(6),  z);
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbt) {
		super.writeEntityToNBT(nbt);
		nbt.setInteger("siegeTier", this.getTier().id);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbt) {
		super.readEntityFromNBT(nbt);
		this.setTier(SiegeTier.tiers[nbt.getInteger("siegeTier")]);
	}

	@Override
	public IEntityLivingData onInitialSpawn(net.minecraft.world.DifficultyInstance difficulty, IEntityLivingData data) {
		this.setTier(SiegeTier.tiers[rand.nextInt(SiegeTier.getLength())]);
		return super.onInitialSpawn(difficulty, data);
	}

	@Override
	protected void dropFewItems(boolean byPlayer, int fortune) {

		if(byPlayer) {
			for(ItemStack drop : this.getTier().dropItem) {
				this.entityDropItem(drop.copy(), 0F);
			}
		}
	}
}
