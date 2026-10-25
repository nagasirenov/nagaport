package com.hbm.tileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

@Deprecated
public class TileEntityProxyInventory extends TileEntityProxyBase implements ISidedInventory {

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

	@Override
	public int getSizeInventory() {

		ISidedInventory inv = this.getBase();

		if(inv != null)
			return inv.getSizeInventory();

		return 0;
	}

	private static ItemStack nativeStack(ItemStack stack) {
		return stack == null || stack.isEmpty() ? ItemStack.EMPTY : stack;
	}

	@Override
	public ItemStack getStackInSlot(int slot) {

		ISidedInventory inv = this.getBase();

		if(inv != null)
			return nativeStack(inv.getStackInSlot(slot));

		return ItemStack.EMPTY;
	}

	@Override
	public ItemStack decrStackSize(int slot, int count) {

		ISidedInventory inv = this.getBase();

		if(inv != null)
			return nativeStack(inv.decrStackSize(slot, count));

		return ItemStack.EMPTY;
	}

	@Override
	public ItemStack removeStackFromSlot(int slot) {

		ISidedInventory inv = this.getBase();

		if(inv != null)
			return nativeStack(inv.removeStackFromSlot(slot));

		return ItemStack.EMPTY;
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack stack) {

		ISidedInventory inv = this.getBase();

		if(inv != null)
			inv.setInventorySlotContents(slot, nativeStack(stack));
	}

	@Override
	public String getName() {

		ISidedInventory inv = this.getBase();

		if(inv != null)
			return inv.getName();

		return "";
	}

	@Override
	public boolean hasCustomName() {

		ISidedInventory inv = this.getBase();

		if(inv != null)
			return inv.hasCustomName();

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

		ISidedInventory inv = this.getBase();

		if(inv != null)
			return inv.getInventoryStackLimit();

		return 64;
	}

	@Override
	public boolean isUsableByPlayer(EntityPlayer player) {
		return false; //never usable, it's just a proxy
	}

	@Override
	public void openInventory(EntityPlayer player) { }

	@Override
	public void closeInventory(EntityPlayer player) { }

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) {

		ISidedInventory inv = this.getBase();

		if(inv != null)
			return inv.isItemValidForSlot(slot, stack);

		return false;
	}

	@Override
	public int[] getSlotsForFace(net.minecraft.util.EnumFacing nativeFace) {
		int side = com.hbm.lib.ForgeDirection.fromEnumFacing(nativeFace).ordinal();

		ISidedInventory inv = this.getBase();

		if(inv != null)
			return inv.getSlotsForFace(com.hbm.lib.ForgeDirection.getOrientation(side).toEnumFacing());

		return new int[0];
	}

	@Override
	public boolean canInsertItem(int slot, ItemStack stack, net.minecraft.util.EnumFacing nativeFace) {
		int side = com.hbm.lib.ForgeDirection.fromEnumFacing(nativeFace).ordinal();

		ISidedInventory inv = this.getBase();

		if(inv != null)
			return inv.canInsertItem(slot, stack, com.hbm.lib.ForgeDirection.getOrientation(side).toEnumFacing());

		return false;
	}

	@Override
	public boolean canExtractItem(int slot, ItemStack stack, net.minecraft.util.EnumFacing nativeFace) {
		int side = com.hbm.lib.ForgeDirection.fromEnumFacing(nativeFace).ordinal();

		ISidedInventory inv = this.getBase();

		if(inv != null)
			return inv.canExtractItem(slot, stack, com.hbm.lib.ForgeDirection.getOrientation(side).toEnumFacing());

		return false;
	}

	private ISidedInventory getBase() {

		TileEntity te = this.getTE();

		if(te instanceof ISidedInventory)
			return (ISidedInventory)te;

		return null;
	}

}
