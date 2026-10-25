package com.hbm.render.entity.effect;

import java.util.Random;

import org.lwjgl.opengl.GL11;

import com.hbm.entity.effect.EntityRagingVortex;
import com.hbm.entity.effect.EntityVortex;
import com.hbm.lib.RefStrings;
import com.hbm.entity.effect.EntityBlackHole;

import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import com.hbm.render.mesh.ObjMeshModel;
import com.hbm.render.mesh.MeshModel;

public class RenderBlackHole extends Render<Entity> {

	protected static final ResourceLocation objTesterModelRL = new ResourceLocation(RefStrings.MODID, "models/Sphere.obj");
	protected MeshModel blastModel;
	protected ResourceLocation hole = new ResourceLocation(RefStrings.MODID, "textures/models/BlackHole.png");
	protected ResourceLocation swirl = new ResourceLocation(RefStrings.MODID, "textures/entity/bhole.png");
	protected ResourceLocation disc = new ResourceLocation(RefStrings.MODID, "textures/entity/bholeDisc.png");

	public RenderBlackHole(RenderManager renderManager) {
		super(renderManager);
		blastModel = new ObjMeshModel(objTesterModelRL);
	}

	@Override
	public void doRender(Entity entity, double p_76986_2_, double p_76986_4_, double p_76986_6_, float p_76986_8_, float interp) {

		GL11.glPushMatrix();
		GL11.glTranslatef((float) p_76986_2_, (float) p_76986_4_, (float) p_76986_6_);
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_CULL_FACE);

		float size = ((EntityBlackHole) entity).getBlackHoleSize();

		GL11.glScalef(size, size, size);

		bindTexture(hole);
		blastModel.renderAll();

		if(entity instanceof EntityVortex) {
			renderSwirl(entity, interp);

		} else if(entity instanceof EntityRagingVortex) {
			renderSwirl(entity, interp);
			renderJets(entity, interp);

		} else {
			renderDisc(entity, interp);
			renderJets(entity, interp);
		}

		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glEnable(GL11.GL_LIGHTING);

