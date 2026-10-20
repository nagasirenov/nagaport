package com.hbm.render.entity.projectile;

import java.util.Random;

import org.lwjgl.opengl.GL11;

import com.hbm.lib.RefStrings;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public class RenderBeam5 extends Render {
	
	Random rand = new Random();

	@Override
	public void doRender(Entity rocket, double x, double y, double z, float p_76986_8_, float p_76986_9_) {
		//float radius = 0.12F;
		//float radius = 0.06F;
		float radius = 0.175F;
		int distance = 2;
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder tessellatorBuffer = tessellator.getBuffer();

		GL11.glPushMatrix();
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		GL11.glTranslatef((float) x, (float) y, (float) z);

		GL11.glRotatef(rocket.rotationYaw, 0.0F, 1.0F, 0.0F);
		GL11.glRotatef(-rocket.rotationPitch, 1.0F, 0.0F, 0.0F);

		boolean red = false;
		boolean green = false;
		boolean blue = true;

		for (float o = 0; o <= radius; o += radius / 8) {
			float color = 1f - (o * 8.333f);
			if (color < 0)
				color = 0;
			tessellatorBuffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
			int tessellatorColor0Red = Math.max(0, Math.min(255, (int) ((red ? 1 : color) * 255.0F)));
			int tessellatorColor0Green = Math.max(0, Math.min(255, (int) ((green ? 1 : color) * 255.0F)));
			int tessellatorColor0Blue = Math.max(0, Math.min(255, (int) ((blue ? 1 : color) * 255.0F)));
			int tessellatorColor0Alpha = Math.max(0, Math.min(255, (int) ((1f) * 255.0F)));
			tessellatorBuffer.pos(0 + o, 0 - o, 0).color(tessellatorColor0Red, tessellatorColor0Green, tessellatorColor0Blue, tessellatorColor0Alpha).endVertex();
			tessellatorBuffer.pos(0 + o, 0 + o, 0).color(tessellatorColor0Red, tessellatorColor0Green, tessellatorColor0Blue, tessellatorColor0Alpha).endVertex();
			tessellatorBuffer.pos(0 + o, 0 + o, 0 + distance).color(tessellatorColor0Red, tessellatorColor0Green, tessellatorColor0Blue, tessellatorColor0Alpha).endVertex();
			tessellatorBuffer.pos(0 + o, 0 - o, 0 + distance).color(tessellatorColor0Red, tessellatorColor0Green, tessellatorColor0Blue, tessellatorColor0Alpha).endVertex();
			tessellator.draw();
			tessellatorBuffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
			int tessellatorColor1Red = Math.max(0, Math.min(255, (int) ((red ? 1 : color) * 255.0F)));
			int tessellatorColor1Green = Math.max(0, Math.min(255, (int) ((green ? 1 : color) * 255.0F)));
			int tessellatorColor1Blue = Math.max(0, Math.min(255, (int) ((blue ? 1 : color) * 255.0F)));
			int tessellatorColor1Alpha = Math.max(0, Math.min(255, (int) ((1f) * 255.0F)));
			tessellatorBuffer.pos(0 - o, 0 - o, 0).color(tessellatorColor1Red, tessellatorColor1Green, tessellatorColor1Blue, tessellatorColor1Alpha).endVertex();
			tessellatorBuffer.pos(0 + o, 0 - o, 0).color(tessellatorColor1Red, tessellatorColor1Green, tessellatorColor1Blue, tessellatorColor1Alpha).endVertex();
			tessellatorBuffer.pos(0 + o, 0 - o, 0 + distance).color(tessellatorColor1Red, tessellatorColor1Green, tessellatorColor1Blue, tessellatorColor1Alpha).endVertex();
			tessellatorBuffer.pos(0 - o, 0 - o, 0 + distance).color(tessellatorColor1Red, tessellatorColor1Green, tessellatorColor1Blue, tessellatorColor1Alpha).endVertex();
			tessellator.draw();
			tessellatorBuffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
			int tessellatorColor2Red = Math.max(0, Math.min(255, (int) ((red ? 1 : color) * 255.0F)));
			int tessellatorColor2Green = Math.max(0, Math.min(255, (int) ((green ? 1 : color) * 255.0F)));
			int tessellatorColor2Blue = Math.max(0, Math.min(255, (int) ((blue ? 1 : color) * 255.0F)));
			int tessellatorColor2Alpha = Math.max(0, Math.min(255, (int) ((1f) * 255.0F)));
			tessellatorBuffer.pos(0 - o, 0 + o, 0).color(tessellatorColor2Red, tessellatorColor2Green, tessellatorColor2Blue, tessellatorColor2Alpha).endVertex();
			tessellatorBuffer.pos(0 - o, 0 - o, 0).color(tessellatorColor2Red, tessellatorColor2Green, tessellatorColor2Blue, tessellatorColor2Alpha).endVertex();
			tessellatorBuffer.pos(0 - o, 0 - o, 0 + distance).color(tessellatorColor2Red, tessellatorColor2Green, tessellatorColor2Blue, tessellatorColor2Alpha).endVertex();
			tessellatorBuffer.pos(0 - o, 0 + o, 0 + distance).color(tessellatorColor2Red, tessellatorColor2Green, tessellatorColor2Blue, tessellatorColor2Alpha).endVertex();
			tessellator.draw();
			tessellatorBuffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
			int tessellatorColor3Red = Math.max(0, Math.min(255, (int) ((red ? 1 : color) * 255.0F)));
			int tessellatorColor3Green = Math.max(0, Math.min(255, (int) ((green ? 1 : color) * 255.0F)));
			int tessellatorColor3Blue = Math.max(0, Math.min(255, (int) ((blue ? 1 : color) * 255.0F)));
			int tessellatorColor3Alpha = Math.max(0, Math.min(255, (int) ((1f) * 255.0F)));
			tessellatorBuffer.pos(0 + o, 0 + o, 0).color(tessellatorColor3Red, tessellatorColor3Green, tessellatorColor3Blue, tessellatorColor3Alpha).endVertex();
			tessellatorBuffer.pos(0 - o, 0 + o, 0).color(tessellatorColor3Red, tessellatorColor3Green, tessellatorColor3Blue, tessellatorColor3Alpha).endVertex();
			tessellatorBuffer.pos(0 - o, 0 + o, 0 + distance).color(tessellatorColor3Red, tessellatorColor3Green, tessellatorColor3Blue, tessellatorColor3Alpha).endVertex();
			tessellatorBuffer.pos(0 + o, 0 + o, 0 + distance).color(tessellatorColor3Red, tessellatorColor3Green, tessellatorColor3Blue, tessellatorColor3Alpha).endVertex();
			tessellator.draw();
		}
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glPopMatrix();
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity p_110775_1_) {
		return new ResourceLocation(RefStrings.MODID + ":textures/models/PlasmaBeam.png");
	}
}
