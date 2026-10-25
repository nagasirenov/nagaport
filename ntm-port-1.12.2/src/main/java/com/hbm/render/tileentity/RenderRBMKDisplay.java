package com.hbm.render.tileentity;

import org.lwjgl.opengl.GL11;

import com.hbm.tileentity.machine.rbmk.TileEntityRBMKDisplay;
import com.hbm.tileentity.machine.rbmk.TileEntityRBMKConsole.RBMKColumn;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntity;

public class RenderRBMKDisplay extends TileEntitySpecialRenderer {

	@Override
	public void render(TileEntity te, double x, double y, double z, float interp, int nativeDestroyStage, float nativeAlpha) {

		GL11.glPushMatrix();
		GL11.glTranslated(x + 0.5, y, z + 0.5);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glEnable(GL11.GL_LIGHTING);

		switch(te.getBlockMetadata()) {
		case 2: GL11.glRotatef(90, 0F, 1F, 0F); break;
		case 4: GL11.glRotatef(180, 0F, 1F, 0F); break;
		case 3: GL11.glRotatef(270, 0F, 1F, 0F); break;
		case 5: GL11.glRotatef(0, 0F, 1F, 0F); break;
		}

		TileEntityRBMKDisplay display = (TileEntityRBMKDisplay) te;

		GL11.glTranslated(0, 0.5, 0);
		GL11.glScaled(1, 8D / 7D, 8D / 7D);
		GL11.glTranslated(0, -0.5, 0);

		Tessellator tess = Tessellator.getInstance();
		BufferBuilder buffer = tess.getBuffer();
		float[] colorState = {1F, 1F, 1F};
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

		for(int i = 0; i < display.columns.length; i++) {

			RBMKColumn col = display.columns[i];

			if(col == null) continue;

			double kx = 0.28125D;
			double ky = -(i / 7) * 0.125 + 0.875;
			double kz = -(i % 7) * 0.125 + 0.125D * 3;

			if(col.data.hasKey("color") && col.data.getByte("color") >= 0) {
				byte color = col.data.getByte("color");
				if(color == 0) setColor(colorState, 0xFF0000);
				if(color == 1) setColor(colorState, 0xFFFF00);
				if(color == 2) setColor(colorState, 0x008000);
				if(color == 3) setColor(colorState, 0x0000FF);
				if(color == 4) setColor(colorState, 0x8000FF);
			} else {
				double heat = col.data.getDouble("heat") / col.data.getDouble("maxHeat");
				double colorValue = 0.65D + (i % 2) * 0.05D;
				setColor(colorState, (float) (colorValue + ((1 - colorValue) * heat)), (float) colorValue, (float) colorValue);
			}

			if(col.data.getByte("indicator") > 0) setColor(colorState, 1F, 1F, 0F);

			drawColumn(buffer, colorState, kx, ky, kz);

			switch(col.type) {
			case FUEL:
			case FUEL_SIM:		drawFuel(buffer, colorState, kx + 0.01, ky, kz, col.data.getDouble("enrichment")); break;
			case CONTROL:		drawControl(buffer, colorState, kx + 0.01, ky, kz, col.data.getDouble("level")); break;
			case CONTROL_AUTO:	drawControlAuto(buffer, colorState, kx + 0.01, ky, kz, col.data.getDouble("level")); break;
			default:
			}
		}

		tess.draw();
		GL11.glEnable(GL11.GL_TEXTURE_2D);

		GL11.glPopMatrix();
	}

	private void drawColumn(BufferBuilder buffer, float[] color, double x, double y, double z) {

		double width = 0.0625D * 0.75;

		addVertex(buffer, color, x, y + width, z - width);
		addVertex(buffer, color, x, y + width, z + width);
		addVertex(buffer, color, x, y - width, z + width);
		addVertex(buffer, color, x, y - width, z - width);
	}

	private void drawFuel(BufferBuilder buffer, float[] color, double x, double y, double z, double enrichment) {
		this.drawDot(buffer, color, x, y, z, 0F, 0.25F + (float) (enrichment * 0.75D), 0F);
	}

	private void drawControl(BufferBuilder buffer, float[] color, double x, double y, double z, double level) {
		this.drawDot(buffer, color, x, y, z, (float) level, (float) level, 0F);
	}

	private void drawControlAuto(BufferBuilder buffer, float[] color, double x, double y, double z, double level) {
		this.drawDot(buffer, color, x, y, z, (float) level, 0F, (float) level);
	}

	private void drawDot(BufferBuilder buffer, float[] color, double x, double y, double z, float r, float g, float b) {

		double width = 0.03125D;
		double edge = 0.022097D;

		setColor(color, r, g, b);

		addVertex(buffer, color, x, y + width, z);
		addVertex(buffer, color, x, y + edge, z + edge);
		addVertex(buffer, color, x, y, z + width);
		addVertex(buffer, color, x, y - edge, z + edge);

		addVertex(buffer, color, x, y + edge, z - edge);
		addVertex(buffer, color, x, y + width, z);
		addVertex(buffer, color, x, y - edge, z - edge);
		addVertex(buffer, color, x, y, z - width);

		addVertex(buffer, color, x, y + width, z);
		addVertex(buffer, color, x, y - edge, z + edge);
		addVertex(buffer, color, x, y - width, z);
		addVertex(buffer, color, x, y - edge, z - edge);

		setColor(color, 1F, 1F, 1F);
	}

	private static void setColor(float[] color, int rgb) {
		color[0] = (rgb >> 16 & 255) / 255F;
		color[1] = (rgb >> 8 & 255) / 255F;
		color[2] = (rgb & 255) / 255F;
	}

	private static void setColor(float[] color, float red, float green, float blue) {
		color[0] = red;
		color[1] = green;
		color[2] = blue;
	}

	private static void addVertex(BufferBuilder buffer, float[] color, double x, double y, double z) {
		buffer.pos(x, y, z).color(color[0], color[1], color[2], 1F).tex(0D, 0D)
				.lightmap(0, 240).normal(1F, 0F, 0F).endVertex();
	}
}
