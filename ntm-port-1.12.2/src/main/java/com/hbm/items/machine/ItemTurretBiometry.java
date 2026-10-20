package com.hbm.items.machine;

import java.util.Arrays;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

public class ItemTurretBiometry extends Item {

	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer player, List list, boolean bool) {
		String[] names = getNames(itemstack);
		if(names != null)
			for(int i = 0; i < names.length; i++)
				list.add(names[i]);
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {

		addName(stack, player.getDisplayName());

		if(world.isRemote)
			player.sendMessage(new TextComponentString("Added player data!"));

		world.playSoundAtEntity(player, "hbm:item.techBleep", 1.0F, 1.0F);

		player.swingItem();

		return stack;
	}

	public static String[] getNames(ItemStack stack) {
		if(stack.getTagCompound() == null) {
			stack.setTagCompound(new NBTTagCompound());
			return null;
		}

		String[] names = new String[stack.getTagCompound().getInteger("playercount")];

		for(int i = 0; i < names.length; i++) {
			names[i] = stack.getTagCompound().getString("player_" + i);
		}

		if(names.length == 0)
			return null;

		return names;
	}

	public static void addName(ItemStack stack, String s) {
		if(stack.getTagCompound() == null) {
			stack.setTagCompound(new NBTTagCompound());
		}

		String[] names = getNames(stack);
		int count = 0;

		if(names != null && Arrays.asList(names).contains(s))
			return;

		if(names != null)
			count = names.length;

		stack.getTagCompound().setInteger("playercount", count + 1);

		stack.getTagCompound().setString("player_" + count, s);
	}

	public static void clearNames(ItemStack stack) {
		if(stack.getTagCompound() == null) {
			stack.setTagCompound(new NBTTagCompound());
		}

		stack.getTagCompound().setInteger("playercount", 0);
	}
}
