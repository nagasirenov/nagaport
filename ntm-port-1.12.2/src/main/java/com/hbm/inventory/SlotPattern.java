package com.hbm.inventory;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotPattern extends Slot {

	protected boolean canHover = true;

	protected boolean allowStackSize = false;
	
	public SlotPattern(IInventory inv, int index, int x, int y) {
		super(inv, index, x, y);
	}

	public SlotPattern(IInventory inv, int index, int x, int y, boolean allowStackSize) {
		super(inv, index, x, y);
		this.allowStackSize = allowStackSize;
	}
	
	@Override
	public boolean canTakeStack(EntityPlayer player) {
		return false;
	}
	
	@Override
	public int getSlotStackLimit() {
		return 1;
	}
	
	@Override
	public void putStack(ItemStack stack) {
		if (stack != null && !stack.isEmpty()) {
			stack = stack.copy();
			
			if (!allowStackSize)
				stack.setCount(1);
		}
		super.putStack(stack == null || stack.isEmpty() ? ItemStack.EMPTY : stack);
	}

	public SlotPattern disableHover() {
		this.canHover = false;
		return this;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean isEnabled() {
		return canHover;
	}
}
