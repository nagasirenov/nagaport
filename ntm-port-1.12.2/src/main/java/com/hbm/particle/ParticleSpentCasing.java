package com.hbm.particle;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.hbm.main.ModSoundEvents;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import com.hbm.main.ResourceManager;
import com.hbm.util.BobMathUtil;
import com.hbm.util.Vec3NT;
import com.hbm.util.Tuple.Pair;

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
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

@SideOnly(Side.CLIENT)
public class ParticleSpentCasing extends Particle {
	/** Initial visual offset also resets interpolation under native particle spawn rules. */
	public void offsetSpawn(double x, double y, double z) {
		setPosition(posX + x, posY + y, posZ + z);
		prevPosX = posX; prevPosY = posY; prevPosZ = posZ;
	}

	// Local mesh orientation, independent of native particle billboard angle.
	public float rotationYaw, rotationPitch, prevRotationYaw, prevRotationPitch;
	
	public static final Random rand = new Random();
	private static float dScale = 0.05F, smokeJitter = 0.001F;

	private int maxSmokeGen = 120;
	private double smokeLift = 0.5D;
	private int nodeLife = 30;

	private final List<Pair<Vec3NT, Double>> smokeNodes = new ArrayList();

	private final TextureManager textureManager;

	private final SpentCasing config;
	private boolean isSmoking;

	private float momentumPitch, momentumYaw;

	public ParticleSpentCasing(TextureManager textureManager, World world, double x, double y, double z, double mx, double my, double mz, float momentumPitch, float momentumYaw, SpentCasing config, boolean smoking, int smokeLife, double smokeLift, int nodeLife) {
		super(world, x, y, z, 0, 0, 0);
		this.textureManager = textureManager;
		this.momentumPitch = momentumPitch;
		this.momentumYaw = momentumYaw;
		this.config = config;
		
		this.particleMaxAge = config.getMaxAge();
		this.setSize(2 * dScale * Math.max(config.getScaleX(), config.getScaleZ()), dScale * config.getScaleY());
		// Native particle bounding boxes start at the supplied position.
		
		this.isSmoking = smoking;
		this.maxSmokeGen = smokeLife;
		this.smokeLift = smokeLift;
		this.nodeLife = nodeLife;
		
		this.prevPosX = x;
		this.prevPosY = y;
		this.prevPosZ = z;

		this.motionX = mx;
		this.motionY = my;
		this.motionZ = mz;
		
		// i am at a loss for words as to what the fuck is going on here, but this is needed, stop asking, fuck you
		this.setPosition(x, y, z);

		particleGravity = 1F;
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

		if(this.particleAge++ >= this.particleMaxAge) {
			this.setExpired();
		}

		this.motionY -= 0.04D * (double) this.particleGravity;
		this.move(this.motionX, this.motionY, this.motionZ);
		this.motionX *= 0.98D;
		this.motionY *= 0.98D;
		this.motionZ *= 0.98D;
		
		if(this.onGround) {
			this.motionX *= 0.7D;
			this.motionZ *= 0.7D;
			
			this.rotationPitch = (float) (Math.floor(this.rotationPitch / 180F + 0.5F)) * 180F;
			this.momentumYaw *= 0.7F;
			this.onGround = false;
		}
		
		if(particleAge > maxSmokeGen && !smokeNodes.isEmpty())
			smokeNodes.clear();

		if(isSmoking && particleAge <= maxSmokeGen) {

			for(Pair<Vec3NT, Double> pair : smokeNodes) {
				Vec3NT node = pair.getKey();

				node.x += rand.nextGaussian() * smokeJitter;
				node.z += rand.nextGaussian() * smokeJitter;
				node.y += smokeLift * dScale;
				
				pair.value = Math.max(0, pair.value - (1D / (double) nodeLife));
			}

			if(particleAge < maxSmokeGen || world.isMaterialInBB(getBoundingBox(), net.minecraft.block.material.Material.WATER)) {
				smokeNodes.add(new Pair<Vec3NT, Double>(new Vec3NT(0, 0, 0), smokeNodes.isEmpty() ? 0.0D : 1D));
			}
		}
		
		prevRotationPitch = rotationPitch;
		prevRotationYaw = rotationYaw;
		
		rotationPitch += momentumPitch;
		rotationYaw += momentumYaw;
		
		if(Math.abs(prevRotationPitch - rotationPitch) > 180) {
			if(prevRotationPitch < rotationPitch) prevRotationPitch += 360;
			if(prevRotationPitch > rotationPitch) prevRotationPitch -= 360;
		}
		
		if(Math.abs(prevRotationYaw - rotationYaw) > 180) {
			if(prevRotationYaw < rotationYaw) prevRotationYaw += 360;
			if(prevRotationYaw > rotationYaw) prevRotationYaw -= 360;
		}
	}
	
