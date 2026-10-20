package com.hbm.render.loader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;

import com.hbm.lib.RefStrings;
import com.hbm.render.mesh.MeshModel;
import com.hbm.render.mesh.ObjMesh;
import com.hbm.render.mesh.ObjMeshParser;
import com.hbm.render.mesh.ObjMeshRenderer;
import com.hbm.render.mesh.ObjMeshRenderer.Shading;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

/** Native HFR geometry owner; parse-time shading and original reload/VBO ownership are retained. */
public class HFRWavefrontObject implements MeshModel {
    public static LinkedHashSet<HFRWavefrontObject> allModels = new LinkedHashSet<>();
    public static LinkedHashMap<HFRWavefrontObjectVBO, HFRWavefrontObject> allVBOs = new LinkedHashMap<>();

    public ResourceLocation resource;
    private final String fileName;
    private ObjMesh mesh = ObjMesh.empty();
    private boolean smoothing = true;
    private boolean allowMixedMode;
    private Shading capturedShading = Shading.HFR_SMOOTH;

    public HFRWavefrontObject(String name) { this(new ResourceLocation(RefStrings.MODID, name), false); }
    public HFRWavefrontObject(String name, boolean mixedMode) {
        this(new ResourceLocation(RefStrings.MODID, name), mixedMode);
    }
    public HFRWavefrontObject(ResourceLocation resource) { this(resource, false); }
    public HFRWavefrontObject(ResourceLocation resource, boolean mixedMode) {
        this(Minecraft.getMinecraft().getResourceManager(), resource, mixedMode);
    }

    /** Same eager constructor, with an explicit native resource manager for controlled loading. */
    public HFRWavefrontObject(IResourceManager resources, ResourceLocation resource, boolean mixedMode) {
        this.resource = resource;
        this.fileName = resource.toString();
        if(mixedMode) mixedMode();
        try(IResource res = resources.getResource(resource)) {
            loadObjModel(res.getInputStream());
        } catch(IOException e) {
            throw new IllegalStateException("Cannot load HFR OBJ " + fileName, e);
        }
        allModels.add(this);
    }

    /** Existing faces keep their captured policy; only the next parse observes this change. */
    public HFRWavefrontObject noSmooth() { smoothing = false; return this; }
    /** Mixed-mode geometry is available to direct readers, not to this model's draw entry points. */
    public void mixedMode() { allowMixedMode = true; }
    @Override public ObjMesh getMesh() { return mesh; }
    @Override public List<String> getPartNames() { return mesh.getPartNames(); }
    public Shading getCapturedShading() { return capturedShading; }

    public void destroy() { mesh = ObjMesh.empty(); }

    /** Owns/closes the stream, as did the original HFR loader. */
    public void loadObjModel(InputStream stream) {
        final ObjMesh parsed;
        try(Reader reader = new InputStreamReader(stream)) {
            parsed = ObjMeshParser.parse(reader, fileName, allowMixedMode);
        } catch(IOException e) {
            throw new IllegalStateException("Cannot parse HFR OBJ " + fileName, e);
        }
        mesh = parsed;
        capturedShading = smoothing ? Shading.HFR_SMOOTH : Shading.HFR_FLAT;
    }

    private void requireDrawable() {
        if(allowMixedMode)
            throw new UnsupportedOperationException("Rendering of mixed-mode model " + fileName + " is not supported!");
    }
    @Override public void renderAll() {
        requireDrawable();
        ObjMeshRenderer.drawAll(mesh, capturedShading, 0, fileName);
    }
    @Override public void renderPart(String name) { renderOnly(name); }
    @Override public void renderOnly(String... names) {
        requireDrawable();
        ObjMeshRenderer.drawParts(mesh.selectParts(names), capturedShading, 0, fileName);
    }
    @Override public void renderAllExcept(String... names) {
        requireDrawable();
        ObjMeshRenderer.drawParts(mesh.excludingParts(names), capturedShading, 0, fileName);
    }

    public HFRWavefrontObjectVBO asVBO() {
        HFRWavefrontObjectVBO vbo = new HFRWavefrontObjectVBO(this);
        allVBOs.put(vbo, this);
        return vbo;
    }
}
