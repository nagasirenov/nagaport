package com.hbm.render.item.nativeitem;

import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Explicit, client-owned native registration, not an adapter for legacy renderer dispatch. */
public final class StaticMeshItemRegistration {
    private final Item item;
    private final TileEntityItemStackRenderer renderer;
    private final Supplier<StaticMeshItemModel> bake;
    private final int[] metadata;
    private ModelResourceLocation location;
    public StaticMeshItemRegistration(Item item, TileEntityItemStackRenderer renderer,
            Supplier<StaticMeshItemModel> bake, int... metadata) {
        this.item = Objects.requireNonNull(item, "item");
        this.renderer = Objects.requireNonNull(renderer, "renderer");
        this.bake = Objects.requireNonNull(bake, "bake");
        if(metadata.length == 0) throw new IllegalArgumentException("Explicit metadata coverage is required");
        this.metadata = metadata.clone();
    }
    @SubscribeEvent public void register(ModelRegistryEvent event) {
        location = modelLocation(item);
        for(int meta : metadata) ModelLoader.setCustomModelResourceLocation(item, meta, location);
    }
    @SubscribeEvent public void bake(ModelBakeEvent event) {
        StaticMeshItemModel model = bake.get();
        requireComplete(model);
        ModelResourceLocation key = location == null ? modelLocation(item) : location;
        // Keep the original native geometry for explicit no-custom GUI draws.
        model = model.withBaseModel(event.getModelRegistry().getObject(key));
        // Check completeness before changing either native registration.
        event.getModelRegistry().putObject(key, model);
        item.setTileEntityItemStackRenderer(renderer);
    }
    public static ModelResourceLocation modelLocation(Item item) {
        if(item.getRegistryName() == null) throw new IllegalStateException("Register the item before its client model");
        return new ModelResourceLocation(item.getRegistryName(), "inventory");
    }
    public static void requireComplete(StaticMeshItemModel model) {
        if(!model.hasEveryNativePerspective())
            throw new IllegalStateException("Incomplete native perspective coverage; do not activate this item handler");
    }
}
