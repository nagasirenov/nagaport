package com.hbm.items.armor;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;
import com.hbm.util.ArmorUtil;

public class ArmorEuphemium extends ItemArmor {

	public ArmorEuphemium(ArmorMaterial armorMaterial, int armorType) {
		super(armorMaterial, 0, com.hbm.util.ArmorSlotOrder.fromIndex(armorType));
		this.setCreativeTab(null);
	}

	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, net.minecraft.inventory.EntityEquipmentSlot slot, String layer) {
		if(stack.getItem().equals(ModItems.euphemium_helmet) || stack.getItem().equals(ModItems.euphemium_plate) || stack.getItem().equals(ModItems.euphemium_boots)) {
			return (RefStrings.MODID + ":textures/armor/euphemium_1.png");
		}
		if(stack.getItem().equals(ModItems.euphemium_legs)) {
			return (RefStrings.MODID + ":textures/armor/euphemium_2.png");
		}

		else return null;
	}

	@Override
	public void onArmorTick(World world, EntityPlayer player, ItemStack armor) {
		if(player instanceof EntityPlayer && ArmorUtil.checkArmor(player, ModItems.euphemium_helmet, ModItems.euphemium_plate, ModItems.euphemium_legs, ModItems.euphemium_boots))
		{
			player.addPotionEffect(new PotionEffect(net.minecraft.init.MobEffects.REGENERATION, 5, 127, true, true));
			player.addPotionEffect(new PotionEffect(net.minecraft.init.MobEffects.RESISTANCE, 5, 127, true, true));
			player.addPotionEffect(new PotionEffect(net.minecraft.init.MobEffects.FIRE_RESISTANCE, 5, 127, true, true));
			player.addPotionEffect(new PotionEffect(net.minecraft.init.MobEffects.SATURATION, 5, 127, true, true));

			if(player.motionY < -0.25D)
			{
				player.motionY = -0.25D;
				player.fallDistance = 0;
			}

		}
	}

	@Override
	public EnumRarity getRarity(ItemStack p_77613_1_) {

		return EnumRarity.EPIC;
	}

	//do literally nothing lole
	@Override
	public void setDamage(ItemStack stack, int damage) {  }

}
