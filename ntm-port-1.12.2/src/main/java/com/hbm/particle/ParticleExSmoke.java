package com.hbm.particle;

import java.util.Random;


import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.world.World;

@SideOnly(Side.CLIENT)
public class ParticleExSmoke extends Particle {

	@Override
	public boolean shouldDisableDepth() { return true; }


	private final int visualSeed = System.identityHashCode(this) & Integer.MAX_VALUE;
	
	private int age;
	public int maxAge;

	public ParticleExSmoke(TextureManager p_i1213_1_, World p_i1218_1_, double p_i1218_2_, double p_i1218_4_, double p_i1218_6_) {
		super(p_i1218_1_, p_i1218_2_, p_i1218_4_, p_i1218_6_);
		particleTexture = ParticleSprites.base;
		maxAge = 100 + rand.nextInt(40);
	}

	public void setMotion(double x, double y, double z) {
		this.motionX = x;
		this.motionY = y;
		this.motionZ = z;
	}

	public void onUpdate() {
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;
		
		particleAlpha = 1 - ((float) age / (float) maxAge);
		
		++this.age;

		if (this.age == this.maxAge) {
			this.setExpired();
		}

		this.motionX *= 0.7599999785423279D;
		this.motionY *= 0.7599999785423279D;
		this.motionZ *= 0.7599999785423279D;
		
        this.move(this.motionX, this.motionY, this.motionZ);
	}

	public int getFXLayer() {
		return 1;
	}

	public void renderParticle(BufferBuilder p_70539_1_, Entity camera, float p_70539_2_, float p_70539_3_, float p_70539_4_, float p_70539_5_, float p_70539_6_, float p_70539_7_) {
		int brightness = getBrightnessForRender(p_70539_2_);
		
		Random urandom = new Random(this.visualSeed);
		
		for(int i = 0; i < 6; i++) {
			
	        this.particleRed = this.particleGreen = this.particleBlue = urandom.nextFloat() * 0.25F + 0.25F;
			
			float scale = urandom.nextFloat() + 0.5F;
	        float pX = (float) ((this.prevPosX + (this.posX - this.prevPosX) * (double)p_70539_2_ - interpPosX) + (urandom.nextGaussian() - 1D) * 0.75F);
	        float pY = (float) ((this.prevPosY + (this.posY - this.prevPosY) * (double)p_70539_2_ - interpPosY) + (urandom.nextGaussian() - 1D) * 0.75F);
	        float pZ = (float) ((this.prevPosZ + (this.posZ - this.prevPosZ) * (double)p_70539_2_ - interpPosZ) + (urandom.nextGaussian() - 1D) * 0.75F);
	        
			ParticleAtlasVertex.append(p_70539_1_, (double)(pX - p_70539_3_ * scale - p_70539_6_ * scale), (double)(pY - p_70539_4_ * scale), (double)(pZ - p_70539_5_ * scale - p_70539_7_ * scale), particleTexture.getMaxU(), particleTexture.getMaxV(), this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha, brightness);
			ParticleAtlasVertex.append(p_70539_1_, (double)(pX - p_70539_3_ * scale + p_70539_6_ * scale), (double)(pY + p_70539_4_ * scale), (double)(pZ - p_70539_5_ * scale + p_70539_7_ * scale), particleTexture.getMaxU(), particleTexture.getMinV(), this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha, brightness);
			ParticleAtlasVertex.append(p_70539_1_, (double)(pX + p_70539_3_ * scale + p_70539_6_ * scale), (double)(pY + p_70539_4_ * scale), (double)(pZ + p_70539_5_ * scale + p_70539_7_ * scale), particleTexture.getMinU(), particleTexture.getMinV(), this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha, brightness);
			ParticleAtlasVertex.append(p_70539_1_, (double)(pX + p_70539_3_ * scale - p_70539_6_ * scale), (double)(pY - p_70539_4_ * scale), (double)(pZ + p_70539_5_ * scale - p_70539_7_ * scale), particleTexture.getMinU(), particleTexture.getMaxV(), this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha, brightness);
		}
	}
}
