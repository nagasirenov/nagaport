package com.hbm.render.tileentity;

import org.lwjgl.opengl.GL11;

import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;
import com.hbm.render.model.ModelGun;
import com.hbm.render.model.ModelStatue;
import com.hbm.render.util.NativeMachineItemRenderer;
import com.hbm.tileentity.deco.TileEntityDecoBlockAltF;
import com.hbm.tileentity.deco.TileEntityDecoBlockAltG;
import com.hbm.tileentity.deco.TileEntityDecoBlockAltW;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

public class RenderDecoBlockAlt extends TileEntitySpecialRenderer<TileEntity> {

private static final ResourceLocation texture = new ResourceLocation(RefStrings.MODID + ":" + "textures/models/ModelStatue.png");
private static final ResourceLocation gunTexture = new ResourceLocation(RefStrings.MODID + ":" + "textures/models/ModelGun.png");
	
	private ModelStatue model;
	private ModelGun gun;
	private final NativeMachineItemRenderer itemRenderer = new NativeMachineItemRenderer();
	
	public RenderDecoBlockAlt() {
		this.model = new ModelStatue();
		this.gun = new ModelGun();
	}
	
	@Override
	public void render(TileEntity tileentity, double x, double y, double z, float f, int destroyStage, float alpha) {

		GL11.glPushMatrix();
			GL11.glTranslatef((float) x + 0.5F, (float) y + 1.5F, (float) z + 0.5F);
			GL11.glRotatef(180, 0F, 0F, 1F);
			switch(tileentity.getBlockMetadata())
			{
			case 4:
				GL11.glRotatef(90, 0F, 1F, 0F); break;
			case 2:
				GL11.glRotatef(180, 0F, 1F, 0F); break;
			case 5:
				GL11.glRotatef(270, 0F, 1F, 0F); break;
			case 3:
				GL11.glRotatef(0, 0F, 1F, 0F); break;
			}
			
			EntityItem watch = new EntityItem(null, 0.0D, 0.0D, 0.0D, new ItemStack(ModItems.watch));
			
			this.bindTexture(texture);
			this.model.renderModel(0.0625F);
			float g = 0.0625F;
			float q = g * 2 + 0.0625F / 3;
			GL11.glTranslatef(0.0F, -2 * g, q);
			GL11.glRotatef(180, 0F, 0F, 1F);
			if(tileentity instanceof TileEntityDecoBlockAltW || tileentity instanceof TileEntityDecoBlockAltF)
				this.itemRenderer.renderFrame(watch, 0.0D, 0.0D, 0.0D, 0.0F, 0.0F, NativeMachineItemRenderer.Display.DECORATION_FRAME);

			GL11.glTranslatef(0.0F, 2 * g, -q);
			GL11.glRotatef(180, 0F, 0F, 1F);
			GL11.glRotatef(90, 0F, 1F, 0F);
			GL11.glScalef(0.5F, 0.5F, 0.5F);
			GL11.glTranslatef(-g * 20, g * 4, g * 11);
			GL11.glRotatef(-20, 0F, 0F, 1F);
			this.bindTexture(gunTexture);
			if(tileentity instanceof TileEntityDecoBlockAltG || tileentity instanceof TileEntityDecoBlockAltF)
				this.gun.renderModel(0.0625F);
		GL11.glPopMatrix();
	}
}
