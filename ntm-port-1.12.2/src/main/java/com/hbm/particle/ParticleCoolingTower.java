package com.hbm.particle;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.world.World;

@SideOnly(Side.CLIENT)
public class ParticleCoolingTower extends Particle {

	private float baseScale = 1.0F;
	private float maxScale = 1.0F;
	private float lift = 0.3F;
	private float strafe = 0.075F;
	private boolean windDir = true;
	private float alphaMod = 0.25F;

	public ParticleCoolingTower(TextureManager texman, World world, double x, double y, double z) {
		super(world, x, y, z);
		particleTexture = ParticleSprites.base;
		this.particleRed = this.particleGreen = this.particleBlue = 0.9F + world.rand.nextFloat() * 0.05F;
		this.canCollide = false;
	}
	
	public void setBaseScale(float f) { this.baseScale = f; }
	public void setMaxScale(float f) { this.maxScale = f; }
	public void setLift(float f) { this.lift = f; }
	public void setLife(int i) { this.particleMaxAge = i; }
	public void setStrafe(float f) { this.strafe = f; }
	public void noWind() { this.windDir = false; }
	public void alphaMod(float mod) { this.alphaMod = mod; }

	public void setMotion(double x, double y, double z) {
		this.motionX = x;
		this.motionY = y;
		this.motionZ = z;
	}

	public void onUpdate() {
		
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;

		float ageScale = (float) this.particleAge / (float) this.particleMaxAge;
		
		this.particleAlpha = alphaMod - ageScale * alphaMod;
		this.particleScale = baseScale + (float)Math.pow((maxScale * ageScale - baseScale), 2);

		this.particleAge++;

		if(lift > 0 && this.motionY < this.lift) {
			this.motionY += 0.01F;
		}
		if(lift < 0 && this.motionY > this.lift) {
			this.motionY -= 0.01F;
		}

		this.motionX += rand.nextGaussian() * strafe * ageScale;
		this.motionZ += rand.nextGaussian() * strafe * ageScale;

		if(windDir) {
			this.motionX += 0.02 * ageScale;
			this.motionZ -= 0.01 * ageScale;
		}

		if(this.particleAge == this.particleMaxAge) {
			this.setExpired();
		}

		this.move(this.motionX, this.motionY, this.motionZ);

		motionX *= 0.925;
		motionY *= 0.925;
		motionZ *= 0.925;
	}

	@Override
	public boolean shouldDisableDepth() {
		return true;
	}

	public int getFXLayer() {
		return 1;
	}

	public void renderParticle(BufferBuilder buffer, Entity camera, float interp, float fX, float fY, float fZ, float sX, float sZ) {
		float x = (float) (this.prevPosX + (this.posX - this.prevPosX) * (double) interp - interpPosX);
		float y = (float) (this.prevPosY + (this.posY - this.prevPosY) * (double) interp - interpPosY);
		float z = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * (double) interp - interpPosZ);
		ParticleQuadRenderer.append(buffer, particleTexture, x, y, z, particleScale,
				fX, fY, fZ, sX, sZ, particleRed, particleGreen, particleBlue, particleAlpha,
				getBrightnessForRender(interp));
	}
}
