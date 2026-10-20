package com.hbm.render.util;

import org.lwjgl.opengl.GL11;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL12;

import com.hbm.config.ClientConfig;
import com.hbm.config.GeneralConfig;
import com.hbm.config.MobConfig;
import com.hbm.extprop.HbmPlayerProps;
import com.hbm.handler.ImpactWorldHandler;
import com.hbm.interfaces.Spaghetti;
import com.hbm.interfaces.Untested;
import com.hbm.items.weapon.sedna.Crosshair;
import com.hbm.items.weapon.sedna.impl.ItemGunStinger;
import com.hbm.lib.RefStrings;
import com.hbm.util.Clock;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.GuiIngameForge;

public class RenderScreenOverlay {

	public static final ResourceLocation misc = new ResourceLocation(RefStrings.MODID + ":textures/misc/overlay_misc.png");
	
	private static long lastSurvey;
	private static float prevResult;
	private static float lastResult;
	
	private static float fadeOut = 0F;
	
	public static void renderRadCounter(ScaledResolution resolution, float in, Gui gui) {
		GL11.glPushMatrix();

		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.disableAlpha();

		float radiation = 0;

		radiation = lastResult - prevResult;

		if(Clock.get_ms() >= lastSurvey + 1000) {
			lastSurvey = Clock.get_ms();
			prevResult = lastResult;
			lastResult = in;
		}

		int length = 74;
		int maxRad = 1000;

		int bar = getScaled(in, maxRad, 74);

		int posX = 16 + ClientConfig.GEIGER_OFFSET_HORIZONTAL.get();
		int posY = resolution.getScaledHeight() - 20 - ClientConfig.GEIGER_OFFSET_VERTICAL.get();

		Minecraft.getMinecraft().renderEngine.bindTexture(misc);
		gui.drawTexturedModalRect(posX, posY, 0, 0, 94, 18);
		gui.drawTexturedModalRect(posX + 1, posY + 1, 1, 19, bar, 16);

		if(radiation >= 25) gui.drawTexturedModalRect(posX + length + 2, posY - 18, 36, 36, 18, 18);
		else if(radiation >= 10) gui.drawTexturedModalRect(posX + length + 2, posY - 18, 18, 36, 18, 18);
		else if(radiation >= 2.5) gui.drawTexturedModalRect(posX + length + 2, posY - 18, 0, 36, 18, 18);

		if(radiation > 1000) Minecraft.getMinecraft().fontRenderer.drawString(">1000 RAD/s", posX, posY - 8, 0xFF0000);
		else if(radiation >= 1) Minecraft.getMinecraft().fontRenderer.drawString(((int) Math.round(radiation)) + " RAD/s", posX, posY - 8, 0xFF0000);
		else if(radiation > 0) Minecraft.getMinecraft().fontRenderer.drawString("<1 RAD/s", posX, posY - 8, 0xFF0000);

		GlStateManager.enableDepth();
		GlStateManager.depthMask(true);
		GL11.glPopMatrix();
		Minecraft.getMinecraft().renderEngine.bindTexture(Gui.ICONS);
	}
	
