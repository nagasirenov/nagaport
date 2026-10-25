package com.hbm.render.util;

import java.util.List;

import org.lwjgl.opengl.GL11;

import com.hbm.dim.CelestialBody;
import com.hbm.dim.SolarSystem;
import com.hbm.dim.SolarSystem.OrreryMetric;
import com.hbm.util.RenderUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class OrreryPronter {

	private static long lastTick;
	private static float lastPartial;

	private static List<OrreryMetric> metrics;

	public static void render(Minecraft mc, World world, float partialTicks) {
		Tessellator tessellator = Tessellator.getInstance();
		CelestialBody sun = CelestialBody.getStar(world);

		// Update metrics
		if(metrics == null || lastTick != world.getTotalWorldTime() || lastPartial != partialTicks) {
			metrics = SolarSystem.calculatePositionsOrrery(world, partialTicks);
			lastTick = world.getTotalWorldTime();
			lastPartial = partialTicks;
		}

		// Setup glow
		GL11.glPushAttrib(GL11.GL_LIGHTING_BIT);
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);

		GL11.glPushMatrix();
		{

			// Draw sun
			GL11.glPushMatrix();
			{

				mc.renderEngine.bindTexture(sun.texture);
				RenderUtil.renderBlock(tessellator, 0.375, 0.625);

			}
			GL11.glPopMatrix();

			// Scale so sun is 1x1x1
			double scale = 1 / Math.min(sun.radiusKm, SolarSystem.ORRERY_MAX_RADIUS);
			GL11.glScaled(scale, scale, scale);


			// Draw bodies
			for(OrreryMetric metric : metrics) {
				mc.renderEngine.bindTexture(metric.body.texture);

				GL11.glPushMatrix();
				{

					double bodyScale = metric.body.radiusKm;
					if(bodyScale < 2_000) bodyScale = (bodyScale / 63) * (bodyScale / 63) + 1_000;

					GL11.glTranslated(metric.position.x, metric.position.z, metric.position.y);
					GL11.glScaled(bodyScale, bodyScale, bodyScale);

					RenderUtil.renderBlock(tessellator);

				}
				GL11.glPopMatrix();

				GL11.glDisable(GL11.GL_TEXTURE_2D);
				GL11.glColor3f(metric.body.color[0], metric.body.color[1], metric.body.color[2]);

				BufferBuilder buffer = tessellator.getBuffer();
				buffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION);

				for(int i = 1; i < metric.orbitalPath.length; i++) {
					Vec3d from = metric.orbitalPath[i-1];
					Vec3d to = metric.orbitalPath[i];

					buffer.pos(from.x, from.z, from.y).endVertex();
					buffer.pos(to.x, to.z, to.y).endVertex();
				}

				Vec3d first = metric.orbitalPath[0];
				Vec3d last = metric.orbitalPath[metric.orbitalPath.length - 1];

				buffer.pos(last.x, last.z, last.y).endVertex();
				buffer.pos(first.x, first.z, first.y).endVertex();

				tessellator.draw();

				GL11.glEnable(GL11.GL_TEXTURE_2D);
				GL11.glColor3f(1, 1, 1);
			}

		}
		GL11.glPopMatrix();


		// Reset state
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glPopAttrib();
	}

}
