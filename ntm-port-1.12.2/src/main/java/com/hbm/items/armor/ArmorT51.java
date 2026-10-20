package com.hbm.items.armor;

import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.model.ModelArmorT51;
import com.hbm.render.tileentity.IItemRendererProvider;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;


public class ArmorT51 extends ArmorFSBPowered implements IItemRendererProvider {

	public ArmorT51(ArmorMaterial material, int slot, String texture, long maxPower, long chargeRate, long consumption, long drain) {
		super(material, slot, texture, maxPower, chargeRate, consumption, drain);
	}

	@SideOnly(Side.CLIENT)
	ModelArmorT51[] models;

	@Override
	@SideOnly(Side.CLIENT)
	public ModelBiped getArmorModel(EntityLivingBase entityLiving, ItemStack itemStack, int armorSlot) {
		
		if(models == null) {
			models = new ModelArmorT51[4];
			
			for(int i = 0; i < 4; i++)
				models[i] = new ModelArmorT51(i);
		}
		
		return models[armorSlot];
	}

	@Override public Item getItemForRenderer() { return this; }

	@Override
	@net.minecraftforge.fml.relauncher.SideOnly(net.minecraftforge.fml.relauncher.Side.CLIENT)
	public ItemRenderBase getRenderer() {
		return new ItemRenderBase( ) {
			public void renderInventory() { setupRenderInv(); }
			public void renderNonInv() { setupRenderNonInv(); }
			public void renderCommon() {
				renderStandard(ResourceManager.armor_t51, armorType,
						ResourceManager.t51_helmet, ResourceManager.t51_chest, ResourceManager.t51_arm, ResourceManager.t51_leg,
						"Helmet", "Chest", "LeftArm", "RightArm", "LeftLeg", "RightLeg", "LeftBoot", "RightBoot");
			}};
	}
}
