package com.hbm.render.tileentity;

import org.lwjgl.opengl.GL11;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.main.ResourceManager;
import com.hbm.tileentity.machine.TileEntityMachineDriveProcessor;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;

public class RenderDriveProcessor extends TileEntitySpecialRenderer {

	@Override
	public void render(TileEntity te, double x, double y, double z, float interp, int nativeDestroyStage, float nativeAlpha) {
		if(!(te instanceof TileEntityMachineDriveProcessor)) return;
		TileEntityMachineDriveProcessor processor = (TileEntityMachineDriveProcessor) te;

		GL11.glPushMatrix();
		{

			GL11.glTranslated(x + 0.5D, y, z + 0.5D);
			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glShadeModel(GL11.GL_SMOOTH);

			GL11.glRotatef(90, 0, 1, 0);
	
			switch(te.getBlockMetadata() - BlockDummyable.offset) {
			case 2: GL11.glRotatef(0, 0F, 1F, 0F); break;
			case 4: GL11.glRotatef(90, 0F, 1F, 0F); break;
			case 3: GL11.glRotatef(180, 0F, 1F, 0F); break;
			case 5: GL11.glRotatef(270, 0F, 1F, 0F); break;
			}
	
			bindTexture(ResourceManager.drive_processor_tex);
			ResourceManager.drive_processor.renderPart("Base");

			if(processor.hasDrive)
				ResourceManager.drive_processor.renderPart("Drive");

			GL11.glShadeModel(GL11.GL_FLAT);

		}
		GL11.glPopMatrix();
	}


	
}
