package com.hbm.render.entity.mob;

import com.hbm.main.ResourceManager;
import com.hbm.render.model.ModelBlockSpider;

import net.minecraft.client.renderer.entity.RenderLiving;
import com.hbm.entity.mob.EntityBlockSpider;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderBlockSpider extends RenderLiving<EntityBlockSpider> {

	public RenderBlockSpider(RenderManager manager) {
		super(manager, new ModelBlockSpider(), 1.0F);
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityBlockSpider p_110775_1_) {
		return ResourceManager.spider_tex;
	}
}
