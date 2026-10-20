package com.hbm.items.machine;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class ItemLens extends Item {
	
	public long maxDamage;
	
	public ItemLens(long maxDamage) {
		this.maxDamage = maxDamage;
	}
	
	public static long getLensDamage(ItemStack stack) {
		
		if(!stack.hasTagCompound()) {
			stack.setTagCompound(new NBTTagCompound());
			return 0;
		}
		
		return stack.getTagCompound().getLong("damage");
	}

	
	@Override
	public void addInformation(ItemStack stack, net.minecraft.world.World tooltipWorld, List<String> list, net.minecraft.client.util.ITooltipFlag tooltipFlag) {
		
		long damage = getLensDamage(stack);
		int percent = (int) ((maxDamage - damage) * 100 / maxDamage);
		
		list.add("Durability: " + (maxDamage - damage) + "/" + maxDamage + " (" + percent + "%)");
	}
	
	public static void setLensDamage(ItemStack stack, long damage) {
		
		if(!stack.hasTagCompound()) {
			stack.setTagCompound(new NBTTagCompound());
		}
		
		stack.getTagCompound().setLong("damage", damage);
	}

	@Override
	public double getDurabilityForDisplay(ItemStack stack) {
		return (double) getLensDamage(stack) / (double) maxDamage;
	}

	@Override
	public boolean showDurabilityBar(ItemStack stack) {
		return getDurabilityForDisplay(stack) != 0;
	}
}
