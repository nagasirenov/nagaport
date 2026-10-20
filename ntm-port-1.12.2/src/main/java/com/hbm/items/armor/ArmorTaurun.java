package com.hbm.items.armor;

import org.lwjgl.opengl.GL11;

import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.model.ModelArmorTaurun;
import com.hbm.render.tileentity.IItemRendererProvider;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;


public class ArmorTaurun extends ArmorFSB implements IItemRendererProvider {

	public ArmorTaurun(ArmorMaterial material, int slot, String texture) {
		super(material, slot, texture);
		this.setMaxDamage(0);
	}

	@SideOnly(Side.CLIENT)
	ModelArmorTaurun[] models;

	@Override
	@SideOnly(Side.CLIENT)
	public ModelBiped getArmorModel(EntityLivingBase entityLiving, ItemStack itemStack, net.minecraft.inventory.EntityEquipmentSlot armorSlot, ModelBiped defaultModel) {

		if(models == null) {
			models = new ModelArmorTaurun[4];

			for(int i = 0; i < 4; i++)
				models[i] = new ModelArmorTaurun(i);
		}

		return models[com.hbm.util.ArmorSlotOrder.index(armorSlot)];
	}

	@Override public Item getItemForRenderer() { return this; }

	@Override
	@net.minecraftforge.fml.relauncher.SideOnly(net.minecraftforge.fml.relauncher.Side.CLIENT)
	public ItemRenderBase getRenderer() {
		return new ItemRenderBase( ) {
			public void renderInventory() { 
				if(com.hbm.util.ArmorSlotOrder.index(armorType) == 0) GL11.glTranslated(0, 1, 0);
				if(com.hbm.util.ArmorSlotOrder.index(armorType) == 1) GL11.glTranslated(0, 1.5, 0);
				setupRenderInv();
			}
			public void renderNonInv() { setupRenderNonInv(); }
			public void renderCommon() {
				renderStandard(ResourceManager.armor_taurun, com.hbm.util.ArmorSlotOrder.index(armorType),
						ResourceManager.taurun_helmet, ResourceManager.taurun_chest, ResourceManager.taurun_arm, ResourceManager.taurun_leg,
						"Helmet", "Chest", "LeftArm", "RightArm", "LeftLeg", "RightLeg", "LeftBoot", "RightBoot");
			}};
	}
}
