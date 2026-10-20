package com.hbm.tileentity.machine.oil;

import com.hbm.handler.pollution.PollutionHandler;
import com.hbm.handler.pollution.PollutionHandler.PollutionType;
import com.hbm.handler.CompatHandler.OCComponent;
import com.hbm.inventory.FluidStack;
import com.hbm.inventory.container.ContainerMachineCoker;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.gui.GUIMachineCoker;
import com.hbm.inventory.recipes.CokerRecipes;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.IFluidCopiable;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.Tuple.Triplet;
import com.hbm.util.fauxpointtwelve.DirPos;

import api.hbm.fluid.IFluidStandardTransceiver;
import api.hbm.tile.IHeatSource;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SimpleComponent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

@Optional.InterfaceList({@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "opencomputers")})
public class TileEntityMachineCoker extends TileEntityMachineBase implements SimpleComponent, OCComponent, IFluidStandardTransceiver, IGUIProvider, IFluidCopiable {

	public boolean wasOn;
	public int progress;
	public static int processTime = 20_000;

	public int heat;
	public static int maxHeat = 100_000;
	public static double diffusion = 0.25D;

	public FluidTank[] tanks;

	public TileEntityMachineCoker() {
		super(2);
		tanks = new FluidTank[2];
		tanks[0] = new FluidTank(Fluids.HEAVYOIL, 16_000);
		tanks[1] = new FluidTank(Fluids.OIL_COKER, 8_000);
	}

	@Override
	public String getDefaultName() {
		return "container.machineCoker";
	}

	@Override
	public void update() {

		if(!this.world.isRemote) {

			this.tryPullHeat();
			this.tanks[0].setType(0, slots);

			if(this.world.getTotalWorldTime() % 20 == 0) {
				for(DirPos pos : getConPos()) {
					this.trySubscribe(tanks[0].getTankType(), this.world, pos.getX(), pos.getY(), pos.getZ(), pos.getDir());
				}
			}

			this.wasOn = false;

			if(canProcess()) {
				int burn = heat / 100;

				if(burn > 0) {
					this.wasOn = true;
					this.progress += burn;
					this.heat -= burn;

					if(progress >= processTime) {
						this.markChanged();
						progress -= this.processTime;

						Triplet<Integer, ItemStack, FluidStack> recipe = CokerRecipes.getOutput(tanks[0].getTankType());
						int fillReq = recipe.getX();
						ItemStack output = recipe.getY();
						FluidStack byproduct = recipe.getZ();

						if(output != null) {
							if(slots[1] == null) {
								slots[1] = output.copy();
							} else {
								slots[1].stackSize += output.stackSize;
							}
						}

						if(byproduct != null) {
							tanks[1].setFill(tanks[1].getFill() + byproduct.fill);
						}

						tanks[0].setFill(tanks[0].getFill() - fillReq);
					}
				}

				if(wasOn && this.world.getTotalWorldTime() % 5 == 0) PollutionHandler.incrementPollution(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ(), PollutionType.SOOT, PollutionHandler.SOOT_PER_SECOND * 5);
			}

			for(DirPos pos : getConPos()) {
				if(this.tanks[1].getFill() > 0) this.sendFluid(tanks[1], this.world, pos.getX(), pos.getY(), pos.getZ(), pos.getDir());
			}

			this.networkPackNT(25);
		} else {

			if(this.wasOn) {

				if(this.world.getTotalWorldTime() % 2 == 0) {
					NBTTagCompound fx = new NBTTagCompound();
					fx.setString("type", "tower");
					fx.setFloat("lift", 10F);
					fx.setFloat("base", 0.75F);
					fx.setFloat("max", 3F);
					fx.setInteger("life", 200 + this.world.rand.nextInt(50));
					fx.setInteger("color",0x404040);
					fx.setDouble("posX", this.pos.getX() + 0.5);
					fx.setDouble("posY", this.pos.getY() + 22);
					fx.setDouble("posZ", this.pos.getZ() + 0.5);
					MainRegistry.proxy.effectNT(fx);
				}
			}
		}
	}



	public DirPos[] getConPos() {

		return new DirPos[] {
				new DirPos(this.pos.getX() + 2, this.pos.getY(), this.pos.getZ() + 1, Library.POS_X),
				new DirPos(this.pos.getX() + 2, this.pos.getY(), this.pos.getZ() - 1, Library.POS_X),
				new DirPos(this.pos.getX() - 2, this.pos.getY(), this.pos.getZ() + 1, Library.NEG_X),
				new DirPos(this.pos.getX() - 2, this.pos.getY(), this.pos.getZ() - 1, Library.NEG_X),
				new DirPos(this.pos.getX() + 1, this.pos.getY(), this.pos.getZ() + 2, Library.POS_Z),
				new DirPos(this.pos.getX() - 1, this.pos.getY(), this.pos.getZ() + 2, Library.POS_Z),
				new DirPos(this.pos.getX() + 1, this.pos.getY(), this.pos.getZ() - 2, Library.NEG_Z),
				new DirPos(this.pos.getX() - 1, this.pos.getY(), this.pos.getZ() - 2, Library.NEG_Z)
		};
	}

