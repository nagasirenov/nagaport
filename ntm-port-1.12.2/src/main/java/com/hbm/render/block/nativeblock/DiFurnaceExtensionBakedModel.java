package com.hbm.render.block.nativeblock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import com.hbm.render.mesh.ObjMesh;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;

/** The original Top/Bottom/Side OBJ, baked without re-triangulating, contracting UVs or replacing material assignments. */
public final class DiFurnaceExtensionBakedModel implements IBakedModel {
    private final List<BakedQuad> quads;
    private final TextureAtlasSprite particle;
    public DiFurnaceExtensionBakedModel(ObjMesh mesh, boolean inventory, Function<String, TextureAtlasSprite> sprites) {
        List<BakedQuad> result = new ArrayList<>();
        VertexFormat format = inventory ? DefaultVertexFormats.ITEM : DefaultVertexFormats.BLOCK;
        for(String name : new String[] {"Top", "Bottom", "Side"}) {
            ObjMesh.Part selected = null;
            // Original renderPartWithIcon is exact-case and uses the last matching source group.
            for(ObjMesh.Part part : mesh.parts) if(name.equals(part.name)) selected = part;
            if(selected == null) throw new IllegalArgumentException("Missing extension OBJ part " + name);
            String texture = name.equals("Top") ? "difurnace_top_off_alt" : name.equals("Bottom") ? "brick_fire" : "difurnace_extension";
            TextureAtlasSprite sprite = sprites.apply("hbm:blocks/" + texture);
            for(ObjMesh.Face face : selected.faces) {
                UnpackedBakedQuad.Builder builder = new UnpackedBakedQuad.Builder(format);
                builder.setTexture(sprite);
                builder.setQuadTint(-1);
                builder.setQuadOrientation(EnumFacing.getFacingFromVector(face.normal.x, face.normal.y, face.normal.z));
                builder.setApplyDiffuseLighting(inventory);
                builder.setContractUVs(false);
                float shade = inventory ? 1 : Math.max(.45F, face.normal.y * .3F + .7F - Math.abs(face.normal.x) * .1F + Math.abs(face.normal.z) * .1F);
                // Legacy Tessellator setColorOpaque_F truncates to byte components.
                shade = (int)(shade * 255F) / 255F;
                for(int vertex = 0; vertex < 4; vertex++) {
                    ObjMesh.Corner corner = face.corners.get(Math.min(vertex, face.corners.size() - 1));
                    if(corner.uv == null) throw new IllegalArgumentException("Extension OBJ requires UVs");
                    for(int e = 0; e < format.getElementCount(); e++) {
                        VertexFormatElement element = format.getElement(e);
                        switch(element.getUsage()) {
                            case POSITION: builder.put(e, corner.position.x + .5F, corner.position.y, corner.position.z + .5F, 1); break;
                            case COLOR: builder.put(e, shade, shade, shade, 1); break;
                            case UV:
                                if(element.getIndex() == 0) builder.put(e, sprite.getInterpolatedU(corner.uv.x * 16D), sprite.getInterpolatedV(corner.uv.y * 16D), 0, 1);
                                else builder.put(e, 0, 0, 0, 1);
                                break;
                            case NORMAL: builder.put(e, face.normal.x, face.normal.y, face.normal.z, 0); break;
                            default: builder.put(e);
                        }
                    }
                }
                BakedQuad quad = builder.build();
                result.add(inventory ? quad : new BlockPositionLightQuad(quad));
            }
        }
        quads = Collections.unmodifiableList(result);
        particle = sprites.apply("hbm:blocks/brick_fire");
    }
    @Override public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long seed) { return side == null ? quads : Collections.emptyList(); }
    @Override public boolean isAmbientOcclusion() { return false; }
    @Override public boolean isGui3d() { return true; }
    @Override public boolean isBuiltInRenderer() { return false; }
    @Override public TextureAtlasSprite getParticleTexture() { return particle; }
    @Override public ItemCameraTransforms getItemCameraTransforms() { return ThermalModelTransforms.BLOCK; }
    @Override public ItemOverrideList getOverrides() { return ItemOverrideList.NONE; }
}
