package com.hbm.render.item.nativeitem.context;

import com.hbm.render.item.nativeitem.AngelCoreFrameCorrection;
import com.hbm.render.item.nativeitem.StaticMeshItemRegistration;
import java.util.*;
import java.util.function.Consumer;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.item.Item;
import net.minecraft.util.registry.IRegistry;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

/** Model activation is gated by complete native renderer ownership. Until that
 * transaction succeeds the baked models and item TEISRs remain unchanged. */
public final class ContextItemRegistration {
    private final Map<Item, ContextItemRenderer> renderers;
    private final Consumer<String> warning;
    private IRegistry<ModelResourceLocation, IBakedModel> registry;
    private boolean active;
    public ContextItemRegistration(Map<Item, ContextItemRenderer> source, Consumer<String> warning) {
        Map<Item, ContextItemRenderer> copy = new IdentityHashMap<>();
        for(Map.Entry<Item, ContextItemRenderer> entry : source.entrySet())
            copy.put(Objects.requireNonNull(entry.getKey()), Objects.requireNonNull(entry.getValue()));
        renderers = Collections.unmodifiableMap(copy);
        this.warning = Objects.requireNonNull(warning);
    }
    public Set<Item> items() { return renderers.keySet(); }
    public boolean isActive() { return active; }
    @SubscribeEvent public void register(ModelRegistryEvent event) {
        for(Item item : renderers.keySet()) {
            ModelResourceLocation location = StaticMeshItemRegistration.modelLocation(item);
            ModelBakery.registerItemVariants(item, location);
            ModelLoader.setCustomMeshDefinition(item, stack -> location);
        }
    }
    @SubscribeEvent public void bake(ModelBakeEvent event) {
        registry = event.getModelRegistry();
        if(active) {
            Activation activation = prepare();
            try { activation.commit(); }
            catch(RuntimeException | Error failure) { activation.rollback(); throw failure; }
        }
    }
    @SubscribeEvent public void frame(RenderItemInFrameEvent event) {
        if(!active || event.getItem().isEmpty() || !renderers.containsKey(event.getItem().getItem())) return;
        ContextItemRenderer renderer = renderers.get(event.getItem().getItem());
        if(renderer instanceof NativeSelectiveItemRenderer && !((NativeSelectiveItemRenderer)renderer).handlesPerspective(
                event.getItem(), ItemCameraTransforms.TransformType.FIXED)) return;
        double[] delta = AngelCoreFrameCorrection.translation(event.getEntityItemFrame().getRotation());
        GL11.glTranslated(delta[0], delta[1], delta[2]);
    }
    Activation prepare() {
        if(registry == null) throw new IllegalStateException("Native model bake has not completed");
        return new Activation();
    }
    final class Activation {
        private final IRegistry<ModelResourceLocation, IBakedModel> target = registry;
        private final List<Item> items = new ArrayList<>(renderers.keySet());
        private final List<ModelResourceLocation> keys = new ArrayList<>();
        private final List<IBakedModel> before = new ArrayList<>(), after = new ArrayList<>();
        private final List<TileEntityItemStackRenderer> previousRenderers = new ArrayList<>();
        private final boolean previouslyActive = active;
        private int changed;
        Activation() {
            for(Item item : items) {
                ModelResourceLocation key = StaticMeshItemRegistration.modelLocation(item);
                IBakedModel original = target.getObject(key);
                if(original == null || original.getParticleTexture() == null)
                    throw new IllegalStateException("Missing original native model/particle: " + key);
                if(original.getParticleTexture() == net.minecraft.client.Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite())
                    warning.accept("Native contextual mesh retains an unresolved atlas particle for " + key
                            + "; item texture registration still requires verification before visual acceptance.");
                keys.add(key); before.add(original); previousRenderers.add(item.getTileEntityItemStackRenderer());
                after.add(ContextItemModel.create(original, renderers.get(item)));
            }
        }
        void commit() {
            if(registry != target) throw new IllegalStateException("Model registry changed during ownership installation");
            for(int i = 0; i < items.size(); i++)
                if(target.getObject(keys.get(i)) != before.get(i)) throw new IllegalStateException("Model ownership changed: " + keys.get(i));
            for(int i = 0; i < items.size(); i++) {
                changed = i + 1;
                target.putObject(keys.get(i), after.get(i));
                items.get(i).setTileEntityItemStackRenderer(renderers.get(items.get(i)));
            }
            active = true;
        }
        void rollback() {
            Throwable failure = null;
            for(int i = changed - 1; i >= 0; i--) {
                try { target.putObject(keys.get(i), before.get(i)); }
                catch(RuntimeException | Error problem) { if(failure == null) failure = problem; else failure.addSuppressed(problem); }
                try { items.get(i).setTileEntityItemStackRenderer(previousRenderers.get(i)); }
                catch(RuntimeException | Error problem) { if(failure == null) failure = problem; else failure.addSuppressed(problem); }
            }
            active = previouslyActive;
            changed = 0;
            if(failure != null) throw new IllegalStateException("Native item-model rollback failed", failure);
        }
    }
    void unsupported(String reason) {
        warning.accept("Native context-item rendering is not activated: " + reason
                + ". Original models and renderer owners were preserved; this combination is unsupported, not visual parity.");
    }
}
