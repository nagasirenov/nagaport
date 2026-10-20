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
public class ParticlePlasmaBlast extends Particle {

	private static final ResourceLocation texture = new ResourceLocation(RefStrings.MODID + ":textures/particle/shockwave.png");
	private TextureManager theRenderEngine;
	private final float pitch;
	private final float yaw;

	public ParticlePlasmaBlast(TextureManager texman, World world, double x, double y, double z, float r, float g, float b, float pitch, float yaw) {
		super(world, x, y, z);
		this.canCollide = false;
		this.theRenderEngine = texman;
		this.particleMaxAge = 20;
		this.particleRed = r;
		this.particleGreen = g;
		this.particleBlue = b;
		this.pitch = pitch;
		this.yaw = yaw;
	}

	public void setMaxAge(int maxAge) {
		this.particleMaxAge = maxAge;
	}

	public void setScale(float scale) {
		this.particleScale = scale;
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

	@Override
	public void renderParticle(BufferBuilder tess, Entity camera, float interp, float x, float y, float z, float tx, float tz) {

		this.theRenderEngine.bindTexture(texture);

		GlStateManager.pushMatrix();
		GlStateManager.disableLighting();
		GlStateManager.enableBlend();
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0);
		GlStateManager.depthMask(false);
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		GlStateManager.disableCull();
		RenderHelper.disableStandardItemLighting();

		boolean fog = GL11.glIsEnabled(GL11.GL_FOG);
		if(fog) GlStateManager.disableFog();

		float pX = (float) (this.prevPosX + (this.posX - this.prevPosX) * (double)interp - interpPosX);
		float pY = (float) (this.prevPosY + (this.posY - this.prevPosY) * (double)interp - interpPosY);
		float pZ = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * (double)interp - interpPosZ);

		GlStateManager.translate(pX, pY, pZ);
		GlStateManager.rotate(this.yaw, 0, 1, 0);
		GlStateManager.rotate(this.pitch, 1, 0, 0);

		tess.begin(GL11.GL_QUADS, ParticleVertexFormats.LIT_TEXTURED);

		appendVertices(tess, interp);
		Tessellator.getInstance().draw();

		if(fog) GlStateManager.enableFog();
		GlStateManager.doPolygonOffset(0.0F, 0.0F);
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
		GlStateManager.enableLighting();
		GlStateManager.popMatrix();
		GlStateManager.depthMask(true);
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		GlStateManager.disableBlend();
		GlStateManager.enableCull();
	}

	// BufferBuilder does not clamp RGBA like the old tessellator did.
	private void appendVertices(BufferBuilder tess, float interp) {
		this.particleAlpha = 1 - (((float)this.particleAge + interp) / (float)this.particleMaxAge);
		float scale = (1 - (float)Math.pow(Math.E, (this.particleAge + interp) * -0.125)) * this.particleScale;

		tess.pos((double)(- 1 * scale), 0, (double)(- 1 * scale)).tex(1, 1).color(MathHelper.clamp(this.particleRed, 0F, 1F), MathHelper.clamp(this.particleGreen, 0F, 1F), MathHelper.clamp(this.particleBlue, 0F, 1F), MathHelper.clamp(this.particleAlpha, 0F, 1F)).lightmap(0, 240).normal(0, 1, 0).endVertex();
		tess.pos((double)(- 1 * scale), 0, (double)(+ 1 * scale)).tex(1, 0).color(MathHelper.clamp(this.particleRed, 0F, 1F), MathHelper.clamp(this.particleGreen, 0F, 1F), MathHelper.clamp(this.particleBlue, 0F, 1F), MathHelper.clamp(this.particleAlpha, 0F, 1F)).lightmap(0, 240).normal(0, 1, 0).endVertex();
		tess.pos((double)(+ 1 * scale), 0, (double)(+ 1 * scale)).tex(0, 0).color(MathHelper.clamp(this.particleRed, 0F, 1F), MathHelper.clamp(this.particleGreen, 0F, 1F), MathHelper.clamp(this.particleBlue, 0F, 1F), MathHelper.clamp(this.particleAlpha, 0F, 1F)).lightmap(0, 240).normal(0, 1, 0).endVertex();
		tess.pos((double)(+ 1 * scale), 0, (double)(- 1 * scale)).tex(0, 1).color(MathHelper.clamp(this.particleRed, 0F, 1F), MathHelper.clamp(this.particleGreen, 0F, 1F), MathHelper.clamp(this.particleBlue, 0F, 1F), MathHelper.clamp(this.particleAlpha, 0F, 1F)).lightmap(0, 240).normal(0, 1, 0).endVertex();
	}
}
