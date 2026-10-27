package com.hbm.inventory.container;

import com.hbm.inventory.SlotPattern;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerCartDestroyer extends Container {

	private IInventory diFurnace;

	public ContainerCartDestroyer(InventoryPlayer invPlayer, IInventory tedf) {
		diFurnace = tedf;

		for(int i = 0; i < 3; i++) {
			for(int j = 0; j < 3; j++) {
				this.addSlotToContainer(new SlotPattern(tedf, j + i * 3, 10 + j * 18, 17 + i * 18));
				this.addSlotToContainer(new SlotPattern(tedf, j + i * 3 + 9, 114 + j * 18, 17 + i * 18));
			}
		}

		for(int i = 0; i < 3; i++) {
			for(int j = 0; j < 9; j++) {
				this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
			}
		}

		for(int i = 0; i < 9; i++) {
			this.addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18, 142));
		}
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slot) {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return diFurnace.isUsableByPlayer(player);
	}

	@Override
	public ItemStack slotClick(int index, int button, ClickType mode, EntityPlayer player) {

		//L/R: 0
		//M3: 3
		//SHIFT: 1
		//DRAG: 5
		//System.out.println("Mode " + mode);
		//System.out.println("Slot " + index);

		if(index < 0 || index >= diFurnace.getSizeInventory()) {
			return super.slotClick(index, button, mode, player);
		}

		Slot slot = this.getSlot(index);

		ItemStack ret = ItemStack.EMPTY;
		ItemStack held = player.inventory.getItemStack();

		if(slot.getHasStack())
			ret = slot.getStack().copy();

		slot.putStack(held);

		return ret;
	}
}
