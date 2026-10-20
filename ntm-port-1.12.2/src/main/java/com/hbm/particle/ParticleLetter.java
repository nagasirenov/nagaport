package com.hbm.particle;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ParticleLetter extends Particle {
	
	int color;
	char c;
	
	public ParticleLetter(World world, double x, double y, double z, int color, char c) {
		super(world, x, y, z);
		this.particleMaxAge = 30;
		this.color = color;
		this.c = c;
	}

	public int getFXLayer() {
		return 3;
	}

	public void renderParticle(BufferBuilder tess, Entity camera, float interp, float x, float y, float z, float tx, float tz) {

	    GL11.glPushMatrix();
	    
		GlStateManager.disableLighting();
		GlStateManager.enableBlend();
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0);
		GlStateManager.depthMask(false);
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 0.0F);
		RenderHelper.disableStandardItemLighting();
		
		Minecraft mc = Minecraft.getMinecraft();
	    FontRenderer font = mc.fontRenderer;
	    
		Entity view = camera != null ? camera : mc.player;
		if(view == null) { GL11.glPopMatrix(); return; }
		float yaw = -view.rotationYaw;
		float pitch = view.rotationPitch;
		
	    float pX = (float) (this.prevPosX + (this.posX - this.prevPosX) * (double)interp - interpPosX);
	    float pY = (float) (this.prevPosY + (this.posY - this.prevPosY) * (double)interp - interpPosY);
	    float pZ = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * (double)interp - interpPosZ);
	    
	    GL11.glTranslatef(pX, pY, pZ);
	    GL11.glRotatef(yaw, 0.0F, 1.0F, 0.0F);
	    GL11.glRotatef(pitch, 1.0F, 0.0F, 0.0F);
	    GL11.glScalef(-1.0F, -1.0F, 1.0F);
	    
	    float time = (this.particleAge + interp) * 4F / this.particleMaxAge;
	    
	    double scale = 1 - (1D / Math.pow(Math.E, time));
	    
		this.particleAlpha = 1 - (((float)this.particleAge + interp) / (float)this.particleMaxAge);
		
		if(particleAlpha < 0)
			particleAlpha = 0;
		
		int alpha = (int) (particleAlpha * 255);
		
		if(alpha > 255)
			alpha = 255;
		
		if(alpha < 10)
			alpha = 10;
		
		int col = color + (alpha << 24);
	    
	    GL11.glScaled(scale, scale, scale);
	    
	    font.drawString(String.valueOf(c), -(int)(font.getStringWidth(String.valueOf(c)) * 0.5F), -(int)(font.FONT_HEIGHT * 0.5F), col);
	    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		
		GlStateManager.doPolygonOffset(0.0F, 0.0F);
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
		GlStateManager.enableLighting();
		
	    GL11.glPopMatrix();
	}


}
