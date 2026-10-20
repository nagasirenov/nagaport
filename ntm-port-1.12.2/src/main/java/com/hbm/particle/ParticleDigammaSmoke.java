package com.hbm.particle;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.world.World;

@SideOnly(Side.CLIENT)
public class ParticleDigammaSmoke extends Particle {

	private int age;
	public int maxAge;

	public ParticleDigammaSmoke(TextureManager p_i1213_1_, World p_i1218_1_, double p_i1218_2_, double p_i1218_4_, double p_i1218_6_) {
		super(p_i1218_1_, p_i1218_2_, p_i1218_4_, p_i1218_6_);
		particleTexture = ParticleSprites.base;
		maxAge = 100 + rand.nextInt(40);
		this.canCollide = false;
		
		this.particleScale = 5;
		
		this.particleRed = 0.5F + rand.nextFloat() * 0.2F;
		this.particleGreen = 0.0F;
		this.particleBlue = 0.0F;
	}
	
	public void setMotion(double x, double y, double z) {
		this.motionX = x;
		this.motionY = y;
		this.motionZ = z;
	}

	@Override
	public void onUpdate() {
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;

		particleAlpha = 1 - ((float) age / (float) maxAge);

		++this.age;

		if(this.age == this.maxAge) {
			this.setExpired();
		}

		this.motionX *= 0.99D;
		this.motionY *= 0.99D;
		this.motionZ *= 0.99D;

		this.move(this.motionX, this.motionY, this.motionZ);
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
	
	@Override
	public int getBrightnessForRender(float p_70070_1_) {
		return 240;
	}
}
