package com.hbm.tileentity.machine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.inventory.OreDictManager;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.RecipesCommon.OreDictStack;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.tileentity.IConfigurableMachine;
import com.hbm.tileentity.IPersistentNBT;
import com.hbm.tileentity.IRepairable;
import com.hbm.util.fauxpointtwelve.DirPos;
import com.hbm.world.gen.nbt.INBTTileEntityTransformable;

import api.hbm.energymk2.IEnergyReceiverMK2;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

public class TileEntityCondenserPowered extends TileEntityCondenser implements IEnergyReceiverMK2, IRepairable, INBTTileEntityTransformable, IPersistentNBT {

	public long power;
	public float spin;
	public float lastSpin;

	//Configurable values
	public static long maxPower = 10_000_000;
	public static int inputTankSizeP = 1_000_000;
	public static int outputTankSizeP = 1_000_000;
	public static int powerConsumption = 10;

	public boolean damaged;
	public Explosion lastExplosion;

	public TileEntityCondenserPowered() {
		tanks = new FluidTank[2];
		tanks[0] = new FluidTank(Fluids.SPENTSTEAM, inputTankSizeP);
		tanks[1] = new FluidTank(Fluids.WATER, outputTankSizeP);
		vacuumOptimised = true;
	}

	@Override
	public String getConfigName() {
		return "condenserPowered";
	}
	@Override
	public void readIfPresent(JsonObject obj) {
		maxPower = IConfigurableMachine.grab(obj, "L:maxPower", maxPower);
		inputTankSizeP = IConfigurableMachine.grab(obj, "I:inputTankSize", inputTankSizeP);
		outputTankSizeP = IConfigurableMachine.grab(obj, "I:outputTankSize", outputTankSizeP);
		powerConsumption = IConfigurableMachine.grab(obj, "I:powerConsumption", powerConsumption);
	}

	@Override
	public void writeConfig(JsonWriter writer) throws IOException {
		writer.name("L:maxPower").value(maxPower);
		writer.name("I:inputTankSize").value(inputTankSizeP);
		writer.name("I:outputTankSize").value(outputTankSizeP);
		writer.name("I:powerConsumption").value(powerConsumption);
	}

