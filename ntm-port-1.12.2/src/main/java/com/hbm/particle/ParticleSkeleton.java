package com.hbm.particle;

import org.lwjgl.opengl.GL11;

import com.hbm.lib.RefStrings;
import com.hbm.main.MainRegistry;
import com.hbm.particle.helper.SkeletonCreator.EnumSkeletonType;
import com.hbm.render.loader.HFRWavefrontObject;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import com.hbm.render.mesh.MeshModel;

@SideOnly(Side.CLIENT)
public class ParticleSkeleton extends Particle {
	public void setMotion(double x, double y, double z) { this.motionX = x; this.motionY = y; this.motionZ = z; }

	// Local mesh orientation, independent of native particle billboard angle.
	public float rotationYaw, rotationPitch, prevRotationYaw, prevRotationPitch;

	public static final ResourceLocation texture = new ResourceLocation(RefStrings.MODID + ":textures/particle/skeleton.png");
	public static final ResourceLocation texture_ext = new ResourceLocation(RefStrings.MODID + ":textures/particle/skoilet.png");
	public static final ResourceLocation texture_blood = new ResourceLocation(RefStrings.MODID + ":textures/particle/skeleton_blood.png");
	public static final ResourceLocation texture_blood_ext = new ResourceLocation(RefStrings.MODID + ":textures/particle/skoilet_blood.png");
	public static final MeshModel skeleton = new HFRWavefrontObject(new ResourceLocation(RefStrings.MODID, "models/effect/skeleton.obj"), false).asVBO();
	protected EnumSkeletonType type;

	public ResourceLocation useTexture;
	public ResourceLocation useTextureExt;
	
	private float momentumYaw;
	private float momentumPitch;
	private int initialDelay;
	
	private final TextureManager textureManager;

	public ParticleSkeleton(TextureManager textureManager, World world, double x, double y, double z, float r, float g, float b, EnumSkeletonType type) {
		super(world, x, y, z);
		this.textureManager = textureManager;
		this.type = type;

		this.particleMaxAge = 1200 + rand.nextInt(20);
		
		this.particleRed = r;
		this.particleGreen = g;
		this.particleBlue = b;
		this.particleGravity = 0.02F;
		this.initialDelay = 20;

		this.momentumPitch = rand.nextFloat() * 5 * (rand.nextBoolean() ? 1 : -1);
		this.momentumYaw = rand.nextFloat() * 5 * (rand.nextBoolean() ? 1 : -1);

		this.useTexture = texture;
		this.useTextureExt = texture_ext;
	}
	
	public ParticleSkeleton makeGib() {
		this.initialDelay = -2; // skip post delay motion randomization
		this.useTexture = texture_blood;
		this.useTextureExt = texture_blood_ext;
		this.particleGravity = 0.04F;
		this.particleMaxAge = 600 + rand.nextInt(20);
		return this;
	}
	
	@Override
	public void onUpdate() {

		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;
		this.prevRotationPitch = this.rotationPitch;
		this.prevRotationYaw = this.rotationYaw;
		
		if(initialDelay-- > 0) return;
		
		if(initialDelay == -1) {
			this.motionX = rand.nextGaussian() * 0.025;
			this.motionZ = rand.nextGaussian() * 0.025;
		}

		if(this.particleAge++ >= this.particleMaxAge) {
			this.setExpired();
		}
		boolean wasOnGround = this.onGround;
		
		this.motionY -= this.particleGravity;
		this.move(this.motionX, this.motionY, this.motionZ);
		this.motionX *= 0.98D;
		this.motionY *= 0.98D;
		this.motionZ *= 0.98D;
		
		if(!this.onGround) {
			this.rotationPitch += this.momentumPitch;
			this.rotationYaw += this.momentumYaw;
		} else {
			this.motionX = 0;
			this.motionY = 0;
			this.motionZ = 0;
			
			if(!wasOnGround) {
				world.playSound(posX, posY, posZ, net.minecraft.init.SoundEvents.ENTITY_SKELETON_HURT, net.minecraft.util.SoundCategory.HOSTILE, 0.25F, 0.8F + rand.nextFloat() * 0.4F, false);
			}
		}
	}
	
	@Override
	public int getFXLayer() {
		return 3;
	}
	
	@Override
	public void renderParticle(BufferBuilder tess, Entity camera, float interp, float x, float y, float z, float tx, float tz) {

		GL11.glPushMatrix();
		GlStateManager.enableLighting();
		GlStateManager.enableCull();
		GlStateManager.enableBlend();
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0);
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		RenderHelper.enableStandardItemLighting();

		double pX = prevPosX + (posX - prevPosX) * interp;
		double pY = prevPosY + (posY - prevPosY) * interp;
		double pZ = prevPosZ + (posZ - prevPosZ) * interp;
		Entity player = camera != null ? camera : Minecraft.getMinecraft().player;
		if(player == null) return;
		double dX = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double)interp;
		double dY = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double)interp;
		double dZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double)interp;

		GL11.glTranslated(pX - dX, pY - dY, pZ - dZ);

		GL11.glRotated(this.prevRotationYaw + (this.rotationYaw - this.prevRotationYaw) * interp, 0, 1, 0);
		GL11.glRotated(this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * interp, 1, 0, 0);
		
		float timeLeft = this.particleMaxAge - (this.particleAge + interp);
		if(timeLeft < 40) {
			this.particleAlpha = timeLeft / 40F;
		} else {
			this.particleAlpha = 1F;
		}
		
		int brightness = world.getCombinedLight(new BlockPos(pX, pY, pZ), 0);
		int lX = brightness % 65536;
		int lY = brightness / 65536;
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)lX / 1.0F, (float)lY / 1.0F);
		
		GlStateManager.color(particleRed, particleGreen, particleBlue, particleAlpha);
		GL11.glRotated(-90, 0, 1, 0); 
		
		switch(type) {
		case SKULL: 
			this.textureManager.bindTexture(useTexture);
			skeleton.renderPart("Skull"); break;
		case TORSO: 
			this.textureManager.bindTexture(useTexture);
			skeleton.renderPart("Torso"); break;
		case LIMB: 
			this.textureManager.bindTexture(useTexture);
			skeleton.renderPart("Limb"); break;
		case SKULL_VILLAGER: 
			this.textureManager.bindTexture(useTextureExt);
			skeleton.renderPart("SkullVillager"); break;
		}
		
		GlStateManager.color(1F, 1F, 1F, 1F);
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
		GlStateManager.disableCull();
		
		GL11.glPopMatrix();
	}
}
