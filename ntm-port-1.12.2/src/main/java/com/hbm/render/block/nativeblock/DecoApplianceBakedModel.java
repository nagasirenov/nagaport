package com.hbm.render.block.nativeblock;
import java.util.*;
import com.hbm.render.block.RenderToaster;
import com.hbm.render.block.RenderBlockDecoModel;
import com.hbm.render.mesh.ObjMesh;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
public final class DecoApplianceBakedModel implements IBakedModel {
    private final TextureAtlasSprite sprite;
    private final List<BakedQuad> quads;
    public DecoApplianceBakedModel(ObjMesh mesh, boolean computer, int metadata, boolean inventory, TextureAtlasSprite sprite) {
        this.sprite=sprite;
        quads=computer?RenderBlockDecoModel.bake(mesh,metadata,inventory,sprite):RenderToaster.bake(mesh,metadata,inventory,sprite);
    }
    @Override public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long seed) { return side==null?quads:Collections.emptyList(); }
    @Override public boolean isAmbientOcclusion() { return false; }
    @Override public boolean isGui3d() { return true; }
    @Override public boolean isBuiltInRenderer() { return false; }
    @Override public TextureAtlasSprite getParticleTexture() { return sprite; }
    @Override public ItemCameraTransforms getItemCameraTransforms() { return ThermalModelTransforms.BLOCK; }
    @Override public ItemOverrideList getOverrides() { return ItemOverrideList.NONE; }
}
