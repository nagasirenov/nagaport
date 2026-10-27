package com.hbm.inventory.container;

import com.hbm.inventory.SlotPattern;
import com.hbm.tileentity.network.TileEntityRadioTorchCounter;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;

public class ContainerCounterTorch extends ContainerBase {

	protected TileEntityRadioTorchCounter radio;

	public ContainerCounterTorch(InventoryPlayer invPlayer, TileEntityRadioTorchCounter radio) {
		super(invPlayer, radio);
		this.radio = radio;

		for(int i = 0; i < 3; i++) {
			this.addSlotToContainer(new SlotPattern(radio, i, 138, 18 + 44 * i));
		}
		playerInv(invPlayer, 12, 156, 214);
	}

	@Override public ItemStack transferStackInSlot(EntityPlayer player, int slot) { return ItemStack.EMPTY; }

	@Override
	public ItemStack slotClick(int index, int button, ClickType mode, EntityPlayer player) {

		//L/R: 0
		//M3: 3
		//SHIFT: 1
		//DRAG: 5
		if(index < 0 || index > 2) {
			return super.slotClick(index, button, mode, player);
		}

		Slot slot = this.getSlot(index);

		ItemStack ret = ItemStack.EMPTY;
		ItemStack held = player.inventory.getItemStack();

		if(slot.getHasStack())
			ret = slot.getStack().copy();

		if(button == 1 && mode == ClickType.PICKUP && slot.getHasStack()) {
			radio.matcher.nextMode(radio.getWorld(), slot.getStack(), index);
			return ret;

		} else {
			slot.putStack(held);
			radio.matcher.initPatternStandard(radio.getWorld(), slot.getStack(), index);

			return ret;
		}
	}
}
