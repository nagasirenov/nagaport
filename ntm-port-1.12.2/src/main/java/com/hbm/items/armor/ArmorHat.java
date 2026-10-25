package com.hbm.items.armor;

import java.util.List;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public class ArmorHat extends ArmorModel implements IAttackHandler, IDamageHandler {

	public ArmorHat(ArmorMaterial armorMaterial, int armorType) {
		super(armorMaterial, armorType);
	}

	@Override
	public boolean onEntityItemUpdate(EntityItem entityItem) {
		entityItem.setDead();
		return true;
	}

	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer player, List list, boolean bool) {
		list.add(TextFormatting.BLUE + "+2 DT");
	}

	@Override
	public void handleDamage(LivingHurtEvent event, ItemStack stack) {
		
		if(event.getSource().isUnblockable())
			return;
		
		event.setAmount((float) (event.getAmount() - (2F)));
		
		if(event.getAmount() < 0)
			event.setAmount(0);
	}

	@Override
	public void handleAttack(LivingAttackEvent event, ItemStack armor) {
		
		if(event.getSource().isUnblockable())
			return;
		
		if(event.getAmount() <= 2F) {
			event.getEntityLiving().world.playSound(null, event.getEntityLiving().posX, event.getEntityLiving().posY, event.getEntityLiving().posZ, com.hbm.main.ModSoundEvents.resolve("random.break"), net.minecraft.util.SoundCategory.PLAYERS, 5F, 1.0F + event.getEntityLiving().getRNG().nextFloat() * 0.5F);
			event.setCanceled(true);
		}
	}
}
