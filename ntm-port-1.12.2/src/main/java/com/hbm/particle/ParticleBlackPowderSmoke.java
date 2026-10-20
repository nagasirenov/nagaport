package com.hbm.particle;

import java.awt.Color;


import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

@SideOnly(Side.CLIENT)
public class ParticleBlackPowderSmoke extends EntityFXRotating {
	
	public float hue;

	public ParticleBlackPowderSmoke(World world, double x, double y, double z, float scale) {
		super(world, x, y, z);
		particleTexture = ParticleSprites.base;
		this.particleMaxAge = 30 + rand.nextInt(15);
		this.particleScale = scale * 0.9F + rand.nextFloat() * 0.2F;
		
		this.particleGravity = 0F;

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
		this.spinDegrees += (1 - ageScaled) * 2 * ((this.visualSeed % 2) - 0.5);

		this.motionX *= 0.65D;
		this.motionY *= 0.65D;
		this.motionZ *= 0.65D;
		
		this.move(this.motionX, this.motionY, this.motionZ);
	}

	@Override
	public void renderParticle(BufferBuilder tess, Entity camera, float interp, float sX, float sY, float sZ, float dX, float dZ) {
		
		double ageScaled = (double) (this.particleAge + interp) / (double) this.particleMaxAge;
		
		Color color = Color.getHSBColor(hue / 255F, Math.max(1F - (float) ageScaled * 4F, 0), MathHelper.clamp(1.25F - (float) ageScaled * 2F, 0.7F, 1F));
		this.particleRed = color.getRed() / 255F;
		this.particleGreen = color.getGreen() / 255F;
		this.particleBlue = color.getBlue() / 255F;

		this.particleAlpha = (float) Math.pow(1 - Math.min(ageScaled, 1), 0.25);

		double scale = (0.25 + ageScaled + (this.particleAge + interp) * 0.025) * this.particleScale;
		renderParticleRotated(tess, interp, sX, sY, sZ, dX, dZ, scale, this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha * 0.25F, getBrightnessForRender(interp));
	}
}