	private static int getScaled(double cur, double max, double scale) {
		return (int) Math.min(cur / max * scale, scale);
	}

	
	public static void renderCustomCrosshairs(ScaledResolution resolution, Gui gui, Crosshair cross) {

		if(cross == Crosshair.NONE) {
			Minecraft.getMinecraft().renderEngine.bindTexture(Gui.ICONS);
			return;
		}

		int size = cross.size;

		GL11.glPushMatrix();
		Minecraft.getMinecraft().renderEngine.bindTexture(misc);
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GL11.GL_ONE_MINUS_DST_COLOR, GL11.GL_ONE_MINUS_SRC_COLOR, 1, 0);
		gui.drawTexturedModalRect(resolution.getScaledWidth() / 2 - (size / 2), resolution.getScaledHeight() / 2 - (size / 2), cross.x, cross.y, size, size);
		GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
		GlStateManager.disableBlend();
        GL11.glPopMatrix();
		Minecraft.getMinecraft().renderEngine.bindTexture(Gui.ICONS);
	}
	
	public static void renderStingerLockon(ScaledResolution resolution, Gui gui) {

		int progress = (int) (ItemGunStinger.lockon * 28);
		
		GL11.glPushMatrix();
		Minecraft.getMinecraft().renderEngine.bindTexture(misc);
		GlStateManager.disableBlend();
		gui.drawTexturedModalRect(resolution.getScaledWidth() / 2 - 15, resolution.getScaledHeight() / 2  + 18, 146, 18, 30, 10);
		gui.drawTexturedModalRect(resolution.getScaledWidth() / 2 - 14, resolution.getScaledHeight() / 2  + 19, 147, 29, progress, 8);
		GL11.glPopMatrix();
		Minecraft.getMinecraft().renderEngine.bindTexture(Gui.ICONS);
	}
	
	public static void renderAmmo(ScaledResolution resolution, Gui gui, ItemStack ammo, int count, int max, int dura, boolean renderCount) {
		
		GL11.glPushMatrix();
		
		Minecraft mc = Minecraft.getMinecraft();
		
		int pX = resolution.getScaledWidth() / 2 + 62 + 36;
		int pZ = resolution.getScaledHeight() - 21;
		
		Minecraft.getMinecraft().renderEngine.bindTexture(misc);
		gui.drawTexturedModalRect(pX, pZ + 16, 94, 0, 52, 3);
		gui.drawTexturedModalRect(pX + 1, pZ + 16, 95, 3, 50 - dura, 3);
		
		String cap = max == -1 ? ("∞") : ("" + max);
		
		if(renderCount)
			Minecraft.getMinecraft().fontRenderer.drawString(count + " / " + cap, pX + 16, pZ + 6, 0xFFFFFF);
		
		GlStateManager.disableBlend();
		GlStateManager.enableRescaleNormal();
		RenderHelper.enableGUIStandardItemLighting();
		mc.getRenderItem().renderItemAndEffectIntoGUI(ammo, pX, pZ);
		RenderHelper.disableStandardItemLighting();
		GlStateManager.disableRescaleNormal();
		
		GL11.glPopMatrix();
		Minecraft.getMinecraft().renderEngine.bindTexture(Gui.ICONS);
	}
	
	public static void renderAmmoAlt(ScaledResolution resolution, Gui gui, ItemStack ammo, int count) {
		
		GL11.glPushMatrix();
		
		Minecraft mc = Minecraft.getMinecraft();
		
		int pX = resolution.getScaledWidth() / 2 + 62 + 36 + 18;
		int pZ = resolution.getScaledHeight() - 21 - 16;
		
		Minecraft.getMinecraft().renderEngine.bindTexture(misc);
		
		Minecraft.getMinecraft().fontRenderer.drawString(count + "x", pX + 16, pZ + 6, 0xFFFFFF);

		GlStateManager.disableBlend();
		GlStateManager.enableRescaleNormal();
		RenderHelper.enableGUIStandardItemLighting();
			mc.getRenderItem().renderItemAndEffectIntoGUI(ammo, pX, pZ);
		RenderHelper.disableStandardItemLighting();
		GlStateManager.disableRescaleNormal();
		
		GL11.glPopMatrix();
		Minecraft.getMinecraft().renderEngine.bindTexture(Gui.ICONS);
	}
	
	@Spaghetti ("like a fella once said, aint that a kick in the head")
	public static void renderDashBar(ScaledResolution resolution, Gui gui, HbmPlayerProps props) {

		GL11.glPushMatrix();
		
		GlStateManager.enableBlend();
		GlStateManager.disableDepth();
		GlStateManager.depthMask(false);
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.disableAlpha();
		
		Minecraft mc = Minecraft.getMinecraft();
		
		int width = 30;
		int posX = 16;
		int posY = resolution.getScaledHeight() - 40 - 2;
		
		mc.renderEngine.bindTexture(misc);
		
		gui.drawTexturedModalRect(posX-10, posY, 107, 18, 7, 10); 
		
		int stamina = props.getStamina();
		int dashes = props.getDashCount();
		int rows = dashes / 3;
		int finalColumns = dashes % 3;
		
		for(int y = 0; y < rows; y++) {
			for(int x = 0; x < 3; x++) {
				if(y == rows && x > finalColumns) 
					break;
				gui.drawTexturedModalRect(posX + (width+2)*x, posY - 12*y, 76, 48, width, 10);
				int staminaDiv = stamina / 30;
				int staminaMod = stamina % 30;
				int barID = (3*y)+x;
				int barStatus = 1; //0 = red, 1 = normal, 2 = greyed, 3 = dashed, 4 = ascended
				int barSize = width;
				if(staminaDiv < barID) {
					barStatus = 3;
				} else if(staminaDiv == barID) {
					barStatus = 2;
					barSize = (int) ((float) (stamina % 30) * (width/30F));
					if(barID == 0) barStatus = 0;
				}
				gui.drawTexturedModalRect(posX + (width+2)*x, posY - 12*y, 76, 18+(10*barStatus), barSize, 10);
				
				if(staminaDiv == barID && staminaMod >= 27) {
					fadeOut = 1F;
				}
				if(fadeOut > 0 && staminaDiv-1 == barID) {
					GlStateManager.color(1F, 1F, 1F, fadeOut);
					int bar = barID;
					if(stamina % 30 >= 25) bar++;
					if(bar / 3 != y) y++;
					
					bar = bar % 3;
					gui.drawTexturedModalRect(posX + (width + 2) * bar, posY - 12 * y, 76, 58, width, 10);
					fadeOut -= 0.04F;
					GlStateManager.color(1F, 1F, 1F, 1F);
				}
			}
		}
		
		
		GlStateManager.enableDepth();
		GlStateManager.depthMask(true);
		GL11.glPopMatrix();
		mc.renderEngine.bindTexture(Gui.ICONS);
	}
	
	//call in post health bar rendering event
	public static void renderShieldBar(ScaledResolution resolution, Gui gui) {
		
		EntityPlayer player = Minecraft.getMinecraft().player;
		HbmPlayerProps props = HbmPlayerProps.getData(player);
		FontRenderer font = Minecraft.getMinecraft().fontRenderer;

		int width = resolution.getScaledWidth();
		int height = resolution.getScaledHeight();
		int left = width / 2 - 91;
		int top = height - GuiIngameForge.left_height;
 
		Minecraft.getMinecraft().renderEngine.bindTexture(misc);
		gui.drawTexturedModalRect(left, top, 146, 0, 81, 9);
		int i = (int) Math.ceil(props.shield * 79 / props.getEffectiveMaxShield());
		gui.drawTexturedModalRect(left + 1, top, 147, 9, i, 9);
		
		String label = "" + ((int) (props.shield * 10F)) / 10D;
		font.drawString(label, left + 41 - font.getStringWidth(label) / 2, top + 1, 0x0000);
		font.drawString(label, left + 39 - font.getStringWidth(label) / 2, top + 1, 0x0000);
		font.drawString(label, left + 40 - font.getStringWidth(label) / 2, top, 0x0000);
		font.drawString(label, left + 40 - font.getStringWidth(label) / 2, top + 2, 0x0000);
		font.drawString(label, left + 40 - font.getStringWidth(label) / 2, top + 1, 0xFFFF80);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		
		GuiIngameForge.left_height += 10;
		Minecraft.getMinecraft().renderEngine.bindTexture(Gui.ICONS);
	}

	
	@Untested
	public static void renderScope(ScaledResolution res, ResourceLocation tex) {

		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.disableAlpha();
		
		Minecraft.getMinecraft().renderEngine.bindTexture(tex);
		Tessellator tess = Tessellator.getInstance();
		BufferBuilder buffer = tess.getBuffer();

		double w = res.getScaledWidth();
		double h = res.getScaledHeight();
		
		double smallest = Math.min(w, h);
		double divisor = smallest / (9D / 16D);
		smallest = 9D / 16D;
		double largest = Math.max(w, h) / divisor;

		double hMin = h < w ? 0.5 - smallest / 2D : 0.5 - largest / 2D;
		double hMax = h < w ? 0.5 + smallest / 2D : 0.5 + largest / 2D;
		double wMin = w < h ? 0.5 - smallest / 2D : 0.5 - largest / 2D;
		double wMax = w < h ? 0.5 + smallest / 2D : 0.5 + largest / 2D;
		
		double depth = -300D;
		
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		
		buffer.pos(0, h, depth).tex(wMin, hMax).endVertex();
		buffer.pos(w, h, depth).tex(wMax, hMax).endVertex();
		buffer.pos(w, 0, depth).tex(wMax, hMin).endVertex();
		buffer.pos(0, 0, depth).tex(wMin, hMin).endVertex();
		tess.draw();
		
		GlStateManager.depthMask(true);
		GlStateManager.enableDepth();
		GlStateManager.enableAlpha();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
	}
	
	public static void renderBadges(ScaledResolution res, Gui gui) {
		
		GL11.glPushMatrix();
		
		Minecraft.getMinecraft().entityRenderer.setupOverlayRendering();
		
		GlStateManager.enableBlend();
		GlStateManager.disableDepth();
		GlStateManager.depthMask(false);
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.disableAlpha();

		Minecraft.getMinecraft().renderEngine.bindTexture(misc);
		
		int offsetX = 2;
		int offsetY = 2;
		int width = 26;
		
		boolean true528 = GeneralConfig.true528();
		boolean trueExp = GeneralConfig.trueExp();
		boolean trueRam = MobConfig.trueRam();
		boolean true328 = true528 && trueExp && trueRam;
		
		if(true528) { gui.drawTexturedModalRect(offsetX, offsetY, 0, 218, 24, 8); offsetX += width; }
		if(trueExp) { gui.drawTexturedModalRect(offsetX, offsetY, 0, 226, 24, 8); offsetX += width; }
		if(trueRam) { gui.drawTexturedModalRect(offsetX, offsetY, 0, 234, 24, 8); offsetX += width; }
		if(true328) { gui.drawTexturedModalRect(offsetX, offsetY, 0, 242, 24, 8); offsetX += width; }
		
		Minecraft.getMinecraft().renderEngine.bindTexture(Gui.ICONS);

		GlStateManager.depthMask(true);
		GlStateManager.enableDepth();
		GlStateManager.enableAlpha();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glPopMatrix();
	}
	

	public static void renderCountdown(ScaledResolution resolution, Gui gui, World world) {
		GL11.glPushMatrix();
		GL11.glTranslatef(0, 0, 0.0F);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.disableBlend();

		int left = 5;
		int top = 5;

		final long ticks = ImpactWorldHandler.getTimeForClient(world);
		final int seconds = (int)(ticks/20) % 60;
		final int minutes = (int) Math.floor(ticks / (60 * 20)) % 60; 
		final int hours = (int) Math.floor(ticks / (60 * 60 * 20));
		
		int color = 0x000000;
		if(hours >= 10) {
			color = 0x55FF55;
		}
		if(hours < 10) {
			color = 0xFFFFFF;
		}
		if(hours < 5) {
			color = 0xFFFF55;
		}
		if(hours < 1) {
			color = 0xFFAA00;
		}
		if(minutes < 20) {
			color = 0xFF5555;
		}
		if(minutes < 2) {
			color = 0xAA0000;
		}
		
		if(minutes < 10) {
			if(seconds < 10) {
				Minecraft.getMinecraft().fontRenderer.drawString("Remaining time to impact: " + hours + ":0" + minutes + ":0" + seconds, left, top, color);	
			} else {
				Minecraft.getMinecraft().fontRenderer.drawString("Remaining time to impact: " + hours + ":0" + minutes + ":" + seconds, left, top, color);
			}
		} else {
			if(seconds < 10) {
				Minecraft.getMinecraft().fontRenderer.drawString("Remaining time to impact: " + hours + ":" + minutes + ":0" + seconds, left, top, color);	
			} else {
				Minecraft.getMinecraft().fontRenderer.drawString("Remaining time to impact: " + hours + ":" + minutes + ":" + seconds, left, top, color);
			}
		}

		GlStateManager.enableBlend();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glPopMatrix();
	}	
	
	public static void renderTaintBar(ScaledResolution resolution, Gui gui) {
		int width = resolution.getScaledWidth();
		int height = resolution.getScaledHeight();
		int left = width / 2 - 92;
		int top = height - 41;
 
		Minecraft.getMinecraft().renderEngine.bindTexture(misc);
		gui.drawTexturedModalRect(left, top, 146, 18, 81, 12);
		
	
		Minecraft.getMinecraft().renderEngine.bindTexture(Gui.ICONS);
	}

	public static void renderFlashbangOverlay(ScaledResolution resolution) {
		ResourceLocation tex = new ResourceLocation (RefStrings.MODID + ":textures/misc/overlay_flashbang.png");

		Minecraft.getMinecraft().getTextureManager().bindTexture(tex);

		GlStateManager.enableBlend();
		GlStateManager.disableDepth();
		GlStateManager.depthMask(false);
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.disableAlpha();

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		buffer.pos(0.0D, (double) resolution.getScaledHeight(), -90.0D).tex(0.0D, 1.0D).endVertex();
		buffer.pos((double) resolution.getScaledWidth(), (double) resolution.getScaledHeight(), -90.0D).tex(1.0D, 1.0D).endVertex();
		buffer.pos((double) resolution.getScaledWidth(), 0.0D, -90.0D).tex(1.0D, 0.0D).endVertex();
		buffer.pos(0.0D, 0.0D, -90.0D).tex(0.0D, 0.0D).endVertex();
		tessellator.draw();
		GlStateManager.depthMask(true);
		GlStateManager.enableDepth();
		GlStateManager.enableAlpha();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
	}

}