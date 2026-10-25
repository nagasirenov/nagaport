package com.hbm.tileentity.machine.rbmk;

import api.hbm.fluidmk2.IFluidStandardSenderMK2;

import com.hbm.blocks.ModBlocks;
import com.hbm.entity.projectile.EntityRBMKDebris.DebrisType;
import com.hbm.handler.CompatHandler;
import com.hbm.handler.neutron.NeutronStream;
import com.hbm.handler.neutron.RBMKNeutronHandler;
import com.hbm.inventory.FluidStack;
import com.hbm.inventory.container.ContainerRBMKOutgasser;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.gui.GUIRBMKOutgasser;
import com.hbm.inventory.recipes.OutgasserRecipes;
import com.hbm.inventory.recipes.OutgasserRecipes.OutgasserRecipe;
import com.hbm.lib.Library;
import com.hbm.tileentity.machine.rbmk.TileEntityRBMKConsole.ColumnType;
import com.hbm.util.fauxpointtwelve.DirPos;
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
import net.minecraft.world.World;

@Optional.InterfaceList({@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "OpenComputers")})
public class TileEntityRBMKOutgasser extends TileEntityRBMKSlottedBase implements IRBMKFluxReceiver, IFluidStandardSenderMK2, SimpleComponent, CompatHandler.OCComponent {

	@Override
	public boolean isEmpty() {
		for(int i = 0; i < this.getSizeInventory(); i++) {
			if(this.getStackInSlot(i) != null && !this.getStackInSlot(i).isEmpty()) return false;
		}
		return true;
	}

	@Override public int getField(int id) { return 0; }
	@Override public void setField(int id, int value) {}
	@Override public int getFieldCount() { return 0; }

	@Override
	public void clear() {
		for(int i = 0; i < this.getSizeInventory(); i++) {
		this.setInventorySlotContents(i, null);
		}
	}

	public FluidTank gas;
	public double progress;
	public static final int duration = 10000;

	public TileEntityRBMKOutgasser() {
		super(2);
		gas = new FluidTank(Fluids.TRITIUM, 64000);
	}

	@Override
	public String getName() {
		return "container.rbmkOutgasser";
	}

	@Override
	public void update() {

		if(!this.world.isRemote) {

			if(!canProcess()) this.progress = 0;
			for(DirPos pos : getOutputPos()) if(this.gas.getFill() > 0) this.tryProvide(gas, this.world, pos);
		}

		super.update();
	}

	protected DirPos[] getOutputPos() {

		if(com.hbm.world.WorldBlockState.read(this.world, this.pos.getX(), this.pos.getY() - 1, this.pos.getZ()).getBlock() == ModBlocks.rbmk_loader) {
			return new DirPos[] {
					new DirPos(this.pos.getX(), this.pos.getY() + RBMKDials.getColumnHeight(this.world) + 1, this.pos.getZ(), Library.POS_Y),
					new DirPos(this.pos.getX() + 1, this.pos.getY() - 1, this.pos.getZ(), Library.POS_X),
					new DirPos(this.pos.getX() - 1, this.pos.getY() - 1, this.pos.getZ(), Library.NEG_X),
					new DirPos(this.pos.getX(), this.pos.getY() - 1, this.pos.getZ() + 1, Library.POS_Z),
					new DirPos(this.pos.getX(), this.pos.getY() - 1, this.pos.getZ() - 1, Library.NEG_Z),
					new DirPos(this.pos.getX(), this.pos.getY() - 2, this.pos.getZ(), Library.NEG_Y)
			};
		} else if(com.hbm.world.WorldBlockState.read(this.world, this.pos.getX(), this.pos.getY() - 2, this.pos.getZ()).getBlock() == ModBlocks.rbmk_loader) {
			return new DirPos[] {
					new DirPos(this.pos.getX(), this.pos.getY() + RBMKDials.getColumnHeight(this.world) + 1, this.pos.getZ(), Library.POS_Y),
					new DirPos(this.pos.getX() + 1, this.pos.getY() - 2, this.pos.getZ(), Library.POS_X),
					new DirPos(this.pos.getX() - 1, this.pos.getY() - 2, this.pos.getZ(), Library.NEG_X),
					new DirPos(this.pos.getX(), this.pos.getY() - 2, this.pos.getZ() + 1, Library.POS_Z),
					new DirPos(this.pos.getX(), this.pos.getY() - 2, this.pos.getZ() - 1, Library.NEG_Z),
					new DirPos(this.pos.getX(), this.pos.getY() - 3, this.pos.getZ(), Library.NEG_Y)
			};
		} else {
			return new DirPos[] {
					new DirPos(this.pos.getX(), this.pos.getY() + RBMKDials.getColumnHeight(this.world) + 1, this.pos.getZ(), Library.POS_Y),
					new DirPos(this.pos.getX(), this.pos.getY() - 1, this.pos.getZ(), Library.NEG_Y)
			};
		}
	}

	@Override
	public void receiveFlux(NeutronStream stream) {

		if(canProcess()) {

			double efficiency = Math.min(1 - stream.fluxRatio * 0.8, 1);

			progress += stream.fluxQuantity * efficiency * RBMKDials.getOutgasserMod(this.world);

			if(progress > duration) {
				process();
				this.markDirty();
			}
		}
	}

