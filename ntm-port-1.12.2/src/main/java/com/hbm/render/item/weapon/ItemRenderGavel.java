package com.hbm.render.item.weapon;

import org.lwjgl.opengl.GL11;

import com.hbm.items.ModItems;
import com.hbm.main.ResourceManager;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import com.hbm.render.item.nativeitem.context.*;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;

public class ItemRenderGavel extends NativeOriginalItemRenderer {
	
	public ItemRenderGavel() { super(false, true); }

	@Override
	protected void drawModel(ItemStack item, NativeItemContext context) {
		GL11.glPushMatrix();
		
		net.minecraft.entity.EntityLivingBase player = context.holder;

		if(item.getItem() == ModItems.wood_gavel)
			Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.gavel_wood);
		if(item.getItem() == ModItems.lead_gavel)
			Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.gavel_lead);
		if(item.getItem() == ModItems.diamond_gavel)
			Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.gavel_diamond);
		if(item.getItem() == ModItems.mese_gavel)
			Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.gavel_mese);
		
		switch(context.transform) {
		case FIRST_PERSON_LEFT_HAND:
		case FIRST_PERSON_RIGHT_HAND:

			GL11.glTranslated(1, 0.5, 0);
			
			if(NativeToolItemPerspectives.isBlocking(item, context)) {
				GL11.glTranslated(-0.5, 0, 0);
			}
			
			GL11.glRotated(45, 0, 0, 1);
			GL11.glRotated(90, 0, 1, 0);
			
			if(item.getItem() == ModItems.mese_gavel)
				GL11.glScaled(2, 2, 2);
			
			break;

		case NONE:
		case HEAD:
		case FIXED:
		case GROUND:
			GL11.glTranslated(-0.5, 0, 0);
		case THIRD_PERSON_LEFT_HAND:
		case THIRD_PERSON_RIGHT_HAND:

			GL11.glScaled(0.5, 0.5, 0.5);
			GL11.glRotated(45, 0, 0, 1);
			GL11.glTranslated(1.375, 0, 0);
			GL11.glRotated(90, 0, 1, 0);
			
			if(item.getItem() == ModItems.mese_gavel) {
				GL11.glScaled(2, 2, 2);
				GL11.glTranslated(0, 0.25, 0);
			}
			
			break;

		default: break;
		}
		
		GL11.glShadeModel(GL11.GL_SMOOTH);
		ResourceManager.gavel.renderAll();
		GL11.glShadeModel(GL11.GL_FLAT);
		
		GL11.glPopMatrix();
	}
}
