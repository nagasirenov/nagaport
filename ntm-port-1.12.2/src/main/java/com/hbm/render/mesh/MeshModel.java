package com.hbm.render.mesh;

import java.util.List;

/**
 * NTM-owned drawable geometry. Implementations own either native CPU emission or real GPU buffers;
 * textures, matrices, tint and lighting stay with the caller. Parts retain source order and names.
 */
public interface MeshModel {
    ObjMesh getMesh();
    List<String> getPartNames();
    void renderAll();
    void renderPart(String name);
    void renderOnly(String... names);
    void renderAllExcept(String... names);
}
