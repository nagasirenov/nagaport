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
public class ParticleHadron extends Particle {

	private static final ResourceLocation texture = new ResourceLocation(RefStrings.MODID + ":textures/particle/hadron.png");
	private TextureManager theRenderEngine;

	public ParticleHadron(TextureManager texman, World world, double x, double y, double z) {
		super(world, x, y, z);
		this.canCollide = false;
		this.theRenderEngine = texman;
		this.particleMaxAge = 10;
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

		Entity player = camera != null ? camera : Minecraft.getMinecraft().player;
		if(player == null) return;

		this.theRenderEngine.bindTexture(texture);

		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.disableLighting();
		GlStateManager.enableBlend();
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0);
		GlStateManager.depthMask(false);
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		RenderHelper.disableStandardItemLighting();

		tess.begin(GL11.GL_QUADS, ParticleVertexFormats.LIT_TEXTURED);

		double dX = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double)interp;
		double dY = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double)interp;
		double dZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double)interp;

		double pX = this.prevPosX + (this.posX - this.prevPosX) * (double) interp - dX;
		double pY = this.prevPosY + (this.posY - this.prevPosY) * (double) interp - dY;
		double pZ = this.prevPosZ + (this.posZ - this.prevPosZ) * (double) interp - dZ;

		appendVertices(tess, interp, pX, pY, pZ, x, y, z, tx, tz);
		Tessellator.getInstance().draw();

		GlStateManager.doPolygonOffset(0.0F, 0.0F);
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
		GlStateManager.enableLighting();
		GlStateManager.depthMask(true);
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		GlStateManager.disableBlend();
		GlStateManager.enableCull();
	}

	// BufferBuilder does not clamp RGBA like the old tessellator did.
	private void appendVertices(BufferBuilder tess, float interp, double pX, double pY, double pZ, float x, float y, float z, float tx, float tz) {
		this.particleAlpha = 1 - (((float)this.particleAge + interp) / (float)this.particleMaxAge);
		float scale = (this.particleAge + interp) * 0.15F;

		tess.pos((double)(pX - x * scale - tx * scale), (double)(pY - y * scale), (double)(pZ - z * scale - tz * scale)).tex(1, 1).color(1F, 1F, 1F, MathHelper.clamp(this.particleAlpha, 0F, 1F)).lightmap(0, 240).normal(0, 1, 0).endVertex();
		tess.pos((double)(pX - x * scale + tx * scale), (double)(pY + y * scale), (double)(pZ - z * scale + tz * scale)).tex(1, 0).color(1F, 1F, 1F, MathHelper.clamp(this.particleAlpha, 0F, 1F)).lightmap(0, 240).normal(0, 1, 0).endVertex();
		tess.pos((double)(pX + x * scale + tx * scale), (double)(pY + y * scale), (double)(pZ + z * scale + tz * scale)).tex(0, 0).color(1F, 1F, 1F, MathHelper.clamp(this.particleAlpha, 0F, 1F)).lightmap(0, 240).normal(0, 1, 0).endVertex();
		tess.pos((double)(pX + x * scale - tx * scale), (double)(pY - y * scale), (double)(pZ + z * scale - tz * scale)).tex(0, 1).color(1F, 1F, 1F, MathHelper.clamp(this.particleAlpha, 0F, 1F)).lightmap(0, 240).normal(0, 1, 0).endVertex();
	}
}
