package com.hbm.particle;

import org.lwjgl.opengl.GL11;

import com.hbm.lib.RefStrings;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
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
public class ParticleRBMKFlame extends Particle {

	public static final ResourceLocation texture = new ResourceLocation(RefStrings.MODID + ":textures/particle/rbmk_fire.png");
	private TextureManager theRenderEngine;

	public ParticleRBMKFlame(TextureManager texman, World world, double x, double y, double z, int maxAge) {
		super(world, x, y, z);
		this.canCollide = false;
		this.theRenderEngine = texman;
		this.particleMaxAge = maxAge;
		this.particleScale = rand.nextFloat() + 1F;
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

		this.theRenderEngine.bindTexture(getTexture());
		boolean fog = GL11.glIsEnabled(GL11.GL_FOG);
		if(fog) GlStateManager.disableFog();

		GlStateManager.pushMatrix();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.disableLighting();
		GlStateManager.enableBlend();
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0);
		GlStateManager.depthMask(false);
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		RenderHelper.disableStandardItemLighting();

		tess.begin(GL11.GL_QUADS, ParticleVertexFormats.LIT_TEXTURED);

		float pX = (float) (this.prevPosX + (this.posX - this.prevPosX) * (double) interp - interpPosX);
		float pY = (float) (this.prevPosY + (this.posY - this.prevPosY) * (double) interp - interpPosY);
		float pZ = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * (double) interp - interpPosZ);

		GlStateManager.translate(pX + x, pY + y, pZ + z);
		GlStateManager.rotate(-Minecraft.getMinecraft().getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);

		appendVertices(tess);
		Tessellator.getInstance().draw();

		GlStateManager.doPolygonOffset(0.0F, 0.0F);
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
		GlStateManager.enableLighting();
		GlStateManager.popMatrix();
		if(fog) GlStateManager.enableFog();
		GlStateManager.depthMask(true);
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		GlStateManager.disableBlend();
		GlStateManager.enableCull();
	}

	protected ResourceLocation getTexture() {
		return texture;
	}

	// BufferBuilder does not clamp RGBA like the old tessellator did.
	private void appendVertices(BufferBuilder tess) {
		if(this.particleAge > this.particleMaxAge)
			this.particleAge = this.particleMaxAge;

		int texIndex = this.particleAge * 5 % 14;
		float f0 = 1F / 14F;

		float uMin = texIndex % 5 * f0;
		float uMax = uMin + f0;
		float vMin = 0;
		float vMax = 1;

		this.particleAlpha = 1F;

		if(this.particleAge < 20) {
			this.particleAlpha = this.particleAge / 20F;
		}

		if(this.particleAge > this.particleMaxAge - 20) {
			this.particleAlpha = (this.particleMaxAge - this.particleAge) / 20F;
		}

		this.particleAlpha *= 0.5F;

		tess.pos((double) (-this.particleScale - 1), (double) (-this.particleScale * 2), (double) (0)).tex(uMax, vMax).color(1F, 1F, 1F, MathHelper.clamp(this.particleAlpha, 0F, 1F)).lightmap(0, 240).normal(0, 1, 0).endVertex();
		tess.pos((double) (-this.particleScale - 1), (double) (this.particleScale * 2), (double) 0).tex(uMax, vMin).color(1F, 1F, 1F, MathHelper.clamp(this.particleAlpha, 0F, 1F)).lightmap(0, 240).normal(0, 1, 0).endVertex();
		tess.pos((double) (this.particleScale - 1), (double) (this.particleScale * 2), (double) (0)).tex(uMin, vMin).color(1F, 1F, 1F, MathHelper.clamp(this.particleAlpha, 0F, 1F)).lightmap(0, 240).normal(0, 1, 0).endVertex();
		tess.pos((double) (this.particleScale - 1), (double) (-this.particleScale * 2), (double) (0)).tex(uMin, vMax).color(1F, 1F, 1F, MathHelper.clamp(this.particleAlpha, 0F, 1F)).lightmap(0, 240).normal(0, 1, 0).endVertex();
	}
}
