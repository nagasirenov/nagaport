package com.hbm.particle;

import java.util.Random;

import org.lwjgl.opengl.GL11;

import com.hbm.lib.RefStrings;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
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
public class ParticleMukeFlash extends Particle {

	private static final ResourceLocation texture = new ResourceLocation(RefStrings.MODID + ":textures/particle/flare.png");
	private TextureManager theRenderEngine;

	boolean bf;

	public ParticleMukeFlash(TextureManager texman, World world, double x, double y, double z, boolean bf) {
		super(world, x, y, z);
		this.theRenderEngine = texman;
		this.particleMaxAge = 20;
		this.bf = bf;
	}

	public int getFXLayer() {
		return 3;
	}

	public void onUpdate() {
		super.onUpdate();

		if(this.particleAge == 15) {

			// Stem
			for(double d = 0.0D; d <= 1.8D; d += 0.1) {
				ParticleMukeCloud cloud = getCloud(theRenderEngine, world, posX, posY, posZ, rand.nextGaussian() * 0.05, d + rand.nextGaussian() * 0.02, rand.nextGaussian() * 0.05);
				Minecraft.getMinecraft().effectRenderer.addEffect(cloud);
			}

			// Ground
			for(int i = 0; i < 100; i++) {
				ParticleMukeCloud cloud = getCloud(theRenderEngine, world, posX, posY + 0.5, posZ, rand.nextGaussian() * 0.5, rand.nextInt(5) == 0 ? 0.02 : 0, rand.nextGaussian() * 0.5);
				Minecraft.getMinecraft().effectRenderer.addEffect(cloud);
			}

			// Mush
			for(int i = 0; i < 75; i++) {
				double x = rand.nextGaussian() * 0.5;
				double z = rand.nextGaussian() * 0.5;

				if(x * x + z * z > 1.5) {
					x *= 0.5;
					z *= 0.5;
				}

				double y = 1.8 + (rand.nextDouble() * 3 - 1.5) * (0.75 - (x * x + z * z)) * 0.5;

				ParticleMukeCloud cloud = getCloud(theRenderEngine, world, posX, posY, posZ, x, y + rand.nextGaussian() * 0.02, z);
				Minecraft.getMinecraft().effectRenderer.addEffect(cloud);
			}
		}
	}

	private ParticleMukeCloud getCloud(TextureManager texman, World world, double x, double y, double z, double mx, double my, double mz) {

		if(this.bf) {
			return new ParticleMukeCloudBF(theRenderEngine, world, x, y, z, mx, my, mz);
		} else {
			return new ParticleMukeCloud(theRenderEngine, world, x, y, z, mx, my, mz);
		}
	}

	public void renderParticle(BufferBuilder tess, Entity camera, float interp, float x, float y, float z, float tx, float tz) {

		this.theRenderEngine.bindTexture(texture);
		boolean fog = GL11.glIsEnabled(GL11.GL_FOG);
		if(fog) GlStateManager.disableFog();

		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.disableLighting();
		GlStateManager.enableBlend();
		GlStateManager.depthMask(false);
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0);
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		RenderHelper.disableStandardItemLighting();

		tess.begin(7, ParticleVertexFormats.LIT_TEXTURED);




		this.particleAlpha = 1 - (((float) this.particleAge + interp) / (float) this.particleMaxAge);
		float scale = (this.particleAge + interp) * 3F + 1F;



		float dX = (float) (this.prevPosX + (this.posX - this.prevPosX) * (double) interp - interpPosX);
		float dY = (float) (this.prevPosY + (this.posY - this.prevPosY) * (double) interp - interpPosY);
		float dZ = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * (double) interp - interpPosZ);

		Random rand = new Random();

		for(int i = 0; i < 24; i++) {

			rand.setSeed(i * 31 + 1);

			float pX = (float) (dX + rand.nextDouble() * 15 - 7.5);
			float pY = (float) (dY + rand.nextDouble() * 7.5 - 3.75);
			float pZ = (float) (dZ + rand.nextDouble() * 15 - 7.5);

			tess.pos((double) (pX - x * scale - tx * scale), (double) (pY - y * scale), (double) (pZ - z * scale - tz * scale)).tex(1, 1).color(1.0F, 0.9F, 0.75F, this.particleAlpha * 0.5F).lightmap((240) >> 16 & 65535, (240) & 65535).normal(0, 1, 0).endVertex();
			tess.pos((double) (pX - x * scale + tx * scale), (double) (pY + y * scale), (double) (pZ - z * scale + tz * scale)).tex(1, 0).color(1.0F, 0.9F, 0.75F, this.particleAlpha * 0.5F).lightmap((240) >> 16 & 65535, (240) & 65535).normal(0, 1, 0).endVertex();
			tess.pos((double) (pX + x * scale + tx * scale), (double) (pY + y * scale), (double) (pZ + z * scale + tz * scale)).tex(0, 0).color(1.0F, 0.9F, 0.75F, this.particleAlpha * 0.5F).lightmap((240) >> 16 & 65535, (240) & 65535).normal(0, 1, 0).endVertex();
			tess.pos((double) (pX + x * scale - tx * scale), (double) (pY - y * scale), (double) (pZ + z * scale - tz * scale)).tex(0, 1).color(1.0F, 0.9F, 0.75F, this.particleAlpha * 0.5F).lightmap((240) >> 16 & 65535, (240) & 65535).normal(0, 1, 0).endVertex();
		}
		Tessellator.getInstance().draw();

		GlStateManager.doPolygonOffset(0.0F, 0.0F);
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
		GlStateManager.enableLighting();
		if(fog) GlStateManager.enableFog();
	}
}
