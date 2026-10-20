package com.hbm.items.armor;

import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.model.ModelArmorAJRO;
import com.hbm.render.tileentity.IItemRendererProvider;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;


public class ArmorAJRO extends ArmorFSBPowered implements IItemRendererProvider {

	public ArmorAJRO(ArmorMaterial material, int slot, String texture, long maxPower, long chargeRate, long consumption, long drain) {
		super(material, slot, texture, maxPower, chargeRate, consumption, drain);
	}

	@SideOnly(Side.CLIENT)
	ModelArmorAJRO[] models;

	@Override
	@SideOnly(Side.CLIENT)
	public ModelBiped getArmorModel(EntityLivingBase entityLiving, ItemStack itemStack, net.minecraft.inventory.EntityEquipmentSlot armorSlot, ModelBiped defaultModel) {
		
		if(models == null) {
			models = new ModelArmorAJRO[4];
			
			for(int i = 0; i < 4; i++)
				models[i] = new ModelArmorAJRO(i);
		}
		
		return models[com.hbm.util.ArmorSlotOrder.index(armorSlot)];
	}

	@Override public Item getItemForRenderer() { return this; }

	@Override
	@net.minecraftforge.fml.relauncher.SideOnly(net.minecraftforge.fml.relauncher.Side.CLIENT)
	public ItemRenderBase getRenderer() {
		return new ItemRenderBase( ) {
			public void renderInventory() { setupRenderInv(); }
			public void renderNonInv() { setupRenderNonInv(); }
			public void renderCommon() {
				renderStandard(ResourceManager.armor_ajr, com.hbm.util.ArmorSlotOrder.index(armorType),
						ResourceManager.ajro_helmet, ResourceManager.ajro_chest, ResourceManager.ajro_arm, ResourceManager.ajro_leg,
						"Head", "Body", "LeftArm", "RightArm", "LeftLeg", "RightLeg", "LeftBoot", "RightBoot");
			}};
	}
}
