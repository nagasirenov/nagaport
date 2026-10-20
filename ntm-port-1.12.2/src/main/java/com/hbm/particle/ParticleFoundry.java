package com.hbm.particle;

import java.awt.Color;

import org.lwjgl.opengl.GL11;
import net.minecraft.client.renderer.GlStateManager;

import com.hbm.lib.RefStrings;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

@SideOnly(Side.CLIENT)
public class ParticleFoundry extends Particle {

	protected int color;
	protected ForgeDirection dir;
	/* how far the metal splooshes down from the base point */
	protected double length;
	/* the material coming right out of the faucet, either above or next to the base point */
	protected double base;
	/* how far the base part goes back */
	protected double offset;
	
	public static final ResourceLocation lava = new ResourceLocation(RefStrings.MODID + ":textures/blocks/lava_gray.png");
	private TextureManager theRenderEngine;

	public ParticleFoundry(TextureManager texan, World world, double x, double y, double z, int color, int direction, double length, double base, double offset) {
		super(world, x, y, z);
		this.color = color;
		this.dir = ForgeDirection.getOrientation(direction);
		this.theRenderEngine = texan; //it's a little texan man we enslaved to do the texture binding for us
		this.length = length;
		this.base = base;
		this.offset = offset;
		
		this.particleMaxAge = 20;
	}
	
	@Override
	public void onUpdate() {
		
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;

		if(this.particleAge++ >= this.particleMaxAge) {
			this.setExpired();
		}
	}

	@Override
	public int getFXLayer() {
		return 3;
	}

