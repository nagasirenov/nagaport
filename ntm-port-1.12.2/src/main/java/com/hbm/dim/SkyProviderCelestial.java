package com.hbm.dim;

import java.util.List;
import java.util.Map;
import java.util.Random;

import org.lwjgl.opengl.GL11;

import com.hbm.dim.SolarSystem.AstroMetric;
import com.hbm.dim.orbit.OrbitalStation;
import com.hbm.dim.thatmo.WorldProviderThatmo.MeteorType;
import com.hbm.dim.trait.CBT_Atmosphere;
import com.hbm.dim.trait.CBT_Dyson;
import com.hbm.dim.trait.CelestialBodyTrait.CBT_COMPROMISED;
import com.hbm.dim.trait.CBT_War;
import com.hbm.dim.trait.CBT_Destroyed;
import com.hbm.extprop.HbmLivingProps;
import com.hbm.lib.RefStrings;
import com.hbm.main.ResourceManager;
import com.hbm.render.shader.Shader;
import com.hbm.saveddata.SatelliteSavedData;
import com.hbm.saveddata.satellites.Satellite;
import com.hbm.util.BobMathUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.IRenderHandler;

import org.lwjgl.opengl.ContextCapabilities;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GLContext;

import com.hbm.dim.trait.CBT_Impact;
import com.hbm.dim.trait.CBT_Lights;
import com.hbm.main.ModEventHandler;
import com.hbm.main.ModEventHandlerClient;
import com.hbm.main.ModEventHandlerRenderer;

