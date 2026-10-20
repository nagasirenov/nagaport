package com.hbm.tileentity.machine.fusion;

import com.hbm.handler.CompatHandler;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.fluid.trait.FT_Heatable;
import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.uninos.GenNode;
import com.hbm.uninos.UniNodespace;
import com.hbm.uninos.networkproviders.PlasmaNetworkProvider;
import com.hbm.util.fauxpointtwelve.BlockPos;
import com.hbm.util.fauxpointtwelve.DirPos;

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
import com.hbm.lib.ForgeDirection;

@Optional.InterfaceList({@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "OpenComputers")})
public class TileEntityFusionBoiler extends TileEntityLoadedBase implements IFluidStandardTransceiverMK2, IFusionPowerReceiver, SimpleComponent, CompatHandler.OCComponent, net.minecraft.util.ITickable {

	protected GenNode plasmaNode;

	public long plasmaEnergy;
	public long plasmaEnergySync;
	public FluidTank[] tanks;

	public TileEntityFusionBoiler() {
		this.tanks = new FluidTank[2];
		this.tanks[0] = new FluidTank(Fluids.WATER, 32_000);
		this.tanks[1] = new FluidTank(Fluids.SUPERHOTSTEAM, 32_000);
	}

	@Override
	public void update() {

		if(!this.world.isRemote) {

			this.plasmaEnergySync = this.plasmaEnergy;
			this.plasmaEnergy = 0;

			for(DirPos pos : getConPos()) {
				if(tanks[0].getTankType() != Fluids.NONE) this.trySubscribe(tanks[0].getTankType(), this.world, pos);
				if(tanks[1].getFill() > 0) this.tryProvide(tanks[1], this.world, pos);
			}

			if(plasmaNode == null || plasmaNode.expired) {
				ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10).getOpposite();
				plasmaNode = UniNodespace.getNode(this.world, this.pos.getX() + dir.offsetX * 4, this.pos.getY() + 2, this.pos.getZ() + dir.offsetZ * 4, PlasmaNetworkProvider.THE_PROVIDER);

				if(plasmaNode == null) {
					plasmaNode = new GenNode(PlasmaNetworkProvider.THE_PROVIDER,
							new BlockPos(this.pos.getX() + dir.offsetX * 4, this.pos.getY() + 2, this.pos.getZ() + dir.offsetZ * 4))
							.setConnections(new DirPos(this.pos.getX() + dir.offsetX * 5, this.pos.getY() + 2, this.pos.getZ() + dir.offsetZ * 5, dir));

					UniNodespace.createNode(this.world, plasmaNode);
				}
			}

			if(plasmaNode != null && plasmaNode.hasValidNet()) plasmaNode.net.addReceiver(this);

			this.networkPackNT(50);
		}
	}

	public DirPos[] getConPos() {
		ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10);
		ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

		return new DirPos[] {
				//new DirPos(xCoord + dir.offsetX * 5, yCoord + 2, zCoord + dir.offsetZ * 5, dir),
				new DirPos(this.pos.getX() - dir.offsetX * 1 + rot.offsetX * 2, this.pos.getY(), this.pos.getZ() - dir.offsetZ * 1 + rot.offsetZ * 2, rot),
				new DirPos(this.pos.getX() - dir.offsetX * 1 - rot.offsetX * 2, this.pos.getY(), this.pos.getZ() - dir.offsetZ * 1 - rot.offsetZ * 2, rot.getOpposite()),
				new DirPos(this.pos.getX() + dir.offsetX * 2 + rot.offsetX * 2, this.pos.getY(), this.pos.getZ() + dir.offsetZ * 2 + rot.offsetZ * 2, rot),
				new DirPos(this.pos.getX() + dir.offsetX * 2 - rot.offsetX * 2, this.pos.getY(), this.pos.getZ() + dir.offsetZ * 2 - rot.offsetZ * 2, rot.getOpposite())
		};
	}

	@Override public boolean receivesFusionPower() { return true; }

	@Override
	public void receiveFusionPower(long fusionPower, double neutronPower) {
		this.plasmaEnergy = fusionPower;

		int waterCycles = Math.min(tanks[0].getFill(), tanks[1].getMaxFill() - tanks[1].getFill());
		int steamCycles = (int) (Math.min(fusionPower / tanks[0].getTankType().getTrait(FT_Heatable.class).getFirstStep().heatReq, waterCycles));
		// the Math.min call was mushed into the steam cycles call instead of doing it afterwards as usual
		// in order to prevent issues when casting, should the fusion reactor output truly absurd amounts of power
		// due to the water cycles being effectively capped via the buffer size

		if(steamCycles > 0) {
			tanks[0].setFill(tanks[0].getFill() - steamCycles);
			tanks[1].setFill(tanks[1].getFill() + steamCycles);

			if(this.world.rand.nextInt(200) == 0) {
				com.hbm.sound.WorldSoundEffects.play(this.world, this.pos.getX() + 0.5, this.pos.getY() + 2, this.pos.getZ() + 0.5, "hbm:block.boilerGroan", 2.5F, 1.0F);
			}
		}
	}

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
	public void invalidate() {
		super.invalidate();

		if(!this.world.isRemote) {
			if(this.plasmaNode != null) UniNodespace.destroyNode(this.world, plasmaNode);
		}
	}

	@Override public FluidTank[] getAllTanks() { return tanks; }
	@Override public FluidTank[] getReceivingTanks() { return new FluidTank[] {tanks[0]}; }
	@Override public FluidTank[] getSendingTanks() { return new FluidTank[] {tanks[1]}; }

	AxisAlignedBB bb = null;

	@Override
	public AxisAlignedBB getRenderBoundingBox() {

		if(bb == null) {
			bb = AxisAlignedBB.getBoundingBox(
					this.pos.getX() - 4,
					this.pos.getY(),
					this.pos.getZ() - 4,
					this.pos.getX() + 5,
					this.pos.getY() + 4,
					this.pos.getZ() + 5
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
		return "ntm_fusion_boiler";
	}

	@Callback(direct = true)
	@Optional.Method(modid = "OpenComputers")
	public Object[] getPlasmaEnergy(Context context, Arguments args) {
		return new Object[] {plasmaEnergySync};
	}

	@Callback(direct = true)
	@Optional.Method(modid = "OpenComputers")
	public Object[] getFluid(Context context, Arguments args) {
		return new Object[] {
			tanks[0].getFill(), tanks[0].getMaxFill(),
			tanks[1].getFill(), tanks[1].getMaxFill()
		};
	}

	@Callback(direct = true)
	@Optional.Method(modid = "OpenComputers")
	public Object[] getInfo(Context context, Arguments args) {
		return new Object[] {
			plasmaEnergySync,

			tanks[0].getFill(), tanks[0].getMaxFill(),
			tanks[1].getFill(), tanks[1].getMaxFill(),
		};
	}

	@Override
	@Optional.Method(modid = "OpenComputers")
	public String[] methods() {
		return new String[] {
			"getPlasmaEnergy",
			"getFluid",
			"getInfo"
		};
	}

	@Override
	@Optional.Method(modid = "OpenComputers")
	public Object[] invoke(String method, Context context, Arguments args) throws Exception {
		switch (method) {
			case "getPlasmaEnergy": return getPlasmaEnergy(context, args);
			case "getFluid": return getFluid(context, args);
			case "getInfo": return getInfo(context, args);
		}
		throw new NoSuchMethodException();
	}
}
