package com.hbm.entity.projectile;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableSet;
import com.hbm.entity.logic.IChunkLoader;
import com.hbm.entity.projectile.rocketbehavior.IRocketSteeringBehavior;
import com.hbm.entity.projectile.rocketbehavior.IRocketTargetingBehavior;
import com.hbm.entity.projectile.rocketbehavior.RocketSteeringBallisticArc;
import com.hbm.entity.projectile.rocketbehavior.RocketTargetingPredictive;
import com.hbm.items.weapon.ItemAmmoHIMARS;
import com.hbm.items.weapon.ItemAmmoHIMARS.HIMARSRocket;
import com.hbm.main.MainRegistry;
import com.hbm.util.Vec3NT;

import api.hbm.entity.IRadarDetectable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.ForgeChunkManager.Type;

public class EntityArtilleryRocket extends EntityThrowableInterp implements IChunkLoader, IRadarDetectable {

	private Ticket loaderTicket;

	//TODO: find satisfying solution for when an entity is unloaded and reloaded, possibly a custom entity lookup using persistent UUIDs
	public Entity targetEntity = null;
	public Vec3d lastTargetPos;

	public IRocketTargetingBehavior targeting;
	public IRocketSteeringBehavior steering;

	public EntityArtilleryRocket(World world) {
		super(world);
		this.ignoreFrustumCheck = true;

		this.targeting = new RocketTargetingPredictive();
		this.steering = new RocketSteeringBallisticArc();
	}

	@Override
	protected void entityInit() {
		init(ForgeChunkManager.requestTicket(MainRegistry.instance, worldObj, Type.ENTITY));
		this.dataWatcher.addObject(10, new Integer(0));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean isInRangeToRenderDist(double distance) {
		return true;
	}

	public EntityArtilleryRocket setType(int type) {
		this.dataWatcher.updateObject(10, type);
		return this;
	}

	public HIMARSRocket getType() {
		try {
			return ItemAmmoHIMARS.itemTypes[this.dataWatcher.getWatchableObjectInt(10)];
		} catch(Exception ex) {
			return ItemAmmoHIMARS.itemTypes[0];
		}
	}

	public EntityArtilleryRocket setTarget(Entity target) {
		this.targetEntity = target;
		setTarget(target.posX, target.posY - target.yOffset + target.height / 2D, target.posZ);
		return this;
	}

	public EntityArtilleryRocket setTarget(double x, double y, double z) {
		this.lastTargetPos = new Vec3d(x, y, z);
		return this;
	}

	public Vec3d getLastTarget() {
		return this.lastTargetPos;
	}

	@Override
	public void onUpdate() {

		if(worldObj.isRemote) {
			this.lastTickPosX = this.posX;
			this.lastTickPosY = this.posY;
			this.lastTickPosZ = this.posZ;
		}

		super.onUpdate();

		if(!worldObj.isRemote) {
			
			Vec3NT delta = new Vec3NT(this.lastTargetPos.x - this.posX, this.lastTargetPos.y - this.posY, this.lastTargetPos.z - this.posZ);
			double momentum = Math.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ) * motionMult();
			if(delta.length() <= momentum * 1.5) {
				if(this.targetEntity == null || !this.targetEntity.isEntityAlive()) {
					this.targeting = null;
					this.steering = null;
				}
				delta.normalizeSelf();
				motionX = delta.x * momentum / motionMult();
				motionY = delta.y * momentum / motionMult();
				motionZ = delta.z * momentum / motionMult();
			} else {
				if(this.targeting != null && this.targetEntity != null) this.targeting.recalculateTargetPosition(this, this.targetEntity);
				if(this.steering != null) this.steering.adjustCourse(this, 25D, 15D);
			}

			loadNeighboringChunks((int)Math.floor(posX / 16D), (int)Math.floor(posZ / 16D));
			this.getType().onUpdate(this);
		} else {

			Vec3d v = new Vec3d(lastTickPosX - posX, lastTickPosY - posY, lastTickPosZ - posZ);
			double velocity = v.length();
			v = v.normalize();

			int offset = 6;
			if(velocity > 1) {
				for (int i = offset; i < velocity + offset; i++) {
					NBTTagCompound data = new NBTTagCompound();
					data.setDouble("posX", posX + v.x * i);
					data.setDouble("posY", posY + v.y * i);
					data.setDouble("posZ", posZ + v.z * i);
					data.setString("type", "exKerosene");
					MainRegistry.proxy.effectNT(data);
				}
			}
		}
	}

	@Override
	protected void onImpact(RayTraceResult mop) {

		if(!worldObj.isRemote) {
			this.getType().onImpact(this, mop);
		}
	}

	@Override
	public void init(Ticket ticket) {
		if(!worldObj.isRemote && ticket != null) {
			if(loaderTicket == null) {
				loaderTicket = ticket;
				loaderTicket.bindEntity(this);
				loaderTicket.getModData();
			}
			ForgeChunkManager.forceChunk(loaderTicket, new ChunkCoordIntPair(chunkCoordX, chunkCoordZ));
		}
	}

	List<ChunkCoordIntPair> loadedChunks = new ArrayList<ChunkCoordIntPair>();

	public void loadNeighboringChunks(int newChunkX, int newChunkZ) {
		if(!worldObj.isRemote && loaderTicket != null) {

			for(ChunkCoordIntPair chunk : ImmutableSet.copyOf(loaderTicket.getChunkList())) {
				ForgeChunkManager.unforceChunk(loaderTicket, chunk);
			}

			loadedChunks.clear();
			loadedChunks.add(new ChunkCoordIntPair(newChunkX, newChunkZ));
			//loadedChunks.add(new ChunkCoordIntPair(newChunkX + (int) Math.floor((this.posX + this.motionX) / 16D), newChunkZ + (int) Math.floor((this.posZ + this.motionZ) / 16D)));

			for(ChunkCoordIntPair chunk : loadedChunks) {
				ForgeChunkManager.forceChunk(loaderTicket, chunk);
			}
		}
	}

	public void killAndClear() {
		this.setDead();
		this.clearChunkLoader();
	}

	public void clearChunkLoader() {
		if(!worldObj.isRemote && loaderTicket != null) {
			ForgeChunkManager.releaseTicket(loaderTicket);
			this.loaderTicket = null;
		}
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbt) {
		super.writeEntityToNBT(nbt);

		if(this.lastTargetPos == null) {
			this.lastTargetPos = new Vec3d(posX, posY, posZ);
		}

		nbt.setDouble("targetX", this.lastTargetPos.x);
		nbt.setDouble("targetY", this.lastTargetPos.y);
		nbt.setDouble("targetZ", this.lastTargetPos.z);

		nbt.setInteger("type", this.dataWatcher.getWatchableObjectInt(10));
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbt) {
		super.readEntityFromNBT(nbt);

		this.lastTargetPos = new Vec3d(nbt.getDouble("targetX"), nbt.getDouble("targetY"), nbt.getDouble("targetZ"));

		this.dataWatcher.updateObject(10, nbt.getInteger("type"));
	}

	@Override
	protected float getAirDrag() {
		return 1.0F;
	}

	@Override
	public double getGravityVelocity() {
		return this.steering != null ? 0D : 0.01D;
	}

	@Override
	public RadarTargetType getTargetType() {
		return RadarTargetType.ARTILLERY;
	}

	@Override
	public int approachNum() {
		return 0; //
	}
}
