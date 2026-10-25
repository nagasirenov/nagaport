package com.hbm.hazard.type;

import java.util.List;

import com.hbm.config.RadiationConfig;
import com.hbm.hazard.modifier.HazardModifier;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

public class HazardTypeHydroactive extends HazardTypeBase {

	@Override
	public void onUpdate(EntityLivingBase target, float level, ItemStack stack) {

		if(RadiationConfig.disableHydro)
			return;

		if(target.isWet() && stack.getCount() > 0) {
			stack.setCount(0);
			target.world.newExplosion(null, target.posX, target.posY + target.getEyeHeight() - target.getYOffset(), target.posZ, level, false, true);
		}
	}

	@Override
	public void updateEntity(EntityItem item, float level) {

		if(RadiationConfig.disableHydro)
			return;

		if(item.isWet() || com.hbm.world.WorldBlockState.read(item.world, (int) Math.floor(item.posX), (int) Math.floor(item.posY), (int) Math.floor(item.posZ)).getMaterial() == Material.WATER) {
			item.setDead();
			item.world.newExplosion(null, item.posX, item.posY + item.height * 0.5, item.posZ, level, false, true);
		}
	}

	@Override
	public void addHazardInformation(EntityPlayer player, List list, float level, ItemStack stack, List<HazardModifier> modifiers) {
		list.add(TextFormatting.RED + "[" + I18nUtil.resolveKey("trait.hydro") + "]");
	}
}
