package com.hbm.render.mesh;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;

/**
 * The HFR VBO's three independent float3 streams, without GL allocation or upload.
 * This is deliberately not the CPU renderer's inset/attribute-carry representation:
 * W and every source normal float survive, and absent texture coordinates become zero.
 */
public final class ObjMeshVboPayload {
    public final FloatBuffer positions;
    public final FloatBuffer uvs;
    public final FloatBuffer normals;
    public final int vertices;

    private ObjMeshVboPayload(FloatBuffer positions, FloatBuffer uvs, FloatBuffer normals, int vertices) {
        this.positions = positions;
        this.uvs = uvs;
        this.normals = normals;
        this.vertices = vertices;
    }

    public static ObjMeshVboPayload pack(ObjMesh.Part part) {
        // Keep the old triangle-only capacity and fail on quads, rather than silently
        // triangulating, enlarging the stream, or changing GL_TRIANGLES draw semantics.
        int capacity = part.faces.size() * 3 * 3;
        FloatBuffer positions = BufferUtils.createFloatBuffer(capacity);
        FloatBuffer uvs = BufferUtils.createFloatBuffer(capacity);
        FloatBuffer normals = BufferUtils.createFloatBuffer(capacity);
        int vertices = 0;
        for(ObjMesh.Face face : part.faces) {
            for(ObjMesh.Corner corner : face.corners) {
                ObjMesh.Vec position = corner.position;
                ObjMesh.Vec uv = corner.uv;
                ObjMesh.Vec normal = corner.normal;
                // The former vertexNormals[i] dereference also failed for faces
                // without source normals. Do not substitute a generated face normal.
                if(normal == null) throw new NullPointerException("VBO corners require source normals");
                vertices++;
                positions.put(new float[] {position.x, position.y, position.z});
                uvs.put(uv == null ? new float[] {0, 0, 0} : new float[] {uv.x, uv.y, uv.z});
                normals.put(new float[] {normal.x, normal.y, normal.z});
            }
        }
        positions.flip();
        uvs.flip();
        normals.flip();
        return new ObjMeshVboPayload(positions, uvs, normals, vertices);
    }
}
