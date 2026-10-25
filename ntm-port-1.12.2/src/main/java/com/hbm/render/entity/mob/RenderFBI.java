package com.hbm.render.entity.mob;

import com.hbm.lib.RefStrings;
import com.hbm.render.model.ModelFBI;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.ResourceLocation;

public class RenderFBI extends RenderBiped<EntityLiving> {

    private static final ResourceLocation texture = new ResourceLocation(RefStrings.MODID + ":" + "textures/entity/fbi.png");

    public RenderFBI(RenderManager renderManager) {
        super(renderManager, new ModelFBI(), 0.5F);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityLiving entity) {
        return texture;
    }

    @Override
    public void doRender(EntityLiving entity, double x, double y, double z, float entityYaw, float partialTicks) {
        ((ModelBiped) this.mainModel).rightArmPose = ModelBiped.ArmPose.BOW_AND_ARROW;
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }
}
