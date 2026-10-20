package com.hbm.render.item.nativeitem.direct;

import com.hbm.render.item.nativeitem.ItemTransformMatrix;
import com.hbm.render.item.nativeitem.staticfamily.OrdinaryMeshItemModel;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.vecmath.Matrix4f;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

/** Per-call immutable pose snapshot. Only local matrices vary with native context;
 * draw receives the actual native stack directly, so nested/failed draws cannot
 * retain or overwrite another stack or holder.
 */
public final class DirectMeshItemModel implements IBakedModel, com.hbm.render.util.NativeItemBaseModel {
    private final IBakedModel baseModel;
    private final TextureAtlasSprite sprite;
    private final DirectMeshItemRenderer renderer;
    private final Matrix4f rightSneak, leftSneak;
    private final ItemOverrideList overrides = new ItemOverrideList(Collections.emptyList()) {
        @Override public IBakedModel handleItemState(IBakedModel original, ItemStack stack, World world, EntityLivingBase holder) {
            Matrix4f right = null, left = null;
            if(holder != null && holder.isSneaking()) {
                Render<?> render = Minecraft.getMinecraft().getRenderManager().getEntityRenderObject(holder);
                if(render instanceof RenderLivingBase && ((RenderLivingBase<?>)render).getMainModel() instanceof ModelBiped) {
                    ModelBiped biped = (ModelBiped)((RenderLivingBase<?>)render).getMainModel();
                    right = correction(biped.bipedRightArm, false);
                    left = correction(biped.bipedLeftArm, true);
                }
            }
            // Return a fresh snapshot even when called on a formerly sneaking wrapper.
            return new DirectMeshItemModel(sprite, renderer, right, left, baseModel);
        }
    };
    public DirectMeshItemModel(TextureAtlasSprite sprite, DirectMeshItemRenderer renderer) {
        this(sprite, renderer, null);
    }
    public DirectMeshItemModel(TextureAtlasSprite sprite, DirectMeshItemRenderer renderer, IBakedModel baseModel) {
        this(sprite, renderer, null, null, baseModel);
    }
    private DirectMeshItemModel(TextureAtlasSprite sprite, DirectMeshItemRenderer renderer, Matrix4f right, Matrix4f left, IBakedModel baseModel) {
        this.baseModel = baseModel;
        this.sprite = Objects.requireNonNull(sprite, "particle");
        this.renderer = Objects.requireNonNull(renderer, "renderer");
        this.rightSneak = right;
        this.leftSneak = left;
    }
    @Override public IBakedModel getBaseModel() { return baseModel; }
    private static Matrix4f correction(ModelRenderer arm, boolean left) {
        return OrdinaryMeshItemModel.compensateSneaking(new ItemTransformMatrix().build(),
                arm.rotateAngleX, arm.rotateAngleY, arm.rotateAngleZ, left);
    }
    @Override public Pair<? extends IBakedModel, Matrix4f> handlePerspective(TransformType type) {
        if(type == TransformType.GUI) {
            // Native RenderItem owns the GUI lighting lifetime. Install the original
            // directions before applying local geometry, as its outer caller expects;
            // only the temporary matrix is restored here, not those intended lights.
            GL11.glEnable(GL11.GL_LIGHTING);
            if(renderer.usesGuiStandardLighting()) {
                GlStateManager.pushMatrix();
                try {
                    GlStateManager.scale(1F / 16F, -1F / 16F, 1F / 16F);
                    RenderHelper.enableGUIStandardItemLighting();
                } finally { GlStateManager.popMatrix(); }
            }
        }
        Matrix4f matrix = PlainItemPerspectives.compose(type, renderer.localTransform(type, System.currentTimeMillis()));
        Matrix4f correction = type == TransformType.THIRD_PERSON_RIGHT_HAND ? rightSneak
                : type == TransformType.THIRD_PERSON_LEFT_HAND ? leftSneak : null;
        if(correction != null) matrix = new ItemTransformMatrix().multiply(correction).multiply(matrix).build();
        return Pair.of(this, matrix);
    }
    @Override public List<BakedQuad> getQuads(IBlockState state, EnumFacing face, long seed) { return Collections.emptyList(); }
    @Override public boolean isAmbientOcclusion() { return false; }
    @Override public boolean isGui3d() { return true; }
    @Override public boolean isBuiltInRenderer() { return true; }
    @Override public TextureAtlasSprite getParticleTexture() { return sprite; }
    @Override public ItemCameraTransforms getItemCameraTransforms() { return ItemCameraTransforms.DEFAULT; }
    @Override public ItemOverrideList getOverrides() { return overrides; }
}