	public boolean canProcess() {
		Triplet<Integer, ItemStack, FluidStack> recipe = CokerRecipes.getOutput(tanks[0].getTankType());

		if(recipe == null) return false;

		int fillReq = recipe.getX();
		ItemStack output = recipe.getY();
		FluidStack byproduct = recipe.getZ();

		if(byproduct != null) tanks[1].setTankType(byproduct.type);

		if(tanks[0].getFill() < fillReq) return false;
		if(byproduct != null && byproduct.fill + tanks[1].getFill() > tanks[1].getMaxFill()) return false;

		if(output != null && slots[1] != null) {
			if(output.getItem() != slots[1].getItem()) return false;
			if(output.getItemDamage() != slots[1].getItemDamage()) return false;
			if(output.stackSize + slots[1].stackSize > output.getMaxStackSize()) return false;
		}

		return true;
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeBoolean(this.wasOn);
		buf.writeInt(this.heat);
		buf.writeInt(this.progress);
		tanks[0].serialize(buf);
		tanks[1].serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		this.wasOn = buf.readBoolean();
		this.heat = buf.readInt();
		this.progress = buf.readInt();
		tanks[0].deserialize(buf);
		tanks[1].deserialize(buf);
	}

	protected void tryPullHeat() {

		if(this.heat >= this.maxHeat) return;

		TileEntity con = this.world.getTileEntity(new net.minecraft.util.math.BlockPos(this.pos.getX(), this.pos.getY() - 1, this.pos.getZ()));

		if(con instanceof IHeatSource) {
			IHeatSource source = (IHeatSource) con;
			int diff = source.getHeatStored() - this.heat;

			if(diff == 0) {
				return;
			}

			if(diff > 0) {
				diff = (int) Math.ceil(diff * diffusion);
				source.useUpHeat(diff);
				this.heat += diff;
				if(this.heat > this.maxHeat)
					this.heat = this.maxHeat;
				return;
			}
		}

		this.heat = Math.max(this.heat - Math.max(this.heat / 1000, 1), 0);
	}

	@Override
	public boolean canExtractItem(int slot, ItemStack stack, net.minecraft.util.EnumFacing nativeFace) {
		int side = com.hbm.lib.ForgeDirection.fromEnumFacing(nativeFace).ordinal();
		return true;
	}

	@Override
	public int[] getSlotsForFace(net.minecraft.util.EnumFacing nativeFace) {
		int side = com.hbm.lib.ForgeDirection.fromEnumFacing(nativeFace).ordinal();
		return new int[] { 1 };
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.tanks[0].readFromNBT(nbt, "t0");
		this.tanks[1].readFromNBT(nbt, "t1");
		this.progress = nbt.getInteger("prog");
		this.heat = nbt.getInteger("heat");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		this.tanks[0].writeToNBT(nbt, "t0");
		this.tanks[1].writeToNBT(nbt, "t1");
		nbt.setInteger("prog", progress);
		nbt.setInteger("heat", heat);
		return nbt;
	}

	@Override
	public FluidTank[] getAllTanks() {
		return tanks;
	}

	@Override
	public FluidTank[] getSendingTanks() {
		return new FluidTank[] { tanks[1] };
	}

	@Override
	public FluidTank[] getReceivingTanks() {
		return new FluidTank[] { tanks[0] };
	}

	AxisAlignedBB bb = null;

	@Override
	public AxisAlignedBB getRenderBoundingBox() {

		if(bb == null) {
			bb = AxisAlignedBB.getBoundingBox(
					this.pos.getX() - 2,
					this.pos.getY(),
					this.pos.getZ() - 2,
					this.pos.getX() + 3,
					this.pos.getY() + 23,
					this.pos.getZ() + 3
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
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerMachineCoker(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIMachineCoker(player.inventory, this);
	}

	@Override
	@Optional.Method(modid = "OpenComputers")
	public String getComponentName() {
		return "ntm_coker";
	}

	@Callback(direct = true)
	@Optional.Method(modid = "OpenComputers")
	public Object[] getTypeStored(Context context, Arguments args) {
		return new Object[] {tanks[0].getTankType().getName(), tanks[1].getTankType().getName()};
	}

	@Callback(direct = true)
	@Optional.Method(modid = "OpenComputers")
	public Object[] getFluidStored(Context context, Arguments args) {
		return new Object[] {tanks[0].getFill(), tanks[1].getFill()};
	}

	@Callback(direct = true)
	@Optional.Method(modid = "OpenComputers")
	public Object[] getHeat(Context context, Arguments args) {
		return new Object[] {this.heat};
	}

	@Callback(direct = true)
	@Optional.Method(modid = "OpenComputers")
	public Object[] getInfo(Context context, Arguments args) {
		return new Object[] {tanks[0].getTankType().getName(), tanks[1].getTankType().getName(), tanks[0].getFill(), tanks[1].getFill(), this.heat};
	}

	@Override
	@Optional.Method(modid = "OpenComputers")
	public String[] methods() {
		return new String[] {
			"getFluidStored",
			"getTypeStored",
			"getHeat",
			"getInfo"};
	}

	@Override
	@Optional.Method(modid = "OpenComputers")
	public Object[] invoke(String method, Context context, Arguments args) throws Exception {
		switch (method) {
			case "getFluidStored":
				return getFluidStored(context, args);
			case "getTypeStored":
				return getTypeStored(context, args);
			case "getHeat":
				return getHeat(context, args);
			case "getInfo":
				return getInfo(context, args);
		}
		throw new NoSuchMethodException();
	}
}
