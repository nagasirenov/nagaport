package com.hbm.entity.item;

import com.hbm.handler.threading.PacketThreading;
import com.hbm.packet.toclient.AuxParticlePacketNT;

import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class EntityFireworks extends Entity {

	int color;
	int character;

	public EntityFireworks(World world) {
		super(world);
	}

	public EntityFireworks(World world, double x, double y, double z, int color, int character) {
		super(world);
		this.setPositionAndRotation(x, y, z, 0.0F, 0.0F);
		this.color = color;
		this.character = character;
	}

	@Override
	protected void entityInit() { }

	@Override
	public void onUpdate() {

		this.moveEntity(0.0, 3.0D, 0.0);
		this.world.spawnParticle(net.minecraft.util.EnumParticleTypes.FLAME, posX, posY, posZ, 0.0, -0.3, 0.0);
		this.world.spawnParticle(net.minecraft.util.EnumParticleTypes.SMOKE_NORMAL, posX, posY, posZ, 0.0, -0.2, 0.0);

		if(!this.world.isRemote) {

			ticksExisted++;

			if(this.ticksExisted > 30) {

				com.hbm.sound.VanillaWorldSoundEffects.play(this.world, posX, posY, posZ, "fireworks.blast", 20, 1F + this.rand.nextFloat() * 0.2F);

				this.setDead();
				NBTTagCompound data = new NBTTagCompound();
				data.setString("type", "fireworks");
				data.setInteger("color", color);
				data.setInteger("char", character);
				PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, posX, posY, posZ), new TargetPoint(this.world.provider.getDimension(), posX, posY, posZ, 300));
			}
		}
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbt) {
		this.character = nbt.getInteger("char");
		this.color = nbt.getInteger("color");
		this.ticksExisted = nbt.getInteger("ticksExisted");
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbt) {
		nbt.setInteger("char", character);
		nbt.setInteger("color", color);
		nbt.setInteger("ticksExisted", ticksExisted);
	}

}
