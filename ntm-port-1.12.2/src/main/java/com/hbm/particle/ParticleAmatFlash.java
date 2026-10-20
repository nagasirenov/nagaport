package com.hbm.particle;

import java.util.Random;

import org.lwjgl.opengl.GL11;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.world.World;
import net.minecraft.util.math.MathHelper;

@SideOnly(Side.CLIENT)
public class ParticleAmatFlash extends Particle {

	public ParticleAmatFlash(World world, double x, double y, double z, float scale) {
		super(world, x, y, z);
		this.canCollide = false;
		this.particleMaxAge = 10;
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

		Entity player = camera != null ? camera : Minecraft.getMinecraft().player;
		if(player == null) return;
		double dX = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double)interp;
		double dY = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double)interp;
		double dZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double)interp;

		float pX = (float) ((this.prevPosX + (this.posX - this.prevPosX) * (double) interp - dX));
		float pY = (float) ((this.prevPosY + (this.posY - this.prevPosY) * (double) interp - dY));
		float pZ = (float) ((this.prevPosZ + (this.posZ - this.prevPosZ) * (double) interp - dZ));

		GlStateManager.pushMatrix();
		GlStateManager.translate(pX, pY, pZ);

		GlStateManager.scale(0.2F * particleScale, 0.2F * particleScale, 0.2F * particleScale);

		double intensity = (double) (this.particleAge + interp) / (double) this.particleMaxAge;
		double inverse = 1.0D - intensity;

		RenderHelper.disableStandardItemLighting();

		Random random = new Random(432L);
		GlStateManager.disableTexture2D();
		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		GlStateManager.disableAlpha();
		GlStateManager.enableCull();
		GlStateManager.depthMask(false);

		float scale = 0.5F;

		for(int i = 0; i < 100; i++) {

			GlStateManager.rotate(random.nextFloat() * 360.0F, 1.0F, 0.0F, 0.0F);
			GlStateManager.rotate(random.nextFloat() * 360.0F, 0.0F, 1.0F, 0.0F);
			GlStateManager.rotate(random.nextFloat() * 360.0F, 0.0F, 0.0F, 1.0F);
			GlStateManager.rotate(random.nextFloat() * 360.0F, 1.0F, 0.0F, 0.0F);
			GlStateManager.rotate(random.nextFloat() * 360.0F, 0.0F, 1.0F, 0.0F);

			float vert1 = (random.nextFloat() * 20.0F + 5.0F + 1 * 10.0F) * (float) (intensity * scale);
			float vert2 = (random.nextFloat() * 2.0F + 1.0F + 1 * 2.0F) * (float) (intensity * scale);

			tess.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
			appendFan(tess, vert1, vert2, (float) inverse);
			Tessellator.getInstance().draw();
		}

		GlStateManager.popMatrix();

		GlStateManager.depthMask(true);
		GlStateManager.enableCull();
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		GlStateManager.disableBlend();
		GlStateManager.shadeModel(GL11.GL_FLAT);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.enableTexture2D();
		GlStateManager.enableAlpha();
		RenderHelper.enableStandardItemLighting();

	}

	private static void appendFan(BufferBuilder tess, float length, float width, float alpha) {
		tess.pos(0, 0, 0).color(1F, 1F, 1F, MathHelper.clamp(alpha, 0F, 1F)).endVertex();
		tess.pos(-0.866D * width, length, -0.5F * width).color(1F, 1F, 1F, 0F).endVertex();
		tess.pos(0.866D * width, length, -0.5F * width).color(1F, 1F, 1F, 0F).endVertex();
		tess.pos(0, length, width).color(1F, 1F, 1F, 0F).endVertex();
		tess.pos(-0.866D * width, length, -0.5F * width).color(1F, 1F, 1F, 0F).endVertex();
	}
}
