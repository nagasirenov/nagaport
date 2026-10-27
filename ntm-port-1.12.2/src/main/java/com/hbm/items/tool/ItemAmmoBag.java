package com.hbm.items.tool;

import com.hbm.inventory.container.ContainerAmmoBag;
import com.hbm.inventory.gui.GUIAmmoBag;
import com.hbm.items.ModItems;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.util.ItemStackUtil;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class ItemAmmoBag extends Item implements IGUIProvider {

	public ItemAmmoBag() {
		this.setMaxStackSize(1);
	}

	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		return 1;
	}

	@Override
	public net.minecraft.util.ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, net.minecraft.util.EnumHand nativeHand) {
		ItemStack stack = player.getHeldItem(nativeHand);
		return new net.minecraft.util.ActionResult<ItemStack>(net.minecraft.util.EnumActionResult.PASS, onItemRightClick(stack, world, player));
	}

	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		if(!world.isRemote) player.openGui(MainRegistry.instance, 0, world, 0, 0, 0);
		return stack;
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerAmmoBag(player.inventory, new InventoryAmmoBag(com.hbm.util.NativeHeldItem.getMainHandNullable(player)));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIAmmoBag(player.inventory, new InventoryAmmoBag(com.hbm.util.NativeHeldItem.getMainHandNullable(player)));
	}

	@Override
	public boolean showDurabilityBar(ItemStack stack) {
		if(this == ModItems.ammo_bag_infinite) return false;
		return !stack.hasTagCompound() || getDurabilityForDisplay(stack) != 0;
	}

	@Override
	public double getDurabilityForDisplay(ItemStack stack) {
		if(!stack.hasTagCompound()) return 1D;

		InventoryAmmoBag inv = new InventoryAmmoBag(stack);
		int capacity = 0;
		int bullets = 0;
		for(int i = 0; i < inv.getSizeInventory(); i++) {
			ItemStack slot = inv.getStackInSlot(i);
			if(slot == null) {
				capacity += 64;
			} else {
				capacity += slot.getMaxStackSize();
				bullets += slot.getCount();
			}
		}
		return 1D - (double) bullets / (double) capacity;
	}

	public static class InventoryAmmoBag implements IInventory {

		@Override
		public net.minecraft.util.text.ITextComponent getDisplayName() {
			return new net.minecraft.util.text.TextComponentString(this.getName());
		}

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

		public final ItemStack box;
		public ItemStack[] slots;

		public InventoryAmmoBag(ItemStack bag) {
			this.box = bag;
			slots = new ItemStack[this.getSizeInventory()];

			if(!bag.hasTagCompound())
				bag.setTagCompound(new NBTTagCompound());

			ItemStack[] fromNBT = ItemStackUtil.readStacksFromNBT(bag, slots.length);

			if(fromNBT != null) {
				for(int i = 0; i < slots.length; i++) {
					slots[i] = fromNBT[i];
				}
			}
		}

		@Override public int getSizeInventory() { return 8; }
		@Override public ItemStack getStackInSlot(int slot) { ItemStack stack = slots[slot]; return stack == null || stack.isEmpty() ? ItemStack.EMPTY : stack; }

		@Override
		public ItemStack decrStackSize(int slot, int amount) {
			ItemStack stack = getStackInSlot(slot);
			if (!stack.isEmpty()) {
				if (stack.getCount() > amount) {
					stack = stack.splitStack(amount);
					markDirty();
				} else {
					setInventorySlotContents(slot, ItemStack.EMPTY);
				}
			}
			return stack;
		}

		@Override
		public ItemStack removeStackFromSlot(int slot) {
			ItemStack stack = getStackInSlot(slot);
			setInventorySlotContents(slot, ItemStack.EMPTY);
			return stack == null || stack.isEmpty() ? ItemStack.EMPTY : stack;
		}

		@Override
		public void setInventorySlotContents(int slot, ItemStack stack) {
			stack = stack == null || stack.isEmpty() ? null : stack;

			if(stack != null) {
				stack.setCount(Math.min(stack.getCount(), this.getInventoryStackLimit()));
			}

			slots[slot] = stack;
			markDirty();
		}

		@Override public String getName() { return "container.ammoBag"; }
		@Override public boolean hasCustomName() { return box.hasDisplayName(); }
		public String getInventoryName() { return getName(); }
		public boolean hasCustomInventoryName() { return hasCustomName(); }
		@Override public int getInventoryStackLimit() { return 64; }

		@Override
		public void markDirty() {
			for(int i = 0; i < getSizeInventory(); ++i) {
				if(!getStackInSlot(i).isEmpty() && getStackInSlot(i).getCount() == 0) {
					slots[i] = null;
				}
			}

			ItemStackUtil.addStacksToNBT(box, slots);
		}

		@Override public boolean isUsableByPlayer(EntityPlayer player) { return true; }
		@Override public void openInventory(net.minecraft.entity.player.EntityPlayer viewer) { }
		@Override public void closeInventory(net.minecraft.entity.player.EntityPlayer viewer) { }
		@Override public boolean isItemValidForSlot(int slot, ItemStack stack) { return !stack.hasTagCompound(); }
	}
}
