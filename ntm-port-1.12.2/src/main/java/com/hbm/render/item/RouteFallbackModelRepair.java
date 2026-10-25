package com.hbm.render.item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.hbm.main.BakeOwnedModelLoader;
import com.hbm.render.LegacyTexturePath;
import com.hbm.render.item.nativeitem.NativeItemParticles;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.IResource;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
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
 * sprites under the port's registry-name convention. Resolution order per item:
 * native particle sources, the declared item texture, the item's own legacy
 * callbacks (metadata subtypes via getIconFromDamage, layered items via
 * getIcon(stack, pass)), then its own legacy icon name. Everything that stays
 * unverifiable keeps the honest placeholder instead of a guessed texture.
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

    /** LOW, ahead of the LOWEST bake validation, so repaired models are counted as replaced. */
    @SubscribeEvent(priority = EventPriority.LOW)
    public void bake(ModelBakeEvent event) {
        if(ROUTES.isEmpty()) return;
        IBakedModel missing = event.getModelRegistry().getObject(ModelBakery.MODEL_MISSING);
        int repaired = 0;
        List<String> unresolved = new ArrayList<>();
        for(Map.Entry<ModelResourceLocation, Item> entry : ROUTES.entrySet()) {
            IBakedModel current = event.getModelRegistry().getObject(entry.getKey());
            if(!isPlaceholder(current, missing)) continue; // a real owner already supplies the model
            IBakedModel repairedModel = repairModel(entry.getValue());
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

    /** Full resolution chain for one item; null keeps the honest placeholder. */
    private static IBakedModel repairModel(Item item) {
        // Callback owners keep their metadata/NBT (and layered) appearance; they
        // must win over a static declared texture, which would otherwise flatten
        // real variants (soyuz.png would shadow soyuz_0/1/2).
        java.lang.reflect.Method byMeta = declaredCallback(item, "getIconFromDamage", int.class);
        java.lang.reflect.Method byPass = declaredCallback(item, "getIcon", ItemStack.class, int.class);
        if(byMeta != null || byPass != null) {
            IBakedModel callbackModel = callbackModel(item, byMeta, byPass);
            if(callbackModel != null) return callbackModel;
        }

        TextureAtlasSprite sprite = NativeItemParticles.resolve(item, null);
        if(sprite == null) sprite = legacyNameSprite(item);
        if(sprite != null) return bakeGenerated(item,
                Collections.singletonList(sprite), null);
        return null;
    }

    /** Per-variant models driven by the item's real sub-item stacks. */
    private static IBakedModel callbackModel(Item item,
            java.lang.reflect.Method byMeta, java.lang.reflect.Method byPass) {
        // Real stacks (from getSubItems where available) preserve metadata and
        // NBT; molds carry IDs beyond the 4-bit block-metadata range.
        List<ItemStack> resolvedStacks = new ArrayList<>();
        List<List<TextureAtlasSprite>> layersList = new ArrayList<>();
        for(ItemStack stack : subItems(item)) {
            List<TextureAtlasSprite> layers = layersFor(item, byMeta, byPass, stack);
            if(layers != null) {
                resolvedStacks.add(stack);
                layersList.add(layers);
            }
        }
        if(layersList.isEmpty()) return null;

        List<TextureAtlasSprite> base = layersList.get(0);
        IBakedModel baseModel = bakeGenerated(item, base, null);
        if(baseModel == null) return null;
        if(layersList.size() == 1) return baseModel;

        Map<ItemStack, IBakedModel> byStack = new java.util.LinkedHashMap<>();
        for(int i = 0; i < layersList.size(); i++) {
            if(layersList.get(i) == base) continue; // shares the base model
            IBakedModel model = bakeGenerated(item, layersList.get(i), null);
            if(model == null) return null;
            byStack.put(resolvedStacks.get(i), model);
        }
        return new VariantSelectingModel(baseModel, byStack);
    }

    /** The item's actual variant stacks; synthesized metas for tab-less items. */
    private static List<ItemStack> subItems(Item item) {
        net.minecraft.util.NonNullList<ItemStack> list = net.minecraft.util.NonNullList.create();
        for(net.minecraft.creativetab.CreativeTabs tab : net.minecraft.creativetab.CreativeTabs.CREATIVE_TAB_ARRAY)
            item.getSubItems(tab, list);
        if(list.isEmpty()) {
            // No creative exposure (helpers, internal items): cover the plain
            // damage range their callbacks may address.
            for(int meta = 0; meta < 32; meta++) {
                ItemStack stack = new ItemStack(item, 1, meta);
                if(!stack.isEmpty()) list.add(stack);
            }
        }
        return list;
    }

    /** Layer sprites for one real stack; null when no layer resolves. */
    private static List<TextureAtlasSprite> layersFor(Item item,
            java.lang.reflect.Method byMeta, java.lang.reflect.Method byPass, ItemStack stack) {
        try {
            List<TextureAtlasSprite> layers = new ArrayList<>();
            if(byPass != null) {
                for(int pass = 0; pass < 2; pass++) {
                    TextureAtlasSprite sprite = NativeItemParticles.spriteOf(
                            (com.hbm.render.icon.IIcon) byPass.invoke(item, stack, pass));
                    if(sprite == null) break;
                    layers.add(sprite);
                }
            }
            if(layers.isEmpty() && byMeta != null) {
                TextureAtlasSprite sprite = NativeItemParticles.spriteOf(
                        (com.hbm.render.icon.IIcon) byMeta.invoke(item, stack.getMetadata()));
                if(sprite != null) layers.add(sprite);
            }
            return layers.isEmpty() ? null : layers;
        } catch(ReflectiveOperationException | RuntimeException failure) {
            return null;
        }
    }

    /** A legacy callback declared by the item class itself (not by vanilla Item). */
    private static java.lang.reflect.Method declaredCallback(Item item, String name, Class<?>... parameters) {
        for(Class<?> current = item.getClass(); current != null && current != Item.class; current = current.getSuperclass()) {
            try {
                java.lang.reflect.Method method = current.getDeclaredMethod(name, parameters);
                method.setAccessible(true);
                return method;
            } catch(NoSuchMethodException ignored) { }
        }
        return null;
    }

    /** A model that dispatches on the real stack (metadata and NBT) while
     * delegating everything else to the base model. */
    private static final class VariantSelectingModel implements IBakedModel {
        private final IBakedModel base;
        private final Map<ItemStack, IBakedModel> byStack;
        private final ItemOverrideList overrides = new ItemOverrideList(java.util.Collections.emptyList()) {
            @Override public IBakedModel handleItemState(IBakedModel model, ItemStack stack, net.minecraft.world.World world, net.minecraft.entity.EntityLivingBase entity) {
                if(stack == null || stack.isEmpty()) return base;
                for(Map.Entry<ItemStack, IBakedModel> entry : byStack.entrySet())
                    if(ItemStack.areItemStacksEqual(stack, entry.getKey())) return entry.getValue();
                for(Map.Entry<ItemStack, IBakedModel> entry : byStack.entrySet())
                    if(entry.getKey().getMetadata() == stack.getMetadata()) return entry.getValue();
                return base;
            }
        };
        VariantSelectingModel(IBakedModel base, Map<ItemStack, IBakedModel> byStack) {
            this.base = base;
            this.byStack = new HashMap<>(byStack);
        }
        @Override public List<BakedQuad> getQuads(IBlockState state, net.minecraft.util.EnumFacing side, long rand) { return base.getQuads(state, side, rand); }
        @Override public boolean isAmbientOcclusion() { return base.isAmbientOcclusion(); }
        @Override public boolean isGui3d() { return base.isGui3d(); }
        @Override public boolean isBuiltInRenderer() { return base.isBuiltInRenderer(); }
        @Override public net.minecraft.client.renderer.texture.TextureAtlasSprite getParticleTexture() { return base.getParticleTexture(); }
        @Override public ItemCameraTransforms getItemCameraTransforms() { return base.getItemCameraTransforms(); }
        @Override public ItemOverrideList getOverrides() { return overrides; }
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

    /** Generated item model of one or more sprite layers (layer0, layer1). */
    private static IBakedModel bakeGenerated(Item item, List<TextureAtlasSprite> layers, ModelResourceLocation route) {
        try {
            ImmutableMap.Builder<String, String> textures = new ImmutableMap.Builder<>();
            for(int i = 0; i < layers.size(); i++)
                textures.put("layer" + i, layers.get(i).getIconName());
            IModel model = ModelLoaderRegistry.getModel(
                    new ResourceLocation("minecraft", item.isFull3D() ? "item/handheld" : "item/generated"))
                    .retexture(textures.build());
            return model.bake(model.getDefaultState(), DefaultVertexFormats.ITEM,
                    location -> Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(location.toString()));
        } catch(Exception failure) {
            org.apache.logging.log4j.LogManager.getLogger("hbm").warn(
                    "Route fallback repair keeps the placeholder for {}: {}", route, failure.toString());
            return null;
        }
    }
}
