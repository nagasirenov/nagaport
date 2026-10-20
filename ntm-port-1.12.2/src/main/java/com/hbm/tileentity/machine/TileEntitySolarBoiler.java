package com.hbm.tileentity.machine;

import java.util.HashSet;

import com.hbm.dim.CelestialBody;
import com.hbm.dim.orbit.WorldProviderOrbit;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.lib.Library;
import com.hbm.tileentity.IBufPacketReceiver;
import com.hbm.tileentity.IFluidCopiable;
import com.hbm.tileentity.TileEntityLoadedBase;

import api.hbm.fluidmk2.IFluidStandardTransceiverMK2;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;

public class TileEntitySolarBoiler extends TileEntityLoadedBase implements IFluidStandardTransceiverMK2, IBufPacketReceiver, IFluidCopiable, net.minecraft.util.ITickable {

	private FluidTank water;
	private FluidTank steam;
	public int display;
	public int heat;

	public HashSet<SolarMirrorPosition> primary = new HashSet<>();
	public HashSet<SolarMirrorPosition> secondary = new HashSet<>();

	public TileEntitySolarBoiler() {
		water = new FluidTank(Fluids.WATER, 100);
		steam = new FluidTank(Fluids.STEAM, 10_000);
	}

	@Override
	public void update() {

		if(!this.world.isRemote) {

			this.trySubscribe(water.getTankType(), this.world, this.pos.getX(), this.pos.getY() + 3, this.pos.getZ(), Library.POS_Y);
			this.trySubscribe(water.getTankType(), this.world, this.pos.getX(), this.pos.getY() - 1, this.pos.getZ(), Library.NEG_Y);

			float sunPower = this.world.provider instanceof WorldProviderOrbit
				? ((WorldProviderOrbit)this.world.provider).getSunPower()
				: CelestialBody.getBody(this.world).getSunPower();

			int process = (int)(heat * sunPower) / 50;
			this.display = process;
			process = Math.min(process, water.getFill());
			process = Math.min(process, (steam.getMaxFill() - steam.getFill()) / 100);

			if(process < 0) process = 0;

			water.setFill(water.getFill() - process);
			steam.setFill(steam.getFill() + process * 100);

			this.tryProvide(steam, this.world, this.pos.getX(), this.pos.getY() + 3, this.pos.getZ(), Library.POS_Y);
			this.tryProvide(steam, this.world, this.pos.getX(), this.pos.getY() - 1, this.pos.getZ(), Library.NEG_Y);

			heat = 0;

			networkPackNT(15);
		} else {

			//a delayed queue of mirror positions because we can't expect the boiler to always tick first
			secondary.clear();
			secondary.addAll(primary);
			primary.clear();
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		this.water.readFromNBT(nbt, "water");
		this.steam.readFromNBT(nbt, "steam");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		this.water.writeToNBT(nbt, "water");
		this.steam.writeToNBT(nbt, "steam");
		return nbt;
	}

	AxisAlignedBB bb = null;

	@Override
	public AxisAlignedBB getRenderBoundingBox() {

		if(bb == null) {
			bb = AxisAlignedBB.getBoundingBox(
					this.pos.getX() - 1,
					this.pos.getY(),
					this.pos.getZ() - 1,
					this.pos.getX() + 2,
					this.pos.getY() + 3,
					this.pos.getZ() + 2
					);
		}

		return bb;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}

	@Override
	public FluidTank[] getSendingTanks() {
		return new FluidTank[] { steam };
	}

	@Override
	public FluidTank[] getReceivingTanks() {
		return new FluidTank[] { water };
	}

	@Override
	public FluidTank[] getAllTanks() {
		return new FluidTank[] { water, steam };
	}

	@Override
	public void serialize(ByteBuf buf) {
		buf.writeInt(display);
		water.serialize(buf);
		steam.serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		this.display = buf.readInt();
		water.deserialize(buf);
		steam.deserialize(buf);
	}

	@Override public FluidTank getTankToPaste() { return null; }
}
