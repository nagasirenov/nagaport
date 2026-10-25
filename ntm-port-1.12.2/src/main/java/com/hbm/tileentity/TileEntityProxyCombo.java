package com.hbm.tileentity;

import api.hbm.block.ICrucibleAcceptor;
import com.hbm.handler.CompatHandler;
import com.hbm.handler.CompatHandler.OCComponent;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.tank.FluidTank;

import api.hbm.energymk2.IEnergyConductorMK2;
import api.hbm.energymk2.IEnergyConnectorMK2;
import api.hbm.energymk2.IEnergyReceiverMK2;
import api.hbm.fluidmk2.IFluidConnectorMK2;
import api.hbm.fluidmk2.IFluidReceiverMK2;
import api.hbm.redstoneoverradio.IRORInfo;
import api.hbm.redstoneoverradio.IRORInteractive;
import api.hbm.redstoneoverradio.IRORValueProvider;
import api.hbm.tile.IHeatSource;
import com.hbm.inventory.material.Mats;
import com.hbm.util.Compat;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SimpleComponent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

@Optional.InterfaceList({
		@Optional.Interface(iface = "com.hbm.handler.CompatHandler.OCComponent", modid = "opencomputers"),
		@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "opencomputers")
})
public class TileEntityProxyCombo extends TileEntityProxyBase implements IEnergyReceiverMK2, IEnergyConductorMK2, ISidedInventory, IFluidReceiverMK2, IHeatSource, ICrucibleAcceptor, SimpleComponent, OCComponent, IRORValueProvider, IRORInteractive {

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

	TileEntity tile;
	boolean inventory;
	boolean power;
	boolean conductor;
	boolean fluid;
	boolean heat;
	public boolean moltenMetal;

	// due to some issues with OC deciding that it's gonna call the component name function before the worldObj is loaded
	// the component name must be cached to prevent it from shitting itself
	String componentName = CompatHandler.nullComponent;

	public TileEntityProxyCombo() { }

	public TileEntityProxyCombo(boolean inventory, boolean power, boolean fluid) {
		this.inventory = inventory;
		this.power = power;
		this.fluid = fluid;
	}

	public TileEntityProxyCombo inventory() {
		this.inventory = true;
		return this;
	}

	public TileEntityProxyCombo power() {
		this.power = true;
		return this;
	}
	public TileEntityProxyCombo conductor() {
		this.conductor = true;
		return this;
	}
	public TileEntityProxyCombo moltenMetal() {
		this.moltenMetal = true;
		return this;
	}
	public TileEntityProxyCombo fluid() {
		this.fluid = true;
		return this;
	}

	public TileEntityProxyCombo heatSource() {
		this.heat = true;
		return this;
	}

	/** Returns the actual tile entity that represents the core. Only for internal use, and EnergyControl. */
	public TileEntity getTile() {
		if(tile == null || tile.isInvalid() || (tile instanceof TileEntityLoadedBase && !((TileEntityLoadedBase) tile).isLoaded)) {
			tile = this.getTE();
		}
		return tile;
	}

	/** Returns the core tile entity, or a delegate object. */
	protected Object getCoreObject() {
		return getTile();
	}

	@Override
	public void setPower(long i) {

		if(!power)
			return;

		if(getCoreObject() instanceof IEnergyReceiverMK2) {
			((IEnergyReceiverMK2)getCoreObject()).setPower(i);
		}
	}

	@Override
	public long getPower() {

		if(!power)
			return 0;

		if(getCoreObject() instanceof IEnergyReceiverMK2) {
			return ((IEnergyReceiverMK2)getCoreObject()).getPower();
		}

		return 0;
	}

	@Override
	public long getMaxPower() {

		if(!power)
			return 0;

		if(getCoreObject() instanceof IEnergyReceiverMK2) {
			return ((IEnergyReceiverMK2)getCoreObject()).getMaxPower();
		}

		return 0;
	}

	@Override
	public long transferPower(long power) {

		if(!this.power)
			return power;

		if(getCoreObject() instanceof IEnergyReceiverMK2) {
			return ((IEnergyReceiverMK2)getCoreObject()).transferPower(power);
		}

		return power;
	}

	@Override
	public boolean canConnect(ForgeDirection dir) {

		if(power && getCoreObject() instanceof IEnergyConnectorMK2) {
			return ((IEnergyConnectorMK2)getCoreObject()).canConnect(dir);
		}

		if(conductor && getCoreObject() instanceof IEnergyConductorMK2) {
			return ((IEnergyConductorMK2)getCoreObject()).canConnect(dir);
		}

		return false;
	}

