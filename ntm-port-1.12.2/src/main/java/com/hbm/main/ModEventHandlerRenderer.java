package com.hbm.main;

import com.hbm.blocks.ICustomBlockHighlight;
import com.hbm.config.ClientConfig;
import com.hbm.config.RadiationConfig;
import com.hbm.dim.WorldProviderCelestial;
import com.hbm.entity.missile.EntityRideableRocket;
import com.hbm.extprop.HbmLivingProps;
import com.hbm.handler.pollution.PollutionHandler.PollutionType;
import com.hbm.items.ModItems;
import com.hbm.items.armor.ItemModOxy;
import com.hbm.items.weapon.sedna.factory.XFactoryDrill;
import com.hbm.packet.PermaSyncHandler;
import com.hbm.render.model.ModelMan;
import com.hbm.util.ArmorUtil;
import com.hbm.render.util.RenderScreenOverlay;
import com.hbm.util.Clock;
import com.hbm.world.biome.CraterBiomeMarker;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.EntityViewRenderEvent.FogColors;
import net.minecraftforge.client.event.EntityViewRenderEvent.FogDensity;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.common.ForgeModContainer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;

public class ModEventHandlerRenderer {


	@SubscribeEvent
	public void onRenderTickPre(TickEvent.RenderTickEvent event) {
		Minecraft mc = Minecraft.getMinecraft();
		EntityPlayer player = mc.player;

		if(event.phase == Phase.START) {
			// Zoom out third person view when inside a rocket
			if(player != null && player.getRidingEntity() != null && player.getRidingEntity() instanceof EntityRideableRocket) {
				net.minecraftforge.fml.relauncher.ReflectionHelper.setPrivateValue(net.minecraft.client.renderer.EntityRenderer.class, mc.entityRenderer, 12.0F, "field_78490_B", "thirdPersonDistance");
			} else {
				net.minecraftforge.fml.relauncher.ReflectionHelper.setPrivateValue(net.minecraft.client.renderer.EntityRenderer.class, mc.entityRenderer, 4.0F, "field_78490_B", "thirdPersonDistance");
			}
		}
	}

	// Native SednaPlayerOwnership supplies lexical model visibility and armor layers.

