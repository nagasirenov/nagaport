package com.hbm.particle;

import java.awt.Color;


import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

@SideOnly(Side.CLIENT)
public class ParticleExplosionSmall extends EntityFXRotating {
	
	public float hue;

	public ParticleExplosionSmall(World world, double x, double y, double z, float scale, float speedMult) {
		super(world, x, y, z);
		particleTexture = ParticleSprites.base;
		this.particleMaxAge = 25 + rand.nextInt(10);
		this.particleScale = scale * 0.9F + rand.nextFloat() * 0.2F;

		this.motionX = world.rand.nextGaussian() * speedMult;
		this.motionZ = world.rand.nextGaussian() * speedMult;
		
		this.particleGravity = rand.nextFloat() * -0.01F;

		this.hue = 20F + rand.nextFloat() * 20F;
		Color color = Color.getHSBColor(hue / 255F, 1F, 1F);
		this.particleRed = color.getRed() / 255F;
		this.particleGreen = color.getGreen() / 255F;
		this.particleBlue = color.getBlue() / 255F;
		
		this.canCollide = false;
	}

	@Override
	public void onUpdate() {
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;
		
		this.particleAge++;

		if(this.particleAge >= this.particleMaxAge) {
			this.setExpired();
		}
		
		this.motionY -= particleGravity;
		this.previousSpinDegrees = this.spinDegrees;
		
		float ageScaled = (float) this.particleAge / (float) this.particleMaxAge;
		this.spinDegrees += (1 - ageScaled) * 5 * ((this.visualSeed % 2) - 0.5);

		this.motionX *= 0.65D;
		this.motionZ *= 0.65D;
		
		this.move(this.motionX, this.motionY, this.motionZ);
	}

	@Override
	public void renderParticle(BufferBuilder tess, Entity camera, float interp, float sX, float sY, float sZ, float dX, float dZ) {
		
		double ageScaled = (double) (this.particleAge + interp) / (double) this.particleMaxAge;
		
		Color color = Color.getHSBColor(hue / 255F, Math.max(1F - (float) ageScaled * 2F, 0), MathHelper.clamp(1.25F - (float) ageScaled * 2F, hue * 0.01F - 0.1F, 1F));
		this.particleRed = color.getRed() / 255F;
		this.particleGreen = color.getGreen() / 255F;
		this.particleBlue = color.getBlue() / 255F;

		this.particleAlpha = (float) Math.pow(1 - Math.min(ageScaled, 1), 0.25);

		double scale = (0.25 + 1 - Math.pow(1 - ageScaled, 4) + (this.particleAge + interp) * 0.02) * this.particleScale;
		renderParticleRotated(tess, interp, sX, sY, sZ, dX, dZ, scale, this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha * 0.5F, getBrightnessForRender(interp));
	}
}
