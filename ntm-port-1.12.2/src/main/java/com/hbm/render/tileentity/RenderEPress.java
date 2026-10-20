package com.hbm.render.tileentity;

import org.lwjgl.opengl.GL11;

import com.hbm.blocks.BlockDummyable;
import com.hbm.main.ResourceManager;
import com.hbm.render.util.NativeMachineItemRenderer;
import com.hbm.tileentity.machine.TileEntityMachineEPress;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.MathHelper;

public class RenderEPress extends TileEntitySpecialRenderer<TileEntity> {
	
	private final NativeMachineItemRenderer itemRenderer = new NativeMachineItemRenderer();
	
	public RenderEPress() { }
	
	@Override
	public void render(TileEntity tileentity, double x, double y, double z, float f, int destroyStage, float alpha) {
		GL11.glPushMatrix();
			GL11.glTranslated(x + 0.5D, y, z + 0.5D);
			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glRotatef(180, 0F, 1F, 0F);
			
			switch(tileentity.getBlockMetadata() - BlockDummyable.offset) {
			case 2: GL11.glRotatef(270, 0F, 1F, 0F); break;
			case 4: GL11.glRotatef(0, 0F, 1F, 0F); break;
			case 3: GL11.glRotatef(90, 0F, 1F, 0F); break;
			case 5: GL11.glRotatef(180, 0F, 1F, 0F); break;
			}
			
			this.bindTexture(ResourceManager.epress_body_tex);
			
			ResourceManager.epress_body.renderAll();
				
		GL11.glPopMatrix();
		
		renderTileEntityAt2(tileentity, x, y, z, f);
	}

	public void renderTileEntityAt2(TileEntity tileentity, double x, double y, double z, float f) {
		GL11.glPushMatrix();
			GL11.glTranslated(x + 0.5D, y + 1, z + 0.5D);
			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glRotatef(180, 0F, 1F, 0F);
			
			switch(tileentity.getBlockMetadata() - BlockDummyable.offset) {
			case 2: GL11.glRotatef(270, 0F, 1F, 0F); break;
			case 4: GL11.glRotatef(0, 0F, 1F, 0F); break;
			case 3: GL11.glRotatef(90, 0F, 1F, 0F); break;
			case 5: GL11.glRotatef(180, 0F, 1F, 0F); break;
			}

			TileEntityMachineEPress press = (TileEntityMachineEPress)tileentity;
			double p = (press.lastPress + (press.renderPress - press.lastPress) * f) /(double) press.maxPress;
			GL11.glTranslated(0, MathHelper.clamp((1D - p), 0D, 1D) * 0.875D, 0);
		
			this.bindTexture(ResourceManager.epress_head_tex);
		
			ResourceManager.epress_head.renderAll();
			
		GL11.glPopMatrix();
		
		renderTileEntityAt3(tileentity, x, y, z, f);
	}

	public void renderTileEntityAt3(TileEntity tileentity, double x, double y, double z, float f) {

		GL11.glPushMatrix();
			GL11.glTranslated(x + 0.5D, y + 1, z + 0.5);
			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glRotatef(180, 0F, 1F, 0F);
			
			switch(tileentity.getBlockMetadata() - BlockDummyable.offset) {
			case 2:
				GL11.glRotatef(270, 0F, 1F, 0F); break;
			case 4:
				GL11.glRotatef(0, 0F, 1F, 0F); break;
			case 3:
				GL11.glRotatef(90, 0F, 1F, 0F); break;
			case 5:
				GL11.glRotatef(180, 0F, 1F, 0F); break;
			}

			GL11.glRotatef(90, 0F, 1F, 0F);
			GL11.glRotatef(-90, 1F, 0F, 0F);
			GL11.glTranslatef(1.0F, 1.0F - 0.0625F * 165/100, 0.0F);
			GL11.glTranslatef(-1, -1.15F, 0);
			
			TileEntityMachineEPress press = (TileEntityMachineEPress)tileentity;
			if(press.syncStack != null) {
				ItemStack stack = press.syncStack.copy();
				
				EntityItem item = new EntityItem(null, 0.0D, 0.0D, 0.0D, stack);
				item.getItem().setCount(1);
				item.hoverStart = 0.0F;
							
				this.itemRenderer.renderFrame(item, 0.0D, 0.0D, 0.0D, 0.0F, 0.0F, NativeMachineItemRenderer.Display.DECORATION_FRAME);
			}
			
		GL11.glPopMatrix();
	}
}
