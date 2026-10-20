package com.hbm.render.entity.effect;

import org.lwjgl.opengl.GL11;

import com.hbm.entity.effect.EntityCloudTom;
import com.hbm.main.ResourceManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;

public class RenderCloudTom extends Render {

	public RenderCloudTom(RenderManager renderManager) {
		super(renderManager);
	}

	@Override
	public void doRender(Entity entity, double x, double y, double z, float f0, float f1) {
		
		GL11.glPushMatrix();
		GL11.glTranslated(x, y, z);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        GL11.glDepthMask(false);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
		
		EntityCloudTom blast = (EntityCloudTom)entity;
		
		double scale = blast.age + f0;
		
		int segments = 16;
		float angle = (float) Math.toRadians(360D/segments);
		int height = 20;
		int depth = 20;
		
		Tessellator tess = Tessellator.getInstance();
		BufferBuilder tessBuffer = tess.getBuffer();
		tessBuffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
		
		bindTexture(this.getEntityTexture(blast));
		
        GL11.glMatrixMode(GL11.GL_TEXTURE);
        GL11.glLoadIdentity();

    	float movement = -(Minecraft.getMinecraft().player.ticksExisted + f0) * 0.005F * 10;
    	GL11.glTranslatef(0, movement, 0);
        
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
		
		for(int i = 0; i < segments; i++) {
			
			for(int j = 0; j < 5; j++) {
				
				double mod = 1 - j * 0.025;
				double h = height + j * 10;
				double off = 1D / j;
				
				Vec3d vec = new Vec3d(scale, 0, 0);
				vec = vec.rotateYaw(angle * i);
				double x0 = vec.x * mod;
				double z0 = vec.z * mod;

				int tessColor0Red = Math.max(0, Math.min(255, (int) ((1.0F) * 255.0F)));
				int tessColor0Green = Math.max(0, Math.min(255, (int) ((1.0F) * 255.0F)));
				int tessColor0Blue = Math.max(0, Math.min(255, (int) ((1.0F) * 255.0F)));
				int tessColor0Alpha = Math.max(0, Math.min(255, (int) ((0.0F) * 255.0F)));
				tessBuffer.pos(x0, h, z0).tex(0, 1 + off).color(tessColor0Red, tessColor0Green, tessColor0Blue, tessColor0Alpha).endVertex();
				int tessColor1Red = Math.max(0, Math.min(255, (int) ((1.0F) * 255.0F)));
				int tessColor1Green = Math.max(0, Math.min(255, (int) ((1.0F) * 255.0F)));
				int tessColor1Blue = Math.max(0, Math.min(255, (int) ((1.0F) * 255.0F)));
				int tessColor1Alpha = Math.max(0, Math.min(255, (int) ((1.0F) * 255.0F)));
				tessBuffer.pos(x0, -depth, z0).tex(0, 0 + off).color(tessColor1Red, tessColor1Green, tessColor1Blue, tessColor1Alpha).endVertex();
				
				vec = vec.rotateYaw(angle);
				x0 = vec.x * mod;
				z0 = vec.z * mod;

				int tessColor2Red = Math.max(0, Math.min(255, (int) ((1.0F) * 255.0F)));
				int tessColor2Green = Math.max(0, Math.min(255, (int) ((1.0F) * 255.0F)));
				int tessColor2Blue = Math.max(0, Math.min(255, (int) ((1.0F) * 255.0F)));
				int tessColor2Alpha = Math.max(0, Math.min(255, (int) ((1.0F) * 255.0F)));
				tessBuffer.pos(x0, -depth, z0).tex(1, 0 + off).color(tessColor2Red, tessColor2Green, tessColor2Blue, tessColor2Alpha).endVertex();
				int tessColor3Red = Math.max(0, Math.min(255, (int) ((1.0F) * 255.0F)));
				int tessColor3Green = Math.max(0, Math.min(255, (int) ((1.0F) * 255.0F)));
				int tessColor3Blue = Math.max(0, Math.min(255, (int) ((1.0F) * 255.0F)));
				int tessColor3Alpha = Math.max(0, Math.min(255, (int) ((0.0F) * 255.0F)));
				tessBuffer.pos(x0, h, z0).tex(1, 1 + off).color(tessColor3Red, tessColor3Green, tessColor3Blue, tessColor3Alpha).endVertex();
			}
		}
		
		tess.draw();
		
        GL11.glMatrixMode(GL11.GL_TEXTURE);
        GL11.glDepthMask(true);
        GL11.glLoadIdentity();
        GL11.glMatrixMode(GL11.GL_MODELVIEW);

        GL11.glDepthMask(true);
        GL11.glShadeModel(GL11.GL_FLAT);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glPopMatrix();
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity entity) {
		return ResourceManager.tomblast;
	}

}
