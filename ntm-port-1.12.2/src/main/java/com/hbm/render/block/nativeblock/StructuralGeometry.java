package com.hbm.render.block.nativeblock;

import java.util.List;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;

/** Geometry/material rules specific to metal fence and chain. */
public final class StructuralGeometry {
    private StructuralGeometry() { }
    public static void fenceBox(List<BakedQuad> output, TextureAtlasSprite sprite,
            double x0, double y0, double z0, double x1, double y1, double z1) {
        for(EnumFacing face : EnumFacing.values()) {
            BakedQuad source = ConveyorCuboidGeometry.face(sprite, false, face, 0, x0, y0, z0, x1, y1, z1, 0);
            // RenderFence set field_152631_f for all its boxes: the old negative-Z
            // and positive-X faces mirror the entire UV interval, not only bounds.
            if(face == EnumFacing.NORTH || face == EnumFacing.EAST) {
                int[] data = source.getVertexData().clone();
                int stride = source.getFormat().getIntegerSize(), uv = source.getFormat().getUvOffsetById(0) / 4;
                float shift = (float)(face == EnumFacing.NORTH ? 1 - x0 - x1 : 1 - z0 - z1) * (sprite.getMaxU() - sprite.getMinU());
                for(int i = 0; i < 4; i++) data[i * stride + uv] = Float.floatToRawIntBits(Float.intBitsToFloat(data[i * stride + uv]) + shift);
                source = new BakedQuad(data, -1, face, sprite, true, source.getFormat());
            }
            output.add(source);
        }
    }
    public static BakedQuad chainFace(TextureAtlasSprite sprite, List<double[]> vertices) {
        VertexFormat format = DefaultVertexFormats.ITEM;
        UnpackedBakedQuad.Builder builder = new UnpackedBakedQuad.Builder(format);
        builder.setTexture(sprite); builder.setQuadTint(-1); builder.setQuadOrientation(EnumFacing.UP);
        builder.setApplyDiffuseLighting(false); builder.setContractUVs(false);
        for(double[] vertex : vertices) for(int e = 0; e < format.getElementCount(); e++) {
            VertexFormatElement element = format.getElement(e);
            switch(element.getUsage()) {
                case POSITION: builder.put(e, (float)vertex[0], (float)vertex[1], (float)vertex[2], 1); break;
                case COLOR: builder.put(e, 1, 1, 1, 1); break;
                case UV: builder.put(e, (float)vertex[3], (float)vertex[4], 0, 1); break;
                case NORMAL: builder.put(e, 0, 0, 0, 0); break;
                default: builder.put(e);
            }
        }
        return new BlockPositionLightQuad(builder.build());
    }
}
