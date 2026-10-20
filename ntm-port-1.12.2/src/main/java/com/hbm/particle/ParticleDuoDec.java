package com.hbm.particle;


import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.world.World;

@SideOnly(Side.CLIENT)
public class ParticleDuoDec extends Particle {

	@Override
	public boolean shouldDisableDepth() { return true; }


	public ParticleDuoDec(TextureManager texman, World world, double x, double y, double z) {
		super(world, x, y, z);
		particleTexture = ParticleSprites.swen;
		this.particleRed =  this.particleGreen = this.particleBlue = 1F - world.rand.nextFloat() * 0.2F;
		this.particleScale = 0.3F;
		this.particleMaxAge = 50 + world.rand.nextInt(50);
		this.particleGravity = 0.2F;
	}

	public int getFXLayer() {
		return 1;
	}

	public void onUpdate() {
		super.onUpdate();

		if(!this.onGround) {
			this.motionX += rand.nextGaussian() * 0.002D;
			this.motionZ += rand.nextGaussian() * 0.002D;
			
			if(this.motionY < -0.025D)
				this.motionY = -0.025D;
		}
	}

	@Override
	public void renderParticle(BufferBuilder tess, Entity camera, float interp, float fX, float fY, float fZ, float sX, float sZ) {
		int brightness = getBrightnessForRender(interp);

	    float scale = this.particleScale;
	    float pX = (float) (this.prevPosX + (this.posX - this.prevPosX) * (double) interp - interpPosX);
	    float pY = (float) (this.prevPosY + (this.posY - this.prevPosY) * (double) interp - interpPosY);
	    float pZ = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * (double) interp - interpPosZ);

	    // Vertex 1
	    ParticleAtlasVertex.append(tess, (double) (pX - fX * scale - sX * scale), (double) (pY - fY * scale), (double) (pZ - fZ * scale - sZ * scale), particleTexture.getMaxU(), particleTexture.getMaxV(), this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha, brightness);

	    // Vertex 2
	    ParticleAtlasVertex.append(tess, (double) (pX - fX * scale + sX * scale), (double) (pY + fY * scale), (double) (pZ - fZ * scale + sZ * scale), particleTexture.getMaxU(), particleTexture.getMinV(), this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha, brightness);

	    // Vertex 3
	    ParticleAtlasVertex.append(tess, (double) (pX + fX * scale + sX * scale), (double) (pY + fY * scale), (double) (pZ + fZ * scale + sZ * scale), particleTexture.getMinU(), particleTexture.getMinV(), this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha, brightness);

	    // Vertex 4
	    ParticleAtlasVertex.append(tess, (double) (pX + fX * scale - sX * scale), (double) (pY - fY * scale), (double) (pZ + fZ * scale - sZ * scale), particleTexture.getMinU(), particleTexture.getMaxV(), this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha, brightness);
	}

}