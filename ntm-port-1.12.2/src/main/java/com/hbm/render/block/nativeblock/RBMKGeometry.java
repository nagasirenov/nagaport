package com.hbm.render.block.nativeblock;

import java.util.*;
import com.hbm.render.mesh.ObjMesh;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;

/** Bounded column geometry; no shared renderer flags, mutable block bounds or legacy render API. */
public final class RBMKGeometry {
    private RBMKGeometry() { }
    public static final class Part {
        public final BakedQuad quad;
        public final boolean obj, cracks;
        public final int yShift, face;
        public Part(BakedQuad quad, boolean obj, boolean cracks, int yShift, int face) {
            this.quad = quad; this.obj = obj; this.cracks = cracks; this.yShift = yShift; this.face = face;
        }
    }
    public static void cube(List<Part> out, RBMKMaterials materials, int lid, boolean pipes, boolean item,
            int faces, double x0, double y0, double z0, double x1, double y1, double z1, int shift, double itemOffset) {
        for(EnumFacing face : EnumFacing.values()) if((faces & 1 << face.getIndex()) != 0) {
            BakedQuad quad = ConveyorCuboidGeometry.face(materials.face(face.getIndex(), lid, pipes), item, face, 0, x0, y0, z0, x1, y1, z1, 0);
            if(item) quad = item(quad, shift, itemOffset);
            out.add(new Part(quad, false, true, item ? 0 : shift, face.getIndex()));
        }
    }
    public static void obj(List<Part> out, ObjMesh mesh, String group, TextureAtlasSprite sprite, boolean item,
            boolean shade, int color, boolean cracks, int shift, double itemOffset) {
        ObjMesh.Part part = null;
        for(ObjMesh.Part candidate : mesh.parts) if(candidate.name.equals(group)) part = candidate;
        if(part == null) return; // Original ObjUtil emits no vertices for a missing named group.
        for(ObjMesh.Face face : part.faces) {
            float nx = face.normal.x, ny = face.normal.y, nz = face.normal.z;
            float brightness = shade ? Math.max(.45F, ny * .3F + .7F - Math.abs(nx) * .1F + Math.abs(nz) * .1F) : 1;
            double[] vertices = new double[20];
            for(int v = 0; v < 4; v++) {
                ObjMesh.Corner corner = face.corners.get(Math.min(v, face.corners.size() - 1));
                if(corner.uv == null) throw new IllegalArgumentException("RBMK OBJ requires UVs");
                vertices[v*5] = corner.position.x + .5D;
                vertices[v*5+1] = corner.position.y;
                vertices[v*5+2] = corner.position.z + .5D;
                vertices[v*5+3] = corner.uv.x * 16D;
                vertices[v*5+4] = corner.uv.y * 16D;
            }
            BakedQuad quad = ConnectionQuadFactory.coloredQuad(sprite, item, brightness, color, nx, ny, nz, vertices);
            if(item) quad = item(quad, shift, itemOffset);
            out.add(new Part(quad, true, cracks, 0, -1));
        }
    }
    private static BakedQuad item(BakedQuad source, int segment, double offset) {
        int[] data = source.getVertexData().clone(); int stride = source.getFormat().getIntegerSize();
        for(int i = 0; i < 4; i++) {
            data[i*stride] = Float.floatToRawIntBits((float)(.5D + (Float.intBitsToFloat(data[i*stride]) - .5D) * .35F));
            data[i*stride+1] = Float.floatToRawIntBits((float)(.5D + offset + (Float.intBitsToFloat(data[i*stride+1]) + segment) * .35F));
            data[i*stride+2] = Float.floatToRawIntBits((float)(.5D + (Float.intBitsToFloat(data[i*stride+2]) - .5D) * .35F));
        }
        return new BakedQuad(data, source.getTintIndex(), source.getFace(), source.getSprite(), source.shouldApplyDiffuseLighting(), source.getFormat());
    }
    public static List<Part> immutable(List<Part> parts) { return Collections.unmodifiableList(parts); }
}
