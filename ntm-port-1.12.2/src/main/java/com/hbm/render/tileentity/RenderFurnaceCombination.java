package com.hbm.render.tileentity;

import org.lwjgl.opengl.GL11;

import com.hbm.blocks.ModBlocks;
import com.hbm.lib.RefStrings;
import com.hbm.main.ResourceManager;
import com.hbm.tileentity.machine.TileEntityFurnaceCombination;

import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

public class RenderFurnaceCombination extends TileEntitySpecialRenderer {

	public static final ResourceLocation texture = new ResourceLocation(RefStrings.MODID + ":textures/particle/rbmk_fire.png");

	@Override
	public void render(TileEntity tileEntity, double x, double y, double z, float f, int nativeDestroyStage, float nativeAlpha) {
		GL11.glPushMatrix();
		GL11.glTranslated(x + 0.5D, y, z + 0.5D);
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_CULL_FACE);

		bindTexture(ResourceManager.combination_oven_tex);
		ResourceManager.combination_oven.renderAll();

		TileEntityFurnaceCombination furnace = (TileEntityFurnaceCombination) tileEntity;

		if(furnace.wasOn) {

			bindTexture(texture);

			GL11.glPushMatrix();
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glAlphaFunc(GL11.GL_GEQUAL, 0);
			GL11.glDepthMask(false);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
			RenderHelper.disableStandardItemLighting();

			int texIndex = (int) (furnace.getWorld().getTotalWorldTime() / 2 % 14);
			float f0 = 1F / 14F;

			float uMin = texIndex % 5 * f0;
			float uMax = uMin + f0;
			float vMin = 0;
			float vMax = 1;

			Tessellator tess = Tessellator.getInstance();
			BufferBuilder buf = tess.getBuffer();
			buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);

			GL11.glTranslated(0, 1.75, 0);
			GL11.glRotatef(-net.minecraft.client.Minecraft.getMinecraft().getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);

			double scaleH = 1;
			double scaleV = 3;

			buf.pos(-scaleH, 0, 0).tex(uMax, vMax).color(1.0F, 1.0F, 1.0F, 1.0F).normal(0.0F, 1.0F, 0.0F).endVertex();
			buf.pos(-scaleH, scaleV, 0).tex(uMax, vMin).color(1.0F, 1.0F, 1.0F, 1.0F).normal(0.0F, 1.0F, 0.0F).endVertex();
			buf.pos(scaleH, scaleV, 0).tex(uMin, vMin).color(1.0F, 1.0F, 1.0F, 1.0F).normal(0.0F, 1.0F, 0.0F).endVertex();
			buf.pos(scaleH, 0, 0).tex(uMin, vMax).color(1.0F, 1.0F, 1.0F, 1.0F).normal(0.0F, 1.0F, 0.0F).endVertex();

			tess.draw();

			//GL11.glPolygonOffset(0.0F, 0.0F);
			GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glDisable(GL11.GL_BLEND);
			GL11.glDepthFunc(GL11.GL_LEQUAL);
			GL11.glDepthMask(true);
			GL11.glPopMatrix();
			RenderHelper.enableStandardItemLighting();
		}

		GL11.glPopMatrix();
	}


}