	@Override
	public void move(double motionX, double motionY, double motionZ) {
		// Keep casing bounce response, using the genuine native immutable AABB API.
		double initMoX = motionX, initMoY = motionY, initMoZ = motionZ;
		AxisAlignedBB bounds = getBoundingBox();
		List<AxisAlignedBB> collisions = world.getCollisionBoxes(null, bounds.expand(motionX, motionY, motionZ));
		for(AxisAlignedBB collision : collisions) motionY = collision.calculateYOffset(bounds, motionY);
		bounds = bounds.offset(0, motionY, 0);
		for(AxisAlignedBB collision : collisions) motionX = collision.calculateXOffset(bounds, motionX);
		bounds = bounds.offset(motionX, 0, 0);
		for(AxisAlignedBB collision : collisions) motionZ = collision.calculateZOffset(bounds, motionZ);
		setBoundingBox(bounds.offset(0, 0, motionZ));
		resetPositionToBB();
		boolean collidedVertically = initMoY != motionY;
		this.onGround = collidedVertically && initMoY < 0;

		//Handles bounces
		if (initMoX != motionX) {
			this.motionX *= -0.25D;
			
			if(Math.abs(momentumYaw) > 1e-7)
				momentumYaw *= -0.75F;
			else
				momentumYaw = (float) rand.nextGaussian() * 10F * this.config.getBounceYaw();
		}
		
		if (initMoY != motionY) {
			this.motionY *= -0.5D;
			
			boolean rotFromSpeed = Math.abs(this.motionY) > 0.04;
			if(rotFromSpeed || Math.abs(momentumPitch) > 1e-7) {
				momentumPitch *= -0.75F;
				if(rotFromSpeed) {
					float mult = (float) BobMathUtil.safeClamp(initMoY / 0.2F, -1F, 1F);
					momentumPitch += rand.nextGaussian() * 10F * this.config.getBouncePitch() * mult;
					momentumYaw += (float) rand.nextGaussian() * 10F * this.config.getBounceYaw() * mult;
				}
			}
		}
		
		if (initMoZ != motionZ) {
			this.motionZ *= -0.25D;
			
			if(Math.abs(momentumYaw) > 1e-7)
				momentumYaw *= -0.75F;
			else
				momentumYaw = (float) rand.nextGaussian() * 10F * this.config.getBounceYaw();
		}
		
		if(this.config.getSound() != null && collidedVertically && Math.abs(initMoY) >= 0.2) {
			SoundEvent sound = resolveBounceSound(this.config.getSound());
			float pitch = 1F + this.rand.nextFloat() * 0.2F;
			if(sound != null) world.playSound(posX, posY, posZ, sound, net.minecraft.util.SoundCategory.PLAYERS, this.config.PLINK_LARGE.equals(this.config.getSound()) ? 1F : 0.5F, pitch, false);
		}
		
	}

	private static final java.util.Set<ResourceLocation> missingSounds = new java.util.HashSet<>();
	private static SoundEvent resolveBounceSound(String name) {
		if(SpentCasing.PLINK_SMALL.equals(name) || SpentCasing.PLINK_MEDIUM.equals(name)
				|| SpentCasing.PLINK_LARGE.equals(name) || SpentCasing.PLINK_SHELL.equals(name)) return ModSoundEvents.resolve(name);
		ResourceLocation location = new ResourceLocation(name);
		SoundEvent sound = SoundEvent.REGISTRY.getObject(location);
		if(sound == null && missingSounds.size() < 32 && missingSounds.add(location))
			org.apache.logging.log4j.LogManager.getLogger("hbm").warn("Unknown casing sound: {}", location);
		return sound;
	}

