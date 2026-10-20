package com.hbm.render.block.nativeblock;

import java.util.Collections;
import java.util.List;
import com.hbm.render.block.RenderPedestal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;

/** All faces remain unculled, matching BlockPedestal.shouldSideBeRendered. */
public final class PedestalBakedModel implements IBakedModel {
    private final List<BakedQuad> quads;
    private final TextureAtlasSprite top;
    public PedestalBakedModel(TextureAtlasSprite top, TextureAtlasSprite side, boolean inventory) {
        this.top = top;
        quads = RenderPedestal.bake(top, side, inventory);
    }
    @Override public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long seed) { return side == null ? quads : Collections.emptyList(); }
    @Override public boolean isAmbientOcclusion() { return true; }
    @Override public boolean isGui3d() { return true; }
    @Override public boolean isBuiltInRenderer() { return false; }
    @Override public TextureAtlasSprite getParticleTexture() { return top; }
    @Override public ItemCameraTransforms getItemCameraTransforms() { return ThermalModelTransforms.BLOCK; }
    @Override public ItemOverrideList getOverrides() { return ItemOverrideList.NONE; }
}
