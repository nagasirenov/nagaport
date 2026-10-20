package com.hbm.render.block.nativeblock;
import java.util.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
public final class FallingTintBakedModel implements IBakedModel {
 private final List<BakedQuad> quads; private final TextureAtlasSprite sprite;
 public FallingTintBakedModel(List<BakedQuad> quads, TextureAtlasSprite sprite) { this.quads=Collections.unmodifiableList(new ArrayList<>(quads));this.sprite=sprite; }
 public List<BakedQuad> getQuads(IBlockState state, EnumFacing side,long seed) { return side==null?Collections.emptyList():Collections.singletonList(quads.get(side.getIndex())); }
 public boolean isAmbientOcclusion(){return true;} public boolean isGui3d(){return true;} public boolean isBuiltInRenderer(){return false;}
 public TextureAtlasSprite getParticleTexture(){return sprite;} public ItemCameraTransforms getItemCameraTransforms(){return ThermalModelTransforms.BLOCK;} public ItemOverrideList getOverrides(){return ItemOverrideList.NONE;}
}
