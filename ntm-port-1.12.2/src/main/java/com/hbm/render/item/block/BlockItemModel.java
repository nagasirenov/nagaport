package com.hbm.render.item.block;

import com.hbm.render.item.nativeitem.StaticMeshItemModel;
import com.hbm.render.item.nativeitem.staticfamily.OrdinaryMeshItemModel;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import javax.vecmath.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/** Per-bake base ownership and per-holder immutable arm compensation. */
public final class BlockItemModel extends StaticMeshItemModel implements com.hbm.render.util.NativeMachineItemRenderer.FrameItemModel {
    private final ItemRenderBlock renderer;
    private final Map<TransformType, Matrix4f> transforms;
    private final ItemOverrideList overrides = new ItemOverrideList(Collections.emptyList()) {
        @Override public IBakedModel handleItemState(IBakedModel original, ItemStack stack, World world, EntityLivingBase holder) {
            if(holder == null || !holder.isSneaking()) return original;
            Render<?> render = Minecraft.getMinecraft().getRenderManager().getEntityRenderObject(holder);
            if(!(render instanceof RenderLivingBase) || !(((RenderLivingBase<?>)render).getMainModel() instanceof ModelBiped)) return original;
            ModelBiped biped = (ModelBiped)((RenderLivingBase<?>)render).getMainModel();
            Map<TransformType, Matrix4f> corrected = copy(transforms);
            correct(corrected, TransformType.THIRD_PERSON_RIGHT_HAND, biped.bipedRightArm, false);
            correct(corrected, TransformType.THIRD_PERSON_LEFT_HAND, biped.bipedLeftArm, true);
            return new BlockItemModel(getBaseModel(), renderer, corrected);
        }
    };
    public BlockItemModel(IBakedModel original, ItemRenderBlock renderer) { this(original, renderer, BlockItemPerspectives.derived()); }
    private BlockItemModel(IBakedModel original, ItemRenderBlock renderer, Map<TransformType, Matrix4f> transforms) {
        super(transforms, original.getParticleTexture(), original);
        this.transforms = copy(transforms);
        this.renderer = renderer;
    }
    private static Map<TransformType, Matrix4f> copy(Map<TransformType, Matrix4f> source) {
        Map<TransformType, Matrix4f> result = new EnumMap<>(TransformType.class);
        for(Map.Entry<TransformType, Matrix4f> entry : source.entrySet()) result.put(entry.getKey(), new Matrix4f(entry.getValue()));
        return result;
    }
    private static void correct(Map<TransformType, Matrix4f> transforms, TransformType type, ModelRenderer arm, boolean left) {
        transforms.put(type, OrdinaryMeshItemModel.compensateSneaking(transforms.get(type), arm.rotateAngleX, arm.rotateAngleY, arm.rotateAngleZ, left));
    }
    @Override public void renderFrame(net.minecraft.entity.item.EntityItem entity, double x, double y, double z, float yaw,
            float partialTicks, com.hbm.render.util.NativeMachineItemRenderer.Display display) {
        renderer.renderFrame(entity, x, y, z, yaw, partialTicks, display);
    }
    @Override public ItemOverrideList getOverrides() { return overrides; }
}
