package com.hbm.entity.item;

import com.hbm.entity.cart.EntityMinecartBogie;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.packet.toclient.AuxParticlePacketNT;

import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntityMagnusCartus extends EntityMinecart {

	public EntityMinecartBogie bogie;

	public EntityMagnusCartus(World world) {
		super(world);
	}

	public EntityMagnusCartus(World p_i1713_1_, double p_i1713_2_, double p_i1713_4_, double p_i1713_6_) {
		super(p_i1713_1_, p_i1713_2_, p_i1713_4_, p_i1713_6_);
	}

	@Override
	public int getMinecartType() {
		return -1;
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.dataWatcher.addObject(25, new Integer(0));
	}

	public void setBogie(EntityMinecartBogie bogie) {
		this.bogie = bogie;
		this.dataWatcher.updateObject(25, bogie.getEntityId());
	}

	public int getBogieID() {
		return this.dataWatcher.getWatchableObjectInt(25);
	}

	@Override
	public void onUpdate() {
		super.onUpdate();

		if(!worldObj.isRemote) {

			double dist = 3.0D;
			double force = 0.3D;

			if(bogie == null) {
				Vec3d vec = new Vec3d(dist, 0, 0);
				vec = vec.rotateYaw(rand.nextFloat() * 6.28F);
				EntityMinecartBogie bog = new EntityMinecartBogie(worldObj, posX + vec.x, posY + vec.y, posZ + vec.z);
				this.setBogie(bog);
				worldObj.spawnEntityInWorld(bog);
			}

			Vec3d delta = new Vec3d(posX - bogie.posX, posY - bogie.posY, posZ - bogie.posZ);
			delta = delta.normalize().scale(dist);

			double x = posX - delta.x;
			double y = posY - delta.y;
			double z = posZ - delta.z;

			NBTTagCompound data = new NBTTagCompound();
			data.setString("type", "vanillaExt");
			data.setString("mode", "reddust");
			PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, x, y, z), new TargetPoint(this.dimension, x, y, z, 25));

			Vec3d pull = new Vec3d(x - bogie.posX, y - bogie.posY, z - bogie.posZ);
			bogie.motionX += pull.x * force;
			bogie.motionY += pull.y * force;
			bogie.motionZ += pull.z * force;

			if(pull.length() > 1) {
				this.motionX -= pull.x * force;
				this.motionY -= pull.y * force;
				this.motionZ -= pull.z * force;
			}
		}
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbt) {
		super.readEntityFromNBT(nbt);
		int bogieID = nbt.getInteger("bogie");
		Entity e = worldObj.getEntityByID(bogieID);

		if(e instanceof EntityMinecartBogie) {
			this.setBogie((EntityMinecartBogie) e);
		}
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbt) {
		super.writeEntityToNBT(nbt);
		nbt.setInteger("bogie", this.getBogieID());
	}
}
