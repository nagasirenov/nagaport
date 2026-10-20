package com.hbm.render.tileentity;

import java.util.Random;

import org.lwjgl.opengl.GL11;

import com.hbm.blocks.ModBlocks;
import com.hbm.main.MainRegistry;
import com.hbm.main.ResourceManager;
import com.hbm.render.util.BeamPronter;
import com.hbm.render.util.BeamPronter.EnumBeamType;
import com.hbm.render.util.BeamPronter.EnumWaveType;
import com.hbm.tileentity.machine.storage.TileEntityBatteryREDD;
import com.hbm.util.BobMathUtil;
import com.hbm.util.Clock;
import com.hbm.util.Vec3NT;

import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.Vec3d;

public class RenderBatteryREDD extends TileEntitySpecialRenderer {

	@Override
	public void render(TileEntity tile, double x, double y, double z, float interp, int nativeDestroyStage, float nativeAlpha) {
		GL11.glPushMatrix();
		GL11.glTranslated(x + 0.5, y, z + 0.5);
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_CULL_FACE);

		switch(tile.getBlockMetadata() - 10) {
		case 2: GL11.glRotatef(270, 0F, 1F, 0F); break;
		case 4: GL11.glRotatef(0, 0F, 1F, 0F); break;
		case 3: GL11.glRotatef(90, 0F, 1F, 0F); break;
		case 5: GL11.glRotatef(180, 0F, 1F, 0F); break;
		}
		
		GL11.glShadeModel(GL11.GL_SMOOTH);
		
		bindTexture(ResourceManager.battery_redd_tex);
		ResourceManager.battery_redd.renderPart("Base");
		
		GL11.glPushMatrix();

		GL11.glTranslated(0, 5.5, 0);
		TileEntityBatteryREDD redd = (TileEntityBatteryREDD) tile;
		float speed = redd.getSpeed();
		double rot = redd.prevRotation + (redd.rotation - redd.prevRotation) * interp;
		GL11.glRotated(rot, 1, 0, 0);
		GL11.glTranslated(0, -5.5, 0);
		
		ResourceManager.battery_redd.renderPart("Wheel");
		
		RenderArcFurnace.fullbright(true);
		ResourceManager.battery_redd.renderPart("Lights");
		
		RenderArcFurnace.fullbright(false);

