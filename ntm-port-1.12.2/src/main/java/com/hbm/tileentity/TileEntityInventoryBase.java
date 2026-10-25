package com.hbm.tileentity;

import java.util.Arrays;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;

public abstract class TileEntityInventoryBase extends TileEntity implements ISidedInventory {

	/** Legacy domain storage remains nullable; native inventory calls never return null. */
	public ItemStack slots[];
	private String customName;

	public TileEntityInventoryBase(int scount) {
		slots = new ItemStack[scount];
	}

	private static ItemStack nativeStack(ItemStack stack) {
		return stack == null || stack.isEmpty() ? ItemStack.EMPTY : stack;
	}

	@Override
	public int getSizeInventory() {
		return slots.length;
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
		ItemStack stack = getStackInSlot(i);
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

	/** Native name contract includes the custom name, unlike the old subclass hook. */
	@Override
	public String getName() {
		return this.hasCustomName() ? this.customName : getDefaultName();
	}

	protected abstract String getDefaultName();

	@Override
	public boolean hasCustomName() {
		return this.customName != null && this.customName.length() > 0;
	}

	@Override
	public ITextComponent getDisplayName() {
		return hasCustomName() ? new TextComponentString(getName()) : new TextComponentTranslation(getName());
	}

	/** Retained for GUISoyuzCapsule until that GUI is ported. */
	public String getInventoryName() { return getName(); }
	/** Retained for GUISoyuzCapsule until that GUI is ported. */
	public boolean hasCustomInventoryName() { return hasCustomName(); }

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

	/** Retained for ContainerSoyuzCapsule until that container is ported. */

	@Override
	public void openInventory(EntityPlayer player) {}
	@Override
	public void closeInventory(EntityPlayer player) {}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemStack) {
		return false;
	}

	@Override
	public ItemStack decrStackSize(int i, int j) {
		if(!getStackInSlot(i).isEmpty()) {
			if(slots[i].getCount() <= j) {
				ItemStack itemStack = slots[i];
				slots[i] = null;
				return itemStack;
			}
			ItemStack itemStack1 = slots[i].splitStack(j);
			if(slots[i].isEmpty()) {
				slots[i] = null;
			}
			return nativeStack(itemStack1);
		} else {
			return ItemStack.EMPTY;
		}
	}

	@Override
	public boolean canInsertItem(int i, ItemStack itemStack, EnumFacing side) {
		return this.isItemValidForSlot(i, itemStack);
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemStack, EnumFacing side) {
		return false;
	}

	@Override
	public int[] getSlotsForFace(EnumFacing side) {
		return new int[] { 0 };
	}

	@Override public int getField(int id) { return 0; }
	@Override public void setField(int id, int value) {}
	@Override public int getFieldCount() { return 0; }
	@Override public void clear() { Arrays.fill(slots, null); }

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		NBTTagList list = nbt.getTagList("items", 10);
		// Preserve the legacy overlay and signed-byte slot bounds, including duplicate entries.
		for(int i = 0; i < list.tagCount(); i++) {
			NBTTagCompound nbt1 = list.getCompoundTagAt(i);
			byte b0 = nbt1.getByte("slot");
			if(b0 >= 0 && b0 < slots.length) {
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
		for(int i = 0; i < slots.length; i++) {
			if(!getStackInSlot(i).isEmpty()) {
				NBTTagCompound nbt1 = new NBTTagCompound();
				nbt1.setByte("slot", (byte)i);
				slots[i].writeToNBT(nbt1);
				list.appendTag(nbt1);
			}
		}
		nbt.setTag("items", list);
		if(customName != null) {
			nbt.setString("name", customName);
		}
		return nbt;
	}
}
