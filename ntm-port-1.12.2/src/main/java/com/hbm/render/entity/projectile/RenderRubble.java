package com.hbm.render.entity.projectile;

import org.lwjgl.opengl.GL11;

import com.hbm.lib.RefStrings;
import com.hbm.render.model.ModelRubble;

import net.minecraft.block.Block;
import com.hbm.entity.projectile.EntityRubble;
import com.hbm.render.util.NativeBlockMaterial;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.util.ResourceLocation;

public class RenderRubble extends Render<EntityRubble> {
	
	ModelRubble mine;

	public RenderRubble(RenderManager manager) {
		super(manager);
		mine = new ModelRubble();
	}

	@Override
	public void doRender(EntityRubble rocket, double p_76986_2_, double p_76986_4_, double p_76986_6_, float p_76986_8_, float p_76986_9_) {
		GL11.glPushMatrix();
		GL11.glTranslatef((float) p_76986_2_, (float) p_76986_4_, (float) p_76986_6_);
		GL11.glScalef(1.0F, 1.0F, 1.0F);
		GL11.glRotatef(180, 1, 0, 0);
		GL11.glRotatef(((rocket.ticksExisted + p_76986_9_) % 360) * 10, 1, 1, 1);

		try {
			Block block = Block.getBlockById(rocket.getBlockId());
			bindTexture(NativeBlockMaterial.texture(block, rocket.getBlockMetadata()));

			mine.renderAll(0.0625F);
		} catch(Exception ex) { }
		
		GL11.glPopMatrix();
	}
	
	@Override
	protected ResourceLocation getEntityTexture(EntityRubble p_110775_1_) {
		return new ResourceLocation(RefStrings.MODID + ":textures/models/ModelRubbleScrap.png");
	}
}
