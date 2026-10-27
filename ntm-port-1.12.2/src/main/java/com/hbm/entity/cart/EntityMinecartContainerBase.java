package com.hbm.entity.cart;

import com.hbm.items.tool.ItemModMinecart.EnumCartBase;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import com.hbm.util.NullableInventoryStackNbt;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;

/**
 * Only exists because some absolute genius at mojang thought it'd be funny to have the base class' stack array be fixed at 36.
 * Yes, 36. No more, no less. Hopper carts use it too, it's just that IInventory caps the accessible slots at 5. Amazing, isn't it?
 * @author hbm
 *
 */
public abstract class EntityMinecartContainerBase extends EntityMinecartNTM implements IInventory {

	protected ItemStack[] slots = new ItemStack[this.getSizeInventory()];

	public EntityMinecartContainerBase(World world) {
		super(world);
	}

	public EntityMinecartContainerBase(World world, double x, double y, double z, EnumCartBase type) {
		super(world, x, y, z, type);
	}

	private static ItemStack nativeStack(ItemStack stack) {
		return stack == null || stack.isEmpty() ? ItemStack.EMPTY : stack;
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return nativeStack(slots[slot]);
	}

	@Override
	public boolean isEmpty() {
		for(ItemStack stack : slots) {
			if(!nativeStack(stack).isEmpty()) return false;
		}
		return true;
	}

	@Override
	public ItemStack decrStackSize(int slot, int amount) {
		if(this.slots[slot] != null) {
			ItemStack itemstack;

			if(this.slots[slot].getCount() <= amount) {
				itemstack = this.slots[slot];
				this.slots[slot] = null;
				return itemstack;
			} else {
				itemstack = this.slots[slot].splitStack(amount);

				if(this.slots[slot].getCount() == 0) {
					this.slots[slot] = null;
				}

				return itemstack;
			}
		} else {
			return ItemStack.EMPTY;
		}
	}

	@Override
	public ItemStack removeStackFromSlot(int slot) {
		if(this.slots[slot] != null) {
			ItemStack itemstack = this.slots[slot];
			this.slots[slot] = null;
			return itemstack == null || itemstack.isEmpty() ? ItemStack.EMPTY : itemstack;
		} else {
			return ItemStack.EMPTY;
		}
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack stack) {
		stack = stack == null || stack.isEmpty() ? null : stack;
		this.slots[slot] = stack;

		if(stack != null && stack.getCount() > this.getInventoryStackLimit()) {
			stack.setCount(this.getInventoryStackLimit());
		}
	}

	@Override
	public String getName() {
		return this.hasCustomName() ? this.getCustomNameTag() : "container.minecart";
	}

	public String getInventoryName() {
		return getName();
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public void markDirty() { }

	@Override
	public boolean isUsableByPlayer(EntityPlayer player) {
		return this.isDead ? false : player.getDistanceSq(this) <= 64.0D;
	}

	@Override
	public void openInventory(net.minecraft.entity.player.EntityPlayer viewer) { }

	@Override
	public void closeInventory(net.minecraft.entity.player.EntityPlayer viewer) { }

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) {
		return true;
	}

	@Override public int getField(int id) { return 0; }
	@Override public void setField(int id, int value) {}
	@Override public int getFieldCount() { return 0; }

	@Override
	public void clear() {
		for(int i = 0; i < this.slots.length; i++) {
			this.slots[i] = null;
		}
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbt) {
		super.writeEntityToNBT(nbt);
		NBTTagList nbttaglist = new NBTTagList();

		for(int i = 0; i < this.slots.length; ++i) {
			if(this.slots[i] != null) {
				NBTTagCompound nbttagcompound1 = new NBTTagCompound();
				nbttagcompound1.setByte("Slot", (byte) i);
				this.slots[i].writeToNBT(nbttagcompound1);
				nbttaglist.appendTag(nbttagcompound1);
			}
		}

		nbt.setTag("Items", nbttaglist);
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbt) {
		super.readEntityFromNBT(nbt);
		NBTTagList nbttaglist = nbt.getTagList("Items", 10);
		this.slots = new ItemStack[this.getSizeInventory()];

		for(int i = 0; i < nbttaglist.tagCount(); ++i) {
			NBTTagCompound nbttagcompound1 = nbttaglist.getCompoundTagAt(i);
			int j = nbttagcompound1.getByte("Slot") & 255;

			if(j >= 0 && j < this.slots.length) {
				this.slots[j] = NullableInventoryStackNbt.read(nbttagcompound1);
			}
		}
	}
}
