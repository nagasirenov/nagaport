package com.hbm.render.entity.projectile;

import java.awt.Color;
import java.util.Random;

import org.lwjgl.opengl.GL11;

import com.hbm.entity.projectile.EntityChemical;
import com.hbm.entity.projectile.EntityChemical.ChemicalStyle;
import com.hbm.lib.RefStrings;

import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;

public class RenderChemical extends Render {

	public RenderChemical(RenderManager renderManager) {
		super(renderManager);
	}
	
	private static ResourceLocation gas = new ResourceLocation(RefStrings.MODID + ":textures/particle/particle_base.png");

	@Override
	public void doRender(Entity entity, double x, double y, double z, float f0, float f1) {
		
		GL11.glPushMatrix();
		GL11.glTranslated(x, y, z);
		
		EntityChemical chem = (EntityChemical) entity;
		ChemicalStyle style = chem.getStyle();
		
		if(style == ChemicalStyle.AMAT || style == ChemicalStyle.LIGHTNING)
			renderAmatBeam(chem, f1);
		
		if(style == ChemicalStyle.GAS) {
			this.bindEntityTexture(chem);
			renderGasCloud(chem, f1);
		}
		
		if(style == ChemicalStyle.GASFLAME) {
			this.bindEntityTexture(chem);
			renderGasFire(chem, f1);
		}

		GL11.glPopMatrix();
	}
	
