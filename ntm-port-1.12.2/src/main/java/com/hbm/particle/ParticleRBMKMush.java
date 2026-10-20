package com.hbm.particle;

import org.lwjgl.opengl.GL11;
import net.minecraft.client.renderer.GlStateManager;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

//gracefully copy-pasted from xR
@SideOnly(Side.CLIENT)
public class ParticleRBMKMush extends Particle {

	private static final ResourceLocation texture = new ResourceLocation(com.hbm.lib.RefStrings.MODID + ":textures/particle/rbmk_mush.png");
	private TextureManager theRenderEngine;
	private int age;
	private int maxAge;

	public ParticleRBMKMush(TextureManager p_i1213_1_, World p_i1218_1_, double p_i1218_2_, double p_i1218_4_, double p_i1218_6_, float scale) {
		super(p_i1218_1_, p_i1218_2_, p_i1218_4_, p_i1218_6_);
		theRenderEngine = p_i1213_1_;
		maxAge = 50;

		this.particleRed = this.particleGreen = this.particleBlue = 0;
		
		this.particleScale = scale;
	}

	public ParticleRBMKMush(TextureManager p_i1213_1_, World p_i1218_1_, double p_i1218_2_, double p_i1218_4_, double p_i1218_6_, float red, float green, float blue, float scale) {
		super(p_i1218_1_, p_i1218_2_, p_i1218_4_, p_i1218_6_);
		theRenderEngine = p_i1213_1_;
		maxAge = 50;

		this.particleRed = red;
		this.particleGreen = green;
		this.particleBlue = blue;

		this.particleScale = scale;
	}

	public void onUpdate() {
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;

		++this.age;

		if(this.age == this.maxAge) {
			this.setExpired();
		}
	}

	public int getFXLayer() {
		return 3;
	}

	public void renderParticle(BufferBuilder tessellator, Entity camera, float p_70539_2_, float x, float y, float z, float sx, float sz) {

		this.theRenderEngine.bindTexture(texture);

		int segs = 30;

		// the size of one frame
		double frame = 1D / (double) segs;
		// how many frames we're in
		int prog = age * segs / maxAge;

		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.disableLighting();
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0);
		GlStateManager.depthMask(false);
		RenderHelper.disableStandardItemLighting();
		
		boolean fog = GL11.glIsEnabled(GL11.GL_FOG);
		if(fog) GlStateManager.disableFog();

		tessellator.begin(GL11.GL_QUADS, ParticleVertexFormats.LIT_TEXTURED);




		float scale = this.particleScale;
		float pX = (float) ((this.prevPosX + (this.posX - this.prevPosX) * (double) p_70539_2_ - interpPosX));
		float pY = (float) ((this.prevPosY + (this.posY - this.prevPosY) * (double) p_70539_2_ - interpPosY)) + particleScale;
		float pZ = (float) ((this.prevPosZ + (this.posZ - this.prevPosZ) * (double) p_70539_2_ - interpPosZ));

		tessellator.pos((double) (pX - x * scale - sx * scale), (double) (pY - y * scale), (double) (pZ - z * scale - sz * scale)).tex(1, (prog + 1) * frame).color(1F, 1F, 1F, 1F).lightmap(0, 240).normal(0, 1, 0).endVertex();
		tessellator.pos((double) (pX - x * scale + sx * scale), (double) (pY + y * scale), (double) (pZ - z * scale + sz * scale)).tex(1, prog * frame).color(1F, 1F, 1F, 1F).lightmap(0, 240).normal(0, 1, 0).endVertex();
		tessellator.pos((double) (pX + x * scale + sx * scale), (double) (pY + y * scale), (double) (pZ + z * scale + sz * scale)).tex(0, prog * frame).color(1F, 1F, 1F, 1F).lightmap(0, 240).normal(0, 1, 0).endVertex();
		tessellator.pos((double) (pX + x * scale - sx * scale), (double) (pY - y * scale), (double) (pZ + z * scale - sz * scale)).tex(0, (prog + 1) * frame).color(1F, 1F, 1F, 1F).lightmap(0, 240).normal(0, 1, 0).endVertex();
		Tessellator.getInstance().draw();

		if(fog) GlStateManager.enableFog();
		GlStateManager.doPolygonOffset(0.0F, 0.0F);
		GlStateManager.enableLighting();
	}
}
