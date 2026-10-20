package com.hbm.render.util;

import java.util.Random;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.Vec3d;

/** State writes also update the native cache, but remain unconditional for
 * existing raw-GL callers. Do not remove the raw write before its cache update. */
public class RenderSparks {
	
	public static void renderSpark(int seed, double x, double y, double z, float length, int min, int max, int color1, int color2) {

        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_TEXTURE_2D); GlStateManager.disableTexture2D();
        GL11.glDisable(GL11.GL_LIGHTING); GlStateManager.disableLighting();
        GL11.glLineWidth(3F);
        Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
        
		Random rand = new Random(seed);
		Vec3d vec = new Vec3d(rand.nextDouble() - 0.5, rand.nextDouble() - 0.5, rand.nextDouble() - 0.5);
		vec = vec.normalize();

		double prevX;
		double prevY;
		double prevZ;
		
		for(int i = 0; i < min + rand.nextInt(max); i++) {

			prevX = x;
			prevY = y;
			prevZ = z;
			
			Vec3d dir = vec.normalize();
			dir = new Vec3d(dir.x * (length * rand.nextFloat()), dir.y * (length * rand.nextFloat()), dir.z * (length * rand.nextFloat()));

			x = prevX + dir.x;
			y = prevY + dir.y;
			z = prevZ + dir.z;

	        GL11.glLineWidth(5F);
            buffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
            buffer.pos(prevX, prevY, prevZ).color(color1 >> 16 & 255, color1 >> 8 & 255, color1 & 255, 255).endVertex();
            buffer.pos(x, y, z).color(color1 >> 16 & 255, color1 >> 8 & 255, color1 & 255, 255).endVertex();
            tessellator.draw();
	        GL11.glLineWidth(2F);
            buffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
            buffer.pos(prevX, prevY, prevZ).color(color2 >> 16 & 255, color2 >> 8 & 255, color2 & 255, 255).endVertex();
            buffer.pos(x, y, z).color(color2 >> 16 & 255, color2 >> 8 & 255, color2 & 255, 255).endVertex();
            tessellator.draw();
		}

        GL11.glEnable(GL11.GL_LIGHTING); GlStateManager.enableLighting();
        GL11.glEnable(GL11.GL_TEXTURE_2D); GlStateManager.enableTexture2D();
        GL11.glPopMatrix();
	}

}
