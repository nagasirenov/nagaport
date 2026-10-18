package com.hbm.items.armor;

import java.util.List;

import com.hbm.handler.ArmorModHandler;
import com.hbm.items.ModItems;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

public class ItemModRevive extends ItemArmorMod {

	public ItemModRevive(int durability) {
		super(ArmorModHandler.extra, false, false, true, false);
		this.setMaxDamage(durability);
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean bool) {

		if(this == ModItems.scrumpy) {
			list.add(TextFormatting.GOLD + "But how did you survive?");
			list.add(TextFormatting.RED + "I was drunk.");
		}
		if(this == ModItems.wild_p) {
			list.add(TextFormatting.DARK_GRAY + "Explosive " + TextFormatting.RED + "Reactive " + TextFormatting.DARK_GRAY + "Plot " + TextFormatting.RED + "Armor");
		}
		
		/*list.add(EnumChatFormatting.ITALIC + "In the news:");
		list.add(EnumChatFormatting.RED + "" + EnumChatFormatting.BOLD + "Man literally too angry to die.");
		list.add("");
		list.add(EnumChatFormatting.ITALIC + "\"I ain't got time to die\" says local");
		list.add(EnumChatFormatting.ITALIC + "man after ripping the physical manifestation");
		list.add(EnumChatFormatting.ITALIC + "of disaster itself in half.");*/
		
		list.add("");
		list.add(TextFormatting.GOLD + "" + (stack.getMaxDamage() - stack.getItemDamage()) + " revives left");
		list.add("");
		super.addInformation(stack, player, list, bool);
	}

	@Override
	public void addDesc(List list, ItemStack stack, ItemStack armor) {

		list.add(TextFormatting.GOLD + "  " + stack.getDisplayName() + " (" + (stack.getMaxDamage() - stack.getItemDamage()) + " revives left)");
	}
}
