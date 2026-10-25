package com.hbm.render.tileentity;

import org.lwjgl.opengl.GL11;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.main.ResourceManager;
import com.hbm.tileentity.machine.TileEntityFurnaceSteel;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;

public class RenderFurnaceSteel extends TileEntitySpecialRenderer {

	@Override
	public void render(TileEntity tileEntity, double x, double y, double z, float f, int nativeDestroyStage, float nativeAlpha) {
		GL11.glPushMatrix();
		GL11.glTranslated(x + 0.5D, y, z + 0.5D);
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_CULL_FACE);

		switch(tileEntity.getBlockMetadata() - BlockDummyable.offset) {
		case 3: GL11.glRotatef(0, 0F, 1F, 0F); break;
		case 5: GL11.glRotatef(90, 0F, 1F, 0F); break;
		case 2: GL11.glRotatef(180, 0F, 1F, 0F); break;
		case 4: GL11.glRotatef(270, 0F, 1F, 0F); break;
		}

		GL11.glRotatef(-90, 0F, 1F, 0F);

		bindTexture(ResourceManager.furnace_steel_tex);
		ResourceManager.furnace_steel.renderAll();

		TileEntityFurnaceSteel furnace = (TileEntityFurnaceSteel) tileEntity;

		if(furnace.wasOn) {
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
			float col = (float )Math.sin(System.currentTimeMillis() * 0.001);
			float r = 0.875F + col * 0.125F;
			float g = 0.625F + col * 0.375F;
			float b = 0F;
			float a = 0.5F;
			Tessellator tess = Tessellator.getInstance();
			BufferBuilder buf = tess.getBuffer();
			buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
			for(int i = 0; i < 4; i++) {
				buf.pos(1 + i * 0.0625, 1, -1).color(r, g, b, a).endVertex();
				buf.pos(1 + i * 0.0625, 1, 1).color(r, g, b, a).endVertex();
				buf.pos(1 + i * 0.0625, 0.5, 1).color(r, g, b, a).endVertex();
				buf.pos(1 + i * 0.0625, 0.5, -1).color(r, g, b, a).endVertex();
			}
			tess.draw();
			GL11.glDisable(GL11.GL_BLEND);
			GL11.glEnable(GL11.GL_TEXTURE_2D);
		}

		GL11.glPopMatrix();
	}



}
