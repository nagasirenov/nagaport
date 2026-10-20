package com.hbm.entity.projectile;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.hbm.blocks.bomb.BlockDetonatable;
import com.hbm.entity.effect.EntityCloudFleijaRainbow;
import com.hbm.entity.effect.EntityEMPBlast;
import com.hbm.entity.logic.EntityNukeExplosionMK3;
import com.hbm.entity.logic.EntityNukeExplosionMK5;
import com.hbm.explosion.ExplosionLarge;
import com.hbm.explosion.ExplosionNukeGeneric;
import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.explosion.vanillant.standard.BlockAllocatorStandard;
import com.hbm.explosion.vanillant.standard.BlockMutatorFire;
import com.hbm.explosion.vanillant.standard.BlockProcessorNoDamage;
import com.hbm.explosion.vanillant.standard.BlockProcessorStandard;
import com.hbm.explosion.vanillant.standard.EntityProcessorStandard;
import com.hbm.explosion.vanillant.standard.ExplosionEffectStandard;
import com.hbm.explosion.vanillant.standard.PlayerProcessorStandard;
import com.hbm.handler.BulletConfigSyncingUtil;
import com.hbm.handler.BulletConfiguration;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.main.MainRegistry;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.potion.HbmPotion;
import com.hbm.util.ArmorUtil;
import com.hbm.util.BobMathUtil;
import com.hbm.util.Tuple.Pair;

import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.world.World;

/**
 * MK2 which features several improvements:
 * - uses generic throwable code, reducing boilerplate nonsense
 * - uses approach-based interpolation, preventing desyncs and making movement silky-smooth
 * - new adjustments in the base class allow for multiple MOP impacts per frame
 * - also comes with tons of legacy code to ensure compat (sadly)
 * @author hbm
 */
@Deprecated
public class EntityBulletBaseNT extends EntityThrowableInterp implements IBulletBase {

	private static final DataParameter<Byte> RENDER_STYLE = EntityDataManager.createKey(EntityBulletBaseNT.class, DataSerializers.BYTE);
	private static final DataParameter<Byte> RENDER_TRAIL = EntityDataManager.createKey(EntityBulletBaseNT.class, DataSerializers.BYTE);
	private static final DataParameter<Integer> BULLET_CONFIG = EntityDataManager.createKey(EntityBulletBaseNT.class, DataSerializers.VARINT);

	public int getRenderStyle() { return this.dataManager.get(RENDER_STYLE); }
	public int getRenderTrail() { return this.dataManager.get(RENDER_TRAIL); }

	@Override public double prevX() { return prevRenderX; }
	@Override public double prevY() { return prevRenderY; }
	@Override public double prevZ() { return prevRenderZ; }
	@Override public void prevX(double d) { prevRenderX = d; }
	@Override public void prevY(double d) { prevRenderY = d; }
	@Override public void prevZ(double d) { prevRenderZ = d; }
	@Override public List<Pair<Vec3d, Double>> nodes() { return this.trailNodes; }

	private BulletConfiguration config;
	public float overrideDamage;

	public double prevRenderX;
	public double prevRenderY;
	public double prevRenderZ;
	public final List<Pair<Vec3d, Double>> trailNodes = new ArrayList<Pair<Vec3d, Double>>();

	public BulletConfiguration getConfig() {
		return config;
	}

	public EntityBulletBaseNT(World world) {
		super(world);
		this.renderDistanceWeight = 10.0D;
		this.setSize(0.5F, 0.5F);
	}

	public EntityBulletBaseNT(World world, int config) {
		super(world);
		this.config = BulletConfigSyncingUtil.pullConfig(config);
		this.dataManager.set(BULLET_CONFIG, config);
		this.dataManager.set(RENDER_STYLE, (byte)this.config.style);
		this.dataManager.set(RENDER_TRAIL, (byte)this.config.trail);
		this.renderDistanceWeight = 10.0D;

		if(this.config == null) {
			this.setDead();
			return;
		}

		this.setSize(0.5F, 0.5F);
	}

