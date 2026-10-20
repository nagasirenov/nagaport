package com.hbm.render.tileentity;

import org.lwjgl.opengl.GL11;

import com.hbm.blocks.BlockDummyable;
import com.hbm.config.ClientConfig;
import com.hbm.main.ResourceManager;
import com.hbm.tileentity.machine.TileEntitySolarBoiler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import com.hbm.tileentity.machine.SolarMirrorPosition;

public class RenderSolarBoiler extends TileEntitySpecialRenderer {

	@Override
	public void render(TileEntity te, double x, double y, double z, float interp, int nativeDestroyStage, float nativeAlpha) {

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

		bindTexture(ResourceManager.solar_tex);

		GL11.glShadeModel(GL11.GL_SMOOTH);
		ResourceManager.solar_boiler.renderPart("Base");
		GL11.glShadeModel(GL11.GL_FLAT);
		GL11.glPopMatrix();
		
		if(te instanceof TileEntitySolarBoiler && Minecraft.getMinecraft().gameSettings.fancyGraphics) {
			TileEntitySolarBoiler boiler = (TileEntitySolarBoiler) te;

			Tessellator tess = Tessellator.getInstance();
			BufferBuilder tessBuffer = tess.getBuffer();
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glDisable(GL11.GL_CULL_FACE);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glShadeModel(GL11.GL_SMOOTH);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
			GL11.glAlphaFunc(GL11.GL_GREATER, 0);
			GL11.glDepthMask(false);
			
			int beamCount = 0;
			
			for(SolarMirrorPosition co : boiler.secondary) {
				beamCount ++;
				
				if(beamCount > ClientConfig.RENDER_HELIOSTAT_BEAM_LIMIT.get()) break;
				
				int dx = boiler.getPos().getX() - co.getX();
				int dy = boiler.getPos().getY() - co.getY();
				int dz = boiler.getPos().getZ() - co.getZ();
				
				double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

				float min = 0.005F;
				float max = 0.01F;
				
				GL11.glPushMatrix();
				
				GL11.glTranslated(-dx, -dy, -dz);

				double pitch = Math.toDegrees(-Math.asin((dy + 0.5) / dist)) + 90;
				double yaw = Math.toDegrees(-Math.atan2(dz, dx)) + 180;

				GL11.glTranslated(0, 1, 0);
				GL11.glRotated(yaw, 0, 1, 0);
				GL11.glRotated(pitch, 0, 0, 1);
				GL11.glTranslated(0, -1, 0);
				
				tessBuffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
				int tessColor0Red = Math.max(0, Math.min(255, (int) ((1F) * 255.0F)));
				int tessColor0Green = Math.max(0, Math.min(255, (int) ((1F) * 255.0F)));
				int tessColor0Blue = Math.max(0, Math.min(255, (int) ((1F) * 255.0F)));
				int tessColor0Alpha = Math.max(0, Math.min(255, (int) ((max) * 255.0F)));
				tessBuffer.pos(0.5, 1.0625, 0.5).color(tessColor0Red, tessColor0Green, tessColor0Blue, tessColor0Alpha).endVertex();
				tessBuffer.pos(0.5, 1.0625, -0.5).color(tessColor0Red, tessColor0Green, tessColor0Blue, tessColor0Alpha).endVertex();
				int tessColor1Red = Math.max(0, Math.min(255, (int) ((1F) * 255.0F)));
				int tessColor1Green = Math.max(0, Math.min(255, (int) ((1F) * 255.0F)));
				int tessColor1Blue = Math.max(0, Math.min(255, (int) ((1F) * 255.0F)));
				int tessColor1Alpha = Math.max(0, Math.min(255, (int) ((min) * 255.0F)));
				tessBuffer.pos(0.5, dist, -0.5).color(tessColor1Red, tessColor1Green, tessColor1Blue, tessColor1Alpha).endVertex();
				tessBuffer.pos(0.5, dist, 0.5).color(tessColor1Red, tessColor1Green, tessColor1Blue, tessColor1Alpha).endVertex();

				int tessColor2Red = Math.max(0, Math.min(255, (int) ((1F) * 255.0F)));
				int tessColor2Green = Math.max(0, Math.min(255, (int) ((1F) * 255.0F)));
				int tessColor2Blue = Math.max(0, Math.min(255, (int) ((1F) * 255.0F)));
				int tessColor2Alpha = Math.max(0, Math.min(255, (int) ((max) * 255.0F)));
				tessBuffer.pos(-0.5, 1.0625, 0.5).color(tessColor2Red, tessColor2Green, tessColor2Blue, tessColor2Alpha).endVertex();
				tessBuffer.pos(-0.5, 1.0625, -0.5).color(tessColor2Red, tessColor2Green, tessColor2Blue, tessColor2Alpha).endVertex();
				int tessColor3Red = Math.max(0, Math.min(255, (int) ((1F) * 255.0F)));
				int tessColor3Green = Math.max(0, Math.min(255, (int) ((1F) * 255.0F)));
				int tessColor3Blue = Math.max(0, Math.min(255, (int) ((1F) * 255.0F)));
				int tessColor3Alpha = Math.max(0, Math.min(255, (int) ((min) * 255.0F)));
				tessBuffer.pos(-0.5, dist, -0.5).color(tessColor3Red, tessColor3Green, tessColor3Blue, tessColor3Alpha).endVertex();
				tessBuffer.pos(-0.5, dist, 0.5).color(tessColor3Red, tessColor3Green, tessColor3Blue, tessColor3Alpha).endVertex();

				int tessColor4Red = Math.max(0, Math.min(255, (int) ((1F) * 255.0F)));
				int tessColor4Green = Math.max(0, Math.min(255, (int) ((1F) * 255.0F)));
				int tessColor4Blue = Math.max(0, Math.min(255, (int) ((1F) * 255.0F)));
				int tessColor4Alpha = Math.max(0, Math.min(255, (int) ((max) * 255.0F)));
				tessBuffer.pos(0.5, 1.0625, 0.5).color(tessColor4Red, tessColor4Green, tessColor4Blue, tessColor4Alpha).endVertex();
				tessBuffer.pos(-0.5, 1.0625, 0.5).color(tessColor4Red, tessColor4Green, tessColor4Blue, tessColor4Alpha).endVertex();
				int tessColor5Red = Math.max(0, Math.min(255, (int) ((1F) * 255.0F)));
				int tessColor5Green = Math.max(0, Math.min(255, (int) ((1F) * 255.0F)));
				int tessColor5Blue = Math.max(0, Math.min(255, (int) ((1F) * 255.0F)));
				int tessColor5Alpha = Math.max(0, Math.min(255, (int) ((min) * 255.0F)));
				tessBuffer.pos(-0.5, dist, 0.5).color(tessColor5Red, tessColor5Green, tessColor5Blue, tessColor5Alpha).endVertex();
				tessBuffer.pos(0.5, dist, 0.5).color(tessColor5Red, tessColor5Green, tessColor5Blue, tessColor5Alpha).endVertex();

				int tessColor6Red = Math.max(0, Math.min(255, (int) ((1F) * 255.0F)));
				int tessColor6Green = Math.max(0, Math.min(255, (int) ((1F) * 255.0F)));
				int tessColor6Blue = Math.max(0, Math.min(255, (int) ((1F) * 255.0F)));
				int tessColor6Alpha = Math.max(0, Math.min(255, (int) ((max) * 255.0F)));
				tessBuffer.pos(0.5, 1.0625, -0.5).color(tessColor6Red, tessColor6Green, tessColor6Blue, tessColor6Alpha).endVertex();
				tessBuffer.pos(-0.5, 1.0625, -0.5).color(tessColor6Red, tessColor6Green, tessColor6Blue, tessColor6Alpha).endVertex();
				int tessColor7Red = Math.max(0, Math.min(255, (int) ((1F) * 255.0F)));
				int tessColor7Green = Math.max(0, Math.min(255, (int) ((1F) * 255.0F)));
				int tessColor7Blue = Math.max(0, Math.min(255, (int) ((1F) * 255.0F)));
				int tessColor7Alpha = Math.max(0, Math.min(255, (int) ((min) * 255.0F)));
				tessBuffer.pos(-0.5, dist, -0.5).color(tessColor7Red, tessColor7Green, tessColor7Blue, tessColor7Alpha).endVertex();
				tessBuffer.pos(0.5, dist, -0.5).color(tessColor7Red, tessColor7Green, tessColor7Blue, tessColor7Alpha).endVertex();
				
				tess.draw();
				GL11.glPopMatrix();
			}

			GL11.glDepthMask(true);
			GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
			GL11.glShadeModel(GL11.GL_FLAT);
			GL11.glDisable(GL11.GL_BLEND);
			GL11.glEnable(GL11.GL_CULL_FACE);
			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glEnable(GL11.GL_TEXTURE_2D);
		}

		GL11.glPopMatrix();
	}

}
