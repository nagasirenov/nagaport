package com.hbm.main;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Exact inventory routes whose named bake owner already supplies their real model.
 * Existing JSON (including resource packs) and every unregistered route stay vanilla.
 */
public final class BakeOwnedModelLoader implements ICustomModelLoader {
    @FunctionalInterface
    public interface ModelSource { IModel load() throws Exception; }

    private static final BakeOwnedModelLoader INSTANCE = new BakeOwnedModelLoader();
    private static boolean registered;
    private final Map<ResourceLocation, ModelSource> sources = new HashMap<>();
    private final Map<ModelResourceLocation, String> loadedMissing = new HashMap<>();
    private final Set<IBakedModel> placeholders = Collections.newSetFromMap(new IdentityHashMap<IBakedModel, Boolean>());
    private IResourceManager resources;

    private BakeOwnedModelLoader() { }

    /** True when this baked instance came from a registerMissing tracked placeholder. */
    public static boolean isPlaceholder(IBakedModel model) {
        return INSTANCE.placeholders.contains(model);
    }

    public static void registerMissing(ModelResourceLocation key, String bakeOwner) {
        register(key, () -> {
            INSTANCE.loadedMissing.put(key, bakeOwner);
            final IModel missing = ModelLoaderRegistry.getMissingModel();
            // Keep the genuine Forge missing model, recording its baked identity so
            // validation cannot confuse it with an ordinary model of the same class.
            return new IModel() {
                @Override public Collection<ResourceLocation> getDependencies() { return missing.getDependencies(); }
                @Override public Collection<ResourceLocation> getTextures() { return missing.getTextures(); }
                @Override public IModelState getDefaultState() { return missing.getDefaultState(); }
                @Override public IBakedModel bake(IModelState state, VertexFormat format,
                        Function<ResourceLocation, TextureAtlasSprite> sprites) {
                    IBakedModel baked = missing.bake(state, format, sprites);
                    INSTANCE.placeholders.add(baked);
                    return baked;
                }
            };
        });
    }

    public static void register(ModelResourceLocation key, ModelSource source) {
        if(!"inventory".equals(key.getVariant())) throw new IllegalArgumentException("Not an inventory route: " + key);
        if(!registered) {
            ModelLoaderRegistry.registerLoader(INSTANCE);
            MinecraftForge.EVENT_BUS.register(INSTANCE);
            registered = true;
        }
        INSTANCE.sources.put(new ResourceLocation(key.getNamespace(), "item/" + key.getPath()), source);
    }

    @Override public void onResourceManagerReload(IResourceManager resources) {
        this.resources = resources;
        loadedMissing.clear();
        placeholders.clear();
    }

    @Override public boolean accepts(ResourceLocation location) {
        ResourceLocation path = normalized(location);
        if(!sources.containsKey(path) || resources == null) return false;
        return !exists(resources, new ResourceLocation(path.getNamespace(), "models/" + path.getPath() + ".json"))
                && !hasBlockstate(new ResourceLocation(path.getNamespace(), path.getPath().substring(5)));
    }

    @Override public IModel loadModel(ResourceLocation location) throws Exception {
        ModelSource source = sources.get(normalized(location));
        if(source == null) throw new IllegalArgumentException("Unowned bake route " + location);
        return source.load();
    }

    public static boolean hasBlockstate(ResourceLocation owner) {
        return exists(Minecraft.getMinecraft().getResourceManager(),
                new ResourceLocation(owner.getNamespace(), "blockstates/" + owner.getPath() + ".json"));
    }

    public static boolean hasInventoryAsset(ResourceLocation owner) {
        return hasBlockstate(owner) || exists(Minecraft.getMinecraft().getResourceManager(),
                new ResourceLocation(owner.getNamespace(), "models/item/" + owner.getPath() + ".json"));
    }

    private static boolean exists(IResourceManager resources, ResourceLocation location) {
        try(IResource resource = resources.getResource(location)) { return true; }
        catch(IOException absent) { return false; }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void validateBake(ModelBakeEvent event) {
        Map<String, Integer> unresolvedOwners = new TreeMap<>();
        int replaced = 0, quietUnresolved = 0;
        // All registered owners publish at NORMAL and the route repair runs at
        // LOW; validate last so the count reflects the final registry.
        for(Map.Entry<ModelResourceLocation, String> entry : new TreeMap<>(loadedMissing).entrySet()) {
            IBakedModel model = event.getModelRegistry().getObject(entry.getKey());
            if(model != null && !placeholders.contains(model)
                    && model != event.getModelManager().getMissingModel()
                    && !model.getClass().getName().equals("net.minecraftforge.client.model.FancyMissingModel$BakedModel")) {
                replaced++;
                continue;
            }
            // DefaultVariantQuiet items carry a mesh route to another owner's
            // model key; the quiet placeholder on the default key is by design
            // and usually never queried. Aggregate them instead of per-item
            // error spam; owning a route still does not prove the default key
            // unreachable, so the count stays visible.
            if("DefaultVariantQuiet".equals(entry.getValue())) {
                quietUnresolved++;
                continue;
            }
            unresolvedOwners.merge(entry.getValue(), 1, Integer::sum);
            MainRegistry.logger.error("Unresolved bake-owned model: owner={} location={}", entry.getValue(), entry.getKey());
        }
        int unresolved = loadedMissing.size() - replaced;
        MainRegistry.logger.info("Bake-owned pre-load validation: loaded={}, replaced={}, unresolved={} (of which quiet-by-design={}), unresolved owners={}",
                loadedMissing.size(), replaced, unresolved, quietUnresolved, unresolvedOwners);
    }

    private static ResourceLocation normalized(ResourceLocation location) {
        String path = location.getPath();
        if(path.startsWith("models/")) path = path.substring(7);
        return new ResourceLocation(location.getNamespace(), path);
    }
}
