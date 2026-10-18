package com.hbm.render.util;

import java.util.Random;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.math.Vec3d;

public class RenderSparks {
	
	public static void renderSpark(int seed, double x, double y, double z, float length, int min, int max, int color1, int color2) {

        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glLineWidth(3F);
        Tessellator tessellator = Tessellator.instance;
        
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
			dir = new Vec3d(dir.x * length * rand.nextFloat(), dir.y * length * rand.nextFloat(), dir.z * length * rand.nextFloat());

			x = prevX + dir.x;
			y = prevY + dir.y;
			z = prevZ + dir.z;

	        GL11.glLineWidth(5F);
            tessellator.startDrawing(3);
            tessellator.setColorOpaque_I(color1);
            tessellator.addVertex(prevX, prevY, prevZ);
            tessellator.addVertex(x, y, z);
            tessellator.draw();
	        GL11.glLineWidth(2F);
            tessellator.startDrawing(3);
            tessellator.setColorOpaque_I(color2);
            tessellator.addVertex(prevX, prevY, prevZ);
            tessellator.addVertex(x, y, z);
            tessellator.draw();
		}

        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glPopMatrix();
	}

}