	public EntityBulletBaseNT(World world, int config, EntityLivingBase entity) {
		super(world);
		this.config = BulletConfigSyncingUtil.pullConfig(config);
		this.dataManager.set(BULLET_CONFIG, config);
		this.dataManager.set(RENDER_STYLE, (byte)this.config.style);
		this.dataManager.set(RENDER_TRAIL, (byte)this.config.trail);
		thrower = entity;

		boolean offsetShot = true;
		boolean accuracyBoost = false;

		this.setLocationAndAngles(entity.posX, entity.posY + entity.getEyeHeight(), entity.posZ, entity.rotationYaw, entity.rotationPitch);

		if(offsetShot) {
			double sideOffset = 0.16D;

			this.posX -= MathHelper.cos(this.rotationYaw / 180.0F * (float) Math.PI) * sideOffset;
			this.posY -= 0.1D;
			this.posZ -= MathHelper.sin(this.rotationYaw / 180.0F * (float) Math.PI) * sideOffset;
		} else {
			this.posY -= 0.1D;
		}
		this.setPosition(this.posX, this.posY, this.posZ);

		this.motionX = -MathHelper.sin(this.rotationYaw / 180.0F * (float) Math.PI) * MathHelper.cos(this.rotationPitch / 180.0F * (float) Math.PI);
		this.motionZ = MathHelper.cos(this.rotationYaw / 180.0F * (float) Math.PI) * MathHelper.cos(this.rotationPitch / 180.0F * (float) Math.PI);
		this.motionY = (-MathHelper.sin(this.rotationPitch / 180.0F * (float) Math.PI));

		this.renderDistanceWeight = 10.0D;
		this.setSize(0.5F, 0.5F);

		this.setThrowableHeading(this.motionX, this.motionY, this.motionZ, 1.0F, this.config.spread * (accuracyBoost ? 0.25F : 1F));
	}

	public EntityBulletBaseNT(World world, int config, EntityLivingBase entity, EntityLivingBase target, float motion, float deviation) {
		super(world);

		this.config = BulletConfigSyncingUtil.pullConfig(config);
		this.dataManager.set(BULLET_CONFIG, config);
		this.dataManager.set(RENDER_STYLE, (byte)this.config.style);
		this.dataManager.set(RENDER_TRAIL, (byte)this.config.trail);
		this.thrower = entity;

		this.renderDistanceWeight = 10.0D;
		this.setSize(0.5F, 0.5F);

		this.posY = entity.posY + entity.getEyeHeight() - 0.10000000149011612D;
		double d0 = target.posX - entity.posX;
		double d1 = target.getEntityBoundingBox().minY + target.height / 3.0F - this.posY;
		double d2 = target.posZ - entity.posZ;
		double d3 = MathHelper.sqrt(d0 * d0 + d2 * d2);

		if (d3 >= 1.0E-7D) {
			float f2 = (float) (Math.atan2(d2, d0) * 180.0D / Math.PI) - 90.0F;
			float f3 = (float) (-(Math.atan2(d1, d3) * 180.0D / Math.PI));
			double d4 = d0 / d3;
			double d5 = d2 / d3;
			this.setLocationAndAngles(entity.posX + d4, this.posY, entity.posZ + d5, f2, f3);
			this.yOffset = 0.0F;
			this.setThrowableHeading(d0, d1, d2, motion, deviation);
		}
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		//style
		this.dataManager.register(RENDER_STYLE, Byte.valueOf((byte) 0));
		//trail
		this.dataManager.register(RENDER_TRAIL, Byte.valueOf((byte) 0));
		//bullet config sync
		this.dataManager.register(BULLET_CONFIG, Integer.valueOf((int) 0));
	}