		GL11.glPopMatrix();
	}

	protected ResourceLocation discTex() {
		return this.disc;
	}

	protected void renderDisc(Entity entity, float interp) {

		float glow = 0.75F;

		bindTexture(discTex());

		GL11.glPushMatrix();
		GL11.glRotatef(entity.getEntityId() % 90 - 45, 1, 0, 0);
		GL11.glRotatef(entity.getEntityId() % 360, 0, 1, 0);
		GL11.glShadeModel(GL11.GL_SMOOTH);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		GL11.glDepthMask(false);
		GL11.glAlphaFunc(GL11.GL_GEQUAL, 0.0F);
		OpenGlHelper.glBlendFunc(770, 771, 1, 0);

		Tessellator tess = Tessellator.getInstance();
		BufferBuilder buffer = tess.getBuffer();

		int count = 16;

		Vec3d vec = new Vec3d(1, 0, 0);

		for(int k = 0; k < steps(); k++) {

			GL11.glPushMatrix();
			GL11.glRotatef((entity.ticksExisted + interp % 360) * -((float)Math.pow(k + 1, 1.25)), 0, 1, 0);
			OpenGlHelper.glBlendFunc(770, 771, 1, 0);
			double s = 3 - k * 0.175D;

			for(int j = 0; j < 2; j++) {

				buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
				for(int i = 0; i < count; i++) {

					int edgeColor = j == 0 ? colorFromIteration(k, 1F) : color(1F, 1F, 1F, glow);
					addTexColorVertex(buffer, vec.x * s, 0, vec.z * s, 0.5 + vec.x * 0.25, 0.5 + vec.z * 0.25, edgeColor);
					int fadeColor = colorFromIteration(k, 0F);
					addTexColorVertex(buffer, vec.x * s * 2, 0, vec.z * s * 2, 0.5 + vec.x * 0.5, 0.5 + vec.z * 0.5, fadeColor);

					vec = vec.rotateYaw((float)(Math.PI * 2 / count));
					addTexColorVertex(buffer, vec.x * s * 2, 0, vec.z * s * 2, 0.5 + vec.x * 0.5, 0.5 + vec.z * 0.5, fadeColor);
					addTexColorVertex(buffer, vec.x * s, 0, vec.z * s, 0.5 + vec.x * 0.25, 0.5 + vec.z * 0.25, edgeColor);
				}
				tess.draw();

				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
			}

			GL11.glPopMatrix();
		}


		GL11.glShadeModel(GL11.GL_FLAT);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
		GL11.glDepthMask(true);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glPopMatrix();
	}

	protected int steps() {
		return 15;
	}

	protected int colorFromIteration(int iteration, float alpha) {
		if(iteration < 5)
			return color(1.0F, 0.125F + iteration * (1F / 10F), 0.0F, alpha);
		if(iteration == 5)
			return color(1.0F, 1.0F, 0.0F, alpha);

		int i = iteration - 6;
		return color(1.0F - i * (1F / 9F), 1F - i * (1F / 9F), i * (1F / 5F), alpha);
	}

	protected void renderSwirl(Entity entity, float interp) {

		float glow = 0.75F;

		if(entity instanceof EntityRagingVortex)
			glow = 0.25F;

		bindTexture(swirl);

		GL11.glPushMatrix();
		GL11.glRotatef(entity.getEntityId() % 90 - 45, 1, 0, 0);
		GL11.glRotatef(entity.getEntityId() % 360, 0, 1, 0);
		GL11.glRotatef((entity.ticksExisted + interp % 360) * -5, 0, 1, 0);
		GL11.glShadeModel(GL11.GL_SMOOTH);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		GL11.glDepthMask(false);
		GL11.glAlphaFunc(GL11.GL_GEQUAL, 0.0F);
		OpenGlHelper.glBlendFunc(770, 771, 1, 0);
		Vec3d vec = new Vec3d(1, 0, 0);

		Tessellator tess = Tessellator.getInstance();
		BufferBuilder buffer = tess.getBuffer();

		double s = 3;
		int count = 16;

		//swirl, inner part (solid)
		for(int j = 0; j < 2; j++) {
			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
			for(int i = 0; i < count; i++) {

				addTexColorVertex(buffer, vec.x * 0.9, 0, vec.z * 0.9, 0.5 + vec.x * 0.25 / s * 0.9, 0.5 + vec.z * 0.25 / s * 0.9, color(0F, 0F, 0F, 1F));
				int ringColor = j == 0 ? getColorFull(entity, 255) : color(1F, 1F, 1F, glow);
				addTexColorVertex(buffer, vec.x * s, 0, vec.z * s, 0.5 + vec.x * 0.25, 0.5 + vec.z * 0.25, ringColor);

				vec = vec.rotateYaw((float)(Math.PI * 2 / count));
				addTexColorVertex(buffer, vec.x * s, 0, vec.z * s, 0.5 + vec.x * 0.25, 0.5 + vec.z * 0.25, ringColor);
				addTexColorVertex(buffer, vec.x * 0.9, 0, vec.z * 0.9, 0.5 + vec.x * 0.25 / s * 0.9, 0.5 + vec.z * 0.25 / s * 0.9, color(0F, 0F, 0F, 1F));
			}

			tess.draw();

			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		}

		OpenGlHelper.glBlendFunc(770, 771, 1, 0);

		//swirl, outer part (fade)
		for(int j = 0; j < 2; j++) {

			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
			for(int i = 0; i < count; i++) {

				int ringColor = j == 0 ? getColorFull(entity, 255) : color(1F, 1F, 1F, glow);
				int fadeColor = getColorFull(entity, 0);
				addTexColorVertex(buffer, vec.x * s, 0, vec.z * s, 0.5 + vec.x * 0.25, 0.5 + vec.z * 0.25, ringColor);
				addTexColorVertex(buffer, vec.x * s * 2, 0, vec.z * s * 2, 0.5 + vec.x * 0.5, 0.5 + vec.z * 0.5, fadeColor);

				vec = vec.rotateYaw((float)(Math.PI * 2 / count));
				addTexColorVertex(buffer, vec.x * s * 2, 0, vec.z * s * 2, 0.5 + vec.x * 0.5, 0.5 + vec.z * 0.5, fadeColor);
				addTexColorVertex(buffer, vec.x * s, 0, vec.z * s, 0.5 + vec.x * 0.25, 0.5 + vec.z * 0.25, ringColor);
			}
			tess.draw();

			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		}


		GL11.glShadeModel(GL11.GL_FLAT);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
		GL11.glDepthMask(true);
		GL11.glEnable(GL11.GL_ALPHA_TEST);

		GL11.glPopMatrix();
	}

	protected void renderJets(Entity entity, float interp) {

		Tessellator tess = Tessellator.getInstance();
		BufferBuilder tessBuffer = tess.getBuffer();

		GL11.glPushMatrix();
		GL11.glRotatef(entity.getEntityId() % 90 - 45, 1, 0, 0);
		GL11.glRotatef(entity.getEntityId() % 360, 0, 1, 0);

		GL11.glDisable(GL11.GL_ALPHA_TEST);
		GL11.glDepthMask(false);
		GL11.glAlphaFunc(GL11.GL_GEQUAL, 0.0F);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		GL11.glShadeModel(GL11.GL_SMOOTH);
		GL11.glDisable(GL11.GL_TEXTURE_2D);

		for(int j = -1; j <= 1; j += 2) {
			tessBuffer.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);

			int tessColor0Red = Math.max(0, Math.min(255, (int) ((1.0F) * 255.0F)));
			int tessColor0Green = Math.max(0, Math.min(255, (int) ((1.0F) * 255.0F)));
			int tessColor0Blue = Math.max(0, Math.min(255, (int) ((1.0F) * 255.0F)));
			int tessColor0Alpha = Math.max(0, Math.min(255, (int) ((0.35F) * 255.0F)));
			tessBuffer.pos(0, 0, 0).color(tessColor0Red, tessColor0Green, tessColor0Blue, tessColor0Alpha).endVertex();
			int tessColor1Red = Math.max(0, Math.min(255, (int) ((1.0F) * 255.0F)));
			int tessColor1Green = Math.max(0, Math.min(255, (int) ((1.0F) * 255.0F)));
			int tessColor1Blue = Math.max(0, Math.min(255, (int) ((1.0F) * 255.0F)));
			int tessColor1Alpha = Math.max(0, Math.min(255, (int) ((0.0F) * 255.0F)));

			Vec3d jet = new Vec3d(0.5, 0, 0);

			for(int i = 0; i <= 12; i++) {

				tessBuffer.pos(jet.x, 10 * j, jet.z).color(tessColor1Red, tessColor1Green, tessColor1Blue, tessColor1Alpha).endVertex();
				jet = jet.rotateYaw((float)(Math.PI / 6 * -j));
			}

			tess.draw();
		}
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glShadeModel(GL11.GL_FLAT);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
		GL11.glDepthMask(true);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glPopMatrix();
	}

	protected void renderFlare(Entity entity) {

		GL11.glPushMatrix();
		GL11.glScalef(0.2F, 0.2F, 0.2F);

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		RenderHelper.disableStandardItemLighting();
		int j = 75;
		float f1 = (j + 2.0F) / 200.0F;
		float f2 = 0.0F;
		int count = 250;

		count = j;

		if(f1 > 0.8F) {
			f2 = (f1 - 0.8F) / 0.2F;
		}

		Random random = new Random(432L);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glShadeModel(GL11.GL_SMOOTH);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glDepthMask(false);

		for(int i = 0; i < count; i++) {
			GL11.glRotatef(random.nextFloat() * 360.0F, 1.0F, 0.0F, 0.0F);
			GL11.glRotatef(random.nextFloat() * 360.0F, 0.0F, 1.0F, 0.0F);
			GL11.glRotatef(random.nextFloat() * 360.0F, 0.0F, 0.0F, 1.0F);
			GL11.glRotatef(random.nextFloat() * 360.0F, 1.0F, 0.0F, 0.0F);
			GL11.glRotatef(random.nextFloat() * 360.0F, 0.0F, 1.0F, 0.0F);
			GL11.glRotatef(random.nextFloat() * 360.0F + f1 * 90.0F, 0.0F, 0.0F, 1.0F);
			buffer.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
			float f3 = random.nextFloat() * 20.0F + 5.0F + f2 * 10.0F;
			float f4 = random.nextFloat() * 2.0F + 1.0F + f2 * 2.0F;
			addColorVertex(buffer, 0.0D, 0.0D, 0.0D, getColorFull(entity, 255));
			int transparentColor = getColorFull(entity, 0);
			addColorVertex(buffer, -0.866D * f4, f3, -0.5F * f4, transparentColor);
			addColorVertex(buffer, 0.866D * f4, f3, -0.5F * f4, transparentColor);
			addColorVertex(buffer, 0.0D, f3, 1.0F * f4, transparentColor);
			addColorVertex(buffer, -0.866D * f4, f3, -0.5F * f4, transparentColor);
			tessellator.draw();
		}

		GL11.glDepthMask(true);
		GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glShadeModel(GL11.GL_FLAT);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		RenderHelper.enableStandardItemLighting();
		GL11.glPopMatrix();
	}

	protected int getColorFull(Entity e, int alpha) {

		if(e instanceof EntityVortex)
			return colorFromRgb(0x3898b3, alpha);

		else if(e instanceof EntityRagingVortex)
			return colorFromRgb(0xe8390d, alpha);

		return colorFromRgb(0xFFB900, alpha);
	}

	private static int colorFromRgb(int rgb, int alpha) {
		return ((rgb >> 16 & 255) << 24) | ((rgb >> 8 & 255) << 16) | ((rgb & 255) << 8) | (alpha & 255);
	}

	private static int color(float red, float green, float blue, float alpha) {
		return (colorChannel(red) << 24) | (colorChannel(green) << 16) | (colorChannel(blue) << 8) | colorChannel(alpha);
	}

	private static int colorChannel(float value) {
		return Math.max(0, Math.min(255, (int) (value * 255.0F)));
	}

	private static void addTexColorVertex(BufferBuilder buffer, double x, double y, double z, double u, double v, int rgba) {
		buffer.pos(x, y, z).tex(u, v)
				.color(rgba >>> 24 & 255, rgba >>> 16 & 255, rgba >>> 8 & 255, rgba & 255)
				.endVertex();
	}

	private static void addColorVertex(BufferBuilder buffer, double x, double y, double z, int rgba) {
		buffer.pos(x, y, z)
				.color(rgba >>> 24 & 255, rgba >>> 16 & 255, rgba >>> 8 & 255, rgba & 255)
				.endVertex();
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity p_110775_1_) {
		return null;
	}

}
