package com.hbm.items;

import java.util.List;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/** The original box transfer algorithm, restricted to item-inventory containers.
 * Domain vacancies are null, not every engine-empty stack. This deliberately does
 * not change InventoryUtil or the native Container ordinary-click algorithm. */
public final class ItemInventoryTransfer {
	private ItemInventoryTransfer() { }

	/** Same read-only mapped quantity convention as the item-inventory owner. */
	public static int quantity(ItemStack stack) { return ItemInventory.quantity(stack); }

	private static ItemStack current(Slot slot) {
		// Preserve Slot's virtual read and its exact noncanonical stack reference.
		ItemStack stack = slot.getStack();
		return stack == ItemStack.EMPTY ? null : stack;
	}

	private static Item rawItem(ItemStack stack) {
		Item item = stack.getItem();
		if(item == net.minecraft.init.Items.AIR) {
			// Same edge-only public registered identity snapshot as ItemInventory.checkNBT.
			// Native capability serialization and registry replacement rules still apply.
			item = Item.getByNameOrId(stack.writeToNBT(new NBTTagCompound()).getString("id"));
		}
		return item;
	}

	private static ItemStack copyWithSize(ItemStack stack, int size) {
		ItemStack copy = stack.copy();
		copy.setCount(size);
		return copy;
	}

	private static boolean tagsEqual(ItemStack left, ItemStack right) {
		NBTTagCompound tag = left.getTagCompound();
		return tag == null ? right.getTagCompound() == null : tag.equals(right.getTagCompound());
	}

	public static boolean mergeItemStack(List<Slot> slots, ItemStack stack, int start, int end, boolean reverse) {
		boolean success = false;
		int index = start;
		if(reverse) index = end - 1;
		Slot slot;
		ItemStack current;

		if(stack.isStackable()) {
			while(quantity(stack) > 0 && (!reverse && index < end || reverse && index >= start)) {
				slot = slots.get(index);
				current = current(slot);
				if(current != null) {
					int max = Math.min(stack.getMaxStackSize(), slot.getSlotStackLimit());
					int toRemove = Math.min(quantity(stack), max);
					if(slot.isItemValid(copyWithSize(stack, toRemove)) && rawItem(current) == stack.getItem() &&
							(!stack.getHasSubtypes() || stack.getItemDamage() == rawItem(current).getDamage(current)) && tagsEqual(stack, current)) {
						int currentSize = quantity(current) + quantity(stack);
						if(currentSize <= max) {
							stack.setCount(0);
							current.setCount(currentSize);
							slot.putStack(current);
							success = true;
						} else if(quantity(current) < max) {
							stack.shrink(max - quantity(current));
							current.setCount(max);
							slot.putStack(current);
							success = true;
						}
					}
				}
				if(reverse) --index; else ++index;
			}
		}

		if(quantity(stack) > 0) {
			index = reverse ? end - 1 : start;
			while((!reverse && index < end || reverse && index >= start) && quantity(stack) > 0) {
				slot = slots.get(index);
				current = current(slot);
				if(current == null) {
					int max = Math.min(stack.getMaxStackSize(), slot.getSlotStackLimit());
					int toRemove = Math.min(quantity(stack), max);
					if(slot.isItemValid(copyWithSize(stack, toRemove))) {
						current = stack.splitStack(toRemove);
						slot.putStack(current);
						success = true;
					}
				}
				if(reverse) --index; else ++index;
			}
		}
		return success;
	}
}
