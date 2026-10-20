package com.hbm.dim.laythe;

import org.lwjgl.opengl.GL11;

import com.hbm.dim.SkyProviderCelestial;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.ResourceLocation;

public class SkyProviderLaytheSunset extends SkyProviderCelestial {

	public SkyProviderLaytheSunset() {
		super();
	}

	@Override
	protected void renderSunset(float partialTicks, WorldClient world, Minecraft mc, float solarAngle, float pressure, ResourceLocation surfaceTexture) {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder tessellatorBuffer = tessellator.getBuffer();

		float[] sunsetColor = world.provider.calcSunriseSunsetColors(world.getCelestialAngle(partialTicks), partialTicks);

		if(sunsetColor != null) {
			OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_CONSTANT_ALPHA, GL11.GL_ONE, GL11.GL_ZERO); // The magic sauce

			float[] anaglyphColor = mc.gameSettings.anaglyph ? applyAnaglyph(sunsetColor) : sunsetColor;
			byte segments = 16;

			GL11.glDisable(GL11.GL_TEXTURE_2D);
			GL11.glShadeModel(GL11.GL_SMOOTH);

			GL11.glPushMatrix();
			{

				GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
				GL11.glRotatef(MathHelper.sin(world.getCelestialAngleRadians(partialTicks)) < 0.0F ? 180.0F : 0.0F, 0.0F, 0.0F, 1.0F);
				GL11.glRotatef(90.0F, 0.0F, 0.0F, 1.0F);

				tessellatorBuffer.begin(6, DefaultVertexFormats.POSITION_COLOR);
				int tessellatorColor0Red = Math.max(0, Math.min(255, (int) ((anaglyphColor[0]) * 255.0F)));
				int tessellatorColor0Green = Math.max(0, Math.min(255, (int) ((anaglyphColor[1]) * 255.0F)));
				int tessellatorColor0Blue = Math.max(0, Math.min(255, (int) ((anaglyphColor[2]) * 255.0F)));
				int tessellatorColor0Alpha = Math.max(0, Math.min(255, (int) ((sunsetColor[3]) * 255.0F)));
				tessellatorBuffer.pos(0.0D, 150.0D, 0.0D).color(tessellatorColor0Red, tessellatorColor0Green, tessellatorColor0Blue, tessellatorColor0Alpha).endVertex();
				int tessellatorColor1Red = Math.max(0, Math.min(255, (int) ((sunsetColor[0]) * 255.0F)));
				int tessellatorColor1Green = Math.max(0, Math.min(255, (int) ((sunsetColor[1]) * 255.0F)));
				int tessellatorColor1Blue = Math.max(0, Math.min(255, (int) ((sunsetColor[2]) * 255.0F)));
				int tessellatorColor1Alpha = Math.max(0, Math.min(255, (int) ((0.0F) * 255.0F)));

				for(int j = 0; j <= segments; ++j) {
					float angle = (float)j * (float)Math.PI * 2.0F / (float)segments;
					float sinAngle = MathHelper.sin(angle);
					float cosAngle = MathHelper.cos(angle);
					tessellatorBuffer.pos((double)(sinAngle * 160.0F), (double)(cosAngle * 160.0F), (double)(-cosAngle * 90.0F * sunsetColor[3])).color(tessellatorColor1Red, tessellatorColor1Green, tessellatorColor1Blue, tessellatorColor1Alpha).endVertex();
				}

				tessellator.draw();

			}
			GL11.glPopMatrix();

			GL11.glPushMatrix();
			{

				GL11.glRotatef(135.0F, 1.0F, 0.0F, 0.0F);
				GL11.glTranslatef(0, -60, 0);

				tessellatorBuffer.begin(6, DefaultVertexFormats.POSITION_COLOR);
				int tessellatorColor2Red = Math.max(0, Math.min(255, (int) ((anaglyphColor[0]) * 255.0F)));
				int tessellatorColor2Green = Math.max(0, Math.min(255, (int) ((anaglyphColor[1]) * 255.0F)));
				int tessellatorColor2Blue = Math.max(0, Math.min(255, (int) ((anaglyphColor[2]) * 255.0F)));
				int tessellatorColor2Alpha = Math.max(0, Math.min(255, (int) ((sunsetColor[3]) * 255.0F)));
				tessellatorBuffer.pos(0.0D, 100.0D, 0.0D).color(tessellatorColor2Red, tessellatorColor2Green, tessellatorColor2Blue, tessellatorColor2Alpha).endVertex();
				int tessellatorColor3Red = Math.max(0, Math.min(255, (int) ((sunsetColor[0]) * 255.0F)));
				int tessellatorColor3Green = Math.max(0, Math.min(255, (int) ((sunsetColor[1]) * 255.0F)));
				int tessellatorColor3Blue = Math.max(0, Math.min(255, (int) ((sunsetColor[2]) * 255.0F)));
				int tessellatorColor3Alpha = Math.max(0, Math.min(255, (int) ((0.0F) * 255.0F)));

				for(int j = 0; j <= segments; ++j) {
					float angle = (float)j * (float)Math.PI * 2.0F / (float)segments;
					float sinAngle = MathHelper.sin(angle);
					float cosAngle = MathHelper.cos(angle);

					tessellatorBuffer.pos((double)(sinAngle * 100.0F), (double)(cosAngle * 100.0F), (double)(-cosAngle * 90.0F)).color(tessellatorColor3Red, tessellatorColor3Green, tessellatorColor3Blue, tessellatorColor3Alpha).endVertex();
				}

				tessellator.draw();

			}
			GL11.glPopMatrix();

			GL11.glPushMatrix();
			{

				GL11.glRotatef(135.0F, 1.0F, 0.0F, 0.0F);
				GL11.glTranslatef(0, -30, 0);

				tessellatorBuffer.begin(6, DefaultVertexFormats.POSITION_COLOR);
				int tessellatorColor4Red = Math.max(0, Math.min(255, (int) ((anaglyphColor[0]) * 255.0F)));
				int tessellatorColor4Green = Math.max(0, Math.min(255, (int) ((anaglyphColor[1]) * 255.0F)));
				int tessellatorColor4Blue = Math.max(0, Math.min(255, (int) ((anaglyphColor[2]) * 255.0F)));
				int tessellatorColor4Alpha = Math.max(0, Math.min(255, (int) ((sunsetColor[3]) * 255.0F)));
				tessellatorBuffer.pos(0.0D, 80.0D, 0.0D).color(tessellatorColor4Red, tessellatorColor4Green, tessellatorColor4Blue, tessellatorColor4Alpha).endVertex();
				int tessellatorColor5Red = Math.max(0, Math.min(255, (int) ((sunsetColor[0]) * 255.0F)));
				int tessellatorColor5Green = Math.max(0, Math.min(255, (int) ((sunsetColor[1] * 0.2F) * 255.0F)));
				int tessellatorColor5Blue = Math.max(0, Math.min(255, (int) ((sunsetColor[2]) * 255.0F)));
				int tessellatorColor5Alpha = Math.max(0, Math.min(255, (int) ((0.0F) * 255.0F)));

				for(int j = 0; j <= segments; ++j) {
					float angle = (float)j * (float)Math.PI * 2.0F / (float)segments;
					float sinAngle = MathHelper.sin(angle);
					float cosAngle = MathHelper.cos(angle);

					tessellatorBuffer.pos((double)(sinAngle * 100.0F), (double)(cosAngle * 100.0F), (double)(-cosAngle * 90.0F)).color(tessellatorColor5Red, tessellatorColor5Green, tessellatorColor5Blue, tessellatorColor5Alpha).endVertex();
				}

				tessellator.draw();

			}
			GL11.glPopMatrix();

			GL11.glShadeModel(GL11.GL_FLAT);
			GL11.glEnable(GL11.GL_TEXTURE_2D);
		}
	}

}
