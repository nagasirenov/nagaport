package com.hbm.particle;

import org.lwjgl.opengl.GL11;

import com.hbm.lib.RefStrings;
import com.hbm.main.ResourceManager;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

@SideOnly(Side.CLIENT)
public class ParticleRift extends Particle {

	private static final ResourceLocation texture = new ResourceLocation(RefStrings.MODID + ":textures/particle/hadron.png");
	private TextureManager theRenderEngine;
	
	public ParticleRift(TextureManager texman, World world, double x, double y, double z) {
		super(world, x, y, z);
		this.theRenderEngine = texman;
		this.particleMaxAge = 10;
	}

	public int getFXLayer() {
		return 3;
	}

	public void renderParticle(BufferBuilder tess, Entity camera, float interp, float x, float y, float z, float tx, float tz) {
		
		this.theRenderEngine.bindTexture(texture);
		
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.disableLighting();
		GlStateManager.enableBlend();
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0);
		GlStateManager.depthMask(false);
		GlStateManager.tryBlendFuncSeparate(GL11.GL_ONE_MINUS_DST_COLOR, GL11.GL_ONE_MINUS_SRC_COLOR, 1, 0);
		RenderHelper.disableStandardItemLighting();
		
		float pX = (float) (this.prevPosX + (this.posX - this.prevPosX) * (double) interp - interpPosX);
		float pY = (float) (this.prevPosY + (this.posY - this.prevPosY) * (double) interp - interpPosY);
		float pZ = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * (double) interp - interpPosZ);
		
		float scale = (this.particleAge + interp) * 0.5F;
		
		GlStateManager.disableTexture2D();
		
		
		GL11.glPushMatrix();
		GL11.glTranslated(pX, pY, pZ);
		GlStateManager.color(1F, 1F, 1F, 1F);
		GlStateManager.enableCull();
		GL11.glScalef(scale, scale, scale);
		ResourceManager.sphere_uv.renderAll();
		GL11.glScalef(1.02F, 1.02F, 1.02F);
		ResourceManager.sphere_uv.renderAll();
		GL11.glScalef(1.05F, 1.05F, 1.05F);
		ResourceManager.sphere_uv.renderAll();
		GL11.glScalef(1.02F, 1.02F, 1.02F);
		ResourceManager.sphere_uv.renderAll();
		GL11.glScalef(1.05F, 1.05F, 1.05F);
		ResourceManager.sphere_uv.renderAll();
		GL11.glPopMatrix();

		GlStateManager.enableTexture2D();
		GlStateManager.doPolygonOffset(0.0F, 0.0F);
		GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
		GlStateManager.enableLighting();
	}
}
