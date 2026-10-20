package com.hbm.items.armor;

import java.util.List;

import com.hbm.extprop.HbmLivingProps;
import com.hbm.handler.ArmorModHandler;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

public class ItemModMedal extends ItemArmorMod {

	public ItemModMedal() {
		super(ArmorModHandler.plate_only, true, true, false, false);
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean bool) {
		list.add(TextFormatting.GOLD + "-10 RAD/s");
		super.addInformation(stack, player, list, bool);
	}

	@Override
	public void addDesc(List list, ItemStack stack, ItemStack armor) {
		list.add(TextFormatting.GOLD + "  " + stack.getDisplayName() + " (-10 RAD/s)");
	}
	
	@Override
	public void modUpdate(EntityLivingBase entity, ItemStack armor) {
		
		if(!entity.world.isRemote) {
			float rad = HbmLivingProps.getRadiation(entity);
			rad -= 0.5F;
			HbmLivingProps.setRadiation(entity, Math.max(rad, 0));
		}
	}
}
