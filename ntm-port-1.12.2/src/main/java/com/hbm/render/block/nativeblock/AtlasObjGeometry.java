package com.hbm.render.block.nativeblock;

import java.util.ArrayList;
import java.util.List;
import com.hbm.render.mesh.ObjMesh;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;

/** The two original atlas-OBJ shading and vertex-stream contracts, not a legacy renderer API. */
public final class AtlasObjGeometry {
    private AtlasObjGeometry() { }
    public static void appendWhole(List<BakedQuad> output, ObjMesh mesh, TextureAtlasSprite sprite,
            boolean inventory, float yaw, float pitch, double scale, double x, double y, double z) {
        List<Vertex> stream = new ArrayList<>();
        for(ObjMesh.Part part : mesh.parts) append(stream, part, inventory, true, yaw, pitch, scale, x, y, z);
        emit(output, stream, sprite, inventory);
    }
    public static void appendPart(List<BakedQuad> output, ObjMesh mesh, String name, TextureAtlasSprite sprite,
            boolean inventory, float yaw, float pitch, double scale, double x, double y, double z) {
        ObjMesh.Part selected = null;
        for(ObjMesh.Part part : mesh.parts) if(name.equals(part.name)) selected = part;
        // Original ObjUtil silently emits nothing for an absent named group.
        if(selected == null) return;
        List<Vertex> stream = new ArrayList<>();
        append(stream, selected, inventory, false, yaw, pitch, scale, x, y, z);
        emit(output, stream, sprite, inventory);
    }
    private static final class Vertex {
        double x, y, z, u, v;
        float nx, ny, nz, color;
    }
    private static void append(List<Vertex> stream, ObjMesh.Part part, boolean inventory, boolean whole,
            float yaw, float pitch, double scale, double x, double y, double z) {
        for(ObjMesh.Face face : part.faces) {
            double[] normal = rotate(face.normal, yaw, pitch);
            float nx = (float)normal[0], ny = (float)normal[1], nz = (float)normal[2];
            float shade = inventory ? 1F : whole
                    ? Math.max(.45F, (ny + .7F) * .9F - Math.abs(nx) * .1F + Math.abs(nz) * .1F)
                    : Math.max(.45F, ny * .3F + .7F - Math.abs(nx) * .1F + Math.abs(nz) * .1F);
            float color = Math.max(0, Math.min(255, (int)(shade * 255F))) / 255F;
            for(int i = 0; i < face.corners.size(); i++) {
                ObjMesh.Corner corner = face.corners.get(i);
                if(corner.uv == null) throw new IllegalArgumentException("Atlas OBJ requires UVs: " + part.name);
                double[] point = rotate(corner.position, yaw, pitch);
                Vertex vertex = new Vertex();
                // ObjUtil cast source coordinates before Tessellator translation / GL scale.
                vertex.x = x + (float)point[0] * scale;
                vertex.y = y + (float)point[1] * scale;
                vertex.z = z + (float)point[2] * scale;
                vertex.u = whole ? corner.uv.x * 16F : corner.uv.x * 16D;
                vertex.v = whole ? corner.uv.y * 16F : corner.uv.y * 16D;
                vertex.nx = nx; vertex.ny = ny; vertex.nz = nz; vertex.color = color;
                stream.add(vertex);
                // Whole ObjUtil duplicates vertex three even for a quad. Keep its
                // continuous stream, including cross-face quads and partial tail.
                if(i % 3 == 2 && (whole || face.corners.size() == 3)) stream.add(vertex);
            }
        }
    }
    private static void emit(List<BakedQuad> output, List<Vertex> stream, TextureAtlasSprite sprite, boolean inventory) {
        VertexFormat format = inventory ? DefaultVertexFormats.ITEM : DefaultVertexFormats.BLOCK;
        // OpenGL GL_QUADS ignored a trailing incomplete primitive at draw end.
        for(int start = 0; start + 3 < stream.size(); start += 4) {
            UnpackedBakedQuad.Builder builder = new UnpackedBakedQuad.Builder(format);
            Vertex first = stream.get(start);
            builder.setTexture(sprite);
            builder.setQuadTint(-1);
            builder.setQuadOrientation(EnumFacing.getFacingFromVector(first.nx, first.ny, first.nz));
            builder.setApplyDiffuseLighting(inventory);
            builder.setContractUVs(false);
            for(int v = 0; v < 4; v++) for(int e = 0; e < format.getElementCount(); e++) {
                Vertex vertex = stream.get(start + v);
                VertexFormatElement element = format.getElement(e);
                switch(element.getUsage()) {
                    case POSITION: builder.put(e, (float)vertex.x, (float)vertex.y, (float)vertex.z, 1); break;
                    case COLOR: builder.put(e, vertex.color, vertex.color, vertex.color, 1); break;
                    case UV:
                        if(element.getIndex() == 0) builder.put(e, sprite.getInterpolatedU(vertex.u), sprite.getInterpolatedV(vertex.v), 0, 1);
                        else builder.put(e, 0, 0, 0, 1);
                        break;
                    case NORMAL:
                        builder.put(e, ((byte)(vertex.nx * 127F)) / 127F, ((byte)(vertex.ny * 127F)) / 127F,
                                ((byte)(vertex.nz * 127F)) / 127F, 0);
                        break;
                    default: builder.put(e);
                }
            }
            BakedQuad quad = builder.build();
            output.add(inventory ? quad : new BlockPositionLightQuad(quad));
        }
    }
    private static double[] rotate(ObjMesh.Vec vector, float yaw, float pitch) {
        // Original Vec3 rotates Z (called pitch), then Y. Its MathHelper table
        // returns floats, promoted to double for the coordinate arithmetic.
        double c = MathHelper.cos(pitch), s = MathHelper.sin(pitch);
        double x = vector.x * c + vector.y * s;
        double y = vector.y * c - vector.x * s;
        c = MathHelper.cos(yaw); s = MathHelper.sin(yaw);
        return new double[] {x * c + vector.z * s, y, vector.z * c - x * s};
    }
}
