package com.hbm.tileentity.machine;

import com.hbm.handler.pollution.PollutionHandler;
import com.hbm.handler.pollution.PollutionHandler.PollutionType;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.lib.Library;
import com.hbm.tileentity.IBufPacketReceiver;
import com.hbm.tileentity.TileEntityLoadedBase;

import api.hbm.fluidmk2.IFluidReceiverMK2;
import io.netty.buffer.ByteBuf;
import net.minecraft.tileentity.TileEntity;
import com.hbm.lib.ForgeDirection;

public abstract class TileEntityChimneyBase extends TileEntityLoadedBase implements IFluidReceiverMK2, IBufPacketReceiver, net.minecraft.util.ITickable {

	public long ashTick = 0;
	public long sootTick = 0;
	public int onTicks;

	@Override
	public void update() {

		if(!this.world.isRemote) {

			if(this.world.getTotalWorldTime() % 20 == 0) {
				FluidType[] types = new FluidType[] {Fluids.SMOKE, Fluids.SMOKE_LEADED, Fluids.SMOKE_POISON};

				for(FluidType type : types) {
					this.trySubscribe(type, this.world, this.pos.getX() + 2, this.pos.getY(), this.pos.getZ(), Library.POS_X);
					this.trySubscribe(type, this.world, this.pos.getX() - 2, this.pos.getY(), this.pos.getZ(), Library.NEG_X);
					this.trySubscribe(type, this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ() + 2, Library.POS_Z);
					this.trySubscribe(type, this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ() - 2, Library.NEG_Z);
				}
			}

			if(ashTick > 0 || sootTick > 0) {

				TileEntity below = this.world.getTileEntity(new net.minecraft.util.math.BlockPos(this.pos.getX(), this.pos.getY() - 1, this.pos.getZ()));

				if(below instanceof TileEntityAshpit) {
					TileEntityAshpit ashpit = (TileEntityAshpit) below;
					ashpit.ashLevelFly += ashTick;
					ashpit.ashLevelSoot += sootTick;
				}
				this.ashTick = 0;
				this.sootTick = 0;
			}

			networkPackNT(150);

			if(onTicks > 0) onTicks--;

		} else {

			if(onTicks > 0) {
				this.spawnParticles();
			}
		}
	}

	public boolean cpaturesAsh() {
		return true;
	}

	public boolean cpaturesSoot() {
		return false;
	}

	public void spawnParticles() { }

	@Override
	public void serialize(ByteBuf buf) {
		buf.writeInt(this.onTicks);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		this.onTicks = buf.readInt();
	}

	@Override
	public boolean canConnect(FluidType type, ForgeDirection dir) {
		return (dir == ForgeDirection.NORTH || dir == ForgeDirection.SOUTH || dir == ForgeDirection.EAST || dir == ForgeDirection.WEST) &&
				(type == Fluids.SMOKE || type == Fluids.SMOKE_LEADED || type == Fluids.SMOKE_POISON);
	}

	@Override
	public long transferFluid(FluidType type, int pressure, long fluid) {

		if(type != Fluids.SMOKE && type != Fluids.SMOKE_LEADED && type != Fluids.SMOKE_POISON) return fluid;

		onTicks = 20;

		if(cpaturesAsh()) ashTick += fluid;
		if(cpaturesSoot()) sootTick += fluid;

		fluid *= getPollutionMod();

		if(type == Fluids.SMOKE) PollutionHandler.incrementPollution(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ(), PollutionType.SOOT, fluid / 100F);
		if(type == Fluids.SMOKE_LEADED) PollutionHandler.incrementPollution(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ(), PollutionType.HEAVYMETAL, fluid / 100F);
		if(type == Fluids.SMOKE_POISON) PollutionHandler.incrementPollution(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ(), PollutionType.POISON, fluid / 100F);

		return 0;
	}

	public abstract double getPollutionMod();

	@Override
	public long getDemand(FluidType type, int pressure) {
		return 1_000_000;
	}

	@Override
	public FluidTank[] getAllTanks() {
		return new FluidTank[] {};
	}
}
