package com.hbm.items.armor;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.hbm.handler.ArmorModHandler;
import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;
import com.hbm.render.model.ModelM65;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class ArmorLiquidator extends ArmorFSB {
	
	private ModelM65 model;
	private ResourceLocation hazmatBlur = new ResourceLocation(RefStrings.MODID + ":textures/misc/overlay_dark.png");

	public ArmorLiquidator(ArmorMaterial material, int slot, String texture) {
		super(material, slot, texture);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public ModelBiped getArmorModel(EntityLivingBase entityLiving, ItemStack itemStack, net.minecraft.inventory.EntityEquipmentSlot armorSlot, ModelBiped defaultModel) {
		
		if (this == ModItems.liquidator_helmet) {
			if (armorSlot == net.minecraft.inventory.EntityEquipmentSlot.HEAD) {
				if (this.model == null) {
					this.model = new ModelM65();
				}
				return this.model;
			}
		}
		
		return super.getArmorModel(entityLiving, itemStack, armorSlot, defaultModel);
	}
	
	@Override
	public Multimap getItemAttributeModifiers(net.minecraft.inventory.EntityEquipmentSlot slot) {

		Multimap multimap = super.getItemAttributeModifiers(slot);
		if(slot == net.minecraft.inventory.EntityEquipmentSlot.OFFHAND) return multimap;
		multimap.put(SharedMonsterAttributes.KNOCKBACK_RESISTANCE.getName(), new AttributeModifier(ArmorModHandler.fixedUUIDs[com.hbm.util.ArmorSlotOrder.index(this.armorType)], "Armor modifier", 100D, 0));
		multimap.put(SharedMonsterAttributes.MOVEMENT_SPEED.getName(), new AttributeModifier(ArmorModHandler.fixedUUIDs[com.hbm.util.ArmorSlotOrder.index(this.armorType)], "Armor modifier", -0.1D, 1));
		return multimap;
	}
	
	@SideOnly(Side.CLIENT)
	public void renderHelmetOverlay(ItemStack stack, EntityPlayer player, ScaledResolution resolution, float partialTicks, boolean hasScreen, int mouseX, int mouseY) {

		GL11.glEnable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDepthMask(false);
		OpenGlHelper.glBlendFunc(770, 771, 1, 0);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		Minecraft.getMinecraft().getTextureManager().bindTexture(hazmatBlur);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder tessellatorBuffer = tessellator.getBuffer();
		tessellatorBuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
		tessellatorBuffer.pos(0.0D, (double) resolution.getScaledHeight(), -90.0D).tex(0.0D, 1.0D).endVertex();
		tessellatorBuffer.pos((double) resolution.getScaledWidth(), (double) resolution.getScaledHeight(), -90.0D).tex(1.0D, 1.0D).endVertex();
		tessellatorBuffer.pos((double) resolution.getScaledWidth(), 0.0D, -90.0D).tex(1.0D, 0.0D).endVertex();
		tessellatorBuffer.pos(0.0D, 0.0D, -90.0D).tex(0.0D, 0.0D).endVertex();
		tessellator.draw();
		GL11.glDepthMask(true);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
	}
}
