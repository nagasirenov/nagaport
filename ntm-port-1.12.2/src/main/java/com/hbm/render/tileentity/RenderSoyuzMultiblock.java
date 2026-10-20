package com.hbm.render.tileentity;

import org.lwjgl.opengl.GL11;

import com.hbm.lib.RefStrings;
import com.hbm.render.item.nativeitem.context.NativeProjectionScope;

import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

/** Native preview TESR. Original fixed materials, cube vertices and placement order are retained. */
public class RenderSoyuzMultiblock extends TileEntitySpecialRenderer<TileEntity> {

	float pixel = 1F/16F;

	@Override
	public void render(TileEntity te, double x, double y, double z, float f, int destroyStage, float alpha) {
		try (NativeProjectionScope scope = new NativeProjectionScope()) {

			GL11.glPushMatrix();

			GL11.glTranslatef((float)x + 1, (float)y + 1, (float)z);

			GL11.glEnable(GL11.GL_BLEND);
			GL11.glEnable(GL11.GL_CULL_FACE);
			OpenGlHelper.glBlendFunc(770, 771, 1, 0);
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.75F);
			GL11.glDisable(GL11.GL_ALPHA_TEST);
			ResourceLocation loc = new ResourceLocation(RefStrings.MODID, "textures/blocks/struct_launcher.png");

			for(int i = -6; i <= 6; i++)
				for(int j = 3; j <= 4; j++)
					for(int k = -6; k <= 6; k++)
						renderSmolBlockAt(loc, i, j, k);

			for(int i = -1; i <= 1; i++)
				for(int j = 3; j <= 4; j++)
					for(int k = -8; k <= -7; k++)
						renderSmolBlockAt(loc, i, j, k);

			for(int i = -2; i <= 2; i++)
				for(int j = 3; j <= 4; j++)
					for(int k = 7; k <= 9; k++)
						renderSmolBlockAt(loc, i, j, k);

			for(int i = -2; i <= 2; i++)
				for(int k = 5; k <= 9; k++)
					renderSmolBlockAt(loc, i, 51, k);

			for(int i = -1; i <= 1; i++)
				for(int k = -8; k <= -6; k++)
					renderSmolBlockAt(loc, i, 38, k);

			loc = new ResourceLocation(RefStrings.MODID, "textures/blocks/concrete.png");

			for(int i = 3; i <= 6; i++)
				for(int j = 0; j <= 2; j++)
					for(int k = 3; k <= 6; k++)
						renderSmolBlockAt(loc, i, j, k);

			for(int i = -6; i <= -3; i++)
				for(int j = 0; j <= 2; j++)
					for(int k = 3; k <= 6; k++)
						renderSmolBlockAt(loc, i, j, k);

			for(int i = -6; i <= -3; i++)
				for(int j = 0; j <= 2; j++)
					for(int k = -6; k <= -3; k++)
						renderSmolBlockAt(loc, i, j, k);

			for(int i = 3; i <= 6; i++)
				for(int j = 0; j <= 2; j++)
					for(int k = -6; k <= -3; k++)
						renderSmolBlockAt(loc, i, j, k);

			for(int i = -1; i <= 1; i++)
				for(int j = 0; j <= 2; j++)
					for(int k = -8; k <= -6; k++)
						renderSmolBlockAt(loc, i, j, k);

			for(int i = -2; i <= 2; i++)
				for(int j = 0; j <= 2; j++)
					for(int k = 5; k <= 9; k++)
						renderSmolBlockAt(loc, i, j, k);

			loc = new ResourceLocation(RefStrings.MODID, "textures/blocks/struct_scaffold.png");

			for(int i = -1; i <= 1; i++)
				for(int j = 5; j <= 50; j++)
					for(int k = 6; k <= 8; k++)
						renderSmolBlockAt(loc, i, j, k);

			for(int j = 5; j <= 37; j++)
				renderSmolBlockAt(loc, 0, j, -7);

			GL11.glDisable(GL11.GL_BLEND);
			GL11.glEnable(GL11.GL_ALPHA_TEST);

			GL11.glPopMatrix();
		}
	}

	public void renderSmolBlockAt(ResourceLocation loc, int x, int y, int z) {
		GL11.glPushMatrix();
		try {
			GL11.glTranslatef(x, y, z);
			GL11.glRotatef(180, 0F, 0F, 1F);

			Tessellator tesseract = Tessellator.getInstance();
			BufferBuilder buffer = tesseract.getBuffer();
			this.bindTexture(loc);
			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
			buffer.pos(1 - 11 * pixel / 2, 1 - 11 * pixel / 2, 1 - 11 * pixel / 2).tex(1, 0).endVertex();
			buffer.pos(11 * pixel / 2, 1 - 11 * pixel / 2, 1 - 11 * pixel / 2).tex(0, 0).endVertex();
			buffer.pos(11 * pixel / 2, 11 * pixel / 2, 1 - 11 * pixel / 2).tex(0, 1).endVertex();
			buffer.pos(1 - 11 * pixel / 2, 11 * pixel / 2,  1 - 11 * pixel / 2).tex(1, 1).endVertex();
			tesseract.draw();

			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
			buffer.pos(1 - 11 * pixel / 2, 1 - 11 * pixel / 2, 11 * pixel / 2).tex(1, 0).endVertex();
			buffer.pos(1 - 11 * pixel / 2, 1 - 11 * pixel / 2, 1 - 11 * pixel / 2).tex(0, 0).endVertex();
			buffer.pos(1 - 11 * pixel / 2, 11 * pixel / 2, 1 - 11 * pixel / 2).tex(0, 1).endVertex();
			buffer.pos(1 - 11 * pixel / 2, 11 * pixel / 2,  11 * pixel / 2).tex(1, 1).endVertex();
			tesseract.draw();

			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
			buffer.pos(11 * pixel / 2, 1 - 11 * pixel / 2, 11 * pixel / 2).tex(1, 0).endVertex();
			buffer.pos(1 - 11 * pixel / 2, 1 - 11 * pixel / 2, 11 * pixel / 2).tex(0, 0).endVertex();
			buffer.pos(1 - 11 * pixel / 2, 11 * pixel / 2,  11 * pixel / 2).tex(0, 1).endVertex();
			buffer.pos(11 * pixel / 2, 11 * pixel / 2, 11 * pixel / 2).tex(1, 1).endVertex();
			tesseract.draw();

			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
			buffer.pos(11 * pixel / 2, 1 - 11 * pixel / 2, 1 - 11 * pixel / 2).tex(1, 0).endVertex();
			buffer.pos(11 * pixel / 2, 1 - 11 * pixel / 2, 11 * pixel / 2).tex(0, 0).endVertex();
			buffer.pos(11 * pixel / 2, 11 * pixel / 2,  11 * pixel / 2).tex(0, 1).endVertex();
			buffer.pos(11 * pixel / 2, 11 * pixel / 2, 1 - 11 * pixel / 2).tex(1, 1).endVertex();
			tesseract.draw();

			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
			buffer.pos(1 - 11 * pixel / 2, 1 - 11 * pixel / 2, 11 * pixel / 2).tex(1, 0).endVertex();
			buffer.pos(11 * pixel / 2, 1 - 11 * pixel / 2, 11 * pixel / 2).tex(0, 0).endVertex();
			buffer.pos(11 * pixel / 2, 1 - 11 * pixel / 2,  1 - 11 * pixel / 2).tex(0, 1).endVertex();
			buffer.pos(1 - 11 * pixel / 2, 1 - 11 * pixel / 2, 1 - 11 * pixel / 2).tex(1, 1).endVertex();
			tesseract.draw();

			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
			buffer.pos(11 * pixel / 2, 11 * pixel / 2, 11 * pixel / 2).tex(1, 0).endVertex();
			buffer.pos(1 - 11 * pixel / 2, 11 * pixel / 2, 11 * pixel / 2).tex(0, 0).endVertex();
			buffer.pos(1 - 11 * pixel / 2, 11 * pixel / 2, 1 - 11 * pixel / 2).tex(0, 1).endVertex();
			buffer.pos(11 * pixel / 2, 11 * pixel / 2,  1 - 11 * pixel / 2).tex(1, 1).endVertex();
			tesseract.draw();
		} finally {
			GL11.glPopMatrix();
		}
	}
}
