package com.hbm.items.armor;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.hbm.handler.ArmorModHandler;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.model.ModelArmorDesh;
import com.hbm.render.tileentity.IItemRendererProvider;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;


public class ArmorDesh extends ArmorFSBFueled implements IItemRendererProvider {

	public ArmorDesh(ArmorMaterial material, int slot, String texture, FluidType fuelType, int maxFuel, int fillRate, int consumption, int drain) {
		super(material, slot, texture, fuelType, maxFuel, fillRate, consumption, drain);
	}
	
	@Override
	public Multimap getItemAttributeModifiers(net.minecraft.inventory.EntityEquipmentSlot slot) {

		Multimap multimap = super.getItemAttributeModifiers(slot);
		if(slot == net.minecraft.inventory.EntityEquipmentSlot.OFFHAND) return multimap;
		multimap.put(SharedMonsterAttributes.MOVEMENT_SPEED.getName(), new AttributeModifier(ArmorModHandler.fixedUUIDs[com.hbm.util.ArmorSlotOrder.index(this.armorType)], "Armor modifier", -0.025D, 1));
		return multimap;
	}

	@SideOnly(Side.CLIENT)
	ModelArmorDesh[] models;

	@Override
	@SideOnly(Side.CLIENT)
	public ModelBiped getArmorModel(EntityLivingBase entityLiving, ItemStack itemStack, net.minecraft.inventory.EntityEquipmentSlot armorSlot, ModelBiped defaultModel) {
		
		if(models == null) {
			models = new ModelArmorDesh[4];
			
			for(int i = 0; i < 4; i++)
				models[i] = new ModelArmorDesh(i);
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
				renderStandard(ResourceManager.armor_steamsuit, com.hbm.util.ArmorSlotOrder.index(armorType),
						ResourceManager.steamsuit_helmet, ResourceManager.steamsuit_chest, ResourceManager.steamsuit_arm, ResourceManager.steamsuit_leg,
						"Head", "Body", "LeftArm", "RightArm", "LeftLeg", "RightLeg", "LeftBoot", "RightBoot");
			}};
	}
}
