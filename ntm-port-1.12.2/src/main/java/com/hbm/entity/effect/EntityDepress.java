package com.hbm.entity.effect;

import java.util.List;

import com.hbm.extprop.HbmLivingProps;
import com.hbm.main.MainRegistry;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

public class EntityDepress extends Entity {
	private static final DataParameter<Byte> DIRECTION = EntityDataManager.createKey(EntityDepress.class, DataSerializers.BYTE);


	public int timeToLive;

	public static double range = 8; // push/pull range

	public EntityDepress(World world) {
		this(world, ForgeDirection.DOWN, 100);
	}

	public EntityDepress(World world, ForgeDirection dir, int timeToLive) {
		super(world);
		this.ignoreFrustumCheck = true;
		this.isImmuneToFire = true;
		this.noClip = true;

		this.timeToLive = timeToLive;
		setDir(dir);
	}

	protected void setDir(ForgeDirection dir) {
		this.dataManager.set(DIRECTION, (byte)dir.ordinal());
	}

	protected ForgeDirection getDir() {
		return ForgeDirection.getOrientation(this.dataManager.get(DIRECTION));
	}

	@Override
	protected void entityInit() {
		this.dataManager.register(DIRECTION, new Byte((byte)0));
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onUpdate() {
		super.onUpdate();

		ForgeDirection dir = getDir();

		NBTTagCompound data = new NBTTagCompound();
		data.setDouble("posX", posX + this.world.rand.nextGaussian() * 0.25);
		data.setDouble("posY", posY + this.world.rand.nextGaussian() * 0.25);
		data.setDouble("posZ", posZ + this.world.rand.nextGaussian() * 0.25);
		data.setString("type", "depress");
		data.setFloat("scale", 0.5f);
		data.setDouble("moX", dir.offsetX * 0.5 + this.world.rand.nextGaussian() * 0.25);
		data.setDouble("moY", dir.offsetY * 0.5 + this.world.rand.nextGaussian() * 0.25);
		data.setDouble("moZ", dir.offsetZ * 0.5 + this.world.rand.nextGaussian() * 0.25);
		data.setInteger("maxAge", 100 + this.world.rand.nextInt(20));
		data.setInteger("color", 0xFFFFFF);
		MainRegistry.proxy.effectNT(data);

		List<Entity> entities = this.world.getEntitiesWithinAABBExcludingEntity(this, AxisAlignedBB.getBoundingBox(
			posX - range, posY - range, posZ - range, posX + range, posY + range, posZ + range));

		for(Entity entity : entities) {
			Vec3d vec = new Vec3d(posX - entity.posX, posY - entity.posY, posZ - entity.posZ);

			// check if the player is in a different pressurized room
			// other living entities update gravity too infrequently to use this check, so it is for players only
			if(entity instanceof EntityPlayer) {
				if(HbmLivingProps.hasGravity((EntityLivingBase) entity)) continue;
			}

			double dist = vec.length();

			if(dist > range)
				continue;

			boolean succ = true;

			switch(dir) {
			case UP: succ = entity.posY < posY; break;
			case DOWN: succ = entity.posY > posY; break;
			case EAST: succ = entity.posX < posX; break;
			case WEST: succ = entity.posX > posX; break;
			case SOUTH: succ = entity.posZ < posZ; break;
			case NORTH: succ = entity.posZ > posZ; break;
			default: break;
			}

			vec = vec.normalize();

			double speed = succ ? 0.1D : -0.1D;
			entity.motionX += vec.x * speed;
			entity.motionY += vec.y * speed;
			entity.motionZ += vec.z * speed;
		}

		timeToLive--;

		if(timeToLive <= 0) {
			setDead();
		}
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbt) {
		timeToLive = nbt.getInteger("ttl");
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbt) {
		nbt.setInteger("ttl", timeToLive);
	}

}
