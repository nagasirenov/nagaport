package com.hbm.render.util;

import org.lwjgl.opengl.GL11;

import com.hbm.lib.RefStrings;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;

public class GaugeUtil {
	
	public static enum Gauge {
		
		ROUND_SMALL(new ResourceLocation(RefStrings.MODID + ":textures/gui/gauges/small_round.png"), 18, 18, 13),
		ROUND_LARGE(new ResourceLocation(RefStrings.MODID + ":textures/gui/gauges/large_round.png"), 36, 36, 13),
		BOW_SMALL(new ResourceLocation(RefStrings.MODID + ":textures/gui/gauges/small_bow.png"), 18, 18, 13),
		BOW_LARGE(new ResourceLocation(RefStrings.MODID + ":textures/gui/gauges/large_bow.png"), 36, 36, 13),
		WIDE_SMALL(new ResourceLocation(RefStrings.MODID + ":textures/gui/gauges/small_wide.png"), 18, 12, 7),
		WIDE_LARGE(new ResourceLocation(RefStrings.MODID + ":textures/gui/gauges/large_wide.png"), 36, 24, 11),
		BAR_SMALL(new ResourceLocation(RefStrings.MODID + ":textures/gui/gauges/small_bar.png"), 36, 12, 16);
		
		ResourceLocation texture;
		int width;
		int height;
		int count;
		
		private Gauge(ResourceLocation texture, int width, int height, int count) {
			this.texture = texture;
			this.width = width;
			this.height = height;
			this.count = count;
		}
	}
	
	/**
	 * 
	 * @param gauge The gauge enum to use
	 * @param x The x coord in the GUI (left)
	 * @param y The y coord in the GUI (top)
	 * @param z The z-level (from GUI.zLevel)
	 * @param progress Double from 0-1 how far the gauge has progressed
	 */
	public static void renderGauge(Gauge gauge, double x, double y, double z, double progress) {
		
		Minecraft.getMinecraft().renderEngine.bindTexture(gauge.texture);
		
		int frameNum = (int) Math.round((gauge.count - 1) * progress);
		double singleFrame = 1D / (double)gauge.count;
		double frameOffset = singleFrame * frameNum;
		
		Tessellator tess = Tessellator.instance;
		tess.startDrawingQuads();
		tess.addVertexWithUV(x, 				y + gauge.height, 	z, 	0, 	frameOffset + singleFrame);
		tess.addVertexWithUV(x + gauge.width, 	y + gauge.height, 	z, 	1, 	frameOffset + singleFrame);
		tess.addVertexWithUV(x + gauge.width, 	y, 					z, 	1, 	frameOffset);
		tess.addVertexWithUV(x, 				y, 					z, 	0, 	frameOffset);
		tess.draw();
	}
	
	public static void drawSmoothGauge(int x, int y, double z, double progress, double tipLength, double backLength, double backSide, int color) {
		drawSmoothGauge(x, y, z, progress, tipLength, backLength, backSide, color, 0x000000);
	}
	
	public static void drawSmoothGauge(int x, int y, double z, double progress, double tipLength, double backLength, double backSide, int color, int colorOuter) {
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		
		progress = MathHelper.clamp(progress, (double) (0), (double) (1));
		
		float angle = (float) Math.toRadians(-progress * 270 - 45);
		com.hbm.util.Vec3NT tip = new com.hbm.util.Vec3NT(0, tipLength, 0).rotateAroundZRad(angle);
		com.hbm.util.Vec3NT left = new com.hbm.util.Vec3NT(backSide, -backLength, 0).rotateAroundZRad(angle);
		com.hbm.util.Vec3NT right = new com.hbm.util.Vec3NT(-backSide, -backLength, 0).rotateAroundZRad(angle);

		
		Tessellator tess = Tessellator.instance;
		tess.startDrawing(GL11.GL_TRIANGLES);
		tess.setColorOpaque_I(colorOuter);
		double mult = 1.5;
		tess.addVertex(x + tip.x * mult, y + tip.y * mult, z);
		tess.addVertex(x + left.x * mult, y + left.y * mult, z);
		tess.addVertex(x + right.x * mult, y + right.y * mult, z);
		tess.setColorOpaque_I(color);
		tess.addVertex(x + tip.x, y + tip.y, z);
		tess.addVertex(x + left.x, y + left.y, z);
		tess.addVertex(x + right.x, y + right.y, z);
		tess.draw();
		
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}
}
