package com.hbm.render.block.nativeblock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.hbm.blocks.generic.BlockGrate;
import com.hbm.blocks.generic.BlockGrate.RenderSnapshot;
import com.hbm.render.block.RenderGrate;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;

/** World-only model. Items retain the original flat top texture through ItemLayerModel. */
public final class GrateBakedModel implements IBakedModel {
    private final TextureAtlasSprite top;
    private final List<BakedQuad> normalized;
    private final int shift;
    public GrateBakedModel(int metadata, TextureAtlasSprite top, TextureAtlasSprite side) {
        this.top = top;
        this.shift = BlockGrate.renderShift(metadata);
        this.normalized = RenderGrate.bake(metadata, top, side);
    }
    @Override public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long seed) {
        if(side != null) return Collections.emptyList();
        RenderSnapshot snapshot = BlockGrate.renderSnapshot(state);
        List<BakedQuad> quads = new ArrayList<>();
        for(BakedQuad quad : normalized) if((snapshot.visible & 1 << quad.getFace().getIndex()) != 0)
            quads.add(shift == 0 ? quad : new GrateShiftedLightQuad(quad, snapshot, shift));
        return Collections.unmodifiableList(quads);
    }
    @Override public boolean isAmbientOcclusion() { return true; }
    @Override public boolean isGui3d() { return false; }
    @Override public boolean isBuiltInRenderer() { return false; }
    @Override public TextureAtlasSprite getParticleTexture() { return top; }
    @Override public ItemCameraTransforms getItemCameraTransforms() { return ItemCameraTransforms.DEFAULT; }
    @Override public ItemOverrideList getOverrides() { return ItemOverrideList.NONE; }
}
