package com.hbm.tileentity.machine;

import api.hbm.block.ICrucibleAcceptor;
import api.hbm.fluid.IFluidStandardTransceiver;
import com.hbm.blocks.BlockDummyable;
import com.hbm.inventory.container.ContainerMachineStrandCaster;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.gui.GUIMachineStrandCaster;
import com.hbm.inventory.material.Mats;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemMold;
import com.hbm.items.machine.ItemScraps;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.util.fauxpointtwelve.DirPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

//god thank you bob for this base class
public class TileEntityMachineStrandCaster extends TileEntityFoundryCastingBase implements IGUIProvider, ICrucibleAcceptor, ISidedInventory, IFluidStandardTransceiver, IInventory {

	public FluidTank water;
	public FluidTank steam;
	private long lastProgressTick = 0;

	public String getName() {
		return "container.machineStrandCaster";
	}

	@Override
	public String getInventoryName() {
		return getName();
	}

	public TileEntityMachineStrandCaster() {
		super(7);
		water = new FluidTank(Fluids.WATER, 64_000);
		steam = new FluidTank(Fluids.SPENTSTEAM, 64_000);
	}

	@Override
	public void update() {

		if(!this.world.isRemote) {

			if(this.lastType != this.type || this.lastAmount != this.amount) {
				this.world.markBlockForUpdate(this.pos.getX(), this.pos.getY(), this.pos.getZ());
				this.lastType = this.type;
				this.lastAmount = this.amount;
			}

			// In case of overfill problems, spit out the excess as scrap
			if(amount > getCapacity()) {
				ItemStack scrap = ItemScraps.create(new Mats.MaterialStack(type, Math.max(amount - getCapacity(), 0)));
				EntityItem item = new EntityItem(this.world, this.pos.getX() + 0.5, this.pos.getY() + 2, this.pos.getZ() + 0.5, scrap);
				this.world.spawnEntity(item);
				this.amount = this.getCapacity();
			}

			if(this.amount == 0) {
				this.type = null;
			}

			this.updateConnections();

			int moldsToCast = maxProcessable();
		
			// Makes it flush the buffers after 10 seconds of inactivity, or when they're full
			if (moldsToCast > 0 && (moldsToCast >= 9 || this.world.getWorldTime() >= lastProgressTick + 200)) {

				ItemMold.Mold mold = this.getInstalledMold();
				
				this.amount -= moldsToCast * mold.getCost();

				ItemStack out = mold.getOutput(type);
				int remaining = out.stackSize * moldsToCast;
				final int maxStackSize = out.getMaxStackSize();

				for (int i = 1; i < 7; i++) {
					if (remaining <= 0) {
						break;
					}

					if (slots[i] == null) {
						slots[i] = new ItemStack(out.getItem(), 0, out.getItemDamage());
					}

					if (slots[i].isItemEqual(out)) {
						int toDeposit = Math.min(remaining, maxStackSize - slots[i].stackSize);
						slots[i].stackSize += toDeposit;
						remaining -= toDeposit;
					}
				}

				markChanged();

				water.setFill(water.getFill() - getWaterRequired() * moldsToCast);
				steam.setFill(steam.getFill() + getWaterRequired() * moldsToCast);

				lastProgressTick = this.world.getWorldTime();
			}

			networkPackNT(150);
		}
	}

	private int maxProcessable() {
		ItemMold.Mold mold = this.getInstalledMold();
		if (type == null || mold == null || mold.getOutput(type) == null) {
			return 0;
		}

		int freeSlots = 0;
		final int stackLimit = mold.getOutput(type).getMaxStackSize();

		for (int i = 1; i < 7; i++) {
			if (slots[i] == null) {
				freeSlots += stackLimit;
			} else if (slots[i].isItemEqual(mold.getOutput(type))) {
				freeSlots += stackLimit - slots[i].stackSize;
			}
		}

		int moldsToCast = amount / mold.getCost();
		moldsToCast = Math.min(moldsToCast, freeSlots / mold.getOutput(type).stackSize);
		moldsToCast = Math.min(moldsToCast, water.getFill() / getWaterRequired());
		moldsToCast = Math.min(moldsToCast, (steam.getMaxFill() - steam.getFill()) / getWaterRequired());

		return moldsToCast;
	}

	public DirPos[] getFluidConPos() {

		ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset);
		ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

