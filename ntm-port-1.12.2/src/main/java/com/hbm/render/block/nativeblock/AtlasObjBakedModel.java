package com.hbm.render.block.nativeblock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;

/** Immutable, unculled atlas geometry whose original shading already resides in vertex colors. */
public final class AtlasObjBakedModel implements IBakedModel {
    private final List<BakedQuad> quads;
    private final TextureAtlasSprite particle;
    public AtlasObjBakedModel(List<BakedQuad> quads, TextureAtlasSprite particle) {
        this.quads = Collections.unmodifiableList(new ArrayList<>(quads));
        this.particle = particle;
    }
    @Override public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long seed) {
        return side == null ? quads : Collections.emptyList();
    }
    @Override public boolean isAmbientOcclusion() { return false; }
    @Override public boolean isGui3d() { return true; }
    @Override public boolean isBuiltInRenderer() { return false; }
    @Override public TextureAtlasSprite getParticleTexture() { return particle; }
    @Override public ItemCameraTransforms getItemCameraTransforms() { return ThermalModelTransforms.BLOCK; }
    @Override public ItemOverrideList getOverrides() { return ItemOverrideList.NONE; }
}
