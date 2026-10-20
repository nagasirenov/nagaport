package com.hbm.tileentity.machine.fusion;

import java.io.IOException;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.handler.CompatHandler;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.main.MainRegistry;
import com.hbm.sound.AudioWrapper;
import com.hbm.tileentity.IConfigurableMachine;
import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.uninos.GenNode;
import com.hbm.uninos.UniNodespace;
import com.hbm.uninos.networkproviders.PlasmaNetworkProvider;
import com.hbm.util.fauxpointtwelve.BlockPos;
import com.hbm.util.fauxpointtwelve.DirPos;

import api.hbm.energymk2.IEnergyProviderMK2;
import api.hbm.fluidmk2.IFluidStandardTransceiverMK2;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SimpleComponent;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import com.hbm.lib.ForgeDirection;

@Optional.InterfaceList({@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "OpenComputers")})
public class TileEntityFusionMHDT extends TileEntityLoadedBase implements IEnergyProviderMK2, IFluidStandardTransceiverMK2, IFusionPowerReceiver, IConfigurableMachine, SimpleComponent, CompatHandler.OCComponent, net.minecraft.util.ITickable {

	protected GenNode plasmaNode;

	public long plasmaEnergy;
	public long plasmaEnergySync;
	public long power;

	public float rotor;
	public float prevRotor;
	public float rotorSpeed;
	public static final float ROTOR_ACCELERATION = 0.125F;

	public static final double PLASMA_EFFICIENCY = 1.35D;
	public static final int COOLANT_USE = 50;
	public static long MINIMUM_PLASMA = 5_000_000L;

	public FluidTank[] tanks;
	private AudioWrapper audio;

	@Override public String getConfigName() { return "mhd-turbine"; }
	@Override public void readIfPresent(JsonObject obj) { MINIMUM_PLASMA = IConfigurableMachine.grab(obj, "L:minimumPlasma", MINIMUM_PLASMA); }
	@Override public void writeConfig(JsonWriter writer) throws IOException { writer.name("L:minimumPlasma").value(MINIMUM_PLASMA); }

	public TileEntityFusionMHDT() {
		this.tanks = new FluidTank[2];
		this.tanks[0] = new FluidTank(Fluids.PERFLUOROMETHYL_COLD, 4_000);
		this.tanks[1] = new FluidTank(Fluids.PERFLUOROMETHYL, 4_000);
	}

	public boolean hasMinimumPlasma() {
		return this.plasmaEnergy >= MINIMUM_PLASMA;
	}

