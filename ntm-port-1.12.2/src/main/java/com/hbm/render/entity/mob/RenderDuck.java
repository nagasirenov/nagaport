package com.hbm.render.entity.mob;

import com.hbm.lib.RefStrings;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RenderChicken;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.util.ResourceLocation;

public class RenderDuck extends RenderChicken {

	public static final ResourceLocation ducc = new ResourceLocation(RefStrings.MODID, "textures/entity/duck.png");

	public RenderDuck(RenderManager renderManager, ModelBase model, float shadowSize) {
		super(renderManager);
		this.mainModel = model;
		this.shadowSize = shadowSize;
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityChicken entity) {
		return ducc;
	}
}
