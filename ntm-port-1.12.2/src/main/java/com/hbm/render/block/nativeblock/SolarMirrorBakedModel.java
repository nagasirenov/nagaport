package com.hbm.render.block.nativeblock;

import java.util.Collections;
import java.util.List;
import com.hbm.blocks.machine.SolarMirrorModelState;
import com.hbm.blocks.machine.SolarMirrorModelState.Snapshot;
import com.hbm.render.block.RenderMirror;
import com.hbm.render.mesh.ObjMesh;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;

/** Target geometry is transient, not retained in a cache keyed by unbounded world coordinates. */
public final class SolarMirrorBakedModel implements IBakedModel {
    private final ObjMesh mesh;
    private final TextureAtlasSprite sprite;
    private final List<BakedQuad> idle;
    public SolarMirrorBakedModel(ObjMesh mesh, TextureAtlasSprite sprite) {
        this.mesh = mesh; this.sprite = sprite;
        this.idle = RenderMirror.bake(mesh, sprite, new Snapshot(true, false, 0, 0, 0));
    }
    @Override public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long seed) {
        if(side != null) return Collections.emptyList();
        Snapshot snapshot = SolarMirrorModelState.snapshot(state);
        if(!snapshot.available) return Collections.emptyList();
        return snapshot.pitched ? RenderMirror.bake(mesh, sprite, snapshot) : idle;
    }
    @Override public boolean isAmbientOcclusion() { return false; }
    @Override public boolean isGui3d() { return false; }
    @Override public boolean isBuiltInRenderer() { return false; }
    @Override public TextureAtlasSprite getParticleTexture() { return sprite; }
    @Override public ItemCameraTransforms getItemCameraTransforms() { return ItemCameraTransforms.DEFAULT; }
    @Override public ItemOverrideList getOverrides() { return ItemOverrideList.NONE; }
}
