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
import net.minecraftforge.client.model.pipeline.IVertexConsumer;
import net.minecraftforge.client.model.pipeline.LightUtil;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;

/** RenderMirror.printMirror, including its unrotated normal/shade and float UV arithmetic. */
public final class SolarMirrorGeometry {
    private SolarMirrorGeometry() { }
    private static final class Vertex {
        float x, y, z, u, v, nx, ny, nz, color;
    }
    public static void append(List<BakedQuad> output, ObjMesh mesh, TextureAtlasSprite sprite, int dx, int dy, int dz) {
        ObjMesh.Part mirror = null;
        for(ObjMesh.Part part : mesh.parts) if(part.name.equals("Mirror")) mirror = part;
        if(mirror == null) return;
        // Original integer products intentionally precede the promotion to double.
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        float pitch = (float)(-Math.asin(dy / distance) + Math.PI / 2D);
        float yaw = (float)(-Math.atan2(dz, dx) - Math.PI / 2D);
        double pc = MathHelper.cos(pitch), ps = MathHelper.sin(pitch);
        double yc = MathHelper.cos(yaw), ys = MathHelper.sin(yaw);
        List<Vertex> stream = new ArrayList<>();
        for(ObjMesh.Face face : mirror.faces) {
            ObjMesh.Vec normal = face.normal;
            float shade = Math.max(.45F, (normal.y + 1) * .65F);
            float color = Math.max(0, Math.min(255, (int)(shade * 255F))) / 255F;
            for(int i = 0; i < face.corners.size(); i++) {
                ObjMesh.Corner corner = face.corners.get(i);
                if(corner.uv == null) throw new IllegalArgumentException("Solar mirror OBJ requires UVs");
                ObjMesh.Vec position = corner.position;
                // Vec3.rotateAroundX followed by rotateAroundY, about y=1.
                double x = position.x, y = position.y - 1, z = position.z;
                double ry = y * pc + z * ps, rz = z * pc - y * ps;
                Vertex vertex = new Vertex();
                vertex.x = (float)(x * yc + rz * ys);
                vertex.y = (float)ry + 1;
                vertex.z = (float)(rz * yc - x * ys);
                vertex.u = corner.uv.x * 16;
                vertex.v = corner.uv.y * 16;
                vertex.nx = normal.x; vertex.ny = normal.y; vertex.nz = normal.z;
                vertex.color = color;
                stream.add(vertex);
                if(i % 3 == 2) stream.add(vertex);
            }
        }
        VertexFormat format = DefaultVertexFormats.BLOCK;
        for(int start = 0; start + 3 < stream.size(); start += 4) {
            Vertex first = stream.get(start);
            UnpackedBakedQuad.Builder builder = new UnpackedBakedQuad.Builder(format);
            builder.setTexture(sprite);
            builder.setQuadTint(-1);
            builder.setQuadOrientation(EnumFacing.getFacingFromVector(first.nx, first.ny, first.nz));
            builder.setApplyDiffuseLighting(false);
            builder.setContractUVs(false);
            for(int v = 0; v < 4; v++) for(int e = 0; e < format.getElementCount(); e++) {
                Vertex vertex = stream.get(start + v);
                VertexFormatElement element = format.getElement(e);
                switch(element.getUsage()) {
                    case POSITION: builder.put(e, (float)(.5D + vertex.x), vertex.y, (float)(.5D + vertex.z), 1); break;
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
            output.add(isPoint(quad) ? new PointLightQuad(quad) : new BlockPositionLightQuad(quad));
        }
    }

    private static boolean isPoint(BakedQuad quad) {
        int[] data = quad.getVertexData();
        int stride = quad.getFormat().getIntegerSize();
        for(int axis = 0; axis < 3; axis++) for(int vertex = 1; vertex < 4; vertex++)
            if(Float.intBitsToFloat(data[vertex * stride + axis]) != Float.intBitsToFloat(data[axis])) return false;
        return true;
    }
    /** Overflow can make the original pitch NaN and collapse a face to a point.
     * Retain that zero-area primitive rather than rejecting or repairing its target.
     * There are no fragments to light; pipe still keeps the original block-light contract. */
    private static final class PointLightQuad extends BakedQuad {
        PointLightQuad(BakedQuad source) {
            super(source.getVertexData(), source.getTintIndex(), EnumFacing.DOWN, source.getSprite(), false, source.getFormat());
        }
        @Override public void pipe(final IVertexConsumer consumer) {
            LightUtil.putBakedQuad(new IVertexConsumer() {
                @Override public VertexFormat getVertexFormat() { return consumer.getVertexFormat(); }
                @Override public void setQuadTint(int tint) { consumer.setQuadTint(tint); }
                @Override public void setQuadOrientation(EnumFacing face) { consumer.setQuadOrientation(face); }
                @Override public void setApplyDiffuseLighting(boolean diffuse) { consumer.setApplyDiffuseLighting(false); }
                @Override public void setTexture(TextureAtlasSprite texture) { consumer.setTexture(texture); }
                @Override public void put(int element, float... values) {
                    if(getVertexFormat().getElement(element).getUsage() == VertexFormatElement.EnumUsage.NORMAL)
                        consumer.put(element, 0, 0, 0, 0);
                    else consumer.put(element, values);
                }
            }, this);
        }
    }
}
