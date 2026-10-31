package com.hbm.render.item.nativeitem.callback;

import com.hbm.render.item.nativeitem.ItemTransformMatrix;
import com.hbm.render.item.nativeitem.StaticMeshItemRegistration;
import com.hbm.render.item.nativeitem.AngelCoreFrameCorrection;

import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.item.nativeitem.staticfamily.OrdinaryMeshItemModel;
import com.hbm.render.item.nativeitem.staticfamily.StaticItemPerspectives;
import java.nio.FloatBuffer;
import java.util.Collections;
import java.util.List;
import javax.vecmath.Matrix4f;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

/** Native per-holder model for existing NTM mesh callbacks. The audited inventory
 * and non-inventory callbacks contain only matrix operations (including animation).
 * Capture those operations locally; never store a last stack/holder/context globally.
 */
public final class CallbackItemModel implements IBakedModel, com.hbm.render.util.NativeItemBaseModel {
    private static final FloatBuffer MATRIX = BufferUtils.createFloatBuffer(16); // render thread scratch
    private final IBakedModel baseModel;
    private final IBakedModel perspective;
    private final ItemRenderBase renderer;
    private final ItemOverrideList overrides = new ItemOverrideList(Collections.emptyList()) {
        @Override public IBakedModel handleItemState(IBakedModel original, ItemStack stack, World world, EntityLivingBase holder) {
            IBakedModel selected = perspective.getOverrides().handleItemState(perspective, stack, world, holder);
            return selected == perspective ? original : new CallbackItemModel(selected, renderer, baseModel);
        }
    };
    public CallbackItemModel(TextureAtlasSprite sprite, ItemRenderBase renderer) {
        this(sprite, renderer, null);
    }
    public CallbackItemModel(TextureAtlasSprite sprite, ItemRenderBase renderer, IBakedModel baseModel) {
        this(new OrdinaryMeshItemModel(StaticItemPerspectives.complete(new ItemTransformMatrix().build()), sprite), renderer, baseModel);
    }
    private CallbackItemModel(IBakedModel perspective, ItemRenderBase renderer, IBakedModel baseModel) {
        this.baseModel = baseModel;
        this.perspective=perspective; this.renderer=renderer;
    }
    @Override public IBakedModel getBaseModel() { return baseModel; }
    @Override public Pair<? extends IBakedModel, Matrix4f> handlePerspective(TransformType type) {
        Matrix4f base = perspective.handlePerspective(type).getRight();
        Matrix4f callback = captureTransform(type==TransformType.GUI ? renderer::renderInventory : renderer::renderNonInv);
        // Base perspectives include +.5 compensation for native renderItem's -.5.
        // Insert the callback before that compensation, exactly before common draw.
        Matrix4f result = new ItemTransformMatrix().multiply(base).translate(-.5,-.5,-.5)
                .multiply(callback).translate(.5,.5,.5).build();
        return Pair.of(this,result);
    }
    public static Matrix4f captureTransform(Runnable callback) {
        int mode=GL11.glGetInteger(GL11.GL_MATRIX_MODE);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);GL11.glPushMatrix();
        try {
            GL11.glLoadIdentity();callback.run();
            float[] columnMajor=new float[16];MATRIX.clear();GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX,MATRIX);MATRIX.get(columnMajor);
            Matrix4f result=new Matrix4f(columnMajor);result.transpose();return result;
        } finally { GL11.glPopMatrix();GL11.glMatrixMode(mode); }
    }
    @Override public List<BakedQuad> getQuads(IBlockState state,EnumFacing face,long seed){return Collections.emptyList();}
    @Override public boolean isAmbientOcclusion(){return false;}
    @Override public boolean isGui3d(){return true;}
    @Override public boolean isBuiltInRenderer(){return true;}
    @Override public TextureAtlasSprite getParticleTexture(){return perspective.getParticleTexture();}
    @Override public ItemCameraTransforms getItemCameraTransforms(){return ItemCameraTransforms.DEFAULT;}
    @Override public ItemOverrideList getOverrides(){return overrides;}
}
