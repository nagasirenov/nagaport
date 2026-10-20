package com.hbm.entity.projectile;

import java.util.List;

import com.hbm.handler.threading.PacketThreading;
import com.hbm.items.weapon.sedna.BulletConfig;
import com.hbm.packet.toclient.AuxParticlePacketNT;

import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntityBulletBeamBase extends Entity implements IEntityAdditionalSpawnData {
	
	public EntityLivingBase thrower;
	public BulletConfig config;
	public float damage;
	public double headingX;
	public double headingY;
	public double headingZ;
	public double beamLength;

	public EntityBulletBeamBase(World world) {
		super(world);
		this.ignoreFrustumCheck = true;
		this.renderDistanceWeight = 10.0D;
		this.setSize(0.5F, 0.5F);
		this.isImmuneToFire = true;
	}
	
	public EntityLivingBase getThrower() { return this.thrower; }
	
	public EntityBulletBeamBase(EntityLivingBase entity, BulletConfig config, float baseDamage) {
		this(entity.world);
		
		this.thrower = entity;
		this.setBulletConfig(config);
		
		this.damage = baseDamage * this.config.damageMult;
	}
	
	public EntityBulletBeamBase(EntityLivingBase entity, BulletConfig config, float baseDamage, float angularInaccuracy, double sideOffset, double heightOffset, double frontOffset) {
		this(entity.world);
		
		this.thrower = entity;
		this.setBulletConfig(config);
		
		this.damage = baseDamage * this.config.damageMult;
		
		this.setLocationAndAngles(thrower.posX, thrower.posY + thrower.getEyeHeight(), thrower.posZ, thrower.rotationYaw + (float) rand.nextGaussian() * angularInaccuracy, thrower.rotationPitch + (float) rand.nextGaussian() * angularInaccuracy);
		
		com.hbm.util.Vec3NT offset = new com.hbm.util.Vec3NT(sideOffset, heightOffset, frontOffset);
		offset.rotateAroundXRad(-this.rotationPitch / 180F * (float) Math.PI);
		offset.rotateAroundYRad(-this.rotationYaw / 180F * (float) Math.PI);

		this.posX += offset.x;
		this.posY += offset.y;
		this.posZ += offset.z;
		
		this.setPosition(this.posX, this.posY, this.posZ);
		
		this.headingX = (double) (-MathHelper.sin(this.rotationYaw / 180.0F * (float) Math.PI) * MathHelper.cos(this.rotationPitch / 180.0F * (float) Math.PI));
		this.headingZ = (double) (MathHelper.cos(this.rotationYaw / 180.0F * (float) Math.PI) * MathHelper.cos(this.rotationPitch / 180.0F * (float) Math.PI));
		this.headingY = (double) (-MathHelper.sin((this.rotationPitch) / 180.0F * (float) Math.PI));
		
		double range = 250D;
		this.headingX *= range;
		this.headingY *= range;
		this.headingZ *= range;
		
		performHitscan();
	}
	
	public void setRotationsFromVector(Vec3d delta) {
		this.rotationPitch = (float) (-Math.asin(delta.y / delta.length()) * 180D / Math.PI);
		this.rotationYaw = (float) (-Math.atan2(delta.x, delta.z) * 180D / Math.PI);
		
		this.headingX = (double) (-MathHelper.sin(this.rotationYaw / 180.0F * (float) Math.PI) * MathHelper.cos(this.rotationPitch / 180.0F * (float) Math.PI));
		this.headingZ = (double) (MathHelper.cos(this.rotationYaw / 180.0F * (float) Math.PI) * MathHelper.cos(this.rotationPitch / 180.0F * (float) Math.PI));
		this.headingY = (double) (-MathHelper.sin((this.rotationPitch) / 180.0F * (float) Math.PI));
	}
	
	public void performHitscanExternal(double range) {
		this.headingX *= range;
		this.headingY *= range;
		this.headingZ *= range;
		performHitscan();
	}

	@Override
	protected void entityInit() {
		this.dataWatcher.addObject(3, Integer.valueOf(0));
	}
	
	public void setBulletConfig(BulletConfig config) {
		this.config = config;
		this.dataWatcher.updateObject(3, config.id);
	}
	
	public BulletConfig getBulletConfig() {
		int id = this.dataWatcher.getWatchableObjectInt(3);
		if(id < 0 || id > BulletConfig.configs.size()) return null;
		return BulletConfig.configs.get(id);
	}
	
	@Override
	public void onUpdate() {
		
		if(config == null) config = this.getBulletConfig();

		if(config == null){
			this.setDead();
			return;
		}
		
		if(config.onUpdate != null) config.onUpdate.accept(this);
		
		super.onUpdate();
		
		if(!this.world.isRemote && this.ticksExisted > config.expires) this.setDead();
	}
	
	protected void performHitscan() {
		
		Vec3d pos = new Vec3d(this.posX, this.posY, this.posZ);
		Vec3d nextPos = new Vec3d(this.posX + this.headingX, this.posY + this.headingY, this.posZ + this.headingZ);
		RayTraceResult mop = null;
		if(!this.isSpectral()) mop = this.world.rayTraceBlocks(pos, nextPos, false, true, false);
		pos = new Vec3d(this.posX, this.posY, this.posZ);
		nextPos = new Vec3d(this.posX + this.headingX, this.posY + this.headingY, this.posZ + this.headingZ);

		if(mop != null) {
			nextPos = new Vec3d(mop.hitVec.x, mop.hitVec.y, mop.hitVec.z);
		}
		
		if(!this.world.isRemote && this.doesImpactEntities()) {
			
			Entity hitEntity = null;
			List<Entity> list = this.world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().addCoord(this.headingX, this.headingY, this.headingZ).expand(1.0D, 1.0D, 1.0D));
			double nearest = 0.0D;
			RayTraceResult nonPenImpact = null;
			RayTraceResult coinHit = null;
			
			double closestCoin = 0;
			EntityCoin hitCoin = null;
			
			for(Entity entity : list) {
				if(entity.isDead) continue;
				if(entity instanceof EntityCoin) {
					double hitbox = 0.3F;
					AxisAlignedBB aabb = entity.getEntityBoundingBox().expand(hitbox, hitbox, hitbox);
					RayTraceResult hitMop = aabb.calculateIntercept(pos, nextPos);
					if(hitMop != null) {
						double dist = pos.distanceTo(hitMop.hitVec);
						if(closestCoin == 0 || dist < closestCoin) {
							closestCoin = dist;
							hitCoin = (EntityCoin) entity;
							coinHit = hitMop;
						}
					}
				}
			}

			for(int j = 0; j < list.size(); ++j) {
				Entity entity = (Entity) list.get(j);
				
				if(entity.canBeCollidedWith() && entity != thrower && entity.isEntityAlive()) {
					double hitbox = 0.3F;
					AxisAlignedBB aabb = entity.getEntityBoundingBox().expand(hitbox, hitbox, hitbox);
					RayTraceResult hitMop = aabb.calculateIntercept(pos, nextPos);

					if(hitMop != null) {
						
						double dist = pos.distanceTo(hitMop.hitVec);
						
						// if penetration is enabled, run impact for all intersecting entities
						if(this.doesPenetrate()) {
							if(hitCoin == null || dist < closestCoin) {
								this.onImpact(new RayTraceResult(entity, hitMop.hitVec));
							}
						} else {
							if(dist < nearest || nearest == 0.0D) {
								hitEntity = entity;
								nearest = dist;
								nonPenImpact = hitMop;
							}
						}
					}
				}
			}

			// if not, only run it for the closest MOP
			if(!this.doesPenetrate() && hitEntity != null) {
				mop = new RayTraceResult(hitEntity, nonPenImpact.hitVec);
			}
			
			if(hitCoin != null) {
				Vec3d vec = new Vec3d(coinHit.hitVec.x - posX, coinHit.hitVec.y - posY, coinHit.hitVec.z - posZ);
				this.beamLength = vec.length();
				
				double range = 50;
				List<Entity> targets = this.world.getEntitiesWithinAABB(Entity.class, AxisAlignedBB.getBoundingBox(coinHit.hitVec.x, coinHit.hitVec.y, coinHit.hitVec.z, coinHit.hitVec.x, coinHit.hitVec.y, coinHit.hitVec.z).expand(range, range, range));
				Entity nearestCoin = null;
				Entity nearestPlayer = null;
				Entity nearestMob = null;
				Entity nearestOther = null;
				double coinDist = 0;
				double playerDist = 0;
				double mobDist = 0;
				double otherDist = 0;
				
				hitCoin.setDead();
				
				// well i mean we could just uuse a single var for all variants and then overwrite stuff
				// when we run into things with higher priority. however i can't be assed fuck off
				for(Entity entity : targets) {
					if(entity == this.getThrower()) continue;
					if(entity.isDead) continue;
					double dist = entity.getDistanceToEntity(hitCoin);
					if(dist > range) continue;
					
					if(entity instanceof EntityCoin) {
						if(coinDist == 0 || dist < coinDist) {
							coinDist = dist;
							nearestCoin = entity;
						}
					} else if(entity instanceof EntityPlayer) {
						if(playerDist == 0 || dist < playerDist) {
							playerDist = dist;
							nearestPlayer = entity;
						}
					} else if(entity instanceof EntityMob) {
						if(mobDist == 0 || dist < mobDist) {
							mobDist = dist;
							nearestMob = entity;
						}
					} else if(entity instanceof EntityLivingBase) {
						if(otherDist == 0 || dist < otherDist) {
							otherDist = dist;
							nearestOther = entity;
						}
					}
				}
				
				// ternary of shame
				Entity target = nearestCoin != null ? nearestCoin :
					nearestPlayer != null ? nearestPlayer :
					nearestMob != null ? nearestMob :
					nearestOther != null ? nearestOther : null;
				
				if(target != null) {
					EntityBulletBeamBase newBeam = new EntityBulletBeamBase(hitCoin.getThrower() != null ? hitCoin.getThrower() : this.thrower, this.config, this.damage * 1.25F);
					newBeam.setPosition(coinHit.hitVec.x, coinHit.hitVec.y, coinHit.hitVec.z);
					Vec3d delta = new Vec3d(target.posX - newBeam.posX, (target.posY + target.height / 2D) - newBeam.posY, target.posZ - newBeam.posZ);
					newBeam.setRotationsFromVector(delta);
					newBeam.performHitscanExternal(250D);
					this.world.spawnEntityInWorld(newBeam);
				} else {
					EntityBulletBeamBase newBeam = new EntityBulletBeamBase(hitCoin.getThrower() != null ? hitCoin.getThrower() : this.thrower, this.config, this.damage * 1.25F);
					newBeam.setPosition(coinHit.hitVec.x, coinHit.hitVec.y, coinHit.hitVec.z);
					newBeam.setRotationsFromVector(new Vec3d(rand.nextGaussian() * 0.5, -1, rand.nextGaussian() * 0.5));
					newBeam.performHitscanExternal(250D);
					this.world.spawnEntityInWorld(newBeam);
				}
				
				NBTTagCompound data = new NBTTagCompound();
				data.setString("type", "vanillaExt");
				data.setString("mode", "largeexplode");
				data.setFloat("size", 1.5F);
				data.setByte("count", (byte)1);
				PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, coinHit.hitVec.x, coinHit.hitVec.y, coinHit.hitVec.z), new TargetPoint(this.world.provider.getDimension(), coinHit.hitVec.x, coinHit.hitVec.y, coinHit.hitVec.z, 100));
			
				
				return;
			}
		}

		if(mop != null) {
			if(mop.typeOfHit == RayTraceResult.Type.BLOCK && this.world.getBlockState(mop.getBlockPos()).getBlock() == Blocks.PORTAL) {
				this.setInPortal();
			} else {
				this.onImpact(mop);
			}

			Vec3d vec = new Vec3d(mop.hitVec.x - posX, mop.hitVec.y - posY, mop.hitVec.z - posZ);
			this.beamLength = vec.length();
		} else {
			Vec3d vec = new Vec3d(nextPos.x - posX, nextPos.y - posY, nextPos.z - posZ);
			this.beamLength = vec.length();
		}
		
	}

	
	protected void onImpact(RayTraceResult mop) {
		if(!this.world.isRemote) {
			if(this.config.onImpactBeam != null) this.config.onImpactBeam.accept(this, mop);
		}
	}
	
	public boolean doesImpactEntities() { return this.config.impactsEntities; }
	public boolean doesPenetrate() { return this.config.doesPenetrate; }
	public boolean isSpectral() { return this.config.isSpectral; }

	@Override @SideOnly(Side.CLIENT) public float getShadowSize() { return 0.0F; }
	
	@Override protected void writeEntityToNBT(NBTTagCompound nbt) { }
	@Override public boolean writeToNBTOptional(NBTTagCompound nbt) { return false; }
	@Override public void readEntityFromNBT(NBTTagCompound nbt) { this.setDead(); }

	@Override public void writeSpawnData(ByteBuf buf) {
		buf.writeDouble(beamLength);
		buf.writeFloat(rotationYaw);
		buf.writeFloat(rotationPitch);
	}
	@Override public void readSpawnData(ByteBuf buf) {
		this.beamLength = buf.readDouble();
		this.rotationYaw = buf.readFloat();
		this.rotationPitch = buf.readFloat();
	}
	
	@Override @SideOnly(Side.CLIENT) public boolean canRenderOnFire() { return false; }
}
