package com.hbm.render.tileentity;

import org.lwjgl.opengl.GL11;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.main.ResourceManager;
import com.hbm.tileentity.machine.TileEntityDysonConverterAnatmogenesis;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;

public class RenderDysonConverterAnatmogenesis extends TileEntitySpecialRenderer {

	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float f) {
		GL11.glPushMatrix();
		{

			GL11.glTranslated(x + 0.5D, y, z + 0.5D);
			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glEnable(GL11.GL_CULL_FACE);

			TileEntityDysonConverterAnatmogenesis converter = (TileEntityDysonConverterAnatmogenesis) tileEntity;

			switch(tileEntity.getBlockMetadata() - BlockDummyable.offset) {
			case 2: GL11.glRotatef(0, 0F, 1F, 0F); break;
			case 4: GL11.glRotatef(90, 0F, 1F, 0F); break;
			case 3: GL11.glRotatef(180, 0F, 1F, 0F); break;
			case 5: GL11.glRotatef(270, 0F, 1F, 0F); break;
			}

			GL11.glShadeModel(GL11.GL_SMOOTH);
			bindTexture(ResourceManager.dyson_anatmogenesis_tex);
			ResourceManager.dyson_anatmogenesis.renderPart("Anatmogenesis");

			if(converter.isConverting) {
				float t = tileEntity.getWorld().getTotalWorldTime() + f;

				GL11.glTranslated(0, Math.sin(t * 0.1) * 0.025 + tileEntity.getWorld().rand.nextFloat() * 0.02, 0);
			}

			ResourceManager.dyson_anatmogenesis.renderPart("Coils");

			GL11.glShadeModel(GL11.GL_FLAT);

		}
		GL11.glPopMatrix();
	}



}
