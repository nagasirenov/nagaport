package com.hbm.render.block.nativeblock;

import java.util.Collections;
import java.util.List;
import com.hbm.blocks.network.RadioTorchModelState;
import com.hbm.render.block.RenderRTTY;
import com.hbm.render.mesh.ObjMesh;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.property.IExtendedBlockState;

/** Two immutable variants per metadata. No world or tile references escape into model caches. */
public final class RadioTorchBakedModel implements IBakedModel {
    private final List<BakedQuad> off, on;
    private final TextureAtlasSprite particle;
    public RadioTorchBakedModel(ObjMesh mesh, int metadata, TextureAtlasSprite off, TextureAtlasSprite on) {
        this.particle = off;
        this.off = RenderRTTY.bake(mesh, metadata, off);
        this.on = off == on ? this.off : RenderRTTY.bake(mesh, metadata, on);
    }
    @Override public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long seed) {
        if(side != null) return Collections.emptyList();
        Boolean active = state instanceof IExtendedBlockState ? ((IExtendedBlockState)state).getValue(RadioTorchModelState.ACTIVE) : null;
        return Boolean.TRUE.equals(active) ? on : off;
    }
    @Override public boolean isAmbientOcclusion() { return false; }
    @Override public boolean isGui3d() { return false; }
    @Override public boolean isBuiltInRenderer() { return false; }
    @Override public TextureAtlasSprite getParticleTexture() { return particle; }
    @Override public ItemCameraTransforms getItemCameraTransforms() { return ItemCameraTransforms.DEFAULT; }
    @Override public ItemOverrideList getOverrides() { return ItemOverrideList.NONE; }
}
