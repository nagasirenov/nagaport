package com.hbm.render.block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.hbm.blocks.generic.BlockSteelDecoration;
import com.hbm.render.block.nativeblock.ConveyorCuboidGeometry;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;

/** Standard-cuboid UVs, including the original within-bounds north/east mirroring. */
public final class RenderSteelWall {
    private RenderSteelWall() { }
    public static List<BakedQuad> bake(int metadata, boolean inventory, TextureAtlasSprite sprite) {
        AxisAlignedBB box = BlockSteelDecoration.wallBounds(inventory ? 2 : metadata);
        return bake(box, inventory, sprite);
    }
    public static List<BakedQuad> bake(AxisAlignedBB box, boolean inventory, TextureAtlasSprite sprite) {
        List<BakedQuad> quads = new ArrayList<>();
        for(EnumFacing face : EnumFacing.values()) {
            BakedQuad quad = ConveyorCuboidGeometry.face(sprite, inventory, face, 0,
                    box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, 0);
            if(inventory) {
                // Original item translation was (-.5,-.5,-.9375), not the corner's block center.
                int[] data = quad.getVertexData().clone();
                int stride = quad.getFormat().getIntegerSize();
                for(int i = 0; i < 4; i++) data[i * stride + 2] = Float.floatToRawIntBits(Float.intBitsToFloat(data[i * stride + 2]) - .4375F);
                quad = new BakedQuad(data, -1, face, sprite, true, quad.getFormat());
            }
            quads.add(quad);
        }
        return Collections.unmodifiableList(quads);
    }
}
