package com.hbm.inventory.container;

import com.hbm.handler.ArmorModHandler;
import com.hbm.items.armor.ItemArmorMod;
import com.hbm.util.InventoryUtil;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import com.hbm.render.icon.IIcon;

public class ContainerArmorTable extends Container {

	public InventoryBasic upgrades = new InventoryBasic("Upgrades", false, ArmorModHandler.MOD_SLOTS);
	public IInventory armor = new InventoryCraftResult();

	public ContainerArmorTable(InventoryPlayer inventory) {
		EntityPlayer player = inventory.player;

		this.addSlotToContainer(new UpgradeSlot(upgrades, ArmorModHandler.helmet_only, 26 + 22, 27));	// helmet only
		this.addSlotToContainer(new UpgradeSlot(upgrades, ArmorModHandler.plate_only, 62 + 22, 27));	// chestplate only
		this.addSlotToContainer(new UpgradeSlot(upgrades, ArmorModHandler.legs_only, 98 + 22, 27));		// leggins only
		this.addSlotToContainer(new UpgradeSlot(upgrades, ArmorModHandler.boots_only, 134 + 22, 45));	// boots only
		this.addSlotToContainer(new UpgradeSlot(upgrades, ArmorModHandler.servos, 134 + 22, 81));		//servos/frame
		this.addSlotToContainer(new UpgradeSlot(upgrades, ArmorModHandler.cladding, 98 + 22, 99));		//radiation cladding
		this.addSlotToContainer(new UpgradeSlot(upgrades, ArmorModHandler.kevlar, 62 + 22, 99));		//kevlar/sapi/(ERA? :) )
		this.addSlotToContainer(new UpgradeSlot(upgrades, ArmorModHandler.extra, 26 + 22, 99));			//special parts
		this.addSlotToContainer(new UpgradeSlot(upgrades, ArmorModHandler.battery, 8 + 22, 63));		//special parts

		this.addSlotToContainer(new Slot(armor, 0, 44 + 22, 63) {

			@Override
			public boolean isItemValid(ItemStack stack) {
				return stack != null && !stack.isEmpty() && stack.getItem() instanceof ItemArmor;
			}

			@Override
			public void putStack(ItemStack stack) {

				if(stack != null && !stack.isEmpty()) {
					ItemStack[] mods = ArmorModHandler.pryMods(stack);

					for(int i = 0; i < ArmorModHandler.MOD_SLOTS; i++) {
						ItemStack mod = (mods != null && i < mods.length && mods[i] != null && !mods[i].isEmpty())
								? mods[i]
								: ItemStack.EMPTY;
						upgrades.setInventorySlotContents(i, mod);
					}
				} else {
					for(int i = 0; i < ArmorModHandler.MOD_SLOTS; i++) {
						upgrades.setInventorySlotContents(i, ItemStack.EMPTY);
					}
				}

				super.putStack(stack != null ? stack : ItemStack.EMPTY);
			}

			@Override
			public ItemStack onTake(EntityPlayer player, ItemStack stack) {
				super.onTake(player, stack);

				// when the armor piece is taken, clear all upgrade display slots
				for(int i = 0; i < ArmorModHandler.MOD_SLOTS; i++) {
					upgrades.setInventorySlotContents(i, ItemStack.EMPTY);
				}
				return stack;
			}
		});

		//player armor slots for easy accessibility
		for(int i = 0; i < 4; ++i) {
			final int k = i;
			this.addSlotToContainer(new Slot(inventory, inventory.mainInventory.size() + 3 - i, -17 + 22, 36 + i * 18) {

				public int getSlotStackLimit() {
					return 1;
				}

				public boolean isItemValid(ItemStack stack) {
					if(stack == null)
						return false;
					return stack.getItem().isValidArmor(stack, com.hbm.util.ArmorSlotOrder.fromIndex(k), player);
				}

				@SideOnly(Side.CLIENT)
				public String getSlotTexture() {
					return ItemArmor.EMPTY_SLOT_NAMES[k];
				}
			});
		}

		for(int i = 0; i < 3; i++) {
			for(int j = 0; j < 9; j++) {
				this.addSlotToContainer(new Slot(inventory, j + i * 9 + 9, 8 + j * 18 + 22, 84 + i * 18 + 56));
			}
		}

		for(int i = 0; i < 9; i++) {
			this.addSlotToContainer(new Slot(inventory, i, 8 + i * 18 + 22, 142 + 56));
		}

		this.onCraftMatrixChanged(this.upgrades);
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return true;
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer p_82846_1_, int par2) {
		ItemStack var3 = ItemStack.EMPTY;
		Slot var4 = (Slot) this.inventorySlots.get(par2);

		if(var4 != null && var4.getHasStack()) {
			ItemStack var5 = var4.getStack();
			var3 = var5.copy();

			if(par2 <= ArmorModHandler.MOD_SLOTS) {
				if(par2 != ArmorModHandler.MOD_SLOTS || !InventoryUtil.mergeItemStack(this.inventorySlots, var5, ArmorModHandler.MOD_SLOTS + 1, ArmorModHandler.MOD_SLOTS + 5, false))
					if(!this.mergeItemStack(var5, ArmorModHandler.MOD_SLOTS + 5, this.inventorySlots.size(), true))
						return ItemStack.EMPTY;

				var4.onTake(p_82846_1_, var5);
			} else {

				if(var5.getItem() instanceof ItemArmor) {
					if(!this.mergeItemStack(var5, ArmorModHandler.MOD_SLOTS, ArmorModHandler.MOD_SLOTS + 1, false))
						return ItemStack.EMPTY;

				} else if(this.inventorySlots.get(ArmorModHandler.MOD_SLOTS) != null && var5.getItem() instanceof ItemArmorMod) {

					ItemArmorMod mod = (ItemArmorMod)var5.getItem();
					int slot = mod.type;

					if(((Slot) this.inventorySlots.get(slot)).isItemValid(var5)) {
						if(!this.mergeItemStack(var5, slot, slot + 1, false))
							return ItemStack.EMPTY;
					} else {
						return ItemStack.EMPTY;
					}
				} else {
					return ItemStack.EMPTY;
				}
			}

			if(var5.getCount() == 0) {
				var4.putStack(ItemStack.EMPTY);
			} else {
				var4.onSlotChanged();
			}
		}

		return var3;
	}

