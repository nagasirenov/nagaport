package com.hbm.particle;

import java.util.Random;

import org.lwjgl.opengl.GL11;

import com.hbm.lib.RefStrings;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

@SideOnly(Side.CLIENT)
public class ParticleSmokePlume extends Particle {
	public void setMotion(double x, double y, double z) { this.motionX = x; this.motionY = y; this.motionZ = z; }

	// Purely cosmetic variation; native particles do not allocate Entity IDs.
	protected final int visualSeed = System.identityHashCode(this) & Integer.MAX_VALUE;

	private static final ResourceLocation texture = new ResourceLocation(RefStrings.MODID + ":textures/particle/contrail.png");
	private TextureManager theRenderEngine;
	private int age;
	private int maxAge;

	public ParticleSmokePlume(TextureManager p_i1213_1_, World p_i1218_1_, double p_i1218_2_, double p_i1218_4_, double p_i1218_6_) {
		super(p_i1218_1_, p_i1218_2_, p_i1218_4_, p_i1218_6_);
		theRenderEngine = p_i1213_1_;
		maxAge = 80 + rand.nextInt(20);
		this.particleScale = 0.25F;
	}

	public void onUpdate() {
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;

		particleAlpha = 1 - ((float) age / (float) maxAge);
		float prevScale = this.particleScale;
		this.particleScale = 0.25F + ((float) age / (float) maxAge) * 2;

		++this.age;

		if(this.age == this.maxAge) {
			this.setExpired();
		}

		double bak = new Vec3d(motionX, motionY, motionZ).length();

		double startY = this.posY;
		double requestedY = this.motionY + (this.particleScale - prevScale);
		this.move(this.motionX, requestedY, this.motionZ);

		if(Math.abs((this.posY - startY) - requestedY) > 1.0E-7) {
			motionY = bak;
		}

		motionX *= 0.925;
		motionY *= 0.925;
		motionZ *= 0.925;
	}

	public int getFXLayer() {
		return 3;
	}

	public void renderParticle(BufferBuilder p_70539_1_, Entity camera, float p_70539_2_, float p_70539_3_, float p_70539_4_, float p_70539_5_, float p_70539_6_, float p_70539_7_) {

		this.theRenderEngine.bindTexture(texture);

		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.disableLighting();
		GlStateManager.enableBlend();
		GlStateManager.depthMask(false);
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		RenderHelper.disableStandardItemLighting();

		Random urandom = new Random(this.visualSeed);
		
		Entity player = camera != null ? camera : Minecraft.getMinecraft().player;
		if(player == null) return;
		double dX = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double)p_70539_2_;
		double dY = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double)p_70539_2_;
		double dZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double)p_70539_2_;

		for(int i = 0; i < 6; i++) {

			p_70539_1_.begin(7, ParticleVertexFormats.LIT_TEXTURED);

			this.particleRed = this.particleGreen = this.particleBlue = urandom.nextFloat() * 0.75F + 0.1F;





			float scale = this.particleScale;
			float pX = (float) ((this.prevPosX + (this.posX - this.prevPosX) * (double) p_70539_2_ - dX) + urandom.nextGaussian() * 0.5 * scale);
			float pY = (float) ((this.prevPosY + (this.posY - this.prevPosY) * (double) p_70539_2_ - dY) + urandom.nextGaussian() * 0.5 * scale);
			float pZ = (float) ((this.prevPosZ + (this.posZ - this.prevPosZ) * (double) p_70539_2_ - dZ) + urandom.nextGaussian() * 0.5 * scale);

			p_70539_1_.pos((double) (pX - p_70539_3_ * scale - p_70539_6_ * scale), (double) (pY - p_70539_4_ * scale), (double) (pZ - p_70539_5_ * scale - p_70539_7_ * scale)).tex(1, 1).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap((240) >> 16 & 65535, (240) & 65535).normal(0, 1, 0).endVertex();
			p_70539_1_.pos((double) (pX - p_70539_3_ * scale + p_70539_6_ * scale), (double) (pY + p_70539_4_ * scale), (double) (pZ - p_70539_5_ * scale + p_70539_7_ * scale)).tex(1, 0).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap((240) >> 16 & 65535, (240) & 65535).normal(0, 1, 0).endVertex();
			p_70539_1_.pos((double) (pX + p_70539_3_ * scale + p_70539_6_ * scale), (double) (pY + p_70539_4_ * scale), (double) (pZ + p_70539_5_ * scale + p_70539_7_ * scale)).tex(0, 0).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap((240) >> 16 & 65535, (240) & 65535).normal(0, 1, 0).endVertex();
			p_70539_1_.pos((double) (pX + p_70539_3_ * scale - p_70539_6_ * scale), (double) (pY - p_70539_4_ * scale), (double) (pZ + p_70539_5_ * scale - p_70539_7_ * scale)).tex(0, 1).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap((240) >> 16 & 65535, (240) & 65535).normal(0, 1, 0).endVertex();
			Tessellator.getInstance().draw();
		}

		GlStateManager.doPolygonOffset(0.0F, 0.0F);
		GlStateManager.enableLighting();
	}
}
