package com.hbm.render.util;

import java.util.Random;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/** State writes also update the native cache, but remain unconditional for
 * existing raw-GL callers. Do not remove the raw write before its cache update. */
public class BeamPronter {
	
	public static Random rand = new Random();

	public static enum EnumWaveType {
		RANDOM, SPIRAL
	}

	public static enum EnumBeamType {
		SOLID, LINE
	}
	
	private static boolean depthMask = false;

	public static void prontBeamwithDepth(Vec3d skeleton, EnumWaveType wave, EnumBeamType beam, int outerColor, int innerColor, int start, int segments, float size, int layers, float thickness) {
		prontBeamwithDepth(skeleton, wave, beam, outerColor, innerColor, start, segments, size, layers, thickness, 1F);
	}

	public static void prontBeamwithDepth(Vec3d skeleton, EnumWaveType wave, EnumBeamType beam, int outerColor, int innerColor, int start, int segments, float size, int layers, float thickness, float alpha) {
		depthMask = true;
		prontBeam(skeleton, wave, beam, outerColor, innerColor, start, segments, size, layers, thickness, alpha);
		depthMask = false;
	}

	public static void prontBeam(Vec3d skeleton, EnumWaveType wave, EnumBeamType beam, int outerColor, int innerColor, int start, int segments, float size, int layers, float thickness) {
		prontBeam(skeleton, wave, beam, outerColor, innerColor, start, segments, size, layers, thickness, 1F);
	}

	public static void prontBeam(Vec3d skeleton, EnumWaveType wave, EnumBeamType beam, int outerColor, int innerColor, int start, int segments, float size, int layers, float thickness, float alpha) {

		GL11.glPushMatrix();
		GL11.glDepthMask(depthMask); GlStateManager.depthMask(depthMask);

		float sYaw = (float) (Math.atan2(skeleton.x, skeleton.z) * 180F / Math.PI);
		float sqrt = MathHelper.sqrt(skeleton.x * skeleton.x + skeleton.z * skeleton.z);
		float sPitch = (float) (Math.atan2(skeleton.y, (double) sqrt) * 180F / Math.PI);

		GL11.glRotatef(180, 0, 1F, 0);
		GL11.glRotatef(sYaw, 0, 1F, 0);
		GL11.glRotatef(sPitch - 90, 1F, 0, 0);

		GL11.glPushMatrix();
		GL11.glDisable(GL11.GL_TEXTURE_2D); GlStateManager.disableTexture2D();
		GL11.glDisable(GL11.GL_LIGHTING); GlStateManager.disableLighting();

		if(beam == EnumBeamType.SOLID) {
			GL11.glDisable(GL11.GL_CULL_FACE); GlStateManager.disableCull();

			GL11.glEnable(GL11.GL_BLEND); GlStateManager.enableBlend();

			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE); GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		}

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();

		Vec3d unit = new Vec3d(0, 1, 0);
		rand.setSeed(start);
		double length = skeleton.length();
		double segLength = length / segments;
		double lastX = 0;
		double lastY = 0;
		double lastZ = 0;