	@Override
	public void onContainerClosed(EntityPlayer player) {
		super.onContainerClosed(player);

		if(!player.world.isRemote) {
			ItemStack armorStack = this.armor.removeStackFromSlot(0);

			if(armorStack != null && !armorStack.isEmpty()) {
				// The armor piece holds all installed modifications.
				// Clear upgrade slots without dropping them or stripping mods from armor.
				for(int i = 0; i < this.upgrades.getSizeInventory(); ++i) {
					this.upgrades.setInventorySlotContents(i, ItemStack.EMPTY);
				}
				player.dropItem(armorStack, false);
			} else {
				// If somehow there are orphan items in upgrades with no armor present, drop them
				for(int i = 0; i < this.upgrades.getSizeInventory(); ++i) {
					ItemStack itemstack = this.upgrades.removeStackFromSlot(i);

					if(itemstack != null && !itemstack.isEmpty()) {
						player.dropItem(itemstack, false);
					}
				}
			}
		}
	}

	public class UpgradeSlot extends Slot {

		public UpgradeSlot(IInventory inventory, int index, int x, int y) {
			super(inventory, index, x, y);
		}

		@Override
		public boolean isItemValid(ItemStack stack) {
			if(stack == null || stack.isEmpty() || !(stack.getItem() instanceof ItemArmorMod))
				return false;
			ItemStack targetArmor = armor.getStackInSlot(0);
			return !targetArmor.isEmpty() && ArmorModHandler.isApplicable(targetArmor, stack) && ((ItemArmorMod)stack.getItem()).type == this.slotNumber;
		}

		@Override
		public void putStack(ItemStack stack) {
			super.putStack(stack != null ? stack : ItemStack.EMPTY);

			ItemStack targetArmor = armor.getStackInSlot(0);
			if(!targetArmor.isEmpty()) {
				if(stack != null && !stack.isEmpty()) {
					if(ArmorModHandler.isApplicable(targetArmor, stack)) {
						ArmorModHandler.applyMod(targetArmor, stack);
					}
				} else {
					ArmorModHandler.removeMod(targetArmor, this.slotNumber);
				}
			}
		}

		@Override
		public ItemStack onTake(EntityPlayer player, ItemStack stack) {
			super.onTake(player, stack);

			ItemStack armorStack = armor.getStackInSlot(0);
			if(armorStack != null && !armorStack.isEmpty()) {
				ArmorModHandler.removeMod(armorStack, this.slotNumber);
			}
			return stack;
		}
	}
}
