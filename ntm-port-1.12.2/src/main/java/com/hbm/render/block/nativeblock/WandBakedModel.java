package com.hbm.render.block.nativeblock;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import com.hbm.render.block.RenderBlockWand;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;

/** Native side buckets preserve the marker's same-owner-only face suppression. */
public final class WandBakedModel implements IBakedModel {
    private final TextureAtlasSprite sprite;
    private final Map<EnumFacing, List<BakedQuad>> faces = new EnumMap<>(EnumFacing.class);
    public WandBakedModel(TextureAtlasSprite sprite) {
        this.sprite = sprite;
        for(EnumFacing side : EnumFacing.values()) faces.put(side, RenderBlockWand.bakeFace(sprite, side));
    }
    @Override public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long seed) { return side == null ? Collections.emptyList() : faces.get(side); }
    @Override public boolean isAmbientOcclusion() { return true; }
    @Override public boolean isGui3d() { return false; }
    @Override public boolean isBuiltInRenderer() { return false; }
    @Override public TextureAtlasSprite getParticleTexture() { return sprite; }
    @Override public ItemCameraTransforms getItemCameraTransforms() { return ItemCameraTransforms.DEFAULT; }
    @Override public ItemOverrideList getOverrides() { return ItemOverrideList.NONE; }
}