	private boolean wasRiding;

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onRenderRidingPlayerPre(RenderPlayerEvent.Pre event) {
		wasRiding = event.getEntityPlayer().getRidingEntity() instanceof EntityRideableRocket;
		if(!wasRiding) return;

		GL11.glPushMatrix();

		GL11.glRotated(-event.getEntityPlayer().getRidingEntity().rotationPitch, 0, 0, 1);
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onRenderRidingPlayerPost(RenderPlayerEvent.Post event) {
		if(!wasRiding) return;

		GL11.glPopMatrix();
	}

	// Native held layers preserve aimed, akimbo and ModelMan item rendering.

	@SubscribeEvent
	public void onDrawHighlight(DrawBlockHighlightEvent event) {

		EntityPlayer player = MainRegistry.proxy.me();
		if(com.hbm.util.NativeHeldItem.getMainHandNullable(player) != null && com.hbm.util.NativeHeldItem.getMainHandNullable(player).getItem() == ModItems.gun_drill) {
			XFactoryDrill.drawBlockHighlight(player, com.hbm.util.NativeHeldItem.getMainHandNullable(player), event.getPartialTicks());
			event.setCanceled(true);
			return;
		}

		RayTraceResult mop = event.getTarget();

		if(mop != null && mop.typeOfHit == RayTraceResult.Type.BLOCK) {
			Block b = event.getPlayer().world.getBlockState(mop.getBlockPos()).getBlock();
			if(b instanceof ICustomBlockHighlight) {
				ICustomBlockHighlight cus = (ICustomBlockHighlight) b;

				if(cus.shouldDrawHighlight(event.getPlayer().world, mop.getBlockPos().getX(), mop.getBlockPos().getY(), mop.getBlockPos().getZ())) {
					cus.drawHighlight(event, event.getPlayer().world, mop.getBlockPos().getX(), mop.getBlockPos().getY(), mop.getBlockPos().getZ());
					event.setCanceled(true);
				}
			}
		}
	}

	float renderSoot = 0;

	@SubscribeEvent
	public void worldTick(WorldTickEvent event) {

		if(event.phase == WorldTickEvent.Phase.START && RadiationConfig.enableSootFog) {

			float step = 0.05F;
			float soot = PermaSyncHandler.pollution[PollutionType.SOOT.ordinal()];

			if(Math.abs(renderSoot - soot) < step) {
				renderSoot = soot;
			} else if(renderSoot < soot) {
				renderSoot += step;
			} else if(renderSoot > soot) {
				renderSoot -= step;
			}
		}
	}

	public static float lastFogDensity;

	@SubscribeEvent(priority = EventPriority.LOW)
	public void thickenFog(FogDensity event) {
		if(event.getEntity().world.provider instanceof WorldProviderCelestial) {
			WorldProviderCelestial provider = (WorldProviderCelestial) event.getEntity().world.provider;
			lastFogDensity = provider.fogDensity(event);

			if(lastFogDensity > 0) {
				if(GLContext.getCapabilities().GL_NV_fog_distance) {
					GL11.glFogi(34138, 34139);
				}
				GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_EXP);

				event.setDensity(lastFogDensity);
				event.setCanceled(true);

				return;
			}
		}

		float soot = (float) (renderSoot - RadiationConfig.sootFogThreshold);

		if(soot > 0 && RadiationConfig.enableSootFog) {

			float farPlaneDistance = (float) (Minecraft.getMinecraft().gameSettings.renderDistanceChunks * 16);
			float fogDist = farPlaneDistance / (1 + soot * 5F / (float) RadiationConfig.sootFogDivisor);
			GL11.glFogf(GL11.GL_FOG_START, 0);
			GL11.glFogf(GL11.GL_FOG_END, fogDist);

			if(GLContext.getCapabilities().GL_NV_fog_distance) {
				GL11.glFogi(34138, 34139);
			}

			event.setCanceled(true);
		}
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public void tintFog(FogColors event) {

		EntityPlayer player = MainRegistry.proxy.me();
		if(com.hbm.world.WorldBlockState.read(player.world, (int) Math.floor(player.posX), (int) Math.floor(player.posY), (int) Math.floor(player.posZ)).getMaterial() != Material.WATER) {
			Vec3d color = getFogBlendColor(player.world, (int) Math.floor(player.posX), (int) Math.floor(player.posZ), event.getRed(), event.getGreen(), event.getBlue(), event.getRenderPartialTicks());
			if(color != null) {
				event.setRed((float) color.x);
				event.setGreen((float) color.y);
				event.setBlue((float) color.z);
			}
		}

		float soot = (float) (renderSoot - RadiationConfig.sootFogThreshold);
		float sootColor = 0.15F;
		float sootReq = (float) RadiationConfig.sootFogDivisor;
		if(soot > 0 && RadiationConfig.enableSootFog) {
			float interp = Math.min(soot / sootReq, 1F);
			event.setRed(event.getRed() * (1 - interp) + sootColor * interp);
			event.setGreen(event.getGreen() * (1 - interp) + sootColor * interp);
			event.setBlue(event.getBlue() * (1 - interp) + sootColor * interp);
		}
	}

	// Native Sedna hand routing is owned by SednaHandHandler.

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onRenderHUD(RenderGameOverlayEvent.Pre event) {
		Tessellator tess = Tessellator.getInstance();

		//TODO: using ALL doesn't work as anticipated - still hides in F1. need a different event for this
		if(event.getType() == ElementType.ALL) {
			if(ClientConfig.BADGES_HUD.get()) RenderScreenOverlay.renderBadges(event.getResolution(), Minecraft.getMinecraft().ingameGUI);
		}

		if(event.getType() == ElementType.HOTBAR && (ModEventHandlerClient.shakeTimestamp + ModEventHandlerClient.shakeDuration - System.currentTimeMillis()) > 0 && ClientConfig.NUKE_HUD_SHAKE.get()) {
			double mult = (ModEventHandlerClient.shakeTimestamp + ModEventHandlerClient.shakeDuration - System.currentTimeMillis()) / (double) ModEventHandlerClient.shakeDuration * 2;
			double horizontal = MathHelper.clamp(Math.sin(System.currentTimeMillis() * 0.02), -0.7, 0.7) * 15;
			double vertical = MathHelper.clamp(Math.sin(System.currentTimeMillis() * 0.01 + 2), -0.7, 0.7) * 3;
			GL11.glTranslated(horizontal * mult, vertical * mult, 0);
		} else if(event.getType() == ElementType.AIR) {
			EntityPlayer player = Minecraft.getMinecraft().player;
			int width = event.getResolution().getScaledWidth();
			int height = event.getResolution().getScaledHeight();

			// If we're suffocating for a reason other than water, render the HUD bubbles
			int air = HbmLivingProps.getOxy(player);
			if(air < 100) {
				GuiIngame gui = Minecraft.getMinecraft().ingameGUI;

				GL11.glEnable(GL11.GL_BLEND);
				int left = width / 2 + 91;
				int top = height - GuiIngameForge.right_height;

				int full = MathHelper.ceil((double)(air - 2) * 10.0D / 100.0D);
				int partial = MathHelper.ceil((double)air * 10.0D / 100.0D) - full;

				for(int i = 0; i < full + partial; ++i) {
					gui.drawTexturedModalRect(left - i * 8 - 9, top, (i < full ? 16 : 25), 18, 9, 9);
				}
				GuiIngameForge.right_height += 10;

				GL11.glDisable(GL11.GL_BLEND);

				// Prevent regular bubbles rendering
				event.setCanceled(true);
			}

			ItemStack tankStack = ArmorUtil.getOxygenTank(player);
			if(tankStack != null) {
				ItemModOxy tank = (ItemModOxy)tankStack.getItem();

				float tot = (float)ItemModOxy.getFuel(tankStack) / (float)tank.getMaxFuel();

				GL11.glDisable(GL11.GL_TEXTURE_2D);
				int right = width / 2 + 91;
				int top = height - GuiIngameForge.right_height + 3;
				BufferBuilder tessBuffer = tess.getBuffer();
				tessBuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
				int tessColor0Packed = 64;
				tessBuffer.pos(right - 81.5, top - 0.5, 0).color(tessColor0Packed, tessColor0Packed, tessColor0Packed, 255).endVertex();
				tessBuffer.pos(right - 81.5, top + 4.5, 0).color(tessColor0Packed, tessColor0Packed, tessColor0Packed, 255).endVertex();
				tessBuffer.pos(right + 0.5, top + 4.5, 0).color(tessColor0Packed, tessColor0Packed, tessColor0Packed, 255).endVertex();
				tessBuffer.pos(right + 0.5, top - 0.5, 0).color(tessColor0Packed, tessColor0Packed, tessColor0Packed, 255).endVertex();
				int tessColor1R = (int) ((1F - tot) * 255);
				int tessColor1G = (int) (tot * 255);
				tessBuffer.pos(right - 81 * tot, top, 0).color(tessColor1R, tessColor1G, tessColor1G, 255).endVertex();
				tessBuffer.pos(right - 81 * tot, top + 4, 0).color(tessColor1R, tessColor1G, tessColor1G, 255).endVertex();
				tessBuffer.pos(right, top + 4, 0).color(tessColor1R, tessColor1G, tessColor1G, 255).endVertex();
				tessBuffer.pos(right, top, 0).color(tessColor1R, tessColor1G, tessColor1G, 255).endVertex();
				tess.draw();
				GL11.glEnable(GL11.GL_TEXTURE_2D);

				GuiIngameForge.right_height += 6;
				event.setCanceled(true);
			}
		}

	}

	private static boolean fogInit = false;
	private static int fogX;
	private static int fogZ;
	private static Vec3d fogRGBMultiplier;
	private static boolean doesBiomeApply = false;
	private static long fogTimer = 0;

	/** Same procedure as getting the blended sky color but for fog */
	public static Vec3d getFogBlendColor(World world, int playerX, int playerZ, float red, float green, float blue, double partialTicks) {

		long millis = Clock.get_ms() - fogTimer;
		if(playerX == fogX && playerZ == fogZ && fogInit && millis < 3000) return fogRGBMultiplier;

		fogInit = true;
		fogTimer = Clock.get_ms();
		GameSettings settings = Minecraft.getMinecraft().gameSettings;
		int[] ranges = ForgeModContainer.blendRanges;
		int distance = 0;

		if(settings.fancyGraphics && settings.renderDistanceChunks >= 0) {
			distance = ranges[Math.min(settings.renderDistanceChunks, ranges.length - 1)];
		}

		float r = 0F;
		float g = 0F;
		float b = 0F;

		int divider = 0;
		doesBiomeApply = false;

		for(int x = -distance; x <= distance; x++) {
			for(int z = -distance; z <= distance; z++) {
				Biome biome = world.getBiome(new BlockPos(playerX + x, 0, playerZ + z));
				Vec3d color = getBiomeFogColors(world, biome, red, green, blue, partialTicks);
				r += color.x;
				g += color.y;
				b += color.z;
				divider++;
			}
		}

		fogX = playerX;
		fogZ = playerZ;

		if(doesBiomeApply) {
			fogRGBMultiplier = new Vec3d(r / divider, g / divider, b / divider);
		} else {
			fogRGBMultiplier = null;
		}

		return fogRGBMultiplier;
	}

	/** Returns the current biome's fog color adjusted for brightness if in a crater, or the world's cached fog color if not */
	public static Vec3d getBiomeFogColors(World world, Biome biome, float r, float g, float b, double partialTicks) {

		if(biome instanceof CraterBiomeMarker) {
			int color = biome.getSkyColorByTemp(biome.getDefaultTemperature());
			r = ((color & 0xff0000) >> 16) / 255F;
			g = ((color & 0x00ff00) >> 8) / 255F;
			b = (color & 0x0000ff) / 255F;

			float celestialAngle = world.getCelestialAngle((float) partialTicks);
			float skyBrightness = MathHelper.clamp(MathHelper.cos(celestialAngle * (float) Math.PI * 2.0F) * 2.0F + 0.5F, 0F, 1F);
			r *= skyBrightness;
			g *= skyBrightness;
			b *= skyBrightness;

			doesBiomeApply = true;
		}

		return new Vec3d(r, g, b);
	}
}
