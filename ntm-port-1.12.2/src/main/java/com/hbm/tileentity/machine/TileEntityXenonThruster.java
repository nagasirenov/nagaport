package com.hbm.tileentity.machine;

import java.util.List;

import com.hbm.blocks.BlockDummyable;
import com.hbm.dim.CelestialBody;
import com.hbm.dim.SolarSystem;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.fluid.trait.FT_Rocket;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.BobMathUtil;
import com.hbm.util.i18n.I18nUtil;
import com.hbm.util.fauxpointtwelve.DirPos;

import api.hbm.energymk2.IEnergyReceiverMK2;
import api.hbm.fluid.IFluidStandardReceiver;
import api.hbm.tile.IPropulsion;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.TextFormatting;
import com.hbm.lib.ForgeDirection;

public class TileEntityXenonThruster extends TileEntityMachineBase implements IPropulsion, IFluidStandardReceiver, IEnergyReceiverMK2 {

	public FluidTank[] tanks;

	public long power;
	public static long maxPower = 20_000_000;

	private static final int POWER_COST_MULTIPLIER = 5_000;

	private boolean isOn;
	public float thrustAmount;

	private boolean hasRegistered;

	private int fuelCost;

	public TileEntityXenonThruster() {
		super(0);
		tanks = new FluidTank[1];
		tanks[0] = new FluidTank(Fluids.XENON, 4_000);
	}

	@Override
	public String getDefaultName() {
		return "container.xenonThruster";
	}

	@Override
	public void update() {
		if(!CelestialBody.inOrbit(this.world)) return;

		if(!this.world.isRemote) {
			if(!hasRegistered) {
				if(isFacingPrograde()) registerPropulsion();
				hasRegistered = true;
				isOn = false;
			}

			for(DirPos pos : getConPos()) {
				for(FluidTank tank : tanks) {
					trySubscribe(this.world, pos.getX(), pos.getY(), pos.getZ(), pos.getDir());
					trySubscribe(tank.getTankType(), this.world, pos.getX(), pos.getY(), pos.getZ(), pos.getDir());
				}
			}

			networkPackNT(250);
		} else {
			if(isOn) {
				thrustAmount += 0.01D;
				if(thrustAmount > 1) thrustAmount = 1;
			} else {
				thrustAmount -= 0.01D;
				if(thrustAmount < 0) thrustAmount = 0;
			}
		}
	}

	private DirPos[] getConPos() {
		ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset);
		ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

		return new DirPos[] {
			new DirPos(this.pos.getX() - dir.offsetX - rot.offsetX, this.pos.getY(), this.pos.getZ() - dir.offsetZ - rot.offsetZ, dir),
			new DirPos(this.pos.getX() - dir.offsetX, this.pos.getY(), this.pos.getZ() - dir.offsetZ, dir),
			new DirPos(this.pos.getX() - dir.offsetX + rot.offsetX, this.pos.getY(), this.pos.getZ() - dir.offsetZ + rot.offsetZ, dir),
		};
	}

	@Override
	public void invalidate() {
		super.invalidate();

		if(hasRegistered) {
			unregisterPropulsion();
			hasRegistered = false;
		}
	}

	@Override
	public void onChunkUnload() {
		super.onChunkUnload();

		if(hasRegistered) {
			unregisterPropulsion();
			hasRegistered = false;
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeBoolean(isOn);
		buf.writeLong(power);
		buf.writeInt(fuelCost);
		for(int i = 0; i < tanks.length; i++) tanks[i].serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		isOn = buf.readBoolean();
		power = buf.readLong();
		fuelCost = buf.readInt();
		for(int i = 0; i < tanks.length; i++) tanks[i].deserialize(buf);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setBoolean("on", isOn);
		nbt.setLong("power", power);
		for(int i = 0; i < tanks.length; i++) tanks[i].writeToNBT(nbt, "t" + i);
		return nbt;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		isOn = nbt.getBoolean("on");
		power = nbt.getLong("power");
		for(int i = 0; i < tanks.length; i++) tanks[i].readFromNBT(nbt, "t" + i);
	}

	public boolean isFacingPrograde() {
		return ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset) == ForgeDirection.WEST;
	}

	AxisAlignedBB bb = null;

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		if(bb == null) bb = AxisAlignedBB.getBoundingBox(this.pos.getX() - 2, this.pos.getY() - 1, this.pos.getZ() - 2, this.pos.getX() + 3, this.pos.getY() + 2, this.pos.getZ() + 3);
		return bb;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}

	@Override
	public TileEntity getTileEntity() {
		return this;
	}

	@Override
	public boolean canPerformBurn(int shipMass, double deltaV) {
		FT_Rocket trait = tanks[0].getTankType().getTrait(FT_Rocket.class);
		int isp = trait != null ? trait.getISP() : 300;

		fuelCost = SolarSystem.getFuelCost(deltaV, shipMass, isp);

		if(power < fuelCost * POWER_COST_MULTIPLIER) return false;

		for(FluidTank tank : tanks) {
			if(tank.getFill() < fuelCost) return false;
		}

		return true;
	}

	@Override
	public void addErrors(List<String> errors) {
		if(power < fuelCost * POWER_COST_MULTIPLIER) {
			errors.add(TextFormatting.RED + I18nUtil.resolveKey(getBlockType().getTranslationKey() + ".name") + " - Insufficient power: needs " + BobMathUtil.getShortNumber(fuelCost * POWER_COST_MULTIPLIER) + "HE");
		}

		for(FluidTank tank : tanks) {
			if(tank.getFill() < fuelCost) {
				errors.add(TextFormatting.RED + I18nUtil.resolveKey(getBlockType().getTranslationKey() + ".name") + " - Insufficient fuel: needs " + fuelCost + "mB");
			}
		}
	}

	@Override
	public float getThrust() {
		// but le realisme :((((
		// do not speak to me of realisme, miss I can carry 2,880,000kg of dirt in my fucking pocket
		return 1_400_000;
	}

	@Override
	public int startBurn() {
		isOn = true;
		power -= fuelCost * POWER_COST_MULTIPLIER;
		for(FluidTank tank : tanks) {
			tank.setFill(tank.getFill() - fuelCost);
		}
		return 200; // ten second prewarm
	}

	@Override
	public int endBurn() {
		isOn = false;
		return 200; // ten second recharge time
	}

	@Override
	public FluidTank[] getAllTanks() {
		return tanks;
	}

	@Override
	public FluidTank[] getReceivingTanks() {
		return tanks;
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
		return maxPower;
	}

}
