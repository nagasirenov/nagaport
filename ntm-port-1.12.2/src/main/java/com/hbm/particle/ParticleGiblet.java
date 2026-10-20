package com.hbm.particle;

import org.lwjgl.opengl.GL11;

import com.hbm.lib.RefStrings;


import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

@SideOnly(Side.CLIENT)
public class ParticleGiblet extends Particle {
	// Local mesh orientation, independent of native particle billboard angle.
	public float rotationYaw, rotationPitch, prevRotationYaw, prevRotationPitch;

	private static final ResourceLocation textureMeat = new ResourceLocation(RefStrings.MODID + ":textures/particle/meat.png");
	private static final ResourceLocation textureSlime = new ResourceLocation(RefStrings.MODID + ":textures/particle/slime.png");
	private static final ResourceLocation textureMetal = new ResourceLocation(RefStrings.MODID + ":textures/particle/metal.png");
	
	private TextureManager theRenderEngine;
	
	private float momentumYaw;
	private float momentumPitch;
	private int gibType;

	public ParticleGiblet(TextureManager texman, World world, double x, double y, double z, double mX, double mY, double mZ, int gibType) {
		super(world, x, y, z);
		this.motionX = mX;
		this.motionY = mY;
		this.motionZ = mZ;
		this.theRenderEngine = texman;
		this.particleMaxAge = 140 + rand.nextInt(20);
		this.particleGravity = 2F;
		this.gibType = gibType;
		
		if(gibType == 2) this.particleGravity *= 2;

		this.momentumYaw = (float) rand.nextGaussian() * 15F;
		this.momentumPitch = (float) rand.nextGaussian() * 15F;
	}
	
	@Override
	public int getFXLayer() {
		return 3;
	}
	
	@Override
	public void onUpdate() {
		super.onUpdate();

		this.prevRotationPitch = this.rotationPitch;
		this.prevRotationYaw = this.rotationYaw;
		
		if(!this.onGround) {
			this.rotationPitch += this.momentumPitch;
			this.rotationYaw += this.momentumYaw;
			
			if(gibType == 2) return;
			
			Particle fx = new net.minecraft.client.particle.ParticleBlockDust.Factory().createParticle(0, world, posX, posY, posZ, 0, 0, 0, net.minecraft.block.Block.getStateId((gibType == 1 ? Blocks.MELON_BLOCK : Blocks.REDSTONE_BLOCK).getDefaultState()));
			fx.setMaxAge(20 + rand.nextInt(20));
			Minecraft.getMinecraft().effectRenderer.addEffect(fx);
		}
	}
	
	@Override
	public void renderParticle(BufferBuilder tess, Entity camera, float interp, float x, float y, float z, float tx, float tz) {

		GL11.glPushMatrix();
		GlStateManager.disableLighting();
		GlStateManager.disableBlend();
		
		this.theRenderEngine.bindTexture(gibType == 2 ? textureMetal : gibType == 1 ? textureSlime : textureMeat);
		
		/* The native layer-three callback supplies its actual view entity. */
		Entity player = camera != null ? camera : Minecraft.getMinecraft().player;
		if(player == null) return;
		double dX = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double)interp;
		double dY = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double)interp;
		double dZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double)interp;

		float f10 = this.particleScale * 0.1F;
		float f11 = (float) (this.prevPosX + (this.posX - this.prevPosX) * (double) interp - dX);
		float f12 = (float) (this.prevPosY + (this.posY - this.prevPosY) * (double) interp - dY);
		float f13 = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * (double) interp - dZ);

		double pX = prevPosX + (posX - prevPosX) * interp;
		double pY = prevPosY + (posY - prevPosY) * interp;
		double pZ = prevPosZ + (posZ - prevPosZ) * interp;
		int brightness = world.getCombinedLight(new BlockPos(pX, pY, pZ), 0);
		int lX = brightness % 65536;
		int lY = brightness / 65536;
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)lX / 1.0F, (float)lY / 1.0F);

		tess.begin(7, ParticleVertexFormats.LIT_TEXTURED);


		tess.pos((double) (f11 - x * f10 - tx * f10), (double) (f12 - y * f10), (double) (f13 - z * f10 - tz * f10)).tex((double) 0, (double) 0).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(brightness >> 16 & 65535, brightness & 65535).normal(0, 1, 0).endVertex();
		tess.pos((double) (f11 - x * f10 + tx * f10), (double) (f12 + y * f10), (double) (f13 - z * f10 + tz * f10)).tex((double) 0, (double) 1).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(brightness >> 16 & 65535, brightness & 65535).normal(0, 1, 0).endVertex();
		tess.pos((double) (f11 + x * f10 + tx * f10), (double) (f12 + y * f10), (double) (f13 + z * f10 + tz * f10)).tex((double) 1, (double) 1).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(brightness >> 16 & 65535, brightness & 65535).normal(0, 1, 0).endVertex();
		tess.pos((double)(f11 + x * f10 - tx * f10), (double)(f12 - y * f10), (double)(f13 + z * f10 - tz * f10)).tex((double)1, (double)0).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(brightness >> 16 & 65535, brightness & 65535).normal(0, 1, 0).endVertex();
		Tessellator.getInstance().draw();
		GL11.glPopMatrix();
	}
}
