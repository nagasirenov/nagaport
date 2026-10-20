package com.hbm.render.tileentity;

import org.lwjgl.opengl.GL11;

import com.hbm.blocks.ModBlocks;
import com.hbm.main.ResourceManager;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;

public class RenderVacuumDistill extends TileEntitySpecialRenderer {

	@Override
	public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float interp) {
		GL11.glPushMatrix();
		GL11.glTranslated(x + 0.5D, y, z + 0.5D);
		GL11.glEnable(GL11.GL_LIGHTING);

		GL11.glShadeModel(GL11.GL_SMOOTH);
		bindTexture(ResourceManager.vacuum_distill_tex);
		ResourceManager.vacuum_distill.renderAll();
		GL11.glShadeModel(GL11.GL_FLAT);
		
		GL11.glPopMatrix();
	}


}
