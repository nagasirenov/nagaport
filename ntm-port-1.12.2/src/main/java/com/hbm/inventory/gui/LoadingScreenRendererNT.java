package com.hbm.inventory.gui;

import java.util.Random;

import org.lwjgl.opengl.GL11;

import com.hbm.handler.HTTPHandler;

import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraft.client.LoadingScreenRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.MinecraftError;

public class LoadingScreenRendererNT extends LoadingScreenRenderer {

	private String message = "";
	private Minecraft mc;
	private String currentlyDisplayedText = "";
	private long time = Minecraft.getSystemTime();
	private boolean doesProgress;
	private ScaledResolution resolution;
	private Framebuffer frameBuffer;
	public String tipOfTheDay = "Tip of the day: " + chooseTip();

	public LoadingScreenRendererNT(Minecraft mc) {
		super(mc);
		this.mc = mc;
		this.resolution = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
		this.frameBuffer = new Framebuffer(mc.displayWidth, mc.displayHeight, false);
		this.frameBuffer.setFramebufferFilter(9728);
	}
	
	private String chooseTip() {
		if(HTTPHandler.tipOfTheDay.isEmpty()) return "Explore! There's tons of free stuff to find.";
		return HTTPHandler.tipOfTheDay.get(new Random().nextInt(HTTPHandler.tipOfTheDay.size()));
	}

	@Override
	public void resetProgressAndMessage(String message) {
		this.doesProgress = false;
		this.func_73722_d(message);
	}

	@Override
	public void displayProgressMessage(String message) {
		this.doesProgress = true;
		this.func_73722_d(message);
	}

	@Override
	public void func_73722_d(String message) {
		this.currentlyDisplayedText = message;

		if(!this.mc.running) {
			if(!this.doesProgress) {
				throw new MinecraftError();
			}
		} else {
			GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
			GL11.glMatrixMode(GL11.GL_PROJECTION);
			GL11.glLoadIdentity();

			if(OpenGlHelper.isFramebufferEnabled()) {
				int scale = this.resolution.getScaleFactor();
				GL11.glOrtho(0.0D, (this.resolution.getScaledWidth() * scale), (this.resolution.getScaledHeight() * scale), 0.0D, 100.0D, 300.0D);
			} else {
				ScaledResolution scaledresolution = new ScaledResolution(this.mc, this.mc.displayWidth, this.mc.displayHeight);
				GL11.glOrtho(0.0D, scaledresolution.getScaledWidth_double(), scaledresolution.getScaledHeight_double(), 0.0D, 100.0D, 300.0D);
			}

			GL11.glMatrixMode(GL11.GL_MODELVIEW);
			GL11.glLoadIdentity();
			GL11.glTranslatef(0.0F, 0.0F, -200.0F);
		}
	}

	@Override
	public void resetProgresAndWorkingMessage(String message) {
		if(!this.mc.running) {
			if(!this.doesProgress) {
				throw new MinecraftError();
			}
		} else {
			this.time = 0L;
			this.message = message;
			this.setLoadingProgress(-1);
			this.time = 0L;
		}
	}

