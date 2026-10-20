package com.hbm.render.block.nativeblock;

import java.util.Collections;
import java.util.List;
import com.hbm.blocks.rail.BlockRailNTM;
import com.hbm.render.block.RenderRail;
import com.hbm.render.block.RenderRail.Kind;
import com.hbm.render.mesh.ObjMesh;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;

/** Immutable static geometry; only the original switch sign depends on extended state. */
public final class RailBakedModel implements IBakedModel {
    private final List<BakedQuad>[] variants;
    private final TextureAtlasSprite particle;
    private final boolean dynamic;
    @SuppressWarnings("unchecked")
    public RailBakedModel(ObjMesh mesh, Kind kind, int metadata, boolean inventory, TextureAtlasSprite... sprites) {
        this.particle=sprites[kind==Kind.FUNNEL?2:0];
        dynamic=!inventory&&(kind==Kind.SWITCH||kind==Kind.SWITCH_FLIPPED);
        variants=new List[dynamic?3:1];
        for(int sign=0;sign<variants.length;sign++) variants[sign]=RenderRail.bake(mesh,kind,metadata,inventory,sign,sprites);
    }
    @Override public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long seed) {
        if(side!=null) return Collections.emptyList();
        return variants[dynamic?BlockRailNTM.switchSign(state):0];
    }
    @Override public boolean isAmbientOcclusion() { return false; }
    @Override public boolean isGui3d() { return true; }
    @Override public boolean isBuiltInRenderer() { return false; }
    @Override public TextureAtlasSprite getParticleTexture() { return particle; }
    @Override public ItemCameraTransforms getItemCameraTransforms() { return ThermalModelTransforms.BLOCK; }
    @Override public ItemOverrideList getOverrides() { return ItemOverrideList.NONE; }
}