		GL11.glPushMatrix();
		GL11.glTranslated(0, 5.5, 0);

		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);
		GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glDisable(GL11.GL_TEXTURE_2D);

		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glPushAttrib(GL11.GL_LIGHTING_BIT);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		GL11.glDepthMask(false);
		
		Vec3NT vec = new Vec3NT(0, 0, 4);
		
		Tessellator tess = Tessellator.getInstance();
		BufferBuilder tessBuffer = tess.getBuffer();
		tessBuffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
		
		double len = 4.25D;
		double width = 0.125D;
		double span = speed * 0.75;
		
		if(span > 0) for(int j = -1; j <= 1; j += 2) {
			for(int i = 0; i < 8; i++) {
				double xOffset = 0.8125 * j;
				vec.setComponents(0, 1, 0);
				vec.rotateAroundXDeg(i * 45D);
				
				int tessColor0Red = Math.max(0, Math.min(255, (int) ((1F) * 255.0F)));
				int tessColor0Green = Math.max(0, Math.min(255, (int) ((1F) * 255.0F)));
				int tessColor0Blue = Math.max(0, Math.min(255, (int) ((0F) * 255.0F)));
				int tessColor0Alpha = Math.max(0, Math.min(255, (int) ((0.75F) * 255.0F)));
				tessBuffer.pos(xOffset, vec.y * len - vec.y * width, vec.z * len - vec.z * width).color(tessColor0Red, tessColor0Green, tessColor0Blue, tessColor0Alpha).endVertex();
				tessBuffer.pos(xOffset, vec.y * len + vec.y * width, vec.z * len + vec.z * width).color(tessColor0Red, tessColor0Green, tessColor0Blue, tessColor0Alpha).endVertex();
				vec.rotateAroundXDeg(span);
				int tessColor1Red = Math.max(0, Math.min(255, (int) ((1F) * 255.0F)));
				int tessColor1Green = Math.max(0, Math.min(255, (int) ((1F) * 255.0F)));
				int tessColor1Blue = Math.max(0, Math.min(255, (int) ((0F) * 255.0F)));
				int tessColor1Alpha = Math.max(0, Math.min(255, (int) ((0.5F) * 255.0F)));
				tessBuffer.pos(xOffset, vec.y * len + vec.y * width, vec.z * len + vec.z * width).color(tessColor1Red, tessColor1Green, tessColor1Blue, tessColor1Alpha).endVertex();
				tessBuffer.pos(xOffset, vec.y * len - vec.y * width, vec.z * len - vec.z * width).color(tessColor1Red, tessColor1Green, tessColor1Blue, tessColor1Alpha).endVertex();
				
				int tessColor2Red = Math.max(0, Math.min(255, (int) ((1F) * 255.0F)));
				int tessColor2Green = Math.max(0, Math.min(255, (int) ((1F) * 255.0F)));
				int tessColor2Blue = Math.max(0, Math.min(255, (int) ((0F) * 255.0F)));
				int tessColor2Alpha = Math.max(0, Math.min(255, (int) ((0.5F) * 255.0F)));
				tessBuffer.pos(xOffset, vec.y * len - vec.y * width, vec.z * len - vec.z * width).color(tessColor2Red, tessColor2Green, tessColor2Blue, tessColor2Alpha).endVertex();
				tessBuffer.pos(xOffset, vec.y * len + vec.y * width, vec.z * len + vec.z * width).color(tessColor2Red, tessColor2Green, tessColor2Blue, tessColor2Alpha).endVertex();
				vec.rotateAroundXDeg(span);
				int tessColor3Red = Math.max(0, Math.min(255, (int) ((1F) * 255.0F)));
				int tessColor3Green = Math.max(0, Math.min(255, (int) ((1F) * 255.0F)));
				int tessColor3Blue = Math.max(0, Math.min(255, (int) ((0F) * 255.0F)));
				int tessColor3Alpha = Math.max(0, Math.min(255, (int) ((0.25F) * 255.0F)));
				tessBuffer.pos(xOffset, vec.y * len + vec.y * width, vec.z * len + vec.z * width).color(tessColor3Red, tessColor3Green, tessColor3Blue, tessColor3Alpha).endVertex();
				tessBuffer.pos(xOffset, vec.y * len - vec.y * width, vec.z * len - vec.z * width).color(tessColor3Red, tessColor3Green, tessColor3Blue, tessColor3Alpha).endVertex();
				
				int tessColor4Red = Math.max(0, Math.min(255, (int) ((1F) * 255.0F)));
				int tessColor4Green = Math.max(0, Math.min(255, (int) ((1F) * 255.0F)));
				int tessColor4Blue = Math.max(0, Math.min(255, (int) ((0F) * 255.0F)));
				int tessColor4Alpha = Math.max(0, Math.min(255, (int) ((0.25F) * 255.0F)));
				tessBuffer.pos(xOffset, vec.y * len - vec.y * width, vec.z * len - vec.z * width).color(tessColor4Red, tessColor4Green, tessColor4Blue, tessColor4Alpha).endVertex();
				tessBuffer.pos(xOffset, vec.y * len + vec.y * width, vec.z * len + vec.z * width).color(tessColor4Red, tessColor4Green, tessColor4Blue, tessColor4Alpha).endVertex();
				vec.rotateAroundXDeg(span);
				int tessColor5Red = Math.max(0, Math.min(255, (int) ((1F) * 255.0F)));
				int tessColor5Green = Math.max(0, Math.min(255, (int) ((1F) * 255.0F)));
				int tessColor5Blue = Math.max(0, Math.min(255, (int) ((0F) * 255.0F)));
				int tessColor5Alpha = Math.max(0, Math.min(255, (int) ((0F) * 255.0F)));
				tessBuffer.pos(xOffset, vec.y * len + vec.y * width, vec.z * len + vec.z * width).color(tessColor5Red, tessColor5Green, tessColor5Blue, tessColor5Alpha).endVertex();
				tessBuffer.pos(xOffset, vec.y * len - vec.y * width, vec.z * len - vec.z * width).color(tessColor5Red, tessColor5Green, tessColor5Blue, tessColor5Alpha).endVertex();
			}
		}
		tess.draw();

		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glDepthMask(true);
		GL11.glPopAttrib();
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		
		GL11.glPopMatrix();
		
		renderSparkle(tile);
		
		GL11.glPopMatrix();
		
		if(speed > 0) renderZaps(tile);
		
		GL11.glShadeModel(GL11.GL_FLAT);
		
		GL11.glPopMatrix();
	}
	
	protected void renderSparkle(TileEntity tile) {
		TileEntityBatteryREDD redd = (TileEntityBatteryREDD) tile;
		
		long time = Clock.get_ms();
		float alpha = 0.45F + (float) (Math.sin(time / 1000D) * 0.15F);
		float alphaMult = redd.getSpeed() / 15F;
		float r = 1.0F;
		float g = 0.25F;
		float b = 0.75F;
		
		double mainOsc = BobMathUtil.sps(time / 1000D) % 1D;
		double sparkleSpin = time / 250D * -1 % 1D;
		double sparkleOsc = Math.sin(time / 1000D) * 0.5D % 1D;

		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);
		GL11.glDisable(GL11.GL_CULL_FACE);

		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glPushAttrib(GL11.GL_LIGHTING_BIT);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		GL11.glDepthMask(false);

		GL11.glColor4f(r, g, b, alpha * alphaMult);
		
		GL11.glMatrixMode(GL11.GL_TEXTURE);
		GL11.glLoadIdentity();
		bindTexture(ResourceManager.fusion_plasma_tex);
		GL11.glTranslated(0, mainOsc, 0);
		ResourceManager.battery_redd.renderPart("Plasma");
		GL11.glMatrixMode(GL11.GL_TEXTURE);
		GL11.glLoadIdentity();
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		
		// cost-cutting measure, don't render extra layers from more than 100m away
		if(MainRegistry.proxy.me().getDistanceSq(tile.getPos().getX() + 0.5, tile.getPos().getY() + 2.5, tile.getPos().getZ() + 0.5) < 100 * 100) {

			GL11.glColor4f(r * 2, g * 2, b * 2, 0.75F * alphaMult);
			
			GL11.glMatrixMode(GL11.GL_TEXTURE);
			GL11.glLoadIdentity();
			bindTexture(ResourceManager.fusion_plasma_sparkle_tex);
			GL11.glTranslated(sparkleSpin, sparkleOsc, 0);
			ResourceManager.battery_redd.renderPart("Plasma");
			GL11.glMatrixMode(GL11.GL_TEXTURE);
			GL11.glLoadIdentity();
			GL11.glMatrixMode(GL11.GL_MODELVIEW);
		}

		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glDepthMask(true);
		GL11.glPopAttrib();
		
		GL11.glEnable(GL11.GL_CULL_FACE);
	}
	
	protected void renderZaps(TileEntity tile) {
		
		Random rand = new Random(tile.getWorld().getTotalWorldTime() / 5);
		rand.nextBoolean();
		
		if(rand.nextBoolean()) {
			GL11.glPushMatrix();
			GL11.glTranslated(3.125, 5.5, 0);
			BeamPronter.prontBeam(new Vec3d(-1.375, -2.625, 3.75), EnumWaveType.RANDOM, EnumBeamType.SOLID, 0x404040, 0x002040, (int)(System.currentTimeMillis() % 1000) / 50, 15, 0.25F, 3, 0.0625F);
			BeamPronter.prontBeam(new Vec3d(-1.375, -2.625, 3.75), EnumWaveType.RANDOM, EnumBeamType.SOLID, 0x404040, 0x002040, (int)(System.currentTimeMillis() % 1000) / 50, 1, 0, 3, 0.0625F);
			GL11.glPopMatrix();
		}
		
		if(rand.nextBoolean()) {
			GL11.glPushMatrix();
			GL11.glTranslated(-3.125, 5.5, 0);
			BeamPronter.prontBeam(new Vec3d(1.375, -2.625, 3.75), EnumWaveType.RANDOM, EnumBeamType.SOLID, 0x404040, 0x002040, (int)(System.currentTimeMillis() % 1000) / 50, 15, 0.25F, 3, 0.0625F);
			BeamPronter.prontBeam(new Vec3d(1.375, -2.625, 3.75), EnumWaveType.RANDOM, EnumBeamType.SOLID, 0x404040, 0x002040, (int)(System.currentTimeMillis() % 1000) / 50, 1, 0, 3, 0.0625F);
			GL11.glPopMatrix();
		}
		
		if(rand.nextBoolean()) {
			GL11.glPushMatrix();
			GL11.glTranslated(3.125, 5.5, 0);
			BeamPronter.prontBeam(new Vec3d(-1.375, -2.625, -3.75), EnumWaveType.RANDOM, EnumBeamType.SOLID, 0x404040, 0x002040, (int)(System.currentTimeMillis() % 1000) / 50, 15, 0.25F, 3, 0.0625F);
			BeamPronter.prontBeam(new Vec3d(-1.375, -2.625, -3.75), EnumWaveType.RANDOM, EnumBeamType.SOLID, 0x404040, 0x002040, (int)(System.currentTimeMillis() % 1000) / 50, 1, 0, 3, 0.0625F);
			GL11.glPopMatrix();
		}
		
		if(rand.nextBoolean()) {
			GL11.glPushMatrix();
			GL11.glTranslated(-3.125, 5.5, 0);
			BeamPronter.prontBeam(new Vec3d(1.375, -2.625, -3.75), EnumWaveType.RANDOM, EnumBeamType.SOLID, 0x404040, 0x002040, (int)(System.currentTimeMillis() % 1000) / 50, 15, 0.25F, 3, 0.0625F);
			BeamPronter.prontBeam(new Vec3d(1.375, -2.625, -3.75), EnumWaveType.RANDOM, EnumBeamType.SOLID, 0x404040, 0x002040, (int)(System.currentTimeMillis() % 1000) / 50, 1, 0, 3, 0.0625F);
			GL11.glPopMatrix();
		}
	}



}