	@Override
	public void setLoadingProgress(int p_73718_1_) {
		if(!this.mc.running) {
			if(!this.doesProgress) {
				throw new MinecraftError();
			}
		} else {
			long time = Minecraft.getSystemTime();

			if(time - this.time >= 100L) {
				this.time = time;
				ScaledResolution scaledresolution = new ScaledResolution(this.mc, this.mc.displayWidth, this.mc.displayHeight);
				int scaleFactor = scaledresolution.getScaleFactor();
				int width = scaledresolution.getScaledWidth();
				int height = scaledresolution.getScaledHeight();

				if(OpenGlHelper.isFramebufferEnabled()) {
					this.frameBuffer.framebufferClear();
				} else {
					GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
				}

				this.frameBuffer.bindFramebuffer(false);
				GL11.glMatrixMode(GL11.GL_PROJECTION);
				GL11.glLoadIdentity();
				GL11.glOrtho(0.0D, scaledresolution.getScaledWidth_double(), scaledresolution.getScaledHeight_double(), 0.0D, 100.0D, 300.0D);
				GL11.glMatrixMode(GL11.GL_MODELVIEW);
				GL11.glLoadIdentity();
				GL11.glTranslatef(0.0F, 0.0F, -200.0F);

				if(!OpenGlHelper.isFramebufferEnabled()) {
					GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
				}

				if(!FMLClientHandler.instance().handleLoadingScreen(scaledresolution)) {
					Tessellator tessellator = Tessellator.getInstance();
					BufferBuilder tessellatorBuffer = tessellator.getBuffer();
					this.mc.getTextureManager().bindTexture(Gui.optionsBackground);
					float f = 32.0F;
					tessellatorBuffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
					int tessellatorColor0Packed = 4210752;
					tessellatorBuffer.pos(0.0D, height, 0.0D).tex(0.0D, height / f).color((tessellatorColor0Packed >> 16) & 255, (tessellatorColor0Packed >> 8) & 255, tessellatorColor0Packed & 255, 255).endVertex();
					tessellatorBuffer.pos(width, height, 0.0D).tex(width / f, height / f).color((tessellatorColor0Packed >> 16) & 255, (tessellatorColor0Packed >> 8) & 255, tessellatorColor0Packed & 255, 255).endVertex();
					tessellatorBuffer.pos(width, 0.0D, 0.0D).tex(width / f, 0.0D).color((tessellatorColor0Packed >> 16) & 255, (tessellatorColor0Packed >> 8) & 255, tessellatorColor0Packed & 255, 255).endVertex();
					tessellatorBuffer.pos(0.0D, 0.0D, 0.0D).tex(0.0D, 0.0D).color((tessellatorColor0Packed >> 16) & 255, (tessellatorColor0Packed >> 8) & 255, tessellatorColor0Packed & 255, 255).endVertex();
					tessellator.draw();

					if(p_73718_1_ >= 0) {
						byte b0 = 100;
						byte b1 = 2;
						int j1 = width / 2 - b0 / 2;
						int k1 = height / 2 + 16;
						GL11.glDisable(GL11.GL_TEXTURE_2D);
						tessellatorBuffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
						int tessellatorColor1Packed = 8421504;
						tessellatorBuffer.pos(j1, k1, 0.0D).color((tessellatorColor1Packed >> 16) & 255, (tessellatorColor1Packed >> 8) & 255, tessellatorColor1Packed & 255, 255).endVertex();
						tessellatorBuffer.pos(j1, k1 + b1, 0.0D).color((tessellatorColor1Packed >> 16) & 255, (tessellatorColor1Packed >> 8) & 255, tessellatorColor1Packed & 255, 255).endVertex();
						tessellatorBuffer.pos(j1 + b0, k1 + b1, 0.0D).color((tessellatorColor1Packed >> 16) & 255, (tessellatorColor1Packed >> 8) & 255, tessellatorColor1Packed & 255, 255).endVertex();
						tessellatorBuffer.pos(j1 + b0, k1, 0.0D).color((tessellatorColor1Packed >> 16) & 255, (tessellatorColor1Packed >> 8) & 255, tessellatorColor1Packed & 255, 255).endVertex();
						int tessellatorColor2Packed = 8454016;
						tessellatorBuffer.pos(j1, k1, 0.0D).color((tessellatorColor2Packed >> 16) & 255, (tessellatorColor2Packed >> 8) & 255, tessellatorColor2Packed & 255, 255).endVertex();
						tessellatorBuffer.pos(j1, (k1 + b1), 0.0D).color((tessellatorColor2Packed >> 16) & 255, (tessellatorColor2Packed >> 8) & 255, tessellatorColor2Packed & 255, 255).endVertex();
						tessellatorBuffer.pos(j1 + p_73718_1_, k1 + b1, 0.0D).color((tessellatorColor2Packed >> 16) & 255, (tessellatorColor2Packed >> 8) & 255, tessellatorColor2Packed & 255, 255).endVertex();
						tessellatorBuffer.pos(j1 + p_73718_1_, k1, 0.0D).color((tessellatorColor2Packed >> 16) & 255, (tessellatorColor2Packed >> 8) & 255, tessellatorColor2Packed & 255, 255).endVertex();
						tessellator.draw();
						GL11.glEnable(GL11.GL_TEXTURE_2D);
					}

					GL11.glEnable(GL11.GL_BLEND);
					OpenGlHelper.glBlendFunc(770, 771, 1, 0);
					this.mc.fontRenderer.drawStringWithShadow(this.currentlyDisplayedText, (width - this.mc.fontRenderer.getStringWidth(this.currentlyDisplayedText)) / 2, height / 2 - 4 - 16, 16777215);
					this.mc.fontRenderer.drawStringWithShadow(this.message, (width - this.mc.fontRenderer.getStringWidth(this.message)) / 2, height / 2 - 4 + 8, 16777215);
					
					String[] frags = this.tipOfTheDay.split("\\$");
					for(int i = 0; i < frags.length; i++) {
						String frag = frags[i];
						this.mc.fontRenderer.drawStringWithShadow(TextFormatting.YELLOW + frag, (width - this.mc.fontRenderer.getStringWidth(frag)) / 2, height / 2 - 4 - 60 + i * 10, 16777215);
					}
				}
				this.frameBuffer.unbindFramebuffer();

				if(OpenGlHelper.isFramebufferEnabled()) {
					this.frameBuffer.framebufferRender(width * scaleFactor, height * scaleFactor);
				}

				this.mc.func_147120_f();

				try { Thread.yield(); } catch(Exception exception) { }
			}
		}
	}

	@Override public void func_146586_a() { }
}
