package com.hbm.particle;

import java.util.Random;

import org.lwjgl.opengl.GL11;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL12;

import com.hbm.lib.RefStrings;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

@SideOnly(Side.CLIENT)
public class ParticleHaze extends Particle {

	private static final ResourceLocation texture = new ResourceLocation(RefStrings.MODID + ":textures/particle/haze.png");
	private final TextureManager theRenderEngine;
	private final int maxAge;

	public ParticleHaze(TextureManager p_i1213_1_, World p_i1218_1_, double p_i1218_2_, double p_i1218_4_, double p_i1218_6_) {
		super(p_i1218_1_, p_i1218_2_, p_i1218_4_, p_i1218_6_);
		theRenderEngine = p_i1213_1_;
		maxAge = 600 + rand.nextInt(100);

		this.particleRed = this.particleGreen = this.particleBlue = 0;
		this.particleScale = 10F;
	}

	public ParticleHaze(TextureManager p_i1213_1_, World p_i1218_1_, double p_i1218_2_, double p_i1218_4_, double p_i1218_6_, float red, float green, float blue, float scale) {
		super(p_i1218_1_, p_i1218_2_, p_i1218_4_, p_i1218_6_);
		theRenderEngine = p_i1213_1_;
		maxAge = 100 + rand.nextInt(40);

		this.particleRed = red;
		this.particleGreen = green;
		this.particleBlue = blue;

		this.particleScale = scale;
	}

	public void onUpdate() {
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;

		this.particleAge++;

		if(this.particleAge >= maxAge) {
			this.setExpired();
		}

		this.motionX *= 0.9599999785423279D;
		this.motionY *= 0.9599999785423279D;
		this.motionZ *= 0.9599999785423279D;

		if(this.onGround) {
			this.motionX *= 0.699999988079071D;
			this.motionZ *= 0.699999988079071D;
		}

		int x = (int)Math.floor(posX) + rand.nextInt(15) - 7;
		int z = (int)Math.floor(posZ) + rand.nextInt(15) - 7;
		int y = world.getHeight(new net.minecraft.util.math.BlockPos(x, 0, z)).getY();
		world.spawnParticle(net.minecraft.util.EnumParticleTypes.LAVA, x + rand.nextDouble(), y + 0.1, z + rand.nextDouble(), 0.0, 0.0, 0.0);
	}

	public int getFXLayer() {
		return 3;
	}

	public void renderParticle(BufferBuilder tess, Entity camera, float p_70539_2_, float p_70539_3_, float p_70539_4_, float p_70539_5_, float p_70539_6_, float p_70539_7_) {

		this.theRenderEngine.bindTexture(texture);

		float alpha = 0;

		alpha = (float) Math.sin(particleAge * Math.PI / (400F)) * 0.25F;

		GlStateManager.color(1.0F, 1.0F, 1.0F, alpha * 0.1F);
		GlStateManager.disableLighting();
		GlStateManager.enableBlend();
		GlStateManager.enableRescaleNormal();
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0);
		GlStateManager.depthMask(false);
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		RenderHelper.disableStandardItemLighting();

		Random rand = new Random(50);

		GlStateManager.pushMatrix();
		for(int i = 0; i < 25; i++) {

			double dX = rand.nextGaussian() * 2.5D;
			double dY = rand.nextGaussian() * 0.15D;
			double dZ = rand.nextGaussian() * 2.5D;
			double size = (rand.nextDouble() * 0.25 + 0.75) * particleScale;

			GlStateManager.translate((float) dX, (float) dY, (float) dZ);

			float pX = (float) ((this.prevPosX + (this.posX - this.prevPosX) * (double) p_70539_2_ - interpPosX) + rand.nextGaussian() * 0.5);
			float pY = (float) ((this.prevPosY + (this.posY - this.prevPosY) * (double) p_70539_2_ - interpPosY) + rand.nextGaussian() * 0.5);
			float pZ = (float) ((this.prevPosZ + (this.posZ - this.prevPosZ) * (double) p_70539_2_ - interpPosZ) + rand.nextGaussian() * 0.5);

			tess.begin(GL11.GL_QUADS, ParticleVertexFormats.LIT_TEXTURED);


			tess.pos((double) (pX - p_70539_3_ * size - p_70539_6_ * size), (double) (pY - p_70539_4_ * size), (double) (pZ - p_70539_5_ * size - p_70539_7_ * size)).tex(1, 1).color(1F, 1F, 1F, alpha * .1F).lightmap(0, 240).normal(0, 1, 0).endVertex();
			tess.pos((double) (pX - p_70539_3_ * size + p_70539_6_ * size), (double) (pY + p_70539_4_ * size), (double) (pZ - p_70539_5_ * size + p_70539_7_ * size)).tex(1, 0).color(1F, 1F, 1F, alpha * .1F).lightmap(0, 240).normal(0, 1, 0).endVertex();
			tess.pos((double) (pX + p_70539_3_ * size + p_70539_6_ * size), (double) (pY + p_70539_4_ * size), (double) (pZ + p_70539_5_ * size + p_70539_7_ * size)).tex(0, 0).color(1F, 1F, 1F, alpha * .1F).lightmap(0, 240).normal(0, 1, 0).endVertex();
			tess.pos((double) (pX + p_70539_3_ * size - p_70539_6_ * size), (double) (pY - p_70539_4_ * size), (double) (pZ + p_70539_5_ * size - p_70539_7_ * size)).tex(0, 1).color(1F, 1F, 1F, alpha * .1F).lightmap(0, 240).normal(0, 1, 0).endVertex();
			Tessellator.getInstance().draw();
		}
		GlStateManager.popMatrix();

		GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
		GlStateManager.disableRescaleNormal();
		GlStateManager.enableLighting();
		GlStateManager.depthMask(true);

		GlStateManager.doPolygonOffset(0.0F, 0.0F);
		GlStateManager.enableLighting();
	}
}
