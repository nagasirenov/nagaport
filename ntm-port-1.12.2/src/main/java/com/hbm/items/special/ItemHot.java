package com.hbm.items.special;

import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class ItemHot extends Item {

	protected static int heat;

	public ItemHot(int heat) {
		this.heat = heat;
	}

	public void onUpdate(ItemStack stack, World world, Entity entity, int i, boolean bool) {

		if(!world.isRemote && stack.hasTagCompound()) {

			int h = stack.getTagCompound().getInteger("heat");

			if(h > 0) {
				stack.getTagCompound().setInteger("heat", h - 1);
			} else {
				stack.setTagCompound(null);
			}
		}
	}

	public static ItemStack heatUp(ItemStack stack) {

		if(!(stack.getItem() instanceof ItemHot))
			return stack;

		if(!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());

		stack.getTagCompound().setInteger("heat", getMaxHeat(stack));
		return stack;
	}

	public static ItemStack heatUp(ItemStack stack, double d) {

		if(!(stack.getItem() instanceof ItemHot))
			return stack;

		if(!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());

		stack.getTagCompound().setInteger("heat", (int) (d * getMaxHeat(stack)));
		return stack;
	}

	public static double getHeat(ItemStack stack) {

		if(!(stack.getItem() instanceof ItemHot))
			return 0;

		if(!stack.hasTagCompound())
			return 0;

		int h = stack.getTagCompound().getInteger("heat");

		return (double) h / (double) heat;
	}

	public static int getMaxHeat(ItemStack stack) {
		return heat;
	}
}