	@Override
	public void onUpdate() {

		if(config == null) config = BulletConfigSyncingUtil.pullConfig(dataManager.get(BULLET_CONFIG));

		if(config == null){
			this.setDead();
			return;
		}

		if(this.world.isRemote && config.style == BulletConfiguration.STYLE_TAU) {
			if(trailNodes.isEmpty()) {
				this.ignoreFrustumCheck = true;
				trailNodes.add(new Pair<Vec3d, Double>(new Vec3d(-motionX * 2, -motionY * 2, -motionZ * 2), 0D));
			} else {
				trailNodes.add(new Pair<Vec3d, Double>(new Vec3d(0, 0, 0), 1D));
			}
		}

		if(this.world.isRemote && this.config.blackPowder && this.ticksExisted == 1) {

			for(int i = 0; i < 15; i++) {
				double mod = rand.nextDouble();
				this.world.spawnParticle("smoke", this.posX, this.posY, this.posZ,
						(this.motionX + rand.nextGaussian() * 0.05) * mod,
						(this.motionY + rand.nextGaussian() * 0.05) * mod,
						(this.motionZ + rand.nextGaussian() * 0.05) * mod);
			}

			double mod = 0.5;
			this.world.spawnParticle("flame", this.posX + this.motionX * mod, this.posY + this.motionY * mod, this.posZ + this.motionZ * mod, 0, 0, 0);
		}

		if(!this.world.isRemote) {

			if(config.maxAge == 0) {
				if(this.config.bntUpdate != null) this.config.bntUpdate.behaveUpdate(this);
				this.setDead();
				return;
			}

			if(this.ticksExisted > config.maxAge) this.setDead();
		}

		if(this.config.bntUpdate != null) this.config.bntUpdate.behaveUpdate(this);

		this.prevPosX = posX;
		this.prevPosY = posY;
		this.prevPosZ = posZ;

		super.onUpdate();

		if(this.world.isRemote && !config.vPFX.isEmpty()) {

			Vec3d vec = new Vec3d(posX - prevPosX, posY - prevPosY, posZ - prevPosZ);
			double motion = Math.max(vec.length(), 0.1);
			vec = vec.normalize();

			for(double d = 0; d < motion; d += 0.5) {

				NBTTagCompound nbt = new NBTTagCompound();
				nbt.setString("type", "vanillaExt");
				nbt.setString("mode", config.vPFX);
				nbt.setDouble("posX", this.posX - vec.x * d);
				nbt.setDouble("posY", this.posY - vec.y * d);
				nbt.setDouble("posZ", this.posZ - vec.z * d);
				MainRegistry.proxy.effectNT(nbt);
			}
		}
	}

