package com.hbm.render.mesh;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

/**
 * Eager resource snapshot with explicit reload. It does not register a reload listener or allocate GL objects.
 * In particular, ModelWormHead retains its original static-load/non-reload lifetime.
 */
public final class ReloadableObjMesh {
    private final ResourceLocation location;
    private final boolean mixedFaces;
    private ObjMesh mesh;

    public ReloadableObjMesh(IResourceManager resources, ResourceLocation location, boolean mixedFaces) {
        this.location = location; this.mixedFaces = mixedFaces;
        reload(resources);
    }
    public ObjMesh getMesh() { return mesh; }

    /** Replace atomically after successful parsing; failure is reported, never hidden. */
    public void reload(IResourceManager resources) {
        final ObjMesh replacement;
        try(IResource resource = resources.getResource(location);
                Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
            replacement = ObjMeshParser.parse(reader, location.toString(), mixedFaces);
        } catch(IOException e) {
            throw new IllegalStateException("Cannot load OBJ mesh " + location, e);
        }
        mesh = replacement;
    }
}
