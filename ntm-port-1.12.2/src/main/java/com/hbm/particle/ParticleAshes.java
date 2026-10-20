package com.hbm.particle;

import com.hbm.util.Vec3NT;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

@SideOnly(Side.CLIENT)
public class ParticleAshes extends EntityFXRotating {

	public ParticleAshes(World world, double x, double y, double z, float scale) {
		super(world, x, y, z);
		particleTexture = ParticleSprites.base;
		this.particleMaxAge = 1200 + rand.nextInt(20);
		this.particleScale = scale * 0.9F + rand.nextFloat() * 0.2F;
		
		this.particleGravity = 0.01F;

		this.particleRed = this.particleGreen = this.particleBlue = this.rand.nextFloat() * 0.1F + 0.1F;
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
		
		if(!this.onGround) this.spinDegrees += 2 * ((this.visualSeed % 2) - 0.5);

		this.motionX *= 0.95D;
		this.motionY *= 0.99D;
		this.motionZ *= 0.95D;
		
		boolean wasOnGround = this.onGround;
		this.move(this.motionX, this.motionY, this.motionZ);
		if(!wasOnGround && this.onGround) this.spinDegrees = rand.nextFloat() * 360F;
		
		if(this.visualSeed % 5 == 0 && this.onGround && rand.nextInt(15) == 0) {
			world.spawnParticle(net.minecraft.util.EnumParticleTypes.SMOKE_NORMAL, posX, posY + 0.125, posZ, 0, 0.05, 0);
		}
	}

	@Override
	public void renderParticle(BufferBuilder tess, Entity camera, float interp, float sX, float sY, float sZ, float dX, float dZ) {
		int brightness = getBrightnessForRender(interp);

		float timeLeft = this.particleMaxAge - (this.particleAge + interp);
		
		if(timeLeft < 40) {
			this.particleAlpha = timeLeft / 40F;
		} else {
			this.particleAlpha = 1F;
		}
		
		if(this.onGround) {
			float pX = (float) (this.prevPosX + (this.posX - this.prevPosX) * (double) interp - interpPosX);
			float pY = (float) (this.prevPosY + (this.posY - this.prevPosY) * (double) interp - interpPosY);
			float pZ = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * (double) interp - interpPosZ);
			
			Vec3NT vec = new Vec3NT(particleScale, 0, particleScale).rotateAroundYDeg(this.spinDegrees);

			ParticleAtlasVertex.append(tess, pX + vec.x, pY + 0.05, pZ + vec.z, particleTexture.getMaxU(), particleTexture.getMaxV(), this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha, brightness);
			vec.rotateAroundYDeg(90);
			ParticleAtlasVertex.append(tess, pX + vec.x, pY + 0.05, pZ + vec.z, particleTexture.getMaxU(), particleTexture.getMinV(), this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha, brightness);
			vec.rotateAroundYDeg(90);
			ParticleAtlasVertex.append(tess, pX + vec.x, pY + 0.05, pZ + vec.z, particleTexture.getMinU(), particleTexture.getMinV(), this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha, brightness);
			vec.rotateAroundYDeg(90);
			ParticleAtlasVertex.append(tess, pX + vec.x, pY + 0.05, pZ + vec.z, particleTexture.getMinU(), particleTexture.getMaxV(), this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha, brightness);
		} else {
			renderParticleRotated(tess, interp, sX, sY, sZ, dX, dZ, this.particleScale, this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha, brightness);
		}
	}
}