	@Override
	protected void onImpact(RayTraceResult mop) {

		if(mop.typeOfHit == Type.BLOCK) {
			int x = mop.getBlockPos().getX();
			int y = mop.getBlockPos().getY();
			int z = mop.getBlockPos().getZ();

			boolean hRic = rand.nextInt(100) < config.HBRC;
			boolean doesRic = config.doesRicochet && hRic;

			if(!config.isSpectral && !doesRic) {
				this.setPosition(mop.hitVec.x, mop.hitVec.y, mop.hitVec.z);
				this.onBlockImpact(x, y, z, mop.sideHit.getIndex());
			}

			if(doesRic) {

				Vec3d face = null;

				switch(mop.sideHit.getIndex()) {
				case 0: face = new Vec3d(0, -1, 0); break;
				case 1: face = new Vec3d(0, 1, 0); break;
				case 2: face = new Vec3d(0, 0, 1); break;
				case 3: face = new Vec3d(0, 0, -1); break;
				case 4: face = new Vec3d(-1, 0, 0); break;
				case 5: face = new Vec3d(1, 0, 0); break;
				}

				if(face != null) {

					Vec3d vel = new Vec3d(motionX, motionY, motionZ);
					vel.normalize();

					boolean lRic = rand.nextInt(100) < config.LBRC;
					double angle = Math.abs(BobMathUtil.getCrossAngle(vel, face) - 90);

					if(hRic || (angle <= config.ricochetAngle && lRic)) {
						switch(mop.sideHit.getIndex()) {
						case 0:
						case 1: motionY *= -1; break;
						case 2:
						case 3: motionZ *= -1; break;
						case 4:
						case 5: motionX *= -1; break;
						}

						if(config.plink == 1)
							this.world.playSoundAtEntity(this, "hbm:weapon.ricochet", 0.25F, 1.0F);
						if(config.plink == 2)
							this.world.playSoundAtEntity(this, "hbm:weapon.gBounce", 1.0F, 1.0F);

						this.setPosition(mop.hitVec.x, mop.hitVec.y, mop.hitVec.z);
						onRicochet(x, y, z);

						//worldObj.setBlock((int) Math.floor(posX), (int) Math.floor(posY), (int) Math.floor(posZ), Blocks.dirt);

					} else {
						if(!this.world.isRemote) {
							this.setPosition(mop.hitVec.x, mop.hitVec.y, mop.hitVec.z);
							onBlockImpact(x, y, z, mop.sideHit.getIndex());
						}
					}

					/*this.posX += (mop.hitVec.x - this.posX) * 0.6;
					this.posY += (mop.hitVec.y - this.posY) * 0.6;
					this.posZ += (mop.hitVec.z - this.posZ) * 0.6;*/

					this.motionX *= config.bounceMod;
					this.motionY *= config.bounceMod;
					this.motionZ *= config.bounceMod;
				}
			}

		}

		if(mop.entityHit != null) {

			DamageSource damagesource = this.config.getDamage(this, this.thrower);
			Entity victim = mop.entityHit;

			if(!config.doesPenetrate) {
				this.setPosition(mop.hitVec.x, mop.hitVec.y, mop.hitVec.z);
				onEntityImpact(victim);
			} else {
				onEntityHurt(victim);
			}

			float damage = rand.nextFloat() * (config.dmgMax - config.dmgMin) + config.dmgMin;

			if(overrideDamage != 0)
				damage = overrideDamage;

			boolean headshot = false;

			if(victim instanceof EntityLivingBase && this.config.headshotMult > 1F) {
				EntityLivingBase living = (EntityLivingBase) victim;
				double head = living.height - living.getEyeHeight();

				if(!!living.isEntityAlive() && mop.hitVec != null && mop.hitVec.y > (living.posY + living.height - head * 2)) {
					damage *= this.config.headshotMult;
					headshot = true;
				}
			}

			if(victim != null && !victim.attackEntityFrom(damagesource, damage)) {

				try {
					Field lastDamage = ReflectionHelper.findField(EntityLivingBase.class, "lastDamage", "field_110153_bc");
					float dmg = (float) damage + lastDamage.getFloat(victim);
					if(!victim.attackEntityFrom(damagesource, dmg)) headshot = false;
				} catch (Exception x) { }

			}

			if(!this.world.isRemote && headshot) {
				if(victim instanceof EntityLivingBase) {
					EntityLivingBase living = (EntityLivingBase) victim;
					double head = living.height - living.getEyeHeight();
					NBTTagCompound data = new NBTTagCompound();
					data.setString("type", "vanillaburst");
					data.setInteger("count", 15);
					data.setDouble("motion", 0.1D);
					data.setString("mode", "blockdust");
					data.setInteger("block", Block.getIdFromBlock(Blocks.REDSTONE_BLOCK));
					PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, living.posX, living.posY + living.height - head, living.posZ), new TargetPoint(living.dimension, living.posX, living.posY, living.posZ, 50));
					com.hbm.sound.VanillaWorldSoundEffects.play(this.world, victim.posX, victim.posY, victim.posZ, "mob.zombie.woodbreak", 1.0F, 0.95F + rand.nextFloat() * 0.2F);
				}
			}
		}
	}

	//for when a bullet dies by hitting a block
	private void onBlockImpact(int bX, int bY, int bZ, int sideHit) {
		Block block = com.hbm.world.WorldBlockState.read(this.world, bX, bY, bZ).getBlock();

		if(config.bntImpact != null)
			config.bntImpact.behaveBlockHit(this, bX, bY, bZ, sideHit);

		if(!this.world.isRemote) {
			if(!config.liveAfterImpact && !config.isSpectral && bY > -1 && !this.inGround) this.setDead();
			if(!config.doesPenetrate && bY == -1) this.setDead();
		}

		if(config.incendiary > 0 && !this.world.isRemote) {
			if(this.world.rand.nextInt(3) == 0 && com.hbm.world.WorldBlockState.read(this.world, (int)posX, (int)posY, (int)posZ).getBlock() == Blocks.AIR) this.world.setBlock((int)posX, (int)posY, (int)posZ, Blocks.FIRE);
			if(this.world.rand.nextInt(3) == 0 && com.hbm.world.WorldBlockState.read(this.world, (int)posX + 1, (int)posY, (int)posZ).getBlock() == Blocks.AIR) this.world.setBlock((int)posX + 1, (int)posY, (int)posZ, Blocks.FIRE);
			if(this.world.rand.nextInt(3) == 0 && com.hbm.world.WorldBlockState.read(this.world, (int)posX - 1, (int)posY, (int)posZ).getBlock() == Blocks.AIR) this.world.setBlock((int)posX - 1, (int)posY, (int)posZ, Blocks.FIRE);
			if(this.world.rand.nextInt(3) == 0 && com.hbm.world.WorldBlockState.read(this.world, (int)posX, (int)posY + 1, (int)posZ).getBlock() == Blocks.AIR) this.world.setBlock((int)posX, (int)posY + 1, (int)posZ, Blocks.FIRE);
			if(this.world.rand.nextInt(3) == 0 && com.hbm.world.WorldBlockState.read(this.world, (int)posX, (int)posY - 1, (int)posZ).getBlock() == Blocks.AIR) this.world.setBlock((int)posX, (int)posY - 1, (int)posZ, Blocks.FIRE);
			if(this.world.rand.nextInt(3) == 0 && com.hbm.world.WorldBlockState.read(this.world, (int)posX, (int)posY, (int)posZ + 1).getBlock() == Blocks.AIR) this.world.setBlock((int)posX, (int)posY, (int)posZ + 1, Blocks.FIRE);
			if(this.world.rand.nextInt(3) == 0 && com.hbm.world.WorldBlockState.read(this.world, (int)posX, (int)posY, (int)posZ - 1).getBlock() == Blocks.AIR) this.world.setBlock((int)posX, (int)posY, (int)posZ - 1, Blocks.FIRE);
		}

		if(config.emp > 0)
			ExplosionNukeGeneric.empBlast(this.world, (int)(this.posX + 0.5D), (int)(this.posY + 0.5D), (int)(this.posZ + 0.5D), config.emp);

		if(config.emp > 3) {
			if (!this.world.isRemote) {

				EntityEMPBlast cloud = new EntityEMPBlast(this.world, config.emp);
				cloud.posX = this.posX;
				cloud.posY = this.posY + 0.5F;
				cloud.posZ = this.posZ;

				this.world.spawnEntityInWorld(cloud);
			}
		}

		if(config.jolt > 0 && !this.world.isRemote)
			ExplosionLarge.jolt(this.world, posX, posY, posZ, config.jolt, 150, 0.25);

		if(config.explosive > 0 && !this.world.isRemote) {
			//worldObj.newExplosion(this.thrower, posX, posY, posZ, config.explosive, config.incendiary > 0, config.blockDamage);
			ExplosionVNT vnt = new ExplosionVNT(this.world, posX, posY, posZ, config.explosive, this.thrower);
			vnt.setBlockAllocator(new BlockAllocatorStandard());
			if(config.blockDamage)	vnt.setBlockProcessor(new BlockProcessorStandard().withBlockEffect(config.incendiary > 0 ? new BlockMutatorFire() : null));
			else					vnt.setBlockProcessor(new BlockProcessorNoDamage().withBlockEffect(config.incendiary > 0 ? new BlockMutatorFire() : null));
			vnt.setEntityProcessor(new EntityProcessorStandard().allowSelfDamage());
			vnt.setPlayerProcessor(new PlayerProcessorStandard());
			vnt.setSFX(new ExplosionEffectStandard());
			vnt.explode();
		}

		if(config.shrapnel > 0 && !this.world.isRemote)
			ExplosionLarge.spawnShrapnels(this.world, posX, posY, posZ, config.shrapnel);

		if(config.rainbow > 0 && !this.world.isRemote) {
			EntityNukeExplosionMK3 ex = EntityNukeExplosionMK3.statFacFleija(this.world, posX, posY, posZ, config.rainbow);
			if(!ex.isDead) {
				com.hbm.sound.VanillaWorldSoundEffects.play(this.world, this.posX, this.posY, this.posZ, "random.explode", 100.0f, this.world.rand.nextFloat() * 0.1F + 0.9F);
				this.world.spawnEntityInWorld(ex);

				EntityCloudFleijaRainbow cloud = new EntityCloudFleijaRainbow(this.world, config.rainbow);
				cloud.posX = this.posX;
				cloud.posY = this.posY;
				cloud.posZ = this.posZ;
				this.world.spawnEntityInWorld(cloud);
			}
		}

		if(config.nuke > 0 && !this.world.isRemote) {
			this.world.spawnEntityInWorld(EntityNukeExplosionMK5.statFac(this.world, config.nuke, posX, posY, posZ));
			NBTTagCompound data = new NBTTagCompound();
			data.setString("type", "muke");
			if(MainRegistry.polaroidID == 11 || rand.nextInt(100) == 0) data.setBoolean("balefire", true);
			PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, posX, posY + 0.5, posZ), new TargetPoint(dimension, posX, posY, posZ, 250));
			com.hbm.sound.WorldSoundEffects.play(this.world, posX, posY, posZ, "hbm:weapon.mukeExplosion", 15.0F, 1.0F);
		}

		if(config.destroysBlocks && !this.world.isRemote) {
			if(block.getBlockHardness(this.world, bX, bY, bZ) <= 120)
				this.world.func_147480_a(bX, bY, bZ, false);
		} else if(config.doesBreakGlass && !this.world.isRemote) {
			if(block == Blocks.GLASS || block == Blocks.GLASS_PANE || block == Blocks.STAINED_GLASS || block == Blocks.STAINED_GLASS_PANE)
				this.world.func_147480_a(bX, bY, bZ, false);

			if(block instanceof BlockDetonatable) {
				((BlockDetonatable) block).onShot(this.world, bX, bY, bZ);
			}
		}
	}

	//for when a bullet dies by hitting a block
	private void onRicochet(int bX, int bY, int bZ) {

		if(config.bntRicochet != null)
			config.bntRicochet.behaveBlockRicochet(this, bX, bY, bZ);
	}

	//for when a bullet dies by hitting an entity
	private void onEntityImpact(Entity e) {
		onEntityHurt(e);
		onBlockImpact(-1, -1, -1, -1);

		if(config.bntHit != null)
			config.bntHit.behaveEntityHit(this, e);

		//this.setDead();
	}

	//for when a bullet hurts an entity, not necessarily dying
	private void onEntityHurt(Entity e) {

		if(config.bntHurt != null)
			config.bntHurt.behaveEntityHurt(this, e);

		if(config.incendiary > 0 && !this.world.isRemote) {
			e.setFire(config.incendiary);
		}

		if(config.leadChance > 0 && !this.world.isRemote && this.world.rand.nextInt(100) < config.leadChance && e instanceof EntityLivingBase) {
			((EntityLivingBase)e).addPotionEffect(new PotionEffect(HbmPotion.lead.id, 10 * 20, 0));
		}

		if(e instanceof EntityLivingBase && config.effects != null && !config.effects.isEmpty() && !this.world.isRemote) {

			for(PotionEffect effect : config.effects) {
				((EntityLivingBase)e).addPotionEffect(new PotionEffect(effect));
			}
		}

		if(config.instakill && e instanceof EntityLivingBase && !this.world.isRemote) {

			if(!(e instanceof EntityPlayer && ((EntityPlayer)e).capabilities.isCreativeMode))
				((EntityLivingBase)e).setHealth(0.0F);
		}

		if(config.caustic > 0 && e instanceof EntityPlayer){
			ArmorUtil.damageSuit((EntityPlayer)e, 0, config.caustic);
			ArmorUtil.damageSuit((EntityPlayer)e, 1, config.caustic);
			ArmorUtil.damageSuit((EntityPlayer)e, 2, config.caustic);
			ArmorUtil.damageSuit((EntityPlayer)e, 3, config.caustic);
		}
	}

	@Override
	public boolean doesPenetrate() {
		return this.config.doesPenetrate;
	}

	@Override
	public boolean isSpectral() {
		return this.config.isSpectral;
	}

	@Override
	public int selfDamageDelay() {
		return this.config.selfDamageDelay;
	}

	@Override
	protected double headingForceMult() {
		return 1D;
	}

	@Override
	public double getGravityVelocity() {
		return this.config.gravity;
	}

	@Override
	protected double motionMult() {
		return this.config.velocity;
	}

	@Override
	protected float getAirDrag() {
		return 1F;
	}

	@Override
	protected float getWaterDrag() {
		return 1F;
	}

	@Override
	public boolean writeToNBTOptional(NBTTagCompound nbt) {
		return false;
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbt) {
		super.readEntityFromNBT(nbt);
		this.setDead();
	}

	public static interface IBulletHurtBehaviorNT { public void behaveEntityHurt(EntityBulletBaseNT bullet, Entity hit); }
	public static interface IBulletHitBehaviorNT { public void behaveEntityHit(EntityBulletBaseNT bullet, Entity hit); }
	public static interface IBulletRicochetBehaviorNT { public void behaveBlockRicochet(EntityBulletBaseNT bullet, int x, int y, int z); }
	public static interface IBulletImpactBehaviorNT { public void behaveBlockHit(EntityBulletBaseNT bullet, int x, int y, int z, int sideHit); }
	public static interface IBulletUpdateBehaviorNT { public void behaveUpdate(EntityBulletBaseNT bullet); }
}
