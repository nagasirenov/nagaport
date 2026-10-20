package com.hbm.render.item.weapon;

import org.lwjgl.opengl.GL11;

import com.hbm.main.ResourceManager;
import com.hbm.render.anim.HbmAnimations;


import net.minecraft.client.Minecraft;

import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import com.hbm.render.item.nativeitem.context.*;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;

public class ItemRenderCrucible extends NativeOriginalItemRenderer {
	
	public ItemRenderCrucible() { super(true, true); }

	@Override
	protected void drawModel(ItemStack item, NativeItemContext context) {
		GL11.glPushMatrix();
		
		net.minecraft.entity.EntityLivingBase player = context.holder;
		GL11.glShadeModel(GL11.GL_SMOOTH);
		
		boolean isOn = item.getItemDamage() < item.getMaxDamage();
		
		switch(context.transform) {
		case FIRST_PERSON_LEFT_HAND:
		case FIRST_PERSON_RIGHT_HAND:

			GL11.glTranslated(1.5, -0.3, 0);
			
			if(NativeToolItemPerspectives.isBlocking(item, context)) {
				GL11.glTranslated(-0.125, -0.25, 0);
			}
			
			if(player != null && player == Minecraft.getMinecraft().player) player.isSwingInProgress = false;

			float[] equip = NativeWeaponHandState.snapEquip(item, context);
			float prevEq = equip[0], eq = equip[1];

			GL11.glScaled(0.3, 0.3, 0.3);
			
			GL11.glRotated(45, 0, 0, 1);
			GL11.glRotated(90, 0, 1, 0);
			
			boolean isSwing = false;

			if(!NativeToolItemPerspectives.isBlocking(item, context)) {
				double[] sRot = HbmAnimations.getRelevantTransformation(item, context.holder, "SWING_ROT");
				double[] sTrans = HbmAnimations.getRelevantTransformation(item, context.holder, "SWING_TRANS");
				GL11.glTranslated(sTrans[0], sTrans[1], sTrans[2]);
				GL11.glRotated(sRot[0], 1, 0, 0);
				GL11.glRotated(sRot[2], 0, 0, 1);
				GL11.glRotated(sRot[1], 0, 1, 0);
				
				if(sRot[0] != 0)
					isSwing = true;
			}
			
			double[] rot = HbmAnimations.getRelevantTransformation(item, context.holder, "GUARD_ROT");
			Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.crucible_hilt);
			ResourceManager.crucible.renderPart("Hilt");

			Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.crucible_guard);
			double rotGuard = rot[0];
			
			if(!isSwing && !isOn)
				rotGuard = 90;
			
			GL11.glPushMatrix();
				GL11.glTranslated(0, 3, 0.5);
				GL11.glRotated(rotGuard, -1, 0, 0);
				GL11.glTranslated(0, -3, -0.5);
			ResourceManager.crucible.renderPart("GuardLeft");
			GL11.glPopMatrix();
			
			GL11.glPushMatrix();
				GL11.glTranslated(0, 3, -0.5);
				GL11.glRotated(rotGuard, 1, 0, 0);
				GL11.glTranslated(0, -3, 0.5);
			ResourceManager.crucible.renderPart("GuardRight");
			GL11.glPopMatrix();
			
			if(eq == 1.0F && prevEq == 1.0F && rot[2] == 0 && (isSwing || isOn)) {
				GL11.glPushMatrix();
				GL11.glPushAttrib(GL11.GL_LIGHTING_BIT);

				GL11.glDisable(GL11.GL_LIGHTING);
				GL11.glDisable(GL11.GL_CULL_FACE);
				OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);
				GL11.glTranslated(0.005, 0, 0);
				Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.crucible_blade);
				ResourceManager.crucible.renderPart("Blade");
				GL11.glEnable(GL11.GL_LIGHTING);

				GL11.glPopAttrib();
				GL11.glPopMatrix();
			}
			
			break;

		case NONE:
		case HEAD:
		case FIXED:
		case GROUND:
			GL11.glTranslated(-0.75, 0.6, 0);
			GL11.glRotated(-45, 0, 0, 1);
		case THIRD_PERSON_LEFT_HAND:
		case THIRD_PERSON_RIGHT_HAND:

			GL11.glRotated(45, 0, 0, 1);
			GL11.glTranslated(0.75, -0.4, 0);
			GL11.glRotated(90, 0, 1, 0);
			GL11.glScaled(0.15, 0.15, 0.15);

			Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.crucible_hilt);
			ResourceManager.crucible.renderPart("Hilt");
			Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.crucible_guard);
			GL11.glPushMatrix();
			GL11.glTranslated(0, 3, 0.5);
			GL11.glRotated(isOn ? 0 : 90, -1, 0, 0);
			GL11.glTranslated(0, -3, -0.5);
			ResourceManager.crucible.renderPart("GuardLeft");
			GL11.glPopMatrix();

			GL11.glPushMatrix();
			GL11.glTranslated(0, 3, -0.5);
			GL11.glRotated(isOn ? 0 : 90, 1, 0, 0);
			GL11.glTranslated(0, -3, 0.5);
			ResourceManager.crucible.renderPart("GuardRight");
			GL11.glPopMatrix();
			
			if(isOn) {
				GL11.glPushMatrix();
				GL11.glPushAttrib(GL11.GL_LIGHTING_BIT);
	
				GL11.glDisable(GL11.GL_LIGHTING);
				GL11.glDisable(GL11.GL_CULL_FACE);
				OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);
				GL11.glTranslated(0.005, 0, 0);
				Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.crucible_blade);
				ResourceManager.crucible.renderPart("Blade");
				GL11.glEnable(GL11.GL_LIGHTING);
	
				GL11.glPopAttrib();
				GL11.glPopMatrix();
			}
			
			break;
		case GUI:

			GL11.glTranslated(2, 14, 0);
			GL11.glRotated(-135, 0, 0, 1);
			GL11.glRotated(90, 0, 1, 0);
			double scale = 1.5D;
			GL11.glScaled(scale, scale, scale);

			Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.crucible_hilt);
			ResourceManager.crucible.renderPart("Hilt");
			Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.crucible_guard);
			GL11.glPushMatrix();
			GL11.glTranslated(0, 3, 0.5);
			GL11.glRotated(isOn ? 0 : 90, -1, 0, 0);
			GL11.glTranslated(0, -3, -0.5);
			ResourceManager.crucible.renderPart("GuardLeft");
			GL11.glPopMatrix();

			GL11.glPushMatrix();
			GL11.glTranslated(0, 3, -0.5);
			GL11.glRotated(isOn ? 0 : 90, 1, 0, 0);
			GL11.glTranslated(0, -3, 0.5);
			ResourceManager.crucible.renderPart("GuardRight");
			GL11.glPopMatrix();
			GL11.glTranslated(0.005, 0, 0);
			if(isOn) {
				Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.crucible_blade);
				ResourceManager.crucible.renderPart("Blade");
			}
			break;

		default: break;
		}
		
		GL11.glShadeModel(GL11.GL_FLAT);
		
		GL11.glPopMatrix();
	}
}
