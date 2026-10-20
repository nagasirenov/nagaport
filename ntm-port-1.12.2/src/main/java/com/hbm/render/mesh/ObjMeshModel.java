package com.hbm.render.mesh;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

/** Eager standalone OBJ model: original plain-Forge flat shading, inset, and non-reload lifetime. */
public final class ObjMeshModel implements MeshModel {
    private final ObjMesh mesh;
    private final ResourceLocation location;

    public ObjMeshModel(ResourceLocation location) {
        this(Minecraft.getMinecraft().getResourceManager(), location);
    }

    /** Explicit resource owner also permits native headless resource-manager verification. */
    public ObjMeshModel(IResourceManager resources, ResourceLocation location) {
        this.location = location;
        mesh = new ReloadableObjMesh(resources, location, false).getMesh();
    }

    @Override public ObjMesh getMesh() { return mesh; }
    @Override public List<String> getPartNames() { return mesh.getPartNames(); }
    @Override public void renderAll() {
        ObjMeshRenderer.drawAll(mesh, ObjMeshRenderer.Shading.LEGACY_FLAT, 0, location.toString());
    }
    @Override public void renderPart(String name) { renderOnly(name); }
    @Override public void renderOnly(String... names) {
        ObjMeshRenderer.drawParts(mesh.selectParts(names), ObjMeshRenderer.Shading.LEGACY_FLAT, 0, location.toString());
    }
    @Override public void renderAllExcept(String... names) {
        ObjMeshRenderer.drawParts(mesh.excludingParts(names), ObjMeshRenderer.Shading.LEGACY_FLAT, 0, location.toString());
    }
}
