package com.hbm.particle;

import java.util.Random;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.world.World;

@SideOnly(Side.CLIENT)
public class ParticleRocketSCH extends ParticleRocketFlame {

	public ParticleRocketSCH(TextureManager p_i1213_1_, World p_i1218_1_, double p_i1218_2_, double p_i1218_4_, double p_i1218_6_) {
		super(p_i1213_1_, p_i1218_1_, p_i1218_2_, p_i1218_4_, p_i1218_6_);
	}

	@Override
	public void renderParticle(BufferBuilder p_70539_1_, Entity camera, float interp, float sX, float sY, float sZ, float dX, float dZ) {

		Random urandom = new Random(this.visualSeed);

		for(int i = 0; i < 10; i++) {

			float add = urandom.nextFloat() * 0.3F;
			float light = 1 - Math.min(((float) (age) / (float) (maxAge * 0.25F)), 1);

			this.particleRed = 1 * light + add;
			this.particleGreen = 1 + add;
			this.particleBlue = 1 + add;

			this.particleAlpha = (float) Math.pow(1 - Math.min(((float) (age) / (float) (maxAge)), 1), 0.5);





			float spread = (float) Math.pow(((float) (age) / (float) maxAge) * 4F, 1.5) + 1F;
			spread *= this.particleScale;

			float scale = (urandom.nextFloat() * 0.5F + 0.1F + ((float) (age) / (float) maxAge) * 2F) * particleScale;
			float pX = (float) ((this.prevPosX + (this.posX - this.prevPosX) * (double) interp - interpPosX) + (urandom.nextGaussian() - 1D) * 0.2F * spread);
			float pY = (float) ((this.prevPosY + (this.posY - this.prevPosY) * (double) interp - interpPosY) + (urandom.nextGaussian() - 1D) * 0.5F * spread);
			float pZ = (float) ((this.prevPosZ + (this.posZ - this.prevPosZ) * (double) interp - interpPosZ) + (urandom.nextGaussian() - 1D) * 0.2F * spread);

			ParticleAtlasVertex.append(p_70539_1_, (double) (pX - sX * scale - dX * scale), (double) (pY - sY * scale), (double) (pZ - sZ * scale - dZ * scale), particleTexture.getMaxU(), particleTexture.getMaxV(), this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha * 0.75F, 240);
			ParticleAtlasVertex.append(p_70539_1_, (double) (pX - sX * scale + dX * scale), (double) (pY + sY * scale), (double) (pZ - sZ * scale + dZ * scale), particleTexture.getMaxU(), particleTexture.getMinV(), this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha * 0.75F, 240);
			ParticleAtlasVertex.append(p_70539_1_, (double) (pX + sX * scale + dX * scale), (double) (pY + sY * scale), (double) (pZ + sZ * scale + dZ * scale), particleTexture.getMinU(), particleTexture.getMinV(), this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha * 0.75F, 240);
			ParticleAtlasVertex.append(p_70539_1_, (double) (pX + sX * scale - dX * scale), (double) (pY - sY * scale), (double) (pZ + sZ * scale - dZ * scale), particleTexture.getMinU(), particleTexture.getMaxV(), this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha * 0.75F, 240);
		}
	}
}
