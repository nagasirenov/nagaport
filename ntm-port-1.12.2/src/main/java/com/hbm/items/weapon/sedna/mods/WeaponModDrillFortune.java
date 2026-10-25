package com.hbm.items.weapon.sedna.mods;

import com.hbm.util.EnchantmentUtil;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;

public class WeaponModDrillFortune extends WeaponModBase {
	
	int addFortune = 0;
	
	public WeaponModDrillFortune(int id, String slot, int fortune) {
		super(id, slot);
		this.setPriority(PRIORITY_ADDITIVE);
		this.addFortune = fortune;
	}

	@Override
	public <T> T eval(T base, ItemStack gun, String key, Object parent) {
		return base;
	}

	@Override
	public void onInstall(ItemStack gun, ItemStack mod, int index) {
		int fortuneLvl = EnchantmentUtil.getEnchantmentLevel(gun, net.minecraft.init.Enchantments.FORTUNE);
		fortuneLvl += this.addFortune;
		EnchantmentUtil.removeEnchantment(gun, net.minecraft.init.Enchantments.FORTUNE);
		EnchantmentUtil.addEnchantment(gun, net.minecraft.init.Enchantments.FORTUNE, fortuneLvl);
	}
	
	@Override
	public void onUninstall(ItemStack gun, ItemStack mod, int index) {
		int fortuneLvl = EnchantmentUtil.getEnchantmentLevel(gun, net.minecraft.init.Enchantments.FORTUNE);
		fortuneLvl -= this.addFortune;
		EnchantmentUtil.removeEnchantment(gun, net.minecraft.init.Enchantments.FORTUNE);
		if(fortuneLvl > 0) EnchantmentUtil.addEnchantment(gun, net.minecraft.init.Enchantments.FORTUNE, fortuneLvl);
	}
}
