package com.hbm.items.armor;

import java.util.List;

import com.hbm.handler.ArmorModHandler;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.text.TextFormatting;

public class ItemModSerum extends ItemArmorMod {

	public ItemModSerum() {
		super(ArmorModHandler.extra, true, true, true, true);
	}

	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer player, List list, boolean bool) {

		list.add(TextFormatting.GREEN + "Cures poison and gives strength");
		list.add("");
		super.addInformation(itemstack, player, list, bool);
	}

	@Override
	public void addDesc(List list, ItemStack stack, ItemStack armor) {
		list.add(TextFormatting.BLUE + "  " + stack.getDisplayName() + " (replaces poison with strength)");
	}

	@Override
	public void modUpdate(EntityLivingBase entity, ItemStack armor) {

		if(!entity.world.isRemote && entity.isPotionActive(net.minecraft.init.MobEffects.POISON)) {
			entity.removePotionEffect(net.minecraft.init.MobEffects.POISON);
			entity.addPotionEffect(new PotionEffect(net.minecraft.init.MobEffects.STRENGTH, 100, 4));
		}
	}
}