	@Override
	public boolean allowDirectProvision() {
		if(!power) return false;
		if(getCoreObject() instanceof IEnergyReceiverMK2) return ((IEnergyReceiverMK2)getCoreObject()).allowDirectProvision();
		return true;
	}

	public static final FluidTank[] EMPTY_TANKS = new FluidTank[0];

	@Override
	public FluidTank[] getAllTanks() {
		if(!fluid) return EMPTY_TANKS;

		if(getCoreObject() instanceof IFluidReceiverMK2) {
			return ((IFluidReceiverMK2)getCoreObject()).getAllTanks();
		}

		return EMPTY_TANKS;
	}

	@Override
	public long transferFluid(FluidType type, int pressure, long amount) {
		if(!fluid) return amount;

		if(getCoreObject() instanceof IFluidReceiverMK2) {
			return ((IFluidReceiverMK2)getCoreObject()).transferFluid(type, pressure, amount);
		}

		return amount;
	}

	@Override
	public long getDemand(FluidType type, int pressure) {
		if(!fluid) return 0;

		if(getCoreObject() instanceof IFluidReceiverMK2) {
			return ((IFluidReceiverMK2)getCoreObject()).getDemand(type, pressure);
		}

		return 0;
	}

	@Override
	public boolean canConnect(FluidType type, ForgeDirection dir) {

		if(!this.fluid)
			return false;

		if(getCoreObject() instanceof IFluidConnectorMK2) {
			return ((IFluidConnectorMK2) getCoreObject()).canConnect(type, dir);
		}
		return true;
	}

	@Override
	public int getSizeInventory() {

		if(!inventory)
			return 0;

		if(getCoreObject() instanceof ISidedInventory) {
			return ((ISidedInventory)getCoreObject()).getSizeInventory();
		}

		return 0;
	}

	private static ItemStack nativeStack(ItemStack stack) {
		return stack == null || stack.isEmpty() ? ItemStack.EMPTY : stack;
	}

	@Override
	public ItemStack getStackInSlot(int slot) {

		if(!inventory)
			return ItemStack.EMPTY;

		if(getCoreObject() instanceof ISidedInventory) {
			return nativeStack(((ISidedInventory)getCoreObject()).getStackInSlot(slot));
		}

		return ItemStack.EMPTY;
	}

	@Override
	public ItemStack decrStackSize(int i, int j) {

		if(getCoreObject() instanceof ISidedInventory) {
			return nativeStack(((ISidedInventory)getCoreObject()).decrStackSize(i, j));
		}

		return ItemStack.EMPTY;
	}

	@Override
	public ItemStack removeStackFromSlot(int slot) {

		if(!inventory)
			return ItemStack.EMPTY;

		if(getCoreObject() instanceof ISidedInventory) {
			return nativeStack(((ISidedInventory)getCoreObject()).removeStackFromSlot(slot));
		}

		return ItemStack.EMPTY;
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack stack) {

		if(!inventory)
			return;

		if(getCoreObject() instanceof ISidedInventory) {
			((ISidedInventory)getCoreObject()).setInventorySlotContents(slot, nativeStack(stack));
		}
	}

	@Override
	public String getName() {

		if(!inventory)
			return "";

		if(getCoreObject() instanceof ISidedInventory) {
			return ((ISidedInventory)getCoreObject()).getName();
		}

		return "";
	}

	@Override
	public boolean hasCustomName() {

		if(!inventory)
			return false;

		if(getCoreObject() instanceof ISidedInventory) {
			return ((ISidedInventory)getCoreObject()).hasCustomName();
		}

		return false;
	}

	public String getInventoryName() {
		return getName();
	}

	public boolean hasCustomInventoryName() {
		return hasCustomName();
	}

	@Override
	public int getInventoryStackLimit() {

		if(!inventory)
			return 0;

		if(getCoreObject() instanceof ISidedInventory) {
			return ((ISidedInventory)getCoreObject()).getInventoryStackLimit();
		}

		return 0;
	}

	@Override
	public boolean isUsableByPlayer(EntityPlayer player) {

		if(!inventory)
			return false;

		if(getCoreObject() instanceof ISidedInventory) {
			return ((ISidedInventory)getCoreObject()).isUsableByPlayer(player);
		}

		return false;
	}

