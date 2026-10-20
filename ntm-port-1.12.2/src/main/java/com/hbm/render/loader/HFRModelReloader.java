package com.hbm.render.loader;

import java.io.IOException;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;

/** Retains the original two ordered phases: CPU reparse first, real VBO deletion/upload second. */
public class HFRModelReloader implements IResourceManagerReloadListener {
    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {
        // The original listener deliberately resolves Minecraft's manager, not its callback argument.
        reloadModels(Minecraft.getMinecraft().getResourceManager());
    }

    /** Performs the real reload phases for the manager chosen by the client callback. */
    public void reloadModels(IResourceManager resourceManager) {
        for(HFRWavefrontObject obj : HFRWavefrontObject.allModels) {
            obj.destroy();
            try(IResource resource = resourceManager.getResource(obj.resource)) {
                obj.loadObjModel(resource.getInputStream());
            } catch(IOException e) {
                // Original behavior: opening-resource IO failure leaves this destroyed model empty.
                // Parsing failures remain unchecked and abort before the VBO phase, rather than disappearing.
            }
        }
        for(Entry<HFRWavefrontObjectVBO, HFRWavefrontObject> entry : HFRWavefrontObject.allVBOs.entrySet()) {
            HFRWavefrontObjectVBO vbo = entry.getKey();
            vbo.destroy();
            vbo.load(entry.getValue());
        }
    }
}
