package com.hbm.render.tileentity;

import net.minecraft.client.renderer.GlStateManager;
import com.hbm.render.item.nativeitem.context.NativeProjectionScope;

import com.hbm.blocks.ModBlocks;
import com.hbm.lib.Library;
import com.hbm.main.ResourceManager;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

public class RenderRTG extends TileEntitySpecialRenderer<TileEntity> {

	@Override
	public void render(TileEntity te, double x, double y, double z, float inter, int destroyStage, float alpha) {
		
        try(NativeProjectionScope scope = new NativeProjectionScope()) {
        GlStateManager.translate(x + 0.5D, y, z + 0.5D);
        GlStateManager.enableLighting();
        GlStateManager.disableCull();
		GlStateManager.rotate(180, 0F, 1F, 0F);
        
        if(te.getBlockType() == ModBlocks.machine_rtg_grey)
            bindTexture(ResourceManager.rtg_tex);
        else if(te.getBlockType() == ModBlocks.machine_powerrtg)
            bindTexture(ResourceManager.rtg_polonium_tex);
        else
            bindTexture(ResourceManager.rtg_cell_tex);
        
        ResourceManager.rtg.renderPart("Gen");
        
        int ix = te.getPos().getX();
        int iy = te.getPos().getY();
        int iz = te.getPos().getZ();

        if(Library.canConnect(te.getWorld(), ix + 1, iy, iz, Library.POS_X))
            ResourceManager.rtg.renderPart("Connector");
        
        if(Library.canConnect(te.getWorld(), ix - 1, iy, iz, Library.NEG_X)) {
    		GlStateManager.rotate(180, 0F, 1F, 0F);
            ResourceManager.rtg.renderPart("Connector");
    		GlStateManager.rotate(-180, 0F, 1F, 0F);
        }
        
        if(Library.canConnect(te.getWorld(), ix, iy, iz - 1, Library.NEG_Z)) {
    		GlStateManager.rotate(90, 0F, 1F, 0F);
            ResourceManager.rtg.renderPart("Connector");
    		GlStateManager.rotate(-90, 0F, 1F, 0F);
        }
        
        if(Library.canConnect(te.getWorld(), ix, iy, iz + 1, Library.POS_Z)) {
    		GlStateManager.rotate(-90, 0F, 1F, 0F);
            ResourceManager.rtg.renderPart("Connector");
    		GlStateManager.rotate(90, 0F, 1F, 0F);
        }

        }
	}

}