	@Override
	public void openInventory(EntityPlayer player) {

		if(!inventory)
			return;

		if(getCoreObject() instanceof ISidedInventory) {
			((ISidedInventory)getCoreObject()).openInventory(player);
		}
	}

	@Override
	public void closeInventory(EntityPlayer player) {

		if(!inventory)
			return;

		if(getCoreObject() instanceof ISidedInventory) {
			((ISidedInventory)getCoreObject()).closeInventory(player);
		}
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) {

		if(!inventory)
			return false;

		if(getCoreObject() instanceof ISidedInventory) {

			if(getCoreObject() instanceof IConditionalInvAccess) return ((IConditionalInvAccess) getCoreObject()).isItemValidForSlot(this.pos.getX(), this.pos.getY(), this.pos.getZ(), slot, stack);

			return ((ISidedInventory)getCoreObject()).isItemValidForSlot(slot, stack);
		}

		return false;
	}

	@Override
	public int[] getSlotsForFace(net.minecraft.util.EnumFacing nativeFace) {
		int side = com.hbm.lib.ForgeDirection.fromEnumFacing(nativeFace).ordinal();

		if(!inventory)
			return new int[0];

		if(getCoreObject() instanceof ISidedInventory) {

			if(getCoreObject() instanceof IConditionalInvAccess) return ((IConditionalInvAccess) getCoreObject()).getAccessibleSlotsFromSide(this.pos.getX(), this.pos.getY(), this.pos.getZ(), side);

			return ((ISidedInventory)getCoreObject()).getSlotsForFace(com.hbm.lib.ForgeDirection.getOrientation(side).toEnumFacing());
		}

		return new int[0];
	}

	@Override
	public boolean canInsertItem(int i, ItemStack stack, net.minecraft.util.EnumFacing nativeFace) {
		int j = com.hbm.lib.ForgeDirection.fromEnumFacing(nativeFace).ordinal();

		if(!inventory)
			return false;

		if(getCoreObject() instanceof ISidedInventory) {

			if(getCoreObject() instanceof IConditionalInvAccess) return ((IConditionalInvAccess) getCoreObject()).canInsertItem(this.pos.getX(), this.pos.getY(), this.pos.getZ(), i, stack, j);

			return ((ISidedInventory)getCoreObject()).canInsertItem(i, stack, com.hbm.lib.ForgeDirection.getOrientation(j).toEnumFacing());
		}

		return false;
	}

	@Override
	public boolean canExtractItem(int i, ItemStack stack, net.minecraft.util.EnumFacing nativeFace) {
		int j = com.hbm.lib.ForgeDirection.fromEnumFacing(nativeFace).ordinal();

		if(!inventory)
			return false;

		if(getCoreObject() instanceof ISidedInventory) {

			if(getCoreObject() instanceof IConditionalInvAccess) return ((IConditionalInvAccess) getCoreObject()).canExtractItem(this.pos.getX(), this.pos.getY(), this.pos.getZ(), i, stack, j);

			return ((ISidedInventory)getCoreObject()).canExtractItem(i, stack, com.hbm.lib.ForgeDirection.getOrientation(j).toEnumFacing());
		}

		return false;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		this.inventory = nbt.getBoolean("inv");
		this.power = nbt.getBoolean("power");
		this.conductor = nbt.getBoolean("conductor");
		this.fluid = nbt.getBoolean("fluid");
		this.moltenMetal = nbt.getBoolean("metal");
		this.heat = nbt.getBoolean("heat");
		if(Loader.isModLoaded("OpenComputers"))
			this.componentName = nbt.getString("ocname");

	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		nbt.setBoolean("inv", inventory);
		nbt.setBoolean("power", power);
		nbt.setBoolean("conductor", conductor);
		nbt.setBoolean("fluid", fluid);
		nbt.setBoolean("metal", moltenMetal);
		nbt.setBoolean("heat", heat);
		if(Loader.isModLoaded("OpenComputers"))
			nbt.setString("ocname", componentName);
		return nbt;
	}

	@Override
	public int getHeatStored() {

		if(!this.heat)
			return 0;

		if(getCoreObject() instanceof IHeatSource) {
			return ((IHeatSource)getCoreObject()).getHeatStored();
		}

		return 0;
	}

	@Override
	public void useUpHeat(int heat) {

		if(!this.heat)
			return;

		if(getCoreObject() instanceof IHeatSource) {
			((IHeatSource)getCoreObject()).useUpHeat(heat);
		}
	}

