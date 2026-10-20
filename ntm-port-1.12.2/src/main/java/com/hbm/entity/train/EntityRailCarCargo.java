package com.hbm.entity.train;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;

public abstract class EntityRailCarCargo extends EntityRailCarBase implements IInventory {

	private static final DataParameter<Integer> OCCUPIED_SLOTS = EntityDataManager.createKey(EntityRailCarCargo.class, DataSerializers.VARINT);

	protected String entityName;
	protected ItemStack[] slots = new ItemStack[this.getSizeInventory()];

	public EntityRailCarCargo(World world) {
		super(world);
	}
	
	@Override
	protected void entityInit() {
		super.entityInit();
		this.dataManager.register(OCCUPIED_SLOTS, 0);
	}
	
	public int countOccupiedSlots() {
		int slots = 0;
		
		for(int i = 0; i < this.getSizeInventory(); i++) {
			// Retain original slot presence, including a live stack consumed to zero.
			if(this.slots[i] != null) slots++;
		}
		
		return slots;
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		ItemStack stack = slots[slot];
		return stack == null || stack.isEmpty() ? ItemStack.EMPTY : stack;
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
	public void onUpdate() {
		super.onUpdate();
		if(!this.world.isRemote) this.dataManager.set(OCCUPIED_SLOTS, this.countOccupiedSlots());
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
	public void openInventory(EntityPlayer player) { }

	@Override
	public void closeInventory(EntityPlayer player) { }

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) {
		return true;
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
				ItemStack stack = new ItemStack(nbttagcompound1);
				this.slots[j] = stack.isEmpty() ? null : stack;
			}
		}
		
		this.dataManager.set(OCCUPIED_SLOTS, this.countOccupiedSlots());
	}
	
	public boolean hasCustomInventoryName() {
		return this.entityName != null;
	}

	/** Domain inventory label retained for the two original train GUIs. */
	public abstract String getInventoryName();

	@Override public boolean hasCustomName() { return hasCustomInventoryName() || super.hasCustomName(); }
	@Override public boolean isEmpty() {
		for(ItemStack stack : slots) if(stack != null && !stack.isEmpty()) return false;
		return true;
	}
	@Override public int getField(int id) { return 0; }
	@Override public void setField(int id, int value) { }
	@Override public int getFieldCount() { return 0; }
	@Override public void clear() { java.util.Arrays.fill(slots, null); }

	/** Client display value, updated by the original server-tick/load boundaries. */
	public int getSyncedOccupiedSlots() { return this.dataManager.get(OCCUPIED_SLOTS); }

	public String getEntityName() {
		return this.entityName;
	}
	
	public void setEntityName(String name) {
		this.entityName = name;
	}

	@Override
	public String getName() {
		return this.entityName != null ? this.entityName : super.getName();
	}
}
