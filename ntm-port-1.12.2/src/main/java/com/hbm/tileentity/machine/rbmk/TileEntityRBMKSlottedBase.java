package com.hbm.tileentity.machine.rbmk;

import com.hbm.tileentity.IGUIProvider;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import com.hbm.util.NullableInventoryStackNbt;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

/**
 * Base class for RBMK components that have GUI slots and thus have to handle
 * those things Yes it's a copy pasted MachineBase class, thank the lack of
 * multiple inheritance for that
 *
 * @author hbm
 *
 */
public abstract class TileEntityRBMKSlottedBase extends TileEntityRBMKActiveBase implements ISidedInventory, IGUIProvider {

	public ItemStack slots[];

	private String customName;

	public TileEntityRBMKSlottedBase(int scount) {
		slots = new ItemStack[scount];
	}

	@Override
	public int getSizeInventory() {
		return slots.length;
	}

	private static ItemStack nativeStack(ItemStack stack) {
		return stack == null || stack.isEmpty() ? ItemStack.EMPTY : stack;
	}

	@Override
	public ItemStack getStackInSlot(int i) {
		return nativeStack(slots[i]);
	}

	@Override
	public boolean isEmpty() {
		for(ItemStack stack : slots) {
			if(!nativeStack(stack).isEmpty()) return false;
		}
		return true;
	}

	@Override
	public void clear() {
		java.util.Arrays.fill(slots, null);
		this.markDirty();
	}

	@Override
	public ItemStack removeStackFromSlot(int i) {
		if(slots[i] != null) {
			ItemStack itemStack = slots[i];
			slots[i] = null;
			return itemStack == null || itemStack.isEmpty() ? ItemStack.EMPTY : itemStack;
		} else {
			return ItemStack.EMPTY;
		}
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemStack) {
		itemStack = itemStack == null || itemStack.isEmpty() ? null : itemStack;
		slots[i] = itemStack;
		if(itemStack != null && itemStack.getCount() > getInventoryStackLimit()) {
			itemStack.setCount(getInventoryStackLimit());
		}
	}

	@Override
	public boolean hasCustomName() {
		return this.customName != null && this.customName.length() > 0;
	}

	public String getInventoryName() {
		return getName();
	}

	public boolean hasCustomInventoryName() {
		return hasCustomName();
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
	public void openInventory(net.minecraft.entity.player.EntityPlayer viewer) {
	}

	@Override
	public void closeInventory(net.minecraft.entity.player.EntityPlayer viewer) {
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemStack) {
		return false;
	}

	@Override
	public boolean isUsableByPlayer(EntityPlayer player) {
		if(this.world.getTileEntity(new net.minecraft.util.math.BlockPos(this.pos.getX(), this.pos.getY(), this.pos.getZ())) != this) {
			return false;
		} else {
			return player.getDistanceSq(this.pos.getX() + 0.5D, this.pos.getY() + 0.5D, this.pos.getZ() + 0.5D) <= 128;
		}
	}



	@Override
	public ItemStack decrStackSize(int slot, int amount) {
		if(slots[slot] != null) {
			if(slots[slot].getCount() <= amount) {
				ItemStack itemStack = slots[slot];
				slots[slot] = null;
				return itemStack;
			}
			ItemStack itemStack1 = slots[slot].splitStack(amount);
			if(slots[slot].getCount() == 0) {
				slots[slot] = null;
			}

			return itemStack1;
		} else {
			return ItemStack.EMPTY;
		}
	}

	@Override
	public boolean canInsertItem(int i, ItemStack itemStack, net.minecraft.util.EnumFacing nativeFace) {
		int j = com.hbm.lib.ForgeDirection.fromEnumFacing(nativeFace).ordinal();
		return this.isItemValidForSlot(i, itemStack);
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemStack, net.minecraft.util.EnumFacing nativeFace) {
		int j = com.hbm.lib.ForgeDirection.fromEnumFacing(nativeFace).ordinal();
		return false;
	}

	@Override
	public int[] getSlotsForFace(net.minecraft.util.EnumFacing nativeFace) {
		int p_94128_1_ = com.hbm.lib.ForgeDirection.fromEnumFacing(nativeFace).ordinal();
		return new int[] {};
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		if(!diag) {
			NBTTagList list = nbt.getTagList("items", 10);

			for(int i = 0; i < list.tagCount(); i++) {
				NBTTagCompound nbt1 = list.getCompoundTagAt(i);
				byte b0 = nbt1.getByte("slot");
				if(b0 >= 0 && b0 < slots.length) {
					slots[b0] = NullableInventoryStackNbt.read(nbt1);
				}
			}

			customName = nbt.getString("name");
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		if(!diag) {
			NBTTagList list = new NBTTagList();

			for(int i = 0; i < slots.length; i++) {
				if(slots[i] != null) {
					NBTTagCompound nbt1 = new NBTTagCompound();
					nbt1.setByte("slot", (byte) i);
					slots[i].writeToNBT(nbt1);
					list.appendTag(nbt1);
				}
			}
			nbt.setTag("items", list);

			if (customName != null) {
				nbt.setString("name", customName);
			}
		}
		return nbt;
	}
}
