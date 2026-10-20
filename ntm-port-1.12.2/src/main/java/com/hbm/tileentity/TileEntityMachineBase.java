package com.hbm.tileentity;

import java.util.List;
import java.util.Arrays;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;

import com.hbm.dim.CelestialBody;
import com.hbm.dim.orbit.WorldProviderOrbit;
import com.hbm.dim.trait.CBT_Atmosphere;
import com.hbm.handler.atmosphere.AtmosphereBlob;
import com.hbm.handler.atmosphere.ChunkAtmosphereManager;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.util.fauxpointtwelve.DirPos;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;
import net.minecraftforge.fluids.FluidTank;

public abstract class TileEntityMachineBase extends TileEntityLoadedBase implements ISidedInventory, net.minecraft.util.ITickable {

	/** Nullable domain storage; native IInventory methods expose EMPTY for absence. */
	public ItemStack slots[];

	private String customName;

	public TileEntityMachineBase(int slotCount) {
		slots = new ItemStack[slotCount];
	}

	/** The "chunks is modified, pls don't forget to save me" effect of markDirty, minus the block updates */
	public void markChanged() {
		this.world.markChunkDirty(this.pos, this);
	}

	@Override
	public int getSizeInventory() {
		return slots.length;
	}

	private static ItemStack nativeStack(ItemStack stack) {
		return stack == null || stack.isEmpty() ? ItemStack.EMPTY : stack;
	}

	@Override
	public boolean isEmpty() {
		for(ItemStack stack : slots) {
			if(!nativeStack(stack).isEmpty()) return false;
		}
		return true;
	}

	@Override
	public ItemStack getStackInSlot(int i) {
		return nativeStack(slots[i]);
	}

	@Override
	public ItemStack removeStackFromSlot(int i) {
		ItemStack stack = nativeStack(slots[i]);
		slots[i] = null;
		return stack;
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemStack) {
		slots[i] = nativeStack(itemStack).isEmpty() ? null : itemStack;
		if(slots[i] != null && itemStack.getCount() > getInventoryStackLimit()) {
			itemStack.setCount(getInventoryStackLimit());
		}
	}

	// Machines sync their actual progress/power/muffled data via serialize, not vanilla window fields.
	@Override public int getField(int id) { return 0; }
	@Override public void setField(int id, int value) {}
	@Override public int getFieldCount() { return 0; }
	@Override public void clear() { Arrays.fill(slots, null); }

	@Override
	public String getName() {
		return this.hasCustomName() ? this.customName : getDefaultName();
	}

	public abstract String getDefaultName();

	@Override
	public net.minecraft.util.text.ITextComponent getDisplayName() {
		return hasCustomName() ? new net.minecraft.util.text.TextComponentString(getName()) : new net.minecraft.util.text.TextComponentTranslation(getName());
	}

	/** Narrow compatibility for existing machine GUI/container callers. */
	public String getInventoryName() { return getName(); }

	/** Narrow compatibility for existing machine GUI/container callers. */
	public boolean hasCustomInventoryName() { return hasCustomName(); }

	@Override
	public boolean hasCustomName() {
		return this.customName != null && this.customName.length() > 0;
	}

	public void setCustomName(String name) {
		this.customName = name;
		markDirty();
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public boolean isUsableByPlayer(EntityPlayer player) {
		if(world.getTileEntity(pos) != this) {
			return false;
		} else {
			return player.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 128;
		}
	}

	@Override
	public void openInventory(EntityPlayer player) {}
	@Override
	public void closeInventory(EntityPlayer player) {}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack itemStack) {
		return false;
	}

