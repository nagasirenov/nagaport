package com.hbm.render.block.nativeblock;

import java.util.List;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;

/** Native cuboid faces retaining the conveyor meshes' original atlas coordinates. */
public final class ConveyorCuboidGeometry {
    private ConveyorCuboidGeometry() { }

    // Native canonical face order, with each bit choosing the maximum coordinate.
    // Cyclic shifts of the original order retain winding/UVs and allow the
    // vanilla smooth-AO fallback to assign its corner light values correctly.
    private static final int[][] CORNERS = {
        {4, 0, 1, 5}, {2, 6, 7, 3}, {3, 1, 0, 2},
        {6, 4, 5, 7}, {2, 0, 4, 6}, {7, 5, 1, 3}
    };

    /**
     * Append six faces in EnumFacing order. Texture/rotation arrays use that same
     * order. Rotation values retain the old bottom/top/east/west/north/south
     * meanings for DOWN/UP/NORTH/SOUTH/WEST/EAST, rather than compass names.
     * Culling is deliberately the calling model's responsibility.
     */
    public static void append(List<BakedQuad> output, boolean inventory,
            TextureAtlasSprite[] sprites, int[] rotations,
            double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        append(output, inventory, sprites, rotations, minX, minY, minZ, maxX, maxY, maxZ, 0);
    }

    /** Position-only offset preserves the belt item's original UV sampling. */
    public static void append(List<BakedQuad> output, boolean inventory,
            TextureAtlasSprite[] sprites, int[] rotations,
            double minX, double minY, double minZ, double maxX, double maxY, double maxZ,
            double yOffset) {
        if(sprites.length != 6 || rotations.length != 6) throw new IllegalArgumentException("Six face materials and rotations required");
        for(EnumFacing side : EnumFacing.values()) {
            output.add(face(sprites[side.getIndex()], inventory, side, rotations[side.getIndex()],
                    minX, minY, minZ, maxX, maxY, maxZ, yOffset));
        }
    }

    public static BakedQuad face(TextureAtlasSprite sprite, boolean inventory, EnumFacing side,
            int rotation, double minX, double minY, double minZ,
            double maxX, double maxY, double maxZ, double yOffset) {
        if(rotation < 0 || rotation > 3) throw new IllegalArgumentException("Conveyor face rotation must be 0..3");
        // Native baked faces use ITEM even in chunk models: normals are retained,
        // and the native lighter supplies the chunk lightmap. In particular, a
        // zero-width grate must not require normalizing a zero cross-product.
        VertexFormat format = DefaultVertexFormats.ITEM;
        UnpackedBakedQuad.Builder builder = new UnpackedBakedQuad.Builder(format);
        builder.setTexture(sprite);
        builder.setQuadTint(-1);
        builder.setQuadOrientation(side);
        builder.setApplyDiffuseLighting(true);
        builder.setContractUVs(false);
        for(int corner : CORNERS[side.getIndex()]) {
            double x = (corner & 1) == 0 ? minX : maxX;
            double y = (corner & 2) == 0 ? minY : maxY;
            double z = (corner & 4) == 0 ? minZ : maxZ;
            double reversedX = (corner & 1) == 0 ? maxX : minX;
            double reversedY = (corner & 2) == 0 ? maxY : minY;
            double reversedZ = (corner & 4) == 0 ? maxZ : minZ;
            double u, v;
            switch(side) {
                case DOWN:
                    u = rotation == 1 ? 1-z : rotation == 2 ? z : rotation == 3 ? 1-x : x;
                    v = rotation == 1 ? x : rotation == 2 ? 1-x : rotation == 3 ? 1-z : z;
                    break;
                case UP:
                    u = rotation == 1 ? z : rotation == 2 ? 1-z : rotation == 3 ? 1-x : x;
                    v = rotation == 1 ? 1-x : rotation == 2 ? x : rotation == 3 ? 1-z : z;
                    break;
                case NORTH:
                    // Original negative-Z and positive-X faces mirror the two
                    // bounds, not the whole texture, for unrotated partial boxes.
                    u = rotation == 1 ? 1-reversedY : rotation == 2 ? reversedY : rotation == 3 ? 1-reversedX : reversedX;
                    v = rotation == 1 ? x : rotation == 2 ? 1-x : rotation == 3 ? y : 1-y;
                    break;
                case SOUTH:
                    u = rotation == 1 ? reversedY : rotation == 2 ? 1-reversedY : rotation == 3 ? 1-x : x;
                    v = rotation == 1 ? 1-x : rotation == 2 ? x : rotation == 3 ? y : 1-y;
                    break;
                case WEST:
                    u = rotation == 1 ? reversedY : rotation == 2 ? 1-reversedY : rotation == 3 ? 1-z : z;
                    v = rotation == 1 ? 1-z : rotation == 2 ? z : rotation == 3 ? y : 1-y;
                    break;
                case EAST:
                    u = rotation == 1 ? 1-reversedY : rotation == 2 ? reversedY : rotation == 3 ? 1-reversedZ : reversedZ;
                    v = rotation == 1 ? z : rotation == 2 ? 1-z : rotation == 3 ? y : 1-y;
                    break;
                default: throw new AssertionError(side);
            }
            for(int e = 0; e < format.getElementCount(); e++) {
                VertexFormatElement element = format.getElement(e);
                switch(element.getUsage()) {
                    case POSITION: builder.put(e, (float)x, (float)(y+yOffset), (float)z, 1); break;
                    case COLOR: builder.put(e, 1, 1, 1, 1); break;
                    case UV: builder.put(e, sprite.getInterpolatedU(u*16), sprite.getInterpolatedV(v*16), 0, 1); break;
                    case NORMAL: builder.put(e, side.getXOffset(), side.getYOffset(), side.getZOffset(), 0); break;
                    default: builder.put(e);
                }
            }
        }
        return builder.build();
    }
}
