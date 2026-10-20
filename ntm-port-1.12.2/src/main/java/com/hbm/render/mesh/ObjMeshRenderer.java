package com.hbm.render.mesh;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import org.lwjgl.opengl.GL11;

/** Native 1.12 vertex emission. Texture binding, matrices, tint and lighting belong to the caller. */
public final class ObjMeshRenderer {
    /** Explicit source-path policies, not a reinterpretation of HFR's late noSmooth() flag. */
    public enum Shading {
        LEGACY_FLAT(false, 0.0005F), HFR_FLAT(false, 0), HFR_SMOOTH(true, 0), HMF(false, 0.0005F);
        final boolean smooth;
        final float inset;
        Shading(boolean smooth, float inset) { this.smooth = smooth; this.inset = inset; }
    }

    /** One tessellation batch's current UV/normal, including faces missing either attribute. */
    public static final class Cursor {
        private ObjMesh.Vec normal = new ObjMesh.Vec(0, 0, 0);
        private float u, v;
        /** Neutral new-batch state; not an implicit snapshot of old Tessellator or external GL state. */
        public Cursor() { }
        public Cursor(float u, float v, ObjMesh.Vec normal) {
            if(normal == null) throw new IllegalArgumentException("Initial normal is required");
            this.u = u; this.v = v; this.normal = normal;
        }
    }
    private ObjMeshRenderer() { }

    public static float hmfOffset(float currentTime, double modulo, double quotient) {
        return (float)(((double)currentTime % modulo) / quotient);
    }

    public static VertexFormat format(List<ObjMesh.Face> faces, Shading shading) {
        boolean uv = false, normal = !shading.smooth;
        for(ObjMesh.Face face : faces) {
            uv |= face.textured();
            // S_Face's smooth normals are set only inside its textured branch.
            if(face.textured()) for(ObjMesh.Corner corner : face.corners) normal |= corner.normal != null;
        }
        if(uv) return normal ? DefaultVertexFormats.POSITION_TEX_NORMAL : DefaultVertexFormats.POSITION_TEX;
        return normal ? DefaultVertexFormats.POSITION_NORMAL : DefaultVertexFormats.POSITION;
    }

    /**
     * Append only: never begins, finishes or draws the caller's buffer.
     * QUADS accepts triangles as [0,1,2,2], preserving the old icon-rendering convention.
     * An explicit cursor preserves current attributes across multiple append calls in one batch.
     */
    public static void append(BufferBuilder buffer, List<ObjMesh.Face> faces, Shading shading,
            float animationOffset, Cursor cursor) {
        append(buffer, faces, shading, animationOffset, cursor, true);
    }

    /** Raw source corners, without the separate icon path's triangle-to-quad padding. */
    public static void appendRaw(BufferBuilder buffer, List<ObjMesh.Face> faces, Shading shading,
            float animationOffset, Cursor cursor) {
        append(buffer, faces, shading, animationOffset, cursor, false);
    }

    private static void append(BufferBuilder buffer, List<ObjMesh.Face> faces, Shading shading,
            float animationOffset, Cursor cursor, boolean padTriangles) {
        VertexFormat format = buffer.getVertexFormat();
        int mode = buffer.getDrawMode();
        if(padTriangles && mode != GL11.GL_TRIANGLES && mode != GL11.GL_QUADS)
            throw new IllegalArgumentException("OBJ emission requires triangles or quads");
        boolean textured = format.hasUvOffset(0), normal = format.hasNormal();
        VertexFormat expected = textured ? (normal ? DefaultVertexFormats.POSITION_TEX_NORMAL : DefaultVertexFormats.POSITION_TEX)
                : (normal ? DefaultVertexFormats.POSITION_NORMAL : DefaultVertexFormats.POSITION);
        if(!format.equals(expected)) throw new IllegalArgumentException("OBJ emission does not own color/lightmap attributes");
        if(format(faces, shading).hasNormal() && !normal)
            throw new IllegalArgumentException("Buffer has no normal attribute required by this shading policy");
        for(ObjMesh.Face face : faces) {
            if(padTriangles && mode == GL11.GL_TRIANGLES && face.corners.size() != 3)
                throw new IllegalArgumentException("A quad cannot be appended to a triangle buffer without an explicit triangulation policy");
            if(face.textured() && !textured) throw new IllegalArgumentException("Buffer has no UV attribute");
            if(!shading.smooth) cursor.normal = face.normal;
            float averageU = 0, averageV = 0;
            if(face.textured()) {
                for(ObjMesh.Corner corner : face.corners) { averageU += corner.uv.x; averageV += corner.uv.y; }
                averageU /= face.corners.size(); averageV /= face.corners.size();
            }
            int count = padTriangles ? (mode == GL11.GL_QUADS ? 4 : 3) : face.corners.size();
            for(int i = 0; i < count; i++) {
                ObjMesh.Corner corner = face.corners.get(Math.min(i, face.corners.size() - 1));
                if(face.textured()) {
                    cursor.u = corner.uv.x + (corner.uv.x > averageU ? -shading.inset : shading.inset);
                    cursor.v = corner.uv.y + (corner.uv.y > averageV ? -shading.inset : shading.inset)
                            + (shading == Shading.HMF ? animationOffset : 0);
                    if(shading.smooth && corner.normal != null) cursor.normal = corner.normal;
                }
                buffer.pos(corner.position.x, corner.position.y, corner.position.z);
                if(textured) buffer.tex(cursor.u, cursor.v);
                if(normal) buffer.normal(cursor.normal.x, cursor.normal.y, cursor.normal.z);
                buffer.endVertex();
            }
        }
    }

