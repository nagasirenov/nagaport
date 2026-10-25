package com.hbm.items.tool;

import java.util.List;

import com.hbm.items.ModItems;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class ItemKeyPin extends Item {


		@Override
	@net.minecraftforge.fml.relauncher.SideOnly(net.minecraftforge.fml.relauncher.Side.CLIENT)
	public void addInformation(net.minecraft.item.ItemStack stack, net.minecraft.world.World worldIn, java.util.List<java.lang.String> tooltip, net.minecraft.client.util.ITooltipFlag flagIn) {
		addInformation(stack, (net.minecraft.entity.player.EntityPlayer) null, tooltip, flagIn.isAdvanced());
	}

public void addInformation(ItemStack itemstack, EntityPlayer player, List list, boolean bool)
	{
		if(getPins(itemstack) != 0)
			list.add("Pin configuration: " + getPins(itemstack));
		else
			list.add("Pins not set!");

		if(this == ModItems.key_fake) {

			list.add("");
			list.add("Pins can neither be changed, nor copied.");
		}
	}

	public static int getPins(ItemStack stack) {
		if(stack.getTagCompound() == null) {
			stack.setTagCompound(new NBTTagCompound());
			return 0;
		}
		return stack.getTagCompound().getInteger("pins");
	}

	public static void setPins(ItemStack stack, int i) {
		if(stack.getTagCompound() == null) {
			stack.setTagCompound(new NBTTagCompound());
		}
		stack.getTagCompound().setInteger("pins", i);
	}

	public boolean canTransfer() {
		return this != ModItems.key_fake;
	}
}
