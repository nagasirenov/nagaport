package com.hbm.render.block.nativeblock;

import java.util.Collections;
import java.util.List;
import com.hbm.blocks.generic.BlockSteelDecoration;
import com.hbm.render.block.RenderSteelCorner;
import com.hbm.render.block.RenderSteelWall;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;

/** Native standard-block AO and diffuse shading, with the original all-face visibility. */
public final class SteelDecorationBakedModel implements IBakedModel {
    private final TextureAtlasSprite sprite;
    private final List<BakedQuad> quads;
    public SteelDecorationBakedModel(boolean corner, int metadata, boolean inventory, TextureAtlasSprite sprite) {
        this.sprite = sprite;
        quads = !corner && !inventory && (metadata < 2 || metadata > 5) ? null : corner ? RenderSteelCorner.bake(metadata, inventory, sprite) : RenderSteelWall.bake(metadata, inventory, sprite);
    }
    @Override public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long seed) { return side != null ? Collections.emptyList() : quads != null ? quads : RenderSteelWall.bake(BlockSteelDecoration.modelBounds(state), false, sprite); }
    @Override public boolean isAmbientOcclusion() { return true; }
    @Override public boolean isGui3d() { return true; }
    @Override public boolean isBuiltInRenderer() { return false; }
    @Override public TextureAtlasSprite getParticleTexture() { return sprite; }
    @Override public ItemCameraTransforms getItemCameraTransforms() { return ThermalModelTransforms.BLOCK; }
    @Override public ItemOverrideList getOverrides() { return ItemOverrideList.NONE; }
}