	@Override
	public void renderParticle(BufferBuilder tess, Entity camera, float interp, float x, float y, float z, float oX, float oZ) {

		Entity player = camera != null ? camera : Minecraft.getMinecraft().player;
		if(player == null) return;
		double dX = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double)interp;
		double dY = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double)interp;
		double dZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double)interp;
		
		float pX = (float) ((this.prevPosX + (this.posX - this.prevPosX) * (double) interp - dX));
		float pY = (float) ((this.prevPosY + (this.posY - this.prevPosY) * (double) interp - dY));
		float pZ = (float) ((this.prevPosZ + (this.posZ - this.prevPosZ) * (double) interp - dZ));
		
		ForgeDirection rot = this.dir.getRotation(ForgeDirection.UP);
		double width = 0.0625 + ((this.particleAge + interp) / this.particleMaxAge) * 0.0625;
		double girth = 0.125 * (1 - ((this.particleAge + interp) / this.particleMaxAge));
		
		Color color = new Color(this.color).brighter();
		double brightener = 0.7D;
		int r = (int) (255D - (255D - color.getRed()) * brightener);
		int g = (int) (255D - (255D - color.getGreen()) * brightener);
		int b = (int) (255D - (255D - color.getBlue()) * brightener);

		GlStateManager.color(r / 255F, g / 255F, b / 255F);

		GlStateManager.pushMatrix();
		GlStateManager.disableCull();
		GlStateManager.disableBlend();
		GlStateManager.translate(pX, pY, pZ);
		this.theRenderEngine.bindTexture(lava);
		
		tess.begin(GL11.GL_QUADS, ParticleVertexFormats.LIT_TEXTURED);



		double dirXG = dir.offsetX * girth;
		double dirZG = dir.offsetZ * girth;
		double rotXW = rot.offsetX * width;
		double rotZW = rot.offsetZ * width;
		
		double uMin = 0.5 - width;
		double uMax = 0.5 + width;
		double vMin = 0;
		double vMax = length;
		
		double add = (int)(System.currentTimeMillis() / 100 % 16) / 16D;

		//lower back
		tess.pos(rotXW, girth, rotZW).tex(uMax, vMax + add + girth).color(r / 255F, g / 255F, b / 255F, 1F).lightmap(0, 240).normal(0, 1, 0).endVertex();
		tess.pos(-rotXW, girth, -rotZW).tex(uMin, vMax + add + girth).color(r / 255F, g / 255F, b / 255F, 1F).lightmap(0, 240).normal(0, 1, 0).endVertex();
		tess.pos(-rotXW, -length, -rotZW).tex(uMin, vMin + add).color(r / 255F, g / 255F, b / 255F, 1F).lightmap(0, 240).normal(0, 1, 0).endVertex();
		tess.pos(rotXW, -length, rotZW).tex(uMax, vMin + add).color(r / 255F, g / 255F, b / 255F, 1F).lightmap(0, 240).normal(0, 1, 0).endVertex();

		//lower front
		tess.pos(dirXG + rotXW, 0, dirZG + rotZW).tex(uMax, vMax + add).color(r / 255F, g / 255F, b / 255F, 1F).lightmap(0, 240).normal(0, 1, 0).endVertex();
		tess.pos(dirXG - rotXW, 0, dirZG - rotZW).tex(uMin, vMax + add).color(r / 255F, g / 255F, b / 255F, 1F).lightmap(0, 240).normal(0, 1, 0).endVertex();
		tess.pos(dirXG - rotXW, -length, dirZG - rotZW).tex(uMin, vMin + add).color(r / 255F, g / 255F, b / 255F, 1F).lightmap(0, 240).normal(0, 1, 0).endVertex();
		tess.pos(dirXG + rotXW, -length, dirZG + rotZW).tex(uMax, vMin + add).color(r / 255F, g / 255F, b / 255F, 1F).lightmap(0, 240).normal(0, 1, 0).endVertex();
		
		double wMin = 0;
		double wMax = girth;
		
		//lower left
		tess.pos(rotXW, girth, rotZW).tex(wMin, vMax + add + girth).color(r / 255F, g / 255F, b / 255F, 1F).lightmap(0, 240).normal(0, 1, 0).endVertex();
		tess.pos(dirXG + rotXW, 0, dirZG + rotZW).tex(wMax, vMax + add).color(r / 255F, g / 255F, b / 255F, 1F).lightmap(0, 240).normal(0, 1, 0).endVertex();
		tess.pos(dirXG + rotXW, -length, dirZG + rotZW).tex(wMax, vMin + add).color(r / 255F, g / 255F, b / 255F, 1F).lightmap(0, 240).normal(0, 1, 0).endVertex();
		tess.pos(rotXW, -length, rotZW).tex(wMin, vMin + add).color(r / 255F, g / 255F, b / 255F, 1F).lightmap(0, 240).normal(0, 1, 0).endVertex();
		
		//lower right
		tess.pos(-rotXW, girth, -rotZW).tex(wMin, vMax + add + girth).color(r / 255F, g / 255F, b / 255F, 1F).lightmap(0, 240).normal(0, 1, 0).endVertex();
		tess.pos(dirXG - rotXW, 0, dirZG - rotZW).tex(wMax, vMax + add).color(r / 255F, g / 255F, b / 255F, 1F).lightmap(0, 240).normal(0, 1, 0).endVertex();
		tess.pos(dirXG - rotXW, -length, dirZG - rotZW).tex(wMax, vMin + add).color(r / 255F, g / 255F, b / 255F, 1F).lightmap(0, 240).normal(0, 1, 0).endVertex();
		tess.pos(-rotXW, -length, -rotZW).tex(wMin, vMin + add).color(r / 255F, g / 255F, b / 255F, 1F).lightmap(0, 240).normal(0, 1, 0).endVertex();

		double dirOX = dir.offsetX * offset;
		double dirOZ = dir.offsetZ * offset;
		
		vMax = offset;

		//upper back
		tess.pos(rotXW, 0, rotZW).tex(uMax, vMax - add).color(r / 255F, g / 255F, b / 255F, 1F).lightmap(0, 240).normal(0, 1, 0).endVertex();
		tess.pos(-rotXW, 0, -rotZW).tex(uMin, vMax - add).color(r / 255F, g / 255F, b / 255F, 1F).lightmap(0, 240).normal(0, 1, 0).endVertex();
		tess.pos(-rotXW - dirOX, base, -rotZW - dirOZ).tex(uMin, vMin - add).color(r / 255F, g / 255F, b / 255F, 1F).lightmap(0, 240).normal(0, 1, 0).endVertex();
		tess.pos(rotXW - dirOX, base, rotZW - dirOZ).tex(uMax, vMin - add).color(r / 255F, g / 255F, b / 255F, 1F).lightmap(0, 240).normal(0, 1, 0).endVertex();
		
		//upper front
		tess.pos(rotXW, girth, rotZW).tex(uMax, vMax - add + 0.25).color(r / 255F, g / 255F, b / 255F, 1F).lightmap(0, 240).normal(0, 1, 0).endVertex();
		tess.pos(-rotXW, girth, -rotZW).tex(uMin, vMax - add + 0.25).color(r / 255F, g / 255F, b / 255F, 1F).lightmap(0, 240).normal(0, 1, 0).endVertex();
		tess.pos(-rotXW - dirOX, base + girth, -rotZW - dirOZ).tex(uMin, vMin - add + 0.25).color(r / 255F, g / 255F, b / 255F, 1F).lightmap(0, 240).normal(0, 1, 0).endVertex();
		tess.pos(rotXW - dirOX, base + girth, rotZW - dirOZ).tex(uMax, vMin - add + 0.25).color(r / 255F, g / 255F, b / 255F, 1F).lightmap(0, 240).normal(0, 1, 0).endVertex();
		
		//upper left
		tess.pos(rotXW, 0, rotZW).tex(wMax, vMax - add + 0.75).color(r / 255F, g / 255F, b / 255F, 1F).lightmap(0, 240).normal(0, 1, 0).endVertex();
		tess.pos(rotXW, girth, rotZW).tex(wMin, vMax - add + 0.75).color(r / 255F, g / 255F, b / 255F, 1F).lightmap(0, 240).normal(0, 1, 0).endVertex();
		tess.pos(rotXW - dirOX, base + girth, rotZW - dirOZ).tex(wMin, vMin - add + 0.75).color(r / 255F, g / 255F, b / 255F, 1F).lightmap(0, 240).normal(0, 1, 0).endVertex();
		tess.pos(rotXW - dirOX, base, rotZW - dirOZ).tex(wMax, vMin - add + 0.75).color(r / 255F, g / 255F, b / 255F, 1F).lightmap(0, 240).normal(0, 1, 0).endVertex();
		
		//upper right
		tess.pos(-rotXW, 0, -rotZW).tex(wMax, vMax - add + 0.75).color(r / 255F, g / 255F, b / 255F, 1F).lightmap(0, 240).normal(0, 1, 0).endVertex();
		tess.pos(-rotXW, girth, -rotZW).tex(wMin, vMax - add + 0.75).color(r / 255F, g / 255F, b / 255F, 1F).lightmap(0, 240).normal(0, 1, 0).endVertex();
		tess.pos(-rotXW - dirOX, base + girth, -rotZW - dirOZ).tex(wMin, vMin - add + 0.75).color(r / 255F, g / 255F, b / 255F, 1F).lightmap(0, 240).normal(0, 1, 0).endVertex();
		tess.pos(-rotXW - dirOX, base, -rotZW - dirOZ).tex(wMax, vMin - add + 0.75).color(r / 255F, g / 255F, b / 255F, 1F).lightmap(0, 240).normal(0, 1, 0).endVertex();
		
		vMax = 0.125F;
		
		//bend
		tess.pos(dirXG + rotXW, 0, dirZG + rotZW).tex(uMax, vMin + add + 0.75).color(r / 255F, g / 255F, b / 255F, 1F).lightmap(0, 240).normal(0, 1, 0).endVertex();
		tess.pos(dirXG - rotXW, 0, dirZG - rotZW).tex(uMin, vMin + add + 0.75).color(r / 255F, g / 255F, b / 255F, 1F).lightmap(0, 240).normal(0, 1, 0).endVertex();
		tess.pos(-rotXW, girth, -rotZW).tex(uMin, vMax + add + 0.75).color(r / 255F, g / 255F, b / 255F, 1F).lightmap(0, 240).normal(0, 1, 0).endVertex();
		tess.pos(rotXW, girth, rotZW).tex(uMax, vMax + add + 0.75).color(r / 255F, g / 255F, b / 255F, 1F).lightmap(0, 240).normal(0, 1, 0).endVertex();

		Tessellator.getInstance().draw();
		
		GlStateManager.color(1F, 1F, 1F);
		GlStateManager.enableCull();
		GlStateManager.popMatrix();
	}
}
