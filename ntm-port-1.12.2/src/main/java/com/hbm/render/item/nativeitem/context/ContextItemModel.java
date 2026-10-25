package com.hbm.render.item.nativeitem.context;

import com.hbm.render.item.nativeitem.ItemTransformMatrix;
import com.hbm.render.item.nativeitem.direct.PlainItemPerspectives;
import com.hbm.render.item.nativeitem.staticfamily.OrdinaryMeshItemModel;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.vecmath.Matrix4f;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
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

/** Each override/perspective returns a fresh immutable argument object. The native
 * RenderItem boundary receives that very model and the actual stack together. */
public class ContextItemModel implements IBakedModel, com.hbm.render.util.NativeItemBaseModel, com.hbm.render.util.NativeMachineItemRenderer.FrameBaseModel {
    private final IBakedModel baseModel;
    final ContextItemRenderer renderer;
    final NativeItemContext context;
    private final TextureAtlasSprite particle;
    private final ItemStack resolvedStack;
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
            IBakedModel resolvedBase = baseModel;
            if(renderer instanceof NativeSelectiveItemRenderer && baseModel != null)
                resolvedBase = baseModel.getOverrides().handleItemState(baseModel, stack, world, holder);
            return create(resolvedBase, particle, renderer, stack, new NativeItemContext(TransformType.NONE, holder), right, left);
        }
    };
    public ContextItemModel(TextureAtlasSprite particle, ContextItemRenderer renderer) {
        this(null, particle, renderer, ItemStack.EMPTY, new NativeItemContext(TransformType.NONE, null), null, null);
    }
    public ContextItemModel(IBakedModel baseModel, ContextItemRenderer renderer) {
        this(Objects.requireNonNull(baseModel), baseModel.getParticleTexture(), renderer, ItemStack.EMPTY,
                new NativeItemContext(TransformType.NONE, null), null, null);
    }
    @Override public IBakedModel getBaseModel() { return baseModel; }
    @Override public IBakedModel machineFrameModel(ItemStack stack) {
        return renderer instanceof NativeSelectiveItemRenderer && !((NativeSelectiveItemRenderer)renderer).handlesPerspective(stack, TransformType.GROUND)
                ? baseModel : this;
    }
    private ContextItemModel(IBakedModel baseModel, TextureAtlasSprite particle, ContextItemRenderer renderer, ItemStack stack,
            NativeItemContext context, Matrix4f rightSneak, Matrix4f leftSneak) {
        this.baseModel = baseModel;
        this.particle = Objects.requireNonNull(particle, "particle");
        this.renderer = Objects.requireNonNull(renderer, "renderer");
        this.resolvedStack = stack;
        this.context = context;
        this.rightSneak = rightSneak;
        this.leftSneak = leftSneak;
    }
    private static Matrix4f correction(ModelRenderer arm, boolean left) {
        return OrdinaryMeshItemModel.compensateSneaking(new ItemTransformMatrix().build(),
                arm.rotateAngleX, arm.rotateAngleY, arm.rotateAngleZ, left);
    }
    @Override public Pair<? extends IBakedModel, Matrix4f> handlePerspective(TransformType type) {
        NativeItemContext selected = new NativeItemContext(type, context.holder);
        NativeSelectiveItemRenderer selective = renderer instanceof NativeSelectiveItemRenderer ? (NativeSelectiveItemRenderer)renderer : null;
        if(selective != null && !selective.handlesPerspective(resolvedStack, type))
            return baseModel.handlePerspective(type);
        Matrix4f matrix = selective == null ? PlainItemPerspectives.compose(type, renderer.localTransform(resolvedStack, selected))
                : selective.composePerspective(resolvedStack, selected);
        Matrix4f correction = type == TransformType.THIRD_PERSON_RIGHT_HAND ? rightSneak
                : type == TransformType.THIRD_PERSON_LEFT_HAND ? leftSneak : null;
        if(correction != null) matrix = new ItemTransformMatrix().multiply(correction).multiply(matrix).build();
        return Pair.of(create(baseModel, particle, renderer, resolvedStack, selected, rightSneak, leftSneak), matrix);
    }
    /** Only explicitly frame-owning renderers publish the machine-frame marker. */
    public static ContextItemModel create(IBakedModel base, ContextItemRenderer renderer) {
        return create(base, base.getParticleTexture(), renderer, ItemStack.EMPTY, new NativeItemContext(TransformType.NONE, null), null, null);
    }
    /** Activation supplies the resolved real particle; the placeholder base model must not define it. */
    public static ContextItemModel create(IBakedModel base, TextureAtlasSprite particle, ContextItemRenderer renderer) {
        return create(base, particle, renderer, ItemStack.EMPTY, new NativeItemContext(TransformType.NONE, null), null, null);
    }
    private static ContextItemModel create(IBakedModel base, TextureAtlasSprite particle, ContextItemRenderer renderer,
            ItemStack stack, NativeItemContext context, Matrix4f right, Matrix4f left) {
        return renderer instanceof com.hbm.render.util.NativeMachineItemRenderer.FrameItemModel
                ? new FrameModel(base, particle, renderer, stack, context, right, left)
                : new ContextItemModel(base, particle, renderer, stack, context, right, left);
    }
    private static final class FrameModel extends ContextItemModel implements com.hbm.render.util.NativeMachineItemRenderer.FrameItemModel {
        FrameModel(IBakedModel base, TextureAtlasSprite particle, ContextItemRenderer renderer, ItemStack stack,
                NativeItemContext context, Matrix4f right, Matrix4f left) { super(base, particle, renderer, stack, context, right, left); }
        @Override public void renderFrame(net.minecraft.entity.item.EntityItem entity, double x, double y, double z,
                float yaw, float partialTicks, com.hbm.render.util.NativeMachineItemRenderer.Display display) {
            ((com.hbm.render.util.NativeMachineItemRenderer.FrameItemModel)renderer).renderFrame(entity, x, y, z, yaw, partialTicks, display);
        }
    }
    @Override public List<BakedQuad> getQuads(IBlockState state, EnumFacing face, long seed) { return Collections.emptyList(); }
    @Override public boolean isAmbientOcclusion() { return false; }
    @Override public boolean isGui3d() { return renderer instanceof NativeSelectiveItemRenderer && baseModel != null ? baseModel.isGui3d() : true; }
    @Override public boolean isBuiltInRenderer() { return true; }
    @Override public TextureAtlasSprite getParticleTexture() { return particle; }
    @Override public ItemCameraTransforms getItemCameraTransforms() { return renderer instanceof NativeSelectiveItemRenderer && baseModel != null ? baseModel.getItemCameraTransforms() : ItemCameraTransforms.DEFAULT; }
    @Override public ItemOverrideList getOverrides() { return overrides; }
}
