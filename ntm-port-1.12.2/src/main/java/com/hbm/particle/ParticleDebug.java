package com.hbm.particle;

import org.lwjgl.opengl.GL11;
import net.minecraft.client.renderer.GlStateManager;

import com.hbm.lib.RefStrings;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

@SideOnly(Side.CLIENT)
public class ParticleDebug extends Particle {

	private static final ResourceLocation power = new ResourceLocation(RefStrings.MODID + ":textures/particle/debug_power.png");
	private static final ResourceLocation fluid = new ResourceLocation(RefStrings.MODID + ":textures/particle/debug_fluid.png");
	private TextureManager theRenderEngine;
	private int type;

	public ParticleDebug(TextureManager texman, World world, double x, double y, double z, double motionX, double motionY, double motionZ) {
		super(world, x, y, z);
		this.theRenderEngine = texman;
		this.particleMaxAge = 10;
		this.type = 0;
		this.motionX = motionX;
		this.motionY = motionY;
		this.motionZ = motionZ;
		this.canCollide = false;
	}

	public ParticleDebug(TextureManager texman, World world, double x, double y, double z, double motionX, double motionY, double motionZ, int color) {
		this(texman, world, x, y, z, motionX, motionY, motionZ);
		this.type = 1;
		this.particleRed = ((color & 0xff0000) >> 16) / 255F;
		this.particleGreen = ((color & 0x00ff00) >> 8) / 255F;
		this.particleBlue = (color & 0x0000ff) / 255F;
	}

	public int getFXLayer() {
		return 3;
	}

	public void renderParticle(BufferBuilder buffer, Entity camera, float interp, float x, float y, float z, float tx, float tz) {

		this.theRenderEngine.bindTexture(type == 0 ? power : fluid);

		GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_CURRENT_BIT);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glDisable(GL11.GL_LIGHTING);

		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
		
		float scale = 0.05F;


		float pX = (float) (this.prevPosX + (this.posX - this.prevPosX) * (double) interp - interpPosX);
		float pY = (float) (this.prevPosY + (this.posY - this.prevPosY) * (double) interp - interpPosY);
		float pZ = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * (double) interp - interpPosZ);

		buffer.pos((double) (pX - x * scale - tx * scale), (double) (pY - y * scale), (double) (pZ - z * scale - tz * scale)).tex(1, 1).color(particleRed, particleGreen, particleBlue, particleAlpha).lightmap(0, 240).endVertex();
		buffer.pos((double) (pX - x * scale + tx * scale), (double) (pY + y * scale), (double) (pZ - z * scale + tz * scale)).tex(1, 0).color(particleRed, particleGreen, particleBlue, particleAlpha).lightmap(0, 240).endVertex();
		buffer.pos((double) (pX + x * scale + tx * scale), (double) (pY + y * scale), (double) (pZ + z * scale + tz * scale)).tex(0, 0).color(particleRed, particleGreen, particleBlue, particleAlpha).lightmap(0, 240).endVertex();
		buffer.pos((double) (pX + x * scale - tx * scale), (double) (pY - y * scale), (double) (pZ + z * scale - tz * scale)).tex(0, 1).color(particleRed, particleGreen, particleBlue, particleAlpha).lightmap(0, 240).endVertex();
		Tessellator.getInstance().draw();

		GlStateManager.doPolygonOffset(0.0F, 0.0F);
		GL11.glPopAttrib();
	}
}
