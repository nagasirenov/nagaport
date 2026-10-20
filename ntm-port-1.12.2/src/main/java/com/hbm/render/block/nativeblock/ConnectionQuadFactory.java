package com.hbm.render.block.nativeblock;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;

/** Native atlas quad construction for the original cable meshes and authored arm templates. */
public final class ConnectionQuadFactory {
    private ConnectionQuadFactory() { }
    /** Four ordered vertices, each XYZ and uncontracted pixel-space UV. */
    public static BakedQuad quad(TextureAtlasSprite sprite, boolean inventory, float shade,
            float nx, float ny, float nz, double[] vertices) {
        return coloredQuad(sprite, inventory, shade, 0xffffff, nx, ny, nz, vertices);
    }
    /** Original OBJ tint multiplies each byte channel before truncation, not afterward. */
    public static BakedQuad coloredQuad(TextureAtlasSprite sprite, boolean inventory, float shade, int color,
            float nx, float ny, float nz, double[] vertices) {
        if(vertices.length != 20) throw new IllegalArgumentException("Cable quads require four XYZUV vertices");
        VertexFormat format = inventory ? DefaultVertexFormats.ITEM : DefaultVertexFormats.BLOCK;
        UnpackedBakedQuad.Builder builder = new UnpackedBakedQuad.Builder(format);
        builder.setTexture(sprite);
        builder.setQuadTint(-1);
        builder.setQuadOrientation(EnumFacing.getFacingFromVector(nx, ny, nz));
        builder.setApplyDiffuseLighting(inventory);
        builder.setContractUVs(false);
        float red = channel((color >> 16) & 255, shade);
        float green = channel((color >> 8) & 255, shade);
        float blue = channel(color & 255, shade);
        for(int v = 0; v < 4; v++) for(int e = 0; e < format.getElementCount(); e++) {
            VertexFormatElement element = format.getElement(e);
            switch(element.getUsage()) {
                case POSITION: builder.put(e, (float)vertices[v * 5], (float)vertices[v * 5 + 1], (float)vertices[v * 5 + 2], 1); break;
                case COLOR: builder.put(e, red, green, blue, 1); break;
                case UV:
                    if(element.getIndex() == 0) builder.put(e, sprite.getInterpolatedU(vertices[v * 5 + 3]), sprite.getInterpolatedV(vertices[v * 5 + 4]), 0, 1);
                    else builder.put(e, 0, 0, 0, 1);
                    break;
                case NORMAL:
                    // The original item Tessellator truncated normals to signed bytes;
                    // Forge's native packer rounds, so submit those already-quantized values.
                    builder.put(e, ((byte)(nx * 127F)) / 127F, ((byte)(ny * 127F)) / 127F, ((byte)(nz * 127F)) / 127F, 0);
                    break;
                default: builder.put(e);
            }
        }
        BakedQuad result = builder.build();
        return inventory ? result : new BlockPositionLightQuad(result);
    }
    private static float channel(int component, float shade) {
        // Original Tessellator setColorRGBA saturated out-of-range channels.
        return Math.max(0, Math.min(255, (int)(component * shade))) / 255F;
    }
}
