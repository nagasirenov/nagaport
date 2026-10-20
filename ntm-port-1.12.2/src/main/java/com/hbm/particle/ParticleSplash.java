package com.hbm.particle;


import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.world.World;

@SideOnly(Side.CLIENT)
public class ParticleSplash extends Particle {

	@Override
	public boolean shouldDisableDepth() { return true; }


	private final int visualSeed = System.identityHashCode(this) & Integer.MAX_VALUE;

	public ParticleSplash(TextureManager texman, World world, double x, double y, double z) {
		super(world, x, y, z);
		particleTexture = ParticleSprites.splash;
		this.particleRed =  this.particleGreen = this.particleBlue = 1F - world.rand.nextFloat() * 0.2F;
		this.particleAlpha = 0.5F;
		this.particleScale = 0.4F;
		this.particleMaxAge = 200 + world.rand.nextInt(50);
		this.particleGravity = 0.4F;
	}

	public int getFXLayer() {
		return 1;
	}

	public void onUpdate() {
		super.onUpdate();

		if(!this.onGround) {
			this.motionX += rand.nextGaussian() * 0.002D;
			this.motionZ += rand.nextGaussian() * 0.002D;
			
			if(this.motionY < -0.5D)
				this.motionY = -0.5D;
		} else {
			this.setExpired();
		}
	}

	@Override
	public void renderParticle(BufferBuilder tess, Entity camera, float interp, float fX, float fY, float fZ, float sX, float sZ) {
		int brightness = getBrightnessForRender(interp);

		float scale = this.particleScale;
		float pX = (float) (this.prevPosX + (this.posX - this.prevPosX) * (double) interp - interpPosX);
		float pY = (float) (this.prevPosY + (this.posY - this.prevPosY) * (double) interp - interpPosY);
		float pZ = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * (double) interp - interpPosZ);

		boolean flipU = this.visualSeed % 2 == 0;
		boolean flipV = this.visualSeed % 4 < 2;

		double minU = flipU ? particleTexture.getMaxU() : particleTexture.getMinU();
		double maxU = flipU ? particleTexture.getMinU() : particleTexture.getMaxU();
		double minV = flipV ? particleTexture.getMaxV() : particleTexture.getMinV();
		double maxV = flipV ? particleTexture.getMinV() : particleTexture.getMaxV();

		ParticleAtlasVertex.append(tess, (double) (pX - fX * scale - sX * scale), (double) (pY - fY * scale), (double) (pZ - fZ * scale - sZ * scale), maxU, maxV, this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha, brightness);
		ParticleAtlasVertex.append(tess, (double) (pX - fX * scale + sX * scale), (double) (pY + fY * scale), (double) (pZ - fZ * scale + sZ * scale), maxU, minV, this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha, brightness);
		ParticleAtlasVertex.append(tess, (double) (pX + fX * scale + sX * scale), (double) (pY + fY * scale), (double) (pZ + fZ * scale + sZ * scale), minU, minV, this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha, brightness);
		ParticleAtlasVertex.append(tess, (double) (pX + fX * scale - sX * scale), (double) (pY - fY * scale), (double) (pZ + fZ * scale - sZ * scale), minU, maxV, this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha, brightness);
	}
}