	@Override
	public void update() {
		if(!this.world.isRemote && damaged) {
			networkPackNT(150);
			return;
		}

		super.update();

		if(this.world.isRemote) {

			this.lastSpin = this.spin;

			if(this.waterTimer > 0) {
				this.spin += 30F;

				if(this.spin >= 360F) {
					this.spin -= 360F;
					this.lastSpin -= 360F;
				}

				if(this.world.getTotalWorldTime() % 4 == 0) {
					ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10);
					this.world.spawnParticle(net.minecraft.util.EnumParticleTypes.CLOUD, this.pos.getX() + 0.5 + dir.offsetX * 1.5, this.pos.getY() + 1.5, this.pos.getZ() + 0.5 + dir.offsetZ * 1.5, dir.offsetX * 0.1, 0, dir.offsetZ * 0.1);
					this.world.spawnParticle(net.minecraft.util.EnumParticleTypes.CLOUD, this.pos.getX() + 0.5 - dir.offsetX * 1.5, this.pos.getY() + 1.5, this.pos.getZ() + 0.5 - dir.offsetZ * 1.5, dir.offsetX * -0.1, 0, dir.offsetZ * -0.1);
				}
			}
		}
	}

	@Override
	public void packExtra(NBTTagCompound data) {
		data.setLong("power", power);
	}

	@Override
	public boolean extraCondition(int convert) {
		return power >= convert * 10;
	}

	@Override
	public void postConvert(int convert) {
		this.power -= convert * powerConsumption;
		if(this.power < 0) this.power = 0;
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeLong(this.power);
		this.tanks[0].serialize(buf);
		this.tanks[1].serialize(buf);
		buf.writeByte(this.waterTimer);
		buf.writeBoolean(this.damaged);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		this.power = buf.readLong();
		this.tanks[0].deserialize(buf);
		this.tanks[1].deserialize(buf);
		this.waterTimer = buf.readByte();
		this.damaged = buf.readBoolean();
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.power = nbt.getLong("power");
		tanks[0].readFromNBT(nbt, "water");
		tanks[1].readFromNBT(nbt, "steam");
		this.damaged = nbt.getBoolean("damaged");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setLong("power", power);
		tanks[0].writeToNBT(nbt, "water");
		tanks[1].writeToNBT(nbt, "steam");
		nbt.setBoolean("damaged", damaged);
		return nbt;
	}

	@Override
	public void subscribeToAllAround(FluidType type, TileEntity te) {
		for(DirPos pos : getConPos()) {
			this.trySubscribe(this.tanks[0].getTankType(), this.world, pos.getX(), pos.getY(), pos.getZ(), pos.getDir());
			this.trySubscribe(this.world, pos.getX(), pos.getY(), pos.getZ(), pos.getDir());
		}
	}

	@Override
	public void sendFluidToAll(FluidTank tank, TileEntity te) {
		for(DirPos pos : getConPos()) {
			this.sendFluid(this.tanks[1], this.world, pos.getX(), pos.getY(), pos.getZ(), pos.getDir());
		}
	}

	public DirPos[] getConPos() {

		ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10);
		ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

		return new DirPos[] {
				new DirPos(this.pos.getX() + rot.offsetX * 4, this.pos.getY() + 1, this.pos.getZ() + rot.offsetZ * 4, rot),
				new DirPos(this.pos.getX() - rot.offsetX * 4, this.pos.getY() + 1, this.pos.getZ() - rot.offsetZ * 4, rot.getOpposite()),
				new DirPos(this.pos.getX() + dir.offsetX * 2 - rot.offsetX, this.pos.getY() + 1, this.pos.getZ() + dir.offsetZ * 2 - rot.offsetZ, dir),
				new DirPos(this.pos.getX() + dir.offsetX * 2 + rot.offsetX, this.pos.getY() + 1, this.pos.getZ() + dir.offsetZ * 2 + rot.offsetZ, dir),
				new DirPos(this.pos.getX() - dir.offsetX * 2 - rot.offsetX, this.pos.getY() + 1, this.pos.getZ() - dir.offsetZ * 2 - rot.offsetZ, dir.getOpposite()),
				new DirPos(this.pos.getX() - dir.offsetX * 2 + rot.offsetX, this.pos.getY() + 1, this.pos.getZ() - dir.offsetZ * 2 + rot.offsetZ, dir.getOpposite())
		};
	}

	AxisAlignedBB bb = null;

	@Override
	public AxisAlignedBB getRenderBoundingBox() {

		if(bb == null) {
			bb = AxisAlignedBB.getBoundingBox(
					this.pos.getX() - 3,
					this.pos.getY(),
					this.pos.getZ() - 3,
					this.pos.getX() + 4,
					this.pos.getY() + 3,
					this.pos.getZ() + 4
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
	public long getPower() {
		return this.power;
	}

	@Override
	public void setPower(long power) {
		this.power = power;
	}

	@Override
	public long getMaxPower() {
		return maxPower;
	}

	@Override
	public boolean isDamaged() { return damaged; }

	List<AStack> repair = new ArrayList<>();

	@Override
	public List<AStack> getRepairMaterials() {
		if(!repair.isEmpty()) return repair;

		repair.add(new OreDictStack(OreDictManager.STEEL.plateWelded(), 4));
		repair.add(new OreDictStack(OreDictManager.STEEL.pipe(), 12));
		repair.add(new OreDictStack(OreDictManager.ANY_RESISTANTALLOY.plateWelded(), 2));
		return repair;
	}

	@Override
	public void repair() {
		damaged = false;
		markDirty();
	}

	@Override
	public void tryExtinguish(World world, int x, int y, int z, EnumExtinguishType type) {}

	@Override
	public void transformTE(World world, int coordBaseMode) {
		damaged = true;
	}

	@Override
	public void writeNBT(NBTTagCompound nbt) {
		if(damaged) nbt.setBoolean("damaged", true);
	}

	@Override
	public void readNBT(NBTTagCompound nbt) {
		damaged = nbt.getBoolean("damaged");
	}

}