	@Override
	public void update() {

		if(!this.world.isRemote) {

			this.plasmaEnergySync = this.plasmaEnergy;

			if(isCool()) {
				this.power = (long) Math.floor(this.plasmaEnergy * PLASMA_EFFICIENCY);
				if(!this.hasMinimumPlasma()) this.power /= 2;
				tanks[0].setFill(tanks[0].getFill() - COOLANT_USE);
				tanks[1].setFill(tanks[1].getFill() + COOLANT_USE);
			}

			for(DirPos pos : getConPos()) {
				this.tryProvide(this.world, pos.getX(), pos.getY(), pos.getZ(), pos.getDir());
				if(tanks[0].getTankType() != Fluids.NONE) this.trySubscribe(tanks[0].getTankType(), this.world, pos);
				if(tanks[1].getFill() > 0) this.tryProvide(tanks[1], this.world, pos);
			}

			if(plasmaNode == null || plasmaNode.expired) {
				ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10).getOpposite();
				plasmaNode = UniNodespace.getNode(this.world, this.pos.getX() + dir.offsetX * 6, this.pos.getY() + 2, this.pos.getZ() + dir.offsetZ * 6, PlasmaNetworkProvider.THE_PROVIDER);

				if(plasmaNode == null) {
					plasmaNode = new GenNode(PlasmaNetworkProvider.THE_PROVIDER,
							new BlockPos(this.pos.getX() + dir.offsetX * 6, this.pos.getY() + 2, this.pos.getZ() + dir.offsetZ * 6))
							.setConnections(new DirPos(this.pos.getX() + dir.offsetX * 7, this.pos.getY() + 2, this.pos.getZ() + dir.offsetZ * 7, dir));

					UniNodespace.createNode(this.world, plasmaNode);
				}
			}

			if(plasmaNode != null && plasmaNode.hasValidNet()) plasmaNode.net.addReceiver(this);

			this.networkPackNT(150);
			this.plasmaEnergy = 0;

		} else {

			if(this.plasmaEnergy > 0 && isCool()) this.rotorSpeed += ROTOR_ACCELERATION;
			else this.rotorSpeed -= ROTOR_ACCELERATION;

			this.rotorSpeed = MathHelper.clamp(this.rotorSpeed, 0F, hasMinimumPlasma() ? 15F : 10F);

			this.prevRotor = this.rotor;
			this.rotor += this.rotorSpeed;

			if(this.rotor >= 360F) {
				this.rotor -= 360F;
				this.prevRotor -= 360F;
			}

			if(this.rotorSpeed > 0 && MainRegistry.proxy.me().getDistanceSq(this.pos.getX() + 0.5, this.pos.getY() + 2.5, this.pos.getZ() + 0.5) < 30 * 30) {

				float speed = this.rotorSpeed / 15F;

				if(audio == null) {
					audio = MainRegistry.proxy.getLoopedSound("hbm:block.largeTurbineRunning", this.pos.getX() + 0.5F, this.pos.getY() + 1.5F, this.pos.getZ() + 0.5F, getVolume(speed), 20F, speed, 20);
					audio.startSound();
				} else {
					audio.updateVolume(getVolume(speed));
					audio.updatePitch(speed);
					audio.keepAlive();
				}

			} else {

				if(audio != null) {
					if(audio.isPlaying()) audio.stopSound();
					audio = null;
				}
			}
		}
	}

	public boolean isCool() {
		return tanks[0].getFill() >= COOLANT_USE && tanks[1].getFill() + COOLANT_USE <= tanks[1].getMaxFill();
	}

	public DirPos[] getConPos() {
		ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10);
		ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

		return new DirPos[] {
				new DirPos(this.pos.getX() + dir.offsetX * 4 + rot.offsetX * 4, this.pos.getY(), this.pos.getZ() + dir.offsetZ * 4 + rot.offsetZ * 4, rot),
				new DirPos(this.pos.getX() + dir.offsetX * 4 - rot.offsetX * 4, this.pos.getY(), this.pos.getZ() + dir.offsetZ * 4 - rot.offsetZ * 4, rot.getOpposite()),
				new DirPos(this.pos.getX() + dir.offsetX * 8, this.pos.getY() + 1, this.pos.getZ() + dir.offsetZ * 8, dir)
		};
	}

	@Override public boolean receivesFusionPower() { return true; }
	@Override public void receiveFusionPower(long fusionPower, double neutronPower) { this.plasmaEnergy = fusionPower; }

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeLong(plasmaEnergySync);

		this.tanks[0].serialize(buf);
		this.tanks[1].serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		this.plasmaEnergy = buf.readLong();

		this.tanks[0].deserialize(buf);
		this.tanks[1].deserialize(buf);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		this.tanks[0].readFromNBT(nbt, "t0");
		this.tanks[1].readFromNBT(nbt, "t1");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		this.tanks[0].writeToNBT(nbt, "t0");
		this.tanks[1].writeToNBT(nbt, "t1");
		return nbt;
	}

	@Override
	public void onChunkUnload() {
		super.onChunkUnload();

		if(audio != null) {
			audio.stopSound();
			audio = null;
		}
	}

	@Override
	public void invalidate() {
		super.invalidate();

		if(audio != null) {
			audio.stopSound();
			audio = null;
		}

		if(!this.world.isRemote) {
			if(this.plasmaNode != null) UniNodespace.destroyNode(this.world, plasmaNode);
		}
	}

	@Override public long getPower() { return power; }
	@Override public void setPower(long power) { this.power = power; }
	@Override public long getMaxPower() { return power; }

	@Override public FluidTank[] getAllTanks() { return tanks; }
	@Override public FluidTank[] getReceivingTanks() { return new FluidTank[] {tanks[0]}; }
	@Override public FluidTank[] getSendingTanks() { return new FluidTank[] {tanks[1]}; }

	AxisAlignedBB bb = null;

	@Override
	public AxisAlignedBB getRenderBoundingBox() {

		if(bb == null) {
			bb = AxisAlignedBB.getBoundingBox(
					this.pos.getX() - 7,
					this.pos.getY(),
					this.pos.getZ() - 7,
					this.pos.getX() + 8,
					this.pos.getY() + 4,
					this.pos.getZ() + 8
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
	@Optional.Method(modid = "OpenComputers")
	public String getComponentName() {
		return "ntm_fusion_mhdt";
	}

	@Callback(direct = true)
	@Optional.Method(modid = "OpenComputers")
	public Object[] getEnergyInfo(Context context, Arguments args) {
		return new Object[] {power};
	}

	@Callback(direct = true)
	@Optional.Method(modid = "OpenComputers")
	public Object[] getPlasmaEnergy(Context context, Arguments args) {
		return new Object[] {plasmaEnergySync};
	}

	@Callback(direct = true)
	@Optional.Method(modid = "OpenComputers")
	public Object[] getCoolant(Context context, Arguments args) {
		return new Object[] {
			tanks[0].getFill(), tanks[0].getMaxFill(),
			tanks[1].getFill(), tanks[1].getMaxFill()
		};
	}

	@Callback(direct = true)
	@Optional.Method(modid = "OpenComputers")
	public Object[] getInfo(Context context, Arguments args) {
		return new Object[] {
			power, plasmaEnergySync,

			tanks[0].getFill(), tanks[0].getMaxFill(),
			tanks[1].getFill(), tanks[1].getMaxFill(),
		};
	}

	@Override
	@Optional.Method(modid = "OpenComputers")
	public String[] methods() {
		return new String[] {
			"getEnergyInfo",
			"getPlasmaEnergy",
			"getCoolant",
			"getInfo"
		};
	}

	@Override
	@Optional.Method(modid = "OpenComputers")
	public Object[] invoke(String method, Context context, Arguments args) throws Exception {
		switch (method) {
			case "getEnergyInfo": return getEnergyInfo(context, args);
			case "getPlasmaEnergy": return getPlasmaEnergy(context, args);
			case "getCoolant": return getCoolant(context, args);
			case "getInfo": return getInfo(context, args);
		}
		throw new NoSuchMethodException();
	}
}