		return new DirPos[] { new DirPos(this.pos.getX() + rot.offsetX * 2 - dir.offsetX, this.pos.getY(), this.pos.getZ() + rot.offsetZ * 2 - dir.offsetZ, rot), new DirPos(this.pos.getX() - rot.offsetX - dir.offsetX, this.pos.getY(), this.pos.getZ() - rot.offsetZ - dir.offsetZ, rot.getOpposite()), new DirPos(this.pos.getX() + rot.offsetX * 2 - dir.offsetX * 5, this.pos.getY(), this.pos.getZ() + rot.offsetZ * 2 - dir.offsetZ * 5, rot), new DirPos(this.pos.getX() - rot.offsetX - dir.offsetX * 5, this.pos.getY(), this.pos.getZ() - rot.offsetZ - dir.offsetZ * 5, rot.getOpposite()) };
	}

	public int[][] getMetalPourPos() {

		ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset);
		ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

		return new int[][] { new int[] { this.pos.getX() + rot.offsetX - dir.offsetX, this.pos.getY() + 2, this.pos.getZ() + rot.offsetZ - dir.offsetZ }, new int[] { this.pos.getX() - dir.offsetX, this.pos.getY() + 2, this.pos.getZ() - dir.offsetZ }, new int[] { this.pos.getX() + rot.offsetX, this.pos.getY() + 2, this.pos.getZ() + rot.offsetZ }, new int[] { this.pos.getX(), this.pos.getY() + 2, this.pos.getZ() } };
	}

	@Override
	public ItemMold.Mold getInstalledMold() {
		if(slots[0] == null)
			return null;

		if(slots[0].getItem() == ModItems.mold) {
			return ((ItemMold) slots[0].getItem()).getMold(slots[0]);
		}

		return null;
	}

	@Override
	public int getMoldSize() {
		return getInstalledMold().size;
	}

	@Override
	public boolean canAcceptPartialPour(World world, int x, int y, int z, double dX, double dY, double dZ, ForgeDirection side, Mats.MaterialStack stack) {

		if(side != ForgeDirection.UP)
			return false;
		for(int[] pos : getMetalPourPos()) {
			if(pos[0] == x && pos[1] == y && pos[2] == z) {
				return this.standardCheck(world, x, y, z, side, stack);
			}
		}
		return false;

	}

	@Override
	public boolean standardCheck(World world, int x, int y, int z, ForgeDirection side, Mats.MaterialStack stack) {
		if(this.type != null && this.type != stack.material)
			return false;
		int limit = this.getInstalledMold() != null ? this.getInstalledMold().getCost() * 9 : this.getCapacity();
		return !(this.amount >= limit || getInstalledMold() == null);
	}

	@Override
	public int getCapacity() {
		ItemMold.Mold mold = this.getInstalledMold();
		return mold == null ? 50000 : mold.getCost() * 10;
	}

	private int getWaterRequired() {
		return getInstalledMold() != null ? 5 * getInstalledMold().getCost() : 50;
	}

	private void updateConnections() {
		for(DirPos pos : getFluidConPos()) {
			this.trySubscribe(water.getTankType(), this.world, pos.getX(), pos.getY(), pos.getZ(), pos.getDir());
			this.sendFluid(steam, this.world, pos.getX(), pos.getY(), pos.getZ(), pos.getDir());
		}
	}

	@Override
	public Mats.MaterialStack standardAdd(World world, int x, int y, int z, ForgeDirection side, Mats.MaterialStack stack) {
		this.type = stack.material;
		int limit = this.getInstalledMold() != null ? this.getInstalledMold().getCost() * 9 : this.getCapacity();
		if(stack.amount + this.amount <= limit) {
			this.amount += stack.amount;
			return null;
		}

		int required = limit - this.amount;
		this.amount = limit;

		stack.amount -= required;

		lastProgressTick = world.getWorldTime();

		return stack;
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
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerMachineStrandCaster(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIMachineStrandCaster(player.inventory, this);
	}

	@Override
	public void serialize(ByteBuf buf) {
		water.serialize(buf);
		steam.serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		water.deserialize(buf);
		steam.deserialize(buf);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		water.writeToNBT(nbt, "w");
		steam.writeToNBT(nbt, "s");
		nbt.setLong("t", lastProgressTick);
		return nbt;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		water.readFromNBT(nbt, "w");
		steam.readFromNBT(nbt, "s");
		lastProgressTick = nbt.getLong("t");
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack stack) {
		if(i == 0)
			return stack.getItem() == ModItems.mold;
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}

	@Override
	public int[] getSlotsForFace(net.minecraft.util.EnumFacing nativeFace) {
		int meta = com.hbm.lib.ForgeDirection.fromEnumFacing(nativeFace).ordinal();
		return new int[] { 1, 2, 3, 4, 5, 6 };
	}

	public void markChanged() {
		this.world.markTileEntityChunkModified(this.pos.getX(), this.pos.getY(), this.pos.getZ(), this);
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player) {
		if(this.world.getTileEntity(new net.minecraft.util.math.BlockPos(this.pos.getX(), this.pos.getY(), this.pos.getZ())) != this) {
			return false;
		} else {
			return player.getDistanceSq(this.pos.getX() + 0.5D, this.pos.getY() + 0.5D, this.pos.getZ() + 0.5D) <= 128;
		}
	}

	@Override
	public boolean canInsertItem(int slot, ItemStack itemStack, net.minecraft.util.EnumFacing nativeFace) {
		int side = com.hbm.lib.ForgeDirection.fromEnumFacing(nativeFace).ordinal();
		return this.isItemValidForSlot(slot, itemStack);
	}

	@Override
	public boolean canExtractItem(int slot, ItemStack itemStack, net.minecraft.util.EnumFacing nativeFace) {
		int side = com.hbm.lib.ForgeDirection.fromEnumFacing(nativeFace).ordinal();
		return !this.isItemValidForSlot(slot, itemStack);
	}

	AxisAlignedBB bb = null;

	@Override
	public AxisAlignedBB getRenderBoundingBox() {

		if(bb == null) {
			bb = AxisAlignedBB.getBoundingBox(this.pos.getX() - 7, this.pos.getY(), this.pos.getZ() - 7, this.pos.getX() + 7, this.pos.getY() + 3, this.pos.getZ() + 7);
		}
		return bb;
	}

	public boolean isLoaded = true;

	@Override
	public boolean isLoaded() {
		return isLoaded;
	}

	@Override
	public void onChunkUnload() {
		super.onChunkUnload();
		this.isLoaded = false;
	}
}
