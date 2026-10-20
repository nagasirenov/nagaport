package com.hbm.render.tileentity;

import org.lwjgl.opengl.GL11;

import com.hbm.blocks.ModBlocks;
import com.hbm.lib.RefStrings;
import com.hbm.render.item.nativeitem.context.NativeProjectionScope;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

/** Native preview TESR. Original fixed materials, cube vertices and placement order are retained. */
public class RenderMultiblock extends TileEntitySpecialRenderer<TileEntity> {

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

			Block b = te.getBlockType();

			if(b == ModBlocks.struct_launcher_core)
				renderCompactLauncher();

			if(b == ModBlocks.struct_launcher_core_large)
				renderLaunchTable();

			GL11.glDisable(GL11.GL_BLEND);
			GL11.glEnable(GL11.GL_ALPHA_TEST);

			GL11.glPopMatrix();
		}
	}

	private void renderCompactLauncher() {

		ResourceLocation loc = new ResourceLocation(RefStrings.MODID, "textures/blocks/struct_launcher.png");

		for(int i = -1; i <= 1; i++)
			for(int j = -1; j <= 1; j++)
				if(i != 0 || j != 0)
					renderSmolBlockAt(loc, i, 0, j);
	}

	private void renderLaunchTable() {

		ResourceLocation loc = new ResourceLocation(RefStrings.MODID, "textures/blocks/struct_launcher.png");

		for(int i = -4; i <= 4; i++)
			for(int j = -4; j <= 4; j++)
				if(i != 0 || j != 0)
					renderSmolBlockAt(loc, i, 0, j);

		loc = new ResourceLocation(RefStrings.MODID, "textures/blocks/struct_scaffold.png");

		switch((int)(System.currentTimeMillis() % 4000 / 1000)) {
		case 0:
			for(int k = 1; k < 12; k++)
				renderSmolBlockAt(loc, 3, k, 0);
			break;

		case 1:
			for(int k = 1; k < 12; k++)
				renderSmolBlockAt(loc, 0, k, 3);
			break;

		case 2:
			for(int k = 1; k < 12; k++)
				renderSmolBlockAt(loc, -3, k, 0);
			break;

		case 3:
			for(int k = 1; k < 12; k++)
				renderSmolBlockAt(loc, 0, k, -3);
			break;
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
