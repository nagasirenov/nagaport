package com.hbm.render.tileentity;

import org.lwjgl.opengl.GL11;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.lib.Library;
import com.hbm.main.ResourceManager;
import com.hbm.tileentity.machine.TileEntitySolarBoiler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;

public class RenderSolarPanel extends TileEntitySpecialRenderer {

	@Override
	public void renderTileEntityAt(TileEntity te, double x, double y, double z, float interp) {

		GL11.glPushMatrix();
		GL11.glTranslated(x + 0.5D, y, z + 0.5D);
		GL11.glEnable(GL11.GL_LIGHTING);

		GL11.glPushMatrix();
		GL11.glRotatef(90, 0F, 1F, 0F);

		switch(te.getBlockMetadata() - BlockDummyable.offset) {
		case 2: GL11.glRotatef(0, 0F, 1F, 0F); break;
		case 4: GL11.glRotatef(90, 0F, 1F, 0F); break;
		case 3: GL11.glRotatef(180, 0F, 1F, 0F); break;
		case 5: GL11.glRotatef(270, 0F, 1F, 0F); break;
		}

		bindTexture(ResourceManager.solarp_tex);

		GL11.glShadeModel(GL11.GL_SMOOTH);
		ResourceManager.solarp.renderPart("solar_panel");
		GL11.glShadeModel(GL11.GL_FLAT);
		GL11.glPopMatrix();

		int ix = te.getPos().getX();
        int iy = te.getPos().getY();
        int iz = te.getPos().getZ();

        if(Library.canConnect(te.getWorld(), ix, iy, iz + 1, Library.POS_Z))
            ResourceManager.solarp.renderPart("connector_front");
        
        if(Library.canConnect(te.getWorld(), ix, iy, iz - 1, Library.NEG_Z))
            ResourceManager.solarp.renderPart("connector_side");

        if(Library.canConnect(te.getWorld(), ix + 1, iy, iz, Library.POS_X)) {
        	GL11.glRotatef(90, 0F, 1F, 0F);
            ResourceManager.solarp.renderPart("connector_front");
            GL11.glRotatef(-90, 0F, 1F, 0F);
        }

        if(Library.canConnect(te.getWorld(), ix - 1, iy, iz, Library.NEG_X)) {
        	GL11.glRotatef(90, 0F, 1F, 0F);
            ResourceManager.solarp.renderPart("connector_side");
            GL11.glRotatef(-90, 0F, 1F, 0F);
        }
        
        if(Library.canConnect(te.getWorld(), ix, iy + 1, iz, Library.POS_Y))
            ResourceManager.solarp.renderPart("connector_top");

        if(Library.canConnect(te.getWorld(), ix, iy - 1, iz, Library.NEG_Y))
            ResourceManager.solarp.renderPart("connector_bottom");

		GL11.glPopMatrix();
	}



}
