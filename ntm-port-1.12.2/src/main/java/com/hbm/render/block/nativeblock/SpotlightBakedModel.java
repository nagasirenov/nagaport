package com.hbm.render.block.nativeblock;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.hbm.blocks.BlockEnums.LightType;
import com.hbm.blocks.machine.SpotlightModelState;
import com.hbm.render.block.RenderLight;
import com.hbm.render.mesh.ObjMesh;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;

/** Flat, unculled lamp mesh with the original block-position light and unshaded color. */
public final class SpotlightBakedModel implements IBakedModel {
    private final ObjMesh mesh;
    private final LightType type;
    private final boolean modular, inventory;
    private final int metadata;
    private final TextureAtlasSprite sprite;
    private final List<BakedQuad> fixed;
    private final Map<Integer,List<BakedQuad>> connected = new ConcurrentHashMap<>();
    public SpotlightBakedModel(ObjMesh mesh, LightType type, boolean modular, int metadata,
            boolean inventory, TextureAtlasSprite sprite) {
        this.mesh = mesh; this.type = type; this.modular = modular; this.metadata = metadata;
        this.inventory = inventory; this.sprite = sprite;
        fixed = !modular || inventory ? RenderLight.bake(mesh,type,modular,metadata,0,inventory,sprite) : null;
    }
    @Override public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long seed) {
        if(side != null) return Collections.emptyList();
        if(fixed != null) return fixed;
        int connections = SpotlightModelState.connections(state);
        return connected.computeIfAbsent(connections, mask -> RenderLight.bake(mesh,type,modular,metadata,mask,inventory,sprite));
    }
    @Override public boolean isAmbientOcclusion() { return false; }
    @Override public boolean isGui3d() { return true; }
    @Override public boolean isBuiltInRenderer() { return false; }
    @Override public TextureAtlasSprite getParticleTexture() { return sprite; }
    @Override public ItemCameraTransforms getItemCameraTransforms() { return ThermalModelTransforms.BLOCK; }
    @Override public ItemOverrideList getOverrides() { return ItemOverrideList.NONE; }
}
