package com.hbm.inventory.container;

import java.util.Iterator;
import java.util.HashSet;
import java.util.Set;

import com.hbm.util.InventoryUtil;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public abstract class ContainerNT extends Container {

	// Container's native drag fields are private; this custom click algorithm owns its state.
	private int dragMode;
	private int dragEvent;
	private final Set<Slot> dragSlots = new HashSet<>();

	private static ItemStack nativeStack(ItemStack stack) {
		return stack == null || stack.isEmpty() ? ItemStack.EMPTY : stack;
	}

	private void resetCustomDrag() {
		this.dragEvent = 0;
		this.dragSlots.clear();
	}

	/**
	 * Fix: mergeItemStack does not respect inventory or slot stack limitations.
	 * We simply intercept the method and call InventoryUtil.mergeItemStack which
	 * runs the same logic but respecting stack limits.
	 */
	@Override
	protected boolean mergeItemStack(ItemStack stack, int start, int end, boolean reverse) {
		return InventoryUtil.mergeItemStack(inventorySlots, stack, start, end, reverse);
	}

	/**
	 * Fix: the default behavior of transferStackInSlot simply crashes the game.
	 * We intercept this part and return EMPTY, which means that by default, the
	 * shift click function does nothing.
	 */
	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int index) {
		return ItemStack.EMPTY;
	}
	
	// fuck you mojang
	@Override
	public ItemStack slotClick(int index, int button, ClickType clickType, EntityPlayer player) {
		
		/* Legacy modes map explicitly, not by enum ordinal:
		 * 0 PICKUP, 1 QUICK_MOVE, 2 SWAP, 3 CLONE, 4 THROW,
		 * 5 QUICK_CRAFT, 6 PICKUP_ALL. -999 is outside the GUI.
		 * Native 1.12.2 SWAP accepts hotbar 0..8 only, not offhand 40.
		 */

		ItemStack returnStack = ItemStack.EMPTY;
		InventoryPlayer invPlayer = player.inventory;
		Slot slot = index >= 0 && index < this.inventorySlots.size() ? (Slot) this.inventorySlots.get(index) : null;
		
		int i1;
		ItemStack itemstack3;

		/// DRAG ///
		if(clickType == ClickType.QUICK_CRAFT) {
			int l = this.dragEvent;
			this.dragEvent = getDragEvent(button);

			if((l != 1 || this.dragEvent != 2) && l != this.dragEvent) {
				this.resetCustomDrag();
			} else if(nativeStack(invPlayer.getItemStack()).isEmpty()) {
				this.resetCustomDrag();
			} else if(this.dragEvent == 0) {
				this.dragMode = extractDragMode(button);

				// Original 1.7.10 func_94528_d accepts only modes 0 and 1, even in creative.
				if(this.dragMode == 0 || this.dragMode == 1) {
					this.dragEvent = 1;
					this.dragSlots.clear();
				} else {
					this.resetCustomDrag();
				}
			} else if(this.dragEvent == 1) {

				if(slot != null && canAddItemToSlot(slot, nativeStack(invPlayer.getItemStack()), true) && slot.isItemValid(nativeStack(invPlayer.getItemStack()))
						&& nativeStack(invPlayer.getItemStack()).getCount() > this.dragSlots.size() && this.canDragIntoSlot(slot)) {
					this.dragSlots.add(slot);
				}
			} else if(this.dragEvent == 2) {
				if(!this.dragSlots.isEmpty()) {
					itemstack3 = nativeStack(invPlayer.getItemStack()).copy();
					i1 = nativeStack(invPlayer.getItemStack()).getCount();
					Iterator iterator = this.dragSlots.iterator();

					while(iterator.hasNext()) {
						Slot slot1 = (Slot) iterator.next();

						if(slot1 != null && canAddItemToSlot(slot1, nativeStack(invPlayer.getItemStack()), true) && slot1.isItemValid(nativeStack(invPlayer.getItemStack()))
								&& nativeStack(invPlayer.getItemStack()).getCount() >= this.dragSlots.size() && this.canDragIntoSlot(slot1)) {
							ItemStack stackCopy = itemstack3.copy();
							int j1 = slot1.getHasStack() ? nativeStack(slot1.getStack()).getCount() : 0;
							computeStackSize(this.dragSlots, this.dragMode, stackCopy, j1);

							if(stackCopy.getCount() > stackCopy.getMaxStackSize()) {
								stackCopy.setCount(stackCopy.getMaxStackSize());
							}

							if(stackCopy.getCount() > slot1.getSlotStackLimit()) {
								stackCopy.setCount(slot1.getSlotStackLimit());
							}

							i1 -= stackCopy.getCount() - j1;
							slot1.putStack(nativeStack(stackCopy));
						}
					}

					itemstack3.setCount(i1);

					if(itemstack3.getCount() <= 0) {
						itemstack3 = ItemStack.EMPTY;
					}

					invPlayer.setItemStack(itemstack3);
				}

				this.resetCustomDrag();
			} else {
				this.resetCustomDrag();
			}
		/// NOBODY KNOWS ///
		} else if(this.dragEvent != 0) {
			this.resetCustomDrag();
			
		/// NON-DRAG ///
		} else {
			
			int l1;
			ItemStack itemstack5;

			/// LMB/RMB ///
			if((clickType == ClickType.PICKUP || clickType == ClickType.QUICK_MOVE) && (button == 0 || button == 1)) {
				if(index == -999) {
					if(!nativeStack(invPlayer.getItemStack()).isEmpty() && index == -999) {
						if(button == 0) {
							player.dropItem(nativeStack(invPlayer.getItemStack()), true);
							invPlayer.setItemStack(ItemStack.EMPTY);
						}

						if(button == 1) {
							player.dropItem(nativeStack(invPlayer.getItemStack()).splitStack(1), true);

							if(nativeStack(invPlayer.getItemStack()).getCount() == 0) {
								invPlayer.setItemStack(ItemStack.EMPTY);
							}
						}
					}
				} else if(clickType == ClickType.QUICK_MOVE) {
					if(index < 0) {
						return ItemStack.EMPTY;
					}

					slot = (Slot) this.inventorySlots.get(index);

					if(slot != null && slot.canTakeStack(player)) {
						itemstack3 = nativeStack(this.transferStackInSlot(player, index));

						if(!itemstack3.isEmpty()) {
							Item item = itemstack3.getItem();
							returnStack = itemstack3.copy();

							if(!nativeStack(slot.getStack()).isEmpty() && nativeStack(slot.getStack()).getItem() == item) {
								this.slotClick(index, button, ClickType.QUICK_MOVE, player);
							}
						}
					}
				} else {
					if(index < 0) {
						return ItemStack.EMPTY;
					}

					slot = (Slot) this.inventorySlots.get(index);

					if(slot != null) {
						itemstack3 = nativeStack(slot.getStack());
						ItemStack draggedStack = nativeStack(invPlayer.getItemStack());

						if(!itemstack3.isEmpty()) {
							returnStack = itemstack3.copy();
						}

						if(itemstack3.isEmpty()) {
							if(!draggedStack.isEmpty() && slot.isItemValid(draggedStack)) {
								l1 = button == 0 ? draggedStack.getCount() : 1;

								if(l1 > slot.getSlotStackLimit()) {
									l1 = slot.getSlotStackLimit();
								}

								if(draggedStack.getCount() >= l1) {
									slot.putStack(nativeStack(draggedStack.splitStack(l1)));
								}

								if(draggedStack.getCount() == 0) {
									invPlayer.setItemStack(ItemStack.EMPTY);
								}
							}
						} else if(slot.canTakeStack(player)) {
							if(draggedStack.isEmpty()) {
								l1 = button == 0 ? itemstack3.getCount() : (itemstack3.getCount() + 1) / 2;
								itemstack5 = nativeStack(slot.decrStackSize(l1));
								invPlayer.setItemStack(itemstack5);

								if(itemstack3.getCount() == 0) {
									slot.putStack(ItemStack.EMPTY);
								}

								slot.onTake(player, nativeStack(invPlayer.getItemStack()));
							} else if(slot.isItemValid(draggedStack)) {
								if(itemstack3.getItem() == draggedStack.getItem() && itemstack3.getItemDamage() == draggedStack.getItemDamage()
										&& ItemStack.areItemStackTagsEqual(itemstack3, draggedStack)) {
									l1 = button == 0 ? draggedStack.getCount() : 1;

									if(l1 > slot.getSlotStackLimit() - itemstack3.getCount()) {
										l1 = slot.getSlotStackLimit() - itemstack3.getCount();
									}

									if(l1 > draggedStack.getMaxStackSize() - itemstack3.getCount()) {
										l1 = draggedStack.getMaxStackSize() - itemstack3.getCount();
									}

									draggedStack.splitStack(l1);

									if(draggedStack.getCount() == 0) {
										invPlayer.setItemStack(ItemStack.EMPTY);
									}

									itemstack3.grow(l1);
								} else if(draggedStack.getCount() <= slot.getSlotStackLimit()) {
									slot.putStack(draggedStack);
									invPlayer.setItemStack(itemstack3);
								}
							} else if(itemstack3.getItem() == draggedStack.getItem() && draggedStack.getMaxStackSize() > 1
									&& (!itemstack3.getHasSubtypes() || itemstack3.getItemDamage() == draggedStack.getItemDamage()) && ItemStack.areItemStackTagsEqual(itemstack3, draggedStack)) {
								l1 = itemstack3.getCount();

								if(l1 > 0 && l1 + draggedStack.getCount() <= draggedStack.getMaxStackSize()) {
									draggedStack.grow(l1);
									itemstack3 = nativeStack(slot.decrStackSize(l1));

									if(itemstack3.getCount() == 0) {
										slot.putStack(ItemStack.EMPTY);
									}

									slot.onTake(player, nativeStack(invPlayer.getItemStack()));
								}
							}
						}

						slot.onSlotChanged();
					}
				}
			} else if(clickType == ClickType.SWAP && button >= 0 && button < 9) {
				slot = (Slot) this.inventorySlots.get(index);

				if(slot.canTakeStack(player)) {
					itemstack3 = nativeStack(invPlayer.getStackInSlot(button));
					boolean flag = itemstack3.isEmpty() || slot.inventory == invPlayer && slot.isItemValid(itemstack3);
					l1 = -1;

					if(!flag) {
						l1 = invPlayer.getFirstEmptyStack();
						flag |= l1 > -1;
					}

					if(slot.getHasStack() && flag) {
						itemstack5 = nativeStack(slot.getStack());
						invPlayer.setInventorySlotContents(button, itemstack5.copy());

						if((slot.inventory != invPlayer || !slot.isItemValid(itemstack3)) && !itemstack3.isEmpty()) {
							if(l1 > -1) {
								invPlayer.addItemStackToInventory(itemstack3);
								slot.decrStackSize(itemstack5.getCount());
								slot.putStack(ItemStack.EMPTY);
								slot.onTake(player, itemstack5);
							}
						} else {
							slot.decrStackSize(itemstack5.getCount());
							slot.putStack(itemstack3);
							slot.onTake(player, itemstack5);
						}
					} else if(!slot.getHasStack() && !itemstack3.isEmpty() && slot.isItemValid(itemstack3)) {
						invPlayer.setInventorySlotContents(button, ItemStack.EMPTY);
						slot.putStack(itemstack3);
					}
				}
			} else if(clickType == ClickType.CLONE && player.capabilities.isCreativeMode && nativeStack(invPlayer.getItemStack()).isEmpty() && index >= 0) {
				slot = (Slot) this.inventorySlots.get(index);

				if(slot != null && slot.getHasStack()) {
					itemstack3 = nativeStack(slot.getStack()).copy();
					itemstack3.setCount(itemstack3.getMaxStackSize());
					invPlayer.setItemStack(itemstack3);
				}
			} else if(clickType == ClickType.THROW && nativeStack(invPlayer.getItemStack()).isEmpty() && index >= 0) {
				slot = (Slot) this.inventorySlots.get(index);

				if(slot != null && slot.getHasStack() && slot.canTakeStack(player)) {
					itemstack3 = nativeStack(slot.decrStackSize(button == 0 ? 1 : nativeStack(slot.getStack()).getCount()));
					slot.onTake(player, itemstack3);
					player.dropItem(itemstack3, true);
				}
			} else if(clickType == ClickType.PICKUP_ALL && index >= 0) {
				slot = (Slot) this.inventorySlots.get(index);
				itemstack3 = nativeStack(invPlayer.getItemStack());

				if(!itemstack3.isEmpty() && (slot == null || !slot.getHasStack() || !slot.canTakeStack(player))) {
					i1 = button == 0 ? 0 : this.inventorySlots.size() - 1;
					l1 = button == 0 ? 1 : -1;

					for(int i2 = 0; i2 < 2; ++i2) {
						for(int j2 = i1; j2 >= 0 && j2 < this.inventorySlots.size() && itemstack3.getCount() < itemstack3.getMaxStackSize(); j2 += l1) {
							Slot slot3 = (Slot) this.inventorySlots.get(j2);

							if(slot3.getHasStack() && canAddItemToSlot(slot3, itemstack3, true) && slot3.canTakeStack(player) && this.canMergeSlot(itemstack3, slot3)
									&& (i2 != 0 || nativeStack(slot3.getStack()).getCount() != nativeStack(slot3.getStack()).getMaxStackSize())) {
								int k1 = Math.min(itemstack3.getMaxStackSize() - itemstack3.getCount(), nativeStack(slot3.getStack()).getCount());
								ItemStack itemstack2 = nativeStack(slot3.decrStackSize(k1));
								itemstack3.grow(k1);

								if(itemstack2.getCount() <= 0) {
									slot3.putStack(ItemStack.EMPTY);
								}

								slot3.onTake(player, itemstack2);
							}
						}
					}
				}

				this.detectAndSendChanges();
			}
		}

		return returnStack;
	}
}