	private void renderGasFire(EntityChemical chem, float interp) {

		float exp = (float) (chem.ticksExisted + interp) / (float) chem.getMaxAge();
		double size = 0.0 + exp * 2;
		Color color = Color.getHSBColor(Math.max((60 - exp * 100) / 360F, 0.0F), 1 - exp * 0.25F, 1 - exp * 0.5F);
		
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glAlphaFunc(GL11.GL_GREATER, 0);
		OpenGlHelper.glBlendFunc(770, 771, 1, 0);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		GL11.glDepthMask(false);
		
		Tessellator tess = Tessellator.getInstance();
		BufferBuilder tessBuffer = tess.getBuffer();
		GL11.glRotatef(180.0F - this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
		GL11.glRotatef(-this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
		tessBuffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
		float tessNormal0X = 0.0F, tessNormal0Y = 1.0F, tessNormal0Z = 0.0F;
		int tessColor0Packed = color.getRGB();
		int tessColor0Alpha = Math.max(0, Math.min(255, (int) Math.max(255 * (1 - exp), 0)));
		tessBuffer.pos(-size, -size, 0.0D).tex(1, 1).color((tessColor0Packed >> 16) & 255, (tessColor0Packed >> 8) & 255, tessColor0Packed & 255, tessColor0Alpha).normal(tessNormal0X, tessNormal0Y, tessNormal0Z).endVertex();
		tessBuffer.pos(size, -size, 0.0D).tex(0, 1).color((tessColor0Packed >> 16) & 255, (tessColor0Packed >> 8) & 255, tessColor0Packed & 255, tessColor0Alpha).normal(tessNormal0X, tessNormal0Y, tessNormal0Z).endVertex();
		tessBuffer.pos(size, size, 0.0D).tex(0, 0).color((tessColor0Packed >> 16) & 255, (tessColor0Packed >> 8) & 255, tessColor0Packed & 255, tessColor0Alpha).normal(tessNormal0X, tessNormal0Y, tessNormal0Z).endVertex();
		tessBuffer.pos(-size, size, 0.0D).tex(1, 0).color((tessColor0Packed >> 16) & 255, (tessColor0Packed >> 8) & 255, tessColor0Packed & 255, tessColor0Alpha).normal(tessNormal0X, tessNormal0Y, tessNormal0Z).endVertex();
		tess.draw();

		GL11.glDepthMask(true);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glAlphaFunc(GL11.GL_GEQUAL, 0.1F);
	}
	
	private void renderGasCloud(EntityChemical chem, float interp) {

		double exp = (double) (chem.ticksExisted + interp) / (double) chem.getMaxAge();
		double size = 0.0 + exp * 10;
		int color = chem.getType().getColor();
		
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glAlphaFunc(GL11.GL_GREATER, 0);
		OpenGlHelper.glBlendFunc(770, 771, 1, 0);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		GL11.glDepthMask(false);
		
		Tessellator tess = Tessellator.getInstance();
		BufferBuilder tessBuffer = tess.getBuffer();
		GL11.glRotatef(180.0F - this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
		GL11.glRotatef(-this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
		
		Random rand = new Random(chem.getEntityId());
		int i = rand.nextInt(2);
		int j = rand.nextInt(2);
		
		tessBuffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
		float tessNormal0X = 0.0F, tessNormal0Y = 1.0F, tessNormal0Z = 0.0F;
		int tessColor0Packed = color;
		int tessColor0Alpha = Math.max(0, Math.min(255, (int) Math.max(127 * (1 - exp), 0)));
		tessBuffer.pos(-size, -size, 0.0D).tex(1 - i, 1 - j).color((tessColor0Packed >> 16) & 255, (tessColor0Packed >> 8) & 255, tessColor0Packed & 255, tessColor0Alpha).normal(tessNormal0X, tessNormal0Y, tessNormal0Z).endVertex();
		tessBuffer.pos(size, -size, 0.0D).tex(i, 1 - j).color((tessColor0Packed >> 16) & 255, (tessColor0Packed >> 8) & 255, tessColor0Packed & 255, tessColor0Alpha).normal(tessNormal0X, tessNormal0Y, tessNormal0Z).endVertex();
		tessBuffer.pos(size, size, 0.0D).tex(i, j).color((tessColor0Packed >> 16) & 255, (tessColor0Packed >> 8) & 255, tessColor0Packed & 255, tessColor0Alpha).normal(tessNormal0X, tessNormal0Y, tessNormal0Z).endVertex();
		tessBuffer.pos(-size, size, 0.0D).tex(1 - i, j).color((tessColor0Packed >> 16) & 255, (tessColor0Packed >> 8) & 255, tessColor0Packed & 255, tessColor0Alpha).normal(tessNormal0X, tessNormal0Y, tessNormal0Z).endVertex();
		tess.draw();

		GL11.glDepthMask(true);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glAlphaFunc(GL11.GL_GEQUAL, 0.1F);
	}
	
	private void renderAmatBeam(EntityChemical chem, float interp) {

		float yaw = chem.prevRotationYaw + (chem.rotationYaw - chem.prevRotationYaw) * interp;
		float pitch = chem.prevRotationPitch + (chem.rotationPitch - chem.prevRotationPitch) * interp;
		GL11.glRotatef(yaw, 0.0F, 1.0F, 0.0F);
		GL11.glRotatef(-pitch - 90, 1.0F, 0.0F, 0.0F);

		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		GL11.glShadeModel(GL11.GL_SMOOTH);
		GL11.glDepthMask(false);
		
		/*double x0 = chem.prevPosX + (chem.posX - chem.prevPosX) * interp;
		double y0 = chem.prevPosX + (chem.posY - chem.prevPosY) * interp;
		double z0 = chem.prevPosX + (chem.posZ - chem.prevPosZ) * interp;*/
		
		double length = new Vec3d(chem.motionX, chem.motionY, chem.motionZ).length() * (chem.ticksExisted + interp) * 0.75;
		double size = 0.0625;
		float o = 0.2F;
		
		Tessellator tess = Tessellator.getInstance();
		BufferBuilder tessBuffer = tess.getBuffer();
		tessBuffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
		
		int tessColor0Red = Math.max(0, Math.min(255, (int) ((1F) * 255.0F)));
		int tessColor0Green = Math.max(0, Math.min(255, (int) ((1F) * 255.0F)));
		int tessColor0Blue = Math.max(0, Math.min(255, (int) ((1F) * 255.0F)));
		int tessColor0Alpha = Math.max(0, Math.min(255, (int) ((o) * 255.0F)));
		tessBuffer.pos(-size, 0, -size).color(tessColor0Red, tessColor0Green, tessColor0Blue, tessColor0Alpha).endVertex();
		tessBuffer.pos(size, 0, -size).color(tessColor0Red, tessColor0Green, tessColor0Blue, tessColor0Alpha).endVertex();
		int tessColor1Red = Math.max(0, Math.min(255, (int) ((1F) * 255.0F)));
		int tessColor1Green = Math.max(0, Math.min(255, (int) ((1F) * 255.0F)));
		int tessColor1Blue = Math.max(0, Math.min(255, (int) ((1F) * 255.0F)));
		int tessColor1Alpha = Math.max(0, Math.min(255, (int) ((0.0F) * 255.0F)));
		tessBuffer.pos(size, length, -size).color(tessColor1Red, tessColor1Green, tessColor1Blue, tessColor1Alpha).endVertex();
		tessBuffer.pos(-size, length, -size).color(tessColor1Red, tessColor1Green, tessColor1Blue, tessColor1Alpha).endVertex();

		int tessColor2Red = Math.max(0, Math.min(255, (int) ((1F) * 255.0F)));
		int tessColor2Green = Math.max(0, Math.min(255, (int) ((1F) * 255.0F)));
		int tessColor2Blue = Math.max(0, Math.min(255, (int) ((1F) * 255.0F)));
		int tessColor2Alpha = Math.max(0, Math.min(255, (int) ((o) * 255.0F)));
		tessBuffer.pos(-size, 0, size).color(tessColor2Red, tessColor2Green, tessColor2Blue, tessColor2Alpha).endVertex();
		tessBuffer.pos(size, 0, size).color(tessColor2Red, tessColor2Green, tessColor2Blue, tessColor2Alpha).endVertex();
		int tessColor3Red = Math.max(0, Math.min(255, (int) ((1F) * 255.0F)));
		int tessColor3Green = Math.max(0, Math.min(255, (int) ((1F) * 255.0F)));
		int tessColor3Blue = Math.max(0, Math.min(255, (int) ((1F) * 255.0F)));
		int tessColor3Alpha = Math.max(0, Math.min(255, (int) ((0.0F) * 255.0F)));
		tessBuffer.pos(size, length, size).color(tessColor3Red, tessColor3Green, tessColor3Blue, tessColor3Alpha).endVertex();
		tessBuffer.pos(-size, length, size).color(tessColor3Red, tessColor3Green, tessColor3Blue, tessColor3Alpha).endVertex();

		int tessColor4Red = Math.max(0, Math.min(255, (int) ((1F) * 255.0F)));
		int tessColor4Green = Math.max(0, Math.min(255, (int) ((1F) * 255.0F)));
		int tessColor4Blue = Math.max(0, Math.min(255, (int) ((1F) * 255.0F)));
		int tessColor4Alpha = Math.max(0, Math.min(255, (int) ((o) * 255.0F)));
		tessBuffer.pos(-size, 0, -size).color(tessColor4Red, tessColor4Green, tessColor4Blue, tessColor4Alpha).endVertex();
		tessBuffer.pos(-size, 0, size).color(tessColor4Red, tessColor4Green, tessColor4Blue, tessColor4Alpha).endVertex();
		int tessColor5Red = Math.max(0, Math.min(255, (int) ((1F) * 255.0F)));
		int tessColor5Green = Math.max(0, Math.min(255, (int) ((1F) * 255.0F)));
		int tessColor5Blue = Math.max(0, Math.min(255, (int) ((1F) * 255.0F)));
		int tessColor5Alpha = Math.max(0, Math.min(255, (int) ((0.0F) * 255.0F)));
		tessBuffer.pos(-size, length, size).color(tessColor5Red, tessColor5Green, tessColor5Blue, tessColor5Alpha).endVertex();
		tessBuffer.pos(-size, length, -size).color(tessColor5Red, tessColor5Green, tessColor5Blue, tessColor5Alpha).endVertex();

		int tessColor6Red = Math.max(0, Math.min(255, (int) ((1F) * 255.0F)));
		int tessColor6Green = Math.max(0, Math.min(255, (int) ((1F) * 255.0F)));
		int tessColor6Blue = Math.max(0, Math.min(255, (int) ((1F) * 255.0F)));
		int tessColor6Alpha = Math.max(0, Math.min(255, (int) ((o) * 255.0F)));
		tessBuffer.pos(size, 0, -size).color(tessColor6Red, tessColor6Green, tessColor6Blue, tessColor6Alpha).endVertex();
		tessBuffer.pos(size, 0, size).color(tessColor6Red, tessColor6Green, tessColor6Blue, tessColor6Alpha).endVertex();
		int tessColor7Red = Math.max(0, Math.min(255, (int) ((1F) * 255.0F)));
		int tessColor7Green = Math.max(0, Math.min(255, (int) ((1F) * 255.0F)));
		int tessColor7Blue = Math.max(0, Math.min(255, (int) ((1F) * 255.0F)));
		int tessColor7Alpha = Math.max(0, Math.min(255, (int) ((0.0F) * 255.0F)));
		tessBuffer.pos(size, length, size).color(tessColor7Red, tessColor7Green, tessColor7Blue, tessColor7Alpha).endVertex();
		tessBuffer.pos(size, length, -size).color(tessColor7Red, tessColor7Green, tessColor7Blue, tessColor7Alpha).endVertex();
		
		tess.draw();

		GL11.glDepthMask(true);
		GL11.glShadeModel(GL11.GL_FLAT);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity entity) {
		return gas;
	}
}
