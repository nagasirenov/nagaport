package com.hbm.render.block.nativeblock;

import java.util.*;
import com.hbm.blocks.generic.BlockChain;
import com.hbm.blocks.generic.BlockMetalFence;
import com.hbm.render.block.RenderChain;
import com.hbm.render.block.RenderFence;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;

/** State-selected native models for the original chain and metal fence. */
public final class StructuralBakedModel implements IBakedModel {
    private final boolean chain;
    private final TextureAtlasSprite material;
    private final List<BakedQuad>[][] quads;
    public StructuralBakedModel(boolean chain, TextureAtlasSprite material, TextureAtlasSprite secondary) {
        this(chain, false, material, secondary);
    }
    @SuppressWarnings("unchecked")
    public StructuralBakedModel(boolean chain, boolean postParticle, TextureAtlasSprite material, TextureAtlasSprite secondary) {
        this.chain = chain; this.material = postParticle ? secondary : material;
        quads = new List[16][chain ? 2 : 16];
        for(int meta = 0; meta < 16; meta++) for(int mask = 0; mask < quads[meta].length; mask++)
            quads[meta][mask] = chain ? RenderChain.bake(meta, mask == 0 ? material : secondary)
                    : RenderFence.bake(meta, (mask & 1) != 0, (mask & 2) != 0, (mask & 4) != 0, (mask & 8) != 0, material, secondary);
    }
    @Override public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long seed) {
        if(side != null || state == null) return Collections.emptyList();
        if(chain) return quads[state.getValue(BlockChain.META)][state.getValue(BlockChain.END) ? 1 : 0];
        int mask = (state.getValue(BlockMetalFence.WEST) ? 1 : 0) | (state.getValue(BlockMetalFence.EAST) ? 2 : 0)
                | (state.getValue(BlockMetalFence.NORTH) ? 4 : 0) | (state.getValue(BlockMetalFence.SOUTH) ? 8 : 0);
        return quads[state.getValue(BlockMetalFence.META)][mask];
    }
    @Override public boolean isAmbientOcclusion() { return !chain; }
    @Override public boolean isGui3d() { return false; }
    @Override public boolean isBuiltInRenderer() { return false; }
    @Override public TextureAtlasSprite getParticleTexture() { return material; }
    @Override public ItemCameraTransforms getItemCameraTransforms() { return ItemCameraTransforms.DEFAULT; }
    @Override public ItemOverrideList getOverrides() { return ItemOverrideList.NONE; }
}
