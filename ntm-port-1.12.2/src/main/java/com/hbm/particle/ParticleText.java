package com.hbm.particle;

import net.minecraft.client.renderer.GlStateManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

public class ParticleText extends Particle {

	int color;
	String text;

	public ParticleText(World world, double x, double y, double z, int color, String text) {
		super(world, x, y, z);
		this.particleMaxAge = 100;
		this.color = color;
		this.text = text;
		
		this.motionY = 0.01D;
		this.canCollide = false;
	}

	public int getFXLayer() {
		return 3;
	}

	public void renderParticle(BufferBuilder buffer, Entity camera, float interp, float x, float y, float z, float tx, float tz) {

		GlStateManager.pushMatrix();

		GlStateManager.disableLighting();
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 0.0F);
		RenderHelper.disableStandardItemLighting();

		Minecraft mc = Minecraft.getMinecraft();
		FontRenderer font = mc.fontRenderer;

		Entity viewer = camera != null ? camera : mc.player;
		if(viewer == null) { GlStateManager.enableLighting(); GlStateManager.popMatrix(); return; }
		float yaw = -viewer.rotationYaw;
		float pitch = viewer.rotationPitch;

		float pX = (float) (this.prevPosX + (this.posX - this.prevPosX) * (double) interp - interpPosX);
		float pY = (float) (this.prevPosY + (this.posY - this.prevPosY) * (double) interp - interpPosY);
		float pZ = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * (double) interp - interpPosZ);

		GlStateManager.translate(pX, pY, pZ);
		GlStateManager.rotate(yaw, 0.0F, 1.0F, 0.0F);
		GlStateManager.rotate(pitch, 1.0F, 0.0F, 0.0F);
		GlStateManager.scale(-1.0F, -1.0F, 1.0F);

		GlStateManager.scale(particleScale * 0.01, particleScale * 0.01, particleScale * 0.01);

		font.drawStringWithShadow(text, -(int) (font.getStringWidth(text) * 0.5F), -(int) (font.FONT_HEIGHT * 0.5F), color);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

		GlStateManager.doPolygonOffset(0.0F, 0.0F);
		GlStateManager.enableLighting();

		GlStateManager.popMatrix();
	}
}
