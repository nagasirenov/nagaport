package com.hbm.items.armor;

import java.util.List;

import com.hbm.handler.ArmorModHandler;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public class ItemModBandaid extends ItemArmorMod {

	public ItemModBandaid() {
		super(ArmorModHandler.extra, true, true, true, true);
	}
	
	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer player, List list, boolean bool) {

		list.add(TextFormatting.RED + "3% chance for full heal when damaged");
		list.add("");
		super.addInformation(itemstack, player, list, bool);
	}
	
	@Override
	public void addDesc(List list, ItemStack stack, ItemStack armor) {
		list.add(TextFormatting.RED + "  " + stack.getDisplayName() + " (3% chance for full heal)");
	}
	
	@Override
	public void modDamage(LivingHurtEvent event, ItemStack armor) {
		
		if(event.getEntity().world.rand.nextInt(100) < 3) {
			event.ammount = 0;
			event.getEntityLiving().heal(event.getEntityLiving().getMaxHealth());
		}
	}
}
