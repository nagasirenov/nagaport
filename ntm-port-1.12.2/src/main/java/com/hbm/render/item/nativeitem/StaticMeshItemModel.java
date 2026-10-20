package com.hbm.render.item.nativeitem;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import javax.vecmath.Matrix4f;
import org.apache.commons.lang3.tuple.Pair;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;

/** Native built-in model for a complete TEISR mesh, with immutable perspective ownership. */
public class StaticMeshItemModel implements IBakedModel, com.hbm.render.util.NativeItemBaseModel {
    private final IBakedModel baseModel;
    private final Map<TransformType, Matrix4f> perspectives = new EnumMap<>(TransformType.class);
    private final TextureAtlasSprite particle;
    public StaticMeshItemModel(Map<TransformType, Matrix4f> transforms, TextureAtlasSprite particle) {
        this(transforms, particle, null);
    }
    protected StaticMeshItemModel(Map<TransformType, Matrix4f> transforms, TextureAtlasSprite particle, IBakedModel baseModel) {
        this.baseModel = baseModel;
        if(particle == null) throw new IllegalArgumentException("A real particle sprite is required");
        for(Map.Entry<TransformType, Matrix4f> entry : transforms.entrySet())
            perspectives.put(entry.getKey(), new Matrix4f(entry.getValue()));
        this.particle = particle;
    }
    @Override public IBakedModel getBaseModel() { return baseModel; }
    public StaticMeshItemModel withBaseModel(IBakedModel original) {
        return new StaticMeshItemModel(perspectives, particle, original);
    }
    @Override public List<BakedQuad> getQuads(IBlockState state, EnumFacing face, long seed) {
        // Complete geometry is rendered by the item's TEISR, not discarded.
        return Collections.emptyList();
    }
    @Override public boolean isAmbientOcclusion() { return false; }
    @Override public boolean isGui3d() { return true; }
    @Override public boolean isBuiltInRenderer() { return true; }
    @Override public TextureAtlasSprite getParticleTexture() { return particle; }
    @Override public ItemCameraTransforms getItemCameraTransforms() { return ItemCameraTransforms.DEFAULT; }
    @Override public ItemOverrideList getOverrides() { return ItemOverrideList.NONE; }
    @Override public Pair<? extends IBakedModel, Matrix4f> handlePerspective(TransformType type) {
        Matrix4f transform = perspectives.get(type);
        if(transform == null) throw new IllegalStateException("Unvalidated item perspective " + type
                + "; incomplete models must remain behind their registration gate");
        return Pair.of(this, new Matrix4f(transform));
    }
    public boolean hasEveryNativePerspective() {
        for(TransformType type : TransformType.values()) if(!perspectives.containsKey(type)) return false;
        return true;
    }
}