    /**
     * Build the same one-batch source-order stream used by renderAll. Caller finishes/draws it.
     * The last source group's primitive mode is retained, including cross-group mixed modes.
     * Texture/normal cursor state spans all groups; no per-group begin/reset is introduced.
     */
    public static void buildAll(BufferBuilder buffer, ObjMesh mesh, Shading shading, float animationOffset) {
        buildAll(buffer, mesh, shading, animationOffset, "OBJ mesh");
    }

    public static void buildAll(BufferBuilder buffer, ObjMesh mesh, Shading shading, float animationOffset, String source) {
        List<ObjMesh.Face> faces = new ArrayList<>();
        int mode = GL11.GL_TRIANGLES;
        String firstGroup = "<empty>";
        for(ObjMesh.Part part : mesh.parts) {
            if(faces.isEmpty() && !part.faces.isEmpty()) firstGroup = part.name;
            faces.addAll(part.faces);
            mode = part.faces.isEmpty() ? -1 : (part.faces.get(0).corners.size() == 3 ? GL11.GL_TRIANGLES : GL11.GL_QUADS);
        }
        requireDefinedAttributes(faces, shading, source, firstGroup);
        buffer.begin(mode, format(faces, shading));
        if(!faces.isEmpty()) appendRaw(buffer, faces, shading, animationOffset, new Cursor());
    }

    public static void drawAll(ObjMesh mesh, Shading shading, float animationOffset) {
        drawAll(mesh, shading, animationOffset, "OBJ mesh");
    }

    public static void drawAll(ObjMesh mesh, Shading shading, float animationOffset, String source) {
        Tessellator tessellator = Tessellator.getInstance();
        buildAll(tessellator.getBuffer(), mesh, shading, animationOffset, source);
        tessellator.draw();
    }

    /**
     * Draw selected source-order parts, with a fresh batch per part (legacy renderPart/renderOnly style).
     * renderAll instead uses drawAll's shared batch; this method deliberately resets between parts.
     * Owns begin/draw; no texture, tint, matrix or lighting changes.
     */
    public static void drawParts(List<ObjMesh.Part> parts, Shading shading, float animationOffset) {
        drawParts(parts, shading, animationOffset, "OBJ mesh");
    }

    public static void drawParts(List<ObjMesh.Part> parts, Shading shading, float animationOffset, String source) {
        // Validate every selected batch before any earlier selected part can begin or draw.
        for(ObjMesh.Part part : parts) requireDefinedAttributes(part.faces, shading, source, part.name);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        for(ObjMesh.Part part : parts) {
            if(buildPart(buffer, part, shading, animationOffset, source)) tessellator.draw();
        }
    }

    /** Begin and append one nonempty source part; caller finishes/draws when this returns true. */
    public static boolean buildPart(BufferBuilder buffer, ObjMesh.Part part, Shading shading, float animationOffset) {
        return buildPart(buffer, part, shading, animationOffset, "OBJ mesh");
    }

    public static boolean buildPart(BufferBuilder buffer, ObjMesh.Part part, Shading shading, float animationOffset, String source) {
        if(part.faces.isEmpty()) return false;
        requireDefinedAttributes(part.faces, shading, source, part.name);
        int count = part.faces.get(0).corners.size();
        for(ObjMesh.Face face : part.faces) if(face.corners.size() != count)
            throw new IllegalArgumentException("Mixed-mode OBJ parts require explicit caller-owned emission: " + part.name);
        buffer.begin(count == 3 ? GL11.GL_TRIANGLES : GL11.GL_QUADS, format(part.faces, shading));
        appendRaw(buffer, part.faces, shading, animationOffset, new Cursor());
        return true;
    }

    /**
     * A later activation in legacy Tessellator exposed unwritten raw slots from unrelated old draws.
     * There is no mesh-local value to carry for that prefix. Reject only that undefined case before
     * touching the buffer, rather than inventing zero UVs/normals. Entirely disabled attributes and
     * carry after a first assignment remain valid. Caller-owned append may supply an explicit Cursor.
     */
    private static void requireDefinedAttributes(List<ObjMesh.Face> faces, Shading shading, String source, String group) {
        if(faces.isEmpty()) return;
        VertexFormat format = format(faces, shading);
        ObjMesh.Face first = faces.get(0);
        boolean missingUv = format.hasUvOffset(0) && !first.textured();
        boolean missingNormal = format.hasNormal() && shading.smooth
                && (!first.textured() || first.corners.get(0).normal == null);
        if(missingUv || missingNormal)
            throw new IllegalStateException("Cannot render OBJ " + source + " group '" + group
                    + "': the batch emits vertices before " + (missingUv ? (missingNormal ? "UV and normal" : "UV") : "normal")
                    + " attributes are first set. Legacy stale-buffer values cannot be reproduced; split the batches or supply initialized attributes.");
    }
}
