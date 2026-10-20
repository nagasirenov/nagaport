package com.hbm.render.entity.effect;

import org.lwjgl.opengl.GL11;

import com.hbm.lib.RefStrings;
import com.hbm.entity.effect.EntityBlackHole;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public class RenderQuasar extends RenderBlackHole {

	public RenderQuasar(RenderManager renderManager) {
		super(renderManager);
	}

	
	protected ResourceLocation quasar = new ResourceLocation(RefStrings.MODID, "textures/entity/bholeD.png");

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
		
		renderDisc(entity, interp);
		renderJets(entity, interp);

		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glEnable(GL11.GL_LIGHTING);

		GL11.glPopMatrix();
	}

	@Override
	protected ResourceLocation discTex() {
		return this.quasar;
	}

	@Override
	protected void setColorFromIteration(Tessellator tess, int iteration, float alpha) {
		float r = 1.0F;
		float g = (float) Math.pow(iteration / 15F, 2);
		float b = (float) Math.pow(iteration / 15F, 2);
		
		tess.setColorRGBA_F(r, g, b, alpha);
	}

	@Override
	protected int steps() {
		return 15;
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity entity) {
		return super.getEntityTexture(entity);
	}
}
