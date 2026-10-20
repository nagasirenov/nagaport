package com.hbm.particle;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ParticleSpark extends Particle {

	List<double[]> steps = new ArrayList();
	int thresh;

	public ParticleSpark(World world, double x, double y, double z, double mX, double mY, double mZ) {
		super(world, x, y, z, mX, mY, mZ);
		thresh = 4 + rand.nextInt(3);
		steps.add(new double[] { motionX, motionY, motionZ });
		this.particleMaxAge = 20 + rand.nextInt(10);
		this.particleGravity = 0.5F;
	}

	public int getFXLayer() {
		return 3;
	}

	public void onUpdate() {

		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;

		if(this.particleAge++ >= this.particleMaxAge) {
			this.setExpired();
		}

		steps.add(new double[] { motionX, motionY, motionZ });

		while(steps.size() > thresh)
			steps.remove(0);

		this.motionY -= 0.04D * (double) this.particleGravity;
		double lastY = this.motionY;
		this.move(this.motionX, this.motionY, this.motionZ);

		if(this.onGround) {
			this.onGround = false;
			motionY = -lastY * 0.8D;
		}
	}

	public void renderParticle(BufferBuilder tess, Entity camera, float interp, float x, float y, float z, float tx, float tz) {

		if(steps.size() < 2)
			return;

		GL11.glPushMatrix();

		GlStateManager.disableTexture2D();
		GlStateManager.disableLighting();
		GL11.glLineWidth(3F);

		float pX = (float) (this.prevPosX + (this.posX - this.prevPosX) * (double) interp - interpPosX);
		float pY = (float) (this.prevPosY + (this.posY - this.prevPosY) * (double) interp - interpPosY);
		float pZ = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * (double) interp - interpPosZ);
		
		tess.begin(3, ParticleVertexFormats.LIT_TEXTURED);



		double[] prev = new double[] { pX, pY, pZ };

		for(int i = 1; i < steps.size(); i++) {

			double[] curr = new double[] { prev[0] + steps.get(i)[0], prev[1] + steps.get(i)[1], prev[2] + steps.get(i)[2] };

			tess.pos(prev[0], prev[1], prev[2]).tex(0, 0).color(1F, 1F, 1F, 1F).lightmap((240) >> 16 & 65535, (240) & 65535).normal(0, 1, 0).endVertex();

			prev = curr;
		}
		Tessellator.getInstance().draw();

		GlStateManager.enableLighting();
		GlStateManager.enableTexture2D();
		GL11.glPopMatrix();
	}
}
