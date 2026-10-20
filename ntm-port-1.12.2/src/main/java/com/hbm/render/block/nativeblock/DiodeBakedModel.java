package com.hbm.render.block.nativeblock;

import java.util.*;
import com.hbm.blocks.network.ConnectionState;
import com.hbm.render.block.RenderDiode;
import com.hbm.render.mesh.ObjMesh;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.BlockRenderLayer;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.property.IExtendedBlockState;

/** Fixed metadata and immutable contextual connection values; bounded per-reload geometry. */
public final class DiodeBakedModel implements IBakedModel {
    private final List<BakedQuad>[] variants;
    private final boolean inventory;
    private final TextureAtlasSprite particle;
    @SuppressWarnings("unchecked")
    public DiodeBakedModel(ObjMesh mesh, int metadata, boolean inventory, TextureAtlasSprite[] sprites) {
        this.inventory = inventory; particle = sprites[0];
        variants = (List<BakedQuad>[])new List<?>[inventory ? 1 : 64];
        for(int mask = 0; mask < variants.length; mask++) variants[mask] = RenderDiode.bake(mesh, metadata, mask, inventory, sprites);
    }
    @Override public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long seed) {
        if(side != null) return Collections.emptyList();
        Integer mask = !inventory && state instanceof IExtendedBlockState ? ((IExtendedBlockState)state).getValue(ConnectionState.CONNECTIONS) : null;
        List<BakedQuad> quads = variants[inventory || mask == null ? 0 : mask];
        if(inventory) return quads;
        BlockRenderLayer layer = MinecraftForgeClient.getRenderLayer();
        if(layer == null) return quads; // Inventory-like inspection outside a chunk pass.
        if(layer == BlockRenderLayer.SOLID) return quads.subList(0, 12);
        if(layer == BlockRenderLayer.CUTOUT_MIPPED) return quads.subList(12, quads.size());
        return Collections.emptyList();
    }
    /** Explicit full geometry for the dedicated native breaking renderer, independent of chunk pass. */
    public List<BakedQuad> getAllQuads(IBlockState state) {
        Integer mask = !inventory && state instanceof IExtendedBlockState ? ((IExtendedBlockState)state).getValue(ConnectionState.CONNECTIONS) : null;
        return variants[inventory || mask == null ? 0 : mask];
    }
    @Override public boolean isAmbientOcclusion() { return inventory || MinecraftForgeClient.getRenderLayer() != BlockRenderLayer.CUTOUT_MIPPED; }
    @Override public boolean isGui3d() { return true; }
    @Override public boolean isBuiltInRenderer() { return false; }
    @Override public TextureAtlasSprite getParticleTexture() { return particle; }
    @Override public ItemCameraTransforms getItemCameraTransforms() { return ThermalModelTransforms.BLOCK; }
    @Override public ItemOverrideList getOverrides() { return ItemOverrideList.NONE; }
}