	public boolean canProcess() {

		if(slots[0] == null)
			return false;

		OutgasserRecipe output = OutgasserRecipes.getOutput(slots[0]);

		if(output == null) return false;
		if(output.fusionOnly) return false;

		FluidStack fluid = output.liquidOutput;

		if(fluid != null) {
			if(gas.getTankType() != fluid.type && gas.getFill() > 0) return false;
			gas.setTankType(fluid.type);
			if(gas.getFill() + fluid.fill > gas.getMaxFill()) return false;
		}

		ItemStack out = output.solidOutput;

		if(slots[1] == null || out == null)
			return true;

		return slots[1].getItem() == out.getItem() && slots[1].getItemDamage() == out.getItemDamage() && slots[1].getCount() + out.getCount() <= slots[1].getMaxStackSize();
	}

	private void process() {

		OutgasserRecipe output = OutgasserRecipes.getOutput(slots[0]);
		this.decrStackSize(0, 1);
		this.progress = 0;

		if(output.liquidOutput != null) {
			gas.setFill(gas.getFill() + output.liquidOutput.fill);
		}

		ItemStack out = output.solidOutput;

		if(out != null) {
			if(slots[1] == null) {
				slots[1] = out.copy();
			} else {
				slots[1].grow(out.getCount());
			}
		}
	}

	@Override
	public void onMelt(int reduce) {

		int count = 4 + this.world.rand.nextInt(2);

		for(int i = 0; i < count; i++) {
			spawnDebris(DebrisType.BLANK);
		}

		super.onMelt(reduce);
	}

	@Override
	public RBMKNeutronHandler.RBMKType getRBMKType() {
		return RBMKNeutronHandler.RBMKType.OUTGASSER;
	}

	@Override
	public ColumnType getConsoleType() {
		return ColumnType.OUTGASSER;
	}

	@Override
	public NBTTagCompound getNBTForConsole() {
		NBTTagCompound data = new NBTTagCompound();
		data.setInteger("gas", this.gas.getFill());
		data.setInteger("maxGas", this.gas.getMaxFill());
		data.setShort("type", (short)this.gas.getTankType().getID());
		data.setDouble("progress", this.progress);
		return data;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		this.progress = nbt.getDouble("progress");
		this.gas.readFromNBT(nbt, "gas");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		nbt.setDouble("progress", this.progress);
		this.gas.writeToNBT(nbt, "gas");
		return nbt;
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		this.gas.serialize(buf);
		buf.writeDouble(this.progress);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		this.gas.deserialize(buf);
		this.progress = buf.readDouble();
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemStack) {
		return OutgasserRecipes.getOutput(itemStack) != null && i == 0;
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemStack, net.minecraft.util.EnumFacing nativeFace) {
		int j = com.hbm.lib.ForgeDirection.fromEnumFacing(nativeFace).ordinal();
		return i == 1;
	}

	@Override
	public int[] getSlotsForFace(net.minecraft.util.EnumFacing nativeFace) {
		int p_94128_1_ = com.hbm.lib.ForgeDirection.fromEnumFacing(nativeFace).ordinal();
		return new int[] {0, 1};
	}

	@Override
	public FluidTank[] getAllTanks() {
		return new FluidTank[] {gas};
	}

	@Override
	public FluidTank[] getSendingTanks() {
		return new FluidTank[] {gas};
	}

	//do some opencomputers stuff
	@Override
	@Optional.Method(modid = "OpenComputers")
	public String getComponentName() {
		return "rbmk_outgasser";
	}

	@Callback(direct = true)
	@Optional.Method(modid = "OpenComputers")
	public Object[] getGas(Context context, Arguments args) {
		return new Object[] {gas.getFill()};
	}

	@Callback(direct = true)
	@Optional.Method(modid = "OpenComputers")
	public Object[] getGasMax(Context context, Arguments args) {
		return new Object[] {gas.getMaxFill()};
	}

		@Callback(direct = true)
	@Optional.Method(modid = "OpenComputers")
	public Object[] getGasType(Context context, Arguments args) {
		return new Object[] {gas.getTankType().getName()};
	}

	@Callback(direct = true)
	@Optional.Method(modid = "OpenComputers")
	public Object[] getProgress(Context context, Arguments args) {
		return new Object[] {progress};
	}

	@Callback(direct = true, doc = "Returns the unlocalized name and size of the stack that the outgasser is crafting (the input), or nil, nil if there is no stack")
	@Optional.Method(modid = "OpenComputers")
	public Object[] getCrafting(Context context, Arguments args) {
		if (slots[0] == null)
			return new Object[] { "", 0 };
		else
			return new Object[]{slots[0].getTranslationKey(), slots[0].getCount() };
	}

	@Callback(direct = true)
	@Optional.Method(modid = "OpenComputers")
	public Object[] getCoordinates(Context context, Arguments args) {
		return new Object[] {this.pos.getX(), this.pos.getY(), this.pos.getZ()};
	}

	@Callback(direct = true)
	@Optional.Method(modid = "OpenComputers")
	public Object[] getInfo(Context context, Arguments args) {
		ItemStack input = slots[0];
		if (input != null)
			return new Object[] {gas.getFill(), gas.getMaxFill(), progress, gas.getTankType().getID(), this.pos.getX(), this.pos.getY(), this.pos.getZ(), input.getTranslationKey(), input.getCount() };
		else
			return new Object[] {gas.getFill(), gas.getMaxFill(), progress, gas.getTankType().getID(), this.pos.getX(), this.pos.getY(), this.pos.getZ(), "", 0 };
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerRBMKOutgasser(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIRBMKOutgasser(player.inventory, this);
	}
}