	@Override
	public boolean canAcceptPartialPour(World world, int x, int y, int z, double dX, double dY, double dZ, ForgeDirection side, Mats.MaterialStack stack) {
		if(this.moltenMetal && getCoreObject() instanceof ICrucibleAcceptor){
			return ((ICrucibleAcceptor)getCoreObject()).canAcceptPartialPour(world, x, y, z, dX, dY, dZ, side, stack);
		}
		return false;
	}

	@Override
	public Mats.MaterialStack pour(World world, int x, int y, int z, double dX, double dY, double dZ, ForgeDirection side, Mats.MaterialStack stack) {
		if(this.moltenMetal && getCoreObject() instanceof ICrucibleAcceptor){
			return ((ICrucibleAcceptor)getCoreObject()).pour(world, x, y, z, dX, dY, dZ, side, stack);
		}
		return null;
	}

	@Override
	public boolean canAcceptPartialFlow(World world, int x, int y, int z, ForgeDirection side, Mats.MaterialStack stack) {
		if(this.moltenMetal && getCoreObject() instanceof ICrucibleAcceptor){
			return ((ICrucibleAcceptor)getCoreObject()).canAcceptPartialFlow(world, x, y, z, side, stack);
		}
		return false;
	}

	@Override
	public Mats.MaterialStack flow(World world, int x, int y, int z, ForgeDirection side, Mats.MaterialStack stack) {
		if(this.moltenMetal && getCoreObject() instanceof ICrucibleAcceptor){
			return ((ICrucibleAcceptor)getCoreObject()).flow(world, x, y, z, side, stack);
		}
		return null;
	}

	@Override // please work
	@Optional.Method(modid = "OpenComputers")
	public String getComponentName() {
		if(this.world == null) // OC is going too fast, grab from NBT!
			return componentName;
		if(this.getCoreObject() instanceof OCComponent) {
			if (componentName == null || componentName.equals(OCComponent.super.getComponentName())) {
				componentName = ((OCComponent) this.getCoreObject()).getComponentName();
			}
			return componentName;
		}
		return OCComponent.super.getComponentName();
	}

	@Override
	@Optional.Method(modid = "OpenComputers")
	public boolean canConnectNode(net.minecraft.util.EnumFacing side) {
		if(this.getCoreObject() instanceof OCComponent) {
			boolean isComponent = false;
			if (this.world != null) {
				Object nodeTE = Compat.getTileStandard(this.world, this.pos.getX() + side.getXOffset(), this.pos.getY() + side.getYOffset(), this.pos.getZ() + side.getZOffset());
				if (nodeTE instanceof TileEntityProxyCombo) {
					TileEntityProxyCombo proxy = (TileEntityProxyCombo)nodeTE;
					if (proxy.getCoreObject() == this.getCoreObject()) isComponent = true;
				} else if (nodeTE == this.getCoreObject()) {
					isComponent = true;
				}
			}
			return (this.getBlockMetadata() >= 6 && this.getBlockMetadata() <= 11)
					&& (power || fluid) &&
					((OCComponent) this.getCoreObject()).canConnectNode(side) &&
					!isComponent;
		}
		return OCComponent.super.canConnectNode(null);
	}

	@Override
	@Optional.Method(modid = "OpenComputers")
	public String[] methods() {
		if(this.getCoreObject() instanceof OCComponent)
			return ((OCComponent) this.getCoreObject()).methods();
		return OCComponent.super.methods();
	}

	@Override
	@Optional.Method(modid = "OpenComputers")
	public Object[] invoke(String method, Context context, Arguments args) throws Exception {
		if(this.getCoreObject() instanceof OCComponent)
			return ((OCComponent) this.getCoreObject()).invoke(method, context, args);
		return OCComponent.super.invoke(null, null, null);
	}

	@Override
	public String[] getFunctionInfo() {
		if(getCoreObject() instanceof IRORInfo) return ((IRORInfo) getCoreObject()).getFunctionInfo();
		return new String[0];
	}

	@Override
	public String provideRORValue(String name) {
		if(getCoreObject() instanceof IRORValueProvider) return ((IRORValueProvider) getCoreObject()).provideRORValue(name);
		return null;
	}

	@Override
	public String runRORFunction(String name, String[] params) {
		if(getCoreObject() instanceof IRORInteractive) return ((IRORInteractive) getCoreObject()).runRORFunction(name, params);
		return null;
	}
}
