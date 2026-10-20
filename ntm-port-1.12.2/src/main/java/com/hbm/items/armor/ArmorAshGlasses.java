package com.hbm.items.armor;

import com.hbm.render.model.ModelGlasses;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;

public class ArmorAshGlasses extends ItemArmor {
	
	@SideOnly(Side.CLIENT)
	private ModelGlasses modelGoggles;

	public ArmorAshGlasses(ArmorMaterial armorMaterial, int armorType) {
		super(armorMaterial, 0, com.hbm.util.ArmorSlotOrder.fromIndex(armorType));
	}

	@SideOnly(Side.CLIENT)
	ModelGlasses model;

	@Override
	@SideOnly(Side.CLIENT)
	public ModelBiped getArmorModel(EntityLivingBase entityLiving, ItemStack itemStack, net.minecraft.inventory.EntityEquipmentSlot armorSlot, ModelBiped defaultModel) {
		
		if(model == null) {
			model = new ModelGlasses(0);
		}
		
		return model;
	}
}
