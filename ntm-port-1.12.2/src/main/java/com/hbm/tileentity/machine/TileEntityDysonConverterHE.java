package com.hbm.tileentity.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.tileentity.IDysonConverter;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.fauxpointtwelve.DirPos;

import api.hbm.energymk2.IEnergyProviderMK2;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import com.hbm.lib.ForgeDirection;

public class TileEntityDysonConverterHE extends TileEntityMachineBase implements IDysonConverter, IEnergyProviderMK2 {

	public long power;

	public boolean isConverting;
	private int cooldown;

	public TileEntityDysonConverterHE() {
		super(0);
	}

	@Override
	public String getDefaultName() {
		return "container.machineDysonConverterHE";
	}

	@Override
	public void update() {
		if(!this.world.isRemote) {
			ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset).getOpposite();
			ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

			DirPos output = new DirPos(this.pos.getX() + dir.offsetX * 5, this.pos.getY(), this.pos.getZ() + dir.offsetZ * 5, dir);
			tryProvide(this.world, output.getX(), output.getY(), output.getZ(), output.getDir());

			isConverting = power > 0;

			if(isConverting && this.world.getTotalWorldTime() % 2 == 0) {
				NBTTagCompound dPart = new NBTTagCompound();
				dPart.setString("type", this.world.getTotalWorldTime() % 10 == 0 ? "tau" : "hadron");
				dPart.setByte("count", (byte) 1);
				PacketDispatcher.wrapper.sendToAllAround(new AuxParticlePacketNT(dPart, this.pos.getX() + 0.5 + dir.offsetX * 4 + rot.offsetX, this.pos.getY() + 2.25, this.pos.getZ() + 0.5 + dir.offsetZ * 4 + rot.offsetZ), new TargetPoint(this.world.provider.getDimension(), this.pos.getX(), this.pos.getY(), this.pos.getZ(), 25));
				PacketDispatcher.wrapper.sendToAllAround(new AuxParticlePacketNT(dPart, this.pos.getX() + 0.5 + dir.offsetX * 4 - rot.offsetX, this.pos.getY() + 2.25, this.pos.getZ() + 0.5 + dir.offsetZ * 4 - rot.offsetZ), new TargetPoint(this.world.provider.getDimension(), this.pos.getX(), this.pos.getY(), this.pos.getZ(), 25));
				PacketDispatcher.wrapper.sendToAllAround(new AuxParticlePacketNT(dPart, this.pos.getX() + 0.5 + dir.offsetX * 3 + rot.offsetX, this.pos.getY() + 2.75, this.pos.getZ() + 0.5 + dir.offsetZ * 3 + rot.offsetZ), new TargetPoint(this.world.provider.getDimension(), this.pos.getX(), this.pos.getY(), this.pos.getZ(), 25));
				PacketDispatcher.wrapper.sendToAllAround(new AuxParticlePacketNT(dPart, this.pos.getX() + 0.5 + dir.offsetX * 3 - rot.offsetX, this.pos.getY() + 2.75, this.pos.getZ() + 0.5 + dir.offsetZ * 3 - rot.offsetZ), new TargetPoint(this.world.provider.getDimension(), this.pos.getX(), this.pos.getY(), this.pos.getZ(), 25));

				cooldown++;
				if(cooldown > 10) {
					// To prevent this machine acting like an endgame battery, but still be able to transmit every drop of power
					// this machine will clear its buffers (almost) immediately after transmitting power
					power = 0;
					cooldown = 0;
				}
			}

			networkPackNT(250);
		}
	}

	@Override
	public boolean provideEnergy(int x, int y, int z, long energy) {
		ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset);
		int rx = this.pos.getX() + dir.offsetX * 4;
		int ry = this.pos.getY() + 1;
		int rz = this.pos.getZ() + dir.offsetZ * 4;

		if(x != rx || y != ry || z != rz) return false;

		power = energy;
		cooldown = 0;

		return true;
	}

	@Override
	public long maximumEnergy() {
		return Long.MAX_VALUE;
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeBoolean(isConverting);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		isConverting = buf.readBoolean();
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		return nbt;
	}

	@Override
	public long getPower() {
		return power;
	}

	@Override
	public void setPower(long power) {
		this.power = power;
	}

	@Override
	public long getMaxPower() {
		return power;
	}

	AxisAlignedBB bb = null;

	@Override
	public AxisAlignedBB getRenderBoundingBox() {

		if(bb == null) {
			bb = AxisAlignedBB.getBoundingBox(
				this.pos.getX() - 6,
				this.pos.getY(),
				this.pos.getZ() - 6,
				this.pos.getX() + 7,
				this.pos.getY() + 6,
				this.pos.getZ() + 7
			);
		}

		return bb;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}

}
