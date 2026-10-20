package com.hbm.render.tileentity;

import com.hbm.render.util.NativeMachineItemRenderer;

import org.lwjgl.opengl.GL11;

import com.hbm.blocks.generic.BlockPedestal.TileEntityPedestal;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

public class RenderPedestalTile extends TileEntitySpecialRenderer<TileEntity> {
	private final NativeMachineItemRenderer itemRenderer = new NativeMachineItemRenderer();

	@Override
	public void render(TileEntity te, double x, double y, double z, float interp, int destroyStage, float alpha) {
		GL11.glPushMatrix();
		GL11.glTranslated(x + 0.5, y + 1, z + 0.5);
		GL11.glEnable(GL11.GL_LIGHTING);
		RenderHelper.enableStandardItemLighting();
		
		TileEntityPedestal pedestal = (TileEntityPedestal) te;
		
		if(pedestal.item != null) {

			EntityPlayer player = Minecraft.getMinecraft().player;
			ItemStack stack = pedestal.item.copy();
			GL11.glScaled(1.5, 1.5, 1.5);

			if(!(NativeMachineItemRenderer.isBlockModel(stack))) {
				GL11.glTranslated(0, 0.125, 0);
				GL11.glRotatef(player.prevRotationYaw + (player.rotationYaw - player.prevRotationYaw) * interp + 180, 0.0F, -1.0F, 0.0F);
				
				GL11.glTranslated(0, Math.sin((player.ticksExisted + interp) * 0.1) * 0.0625, 0);
			} else {
				GL11.glTranslated(0, Math.sin((player.ticksExisted + interp) * 0.1) * 0.0625 + 0.0625, 0);
			}

			EntityItem dummy = new EntityItem(te.getWorld(), 0, 0, 0, stack);
			dummy.hoverStart = 0.0F;

			itemRenderer.renderFrame(dummy, 0.0D, 0.0D, 0.0D, 0.0F, 0.0F, NativeMachineItemRenderer.Display.ENTITY_FRAME);
		}
		
		GL11.glPopMatrix();
	}

}