	@Override
	public ItemStack decrStackSize(int slot, int amount) {
		if(!nativeStack(slots[slot]).isEmpty()) {

			if(slots[slot].getCount() <= amount) {
				ItemStack itemStack = slots[slot];
				slots[slot] = null;
				return itemStack;
			}

			ItemStack itemStack1 = slots[slot].splitStack(amount);
			if(slots[slot].isEmpty()) {
				slots[slot] = null;
			}

			return nativeStack(itemStack1);
		} else {
			return ItemStack.EMPTY;
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
		return false;
	}

	@Override
	public int[] getSlotsForFace(net.minecraft.util.EnumFacing nativeFace) {
		int side = com.hbm.lib.ForgeDirection.fromEnumFacing(nativeFace).ordinal();
		return new int[] { };
	}

	public int getGaugeScaled(int i, FluidTank tank) {
		return tank.getFluidAmount() * i / tank.getCapacity();
	}

	//abstracting this method forces child classes to implement it
	//so i don't have to remember the fucking method name
	//was it update? onUpdate? updateTile? did it have any args?
	//shit i don't know man
	@Override
	public abstract void update();

	@Deprecated
	public void handleButtonPacket(int value, int meta) { }

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		NBTTagList list = nbt.getTagList("items", 10);

		for(int i = 0; i < list.tagCount(); i++)
		{
			NBTTagCompound nbt1 = list.getCompoundTagAt(i);
			byte b0 = nbt1.getByte("slot");
			if(b0 >= 0 && b0 < slots.length)
			{
				ItemStack stack = new ItemStack(nbt1);
				slots[b0] = stack.isEmpty() ? null : stack;
			}
		}

		customName = nbt.getString("name");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		NBTTagList list = new NBTTagList();

		for(int i = 0; i < slots.length; i++)
		{
			if(!nativeStack(slots[i]).isEmpty())
			{
				NBTTagCompound nbt1 = new NBTTagCompound();
				nbt1.setByte("slot", (byte)i);
				slots[i].writeToNBT(nbt1);
				list.appendTag(nbt1);
			}
		}
		nbt.setTag("items", list);

		if (customName != null) {
			nbt.setString("name", customName);
		}
		return nbt;
	}

	public void updateRedstoneConnection(DirPos pos) {

		BlockPos neighborPos = new BlockPos(pos.getX(), pos.getY(), pos.getZ());
		ForgeDirection dir = pos.getDir();
		IBlockState state1 = world.getBlockState(neighborPos);
		Block block1 = state1.getBlock();

		block1.onNeighborChange(world, neighborPos, this.pos);
		// Earlier callbacks may mutate metadata/state; keep the captured block, but refresh state.
		block1.neighborChanged(world.getBlockState(neighborPos), world, neighborPos, this.getBlockType(), this.pos);
		if(block1.isNormalCube(world.getBlockState(neighborPos), world, neighborPos)) {
			neighborPos = neighborPos.add(dir.offsetX, dir.offsetY, dir.offsetZ);
			IBlockState state2 = world.getBlockState(neighborPos);
			Block block2 = state2.getBlock();

			if(block2.getWeakChanges(world, neighborPos)) {
				block2.onNeighborChange(world, neighborPos, this.pos);
				block2.neighborChanged(world.getBlockState(neighborPos), world, neighborPos, this.getBlockType(), this.pos);
			}
		}
	}

	// TODO: Consume air from connected tanks if available
	public boolean breatheAir(int amount) {
		return breatheAir(world, pos.getX(), pos.getY(), pos.getZ(), amount);
	}

	public static boolean breatheAir(World world, int x, int y, int z, int amount) {
		CBT_Atmosphere atmosphere = world.provider instanceof WorldProviderOrbit ? null : CelestialBody.getTrait(world, CBT_Atmosphere.class);
		if(atmosphere != null) {
			if(atmosphere.hasFluid(Fluids.EARTHAIR, 0.19) || atmosphere.hasFluid(Fluids.OXYGEN, 0.09)) {
				return true;
			}
		}

		List<AtmosphereBlob> blobs = ChunkAtmosphereManager.proxy.getBlobs(world, x, y, z);
		for(AtmosphereBlob blob : blobs) {
			if(blob.hasFluid(Fluids.EARTHAIR, 0.19) || blob.hasFluid(Fluids.OXYGEN, 0.09)) {
				blob.consume(amount);
				return true;
			}
		}

		return false;
	}

}
