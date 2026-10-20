package com.hbm.render.entity.projectile;

import org.lwjgl.opengl.GL11;

import com.hbm.entity.projectile.EntitySiegeLaser;
import com.hbm.main.ResourceManager;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public class RenderSiegeLaser extends Render {

	public RenderSiegeLaser(RenderManager renderManager) {
		super(renderManager);
	}

	@Override
	public void doRender(Entity laser, double x, double y, double z, float f0, float f1) {
		
		GL11.glPushMatrix();
		GL11.glTranslatef((float) x, (float) y, (float) z);
		GL11.glRotatef(laser.prevRotationYaw + (laser.rotationYaw - laser.prevRotationYaw) * f1 - 90.0F, 0.0F, 1.0F, 0.0F);
		GL11.glRotatef(laser.prevRotationPitch + (laser.rotationPitch - laser.prevRotationPitch) * f1 + 180, 0.0F, 0.0F, 1.0F);
		
		this.renderDart((EntitySiegeLaser) laser);
		
		GL11.glPopMatrix();
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity entity) {
		return ResourceManager.universal;
	}
	
	private void renderDart(EntitySiegeLaser laser) {

		GL11.glPushMatrix();
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glShadeModel(GL11.GL_SMOOTH);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		GL11.glDepthMask(false);

		GL11.glScalef(1F / 4F, 1F / 8F, 1F / 8F);
		GL11.glScalef(-1, 1, 1);

		GL11.glScalef(2, 2, 2);
		
		int color = laser.getColor();

		Tessellator tess = Tessellator.getInstance();
		BufferBuilder tessBuffer = tess.getBuffer();

		// front
		tessBuffer.begin(4, DefaultVertexFormats.POSITION_COLOR);
		int tessColor0Packed = color;
		tessBuffer.pos(6, 0, 0).color((tessColor0Packed >> 16) & 255, (tessColor0Packed >> 8) & 255, tessColor0Packed & 255, 255).endVertex();
		int tessColor1Packed = color;
		int tessColor1Alpha = Math.max(0, Math.min(255, 0));
		tessBuffer.pos(3, -1, -1).color((tessColor1Packed >> 16) & 255, (tessColor1Packed >> 8) & 255, tessColor1Packed & 255, tessColor1Alpha).endVertex();
		tessBuffer.pos(3, 1, -1).color((tessColor1Packed >> 16) & 255, (tessColor1Packed >> 8) & 255, tessColor1Packed & 255, tessColor1Alpha).endVertex();

		int tessColor2Packed = color;
		int tessColor2Alpha = Math.max(0, Math.min(255, 0));
		tessBuffer.pos(3, -1, 1).color((tessColor2Packed >> 16) & 255, (tessColor2Packed >> 8) & 255, tessColor2Packed & 255, tessColor2Alpha).endVertex();
		int tessColor3Packed = color;
		tessBuffer.pos(6, 0, 0).color((tessColor3Packed >> 16) & 255, (tessColor3Packed >> 8) & 255, tessColor3Packed & 255, 255).endVertex();
		int tessColor4Packed = color;
		int tessColor4Alpha = Math.max(0, Math.min(255, 0));
		tessBuffer.pos(3, 1, 1).color((tessColor4Packed >> 16) & 255, (tessColor4Packed >> 8) & 255, tessColor4Packed & 255, tessColor4Alpha).endVertex();

		int tessColor5Packed = color;
		int tessColor5Alpha = Math.max(0, Math.min(255, 0));
		tessBuffer.pos(3, -1, -1).color((tessColor5Packed >> 16) & 255, (tessColor5Packed >> 8) & 255, tessColor5Packed & 255, tessColor5Alpha).endVertex();
		int tessColor6Packed = color;
		tessBuffer.pos(6, 0, 0).color((tessColor6Packed >> 16) & 255, (tessColor6Packed >> 8) & 255, tessColor6Packed & 255, 255).endVertex();
		int tessColor7Packed = color;
		int tessColor7Alpha = Math.max(0, Math.min(255, 0));
		tessBuffer.pos(3, -1, 1).color((tessColor7Packed >> 16) & 255, (tessColor7Packed >> 8) & 255, tessColor7Packed & 255, tessColor7Alpha).endVertex();

		int tessColor8Packed = color;
		tessBuffer.pos(6, 0, 0).color((tessColor8Packed >> 16) & 255, (tessColor8Packed >> 8) & 255, tessColor8Packed & 255, 255).endVertex();
		int tessColor9Packed = color;
		int tessColor9Alpha = Math.max(0, Math.min(255, 0));
		tessBuffer.pos(3, 1, -1).color((tessColor9Packed >> 16) & 255, (tessColor9Packed >> 8) & 255, tessColor9Packed & 255, tessColor9Alpha).endVertex();
		int tessColor10Packed = color;
		int tessColor10Alpha = Math.max(0, Math.min(255, 0));
		tessBuffer.pos(3, 1, 1).color((tessColor10Packed >> 16) & 255, (tessColor10Packed >> 8) & 255, tessColor10Packed & 255, tessColor10Alpha).endVertex();

		// mid
		int tessColor11Packed = color;
		tessBuffer.pos(6, 0, 0).color((tessColor11Packed >> 16) & 255, (tessColor11Packed >> 8) & 255, tessColor11Packed & 255, 255).endVertex();
		tessBuffer.pos(4, -0.5, -0.5).color((tessColor11Packed >> 16) & 255, (tessColor11Packed >> 8) & 255, tessColor11Packed & 255, 255).endVertex();
		tessBuffer.pos(4, 0.5, -0.5).color((tessColor11Packed >> 16) & 255, (tessColor11Packed >> 8) & 255, tessColor11Packed & 255, 255).endVertex();

		int tessColor12Packed = color;
		tessBuffer.pos(4, -0.5, 0.5).color((tessColor12Packed >> 16) & 255, (tessColor12Packed >> 8) & 255, tessColor12Packed & 255, 255).endVertex();
		tessBuffer.pos(6, 0, 0).color((tessColor12Packed >> 16) & 255, (tessColor12Packed >> 8) & 255, tessColor12Packed & 255, 255).endVertex();
		tessBuffer.pos(4, 0.5, 0.5).color((tessColor12Packed >> 16) & 255, (tessColor12Packed >> 8) & 255, tessColor12Packed & 255, 255).endVertex();

		int tessColor13Packed = color;
		tessBuffer.pos(4, -0.5, -0.5).color((tessColor13Packed >> 16) & 255, (tessColor13Packed >> 8) & 255, tessColor13Packed & 255, 255).endVertex();
		tessBuffer.pos(6, 0, 0).color((tessColor13Packed >> 16) & 255, (tessColor13Packed >> 8) & 255, tessColor13Packed & 255, 255).endVertex();
		tessBuffer.pos(4, -0.5, 0.5).color((tessColor13Packed >> 16) & 255, (tessColor13Packed >> 8) & 255, tessColor13Packed & 255, 255).endVertex();

		int tessColor14Packed = color;
		tessBuffer.pos(6, 0, 0).color((tessColor14Packed >> 16) & 255, (tessColor14Packed >> 8) & 255, tessColor14Packed & 255, 255).endVertex();
		tessBuffer.pos(4, 0.5, -0.5).color((tessColor14Packed >> 16) & 255, (tessColor14Packed >> 8) & 255, tessColor14Packed & 255, 255).endVertex();
		tessBuffer.pos(4, 0.5, 0.5).color((tessColor14Packed >> 16) & 255, (tessColor14Packed >> 8) & 255, tessColor14Packed & 255, 255).endVertex();
		tess.draw();

		// tail
		tessBuffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
		int tessColor15Packed = color;
		tessBuffer.pos(4, 0.5, -0.5).color((tessColor15Packed >> 16) & 255, (tessColor15Packed >> 8) & 255, tessColor15Packed & 255, 255).endVertex();
		tessBuffer.pos(4, 0.5, 0.5).color((tessColor15Packed >> 16) & 255, (tessColor15Packed >> 8) & 255, tessColor15Packed & 255, 255).endVertex();
		int tessColor16Packed = color;
		int tessColor16Alpha = Math.max(0, Math.min(255, 0));
		tessBuffer.pos(0, 0.5, 0.5).color((tessColor16Packed >> 16) & 255, (tessColor16Packed >> 8) & 255, tessColor16Packed & 255, tessColor16Alpha).endVertex();
		tessBuffer.pos(0, 0.5, -0.5).color((tessColor16Packed >> 16) & 255, (tessColor16Packed >> 8) & 255, tessColor16Packed & 255, tessColor16Alpha).endVertex();

		int tessColor17Packed = color;
		tessBuffer.pos(4, -0.5, -0.5).color((tessColor17Packed >> 16) & 255, (tessColor17Packed >> 8) & 255, tessColor17Packed & 255, 255).endVertex();
		tessBuffer.pos(4, -0.5, 0.5).color((tessColor17Packed >> 16) & 255, (tessColor17Packed >> 8) & 255, tessColor17Packed & 255, 255).endVertex();
		int tessColor18Packed = color;
		int tessColor18Alpha = Math.max(0, Math.min(255, 0));
		tessBuffer.pos(0, -0.5, 0.5).color((tessColor18Packed >> 16) & 255, (tessColor18Packed >> 8) & 255, tessColor18Packed & 255, tessColor18Alpha).endVertex();
		tessBuffer.pos(0, -0.5, -0.5).color((tessColor18Packed >> 16) & 255, (tessColor18Packed >> 8) & 255, tessColor18Packed & 255, tessColor18Alpha).endVertex();

		int tessColor19Packed = color;
		tessBuffer.pos(4, -0.5, 0.5).color((tessColor19Packed >> 16) & 255, (tessColor19Packed >> 8) & 255, tessColor19Packed & 255, 255).endVertex();
		tessBuffer.pos(4, 0.5, 0.5).color((tessColor19Packed >> 16) & 255, (tessColor19Packed >> 8) & 255, tessColor19Packed & 255, 255).endVertex();
		int tessColor20Packed = color;
		int tessColor20Alpha = Math.max(0, Math.min(255, 0));
		tessBuffer.pos(0, 0.5, 0.5).color((tessColor20Packed >> 16) & 255, (tessColor20Packed >> 8) & 255, tessColor20Packed & 255, tessColor20Alpha).endVertex();
		tessBuffer.pos(0, -0.5, 0.5).color((tessColor20Packed >> 16) & 255, (tessColor20Packed >> 8) & 255, tessColor20Packed & 255, tessColor20Alpha).endVertex();

		int tessColor21Packed = color;
		tessBuffer.pos(4, -0.5, -0.5).color((tessColor21Packed >> 16) & 255, (tessColor21Packed >> 8) & 255, tessColor21Packed & 255, 255).endVertex();
		tessBuffer.pos(4, 0.5, -0.5).color((tessColor21Packed >> 16) & 255, (tessColor21Packed >> 8) & 255, tessColor21Packed & 255, 255).endVertex();
		int tessColor22Packed = color;
		int tessColor22Alpha = Math.max(0, Math.min(255, 0));
		tessBuffer.pos(0, 0.5, -0.5).color((tessColor22Packed >> 16) & 255, (tessColor22Packed >> 8) & 255, tessColor22Packed & 255, tessColor22Alpha).endVertex();
		tessBuffer.pos(0, -0.5, -0.5).color((tessColor22Packed >> 16) & 255, (tessColor22Packed >> 8) & 255, tessColor22Packed & 255, tessColor22Alpha).endVertex();
		tess.draw();

		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glDepthMask(true);

		GL11.glPopMatrix();
	}
}
