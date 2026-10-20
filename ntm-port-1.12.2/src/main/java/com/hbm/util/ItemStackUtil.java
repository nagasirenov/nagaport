package com.hbm.util;

import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ItemStackUtil {

	public static ItemStack carefulCopy(ItemStack stack) {
		if(stack == null) return null;
		return stack.copy();
	}

	public static ItemStack carefulCopyWithSize(ItemStack stack, int size) {
		if(stack == null)
			return null;

		ItemStack copy = stack.copy();
		if(copy.isEmpty()) return ItemStack.EMPTY;
		copy.setCount(size);
		return copy;
	}

	/**
	 * Runs carefulCopy over the entire ItemStack array.
	 * @param array
	 * @return
	 */
	public static ItemStack[] carefulCopyArray(ItemStack[] array) {
		return carefulCopyArray(array, 0, array.length - 1);
	}

	/**
	 * Recreates the ItemStack array and only runs carefulCopy over the supplied range. All other fields remain null.
	 * @param array
	 * @param start
	 * @param end
	 * @return
	 */
	public static ItemStack[] carefulCopyArray(ItemStack[] array, int start, int end) {
		if(array == null)
			return null;

		ItemStack[] copy = new ItemStack[array.length];

		for(int i = start; i <= end; i++) {
			copy[i] = carefulCopy(array[i]);
		}

		return copy;
	}

	/**
	 * Creates a new array that only contains the copied range.
	 * @param array
	 * @param start
	 * @param end
	 * @return
	 */
	public static ItemStack[] carefulCopyArrayTruncate(ItemStack[] array, int start, int end) {
		if(array == null)
			return null;

		int length = end - start + 1;
		ItemStack[] copy = new ItemStack[length];

		for(int i = 0; i < length; i++) {
			copy[i] = carefulCopy(array[start + i]);
		}

		return copy;
	}

	/**
	 * UNSAFE! Will ignore all existing display tags and override them! In its current state, only fit for items we know don't have any display tags!
	 * Will, however, respect existing NBT tags
	 * @param stack
	 * @param lines
	 */
	public static ItemStack addTooltipToStack(ItemStack stack, String... lines) {

		if(stack.isEmpty()) return stack; // Never attach tags to the shared native empty stack.

		if(!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());

		NBTTagCompound display = new NBTTagCompound();
		NBTTagList lore = new NBTTagList();

		for(String line : lines) {
			lore.appendTag(new NBTTagString(TextFormatting.RESET + "" + TextFormatting.GRAY + line));
		}

		display.setTag("Lore", lore);
		stack.getTagCompound().setTag("display", display);

		return stack;
	}
	
	/**
	 * Automatically adds multistack labels for displays that use a ton of items (like construction recipe handlers).
	 * @param stack
	 * @return
	 */
	public static ItemStack addStackSizeLabel(ItemStack stack) {

		if(stack.getCount() > 64) {
			int stacks = stack.getCount() / 64;
			int items = stack.getCount() % 64;
			addTooltipToStack(stack, TextFormatting.RED + "" + stacks + "x64" + (items > 0 ? (" + " + items) : ""));
		}
		
		return stack;
	}

	public static void addStacksToNBT(ItemStack stack, ItemStack... stacks) {

		if(stack.isEmpty()) return;

		if(!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());

		NBTTagList tags = new NBTTagList();

		for(int i = 0; i < stacks.length; i++) {
			if(stacks[i] != null && !stacks[i].isEmpty()) {
				NBTTagCompound slotNBT = new NBTTagCompound();
				slotNBT.setByte("slot", (byte) i);
				stacks[i].writeToNBT(slotNBT);
				tags.appendTag(slotNBT);
			}
		}
		stack.getTagCompound().setTag("items", tags);
	}

	public static ItemStack[] readStacksFromNBT(ItemStack stack, int count) {

		if(!stack.hasTagCompound())
			return null;

		NBTTagList list = stack.getTagCompound().getTagList("items", 10);
		if(count == 0) {
			count = list.tagCount();
		}

		ItemStack[] stacks = new ItemStack[count];

		for(int i = 0; i < count; i++) {
			NBTTagCompound slotNBT = list.getCompoundTagAt(i);
			byte slot = slotNBT.getByte("slot");
			ItemStack loadedStack = new ItemStack(slotNBT);
			if(slot >= 0 && slot < stacks.length && !loadedStack.isEmpty()) {
				stacks[slot] = loadedStack;
			}
		}

		return stacks;
	}

	public static ItemStack[] readStacksFromNBT(ItemStack stack) {
		return readStacksFromNBT(stack, 0);
	}

	/**
	 * Returns a List<String> of all ore dict names for this stack. Stack cannot be null, list is empty when there are no ore dict entries.
	 * @param stack
	 * @return
	 */
	public static List<String> getOreDictNames(ItemStack stack) {
		List<String> list = new ArrayList();

		int ids[] = OreDictionary.getOreIDs(stack);
		for(int i : ids) {
			list.add(OreDictionary.getOreName(i));
		}

		return list;
	}
	
	/**
	 * Returns a String of the mod id of an itemstack. If a unique identifier can't be found in the registry, returns null.
	 * @param stack
	 * @return
	 */
	public static String getModIdFromItemStack(ItemStack stack) {
		ResourceLocation id = Item.REGISTRY.getNameForObject(stack.getItem());
		if(id!=null) {
			return id.getNamespace();
		}
		return null;
	}

	public static void spillItems(World world, int x, int y, int z, Block block, Random rand) {
		IInventory tileentityfurnace = (IInventory) world.getTileEntity(new BlockPos(x, y, z));

		if(tileentityfurnace != null) {
			for(int slot = 0; slot < tileentityfurnace.getSizeInventory(); ++slot) {
				ItemStack itemstack = tileentityfurnace.getStackInSlot(slot);

				if(itemstack != null && !itemstack.isEmpty()) {
					float oX = rand.nextFloat() * 0.8F + 0.1F;
					float oY = rand.nextFloat() * 0.8F + 0.1F;
					float oZ = rand.nextFloat() * 0.8F + 0.1F;

					while(itemstack.getCount() > 0) {
						int j1 = rand.nextInt(21) + 10;
						if(j1 > itemstack.getCount()) j1 = itemstack.getCount();
						// Native stacks hide their item once count reaches zero. Capture before shrinking.
						Item item = itemstack.getItem();
						int meta = itemstack.getItemDamage();
						NBTTagCompound tag = itemstack.getTagCompound();
						itemstack.shrink(j1);

						EntityItem entityitem = new EntityItem(world, x + oX, y + oY, z + oZ, new ItemStack(item, j1, meta));
						if(tag != null) entityitem.getItem().setTagCompound(tag.copy());

						float motion = 0.05F;
						entityitem.motionX = (float) rand.nextGaussian() * motion;
						entityitem.motionY = (float) rand.nextGaussian() * motion + 0.2F;
						entityitem.motionZ = (float) rand.nextGaussian() * motion;
						world.spawnEntity(entityitem);
					}
				}
			}
			world.updateComparatorOutputLevel(new BlockPos(x, y, z), block);
		}
	}
	
	public static boolean areStacksCompatible(ItemStack sta1, ItemStack sta2) {
		return sta1.getItem() == sta2.getItem() && sta1.getItemDamage() == sta2.getItemDamage() && ItemStack.areItemStackTagsEqual(sta1, sta2);
	}
	
	/** I can never remember where this fucking util crap is, so I just write my own wrapper */
	public static void addNBTFromString(ItemStack stack, String nbt) {
		if(stack != null && stack.isEmpty()) return;
		
		try {
			NBTBase compound = JsonToNBT.getTagFromJson(nbt);
			if(compound instanceof NBTTagCompound) {
				stack.setTagCompound((NBTTagCompound) compound);
			}
			
		} catch(NBTException e) { }
	}
}
