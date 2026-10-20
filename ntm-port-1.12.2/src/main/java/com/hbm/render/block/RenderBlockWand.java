package com.hbm.render.block;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import com.hbm.render.block.nativeblock.ConveyorCuboidGeometry;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;

/** Native faces of the original inside-first, outside-second standard-block render. */
public final class RenderBlockWand {
    private RenderBlockWand() { }

    public static List<BakedQuad> bakeFace(TextureAtlasSprite sprite, EnumFacing side) {
        BakedQuad outside = ConveyorCuboidGeometry.face(sprite, false, side, 0,
                1D/16D, 1D/16D, 1D/16D, 15D/16D, 15D/16D, 15D/16D, 0);
        int[] vertices = outside.getVertexData().clone();
        int stride = outside.getFormat().getIntegerSize();
        int axis = side.getAxis() == EnumFacing.Axis.X ? 2 : 0;
        // RenderBlocks.renderFromInside swaps the in-plane X bounds (Y/Z faces)
        // or Z bounds (X faces), not the UVs. Reversing a vertex list would change
        // the original inside texture. Keep the outward normal/diffuse direction:
        // the original two renderStandardBlock calls use the same face lighting.
        for(int vertex = 0; vertex < 4; vertex++) {
            int offset = vertex * stride + axis;
            vertices[offset] = Float.floatToRawIntBits(1F - Float.intBitsToFloat(vertices[offset]));
        }
        BakedQuad inside = new BakedQuad(vertices, outside.getTintIndex(), side, sprite,
                outside.shouldApplyDiffuseLighting(), outside.getFormat());
        return Collections.unmodifiableList(Arrays.asList(inside, outside));
    }
}
