package com.hbm.render.block.nativeblock;

import java.util.Collections;
import java.util.List;
import javax.vecmath.Matrix4f;
import org.apache.commons.lang3.tuple.Pair;
import com.hbm.render.block.RenderCRT;
import com.hbm.render.item.nativeitem.ItemTransformMatrix;
import com.hbm.render.mesh.ObjMesh;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;

/** No AO or second diffuse factor: original part shading already resides in vertex colors. */
public final class CRTBakedModel implements IBakedModel {
    private static final Matrix4f FIXED = new ItemTransformMatrix().scale(2).translate(0, -.06, 0)
            .rotateY(180).scale(1.25).translate(0, .05, 0).rotateY(-90).scale(.25).build();
    private final TextureAtlasSprite sprite;
    private final List<BakedQuad> quads;
    private final boolean inventory;
    public CRTBakedModel(ObjMesh mesh, int metadata, boolean inventory, TextureAtlasSprite sprite) {
        this.sprite = sprite;
        this.inventory = inventory;
        this.quads = RenderCRT.bake(mesh, metadata, inventory, sprite);
    }
    @Override public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long seed) {
        return side == null ? quads : Collections.emptyList();
    }
    @Override public boolean isAmbientOcclusion() { return false; }
    @Override public boolean isGui3d() { return true; }
    @Override public boolean isBuiltInRenderer() { return false; }
    @Override public TextureAtlasSprite getParticleTexture() { return sprite; }
    @Override public ItemCameraTransforms getItemCameraTransforms() { return ThermalModelTransforms.BLOCK; }
    @Override public ItemOverrideList getOverrides() { return ItemOverrideList.NONE; }
    @Override public Pair<? extends IBakedModel, Matrix4f> handlePerspective(TransformType type) {
        // Ordinary item-frame uses original block helper .25 scale, not wand .5.
        // The matching event supplies the old frame's off-center rotation pivot.
        return inventory && type == TransformType.FIXED ? Pair.of(this, new Matrix4f(FIXED)) : IBakedModel.super.handlePerspective(type);
    }
}
