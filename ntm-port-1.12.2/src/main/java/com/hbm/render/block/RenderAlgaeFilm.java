package com.hbm.render.block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.hbm.render.mesh.ObjMesh;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;

/** Native cached quads from the original algae OBJ, retaining ObjUtil's face shading. */
public final class RenderAlgaeFilm implements IBakedModel {
    private final List<BakedQuad> quads;
    private final TextureAtlasSprite sprite;
    private final ItemCameraTransforms transforms;

    public RenderAlgaeFilm(ObjMesh mesh, TextureAtlasSprite sprite, int meta, boolean inventory, ItemCameraTransforms transforms) {
        this.sprite = sprite;
        this.transforms = transforms;
        List<BakedQuad> result = new ArrayList<>();
        com.hbm.render.block.nativeblock.AtlasObjGeometry.appendWhole(result, mesh, sprite,
                inventory, rotation(meta), 0F, 1D, 0.5D, 0D, 0.5D);
        quads = Collections.unmodifiableList(result);
    }
    public static float rotation(int meta) {
        return meta == 4 ? 90F / 180F * (float)Math.PI : meta == 5 ? 270F / 180F * (float)Math.PI : meta == 3 ? (float)Math.PI : 0F;
    }
    @Override public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) { return side == null ? quads : Collections.<BakedQuad>emptyList(); }
    @Override public boolean isAmbientOcclusion() { return false; }
    @Override public boolean isGui3d() { return true; }
    @Override public boolean isBuiltInRenderer() { return false; }
    @Override public TextureAtlasSprite getParticleTexture() { return sprite; }
    @Override public ItemCameraTransforms getItemCameraTransforms() { return transforms; }
    @Override public ItemOverrideList getOverrides() { return ItemOverrideList.NONE; }
}
