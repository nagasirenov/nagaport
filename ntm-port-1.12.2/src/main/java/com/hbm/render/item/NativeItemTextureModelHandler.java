package com.hbm.render.item;

import com.google.common.collect.ImmutableMap;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Native generated-item foundation for explicit single-texture declarations.
 * Existing enum/icon callbacks and ItemBlocks retain their own model owners.
 * The fallback is baked before NTM's specialized renderers wrap the inventory
 * model, and only replaces missing models. It never replaces a resource-pack
 * model or any unknown custom mesh/model route.
 */
@SideOnly(Side.CLIENT)
public final class NativeItemTextureModelHandler {
    private static final ResourceLocation GENERATED = new ResourceLocation("minecraft", "item/generated");
    private static final ResourceLocation HANDHELD = new ResourceLocation("minecraft", "item/handheld");

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void register(ModelRegistryEvent event) {
        for(Item item : NativeItemTextureRegistry.textures().keySet()) {
            if(!eligible(item)) continue;
            ModelResourceLocation location = location(item);
            ModelBakery.registerItemVariants(item, location);
            // These Forge fields are stable, non-obfuscated API implementation
            // details. Read them only: never erase another model owner's entry.
            if(!hasCustomRoute(item)) {
                // The original default Item icon is metadata-insensitive. A
                // plain subtype item still uses that same image for every meta.
                if(item.getHasSubtypes()) ModelLoader.setCustomMeshDefinition(item, stack -> location);
                else ModelLoader.setCustomModelResourceLocation(item, 0, location);
            }
        }
    }

    @SubscribeEvent
    public void stitch(TextureStitchEvent.Pre event) {
        for(Map.Entry<Item, String> entry : NativeItemTextureRegistry.textures().entrySet()) {
            if(eligible(entry.getKey()))
                event.getMap().registerSprite(NativeItemTextureRegistry.atlasLocation(entry.getValue()));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void bake(ModelBakeEvent event) {
        IBakedModel missing = event.getModelRegistry().getObject(ModelBakery.MODEL_MISSING);
        for(Map.Entry<Item, String> entry : NativeItemTextureRegistry.textures().entrySet()) {
            Item item = entry.getKey();
            if(!eligible(item)) continue;
            ModelResourceLocation location = location(item);
            IBakedModel previous = event.getModelRegistry().getObject(location);
            // Forge wraps individual failed loads in this package-private final
            // model; it is not identical to MODEL_MISSING. Match only that
            // exact native class, never a custom model with a missing particle.
            if(previous != null && previous != missing && !previous.getClass().getName().equals(
                    "net.minecraftforge.client.model.FancyMissingModel$BakedModel")) continue;
            ResourceLocation texture = NativeItemTextureRegistry.atlasLocation(entry.getValue());
            try {
                // Use Forge's genuine generated-item loader, including vanilla
                // perspective transforms and alpha-edge extrusion, not two quads.
                IModel model = ModelLoaderRegistry.getModel(item.isFull3D() ? HANDHELD : GENERATED)
                        .retexture(ImmutableMap.of("layer0", texture.toString()));
                IBakedModel baked = model.bake(model.getDefaultState(), DefaultVertexFormats.ITEM,
                        sprite -> Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(sprite.toString()));
                event.getModelRegistry().putObject(location, baked);
            } catch(Exception failure) {
                throw new IllegalStateException("Unable to bake declared item texture " + texture + " for " + location, failure);
            }
        }
    }

    private static ModelResourceLocation location(Item item) {
        return new ModelResourceLocation(item.getRegistryName(), "inventory");
    }

    private static boolean eligible(Item item) {
        ResourceLocation name = item.getRegistryName();
        if(name == null || !"hbm".equals(name.getNamespace()) || ForgeRegistries.ITEMS.getValue(name) != item
                || item instanceof ItemBlock) return false;
        // An icon callback is a real custom asset owner, not permission to
        // flatten its dynamic, generated, layered or enum subtype appearance.
        for(Method method : item.getClass().getMethods()) {
            String callback = method.getName();
            if(callback.equals("registerIcons") || callback.equals("getIcon") || callback.equals("getIconFromDamage")
                    || callback.equals("getIconFromDamageForRenderPass") || callback.equals("requiresMultipleRenderPasses")
                    || callback.equals("getRenderPasses") || callback.equals("getColorFromItemStack")) return false;
        }
        return true;
    }

    private static boolean hasCustomRoute(Item item) {
        try {
            Field meshes = ModelLoader.class.getDeclaredField("customMeshDefinitions");
            Field models = ModelLoader.class.getDeclaredField("customModels");
            meshes.setAccessible(true);
            models.setAccessible(true);
            if(((Map<?, ?>)meshes.get(null)).containsKey(item.delegate)) return true;
            for(Object key : ((Map<?, ?>)models.get(null)).keySet())
                if(((Pair<?, ?>)key).getLeft() == item.delegate) return true;
            return false;
        } catch(ReflectiveOperationException failure) {
            throw new IllegalStateException("Cannot verify native item model ownership", failure);
        }
    }
}