		for(int i = 0; i <= segments; i++) {

			Vec3d spinner = new Vec3d(size, 0, 0);

			if(wave == EnumWaveType.SPIRAL) {
				spinner = spinner.rotateYaw((float) Math.PI * (float) start / 180F);
				spinner = spinner.rotateYaw((float) Math.PI * 45F / 180F * i);
			} else if(wave == EnumWaveType.RANDOM) {
				spinner = spinner.rotateYaw((float) Math.PI * 2 * rand.nextFloat());
				spinner = spinner.rotateYaw((float) Math.PI * 2 * rand.nextFloat());
			}

			double pX = unit.x * segLength * i + spinner.x;
			double pY = unit.y * segLength * i + spinner.y;
			double pZ = unit.z * segLength * i + spinner.z;

			if(beam == EnumBeamType.LINE && i > 0) {

				buffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
				buffer.pos(pX, pY, pZ).color(outerColor >> 16 & 255, outerColor >> 8 & 255, outerColor & 255, 255).endVertex();
				buffer.pos(lastX, lastY, lastZ).color(outerColor >> 16 & 255, outerColor >> 8 & 255, outerColor & 255, 255).endVertex();
				tessellator.draw();
			}

			if(beam == EnumBeamType.SOLID && i > 0) {

				float radius = thickness / layers;

				for(int j = 1; j <= layers; j++) {

					float inter = (float) (j - 1) / (float) (layers - 1);

					int r1 = ((outerColor & 0xFF0000) >> 16);
					int g1 = ((outerColor & 0x00FF00) >> 8);
					int b1 = ((outerColor & 0x0000FF) >> 0);
					
					int r2 = ((innerColor & 0xFF0000) >> 16);
					int g2 = ((innerColor & 0x00FF00) >> 8);
					int b2 = ((innerColor & 0x0000FF) >> 0);

					int r = ((int)(r1 + (r2 - r1) * inter)) << 16;
					int g = ((int)(g1 + (g2 - g1) * inter)) << 8;
					int b = ((int)(b1 + (b2 - b1) * inter)) << 0;
					
					int color = r | g | b;

					buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
					setColorWithAlpha(tessellator, color, alpha);
					buffer.pos(lastX + (radius * j), lastY, lastZ + (radius * j)).endVertex();
					buffer.pos(lastX + (radius * j), lastY, lastZ - (radius * j)).endVertex();
					buffer.pos(pX + (radius * j), pY, pZ - (radius * j)).endVertex();
					buffer.pos(pX + (radius * j), pY, pZ + (radius * j)).endVertex();
					tessellator.draw();
					buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
					setColorWithAlpha(tessellator, color, alpha);
					buffer.pos(lastX - (radius * j), lastY, lastZ + (radius * j)).endVertex();
					buffer.pos(lastX - (radius * j), lastY, lastZ - (radius * j)).endVertex();
					buffer.pos(pX - (radius * j), pY, pZ - (radius * j)).endVertex();
					buffer.pos(pX - (radius * j), pY, pZ + (radius * j)).endVertex();
					tessellator.draw();
					buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
					setColorWithAlpha(tessellator, color, alpha);
					buffer.pos(lastX + (radius * j), lastY, lastZ + (radius * j)).endVertex();
					buffer.pos(lastX - (radius * j), lastY, lastZ + (radius * j)).endVertex();
					buffer.pos(pX - (radius * j), pY, pZ + (radius * j)).endVertex();
					buffer.pos(pX + (radius * j), pY, pZ + (radius * j)).endVertex();
					tessellator.draw();
					buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
					setColorWithAlpha(tessellator, color, alpha);
					buffer.pos(lastX + (radius * j), lastY, lastZ - (radius * j)).endVertex();
					buffer.pos(lastX - (radius * j), lastY, lastZ - (radius * j)).endVertex();
					buffer.pos(pX - (radius * j), pY, pZ - (radius * j)).endVertex();
					buffer.pos(pX + (radius * j), pY, pZ - (radius * j)).endVertex();
					tessellator.draw();
					
					
				}
			}

			lastX = pX;
			lastY = pY;
			lastZ = pZ;
		}

		if(beam == EnumBeamType.LINE) {

			buffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
			buffer.pos(0, 0, 0).color(innerColor >> 16 & 255, innerColor >> 8 & 255, innerColor & 255, 255).endVertex();
			buffer.pos(0, skeleton.length(), 0).color(innerColor >> 16 & 255, innerColor >> 8 & 255, innerColor & 255, 255).endVertex();
			tessellator.draw();
		}

		if(beam == EnumBeamType.SOLID) {
			GL11.glDisable(GL11.GL_BLEND); GlStateManager.disableBlend();
			GL11.glEnable(GL11.GL_TEXTURE_2D); GlStateManager.enableTexture2D();

		}

		GL11.glEnable(GL11.GL_LIGHTING); GlStateManager.enableLighting();
		GL11.glEnable(GL11.GL_TEXTURE_2D); GlStateManager.enableTexture2D();
		
		GL11.glPopMatrix();
		GL11.glDepthMask(true); GlStateManager.depthMask(true);

		GL11.glPopMatrix();
	}
    private static void setColorWithAlpha(Tessellator tessellator, int color, float alpha) {
        float red = ((color >> 16) & 0xFF) / 255.0f;
        float green = ((color >> 8) & 0xFF) / 255.0f;
        float blue = (color & 0xFF) / 255.0f;
        
        GL11.glColor4f(red, green, blue, alpha); GlStateManager.color(red, green, blue, alpha);
    }
}
