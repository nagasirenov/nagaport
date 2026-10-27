package com.hbm.items;

import com.hbm.util.NullableInventoryStackNbt;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import java.io.IOException;
import java.util.Random;

/**
 * Base class for items containing an inventory. This can be seen in crates, containment boxes, and the toolbox.
 * @author BallOfEnergy/Gammawave
 */
public abstract class ItemInventory implements IInventory {

	public EntityPlayer player;
	public ItemStack[] slots;
	public ItemStack target;

	@Override
	public void markDirty() {

		for(int i = 0; i < getSizeInventory(); ++i) {
			if(getStackInSlotNullable(i) != null && quantity(getStackInSlotNullable(i)) == 0) {
				slots[i] = null;
			}
		}

		writeStacksToNBT(target, slots); // Maintain compatibility with the containment boxes.
		target.setTagCompound(checkNBT(target.getTagCompound()));

	}

	public NBTTagCompound checkNBT(NBTTagCompound nbt) {

		if(nbt == null || nbt.isEmpty())
			return null;

		Random random = new Random();

		try {
			java.io.ByteArrayOutputStream compressed = new java.io.ByteArrayOutputStream();
			CompressedStreamTools.writeCompressed(nbt, compressed);
			byte[] abyte = compressed.toByteArray();

			if (abyte.length > 6000) {
				player.sendMessage(new TextComponentString(TextFormatting.RED + "Warning: Container NBT exceeds 6kB, contents will be ejected!"));
				for (int i1 = 0; i1 < this.getSizeInventory(); ++i1) {
					ItemStack itemstack = this.getStackInSlotNullable(i1);

					if (itemstack != null) {
						float f = random.nextFloat() * 0.8F + 0.1F;
						float f1 = random.nextFloat() * 0.8F + 0.1F;
						float f2 = random.nextFloat() * 0.8F + 0.1F;

						while (quantity(itemstack) > 0) {
							int j1 = random.nextInt(21) + 10;

							if (j1 > quantity(itemstack)) {
								j1 = quantity(itemstack);
							}

							net.minecraft.item.Item item = itemstack.getItem();
							int damage = itemstack.getItemDamage();
							// Native empty masking must not replace a registered raw item with AIR.
							// Edge-only public snapshot; its capability serialization follows native rules.
							if(item == net.minecraft.init.Items.AIR) {
								NBTTagCompound identity = itemstack.writeToNBT(new NBTTagCompound());
								item = net.minecraft.item.Item.getByNameOrId(identity.getString("id"));
								damage = item.getDamage(itemstack);
							}
							NBTTagCompound tag = itemstack.getTagCompound();
							itemstack.shrink(j1);
							EntityItem entityitem = new EntityItem(player.world, player.posX + f, player.posY + f1, player.posZ + f2, new ItemStack(item, j1, damage));

							if (tag != null) {
								entityitem.getItem().setTagCompound(tag.copy());
							}

							float f3 = 0.05F;
							entityitem.motionX = (float) random.nextGaussian() * f3 + player.motionX;
							entityitem.motionY = (float) random.nextGaussian() * f3 + 0.2F + player.motionY;
							entityitem.motionZ = (float) random.nextGaussian() * f3 + player.motionZ;
							player.world.spawnEntity(entityitem);
						}
					}
				}

				return null; // Reset.
			}
		} catch (IOException ignored) {}

		return nbt;
	}

	@Override
	public ItemStack decrStackSize(int slot, int amount) {
		ItemStack stack = getStackInSlotNullable(slot);
		if(stack != null) {
			if (quantity(stack) > amount) {
				stack = stack.splitStack(amount);
				markDirty();
			} else {
				setInventorySlotContents(slot, ItemStack.EMPTY);
			}
		}
		return stack == null ? ItemStack.EMPTY : stack;
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack stack) {
		stack = (stack == null || stack.isEmpty()) ? null : stack;
		if(stack != null) {
			stack.setCount(Math.min(quantity(stack), this.getInventoryStackLimit()));
		}

		slots[slot] = stack;
		markDirty();
	}

	@Override
	public ItemStack removeStackFromSlot(int slot) {
		ItemStack stack = getStackInSlotNullable(slot);
		setInventorySlotContents(slot, ItemStack.EMPTY);
		return stack == null ? ItemStack.EMPTY : stack;
	}

	public ItemStack getStackInSlotNullable(int slot) { return slots[slot]; }
	@Override public ItemStack getStackInSlot(int slot) { ItemStack stack = getStackInSlotNullable(slot); return stack == null ? ItemStack.EMPTY : stack; }

	@Override public boolean isUsableByPlayer(EntityPlayer player) { return true; }
	@Override public boolean isItemValidForSlot(int slot, ItemStack stack) { return true; }
	@Override public int getInventoryStackLimit() { return 64; }

	@Override public void openInventory(net.minecraft.entity.player.EntityPlayer viewer) { com.hbm.sound.WorldSoundEffects.play(player.world, player.posX, player.posY, player.posZ, "hbm:block.crateOpen", 1.0F, 0.8F); }
	@Override public void closeInventory(net.minecraft.entity.player.EntityPlayer viewer) { com.hbm.sound.WorldSoundEffects.play(player.world, player.posX, player.posY, player.posZ, "hbm:block.crateClose", 1.0F, 0.8F); }
	/** Native boundary: internal vacancy remains null; non-null raw stacks retain identity. */
	@Override public boolean isEmpty() {
		for(ItemStack stack : slots) if(stack != null && !stack.isEmpty()) return false;
		return true;
	}
	@Override public void clear() { java.util.Arrays.fill(slots, null); markDirty(); }
	@Override public int getField(int id) { return 0; }
	@Override public void setField(int id, int value) { }
	@Override public int getFieldCount() { return 0; }
	@Override public net.minecraft.util.text.ITextComponent getDisplayName() {
		return hasCustomName() ? new TextComponentString(target.getDisplayName()) : new net.minecraft.util.text.TextComponentTranslation(getName());
	}

	/** Raw count for the original signed clamp and exactly-zero cleanup. */
	protected static int quantity(ItemStack stack) {
		return stack == null ? 0 : stack.getCount();
	}

	/** Original nullable item-inventory list schema, deliberately local to this domain. */
	protected static void writeStacksToNBT(ItemStack owner, ItemStack[] stacks) {
		if(owner.getTagCompound() == null) owner.setTagCompound(new NBTTagCompound());
		net.minecraft.nbt.NBTTagList tags = new net.minecraft.nbt.NBTTagList();
		for(int i = 0; i < stacks.length; i++) {
			if(stacks[i] != null) {
				NBTTagCompound slot = new NBTTagCompound();
				slot.setByte("slot", (byte) i);
				stacks[i].writeToNBT(slot);
				tags.appendTag(slot);
			}
		}
		owner.getTagCompound().setTag("items", tags);
	}
	protected static ItemStack[] readStacksFromNBT(ItemStack owner, int count) {
		if(owner.getTagCompound() == null) return null;
		net.minecraft.nbt.NBTTagList tags = owner.getTagCompound().getTagList("items", 10);
		if(count == 0) count = tags.tagCount();
		ItemStack[] stacks = new ItemStack[count];
		for(int i = 0; i < count; i++) {
			NBTTagCompound slot = tags.getCompoundTagAt(i);
			byte index = slot.getByte("slot");
			ItemStack loaded = NullableInventoryStackNbt.read(slot);
			if(index >= 0 && index < stacks.length && loaded != null) stacks[index] = loaded;
		}
		return stacks;
	}

}
