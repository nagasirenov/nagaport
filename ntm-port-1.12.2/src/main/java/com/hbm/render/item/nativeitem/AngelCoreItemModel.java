package com.hbm.render.item.nativeitem;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import javax.vecmath.Matrix4f;
import org.apache.commons.lang3.tuple.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/** Immutable per-holder model. Neither stack, entity nor last render context is stored globally. */
public final class AngelCoreItemModel extends StaticMeshItemModel {
    private final TextureAtlasSprite sprite;
    private final Map<TransformType, Matrix4f> transforms;
    private final ItemOverrideList overrides = new ItemOverrideList(Collections.emptyList()) {
        @Override public IBakedModel handleItemState(IBakedModel original, ItemStack stack,
                World world, EntityLivingBase holder) {
            if(holder == null || !holder.isSneaking()) return original;
            Render<?> render = Minecraft.getMinecraft().getRenderManager().getEntityRenderObject(holder);
            if(!(render instanceof RenderLivingBase)) return original;
            Object mainModel = ((RenderLivingBase<?>)render).getMainModel();
            if(!(mainModel instanceof ModelBiped)) return original;
            ModelBiped biped = (ModelBiped)mainModel;
            Map<TransformType, Matrix4f> corrected = copy(transforms);
            ModelRenderer arm = biped.bipedRightArm;
            corrected.put(TransformType.THIRD_PERSON_RIGHT_HAND, compensateSneaking(
                    transforms.get(TransformType.THIRD_PERSON_RIGHT_HAND), arm.rotateAngleX,
                    arm.rotateAngleY, arm.rotateAngleZ, false));
            arm = biped.bipedLeftArm;
            corrected.put(TransformType.THIRD_PERSON_LEFT_HAND, compensateSneaking(
                    transforms.get(TransformType.THIRD_PERSON_LEFT_HAND), arm.rotateAngleX,
                    arm.rotateAngleY, arm.rotateAngleZ, true));
            return new AngelCoreItemModel(corrected, sprite, getBaseModel());
        }
    };
    public AngelCoreItemModel(Map<TransformType, Matrix4f> transforms, TextureAtlasSprite sprite) {
        this(transforms, sprite, null);
    }
    private AngelCoreItemModel(Map<TransformType, Matrix4f> transforms, TextureAtlasSprite sprite, IBakedModel baseModel) {
        super(transforms, sprite, baseModel);
        this.transforms = copy(transforms);
        this.sprite = sprite;
    }
    @Override public AngelCoreItemModel withBaseModel(IBakedModel original) {
        return new AngelCoreItemModel(transforms, sprite, original);
    }
    @Override public ItemOverrideList getOverrides() { return overrides; }
    @Override public Pair<? extends IBakedModel, Matrix4f> handlePerspective(TransformType type) {
        if(type == TransformType.GUI) {
            // Native GUI has already applied S(16,-16,16). Old ItemRenderBase set GUI
            // lights before its geometric rotations/scales, in screen translation only.
            GlStateManager.pushMatrix();
            try {
                GlStateManager.scale(1F / 16F, -1F / 16F, 1F / 16F);
                RenderHelper.enableGUIStandardItemLighting();
            } finally { GlStateManager.popMatrix(); }
        }
        return super.handlePerspective(type);
    }
    public static Matrix4f compensateSneaking(Matrix4f perspective,
            float xRadians, float yRadians, float zRadians, boolean left) {
        // Native ModelRenderer.postRender uses Z,Y,X. Pivot translations cancel in
        // A^-1*T*A; reproduce its float degree conversion before native GL rotation.
        Matrix4f arm = new ItemTransformMatrix().rotateZ(zRadians * 57.295776F)
                .rotateY(yRadians * 57.295776F).rotateX(xRadians * 57.295776F).build();
        Matrix4f suffix = new ItemTransformMatrix().rotateX(-90).rotateY(180)
                .translate(left ? -.0625 : .0625, .125, -.625).build();
        Matrix4f inverseArm = new Matrix4f(arm); inverseArm.invert();
        Matrix4f inverseSuffix = new Matrix4f(suffix); inverseSuffix.invert();
        Matrix4f correction = new ItemTransformMatrix().multiply(inverseSuffix).multiply(inverseArm)
                .translate(0, -(double).2F, 0).multiply(arm).multiply(suffix).build();
        if(left) {
            // Forge subsequently conjugates the complete left perspective by flip-X.
            Matrix4f flip = new ItemTransformMatrix().scale(-1, 1, 1).build();
            correction = new ItemTransformMatrix().multiply(flip).multiply(correction).multiply(flip).build();
        }
        return new ItemTransformMatrix().multiply(correction).multiply(perspective).build();
    }
    private static Map<TransformType, Matrix4f> copy(Map<TransformType, Matrix4f> source) {
        Map<TransformType, Matrix4f> result = new EnumMap<>(TransformType.class);
        for(Map.Entry<TransformType, Matrix4f> entry : source.entrySet())
            result.put(entry.getKey(), new Matrix4f(entry.getValue()));
        return result;
    }
}