	/** Used for frame-perfect translation of smoke */
	private boolean setupDeltas = false;
	private double prevRenderX;
	private double prevRenderY;
	private double prevRenderZ;

	@Override
	public void renderParticle(BufferBuilder tessellator, Entity camera, float interp, float x, float y, float z, float tx, float tz) {

		GL11.glPushMatrix();
		RenderHelper.enableStandardItemLighting();
		GlStateManager.disableBlend();
		GlStateManager.enableCull();
		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		GlStateManager.enableRescaleNormal();
		GlStateManager.depthMask(true);

		double pX = prevPosX + (posX - prevPosX) * interp;
		double pY = prevPosY + (posY - prevPosY) * interp;
		double pZ = prevPosZ + (posZ - prevPosZ) * interp;
		
		if(!setupDeltas) {
			prevRenderX = pX;
			prevRenderY = pY;
			prevRenderZ = pZ;
			setupDeltas = true;
		}
		
		int brightness = world.getCombinedLight(new BlockPos(pX, pY, pZ), 0);
		int lX = brightness % 65536;
		int lY = brightness / 65536;
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)lX / 1.0F, (float)lY / 1.0F);

		textureManager.bindTexture(ResourceManager.casings_tex);
		
		Entity player = camera != null ? camera : Minecraft.getMinecraft().player;
		if(player == null) return;
		double dX = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double)interp;
		double dY = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double)interp;
		double dZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double)interp;

		GL11.glTranslated(pX - dX, pY - dY - this.height / 4 + config.getScaleY() * 0.01, pZ - dZ);

		GL11.glScalef(dScale, dScale, dScale);

		GL11.glRotatef(180 - (float) BobMathUtil.interp(prevRotationYaw, rotationYaw, interp), 0, 1, 0);
		GL11.glRotatef((float) -BobMathUtil.interp(prevRotationPitch, rotationPitch, interp), 1, 0, 0);

		GL11.glScalef(config.getScaleX(), config.getScaleY(), config.getScaleZ());

		int index = 0;
		for(String name : config.getType().partNames) {
			int col = this.config.getColors()[index]; //unsafe on purpose, set your colors properly or else...!
			Color color = new Color(col);
			GlStateManager.color(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F);
			ResourceManager.casings.renderPart(name);
			index++;
		}
		
		GlStateManager.color(1F, 1F, 1F);
		GlStateManager.disableRescaleNormal();
		GL11.glPopMatrix();
		
		GL11.glPushMatrix();
		GL11.glTranslated(pX - dX, pY - dY - this.height / 4, pZ - dZ);

		if(!smokeNodes.isEmpty()) {
			tessellator.begin(7, ParticleVertexFormats.LIT_TEXTURED);

			
			float scale = config.getScaleX() * 0.5F * dScale;
			Vec3d vec = new Vec3d(scale, 0, 0);
			float yaw = player.prevRotationYaw + (player.rotationYaw - player.prevRotationYaw) * interp;
			vec = vec.rotateYaw((float) Math.toRadians(-yaw));
			
			double deltaX = prevRenderX - pX;
			double deltaY = prevRenderY - pY;
			double deltaZ = prevRenderZ - pZ;
			
			for(Pair<Vec3NT, Double> pair : smokeNodes) {
				Vec3NT pos = pair.getKey();
				double mult = 1D;
				pos.x += deltaX * mult;
				pos.y += deltaY * mult;
				pos.z += deltaZ * mult;
			}

			for(int i = 0; i < smokeNodes.size() - 1; i++) {
				final Pair<Vec3NT, Double> node = smokeNodes.get(i), past = smokeNodes.get(i + 1);
				final Vec3NT nodeLoc = node.getKey(), pastLoc = past.getKey();
				float nodeAlpha = node.getValue().floatValue();
				float pastAlpha = past.getValue().floatValue();
				
				double timeAlpha = 1D - (double) particleAge / (double) maxSmokeGen;
				nodeAlpha *= timeAlpha;
				pastAlpha *= timeAlpha;



				tessellator.pos(nodeLoc.x, nodeLoc.y, nodeLoc.z).tex(0, 0).color(1F, 1F, 1F, nodeAlpha).lightmap((getBrightnessForRender(0F)) >> 16 & 65535, (getBrightnessForRender(0F)) & 65535).normal(0, 1, 0).endVertex();

				tessellator.pos(nodeLoc.x + vec.x, nodeLoc.y, nodeLoc.z + vec.z).tex(0, 0).color(1F, 1F, 1F, 0F).lightmap((getBrightnessForRender(0F)) >> 16 & 65535, (getBrightnessForRender(0F)) & 65535).normal(0, 1, 0).endVertex();

				tessellator.pos(pastLoc.x + vec.x, pastLoc.y, pastLoc.z + vec.z).tex(0, 0).color(1F, 1F, 1F, 0F).lightmap((getBrightnessForRender(0F)) >> 16 & 65535, (getBrightnessForRender(0F)) & 65535).normal(0, 1, 0).endVertex();

				tessellator.pos(pastLoc.x, pastLoc.y, pastLoc.z).tex(0, 0).color(1F, 1F, 1F, pastAlpha).lightmap((getBrightnessForRender(0F)) >> 16 & 65535, (getBrightnessForRender(0F)) & 65535).normal(0, 1, 0).endVertex();


				tessellator.pos(nodeLoc.x, nodeLoc.y, nodeLoc.z).tex(0, 0).color(1F, 1F, 1F, nodeAlpha).lightmap((getBrightnessForRender(0F)) >> 16 & 65535, (getBrightnessForRender(0F)) & 65535).normal(0, 1, 0).endVertex();

				tessellator.pos(nodeLoc.x - vec.x, nodeLoc.y, nodeLoc.z - vec.z).tex(0, 0).color(1F, 1F, 1F, 0F).lightmap((getBrightnessForRender(0F)) >> 16 & 65535, (getBrightnessForRender(0F)) & 65535).normal(0, 1, 0).endVertex();

				tessellator.pos(pastLoc.x - vec.x, pastLoc.y, pastLoc.z - vec.z).tex(0, 0).color(1F, 1F, 1F, 0F).lightmap((getBrightnessForRender(0F)) >> 16 & 65535, (getBrightnessForRender(0F)) & 65535).normal(0, 1, 0).endVertex();

				tessellator.pos(pastLoc.x, pastLoc.y, pastLoc.z).tex(0, 0).color(1F, 1F, 1F, pastAlpha).lightmap((getBrightnessForRender(0F)) >> 16 & 65535, (getBrightnessForRender(0F)) & 65535).normal(0, 1, 0).endVertex();
			}

			GlStateManager.alphaFunc(GL11.GL_GREATER, 0F);
			GlStateManager.enableBlend();
			GlStateManager.disableTexture2D();
			GlStateManager.disableCull();
			Tessellator.getInstance().draw();
			GlStateManager.enableCull();
			GlStateManager.enableTexture2D();
			GlStateManager.disableBlend();
			GlStateManager.alphaFunc(GL11.GL_GEQUAL, 0.1F);
		}

		GlStateManager.shadeModel(GL11.GL_FLAT);
		GL11.glPopMatrix();
		
		RenderHelper.disableStandardItemLighting();
		
		prevRenderX = pX;
		prevRenderY = pY;
		prevRenderZ = pZ;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getBrightnessForRender(float p_70070_1_) {
		int x = MathHelper.floor(this.posX);
		int z = MathHelper.floor(this.posZ);

		if(this.world.isBlockLoaded(new BlockPos(x, 0, z))) {
			double d0 = (getBoundingBox().maxY - getBoundingBox().minY) * 0.66D;
			int y = MathHelper.floor(this.posY + d0);
			return this.world.getCombinedLight(new BlockPos(x, y, z), 0);
		} else {
			return 0;
		}
	}
}
