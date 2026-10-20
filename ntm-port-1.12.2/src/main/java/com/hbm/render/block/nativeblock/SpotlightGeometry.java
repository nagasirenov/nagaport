package com.hbm.render.block.nativeblock;

import java.util.ArrayList;
import java.util.Collections;
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

/** Unshaded atlas parts. The original light renderer rolled vertices, but deliberately not normals. */
public final class SpotlightGeometry {
    private SpotlightGeometry() { }
    private static final class Vertex {
        float x, y, z, nx, ny, nz;
        double u, v;
    }
    public static List<BakedQuad> bake(ObjMesh mesh, String name, TextureAtlasSprite sprite, boolean inventory,
            float yaw, float pitch, float roll, float x, float y, float z) {
        ObjMesh.Part selected = null;
        for(ObjMesh.Part part : mesh.parts) if(name.equals(part.name)) selected = part;
        if(selected == null) return Collections.emptyList(); // Original exact-case last-part selection.
        List<Vertex> stream = new ArrayList<>();
        for(ObjMesh.Face face : selected.faces) {
            double[] normal = inventory ? new double[] {-face.normal.z, face.normal.y, face.normal.x}
                    : rotate(face.normal, yaw, pitch, 0);
            for(int i = 0; i < face.corners.size(); i++) {
                ObjMesh.Corner corner = face.corners.get(i);
                if(corner.uv == null) throw new IllegalArgumentException("Spotlight atlas part requires UVs: " + name);
                Vertex vertex = new Vertex();
                if(inventory) {
                    // Original GL rotation(-90 Y) and scale(1.5), recentered for native RenderItem.
                    vertex.x = (float)(.5D - corner.position.z * 1.5D);
                    vertex.y = (float)(.5D + corner.position.y * 1.5D);
                    vertex.z = (float)(.5D + corner.position.x * 1.5D);
                } else {
                    double[] point = rotate(corner.position, yaw, pitch, roll);
                    // ObjUtil cast each rotated coordinate before Tessellator translation.
                    vertex.x = (float)(x + (double)(float)point[0]);
                    vertex.y = (float)(y + (double)(float)point[1]);
                    vertex.z = (float)(z + (double)(float)point[2]);
                }
                vertex.nx = (float)normal[0]; vertex.ny = (float)normal[1]; vertex.nz = (float)normal[2];
                vertex.u = corner.uv.x * 16D; vertex.v = corner.uv.y * 16D;
                stream.add(vertex);
                if(face.corners.size() == 3 && i == 2) stream.add(vertex);
            }
        }
        VertexFormat format = inventory ? DefaultVertexFormats.ITEM : DefaultVertexFormats.BLOCK;
        List<BakedQuad> result = new ArrayList<>();
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
                    case POSITION: builder.put(e, vertex.x, vertex.y, vertex.z, 1); break;
                    case COLOR: builder.put(e, 1, 1, 1, 1); break;
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
            result.add(inventory ? quad : new BlockPositionLightQuad(quad));
        }
        return Collections.unmodifiableList(result);
    }
    private static double[] rotate(ObjMesh.Vec vector, float yaw, float pitch, float roll) {
        // Preserve the original 1.7 Vec3 float lookup tables, not Math.sin/cos replacements.
        double c = MathHelper.cos(roll), s = MathHelper.sin(roll);
        double x = vector.x, y = vector.y * c + vector.z * s, z = vector.z * c - vector.y * s;
        c = MathHelper.cos(pitch); s = MathHelper.sin(pitch);
        double xx = x * c + y * s; y = y * c - x * s; x = xx;
        c = MathHelper.cos(yaw); s = MathHelper.sin(yaw);
        return new double[] {x * c + z * s, y, z * c - x * s};
    }
}
