package com.hbm.render.block.nativeblock;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.hbm.blocks.network.ConnectionState;
import com.hbm.blocks.network.FluidConnectionState;
import com.hbm.render.block.RenderTestPipe;
import com.hbm.render.mesh.ObjMesh;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.property.IExtendedBlockState;

/** Standard duct geometry, including its separately colored, coplanar atlas overlay. */
public final class FluidDuctBakedModel implements IBakedModel {
    private final ObjMesh mesh;
    private final TextureAtlasSprite base, overlay;
    private final List<BakedQuad> inventory;
    private final Map<Long, List<BakedQuad>> variants = new LinkedHashMap<Long, List<BakedQuad>>(128, .75F, true) {
        @Override protected boolean removeEldestEntry(Map.Entry<Long, List<BakedQuad>> entry) { return size() > 128; }
    };
    public FluidDuctBakedModel(ObjMesh mesh, int metadata, boolean inventory, TextureAtlasSprite base,
            TextureAtlasSprite overlay, int inventoryColor) {
        if(metadata < 0 || metadata > 15) throw new IllegalArgumentException("Invalid standard duct metadata " + metadata);
        this.mesh = mesh; this.base = base; this.overlay = overlay;
        this.inventory = inventory ? RenderTestPipe.bake(mesh, 0, true, inventoryColor, base, overlay) : null;
    }
    @Override public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long seed) {
        if(side != null) return Collections.emptyList();
        if(inventory != null) return inventory;
        int mask = 0, color = 0xff00ff;
        if(state instanceof IExtendedBlockState) {
            IExtendedBlockState extended = (IExtendedBlockState)state;
            Integer connections = extended.getValue(ConnectionState.CONNECTIONS);
            Integer fluidColor = extended.getValue(FluidConnectionState.FLUID_COLOR);
            if(connections != null) mask = connections;
            if(fluidColor != null) color = fluidColor;
        }
        long key = ((long)mask << 32) | (color & 0xffffffffL);
        synchronized(variants) {
            List<BakedQuad> quads = variants.get(key);
            if(quads == null) {
                quads = RenderTestPipe.bake(mesh, mask, false, color, base, overlay);
                variants.put(key, quads);
            }
            return quads;
        }
    }
    @Override public boolean isAmbientOcclusion() { return false; }
    @Override public boolean isGui3d() { return true; }
    @Override public boolean isBuiltInRenderer() { return false; }
    @Override public TextureAtlasSprite getParticleTexture() { return base; }
    @Override public ItemCameraTransforms getItemCameraTransforms() { return ThermalModelTransforms.BLOCK; }
    @Override public ItemOverrideList getOverrides() { return ItemOverrideList.NONE; }
}
