package com.hbm.particle;

import org.lwjgl.opengl.GL11;

import com.hbm.lib.RefStrings;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.util.math.MathHelper;

@SideOnly(Side.CLIENT)
public class ParticleMukeWave extends Particle {

	private static final ResourceLocation texture = new ResourceLocation(RefStrings.MODID + ":textures/particle/shockwave.png");
	private TextureManager theRenderEngine;

	private float waveScale = 45F;

	public ParticleMukeWave(TextureManager texman,World world, double x, double y, double z) {
		super(world, x, y, z);
		this.canCollide = false;
		this.theRenderEngine = texman;
		this.particleMaxAge = 25;
	}

	/** This owned flash/jet stays at its spawn point; no Entity contact side effects. */
	@Override
	public void onUpdate() {
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;
		if(this.particleAge++ >= this.particleMaxAge) this.setExpired();
	}

	@Override
	public int getFXLayer() {
		return 3;
	}

	public void setup(float scale, int maxAge) {
		this.waveScale = scale;
		this.particleMaxAge = maxAge;
	}

	@Override
	public void renderParticle(BufferBuilder tess, Entity camera, float interp, float x, float y, float z, float tx, float tz) {

		this.theRenderEngine.bindTexture(texture);

		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.disableLighting();
		GlStateManager.enableBlend();
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0);
		GlStateManager.depthMask(false);
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		GlStateManager.disableCull();
		RenderHelper.disableStandardItemLighting();

		boolean fog = GL11.glIsEnabled(GL11.GL_FOG);
		if(fog) GlStateManager.disableFog();

		tess.begin(GL11.GL_QUADS, ParticleVertexFormats.LIT_TEXTURED);

		float pX = (float) (this.prevPosX + (this.posX - this.prevPosX) * (double) interp - interpPosX);
		float pY = (float) (this.prevPosY + (this.posY - this.prevPosY) * (double) interp - interpPosY);
		float pZ = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * (double) interp - interpPosZ);

		appendVertices(tess, interp, pX, pY, pZ);
		Tessellator.getInstance().draw();

		GlStateManager.doPolygonOffset(0.0F, 0.0F);
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
		GlStateManager.enableLighting();
		if(fog) GlStateManager.enableFog();
		GlStateManager.depthMask(true);
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		GlStateManager.disableBlend();
		GlStateManager.enableCull();
	}

	// BufferBuilder does not clamp RGBA like the old tessellator did.
	private void appendVertices(BufferBuilder tess, float interp, float pX, float pY, float pZ) {
		this.particleAlpha = 1 - (((float)this.particleAge + interp) / (float)this.particleMaxAge);
		float scale = (1 - (float)Math.pow(Math.E, (this.particleAge + interp) * -0.125)) * waveScale;

		tess.pos((double)(pX - 1 * scale), (double)(pY - 0.25), (double)(pZ - 1 * scale)).tex(1, 1).color(1F, 1F, 1F, MathHelper.clamp(this.particleAlpha, 0F, 1F)).lightmap(0, 240).normal(0, 1, 0).endVertex();
		tess.pos((double)(pX - 1 * scale), (double)(pY - 0.25), (double)(pZ + 1 * scale)).tex(1, 0).color(1F, 1F, 1F, MathHelper.clamp(this.particleAlpha, 0F, 1F)).lightmap(0, 240).normal(0, 1, 0).endVertex();
		tess.pos((double)(pX + 1 * scale), (double)(pY - 0.25), (double)(pZ + 1 * scale)).tex(0, 0).color(1F, 1F, 1F, MathHelper.clamp(this.particleAlpha, 0F, 1F)).lightmap(0, 240).normal(0, 1, 0).endVertex();
		tess.pos((double)(pX + 1 * scale), (double)(pY - 0.25), (double)(pZ - 1 * scale)).tex(0, 1).color(1F, 1F, 1F, MathHelper.clamp(this.particleAlpha, 0F, 1F)).lightmap(0, 240).normal(0, 1, 0).endVertex();
	}
}
