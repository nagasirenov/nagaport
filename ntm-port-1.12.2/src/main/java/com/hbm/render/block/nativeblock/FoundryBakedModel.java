package com.hbm.render.block.nativeblock;

import java.util.*;
import com.hbm.blocks.machine.FoundryModelState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;

/** Native unculled housing; only dynamic molten faces use the separate exact-light TESR. */
public final class FoundryBakedModel implements IBakedModel {
    private final FoundryGeometry.Kind kind;private final int metadata;private final boolean inventory;private final FoundryMaterials materials;
    private final List<BakedQuad> item;
    public FoundryBakedModel(FoundryGeometry.Kind kind,int metadata,boolean inventory,FoundryMaterials materials) {
        this.kind=kind;this.metadata=metadata;this.inventory=inventory;this.materials=materials;
        item=inventory?FoundryGeometry.build(kind,true,false,metadata,null,materials):null;
    }
    @Override public List<BakedQuad> getQuads(IBlockState state,EnumFacing side,long seed) {
        if(side!=null)return Collections.emptyList();
        return inventory?item:FoundryGeometry.build(kind,false,false,metadata,FoundryModelState.get(state),materials);
    }
    @Override public boolean isAmbientOcclusion() { return false; }
    @Override public boolean isGui3d() { return true; }
    @Override public boolean isBuiltInRenderer() { return false; }
    @Override public TextureAtlasSprite getParticleTexture() { return materials.get("Particle"); }
    @Override public ItemCameraTransforms getItemCameraTransforms() { return ThermalModelTransforms.BLOCK; }
    @Override public ItemOverrideList getOverrides() { return ItemOverrideList.NONE; }
}
