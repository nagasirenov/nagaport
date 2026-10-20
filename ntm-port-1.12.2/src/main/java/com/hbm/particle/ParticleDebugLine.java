package com.hbm.particle;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

public class ParticleDebugLine extends Particle {
	static final VertexFormat LINE_FORMAT = DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP;
	
	int color;

	public ParticleDebugLine(World world, double x, double y, double z, double lx, double ly, double lz, int color) {
		super(world, x, y, z, lx, ly, lz);
		this.motionX = lx;
		this.motionY = ly;
		this.motionZ = lz;
		this.color = color;
		this.particleMaxAge = 60;
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
	public void renderParticle(BufferBuilder buffer, Entity camera, float interp, float x, float y, float z, float tx, float tz) {

		double pX = this.prevPosX + (this.posX - this.prevPosX) * (double) interp - interpPosX;
		double pY = this.prevPosY + (this.posY - this.prevPosY) * (double) interp - interpPosY;
		double pZ = this.prevPosZ + (this.posZ - this.prevPosZ) * (double) interp - interpPosZ;

		double mX = pX + motionX;
		double mY = pY + motionY;
		double mZ = pZ + motionZ;
		
		GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_CURRENT_BIT);
		GL11.glPushMatrix();
		GL11.glDisable(GL11.GL_COLOR_MATERIAL);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_POINT_SMOOTH);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA,GL11.GL_ONE_MINUS_SRC_ALPHA);
		
        buffer.begin(GL11.GL_LINES, LINE_FORMAT);
        appendLine(buffer, pX, pY, pZ, mX, mY, mZ, color,
                (int) (240 - (240 * (this.particleAge + interp) / this.particleMaxAge)));
        Tessellator.getInstance().draw();

		GL11.glEnable(GL11.GL_COLOR_MATERIAL);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_POINT_SMOOTH);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glPopMatrix();
		GL11.glPopAttrib();
	}

    static void appendLine(BufferBuilder buffer, double x, double y, double z,
            double endX, double endY, double endZ, int color, int brightness) {
        int red = color >> 16 & 255, green = color >> 8 & 255, blue = color & 255;
        int sky = brightness >> 16 & 65535, block = brightness & 65535;
        buffer.pos(x, y, z).tex(0, 0).color(red, green, blue, 255).lightmap(sky, block).endVertex();
        buffer.pos(endX, endY, endZ).tex(0, 0).color(red, green, blue, 255).lightmap(sky, block).endVertex();
    }
}
