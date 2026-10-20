package com.hbm.render.block.nativeblock;

import java.util.*;
import com.hbm.blocks.machine.rbmk.RBMKRenderState;
import com.hbm.blocks.machine.rbmk.RBMKRenderState.*;
import com.hbm.render.block.*;
import com.hbm.render.block.nativeblock.RBMKGeometry.Part;
import com.hbm.render.mesh.ObjMesh;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;

/** Immutable per-reload inventory/lid/pipe geometry; neighborhood masks remain per render invocation. */
public final class RBMKBakedModel implements IBakedModel {
    private final List<Part>[] variants;
    private final boolean item;
    private final Profile profile;
    private final TextureAtlasSprite particle;
    @SuppressWarnings("unchecked")
    public RBMKBakedModel(RBMKMaterials materials, ObjMesh element, ObjMesh rods, ObjMesh control, boolean item) {
        this.item = item; this.profile = materials.profile; this.particle = materials.face(2, 0, false);
        variants = (List<Part>[])new List<?>[item ? 1 : 4];
        for(int variant = 0; variant < variants.length; variant++) {
            int lid = variant < 2 ? 0 : variant - 1;
            switch(profile.family) {
                case ROD: variants[variant] = RenderRBMKRod.bake(materials, element, rods, item, lid); break;
                case PASSIVE: variants[variant] = RenderRBMKReflector.bake(materials, item, lid); break;
                case CONTROL: variants[variant] = RenderRBMKControl.bake(materials, control, item, lid, variant != 0); break;
                default: throw new AssertionError(profile.family);
            }
        }
    }
    @Override public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long seed) {
        return side == null ? select(state, null) : Collections.emptyList();
    }
    /**
     * Original mixed-material damage algorithm, NOT a registered damage-dispatch hook.
     * Native default getDamageModel still indiscriminately retextures Inner and loses contexts.
     * This explicit callable is retained/tested for a later tileless damage-dispatch integration.
     */
    public List<BakedQuad> getBreakingQuads(IBlockState state, TextureAtlasSprite crack) {
        if(item) throw new IllegalStateException("World breaking geometry required");
        return select(state, Objects.requireNonNull(crack));
    }
    private List<BakedQuad> select(IBlockState state, TextureAtlasSprite crack) {
        Snapshot snapshot = item ? null : RBMKRenderState.snapshot(state);
        int variant = item || !snapshot.extra() ? 0 : profile.ownLid || snapshot.lid == 0 ? 1 : snapshot.lid + 1;
        List<BakedQuad> result = new ArrayList<>();
        for(Part part : variants[variant]) {
            if(!item && part.face >= 0 && ((part.yShift == 0 ? snapshot.bodyFaces : snapshot.upperFaces) & 1 << part.face) == 0) continue;
            BakedQuad quad = crack != null && part.cracks ? new BakedQuadRetextured(part.quad, crack) : part.quad;
            result.add(item || !part.obj && part.yShift == 0 ? quad : new RBMKContextQuad(quad, snapshot, part.yShift, part.obj));
        }
        return Collections.unmodifiableList(result);
    }
    @Override public boolean isAmbientOcclusion() { return true; }
    @Override public boolean isGui3d() { return true; }
    @Override public boolean isBuiltInRenderer() { return false; }
    @Override public TextureAtlasSprite getParticleTexture() { return particle; }
    @Override public ItemCameraTransforms getItemCameraTransforms() { return ThermalModelTransforms.BLOCK; }
    @Override public ItemOverrideList getOverrides() { return ItemOverrideList.NONE; }
}
