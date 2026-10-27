package com.hbm.tileentity.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import com.hbm.util.NullableInventoryStackNbt;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

/**
 * "Multiple inheritance is bad because...uhhhh...i guess if you do it wrong then it can lead to bad things"
 * ~ genuinely retarded people on StackOverflow
 * like yeah, doing things wrong can lead to bad things
 * no shit
 * just like how java operates already
 * you fucking dork
 *
 * this class has to extend TileEntityRequestNetwork for all the network stuff to work
 * but it also needs slots and all the container boilerplate crap
 * since multiple inheritance is a sin punishable by stoning, i had to cram the entire contents of TileEntityMachineBase into this class
 * is this good code? is this what you wanted? was it worth avoiding those hypothetical scenarios where multiple inheritance is le bad?
 * i believe that neither heaven nor hell awaits me when all is said and done
 * saint peter will send me to southend
 *
 * @author hbm
 */
public abstract class TileEntityRequestNetworkContainer extends TileEntityRequestNetwork implements ISidedInventory {

	public ItemStack slots[];

	private String customName;

	public TileEntityRequestNetworkContainer(int scount) {
		slots = new ItemStack[scount];
	}

	private static ItemStack nativeStack(ItemStack stack) { return stack == null || stack.isEmpty() ? ItemStack.EMPTY : stack; }
	@Override public void clear() { for(int i = 0; i < slots.length; i++) slots[i] = null; }
	@Override public int getSizeInventory() { return slots.length; }
	@Override public ItemStack getStackInSlot(int i) { return nativeStack(slots[i]); }
	@Override public boolean isEmpty() {
		for(ItemStack stack : slots) if(!nativeStack(stack).isEmpty()) return false;
		return true;
	}
	@Override public void openInventory(net.minecraft.entity.player.EntityPlayer viewer) { }
	@Override public void closeInventory(net.minecraft.entity.player.EntityPlayer viewer) { }
	@Override public boolean isItemValidForSlot(int slot, ItemStack itemStack) { return false; }
	@Override public boolean canInsertItem(int slot, ItemStack itemStack, net.minecraft.util.EnumFacing nativeFace) {
		int side = com.hbm.lib.ForgeDirection.fromEnumFacing(nativeFace).ordinal(); return this.isItemValidForSlot(slot, itemStack); }
	@Override public boolean canExtractItem(int slot, ItemStack itemStack, net.minecraft.util.EnumFacing nativeFace) {
		int side = com.hbm.lib.ForgeDirection.fromEnumFacing(nativeFace).ordinal(); return false; }
	@Override public int[] getSlotsForFace(net.minecraft.util.EnumFacing nativeFace) {
		int side = com.hbm.lib.ForgeDirection.fromEnumFacing(nativeFace).ordinal(); return new int[] { }; }

	public void markChanged() {
		this.world.markChunkDirty(this.pos, this);
	}

	@Override
	public ItemStack removeStackFromSlot(int i) {
		if(slots[i] != null)
		{
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
		if(itemStack != null && itemStack.getCount() > getInventoryStackLimit())
		{
			itemStack.setCount(getInventoryStackLimit());
		}
	}

	public String getInventoryName() { return this.hasCustomName() ? this.customName : getName(); }
	public abstract String getName();
	@Override public boolean hasCustomName() { return this.customName != null && this.customName.length() > 0; }
	public boolean hasCustomInventoryName() { return this.hasCustomName(); }
	public void setCustomName(String name) { this.customName = name; markDirty(); }
	@Override public int getInventoryStackLimit() { return 64; }
	@Override public int getField(int id) { return 0; }
	@Override public void setField(int id, int value) {}
	@Override public int getFieldCount() { return 0; }

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
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		NBTTagList list = nbt.getTagList("items", 10);

		for(int i = 0; i < list.tagCount(); i++)
		{
			NBTTagCompound nbt1 = list.getCompoundTagAt(i);
			byte b0 = nbt1.getByte("slot");
			if(b0 >= 0 && b0 < slots.length)
			{
				slots[b0] = NullableInventoryStackNbt.read(nbt1);
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
			if(slots[i] != null)
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
}
