package com.hbm.inventory.container;

import com.hbm.inventory.SlotPattern;
import com.hbm.tileentity.network.TileEntityDroneRequester;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;

public class ContainerDroneRequester extends ContainerCrateBase {

	public ContainerDroneRequester(InventoryPlayer invPlayer, TileEntityDroneRequester tedf) {
		super(invPlayer,tedf);

		for(int i = 0; i < 3; i++) {
			for(int j = 0; j < 3; j++) {
				this.addSlotToContainer(new SlotPattern(tedf, j + i * 3, 98 + j * 18, 17 + i * 18));
			}
		}

		for(int i = 0; i < 3; i++) {
			for(int j = 0; j < 3; j++) {
				this.addSlotToContainer(new Slot(tedf, j + i * 3 + 9, 26 + j * 18, 17 + i * 18));
			}
		}

		this.playerInv(invPlayer, 8, 103, 161);
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer p_82846_1_, int par2) {
		ItemStack var3 = ItemStack.EMPTY;
		Slot var4 = (Slot) this.inventorySlots.get(par2);

		if(var4 != null && var4.getHasStack()) {
			ItemStack var5 = var4.getStack();
			var3 = var5.copy();

			if(par2 < 9) return ItemStack.EMPTY; //ignore filters

			if(par2 <= tile.getSizeInventory() - 1) {
				if(!this.mergeItemStack(var5, tile.getSizeInventory(), this.inventorySlots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else if(!this.mergeItemStack(var5, 9, tile.getSizeInventory(), false)) {
				return ItemStack.EMPTY;
			}

			if(var5.getCount() == 0) {
				var4.putStack(ItemStack.EMPTY);
			} else {
				var4.onSlotChanged();
			}

			var4.onTake(p_82846_1_, var5);
		}

		return var3;
	}

	@Override
	public ItemStack slotClick(int index, int button, ClickType mode, EntityPlayer player) {

		//L/R: 0
		//M3: 3
		//SHIFT: 1
		//DRAG: 5

		if(index < 0 || index > 8) {
			return super.slotClick(index, button, mode, player);
		}

		Slot slot = this.getSlot(index);

		ItemStack ret = ItemStack.EMPTY;
		ItemStack held = player.inventory.getItemStack();
		TileEntityDroneRequester requester = (TileEntityDroneRequester) tile;

		if(slot.getHasStack())
			ret = slot.getStack().copy();

		if(button == 1 && mode == ClickType.PICKUP && slot.getHasStack()) {
			requester.nextMode(index);
			return ret;

		} else {
			slot.putStack(held);
			requester.matcher.initPatternStandard(requester.getWorld(), slot.getStack(), index);

			return ret;
		}
	}
}