import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class SkyProviderCelestial extends IRenderHandler {

	private static final ResourceLocation planetTexture = new ResourceLocation(RefStrings.MODID, "textures/misc/space/planet.png");
	private static final ResourceLocation flareTexture = new ResourceLocation(RefStrings.MODID, "textures/misc/space/sunspike.png");
	private static final ResourceLocation nightTexture = new ResourceLocation(RefStrings.MODID, "textures/misc/space/night.png");
	private static final ResourceLocation digammaStar = new ResourceLocation(RefStrings.MODID, "textures/misc/space/star_digamma.png");
	private static final ResourceLocation lodeStar = new ResourceLocation(RefStrings.MODID, "textures/misc/star_lode.png");
	private static final ResourceLocation stationTexture = new ResourceLocation(RefStrings.MODID, "textures/misc/space/station.png");

	private static final ResourceLocation impactTexture = new ResourceLocation(RefStrings.MODID, "textures/misc/space/impact.png");
	private static final ResourceLocation shockwaveTexture = new ResourceLocation(RefStrings.MODID, "textures/particle/shockwave.png");
	private static final ResourceLocation shockFlareTexture = new ResourceLocation(RefStrings.MODID, "textures/particle/flare.png");

	private static final ResourceLocation ringTexture = new ResourceLocation(RefStrings.MODID, "textures/misc/space/rings.png");
	private static final ResourceLocation destroyedBody = new ResourceLocation(RefStrings.MODID, "textures/misc/space/destroyed.png");

	private static final ResourceLocation thatmoShield = new ResourceLocation(RefStrings.MODID, "textures/particle/cens.png");

	private static final Shader fleshShader = new Shader(new ResourceLocation(RefStrings.MODID, "shaders/fle.frag"));

	private static final ResourceLocation noise = new ResourceLocation(RefStrings.MODID, "shaders/iChannel1.png");

	protected static final Shader planetShader = new Shader(new ResourceLocation(RefStrings.MODID, "shaders/crescent.frag"));
	protected static final Shader swarmShader = new Shader(new ResourceLocation(RefStrings.MODID, "shaders/swarm.vert"), new ResourceLocation(RefStrings.MODID, "shaders/swarm.frag"));

	private static final ResourceLocation particleBase = new ResourceLocation(RefStrings.MODID + ":textures/particle/particle_base.png");


	private static final ResourceLocation[] citylights = new ResourceLocation[] {
		new ResourceLocation(RefStrings.MODID, "textures/misc/space/citylights_0.png"),
		new ResourceLocation(RefStrings.MODID, "textures/misc/space/citylights_1.png"),
		new ResourceLocation(RefStrings.MODID, "textures/misc/space/citylights_2.png"),
		new ResourceLocation(RefStrings.MODID, "textures/misc/space/citylights_3.png"),
	};

	private static final ResourceLocation defaultMask = new ResourceLocation(RefStrings.MODID, "textures/misc/space/default_mask.png");

	private static final String[] GL_SKY_LIST = new String[] { "glSkyList", "field_72771_w", "G" };
	private static final String[] GL_SKY_LIST2 = new String[] { "glSkyList2", "field_72781_x", "H" };

	public static boolean displayListsInitialized = false;
	public static int glSkyList;
	public static int glSkyList2;

	private static boolean gl13;

	public SkyProviderCelestial() {
		if(!displayListsInitialized) {
			initializeDisplayLists();
		}
	}

	private void initializeDisplayLists() {
		ContextCapabilities contextcapabilities = GLContext.getCapabilities();

		Minecraft mc = Minecraft.getMinecraft();
		glSkyList = ReflectionHelper.getPrivateValue(RenderGlobal.class, mc.renderGlobal, GL_SKY_LIST);
		glSkyList2 = ReflectionHelper.getPrivateValue(RenderGlobal.class, mc.renderGlobal, GL_SKY_LIST2);

		gl13 = contextcapabilities.OpenGL13;

		displayListsInitialized = true;
	}

	private static int lastBrightestPixel = 0;

	@Override
	public void render(float partialTicks, WorldClient world, Minecraft mc) {
		// We can now guarantee that this only runs with celestial, but it doesn't hurt to be safe
		if(!(world.provider instanceof WorldProviderCelestial)) return;

		WorldProviderCelestial celestialProvider = (WorldProviderCelestial) world.provider;

		// Without mixins, we have to resort to some very wacky ways of checking that the lightmap needs to be updated
		int[] lmColors = net.minecraftforge.fml.relauncher.ReflectionHelper.getPrivateValue(net.minecraft.client.renderer.EntityRenderer.class, mc.entityRenderer, "field_78504_Q", "lightmapColors");
		net.minecraft.client.renderer.texture.DynamicTexture lmTex = net.minecraftforge.fml.relauncher.ReflectionHelper.getPrivateValue(net.minecraft.client.renderer.EntityRenderer.class, mc.entityRenderer, "field_78513_d", "lightmapTexture");
		// fortunately, thanks to torch flickering, we can just check to see if the brightest pixel has been modified
		if(lmColors != null && lastBrightestPixel != lmColors[255] + lmColors[250]) {
			if(celestialProvider.updateLightmap(lmColors) && lmTex != null) {
				lmTex.updateDynamicTexture();
			}

			lastBrightestPixel = lmColors[255] + lmColors[250];
		}

		float fogIntensity = ModEventHandlerRenderer.lastFogDensity * 30;

		CelestialBody body = CelestialBody.getBody(world);
		CelestialBody sun = body.getStar();
		CBT_Atmosphere atmosphere = body.getTrait(CBT_Atmosphere.class);

		boolean hasAtmosphere = atmosphere != null;

		float pressure = hasAtmosphere ? (float)atmosphere.getPressure() : 0.0F;
		float visibility = hasAtmosphere ? MathHelper.clamp(2.0F - pressure, 0.1F, 1.0F) : 1.0F;

		GL11.glDisable(GL11.GL_TEXTURE_2D);
		Vec3d skyColor = world.getSkyColor(mc.getRenderViewEntity(), partialTicks);

		float skyR = (float) skyColor.x;
		float skyG = (float) skyColor.y;
		float skyB = (float) skyColor.z;

		// Diminish sky colour when leaving the atmosphere
		if(mc.getRenderViewEntity().posY > 300) {
			double curvature = MathHelper.clamp((800.0F - (float)mc.getRenderViewEntity().posY) / 500.0F, 0.0F, 1.0F);
			skyR *= curvature;
			skyG *= curvature;
			skyB *= curvature;
		}

		if(mc.gameSettings.anaglyph) {
			float[] anaglyphColor = applyAnaglyph(skyR, skyG, skyB);
			skyR = anaglyphColor[0];
			skyG = anaglyphColor[1];
			skyB = anaglyphColor[2];
		}

		float planetR = skyR;
		float planetG = skyG;
		float planetB = skyB;

		if(fogIntensity > 0.01F) {
			Vec3d fogColor = world.getFogColor(partialTicks);
			planetR = (float)BobMathUtil.clampedLerp(skyR, fogColor.x, fogIntensity);
			planetG = (float)BobMathUtil.clampedLerp(skyG, fogColor.y, fogIntensity);
			planetB = (float)BobMathUtil.clampedLerp(skyB, fogColor.z, fogIntensity);
		}

		Vec3d planetTint = new Vec3d(planetR, planetG, planetB);

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder tessellatorBuffer = tessellator.getBuffer();

		GL11.glDepthMask(false);
		GL11.glEnable(GL11.GL_FOG);
		GL11.glColor3f(skyR, skyG, skyB);

		// Set maximum sky fog distance to 12 chunks, works nicely with Celeritas/Distant Horizons
		// and makes for a more consistent sky in vanilla too
		GL11.glPushAttrib(GL11.GL_FOG_BIT);
		{

			GL11.glFogf(GL11.GL_FOG_START, 0.0F);
			GL11.glFogf(GL11.GL_FOG_END, Math.min(12.0F, mc.gameSettings.renderDistanceChunks) * 16.0F);

			GL11.glCallList(glSkyList);

		}
		GL11.glPopAttrib();

		GL11.glDisable(GL11.GL_FOG);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		GL11.glEnable(GL11.GL_TEXTURE_2D);

		GL11.glEnable(GL11.GL_BLEND);
		RenderHelper.disableStandardItemLighting();

		OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);

		float starBrightness = world.getStarBrightness(partialTicks) * visibility;
		float solarAngle = world.getCelestialAngle(partialTicks);
		float siderealAngle = (float)SolarSystem.calculateSiderealAngle(world, partialTicks, body);

		// Handle any special per-body sunset rendering
		renderSunset(partialTicks, world, mc, solarAngle, pressure, body.surfaceTexture);

		renderStars(partialTicks, world, mc, starBrightness, solarAngle + siderealAngle, body.axialTilt);


		GL11.glPushMatrix();
		{

			GL11.glRotatef(body.axialTilt, 1.0F, 0.0F, 0.0F);
			GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);
			GL11.glRotatef(solarAngle * 360.0F, 1.0F, 0.0F, 0.0F);

			// Draw DIGAMMA STAR
			renderDigamma(partialTicks, world, mc, solarAngle);

			OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE, GL11.GL_ZERO);

			// Scale sun size for rendering (texture is 4 times larger than actual, for glow)
			double sunSize = SolarSystem.calculateSunSize(body) * SolarSystem.SUN_RENDER_SCALE;
			double coronaSize = sunSize * (3 - MathHelper.clamp(pressure, 0.0F, 1.0F));

			renderSun(partialTicks, world, mc, sun, sunSize, coronaSize, visibility, pressure);

			float blendAmount = hasAtmosphere ? MathHelper.clamp(1 - world.getSunBrightnessFactor(partialTicks), 0.25F, 1F) : 1F;

			renderCelestials(partialTicks, world, mc, celestialProvider.metrics, solarAngle, null, planetTint, visibility, blendAmount, null, SolarSystem.MAX_APPARENT_SIZE_SURFACE);

			GL11.glEnable(GL11.GL_BLEND);

			if(visibility > 0.2F) {
				// JEFF BOZOS WOULD LIKE TO KNOW YOUR LOCATION
				// ... to send you a pakedge :)))
				if(world.provider.getDimension() == 0) {
					Satellite.renderDefault(partialTicks, world, mc, solarAngle, 1916169, new float[] { 1.0F, 0.534F, 0.385F, 1.0F });
				}

				// Light up the sky
				for(Map.Entry<Integer, Satellite> satelliteEntry : SatelliteSavedData.getClientSats().entrySet()) {
					satelliteEntry.getValue().render(partialTicks, world, mc, solarAngle, satelliteEntry.getKey());
				}

				// Stations, too
				for(OrbitalStation station : OrbitalStation.orbitingStations) {
					renderStation(partialTicks, world, mc, station, solarAngle);
				}
			}

		}
		GL11.glPopMatrix();

		render3DModel(partialTicks, world, mc);

		// TODO: fix EVERYTHING

		CBT_War war = body.getTrait(CBT_War.class);
		if(war != null) {
			for(int i = 0; i < war.getProjectiles().size(); i++) {
				CBT_War.Projectile projectile = war.getProjectiles().get(i);
				float thing = projectile.getFlashtime() + partialTicks;

				if(projectile.getTravel() <= 0) {
					float alpd = 1.0F - Math.min(1.0F, thing / 100);

					GL11.glPushMatrix();
					{

						render3DModel(partialTicks, world, mc);

						GL11.glTranslated(projectile.getTranslateX() + 70, projectile.getTranslateY(), projectile.getTranslateZ() + 50);
						GL11.glScaled(thing, thing, thing);
						GL11.glRotated(90.0, -10.0, -1.0, 50.0);
						GL11.glRotated(20.0, -0.0, -1.0, 1.0);

						GL11.glColor4d(1, 1, 1, alpd);

						mc.renderEngine.bindTexture(shockwaveTexture);
						ResourceManager.plane.renderAll();

					}
					GL11.glPopMatrix();

					GL11.glPushMatrix();
					{

						GL11.glTranslated(projectile.getTranslateX() + 70, projectile.getTranslateY(), projectile.getTranslateZ() + 50);
						GL11.glScaled(thing * 0.4f, thing * 0.4f, thing * 0.4f);
						GL11.glRotated(90.0, -10.0, -1.0, 50.0);
						GL11.glRotated(20.0, -0.0, -1.0, 1.0);
						GL11.glColor4d(1, 1, 1, alpd);

						mc.renderEngine.bindTexture(thatmoShield);
						ResourceManager.plane.renderAll();

					}
					GL11.glPopMatrix();
				}
			}
		}
		double playerX = mc.player.prevPosX + (mc.player.posX - mc.player.prevPosX) * partialTicks;
		double playerY = mc.player.prevPosY + (mc.player.posY - mc.player.prevPosY) * partialTicks;
		double playerZ = mc.player.prevPosZ + (mc.player.posZ - mc.player.prevPosZ) * partialTicks;
		for(com.hbm.dim.WorldProviderCelestial.Meteor meteor : WorldProviderCelestial.meteors) {
			GL11.glPushMatrix();

			// optimised 3 sqrt per meteor to just 1
			Vec3d offset = new Vec3d(meteor.posX - playerX, meteor.posY - playerY, meteor.posZ - playerZ);
			double offsetLength = offset.length();
			double distance = Math.min(Minecraft.getMinecraft().gameSettings.renderDistanceChunks * 16, offsetLength);
			Vec3d offsetNormal = offsetLength >= 1.0E-4D ? new Vec3d(offset.x / offsetLength, offset.y / offsetLength, offset.z / offsetLength) : offset;
			Vec3d renderOffset = new Vec3d(offsetNormal.x * distance, offsetNormal.y * distance, offsetNormal.z * distance);

			GL11.glTranslated(renderOffset.x, renderOffset.y, renderOffset.z);

			double descent = 2017d - meteor.posY;
			double quadratic = (-(descent * descent) + (1517 * descent)) / 41;

			float scalar = (float) (quadratic / offsetLength);
			GL11.glScaled(scalar, scalar, scalar);

			if(meteor.type == com.hbm.dim.WorldProviderCelestial.MeteorType.SMOKE) {
				GL11.glColor4d(1, 0, 0, 1);
				renderSmoke(particleBase, meteor.age);
			} else {
				GL11.glColor4d(1, 1, 1, 1);
				renderGlow(shockFlareTexture, 1, 1, 1, partialTicks);
			}

			GL11.glPopMatrix();
		}

		if(body.hasRings) {
			GL11.glPushMatrix();
			{

				GL11.glRotatef(body.axialTilt - body.ringTilt, 1.0F, 0.0F, 0.0F);
				GL11.glTranslatef(0, -100, 0);
				GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);

				renderRings(partialTicks, world, mc, body.ringTilt, body.ringColor, 200, visibility);

			}
			GL11.glPopMatrix();
		}

		renderSpecialEffects(partialTicks, world, mc);

		CBT_COMPROMISED compromised = body.getTrait(CBT_COMPROMISED.class);
		if(compromised != null) {
			GL11.glPushMatrix();
			{

				float time = ((float)world.getWorldTime() + partialTicks) * 0.2F;

				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
				GL11.glDisable(GL11.GL_CULL_FACE);

				fleshShader.use();
				GL11.glScaled(194.5, 70.5, 94.5);
				GL11.glRotated(90, 0, 0, 1);

				mc.renderEngine.bindTexture(noise);
				ResourceManager.sphere_v2.renderAll();

				// Fix orbital plane
				GL11.glRotatef(-90.0F, 0, 1, 0);

				fleshShader.setUniform1f("iTime", time * 0.05F);
				fleshShader.setUniform1i("iChannel1", 0);

				fleshShader.stop();

				OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE, GL11.GL_ZERO);

			}
			GL11.glPopMatrix();
		}

		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glEnable(GL11.GL_FOG);

		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glColor3f(0.0F, 0.0F, 0.0F);

		Vec3d pos = mc.player.getPositionVector();
		double heightAboveHorizon = pos.y - world.getHorizon();

		if(heightAboveHorizon < 0.0D) {
			GL11.glPushMatrix();
			{

				GL11.glTranslatef(0.0F, 12.0F, 0.0F);
				GL11.glCallList(glSkyList2);

			}
			GL11.glPopMatrix();

			float f8 = 1.0F;
			float f9 = -((float) (heightAboveHorizon + 65.0D));
			float opposite = -f8;

			tessellatorBuffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
			int tessellatorColor0Packed = 0;
			int tessellatorColor0Alpha = Math.max(0, Math.min(255, 255));
			tessellatorBuffer.pos(-f8, f9, f8).color((tessellatorColor0Packed >> 16) & 255, (tessellatorColor0Packed >> 8) & 255, tessellatorColor0Packed & 255, tessellatorColor0Alpha).endVertex();
			tessellatorBuffer.pos(f8, f9, f8).color((tessellatorColor0Packed >> 16) & 255, (tessellatorColor0Packed >> 8) & 255, tessellatorColor0Packed & 255, tessellatorColor0Alpha).endVertex();
			tessellatorBuffer.pos(f8, opposite, f8).color((tessellatorColor0Packed >> 16) & 255, (tessellatorColor0Packed >> 8) & 255, tessellatorColor0Packed & 255, tessellatorColor0Alpha).endVertex();
			tessellatorBuffer.pos(-f8, opposite, f8).color((tessellatorColor0Packed >> 16) & 255, (tessellatorColor0Packed >> 8) & 255, tessellatorColor0Packed & 255, tessellatorColor0Alpha).endVertex();
			tessellatorBuffer.pos(-f8, opposite, -f8).color((tessellatorColor0Packed >> 16) & 255, (tessellatorColor0Packed >> 8) & 255, tessellatorColor0Packed & 255, tessellatorColor0Alpha).endVertex();
			tessellatorBuffer.pos(f8, opposite, -f8).color((tessellatorColor0Packed >> 16) & 255, (tessellatorColor0Packed >> 8) & 255, tessellatorColor0Packed & 255, tessellatorColor0Alpha).endVertex();
			tessellatorBuffer.pos(f8, f9, -f8).color((tessellatorColor0Packed >> 16) & 255, (tessellatorColor0Packed >> 8) & 255, tessellatorColor0Packed & 255, tessellatorColor0Alpha).endVertex();
			tessellatorBuffer.pos(-f8, f9, -f8).color((tessellatorColor0Packed >> 16) & 255, (tessellatorColor0Packed >> 8) & 255, tessellatorColor0Packed & 255, tessellatorColor0Alpha).endVertex();
			tessellatorBuffer.pos(f8, opposite, -f8).color((tessellatorColor0Packed >> 16) & 255, (tessellatorColor0Packed >> 8) & 255, tessellatorColor0Packed & 255, tessellatorColor0Alpha).endVertex();
			tessellatorBuffer.pos(f8, opposite, f8).color((tessellatorColor0Packed >> 16) & 255, (tessellatorColor0Packed >> 8) & 255, tessellatorColor0Packed & 255, tessellatorColor0Alpha).endVertex();
			tessellatorBuffer.pos(f8, f9, f8).color((tessellatorColor0Packed >> 16) & 255, (tessellatorColor0Packed >> 8) & 255, tessellatorColor0Packed & 255, tessellatorColor0Alpha).endVertex();
			tessellatorBuffer.pos(f8, f9, -f8).color((tessellatorColor0Packed >> 16) & 255, (tessellatorColor0Packed >> 8) & 255, tessellatorColor0Packed & 255, tessellatorColor0Alpha).endVertex();
			tessellatorBuffer.pos(-f8, f9, -f8).color((tessellatorColor0Packed >> 16) & 255, (tessellatorColor0Packed >> 8) & 255, tessellatorColor0Packed & 255, tessellatorColor0Alpha).endVertex();
			tessellatorBuffer.pos(-f8, f9, f8).color((tessellatorColor0Packed >> 16) & 255, (tessellatorColor0Packed >> 8) & 255, tessellatorColor0Packed & 255, tessellatorColor0Alpha).endVertex();
			tessellatorBuffer.pos(-f8, opposite, f8).color((tessellatorColor0Packed >> 16) & 255, (tessellatorColor0Packed >> 8) & 255, tessellatorColor0Packed & 255, tessellatorColor0Alpha).endVertex();
			tessellatorBuffer.pos(-f8, opposite, -f8).color((tessellatorColor0Packed >> 16) & 255, (tessellatorColor0Packed >> 8) & 255, tessellatorColor0Packed & 255, tessellatorColor0Alpha).endVertex();
			tessellatorBuffer.pos(-f8, opposite, -f8).color((tessellatorColor0Packed >> 16) & 255, (tessellatorColor0Packed >> 8) & 255, tessellatorColor0Packed & 255, tessellatorColor0Alpha).endVertex();
			tessellatorBuffer.pos(-f8, opposite, f8).color((tessellatorColor0Packed >> 16) & 255, (tessellatorColor0Packed >> 8) & 255, tessellatorColor0Packed & 255, tessellatorColor0Alpha).endVertex();
			tessellatorBuffer.pos(f8, opposite, f8).color((tessellatorColor0Packed >> 16) & 255, (tessellatorColor0Packed >> 8) & 255, tessellatorColor0Packed & 255, tessellatorColor0Alpha).endVertex();
			tessellatorBuffer.pos(f8, opposite, -f8).color((tessellatorColor0Packed >> 16) & 255, (tessellatorColor0Packed >> 8) & 255, tessellatorColor0Packed & 255, tessellatorColor0Alpha).endVertex();
			tessellator.draw();
		}

		if(world.provider.isSkyColored()) {
			GL11.glColor3f(skyR * 0.2F + 0.04F, skyG * 0.2F + 0.04F, skyB * 0.6F + 0.1F);
		} else {
			GL11.glColor3f(skyR, skyG, skyB);
		}

		GL11.glPushMatrix();
		{

			GL11.glTranslatef(0.0F, -((float) (heightAboveHorizon - 16.0D)), 0.0F);
			GL11.glCallList(glSkyList2);

		}
		GL11.glPopMatrix();

		double sc = 1 / (pos.y / 1000);
		double uvOffset = (pos.x / 1024) % 1;
		GL11.glPushMatrix();
		{

			GL11.glEnable(GL11.GL_TEXTURE_2D);
			GL11.glDisable(GL11.GL_ALPHA_TEST);
			GL11.glDisable(GL11.GL_FOG);
			GL11.glEnable(GL11.GL_BLEND);

			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

			float sunBrightness = world.getSunBrightness(partialTicks);

			GL11.glColor4f(sunBrightness, sunBrightness, sunBrightness, ((float)pos.y - 200.0F) / 300.0F);
			mc.renderEngine.bindTexture(body.texture);
			GL11.glRotated(180, 1, 0, 0);

			tessellatorBuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
			tessellatorBuffer.pos(-115 * sc, 100.0D, -115 * sc).tex(0.0D + uvOffset, 0.0D).endVertex();
			tessellatorBuffer.pos(115 * sc, 100.0D, -115 * sc).tex(1.0D + uvOffset, 0.0D).endVertex();
			tessellatorBuffer.pos(115 * sc, 100.0D, 115 * sc).tex(1.0D + uvOffset, 1.0D).endVertex();
			tessellatorBuffer.pos(-115 * sc, 100.0D, 115 * sc).tex(0.0D + uvOffset, 1.0D).endVertex();
			tessellator.draw();

			GL11.glDisable(GL11.GL_TEXTURE_2D);
			GL11.glEnable(GL11.GL_ALPHA_TEST);
			GL11.glEnable(GL11.GL_FOG);
			GL11.glDisable(GL11.GL_BLEND);

			OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE, GL11.GL_ZERO);

		}

		GL11.glPopMatrix();
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glDepthMask(true);

	}

	protected void renderSunset(float partialTicks, WorldClient world, Minecraft mc, float solarAngle, float pressure, ResourceLocation surfaceTexture) {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder tessellatorBuffer = tessellator.getBuffer();

		float[] sunsetColor = calcSunriseSunsetColors(partialTicks, world, mc, solarAngle, pressure);

		if(sunsetColor != null) {
			float[] anaglyphColor = mc.gameSettings.anaglyph ? applyAnaglyph(sunsetColor) : sunsetColor;
			float sunsetDirection = MathHelper.sin(world.getCelestialAngleRadians(partialTicks)) < 0.0F ? 180.0F : 0.0F;

			GL11.glDisable(GL11.GL_TEXTURE_2D);
			GL11.glShadeModel(GL11.GL_SMOOTH);

			GL11.glPushMatrix();
			{

				GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
				GL11.glRotatef(sunsetDirection, 0.0F, 0.0F, 1.0F);
				GL11.glRotatef(90.0F, 0.0F, 0.0F, 1.0F);

				tessellatorBuffer.begin(6, DefaultVertexFormats.POSITION_COLOR);
				int tessellatorColor0Red = Math.max(0, Math.min(255, (int) ((anaglyphColor[0]) * 255.0F)));
				int tessellatorColor0Green = Math.max(0, Math.min(255, (int) ((anaglyphColor[1]) * 255.0F)));
				int tessellatorColor0Blue = Math.max(0, Math.min(255, (int) ((anaglyphColor[2]) * 255.0F)));
				int tessellatorColor0Alpha = Math.max(0, Math.min(255, (int) ((sunsetColor[3]) * 255.0F)));
				tessellatorBuffer.pos(0.0, 100.0, 0.0).color(tessellatorColor0Red, tessellatorColor0Green, tessellatorColor0Blue, tessellatorColor0Alpha).endVertex();
				int tessellatorColor1Red = Math.max(0, Math.min(255, (int) ((sunsetColor[0]) * 255.0F)));
				int tessellatorColor1Green = Math.max(0, Math.min(255, (int) ((sunsetColor[1]) * 255.0F)));
				int tessellatorColor1Blue = Math.max(0, Math.min(255, (int) ((sunsetColor[2]) * 255.0F)));
				int tessellatorColor1Alpha = Math.max(0, Math.min(255, (int) ((0.0F) * 255.0F)));
				byte segments = 16;

				for(int j = 0; j <= segments; ++j) {
					float angle = (float)j * 3.1415927F * 2.0F / (float)segments;
					float sinAngle = MathHelper.sin(angle);
					float cosAngle = MathHelper.cos(angle);
					tessellatorBuffer.pos((double)(sinAngle * 120.0F), (double)(cosAngle * 120.0F), (double)(-cosAngle * 40.0F * sunsetColor[3])).color(tessellatorColor1Red, tessellatorColor1Green, tessellatorColor1Blue, tessellatorColor1Alpha).endVertex();
				}

				tessellator.draw();

			}
			GL11.glPopMatrix();
			GL11.glShadeModel(GL11.GL_FLAT);
			GL11.glEnable(GL11.GL_TEXTURE_2D);

			// charged dust
			if(pressure < 0.05F) {
				Random rand = new Random(0);

				GL11.glPushMatrix();
				{

					double time = ((double)world.provider.getWorldTime() + partialTicks) * 0.002;

					GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
					GL11.glRotatef(sunsetDirection, 0.0F, 0.0F, 1.0F);
					GL11.glRotatef(90.0F, 0.0F, 0.0F, 1.0F);

					mc.renderEngine.bindTexture(surfaceTexture);
					GL11.glColor4f(0.5F + rand.nextFloat() * 0.5F, 0.5F + rand.nextFloat() * 0.5F, 0.5F + rand.nextFloat() * 0.5F, rand.nextFloat() * sunsetColor[3] * 4.0F);

					OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_CONSTANT_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);

					tessellatorBuffer.begin(GL11.GL_POINTS, DefaultVertexFormats.POSITION);
					for(int i = 0; i < 1024; i++) {
						tessellatorBuffer.pos(rand.nextGaussian() * 50, 100, -((Math.abs(rand.nextGaussian() * 20) + time) % Math.abs(rand.nextGaussian()) * 20)).endVertex();
					}
					tessellator.draw();

				}
				GL11.glPopMatrix();
			}
		}
	}

	// We don't want certain sunrise/sunset effects to change the fog colour, so we do them here
	protected float[] calcSunriseSunsetColors(float partialTicks, WorldClient world, Minecraft mc, float solarAngle, float pressure) {
		if(pressure < 0.05F) {
			float cutoff = 0.4F;
			float angle = MathHelper.cos(solarAngle * (float)Math.PI * 2.0F) - 0.0F;

			if(angle < -cutoff || angle > cutoff) return null;

			float colorIntensity = angle / cutoff * 0.5F + 0.5F;
			float alpha = 1.0F - (1.0F - MathHelper.sin(colorIntensity * (float)Math.PI)) * 0.99F;
			alpha *= alpha;
			return new float[] { 0.9F, 1.0F, 1.0F, alpha * 0.2F };
		}

		return world.provider.calcSunriseSunsetColors(world.getCelestialAngle(partialTicks), partialTicks);
	}

	protected void renderStars(float partialTicks, WorldClient world, Minecraft mc, float starBrightness, float siderealAngle, float axialTilt) {
		Tessellator tessellator = Tessellator.getInstance();

		if(starBrightness > 0.0F) {
			GL11.glPushMatrix();
			{
				GL11.glRotatef(axialTilt, 1.0F, 0.0F, 0.0F);

				mc.renderEngine.bindTexture(nightTexture);

				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);

				float starBrightnessAlpha = starBrightness * 0.6f;
				GL11.glColor4f(1.0F, 1.0F, 1.0F, starBrightnessAlpha);

				GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);

				GL11.glRotatef(siderealAngle * 360.0F, 1.0F, 0.0F, 0.0F);
				GL11.glRotatef(-90.0F, 1.0F, 0.0F, 0.0F);
				GL11.glColor4f(1.0F, 1.0F, 1.0F, starBrightnessAlpha);

				GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
				GL11.glRotatef(-90.0F, 0.0F, 0.0F, 1.0F);
				renderSkyboxSide(tessellator, 4);

				GL11.glPushMatrix();
				GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
				renderSkyboxSide(tessellator, 1);
				GL11.glPopMatrix();

				GL11.glPushMatrix();
				GL11.glRotatef(-90.0F, 1.0F, 0.0F, 0.0F);
				renderSkyboxSide(tessellator, 0);
				GL11.glPopMatrix();

				GL11.glRotatef(90.0F, 0.0F, 0.0F, 1.0F);
				renderSkyboxSide(tessellator, 5);

				GL11.glRotatef(90.0F, 0.0F, 0.0F, 1.0F);
				renderSkyboxSide(tessellator, 2);

				GL11.glRotatef(90.0F, 0.0F, 0.0F, 1.0F);
				renderSkyboxSide(tessellator, 3);

				OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);

			}
			GL11.glPopMatrix();
		}
	}

	protected void renderSun(float partialTicks, WorldClient world, Minecraft mc, CelestialBody sun, double sunSize, double coronaSize, float visibility, float pressure) {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder tessellatorBuffer = tessellator.getBuffer();

		CBT_Dyson dyson = sun.getTrait(CBT_Dyson.class);
		int swarmCount = dyson != null ? dyson.size() : 0;

		if(sun.shader != null && sun.hasTrait(CBT_Destroyed.class)) {
			// BLACK HOLE SUN
			// WON'T YOU COME
			// AND WASH AWAY THE RAIN

			Shader shader = sun.shader;
			double shaderSize = sunSize * sun.shaderScale;

			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

			shader.use();

			float time = ((float)world.getWorldTime() + partialTicks) / 20.0F;

			mc.renderEngine.bindTexture(noise);
			GL11.glPushMatrix();

			// Fix orbital plane
			GL11.glRotatef(-90.0F, 0, 1, 0);
			shader.setUniform1f("iTime", time);
			shader.setUniform1i("iChannel1", 0);

			tessellatorBuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
			tessellatorBuffer.pos(-shaderSize, 100.0D, -shaderSize).tex(0.0D, 0.0D).endVertex();
			tessellatorBuffer.pos(shaderSize, 100.0D, -shaderSize).tex(1.0D, 0.0D).endVertex();
			tessellatorBuffer.pos(shaderSize, 100.0D, shaderSize).tex(1.0D, 1.0D).endVertex();
			tessellatorBuffer.pos(-shaderSize, 100.0D, shaderSize).tex(0.0D, 1.0D).endVertex();
			tessellator.draw();

			shader.stop();

			GL11.glPopMatrix();

			OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE, GL11.GL_ZERO);
		} else {
			// Some blanking to conceal the stars
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			GL11.glColor4f(0.0F, 0.0F, 0.0F, 1.0F);

			tessellatorBuffer.begin(7, DefaultVertexFormats.POSITION);
			tessellatorBuffer.pos(-sunSize, 99.9D, -sunSize).endVertex();
			tessellatorBuffer.pos(sunSize, 99.9D, -sunSize).endVertex();
			tessellatorBuffer.pos(sunSize, 99.9D, sunSize).endVertex();
			tessellatorBuffer.pos(-sunSize, 99.9D, sunSize).endVertex();
			tessellator.draw();

			// Draw the sun to the depth buffer to block swarm members that are behind
			GL11.glDepthMask(true);
			GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.0F);

			tessellatorBuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
			tessellatorBuffer.pos(-sunSize * 0.25D, 100.1D, -sunSize * 0.25D).tex(0.0D, 0.0D).endVertex();
			tessellatorBuffer.pos(sunSize * 0.25D, 100.1D, -sunSize * 0.25D).tex(1.0D, 0.0D).endVertex();
			tessellatorBuffer.pos(sunSize * 0.25D, 100.1D, sunSize * 0.25D).tex(1.0D, 1.0D).endVertex();
			tessellatorBuffer.pos(-sunSize * 0.25D, 100.1D, sunSize * 0.25D).tex(0.0D, 1.0D).endVertex();
			tessellator.draw();

			GL11.glDepthMask(false);

			GL11.glEnable(GL11.GL_TEXTURE_2D);
			GL11.glColor4f(1.0F, 1.0F, 1.0F, visibility);

			mc.renderEngine.bindTexture(SolarSystem.kerbol.texture);

			float[] sunColor = world.provider instanceof WorldProviderCelestial
				? ((WorldProviderCelestial) world.provider).getSunColor()
				: new float[] { 1.0F, 1.0F, 1.0F };

			GL11.glColor4f(sunColor[0], sunColor[1], sunColor[2], visibility);
			tessellatorBuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
			tessellatorBuffer.pos(-sunSize, 100.0D, -sunSize).tex(0.0D, 0.0D).endVertex();
			tessellatorBuffer.pos(sunSize, 100.0D, -sunSize).tex(1.0D, 0.0D).endVertex();
			tessellatorBuffer.pos(sunSize, 100.0D, sunSize).tex(1.0D, 1.0D).endVertex();
			tessellatorBuffer.pos(-sunSize, 100.0D, sunSize).tex(0.0D, 1.0D).endVertex();
			tessellator.draw();

			// Draw a big ol' spiky flare! Less so when there is an atmosphere
			GL11.glColor4f(sunColor[0], sunColor[1], sunColor[2], 1 - MathHelper.clamp(pressure, 0.0F, 1.0F) * 0.75F);
			mc.renderEngine.bindTexture(flareTexture);

			tessellatorBuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
			tessellatorBuffer.pos(-coronaSize, 99.9D, -coronaSize).tex(0.0D, 0.0D).endVertex();
			tessellatorBuffer.pos(coronaSize, 99.9D, -coronaSize).tex(1.0D, 0.0D).endVertex();
			tessellatorBuffer.pos(coronaSize, 99.9D, coronaSize).tex(1.0D, 1.0D).endVertex();
			tessellatorBuffer.pos(-coronaSize, 99.9D, coronaSize).tex(0.0D, 1.0D).endVertex();
			tessellator.draw();

			// Draw the swarm members with depth occlusion
			// We do this last so we can render transparency against the sun
			renderSwarm(partialTicks, world, mc, sunSize * 0.5, swarmCount);

			// Clear and disable the depth buffer once again, buffer has to be writable to clear it
			GL11.glDepthMask(true);
			GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
			GL11.glDepthMask(false);
		}
	}

	private void renderSwarm(float partialTicks, WorldClient world, Minecraft mc, double swarmRadius, int swarmCount) {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder tessellatorBuffer = tessellator.getBuffer();

		// bloodseeking, parasitic, ecstatically tracing decay
		// thriving in the glow that death emits, the warm perfume it radiates

		//perfume makes my eyes water -j

		swarmShader.use();

		// swarm members render as pixels, which can vary based on screen resolution
		// because of this, we make the pixels more transparent based on their apparent size, which varies by a fair few factors
		// this isn't a foolproof solution, analyzing the projection matrices would be best, but it works for now.
		float swarmScreenSize = (float)((mc.displayHeight / mc.gameSettings.fovSetting) * swarmRadius * 0.002);
		float time = ((float)world.getWorldTime() + partialTicks) / 800.0F;

		swarmShader.setUniform1f("iTime", time);

		int offsetLocation = swarmShader.getUniformLocation("iOffset");

		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glColor4f(0.0F, 0.0F, 0.0F, MathHelper.clamp(swarmScreenSize, (float) (0), (float) (1)));

		GL11.glPushMatrix();
		{

			GL11.glTranslatef(0.0F, 100.0F, 0.0F);
			GL11.glScaled(swarmRadius, swarmRadius, swarmRadius);

			GL11.glPushMatrix();
			{

				GL11.glRotatef(80.0F, 1, 0, 0);

				tessellatorBuffer.begin(GL11.GL_POINTS, DefaultVertexFormats.POSITION);
				for(int i = 0; i < swarmCount; i += 3) {
					swarmShader.setUniform1f(offsetLocation, i);

					float t = i + time;
					double x = Math.cos(t);
					double z = Math.sin(t);

					tessellatorBuffer.pos(x, 0, z).endVertex();
				}
				tessellator.draw();

			}
			GL11.glPopMatrix();

			GL11.glPushMatrix();
			{

				GL11.glRotatef(60.0F, 0, 1, 0);
				GL11.glRotatef(80.0F, 1, 0, 0);

				tessellatorBuffer.begin(GL11.GL_POINTS, DefaultVertexFormats.POSITION);
				for(int i = 1; i < swarmCount; i += 3) {
					swarmShader.setUniform1f(offsetLocation, i);

					float t = i + time;
					double x = Math.cos(t);
					double z = Math.sin(t);

					tessellatorBuffer.pos(x, 0, z).endVertex();
				}
				tessellator.draw();

			}
			GL11.glPopMatrix();

			GL11.glPushMatrix();
			{

				GL11.glRotatef(-60.0F, 0, 1, 0);
				GL11.glRotatef(80.0F, 1, 0, 0);

				tessellatorBuffer.begin(GL11.GL_POINTS, DefaultVertexFormats.POSITION);
				for(int i = 2; i < swarmCount; i += 3) {
					swarmShader.setUniform1f(offsetLocation, i);

					float t = i + time;
					double x = Math.cos(t);
					double z = Math.sin(t);

					tessellatorBuffer.pos(x, 0, z).endVertex();
				}
				tessellator.draw();

			}
			GL11.glPopMatrix();

		}
		GL11.glPopMatrix();

		swarmShader.stop();

		OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE, GL11.GL_ZERO);
	}

	protected void renderCelestials(float partialTicks, WorldClient world, Minecraft mc, List<AstroMetric> metrics, float solarAngle, CelestialBody tidalLockedBody, Vec3d planetTint, float visibility, float blendAmount, CelestialBody orbiting, float maxSize) {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder tessellatorBuffer = tessellator.getBuffer();
		float blendDarken = 0.1F;

		double transitionMinSize = 0.1D;
		double transitionMaxSize = 0.5D;

		for(AstroMetric metric : metrics) {

			// Ignore self
			if(metric.distance == 0)
				continue;

			boolean orbitingThis = metric.body == orbiting;

			double uvOffset = orbitingThis ? 1 - ((((double)world.getWorldTime() + partialTicks) / 1024) % 1) : 0;
			float axialTilt = orbitingThis ? 0 : metric.body.axialTilt;

			GL11.glPushMatrix();
			{

				double size = MathHelper.clamp(metric.apparentSize, (double) (0), (double) (maxSize));
				boolean renderPoint = size < transitionMaxSize;
				boolean renderBody = size > transitionMinSize;

				GL11.glRotated(metric.angle, 1.0, 0.0, 0.0);
				GL11.glRotated(metric.inclination, 0.0, 0.0, 1.0);
				GL11.glRotatef(axialTilt + 90.0F, 0.0F, 1.0F, 0.0F);

				if(renderBody) {
					// Draw the back half of the ring (obscured by body)
					if(metric.body.hasRings) {
						GL11.glPushMatrix();
						{

							GL11.glColor4f(metric.body.ringColor[0], metric.body.ringColor[1], metric.body.ringColor[2], visibility);
							mc.renderEngine.bindTexture(ringTexture);

							GL11.glDisable(GL11.GL_CULL_FACE);

							double ringSize = size * metric.body.ringSize;

							GL11.glTranslatef(0.0F, 100.0F, 0.0F);
							GL11.glRotated(-metric.angle, 0, 0, 1);
							GL11.glRotatef(90.0F - metric.body.ringTilt, 1, 0, 0);
							GL11.glRotated(metric.angle, 0, 1, 0);

							tessellatorBuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
							tessellatorBuffer.pos(-ringSize, 0, -ringSize).tex(0.0D, 0.0D).endVertex();
							tessellatorBuffer.pos(ringSize, 0, -ringSize).tex(1.0D, 0.0D).endVertex();
							tessellatorBuffer.pos(ringSize, 0, 0).tex(1.0D, 0.5D).endVertex();
							tessellatorBuffer.pos(-ringSize, 0, 0).tex(0.0D, 0.5D).endVertex();
							tessellator.draw();

							GL11.glEnable(GL11.GL_CULL_FACE);

						}
						GL11.glPopMatrix();
					}

					CBT_Destroyed d = metric.body.getTrait(CBT_Destroyed.class);

					if(d != null) {
						// Stop calling things "interp", that's a verb not a noun
						//its cause of "interpolate" which is my favorite word apparently :(
						double destroyedProgressClientInterpolation = d.destProgress + size * 0.5;

						float alpha = (float) (1.0F - Math.min(1.0F, destroyedProgressClientInterpolation / 100));
						Random random = new Random(12);

						int numQuads = 30;
						for (int i = 0; i < numQuads; i++) {
							double radius = (random.nextDouble() * size) * d.destProgress;

							double randomTheta = random.nextDouble() * Math.PI * 2;
							double randomPhi = random.nextDouble() * Math.PI;

							double randomX = radius * Math.sin(randomPhi) * Math.cos(randomTheta) * 0.7;
							double randomY = radius * Math.sin(randomPhi) * Math.sin(randomTheta);
							double randomZ = radius * Math.cos(randomPhi) * 0.7;

							float randomRotation = random.nextFloat() * 360.0F;


							double uMin = random.nextDouble();
							double vMin = random.nextDouble();
							double uMax = Math.min(uMin + (random.nextDouble() * 0.2), 1.0);
							double vMax = Math.min(vMin + (random.nextDouble() * 0.2), 1.0);

							GL11.glPushMatrix();
							{

								GL11.glTranslated(randomX * -0.05, randomY * 0.00, randomZ * -0.05);

								GL11.glRotatef(randomRotation * d.destProgress * 0.05F, 0.0F, 1.0F, 0.0F);

								mc.renderEngine.bindTexture(metric.body.texture);
								GL11.glColor4d(1, 1, 1, 1);

								tessellatorBuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
								double qsize = size * random.nextDouble() * 0.1;
								tessellatorBuffer.pos(-qsize, 100.0D, -qsize).tex(uMin, vMin).endVertex();
								tessellatorBuffer.pos(qsize, 100.0D, -qsize).tex(uMax, vMin).endVertex();
								tessellatorBuffer.pos(qsize, 100.0D, qsize).tex(uMax, vMax).endVertex();
								tessellatorBuffer.pos(-qsize, 100.0D, qsize).tex(uMin, vMax).endVertex();

								tessellator.draw();
							}

							GL11.glPopMatrix();
							GL11.glPushMatrix();
							{

								GL11.glTranslated(randomX * 0.04, randomY * 0.00, randomZ * 0.04);

								GL11.glRotatef(randomRotation * d.destProgress * 0.05F, 0.0F, 1.0F, 0.0F);
								mc.renderEngine.bindTexture(destroyedBody);
								GL11.glColor4d(1, 1, 1, 1);
								tessellatorBuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
								double qsize = size * random.nextDouble() * 0.07;
								tessellatorBuffer.pos(-qsize, 100.0D, -qsize).tex(uMin, vMin).endVertex();
								tessellatorBuffer.pos(qsize, 100.0D, -qsize).tex(uMax, vMin).endVertex();
								tessellatorBuffer.pos(qsize, 100.0D, qsize).tex(uMax, vMax).endVertex();
								tessellatorBuffer.pos(-qsize, 100.0D, qsize).tex(uMin, vMax).endVertex();

								tessellator.draw();

							}
							GL11.glPopMatrix();

						}


						GL11.glColor4f(1.0F, 1.0F, 1.0F, alpha);
						mc.renderEngine.bindTexture(shockwaveTexture);
						double interpe = (d.destProgress * 0.5) * size * 0.1;
						tessellatorBuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
						tessellatorBuffer.pos(-interpe, 100.0D, -interpe).tex(0.0D + uvOffset, 0.0D).endVertex();
						tessellatorBuffer.pos(interpe, 100.0D, -interpe).tex(1.0D + uvOffset, 0.0D).endVertex();
						tessellatorBuffer.pos(interpe, 100.0D, interpe).tex(1.0D + uvOffset, 1.0D).endVertex();
						tessellatorBuffer.pos(-interpe, 100.0D, interpe).tex(0.0D + uvOffset, 1.0D).endVertex();
						tessellator.draw();


						GL11.glColor4f(1.0F, 1.0F, 1.0F, alpha * 2);
						mc.renderEngine.bindTexture(shockFlareTexture);

						destroyedProgressClientInterpolation = size * 3;
						tessellatorBuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
						tessellatorBuffer.pos(-destroyedProgressClientInterpolation, 100.0D, -destroyedProgressClientInterpolation).tex(0.0D + uvOffset, 0.0D).endVertex();
						tessellatorBuffer.pos(destroyedProgressClientInterpolation, 100.0D, -destroyedProgressClientInterpolation).tex(1.0D + uvOffset, 0.0D).endVertex();
						tessellatorBuffer.pos(destroyedProgressClientInterpolation, 100.0D, destroyedProgressClientInterpolation).tex(1.0D + uvOffset, 1.0D).endVertex();
						tessellatorBuffer.pos(-destroyedProgressClientInterpolation, 100.0D, destroyedProgressClientInterpolation).tex(0.0D + uvOffset, 1.0D).endVertex();
						tessellator.draw();

					} else {

						GL11.glDisable(GL11.GL_BLEND);
						GL11.glColor4f(1.0F, 1.0F, 1.0F, visibility);
						mc.renderEngine.bindTexture(metric.body.texture);

						tessellatorBuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
						tessellatorBuffer.pos(-size, 100.0D, -size).tex(0.0D + uvOffset, 0.0D).endVertex();
						tessellatorBuffer.pos(size, 100.0D, -size).tex(1.0D + uvOffset, 0.0D).endVertex();
						tessellatorBuffer.pos(size, 100.0D, size).tex(1.0D + uvOffset, 1.0D).endVertex();
						tessellatorBuffer.pos(-size, 100.0D, size).tex(0.0D + uvOffset, 1.0D).endVertex();
						tessellator.draw();


						CBT_Impact impact = metric.body.getTrait(CBT_Impact.class);
						CBT_Lights light = metric.body.getTrait(CBT_Lights.class);

						double impactTime = impact != null ? (world.getTotalWorldTime() - impact.time) + partialTicks : 0;
						int lightIntensity = light != null && impactTime < 40 ? light.getIntensity() : 0;

						int blackoutInterval = 8;
						int maxBlackouts = 5;

						int activeBlackouts = Math.min((int)(impactTime / blackoutInterval), maxBlackouts);

						GL11.glEnable(GL11.GL_BLEND);
						// Draw a shader on top to render celestial phase
						OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);

						GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

						planetShader.use();
						planetShader.setUniform1f("phase", (float)-metric.phase);
						planetShader.setUniform1f("offset", (float)uvOffset);
						planetShader.setUniform1i("lights", 0);
						planetShader.setUniform1i("cityMask", 1);
						planetShader.setUniform1i("blackouts", activeBlackouts);

						mc.renderEngine.bindTexture(citylights[lightIntensity]);
						if(gl13) {
							GL13.glActiveTexture(GL13.GL_TEXTURE1);
							mc.renderEngine.bindTexture(metric.body.cityMask != null ? metric.body.cityMask : defaultMask);
							GL13.glActiveTexture(GL13.GL_TEXTURE0);
						}

						tessellatorBuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
						tessellatorBuffer.pos(-size, 100.0D, -size).tex(0.0D, 0.0D).endVertex();
						tessellatorBuffer.pos(size, 100.0D, -size).tex(1.0D, 0.0D).endVertex();
						tessellatorBuffer.pos(size, 100.0D, size).tex(1.0D, 1.0D).endVertex();
						tessellatorBuffer.pos(-size, 100.0D, size).tex(0.0D, 1.0D).endVertex();
						tessellator.draw();

						GL11.glEnable(GL11.GL_TEXTURE_2D);

						planetShader.stop();

						OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE, GL11.GL_ZERO);

						if(impact != null) {
							double lavaAlpha = Math.min(impactTime * 0.1, 1.0);

							double impactSize = (impactTime * 0.1) * size * 0.035;
							double impactAlpha = 1.0 - Math.min(1.0, impactTime * 0.0015);
							double flareSize = size * 1.5;
							double flareAlpha = 1.0 - Math.min(1.0, impactTime * 0.002);

							if(lavaAlpha > 0) {
								GL11.glColor4d(1.0, 1.0, 1.0, lavaAlpha);
								mc.renderEngine.bindTexture(impactTexture);

								tessellatorBuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
								tessellatorBuffer.pos(-size, 100.0D, -size).tex(0.0D + uvOffset, 0.0D).endVertex();
								tessellatorBuffer.pos(size, 100.0D, -size).tex(1.0D + uvOffset, 0.0D).endVertex();
								tessellatorBuffer.pos(size, 100.0D, size).tex(1.0D + uvOffset, 1.0D).endVertex();
								tessellatorBuffer.pos(-size, 100.0D, size).tex(0.0D + uvOffset, 1.0D).endVertex();
								tessellator.draw();
							}

							GL11.glPushMatrix();
							{

								GL11.glTranslated(-size * 0.5, 0, size * 0.4);

								// impact shockwave, increases in size and fades out
								if(impactAlpha > 0) {
									GL11.glColor4d(1.0, 1.0, 1.0F, impactAlpha);
									mc.renderEngine.bindTexture(shockwaveTexture);

									tessellatorBuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
									tessellatorBuffer.pos(-impactSize, 100.0D, -impactSize).tex(0.0D, 0.0D).endVertex();
									tessellatorBuffer.pos(impactSize, 100.0D, -impactSize).tex(1.0D, 0.0D).endVertex();
									tessellatorBuffer.pos(impactSize, 100.0D, impactSize).tex(1.0D, 1.0D).endVertex();
									tessellatorBuffer.pos(-impactSize, 100.0D, impactSize).tex(0.0D, 1.0D).endVertex();
									tessellator.draw();
								}

								// impact flare, remains static in size and fades out
								if(flareAlpha > 0) {
									GL11.glColor4d(1.0F, 1.0F, 1.0F, flareAlpha);
									mc.renderEngine.bindTexture(shockFlareTexture);

									tessellatorBuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
									tessellatorBuffer.pos(-flareSize, 100.0D, -flareSize).tex(0.0D, 0.0D).endVertex();
									tessellatorBuffer.pos(flareSize, 100.0D, -flareSize).tex(1.0D, 0.0D).endVertex();
									tessellatorBuffer.pos(flareSize, 100.0D, flareSize).tex(1.0D, 1.0D).endVertex();
									tessellatorBuffer.pos(-flareSize, 100.0D, flareSize).tex(0.0D, 1.0D).endVertex();
									tessellator.draw();
								}

							}
							GL11.glPopMatrix();
						}


						GL11.glDisable(GL11.GL_TEXTURE_2D);

						// Draw another layer on top to blend with the atmosphere
						GL11.glColor4d(planetTint.x - blendDarken, planetTint.y - blendDarken, planetTint.z - blendDarken, (1 - blendAmount * visibility));

						tessellatorBuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
						tessellatorBuffer.pos(-size, 100.0D, -size).tex(0.0D, 0.0D).endVertex();
						tessellatorBuffer.pos(size, 100.0D, -size).tex(1.0D, 0.0D).endVertex();
						tessellatorBuffer.pos(size, 100.0D, size).tex(1.0D, 1.0D).endVertex();
						tessellatorBuffer.pos(-size, 100.0D, size).tex(0.0D, 1.0D).endVertex();
						tessellator.draw();

						GL11.glEnable(GL11.GL_TEXTURE_2D);
					}




					// Draw the front half of the ring (unobscured)
					if(metric.body.hasRings) {
						GL11.glColor4f(metric.body.ringColor[0], metric.body.ringColor[1], metric.body.ringColor[2], visibility);
						mc.renderEngine.bindTexture(ringTexture);

						double ringSize = size * metric.body.ringSize;

						GL11.glDisable(GL11.GL_CULL_FACE);

						GL11.glTranslatef(0.0F, 100.0F, 0.0F);
						GL11.glRotated(-metric.angle, 0, 0, 1);
						GL11.glRotatef(90.0F - metric.body.ringTilt, 1, 0, 0);
						GL11.glRotated(metric.angle, 0, 1, 0);

						tessellatorBuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
						tessellatorBuffer.pos(-ringSize, 0, 0).tex(0.0D, 0.5D).endVertex();
						tessellatorBuffer.pos(ringSize, 0, 0).tex(1.0D, 0.5D).endVertex();
						tessellatorBuffer.pos(ringSize, 0, ringSize).tex(1.0D, 1.0D).endVertex();
						tessellatorBuffer.pos(-ringSize, 0, ringSize).tex(0.0D, 1.0D).endVertex();
						tessellator.draw();

						GL11.glEnable(GL11.GL_CULL_FACE);
					}
				}

				if(renderPoint) {
					float alpha = MathHelper.clamp((float)size * 100.0F, 0.0F, 1.0F);
					alpha *= 1 - BobMathUtil.remap01_clamp((float)size, (float)transitionMinSize, (float)transitionMaxSize);
					GL11.glColor4f(metric.body.color[0], metric.body.color[1], metric.body.color[2], alpha * visibility);
					mc.renderEngine.bindTexture(planetTexture);

					tessellatorBuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
					tessellatorBuffer.pos(-1.0D, 100.0D, -1.0D).tex(0.0D, 0.0D).endVertex();
					tessellatorBuffer.pos(1.0D, 100.0D, -1.0D).tex(1.0D, 0.0D).endVertex();
					tessellatorBuffer.pos(1.0D, 100.0D, 1.0D).tex(1.0D, 1.0D).endVertex();
					tessellatorBuffer.pos(-1.0D, 100.0D, 1.0D).tex(0.0D, 1.0D).endVertex();
					tessellator.draw();
				}

			}
			GL11.glPopMatrix();
		}
	}

	protected void renderRings(float partialTicks, WorldClient world, Minecraft mc, float ringTilt, float[] ringColor, float ringSize, float visibility) {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder tessellatorBuffer = tessellator.getBuffer();

		GL11.glColor4f(ringColor[0], ringColor[1], ringColor[2], visibility);
		mc.renderEngine.bindTexture(ringTexture);

		double offset = -20.0D;

		tessellatorBuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
		tessellatorBuffer.pos(offset, -ringSize, -ringSize).tex(0.0D, 0.0D).endVertex();
		tessellatorBuffer.pos(offset, ringSize, -ringSize).tex(1.0D, 0.0D).endVertex();
		tessellatorBuffer.pos(offset, ringSize, ringSize).tex(1.0D, 1.0D).endVertex();
		tessellatorBuffer.pos(offset, -ringSize, ringSize).tex(0.0D, 1.0D).endVertex();
		tessellator.draw();
	}

	protected void renderDigamma(float partialTicks, WorldClient world, Minecraft mc, float solarAngle) {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder tessellatorBuffer = tessellator.getBuffer();

		GL11.glPushMatrix();
		{

			float var12 = 1F + world.rand.nextFloat() * 0.5F;
			double dist = 100D;

			if(ModEventHandlerClient.renderLodeStar) {
				GL11.glPushMatrix();
				GL11.glRotatef(-75.0F, 1.0F, 0.0F, 0.0F);
				GL11.glRotatef(10.0F, 0.0F, 1.0F, 0.0F);
				FMLClientHandler.instance().getClient().renderEngine.bindTexture(lodeStar); // genu-ine bona-fide ass whooping

				tessellatorBuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
				tessellatorBuffer.pos(-var12, dist, -var12).tex(0.0D, 0.0D).endVertex();
				tessellatorBuffer.pos(var12, dist, -var12).tex(0.0D, 1.0D).endVertex();
				tessellatorBuffer.pos(var12, dist, var12).tex(1.0D, 1.0D).endVertex();
				tessellatorBuffer.pos(-var12, dist, var12).tex(1.0D, 0.0D).endVertex();
				tessellator.draw();

				GL11.glPopMatrix();
			}

			OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE, GL11.GL_ZERO);

			float brightness = (float) Math.sin(solarAngle * Math.PI);
			brightness *= brightness;
			GL11.glColor4f(brightness, brightness, brightness, brightness);
			GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);
			GL11.glRotatef(solarAngle * 360.0F, 1.0F, 0.0F, 0.0F);
			GL11.glRotatef(140.0F, 1.0F, 0.0F, 0.0F);
			GL11.glRotatef(-40.0F, 0.0F, 0.0F, 1.0F);

			mc.renderEngine.bindTexture(digammaStar);

			float digamma = HbmLivingProps.getDigamma(Minecraft.getMinecraft().player);
			var12 = 1F * (1 + digamma * 0.25F);
			dist = 100D - digamma * 2.5;

			tessellatorBuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
			tessellatorBuffer.pos(-var12, dist, -var12).tex(0.0D, 0.0D).endVertex();
			tessellatorBuffer.pos(var12, dist, -var12).tex(0.0D, 1.0D).endVertex();
			tessellatorBuffer.pos(var12, dist, var12).tex(1.0D, 1.0D).endVertex();
			tessellatorBuffer.pos(-var12, dist, var12).tex(1.0D, 0.0D).endVertex();
			tessellator.draw();

		}
		GL11.glPopMatrix();
	}

	// Does anyone even play with 3D glasses anymore?
	protected float[] applyAnaglyph(float... colors) {
		float r = (colors[0] * 30.0F + colors[1] * 59.0F + colors[2] * 11.0F) / 100.0F;
		float g = (colors[0] * 30.0F + colors[1] * 70.0F) / 100.0F;
		float b = (colors[0] * 30.0F + colors[2] * 70.0F) / 100.0F;

		return new float[] { r, g, b };
	}

	// is just drawing a big cube with UVs prepared to draw a gradient
	private void renderSkyboxSide(Tessellator tessellator, int side) {
		BufferBuilder tessellatorBuffer = tessellator.getBuffer();
		double u = side % 3 / 3.0D;
		double v = side / 3 / 2.0D;
		tessellatorBuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
		tessellatorBuffer.pos(-100.0D, -100.0D, -100.0D).tex(u, v).endVertex();
		tessellatorBuffer.pos(-100.0D, -100.0D, 100.0D).tex(u, v + 0.5D).endVertex();
		tessellatorBuffer.pos(100.0D, -100.0D, 100.0D).tex(u + 0.3333333333333333D, v + 0.5D).endVertex();
		tessellatorBuffer.pos(100.0D, -100.0D, -100.0D).tex(u + 0.3333333333333333D, v).endVertex();
		tessellator.draw();
	}

	protected void renderSpecialEffects(float partialTicks, WorldClient world, Minecraft mc) {

	}

	protected void render3DModel(float partialTicks, WorldClient world, Minecraft mc) {

	}

	protected void renderStation(float partialTicks, WorldClient world, Minecraft mc, OrbitalStation station, float solarAngle) {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder tessellatorBuffer = tessellator.getBuffer();

		long seed = station.dX * 1024 + station.dZ;

		double ticks = (double)(System.currentTimeMillis() % (1600 * 50)) / 50;

		GL11.glPushMatrix();
		{

			GL11.glRotatef(solarAngle * -360.0F, 1.0F, 0.0F, 0.0F);
			GL11.glRotatef(-40.0F + (float)(seed % 800) * 0.1F - 5.0F, 1.0F, 0.0F, 0.0F);
			GL11.glRotatef((float)(seed % 50) * 0.1F - 20.0F, 0.0F, 1.0F, 0.0F);
			GL11.glRotatef((float)(seed % 80) * 0.1F - 2.5F, 0.0F, 0.0F, 1.0F);
			GL11.glRotated((ticks / 1600.0D) * -360.0D, 1.0F, 0.0F, 0.0F);

			GL11.glColor4f(0.8F, 1, 1, 1);

			mc.renderEngine.bindTexture(stationTexture);

			float size = 0.8F;

			tessellatorBuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
			tessellatorBuffer.pos(-size, 100.0, -size).tex(0.0D, 0.0D).endVertex();
			tessellatorBuffer.pos(size, 100.0, -size).tex(0.0D, 1.0D).endVertex();
			tessellatorBuffer.pos(size, 100.0, size).tex(1.0D, 1.0D).endVertex();
			tessellatorBuffer.pos(-size, 100.0, size).tex(1.0D, 0.0D).endVertex();
			tessellator.draw();

		}
		GL11.glPopMatrix();
	}

	public void renderSmoke(ResourceLocation loc1, long age) {
		GL11.glPushMatrix();
		GL11.glEnable(GL11.GL_BLEND);
		float f4 = 1.0F;
		float f5 = 0.5F;
		float f6 = 0.25F;
		float dark = 1f - Math.min(((float)(age) / (float)(100f * 0.35F)), 1f);
		GL11.glRotatef(180.0F - net.minecraft.client.Minecraft.getMinecraft().getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
		GL11.glRotatef(-net.minecraft.client.Minecraft.getMinecraft().getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);
		GL11.glColor4d(0.6*dark+0.0, 0.6*dark+0.0, 1*dark+0.0, 1);
		Tessellator tess = Tessellator.getInstance();
		BufferBuilder tessBuffer = tess.getBuffer();
		TextureManager tex = Minecraft.getMinecraft().getTextureManager();
		tessBuffer.begin(7, DefaultVertexFormats.POSITION_TEX_NORMAL);
		float tessNormal0X = 0.0F, tessNormal0Y = 1.0F, tessNormal0Z = 0.0F;
		tessBuffer.pos(0.0F - f5, 0.0F - f6, 0.0D).tex(1, 0).normal(tessNormal0X, tessNormal0Y, tessNormal0Z).endVertex();
		tessBuffer.pos(f4 - f5, 0.0F - f6, 0.0D).tex(0, 0).normal(tessNormal0X, tessNormal0Y, tessNormal0Z).endVertex();
		tessBuffer.pos(f4 - f5, f4 - f6, 0.0D).tex(0, 1).normal(tessNormal0X, tessNormal0Y, tessNormal0Z).endVertex();
		tessBuffer.pos(0.0F - f5, f4 - f6, 0.0D).tex(1, 1).normal(tessNormal0X, tessNormal0Y, tessNormal0Z).endVertex();
		tex.bindTexture(loc1);
		tess.draw();
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glPopMatrix();
	}

	public void renderGlow(ResourceLocation loc1, double x, double y, double z, float partialTicks) {
		GL11.glPushMatrix();
		GL11.glEnable(GL11.GL_BLEND);
		float f4 = 1.0F;
		float f5 = 0.5F;
		float f6 = 0.25F;
		GL11.glRotatef(180.0F - net.minecraft.client.Minecraft.getMinecraft().getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
		GL11.glRotatef(-net.minecraft.client.Minecraft.getMinecraft().getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);
		// double distant = 1d-(Math.min(6300000f, Math.max(0d, y-40000f))/6300000f);
		//  double sf = 1d-(Math.min(400000f, Math.max(0d, y-350000f))/400000f);
		//
		//double near = distant*(Math.min(40000f, Math.max(0d, y-35000f))/40000f)*Math.min(1d,Minecraft.getMinecraft().thePlayer.worldObj.getStarBrightness(partialTicks)+sf);
		double near = 0.51d*(Math.min(40000f, Math.max(0d, y-35000d))/40000d);
		//  System.out.println((1d-(Math.min(200d, Math.max(0d, y-2017d))/200f)));
		double entry = near*(1d-Minecraft.getMinecraft().player.world.getRainStrength(partialTicks))+(1d-(Math.min(200d, Math.max(0d, x-2017d))/200f));
		GL11.glColor4d(entry, entry, entry, entry);
		Tessellator tess = Tessellator.getInstance();
		BufferBuilder tessBuffer = tess.getBuffer();
		TextureManager tex = Minecraft.getMinecraft().getTextureManager();
		tessBuffer.begin(7, DefaultVertexFormats.POSITION_TEX_NORMAL);
		float tessNormal0X = 0.0F, tessNormal0Y = 1.0F, tessNormal0Z = 0.0F;
		tessBuffer.pos(0.0F - f5, 0.0F - f6, 0.0D).tex(1, 0).normal(tessNormal0X, tessNormal0Y, tessNormal0Z).endVertex();
		tessBuffer.pos(f4 - f5, 0.0F - f6, 0.0D).tex(0, 0).normal(tessNormal0X, tessNormal0Y, tessNormal0Z).endVertex();
		tessBuffer.pos(f4 - f5, f4 - f6, 0.0D).tex(0, 1).normal(tessNormal0X, tessNormal0Y, tessNormal0Z).endVertex();
		tessBuffer.pos(0.0F - f5, f4 - f6, 0.0D).tex(1, 1).normal(tessNormal0X, tessNormal0Y, tessNormal0Z).endVertex();
		tex.bindTexture(loc1);
		tess.draw();
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glPopMatrix();
	}


}
