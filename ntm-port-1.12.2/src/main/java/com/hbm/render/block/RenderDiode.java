package com.hbm.render.block;

import java.util.*;
import com.hbm.blocks.network.ConnectionState;
import com.hbm.render.block.nativeblock.AtlasObjGeometry;
import com.hbm.render.block.nativeblock.ConveyorCuboidGeometry;
import com.hbm.render.mesh.ObjMesh;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;

/** Original diode plate, coil cube and independently connected cable OBJ arms. */
public final class RenderDiode {
    private RenderDiode() { }
    public static List<BakedQuad> bake(ObjMesh cable, int metadata, int connections, boolean inventory, TextureAtlasSprite[] sprites) {
        if(sprites.length != 3) throw new IllegalArgumentException("Diode plate, coil and cable sprites required");
        List<BakedQuad> result = new ArrayList<>();
        int meta = inventory ? 0 : metadata;
        cuboid(result, inventory, sprites[0], meta == 4 ? .875 : 0, meta == 0 ? .875 : 0, meta == 2 ? .875 : 0,
                meta == 5 ? .125 : 1, meta == 1 ? .125 : 1, meta == 3 ? .125 : 1);
        cuboid(result, inventory, sprites[1], .125, .125, .125, .875, .875, .875);
        String[] parts = {"posX", "negX", "posY", "negY", "posZ", "negZ"};
        int[] bits = {ConnectionState.POS_X, ConnectionState.NEG_X, ConnectionState.POS_Y, ConnectionState.NEG_Y,
                ConnectionState.NEG_Z, ConnectionState.POS_Z};
        for(int i = 0; i < parts.length; i++) if(inventory ? i != 2 : (connections & bits[i]) != 0)
            AtlasObjGeometry.appendPart(result, cable, parts[i], sprites[2], inventory, inventory ? (float)(Math.PI * 1.5) : 0,
                    0, 1, .5, inventory ? .375 : .5, .5);
        return Collections.unmodifiableList(result);
    }
    private static void cuboid(List<BakedQuad> out, boolean inventory, TextureAtlasSprite sprite,
            double x0, double y0, double z0, double x1, double y1, double z1) {
        for(EnumFacing face : EnumFacing.values()) {
            BakedQuad quad = ConveyorCuboidGeometry.face(sprite, inventory, face, 0, x0, y0, z0, x1, y1, z1, inventory ? -.125 : 0);
            if(inventory) {
                int[] data = quad.getVertexData().clone();
                int stride = quad.getFormat().getIntegerSize();
                int normal = quad.getFormat().getNormalOffset() / 4;
                for(int v = 0; v < 4; v++) {
                    int at = v * stride; float x = Float.intBitsToFloat(data[at]), z = Float.intBitsToFloat(data[at + 2]);
                    data[at] = Float.floatToRawIntBits(z); data[at + 2] = Float.floatToRawIntBits(1 - x);
                    int n = data[at + normal], nx = (byte)n, ny = (byte)(n >> 8), nz = (byte)(n >> 16);
                    data[at + normal] = (n & 0xff000000) | (nz & 255) | ((ny & 255) << 8) | ((-nx & 255) << 16);
                }
                EnumFacing rotated = face.getAxis().isHorizontal() ? face.rotateYCCW() : face;
                quad = new BakedQuad(data, quad.getTintIndex(), rotated, sprite, true, quad.getFormat());
            }
            out.add(quad);
        }
    }
}
