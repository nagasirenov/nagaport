package com.hbm.particle;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

@SideOnly(Side.CLIENT)
public class ParticleDust extends Particle {

	public ParticleDust(World world, double x, double y, double z, double mX, double mY, double mZ, float scale) {
		super(world, x, y, z, mX, mY, mZ);
		particleTexture = ParticleSprites.base;
		this.particleRed = 0.4f;
		this.particleGreen = 0.2f;
		this.particleBlue = 0.1f;
		this.particleScale = scale;
		this.motionX = mX;
		this.motionY = mY;
		this.motionZ = mZ;
		this.particleAge = 1;
		this.particleMaxAge = 50 + world.rand.nextInt(50);
		this.particleAlpha = 0.5F;
		this.canCollide = false;
	}

	@Override
	public boolean shouldDisableDepth() {
		return true;
	}

	@Override
	public int getFXLayer() {
		return 1;
	}

	@Override
	public void renderParticle(BufferBuilder buffer, Entity camera, float interp, float fX, float fY, float fZ, float sX, float sZ) {
		float x = (float) (this.prevPosX + (this.posX - this.prevPosX) * (double) interp - interpPosX);
		float y = (float) (this.prevPosY + (this.posY - this.prevPosY) * (double) interp - interpPosY);
		float z = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * (double) interp - interpPosZ);
		ParticleQuadRenderer.append(buffer, particleTexture, x, y, z, particleScale,
				fX, fY, fZ, sX, sZ, particleRed, particleGreen, particleBlue, particleAlpha,
				getBrightnessForRender(interp));
	}
	
}
