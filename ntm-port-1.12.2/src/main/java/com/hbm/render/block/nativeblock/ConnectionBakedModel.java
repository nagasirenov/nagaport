package com.hbm.render.block.nativeblock;

import java.util.Collections;
import java.util.List;
import com.hbm.blocks.network.ConnectionState;
import com.hbm.render.block.RenderCable;
import com.hbm.render.block.RenderCableClassic;
import com.hbm.render.mesh.ObjMesh;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.property.IExtendedBlockState;

/** Immutable connection variants rebuilt when the native resource/model loader reloads. */
public final class ConnectionBakedModel implements IBakedModel {
    private final List<BakedQuad>[] variants;
    private final boolean inventory;
    private final TextureAtlasSprite sprite;
    @SuppressWarnings("unchecked")
    public ConnectionBakedModel(ObjMesh mesh, boolean classic, boolean inventory, TextureAtlasSprite sprite) {
        this.inventory = inventory; this.sprite = sprite;
        variants = (List<BakedQuad>[])new List<?>[inventory ? 1 : 64];
        for(int mask = 0; mask < variants.length; mask++) variants[mask] = classic ? RenderCableClassic.bake(mask, inventory, sprite)
                : RenderCable.bake(mesh, mask, inventory, sprite);
    }
    @Override public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long seed) {
        if(side != null) return Collections.emptyList();
        Integer mask = !inventory && state instanceof IExtendedBlockState ? ((IExtendedBlockState)state).getValue(ConnectionState.CONNECTIONS) : null;
        return variants[inventory || mask == null ? 0 : mask];
    }
    @Override public boolean isAmbientOcclusion() { return false; }
    @Override public boolean isGui3d() { return true; }
    @Override public boolean isBuiltInRenderer() { return false; }
    @Override public TextureAtlasSprite getParticleTexture() { return sprite; }
    @Override public ItemCameraTransforms getItemCameraTransforms() { return ThermalModelTransforms.BLOCK; }
    @Override public ItemOverrideList getOverrides() { return ItemOverrideList.NONE; }
}
