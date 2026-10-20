package com.hbm.particle;

import org.lwjgl.opengl.GL11;

import com.hbm.lib.RefStrings;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

@SideOnly(Side.CLIENT)
public class ParticleMukeCloud extends Particle {

	private static final ResourceLocation texture = new ResourceLocation(RefStrings.MODID + ":textures/particle/explosion.png");
	private TextureManager theRenderEngine;
	
	private float friction;

	public ParticleMukeCloud(TextureManager texman, World world, double x, double y, double z, double mx, double my, double mz) {
		super(world, x, y, z);
		this.theRenderEngine = texman;
		this.motionX = mx;
		this.motionY = my;
		this.motionZ = mz;
		
		if(motionY > 0) {
			this.friction = 0.9F;
			
			if(motionY > 0.1)
				this.particleMaxAge = 92 + rand.nextInt(11) + (int)(motionY * 20);
			else
				this.particleMaxAge = 72 + rand.nextInt(11);
			
		} else if (motionY == 0) {
			
			this.friction = 0.95F;
			this.particleMaxAge = 52 + rand.nextInt(11);
			
		} else {
			
			this.friction = 0.85F;
			this.particleMaxAge = 122 + rand.nextInt(31);
			this.particleAge = 80;
		}
	}

	public int getFXLayer() {
		return 3;
	}
	
	public void onUpdate() {
		
		this.canCollide = this.particleAge > 2;

		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;

		if(this.particleAge++ >= this.particleMaxAge - 2) {
			this.setExpired();
		}

		this.motionY -= 0.04D * (double) this.particleGravity;
		this.move(this.motionX, this.motionY, this.motionZ);
		this.motionX *= friction;
		this.motionY *= friction;
		this.motionZ *= friction;

		if(this.onGround) {
			this.motionX *= 0.7D;
			this.motionZ *= 0.7D;
		}
	}

	public void renderParticle(BufferBuilder tess, Entity camera, float interp, float x, float y, float z, float tx, float tz) {

		this.theRenderEngine.bindTexture(getTexture());
		
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.disableLighting();
		GlStateManager.enableBlend();
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0);
		GlStateManager.depthMask(false);
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		RenderHelper.disableStandardItemLighting();
		
		if(this.particleAge > this.particleMaxAge)
			this.particleAge = this.particleMaxAge;
		
		int texIndex = this.particleAge * 25 / this.particleMaxAge;
		float f0 = 1F / 5F;

		float uMin = texIndex % 5 * f0;
		float uMax = uMin + f0;
		float vMin = texIndex / 5 * f0;
		float vMax = vMin + f0;
			
		tess.begin(7, ParticleVertexFormats.LIT_TEXTURED);


		

		this.particleAlpha = 1F;
		this.particleScale = 3;
		


		float pX = (float) (this.prevPosX + (this.posX - this.prevPosX) * (double) interp - interpPosX);
		float pY = (float) (this.prevPosY + (this.posY - this.prevPosY) * (double) interp - interpPosY);
		float pZ = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * (double) interp - interpPosZ);

		tess.pos((double) (pX - x * this.particleScale - tx * this.particleScale), (double) (pY - 1 * this.particleScale), (double) (pZ - z * this.particleScale - tz * this.particleScale)).tex(uMax, vMax).color(1.0F, 1.0F, 1.0F, this.particleAlpha).lightmap((240) >> 16 & 65535, (240) & 65535).normal(0, 1, 0).endVertex();
		tess.pos((double) (pX - x * this.particleScale + tx * this.particleScale), (double) (pY + 1 * this.particleScale), (double) (pZ - z * this.particleScale + tz * this.particleScale)).tex(uMax, vMin).color(1.0F, 1.0F, 1.0F, this.particleAlpha).lightmap((240) >> 16 & 65535, (240) & 65535).normal(0, 1, 0).endVertex();
		tess.pos((double) (pX + x * this.particleScale + tx * this.particleScale), (double) (pY + 1 * this.particleScale), (double) (pZ + z * this.particleScale + tz * this.particleScale)).tex(uMin, vMin).color(1.0F, 1.0F, 1.0F, this.particleAlpha).lightmap((240) >> 16 & 65535, (240) & 65535).normal(0, 1, 0).endVertex();
		tess.pos((double) (pX + x * this.particleScale - tx * this.particleScale), (double) (pY - 1 * this.particleScale), (double) (pZ + z * this.particleScale - tz * this.particleScale)).tex(uMin, vMax).color(1.0F, 1.0F, 1.0F, this.particleAlpha).lightmap((240) >> 16 & 65535, (240) & 65535).normal(0, 1, 0).endVertex();
		
		Tessellator.getInstance().draw();
		
		GlStateManager.doPolygonOffset(0.0F, 0.0F);
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
		GlStateManager.enableLighting();
	}
	
	protected ResourceLocation getTexture() {
		return texture;
	}
}
