package com.hbm.render.item.weapon;

import org.lwjgl.opengl.GL11;
import com.hbm.items.tool.ItemToolAbilityFueled;
import com.hbm.main.ResourceManager;
import com.hbm.render.anim.HbmAnimations;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import com.hbm.render.item.nativeitem.context.*;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;

public class ItemRenderChainsaw extends NativeOriginalItemRenderer {
    public ItemRenderChainsaw() { super(true, true); }

	@Override
	protected void drawModel(ItemStack item, NativeItemContext context) {
		
		GL11.glPushMatrix();
		
		net.minecraft.entity.EntityLivingBase player = context.holder;
		
		GL11.glEnable(GL11.GL_CULL_FACE);
		Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.chainsaw_tex);
		
		switch(context.transform) {
		
		case FIRST_PERSON_LEFT_HAND:
		case FIRST_PERSON_RIGHT_HAND:
			
			if(player != null && player == Minecraft.getMinecraft().player) player.isSwingInProgress = false;
			
			double s0 = 0.35D;
			GL11.glTranslated(0.5, 0.25, -0.25F);
			GL11.glRotated(45, 0, 0, 1);
			GL11.glRotated(80, 0, 1, 0);
			GL11.glScaled(s0, s0, s0);
			
			if(!NativeToolItemPerspectives.isBlocking(item, context)) {
				double[] sRot = HbmAnimations.getRelevantTransformation(item, context.holder, "SWING_ROT");
				double[] sTrans = HbmAnimations.getRelevantTransformation(item, context.holder, "SWING_TRANS");
				GL11.glTranslated(sTrans[0], sTrans[1], sTrans[2]);
				GL11.glRotated(sRot[2], 0, 0, 1);
				GL11.glRotated(sRot[1], 0, 1, 0);
				GL11.glRotated(sRot[0], 1, 0, 0);
			}
			
			break;
			
		case THIRD_PERSON_LEFT_HAND:
		case THIRD_PERSON_RIGHT_HAND:

			double scale = -0.375D;
			GL11.glScaled(scale, scale, scale);
			GL11.glRotated(85, 0, 1, 0);
			GL11.glRotated(135D, 1.0D, 0.0D, 0.0D);
			GL11.glTranslated(-0.125, -2.0, 1.75);
			
			break;
			
		case NONE:
		case HEAD:
		case FIXED:
		case GROUND:

			double s1 = 0.5D;
			GL11.glScaled(s1, s1, s1);
			break;
			
		case GUI:

            GL11.glEnable(GL11.GL_ALPHA_TEST);
			GL11.glEnable(GL11.GL_LIGHTING);
			
			double s = 4D;
			GL11.glTranslated(8, 10, 0);
			GL11.glRotated(-90, 0, 1, 0);
			GL11.glRotated(-135, 1, 0, 0);
			GL11.glScaled(s, s, -s);
			
			break;
			
		default: break;
		}

		ResourceManager.chainsaw.renderPart("Saw");
		
		for(int i = 0; i < 20; i++) {

			double run = ((ItemToolAbilityFueled) item.getItem()).canOperate(item) ? animationTimeMillis() % 100D * 0.25D / 100D : 0.0625D;
			double forward = i * 0.25 + (run) - 2.0625;
			
			GL11.glPushMatrix();

			GL11.glTranslated(0, 0, 1.9375);
			GL11.glTranslated(0, 0.375, 0.5625);
			double angle = MathHelper.clamp(forward, 0, 0.25 * Math.PI);
			GL11.glRotated(angle * 180D / (Math.PI * 0.25), 1, 0, 0);
			GL11.glTranslated(0, -0.375, -0.5625);
			if(forward < 0) GL11.glTranslated(0, 0, forward);
			if(forward > Math.PI * 0.25) GL11.glTranslated(0, 0, forward - Math.PI * 0.25);
			ResourceManager.chainsaw.renderPart("Tooth");
			GL11.glPopMatrix();
		}
		
		GL11.glPopMatrix();
	}
}
