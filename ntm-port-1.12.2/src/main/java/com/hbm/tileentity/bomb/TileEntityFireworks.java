package com.hbm.tileentity.bomb;

import com.hbm.entity.item.EntityFireworks;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.packet.toclient.AuxParticlePacketNT;

import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class TileEntityFireworks extends TileEntity implements net.minecraft.util.ITickable {

	public int color = 0xff0000;
	public String message = "NUCLEAR TECH";
	public int charges;

	int index;
	int delay;

	@Override
	public void update() {

		if(!this.world.isRemote) {

			if(this.world.isBlockIndirectlyGettingPowered(this.pos.getX(), this.pos.getY(), this.pos.getZ()) && !message.isEmpty() && charges > 0) {

				delay--;

				if(delay <= 0) {
					delay = 30;

					int c = (int)(message.charAt(index));

					int mod = index % 9;

					double offX = (mod / 3 - 1) * 0.3125;
					double offZ = (mod % 3 - 1) * 0.3125;

					EntityFireworks fireworks = new EntityFireworks(this.world, this.pos.getX() + 0.5 + offX, this.pos.getY() + 1.5, this.pos.getZ() + 0.5 + offZ, color, c);
					this.world.spawnEntityInWorld(fireworks);

					this.world.playSoundAtEntity(fireworks, "hbm:weapon.rocketFlame", 3.0F, 1.0F);

					charges--;
					this.markDirty();

					NBTTagCompound data = new NBTTagCompound();
					data.setString("type", "vanillaExt");
					data.setString("mode", "flame");
					PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, this.pos.getX() + 0.5 + offX, this.pos.getY() + 1.125, this.pos.getZ() + 0.5 + offZ), new TargetPoint(this.world.provider.getDimension(), this.pos.getX() + 0.5 + offX, this.pos.getY() + 1.125, this.pos.getZ() + 0.5 + offZ, 100));

					index++;

					if(index >= message.length()) {
						index = 0;
						delay = 100;
					}
				}

			} else {
				delay = 0;
				index = 0;
			}
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		this.charges = nbt.getInteger("charges");
		this.color = nbt.getInteger("color");
		this.message = nbt.getString("message");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		nbt.setInteger("charges", charges);
		nbt.setInteger("color", color);
		nbt.setString("message", message);
		return nbt;
	}
}
