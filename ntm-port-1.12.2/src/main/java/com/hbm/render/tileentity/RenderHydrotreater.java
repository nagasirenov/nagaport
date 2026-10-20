package com.hbm.render.tileentity;

import org.lwjgl.opengl.GL11;

import com.hbm.blocks.ModBlocks;
import com.hbm.main.ResourceManager;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;

public class RenderHydrotreater extends TileEntitySpecialRenderer {

	@Override
	public void render(TileEntity tile, double x, double y, double z, float f, int nativeDestroyStage, float nativeAlpha) {

		GL11.glPushMatrix();
		GL11.glTranslated(x + 0.5, y, z + 0.5);
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_CULL_FACE);

		GL11.glShadeModel(GL11.GL_SMOOTH);
		bindTexture(ResourceManager.hydrotreater_tex);
		ResourceManager.hydrotreater.renderAll();
		GL11.glShadeModel(GL11.GL_FLAT);

		GL11.glPopMatrix();
	}


}
