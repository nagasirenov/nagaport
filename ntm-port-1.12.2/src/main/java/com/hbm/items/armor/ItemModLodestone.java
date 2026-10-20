package com.hbm.items.armor;

import java.util.List;

import com.hbm.extprop.HbmPlayerProps;
import com.hbm.handler.ArmorModHandler;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.math.Vec3d;

public class ItemModLodestone extends ItemArmorMod {

	int range;
	
	public ItemModLodestone(int range) {
		super(ArmorModHandler.extra, true, true, true, true);
		this.range = range;
	}
	
	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer player, List list, boolean bool) {

		list.add(TextFormatting.DARK_GRAY + "Attracts nearby items");
		list.add(TextFormatting.DARK_GRAY + "Item attraction range: " + range);
		list.add("");
		super.addInformation(itemstack, player, list, bool);
	}

	@Override
	public void addDesc(List list, ItemStack stack, ItemStack armor) {
		list.add(TextFormatting.DARK_GRAY + "  " + stack.getDisplayName() + " (Magnetic range: " + range + ")");
	}
	
	@Override
	public void modUpdate(EntityLivingBase entity, ItemStack armor) {

		// No magnet if keybind toggled
		if (entity instanceof EntityPlayer && !HbmPlayerProps.getData((EntityPlayer) entity).isMagnetActive()) return;

		List<EntityItem> items = entity.world.getEntitiesWithinAABB(EntityItem.class, entity.getEntityBoundingBox().expand(range, range, range));
		
		for(EntityItem item : items) {
			
			Vec3d vec = new Vec3d(entity.posX - item.posX, entity.posY - item.posY, entity.posZ - item.posZ);
			vec = vec.normalize();

			item.motionX += vec.x * 0.05;
			item.motionY += vec.y * 0.05;
			item.motionZ += vec.z * 0.05;
			
			if(vec.y > 0 && item.motionY < 0.04)
				item.motionY += 0.2;
		}
	}
}
