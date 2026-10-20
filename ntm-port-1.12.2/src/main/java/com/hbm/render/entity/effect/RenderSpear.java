package com.hbm.render.entity.effect;

import java.util.Random;

import org.lwjgl.opengl.GL11;

import com.hbm.entity.effect.EntitySpear;
import com.hbm.main.ResourceManager;

import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public class RenderSpear extends Render {

	public RenderSpear(RenderManager renderManager) {
		super(renderManager);
	}

	@Override
	public void doRender(Entity entity, double x, double y, double z, float f, float interp) {
		
		GL11.glPushMatrix();
		GL11.glTranslatef((float) x, (float) y + 15, (float) z);
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_CULL_FACE);
		
		GL11.glRotated(180, 1, 0, 0);
		GL11.glScaled(2, 2, 2);
		
		EntitySpear spear = (EntitySpear) entity;
		
		GL11.glShadeModel(GL11.GL_SMOOTH);
		bindTexture(ResourceManager.lance_tex);
		ResourceManager.lance.renderPart("Spear");
		
		if(spear.ticksInGround > 0) {
			float occupancy = Math.min((spear.ticksInGround + interp) / 100F, 1F);
			GL11.glColor4f(1F, 1F, 1F, occupancy);

			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glAlphaFunc(GL11.GL_GEQUAL, 0.0F);
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			
			OpenGlHelper.glBlendFunc(770, 771, 1, 0);
			
			ResourceManager.lance.renderPart("Spear");
			
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
			GL11.glDisable(GL11.GL_BLEND);
			GL11.glEnable(GL11.GL_LIGHTING);
			
			GL11.glColor4f(1F, 1F, 1F, 1F);
			
			renderFlash((spear.ticksInGround + interp) / 200D);
		}
		
		GL11.glShadeModel(GL11.GL_FLAT);
		
		GL11.glPopMatrix();
	}
	
	private void renderFlash(double intensity) {

		GL11.glScalef(0.2F, 0.2F, 0.2F);

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder tessellatorBuffer = tessellator.getBuffer();
		RenderHelper.disableStandardItemLighting();

		Random random = new Random(432L);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glShadeModel(GL11.GL_SMOOTH);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glDepthMask(false);

		GL11.glPushMatrix();

		float scale = 25;

		for(int i = 0; i < 64; i++) {

			GL11.glRotatef(random.nextFloat() * 360.0F, 1.0F, 0.0F, 0.0F);
			GL11.glRotatef(random.nextFloat() * 360.0F, 0.0F, 1.0F, 0.0F);
			GL11.glRotatef(random.nextFloat() * 360.0F, 0.0F, 0.0F, 1.0F);
			GL11.glRotatef(random.nextFloat() * 360.0F, 1.0F, 0.0F, 0.0F);
			GL11.glRotatef(random.nextFloat() * 360.0F, 0.0F, 1.0F, 0.0F);

			float vert1 = (random.nextFloat() * 20.0F + 5.0F + 1 * 10.0F) * (float) (intensity * intensity * scale);
			float vert2 = (random.nextFloat() * 2.0F + 1.0F + 1 * 2.0F) * (float) (intensity * intensity * scale);

			tessellatorBuffer.begin(6, DefaultVertexFormats.POSITION_COLOR);

			int tessellatorColor0Red = Math.max(0, Math.min(255, (int) ((1.0F) * 255.0F)));
			int tessellatorColor0Green = Math.max(0, Math.min(255, (int) ((0.6F) * 255.0F)));
			int tessellatorColor0Blue = Math.max(0, Math.min(255, (int) ((0.6F) * 255.0F)));
			int tessellatorColor0Alpha = Math.max(0, Math.min(255, (int) (((float) (intensity * intensity) * 2F) * 255.0F)));
			tessellatorBuffer.pos(0.0D, 0.0D, 0.0D).color(tessellatorColor0Red, tessellatorColor0Green, tessellatorColor0Blue, tessellatorColor0Alpha).endVertex();
			int tessellatorColor1Red = Math.max(0, Math.min(255, (int) ((1.0F) * 255.0F)));
			int tessellatorColor1Green = Math.max(0, Math.min(255, (int) ((0.6F) * 255.0F)));
			int tessellatorColor1Blue = Math.max(0, Math.min(255, (int) ((0.6F) * 255.0F)));
			int tessellatorColor1Alpha = Math.max(0, Math.min(255, (int) ((0.0F) * 255.0F)));
			tessellatorBuffer.pos(-0.866D * vert2, vert1, -0.5F * vert2).color(tessellatorColor1Red, tessellatorColor1Green, tessellatorColor1Blue, tessellatorColor1Alpha).endVertex();
			tessellatorBuffer.pos(0.866D * vert2, vert1, -0.5F * vert2).color(tessellatorColor1Red, tessellatorColor1Green, tessellatorColor1Blue, tessellatorColor1Alpha).endVertex();
			tessellatorBuffer.pos(0.0D, vert1, 1.0F * vert2).color(tessellatorColor1Red, tessellatorColor1Green, tessellatorColor1Blue, tessellatorColor1Alpha).endVertex();
			tessellatorBuffer.pos(-0.866D * vert2, vert1, -0.5F * vert2).color(tessellatorColor1Red, tessellatorColor1Green, tessellatorColor1Blue, tessellatorColor1Alpha).endVertex();
			tessellator.draw();
		}

		GL11.glPopMatrix();

		GL11.glDepthMask(true);
		GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glShadeModel(GL11.GL_FLAT);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		RenderHelper.enableStandardItemLighting();
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity entity) {
		return ResourceManager.lance_tex;
	}
}
