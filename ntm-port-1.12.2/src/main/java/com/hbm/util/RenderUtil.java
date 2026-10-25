package com.hbm.util;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

public class RenderUtil {

	public static void renderBlock(Tessellator tessellator) {
		renderBlock(tessellator, 0, 1);
	}

	public static void renderBlock(Tessellator tessellator, double uvMin, double uvMax) {
		BufferBuilder buffer = tessellator.getBuffer();
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		buffer.pos(-0.5, +0.5, -0.5).tex(uvMax, uvMax).endVertex();
		buffer.pos(+0.5, +0.5, -0.5).tex(uvMin, uvMax).endVertex();
		buffer.pos(+0.5, -0.5, -0.5).tex(uvMin, uvMin).endVertex();
		buffer.pos(-0.5, -0.5, -0.5).tex(uvMax, uvMin).endVertex();

		buffer.pos(-0.5, +0.5, +0.5).tex(uvMax, uvMax).endVertex();
		buffer.pos(-0.5, +0.5, -0.5).tex(uvMin, uvMax).endVertex();
		buffer.pos(-0.5, -0.5, -0.5).tex(uvMin, uvMin).endVertex();
		buffer.pos(-0.5, -0.5, +0.5).tex(uvMax, uvMin).endVertex();

		buffer.pos(+0.5, +0.5, +0.5).tex(uvMax, uvMax).endVertex();
		buffer.pos(-0.5, +0.5, +0.5).tex(uvMin, uvMax).endVertex();
		buffer.pos(-0.5, -0.5, +0.5).tex(uvMin, uvMin).endVertex();
		buffer.pos(+0.5, -0.5, +0.5).tex(uvMax, uvMin).endVertex();

		buffer.pos(+0.5, +0.5, -0.5).tex(uvMax, uvMax).endVertex();
		buffer.pos(+0.5, +0.5, +0.5).tex(uvMin, uvMax).endVertex();
		buffer.pos(+0.5, -0.5, +0.5).tex(uvMin, uvMin).endVertex();
		buffer.pos(+0.5, -0.5, -0.5).tex(uvMax, uvMin).endVertex();

		buffer.pos(-0.5, -0.5, -0.5).tex(uvMax, uvMax).endVertex();
		buffer.pos(+0.5, -0.5, -0.5).tex(uvMin, uvMax).endVertex();
		buffer.pos(+0.5, -0.5, +0.5).tex(uvMin, uvMin).endVertex();
		buffer.pos(-0.5, -0.5, +0.5).tex(uvMax, uvMin).endVertex();

		buffer.pos(+0.5, +0.5, -0.5).tex(uvMax, uvMax).endVertex();
		buffer.pos(-0.5, +0.5, -0.5).tex(uvMin, uvMax).endVertex();
		buffer.pos(-0.5, +0.5, +0.5).tex(uvMin, uvMin).endVertex();
		buffer.pos(+0.5, +0.5, +0.5).tex(uvMax, uvMin).endVertex();
		tessellator.draw();
	}

}
