package com.hbm.render.tileentity;

import org.lwjgl.opengl.GL11;

import com.hbm.lib.RefStrings;
import com.hbm.main.ResourceManager;
import com.hbm.tileentity.machine.rbmk.TileEntityRBMKRod;
import com.hbm.util.ColorUtil;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

public class RenderRBMKFuelChannel extends TileEntitySpecialRenderer {

	private static final ResourceLocation texture_rods = new ResourceLocation(RefStrings.MODID + ":textures/blocks/rbmk/rbmk_element_fuel.png");

	@Override
	public void render(TileEntity te, double x, double y, double z, float i, int nativeDestroyStage, float nativeAlpha) {

		boolean hasRod = false;
		boolean cherenkov = false;
		int color = 0;

		if(te instanceof TileEntityRBMKRod) {
			TileEntityRBMKRod rod = (TileEntityRBMKRod) te;
			if(rod.hasRod) hasRod = true;
			if(rod.fluxQuantity > 5) cherenkov = true;
			color = rod.rodColor;
		}

		GL11.glPushMatrix();
		GL11.glTranslated(x + 0.5, y, z + 0.5);

		int offset = 1;

		for(int o = 1; o < 16; o++) {

			if(com.hbm.world.WorldBlockState.read(te.getWorld(), te.getPos().getX(), te.getPos().getY() + o, te.getPos().getZ()).getBlock() == te.getBlockType()) {
				offset = o;
				int meta = com.hbm.world.WorldBlockState.readMetadata(te.getWorld(), te.getPos().getX(), te.getPos().getY() + o, te.getPos().getZ());
				if(meta > 5 && meta < 12) break;

			} else {
				break;
			}
		}

		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_CULL_FACE);

		if(hasRod) {
			GL11.glPushMatrix();
			bindTexture(texture_rods);
			GL11.glColor3f(ColorUtil.fr(color), ColorUtil.fg(color), ColorUtil.fb(color));

			for(int j = 0; j <= offset; j++) {
				ResourceManager.rbmk_element_rods_vbo.renderPart("Rods");
				GL11.glTranslated(0, 1, 0);
			}

			GL11.glColor3f(1F, 1F, 1F);
			GL11.glPopMatrix();
		}

		if(cherenkov) {
			GL11.glTranslated(0, 0.75, 0);

			GL11.glDisable(GL11.GL_CULL_FACE);
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			GL11.glDisable(GL11.GL_ALPHA_TEST);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);

			Tessellator tess = Tessellator.getInstance();
			BufferBuilder tessBuffer = tess.getBuffer();
			tessBuffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
			int tessColor0Red = Math.max(0, Math.min(255, (int) ((0.4F) * 255.0F)));
			int tessColor0Green = Math.max(0, Math.min(255, (int) ((0.9F) * 255.0F)));
			int tessColor0Blue = Math.max(0, Math.min(255, (int) ((1.0F) * 255.0F)));
			int tessColor0Alpha = Math.max(0, Math.min(255, (int) ((0.1F) * 255.0F)));

			for(double j = 0; j <= offset; j += 0.25) {
				tessBuffer.pos(-0.5, j, -0.5).color(tessColor0Red, tessColor0Green, tessColor0Blue, tessColor0Alpha).endVertex();
				tessBuffer.pos(-0.5, j, 0.5).color(tessColor0Red, tessColor0Green, tessColor0Blue, tessColor0Alpha).endVertex();
				tessBuffer.pos(0.5, j, 0.5).color(tessColor0Red, tessColor0Green, tessColor0Blue, tessColor0Alpha).endVertex();
				tessBuffer.pos(0.5, j, -0.5).color(tessColor0Red, tessColor0Green, tessColor0Blue, tessColor0Alpha).endVertex();
			}
			tess.draw();

			GL11.glEnable(GL11.GL_ALPHA_TEST);
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			GL11.glDisable(GL11.GL_BLEND);
			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glEnable(GL11.GL_CULL_FACE);
		}

		GL11.glPopMatrix();
	}
}
