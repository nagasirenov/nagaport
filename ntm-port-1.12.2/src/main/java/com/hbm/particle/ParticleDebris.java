package com.hbm.particle;

import java.util.Random;

import org.lwjgl.opengl.GL11;

import com.hbm.wiaj.WorldInAJar;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class ParticleDebris extends Particle {
	// Local mesh orientation, independent of native particle billboard angle.
	public float rotationYaw, rotationPitch, prevRotationYaw, prevRotationPitch;
	// Purely cosmetic variation; native particles do not allocate Entity IDs.
	protected final int visualSeed = System.identityHashCode(this) & Integer.MAX_VALUE;

	
	public WorldInAJar world;
	public static Random rng = new Random();

	public ParticleDebris(World world, double x, double y, double z) {
		super(world, x, y, z);
	}

	public ParticleDebris(World world, double x, double y, double z, double mx, double my, double mz) {
		super(world, x, y, z);
		double mult = 3;
		this.motionX = mx * mult;
		this.motionY = my * mult;
		this.motionZ = mz * mult;
		this.particleMaxAge = 100;
		this.particleGravity = 0.15F;
		this.canCollide = false;
	}

	@Override
	public int getFXLayer() {
		return 3;
	}

	@Override
	public void onUpdate() {
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;
		
		if(this.particleAge > 5) this.canCollide = true;
		
		rng.setSeed(this.visualSeed);
		this.prevRotationPitch = this.rotationPitch;
		this.prevRotationYaw = this.rotationYaw;
		this.rotationPitch += rng.nextFloat() * 10;
		this.rotationYaw += rng.nextFloat() * 10;

		if(this.visualSeed % 3 == 0) {
			TextureManager man = Minecraft.getMinecraft().renderEngine;
			ParticleRocketFlame fx = new ParticleRocketFlame(man, super.world, posX, posY, posZ).setScale(1F * Math.max(world.sizeY, 6) / 16F);
			fx.setMaxAge(50);
			Minecraft.getMinecraft().effectRenderer.addEffect(fx);
		}
		
		this.motionY -= this.particleGravity;
		
		this.move(this.motionX, this.motionY, this.motionZ);
		
		this.particleAge++;
		if(this.onGround || super.world.isMaterialInBB(getBoundingBox(), net.minecraft.block.material.Material.WEB)) this.setExpired();
	}

	@Override
	public void renderParticle(BufferBuilder tess, Entity camera, float interp, float fX, float fY, float fZ, float sX, float sZ) {
		
		if(world == null) return;

		Entity player = camera != null ? camera : Minecraft.getMinecraft().player;
		if(player == null) return;
		double dX = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double)interp;
		double dY = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double)interp;
		double dZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double)interp;
		
		float pX = (float) ((this.prevPosX + (this.posX - this.prevPosX) * (double) interp - dX));
		float pY = (float) ((this.prevPosY + (this.posY - this.prevPosY) * (double) interp - dY));
		float pZ = (float) ((this.prevPosZ + (this.posZ - this.prevPosZ) * (double) interp - dZ));
		
		BlockRendererDispatcher renderer = Minecraft.getMinecraft().getBlockRendererDispatcher();
		world.lightlevel = super.world.getCombinedLight(new BlockPos(posX, posY, posZ), 0);

		RenderHelper.disableStandardItemLighting();
		GlStateManager.disableBlend();
		//GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		
		GL11.glPushMatrix();
		GL11.glTranslatef(pX, pY, pZ);
		GL11.glRotated(prevRotationPitch + (rotationPitch - prevRotationPitch) * interp, 0, 1, 0);
		GL11.glRotated(prevRotationYaw + (rotationYaw - prevRotationYaw) * interp, 0, 0, 1);
		GL11.glTranslated(-world.sizeX / 2D, -world.sizeY / 2D, -world.sizeZ / 2D);
		
		Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		tess.begin(7, DefaultVertexFormats.BLOCK);
		
		for(int ix = 0; ix < world.sizeX; ix++) {
			for(int iy = 0; iy < world.sizeY; iy++) {
				for(int iz = 0; iz < world.sizeZ; iz++) {
					try { BlockPos blockPos = new BlockPos(ix, iy, iz); renderer.renderBlock(world.getBlockState(blockPos), blockPos, world, tess); } catch(Exception ex) { }
				}
			}
		}
		
		Tessellator.getInstance().draw();
		GlStateManager.shadeModel(GL11.GL_FLAT);
		GL11.glPopMatrix();
	}
}
