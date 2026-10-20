package com.hbm.items.armor;

import java.util.List;

import com.hbm.extprop.HbmLivingProps;
import com.hbm.handler.ArmorModHandler;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public class ItemModQuartz extends ItemArmorMod {

	public ItemModQuartz() {
		super(ArmorModHandler.extra, true, true, true, true);
	}
	
	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer player, List list, boolean bool) {
		
		list.add(TextFormatting.DARK_GRAY + "Taking damage removes 10 RAD");
		list.add("");
		super.addInformation(itemstack, player, list, bool);
	}
	
	@Override
	public void addDesc(List list, ItemStack stack, ItemStack armor) {
		list.add(TextFormatting.DARK_GRAY + "  " + stack.getDisplayName() + " (-10 RAD when hit)");
	}
	
	@Override
	public void modDamage(LivingHurtEvent event, ItemStack armor) {
		
		if(!event.getEntityLiving().world.isRemote) {
			float rad = HbmLivingProps.getRadiation(event.getEntityLiving());
			rad = Math.max(rad - 10, 0);
			HbmLivingProps.setRadiation(event.getEntityLiving(), rad);
		}
	}
}
