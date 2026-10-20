package com.hbm.render.tileentity;

import org.lwjgl.opengl.GL11;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.main.ResourceManager;
import com.hbm.render.util.NativeMachineItemRenderer;
import com.hbm.tileentity.machine.TileEntityMachineArcWelder;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

public class RenderArcWelder extends TileEntitySpecialRenderer<TileEntity> {
	
	private final NativeMachineItemRenderer itemRenderer = new NativeMachineItemRenderer();
	
	@Override
	public void render(TileEntity tile, double x, double y, double z, float interp, int destroyStage, float alpha) {
		GL11.glPushMatrix();
		GL11.glTranslated(x + 0.5, y, z + 0.5);
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_CULL_FACE);

		switch(tile.getBlockMetadata() - BlockDummyable.offset) {
		case 2: GL11.glRotatef(90, 0F, 1F, 0F); break;
		case 4: GL11.glRotatef(180, 0F, 1F, 0F); break;
		case 3: GL11.glRotatef(270, 0F, 1F, 0F); break;
		case 5: GL11.glRotatef(0, 0F, 1F, 0F); break;
		}
		
		GL11.glTranslated(-0.5, 0, 0);
		
		bindTexture(ResourceManager.arc_welder_tex);
		ResourceManager.arc_welder.renderAll();
		
		TileEntityMachineArcWelder welder = (TileEntityMachineArcWelder) tile;
		if(welder.display != null) {
			GL11.glPushMatrix();
			GL11.glTranslated(0.0625D * 2.5D, 1.125D, 0D);
			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glRotatef(90, 0F, 1F, 0F);
			GL11.glRotatef(-90, 1F, 0F, 0F);
			
			if(welder.display != null) {
				ItemStack stack = welder.display.copy();
				
				EntityItem item = new EntityItem(null, 0.0D, 0.0D, 0.0D, stack);
				item.getItem().setCount(1);
				item.hoverStart = 0.0F;
				
				GL11.glScaled(1.5, 1.5, 1.5);
				this.itemRenderer.renderFrame(item, 0.0D, 0.0D, 0.0D, 0.0F, 0.0F, NativeMachineItemRenderer.Display.DECORATION_FRAME);
			}
			GL11.glPopMatrix();
		}
		
		GL11.glPopMatrix();
	}


}
