package com.hbm.items.tool;

import com.hbm.inventory.container.ContainerCasingBag;
import com.hbm.inventory.gui.GUICasingBag;
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

public class ItemCasingBag extends Item implements IGUIProvider {

	public ItemCasingBag() {
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
		return new ContainerCasingBag(player.inventory, new InventoryCasingBag(com.hbm.util.NativeHeldItem.getMainHandNullable(player)));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUICasingBag(player.inventory, new InventoryCasingBag(com.hbm.util.NativeHeldItem.getMainHandNullable(player)));
	}

	/**
	 * Returns true if ammo was able to be added
	 * @param bag
	 * @param casing
	 * @param amount
	 * @return
	 */
	public static boolean pushCasing(ItemStack bag, ItemStack casing, float amount) {
		if(!bag.hasTagCompound()) bag.setTagCompound(new NBTTagCompound());
		String name = casing.getTranslationKey() + "@" + casing.getItemDamage();
		boolean ret = false;
		//only add if the previous number did not exceed 1 (i.e. the bag ran full, and may have been emptied, we don't know)
		//this may also cause minor loss, which evens out the dupe mentioned below. not that it matters, casings only have a 50% recovery rate!
		if(bag.getTagCompound().getFloat(name) < 1) {
			ret = true;
			bag.getTagCompound().setFloat(name, bag.getTagCompound().getFloat(name) + amount);
		}
		if(bag.getTagCompound().getFloat(name) >= 1) {
			InventoryCasingBag inv = new InventoryCasingBag(bag);
			ItemStack toAdd = casing.copy();
			while(bag.getTagCompound().getFloat(name) >= 1) {

				boolean didSomething = false;

				for(int i = 0; i < inv.getSizeInventory(); i++) {
					if(toAdd.getCount() <= 0) break;
					ItemStack slot = inv.getStackInSlot(i);
					if(slot != null && slot.getItem() == toAdd.getItem() && slot.getItemDamage() == toAdd.getItemDamage()) {
						int am = Math.min(toAdd.getCount(), slot.getMaxStackSize() - slot.getCount());
						toAdd.shrink(am);
						slot.grow(am);
						if(am > 0) didSomething = true;
					}
				}

				for(int i = 0; i < inv.getSizeInventory(); i++) {
					if(toAdd.getCount() <= 0) break;
					ItemStack slot = inv.getStackInSlot(i);
					if(slot == null) {
						inv.setInventorySlotContents(i, toAdd);
						didSomething = true;
						break;
					}
				}

				if(didSomething) {
					bag.getTagCompound().setFloat(name, bag.getTagCompound().getFloat(name) - 1F);
					ret = true;
				} else {
					break;
				}
			}
			inv.markDirty();
		}
		return ret;
	}

	public static class InventoryCasingBag implements IInventory {

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

		public InventoryCasingBag(ItemStack box) {
			this.box = box;
			slots = new ItemStack[this.getSizeInventory()];

			if(!box.hasTagCompound())
				box.setTagCompound(new NBTTagCompound());

			ItemStack[] fromNBT = ItemStackUtil.readStacksFromNBT(box, slots.length);

			if(fromNBT != null) {
				for(int i = 0; i < slots.length; i++) {
					slots[i] = fromNBT[i];
				}
			}
		}

		@Override public int getSizeInventory() { return 15; }
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

		@Override public String getName() { return "container.casingBag"; }
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
		@Override public boolean isItemValidForSlot(int slot, ItemStack stack) { return false; }
	}
}
