package com.hbm.render.entity.mob;

import com.hbm.entity.mob.EntityDummy;
import com.hbm.lib.RefStrings;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.ResourceLocation;

public class RenderDummy extends RenderBiped {

	public RenderDummy(net.minecraft.client.renderer.entity.RenderManager renderManager) {
		super(renderManager, new ModelBiped(0.0F), 0.5F);
		this.addLayer(new net.minecraft.client.renderer.entity.layers.LayerBipedArmor(this)); // 1.12 RenderBiped no longer draws armor itself
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityLiving entity) {
		return this.getEntityTexture((EntityDummy) entity);
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity entity) {
		return this.getEntityTexture((EntityDummy) entity);
	}

	protected ResourceLocation getEntityTexture(EntityDummy entity) {
		return new ResourceLocation(RefStrings.MODID + ":textures/entity/dummy.png");
	}
}
