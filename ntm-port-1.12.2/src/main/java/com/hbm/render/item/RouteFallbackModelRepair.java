package com.hbm.render.item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.hbm.main.BakeOwnedModelLoader;
import com.hbm.render.LegacyTexturePath;
import com.hbm.render.item.nativeitem.NativeItemParticles;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.IResource;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Repairs ItemRouteFallback inventory routes at bake time. Those items own no
 * native renderer and no JSON asset; their legacy icon callbacks register atlas
 * sprites under the port's registry-name convention. When a real stitched sprite
 * can be verified for an item (native particle sources first, then its own
 * legacy icon name), a generated item model of that sprite replaces the tracked
 * missing placeholder. Everything that stays unverifiable keeps the honest
 * placeholder instead of a guessed or dummy texture.
 */
@SideOnly(Side.CLIENT)
public final class RouteFallbackModelRepair {

    private static final Map<ModelResourceLocation, Item> ROUTES = new HashMap<>();
    private static boolean registered;

    private RouteFallbackModelRepair() { }

    /** Tracks one fallback route whose model may be repairable at bake time. */
    public static void track(ModelResourceLocation location, Item item) {
        if(location == null || item == null) throw new IllegalArgumentException("Route repair requires a location and an item");
        ROUTES.put(location, item);
        if(!registered) {
            MinecraftForge.EVENT_BUS.register(new RouteFallbackModelRepair());
            registered = true;
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void bake(ModelBakeEvent event) {
        if(ROUTES.isEmpty()) return;
        IBakedModel missing = event.getModelRegistry().getObject(ModelBakery.MODEL_MISSING);
        int repaired = 0;
        List<String> unresolved = new ArrayList<>();
        for(Map.Entry<ModelResourceLocation, Item> entry : ROUTES.entrySet()) {
            IBakedModel current = event.getModelRegistry().getObject(entry.getKey());
            if(!isPlaceholder(current, missing)) continue; // a real owner already supplies the model
            TextureAtlasSprite sprite = NativeItemParticles.resolve(entry.getValue(), current);
            if(sprite == null) sprite = legacyNameSprite(entry.getValue());
            IBakedModel repairedModel = sprite == null ? null : bakeGenerated(entry.getValue(), sprite, entry.getKey());
            if(repairedModel == null) {
                unresolved.add(entry.getKey().toString());
                continue;
            }
            event.getModelRegistry().putObject(entry.getKey(), repairedModel);
            repaired++;
        }
        org.apache.logging.log4j.LogManager.getLogger("hbm").info(
                "Route fallback model repair: repaired={}, unresolved={} of tracked={}",
                repaired, unresolved.size(), ROUTES.size());
        if(!unresolved.isEmpty())
            org.apache.logging.log4j.LogManager.getLogger("hbm").info(
                    "Route fallback items without verifiable textures: {}", unresolved);
    }

    private static boolean isPlaceholder(IBakedModel model, IBakedModel missing) {
        return model == null || model == missing
                || model.getClass().getName().equals("net.minecraftforge.client.model.FancyMissingModel$BakedModel")
                || BakeOwnedModelLoader.isPlaceholder(model);
    }

    /** The port's own icon convention: callbacks register getRegistryName() through
     * the legacy atlas path mapping. Verified against a stitched sprite and a
     * backing PNG, never assumed. */
    private static TextureAtlasSprite legacyNameSprite(Item item) {
        ResourceLocation name = item.getRegistryName();
        if(name == null) return null;
        ResourceLocation spriteName = name.getPath().startsWith("tile.")
                ? LegacyTexturePath.blockAtlasLocation(name.toString())
                : LegacyTexturePath.itemAtlasLocation(name.toString());
        TextureMap atlas = Minecraft.getMinecraft().getTextureMapBlocks();
        TextureAtlasSprite sprite = atlas.getTextureExtry(spriteName.toString());
        if(sprite == null || sprite == atlas.getMissingSprite()) return null;
        ResourceLocation png = new ResourceLocation(spriteName.getNamespace(),
                "textures/" + spriteName.getPath() + ".png");
        try(IResource resource = Minecraft.getMinecraft().getResourceManager().getResource(png)) {
            return sprite;
        } catch(Exception absent) {
            return null;
        }
    }

    private static IBakedModel bakeGenerated(Item item, TextureAtlasSprite sprite, ModelResourceLocation route) {
        try {
            IModel model = ModelLoaderRegistry.getModel(
                    new ResourceLocation("minecraft", item.isFull3D() ? "item/handheld" : "item/generated"))
                    .retexture(ImmutableMap.of("layer0", sprite.getIconName()));
            return model.bake(model.getDefaultState(), DefaultVertexFormats.ITEM,
                    location -> Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(location.toString()));
        } catch(Exception failure) {
            org.apache.logging.log4j.LogManager.getLogger("hbm").warn(
                    "Route fallback repair keeps the placeholder for {}: {}", route, failure.toString());
            return null;
        }
    }
}
